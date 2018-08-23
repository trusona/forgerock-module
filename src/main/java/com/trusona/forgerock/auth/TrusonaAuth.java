package com.trusona.forgerock.auth;

import com.sun.identity.authentication.spi.AMLoginModule;
import com.sun.identity.authentication.spi.AuthLoginException;
import com.sun.identity.shared.datastruct.CollectionHelper;
import com.trusona.client.TrusonaClient;
import com.trusona.client.config.TrusonaClientConfig;
import com.trusona.client.v1.TrusonaClientV1;
import com.trusona.forgerock.auth.authenticator.Trusonaficator;
import com.trusona.forgerock.auth.callback.CallbackFactory;
import com.trusona.forgerock.auth.callback.DefaultCallbackParser;
import com.trusona.forgerock.auth.principal.DefaultPrincipalMapper;
import com.trusona.forgerock.auth.principal.IdentityFinder;
import com.trusona.sdk.Trusona;
import com.trusona.sdk.TrusonaEnvironment;
import com.trusona.sdk.resources.exception.TrusonaException;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.login.LoginException;
import java.net.URL;
import java.security.Principal;
import java.util.Map;
import java.util.Set;

import static com.trusona.forgerock.auth.Constants.CALLBACK_ZERO;
import static com.trusona.forgerock.auth.Constants.ENDPOINT_URL_PRODUCTION;
import static com.trusona.forgerock.auth.Constants.ENDPOINT_URL_UAT;


public class TrusonaAuth extends AMLoginModule {
  private static final String TRUSONA_API_TOKEN = "trusona-api-token";
  private static final String TRUSONA_API_SECRET = "trusona-api-secret";
  private static final String TRUSONA_ACTION = "trusona-action";
  private static final String TRUSONA_RESOURCE = "trusona-resource";
  private static final String TRUSONA_DEEPLINK_URL = "trusona-deeplink-url";

  private TrusonaAuthImpl trusonaAuth;

  @Override
  @SuppressWarnings({"rawtypes", "unchecked"})
  public void init(Subject subject, Map sharedState, Map options) {
    String apiToken = CollectionHelper.getMapAttr(options, TRUSONA_API_TOKEN);
    String apiSecret = CollectionHelper.getMapAttr(options, TRUSONA_API_SECRET);

    String action = CollectionHelper.getMapAttr(options, TRUSONA_ACTION);
    String resource = CollectionHelper.getMapAttr(options, TRUSONA_RESOURCE);

    String deeplinkUrl = CollectionHelper.getMapAttr(options, TRUSONA_DEEPLINK_URL);

    TrusonaEnvironment trusonaEnvironment = new TrusonaEnvResolver().getEnvironment();

    Trusona trusona = new Trusona(apiToken, apiSecret, trusonaEnvironment);
    Set<String> userAliasSet;
    String webSdkConfig;

    try {
      webSdkConfig = trusona.getWebSdkConfig();
    }
    catch (TrusonaException e) {
      TrusonaDebug.getInstance().error("Could not get Web SDK Credentials", e);
      throw new RuntimeException("Could not get Web SDK Credentials", e);
    }

    try {
      userAliasSet = getUserAliasList();
    }
    catch (AuthLoginException e) {
      TrusonaDebug.getInstance().error("Failed to obtain UserAliasSet", e);
      throw new RuntimeException("Failed to obtain UserAliasSet", e);
    }

    TrusonaClientConfig trusonaClientConfig = new TrusonaClientConfig();
    trusonaClientConfig.setAccessToken(apiToken);
    trusonaClientConfig.setMacKey(apiSecret);
    trusonaClientConfig.setEndpointUrl(getEndpointUrl(trusonaEnvironment));

    TrusonaClient trusonaClient = new TrusonaClientV1(trusonaClientConfig);

    this.trusonaAuth = new TrusonaAuthImpl(
      new Trusonaficator(trusona, action, resource),
      new DefaultCallbackParser(),
      new CallbackFactory(webSdkConfig, deeplinkUrl, CALLBACK_ZERO),
      new DefaultPrincipalMapper(trusonaClient, new IdentityFinder(userAliasSet, getRequestOrg())),
      this::replaceCallback
    );
  }

  @Override
  public int process(Callback[] callbacks, int state) throws LoginException {
    return trusonaAuth.process(callbacks, state);
  }

  @Override
  public Principal getPrincipal() {
    return trusonaAuth.getPrincipal();
  }

  private URL getEndpointUrl(TrusonaEnvironment trusonaEnvironment) {
    switch (trusonaEnvironment) {
      case UAT:
        return ENDPOINT_URL_UAT;

      case PRODUCTION:
        return ENDPOINT_URL_PRODUCTION;

      default:
        throw new RuntimeException("Invalid Trusona environment configured: " + trusonaEnvironment);
    }
  }
}
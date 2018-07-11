package com.trusona.forgerock.auth;

import com.sun.identity.authentication.spi.AMLoginModule;
import com.sun.identity.shared.datastruct.CollectionHelper;
import com.trusona.forgerock.auth.authenticator.Trusonaficator;
import com.trusona.forgerock.auth.callback.DefaultCallbackParser;
import com.trusona.forgerock.auth.principal.DefaultPrincipalMapper;
import com.trusona.sdk.Trusona;
import com.trusona.sdk.resources.exception.TrusonaException;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.login.LoginException;
import java.security.Principal;
import java.util.Map;


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

    Trusona trusona = new Trusona(apiToken, apiSecret, new TrusonaEnvResolver().getEnvironment());
    String webSdkConfig;

    try {
      webSdkConfig = trusona.getWebSdkConfig();
    }
    catch (TrusonaException e) {
      TrusonaDebug.getInstance().error("Could not get Web SDK Credentials", e);
      throw new RuntimeException("Could not get Web SDK Credentials", e);
    }

    this.trusonaAuth = new TrusonaAuthImpl(
      new Trusonaficator(trusona, action, resource),
      new DefaultCallbackParser(webSdkConfig, deeplinkUrl, "callback_0"),
      new DefaultPrincipalMapper(),
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
}
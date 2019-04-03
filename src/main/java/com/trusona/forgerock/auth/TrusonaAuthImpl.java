package com.trusona.forgerock.auth;

import com.sun.identity.authentication.callbacks.HiddenValueCallback;
import com.sun.identity.authentication.spi.AuthLoginException;
import com.sun.identity.authentication.util.ISAuthConstants;
import com.trusona.forgerock.auth.authenticator.Authenticator;
import com.trusona.forgerock.auth.callback.CallbackFactory;
import com.trusona.forgerock.auth.callback.CallbackParser;
import com.trusona.forgerock.auth.callback.TrusonaCallback;
import com.trusona.forgerock.auth.principal.PrincipalMapper;
import java.security.Principal;
import java.util.UUID;
import javax.security.auth.callback.Callback;
import javax.security.auth.login.LoginException;
import org.apache.commons.lang3.StringUtils;

public class TrusonaAuthImpl {

  private Authenticator authenticator;
  private CallbackParser callbackParser;
  private CallbackFactory callbackFactory;
  private PrincipalMapper principalMapper;
  private Principal principal;
  private UUID trusonaficationId;
  private TriConsumer<Integer, Integer, Callback> callbackUpdate;

  public static final int TRUCODE_STATE = 2;
  public static final int IDENTIFIER_STATE = 3;
  public static final int COMPLETION_STATE = 4;
  public static final int REDIRECT_TO_DEEPLINK = 5;

  public TrusonaAuthImpl(Authenticator authenticator, CallbackParser callbackParser, CallbackFactory callbackFactory,
                         PrincipalMapper principalMapper, TriConsumer<Integer, Integer, Callback> callbackUpdate) {
    this.authenticator = authenticator;
    this.callbackParser = callbackParser;
    this.callbackFactory = callbackFactory;
    this.principalMapper = principalMapper;
    this.callbackUpdate = callbackUpdate;
    this.principal = null;
    this.trusonaficationId = null;
  }

  public int process(Callback[] callbacks, int state) throws LoginException {
    TrusonaDebug.getInstance().message("Process called with {} callbacks in state {}", callbacks.length, state);

    switch (state) {
      case ISAuthConstants.LOGIN_START:
        callbackUpdate.accept(TRUCODE_STATE, 0, callbackFactory.makeScriptCallback("app.run();"));
        return TRUCODE_STATE;

      case TRUCODE_STATE:
        TrusonaDebug.getInstance().message("In TRUCODE_STATE with {} callbacks", callbacks.length);

        String trucodeId = callbackParser.getCallbackValue((HiddenValueCallback) callbacks[1]);
        String error = callbackParser.getCallbackValue((HiddenValueCallback) callbacks[2]);
        String payload = callbackParser.getCallbackValue((HiddenValueCallback) callbacks[3]);
        String savedTrusonaficationIdString = callbackParser.getCallbackValue((HiddenValueCallback) callbacks[4]);

        if (StringUtils.isNotBlank(error)) {
          throw new AuthLoginException(String.format("Error from TruCode SDK: %s", error));
        }

        if (StringUtils.isBlank(trucodeId) && StringUtils.isBlank(savedTrusonaficationIdString)) {
          return IDENTIFIER_STATE;
        }

        if (StringUtils.isNotBlank(savedTrusonaficationIdString)) {
          try {
            trusonaficationId = UUID.fromString(savedTrusonaficationIdString);
          }
          catch (IllegalArgumentException e) {
            TrusonaDebug.getInstance().error("Oops!", e);
          }
        }

        if (trusonaficationId == null) {
          trusonaficationId = createTrusonaficationFromCallbacks(callbacks);
        }

        if (StringUtils.isNotBlank(payload)) {
          callbackUpdate.accept(REDIRECT_TO_DEEPLINK, 0, callbackFactory
            .makeScriptCallback(String.format("app.saveTrusonaficationCookie('%s');", trusonaficationId)));

          callbackUpdate.accept(REDIRECT_TO_DEEPLINK, 1, callbackFactory.makeRedirectCallback(payload));
          return REDIRECT_TO_DEEPLINK;
        }

        return COMPLETION_STATE;

      case REDIRECT_TO_DEEPLINK:
        TrusonaDebug.getInstance().message("In REDIRECT_TO_DEEPLINK state");
        return COMPLETION_STATE;

      case IDENTIFIER_STATE:
        TrusonaDebug.getInstance().message("In IDENTIFIER_STATE with {} callbacks", callbacks.length);
        trusonaficationId = createTrusonaficationFromCallbacks(callbacks);
        return COMPLETION_STATE;

      case COMPLETION_STATE:
        TrusonaDebug.getInstance().message("In COMPLETION_STATE with trusonaficationId {}", trusonaficationId);

        principal = principalMapper.mapPrincipal(authenticator.getTrusonaficationResult(trusonaficationId))
          .orElseThrow(() -> new AuthLoginException("Could not get a principal from Trusonafication Result"));

        return ISAuthConstants.LOGIN_SUCCEED;

      default:
        throw new AuthLoginException("Unexpected state");
    }
  }

  public Principal getPrincipal() {
    return principal;
  }

  private UUID createTrusonaficationFromCallbacks(Callback[] callbacks) throws AuthLoginException {
    TrusonaCallback trusonaCallback = callbackParser.getTrusonaCallback(callbacks)
      .filter(TrusonaCallback::isValid)
      .orElseThrow(() -> new AuthLoginException("Got invalid callbacks in initial state"));

    return authenticator.createTrusonafication(trusonaCallback);
  }
}
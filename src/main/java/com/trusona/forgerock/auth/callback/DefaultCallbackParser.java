package com.trusona.forgerock.auth.callback;


import com.sun.identity.authentication.callbacks.HiddenValueCallback;
import com.sun.identity.authentication.callbacks.ScriptTextOutputCallback;
import com.sun.identity.authentication.spi.RedirectCallback;
import com.trusona.forgerock.auth.TrusonaDebug;
import okio.Okio;
import org.apache.commons.lang3.StringUtils;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.ChoiceCallback;
import javax.security.auth.callback.NameCallback;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

public class DefaultCallbackParser implements CallbackParser {
  private static final String USER_IDENTIFIER = "user_identifier";
  private static final String DEVICE_IDENTIFIER = "device_identifier";
  private static final String TRUCODE_ID = "trucode_id";
  private static final String JS_RESOURCE = "main.bundle.js";
  private static final String HTTP_GET = "GET";
  private static final String DEFAULT_DEEPLINK_URL = "https://trusona-app.net/idp";

  private final String webSdkConfig;
  private final String deeplinkUrl;
  private final String trucodeElementId;

  public DefaultCallbackParser(String webSdkConfig, String deeplinkUrl, String trucodeElementId) {
    this.webSdkConfig = webSdkConfig;
    this.trucodeElementId = trucodeElementId;
    this.deeplinkUrl = Optional.ofNullable(deeplinkUrl)
      .filter(StringUtils::isNotBlank)
      .orElse(DEFAULT_DEEPLINK_URL);
  }

  @Override
  public Optional<TrusonaCallback> getTrusonaCallback(Callback[] callbacks) {
    TrusonaCallback trusonaCallback = null;

    if (callbacks.length == 2) {
      NameCallback identifierCallback = (NameCallback) callbacks[0];
      ChoiceCallback typeCallback = (ChoiceCallback) callbacks[1];

      String identifierType = typeCallback.getChoices()[typeCallback.getSelectedIndexes()[0]];

      TrusonaDebug.getInstance().message(String.format("Got an identifier of %s and a type of %s", identifierCallback.getName(), identifierType));

      switch (identifierType) {
        case USER_IDENTIFIER:
          trusonaCallback = new UserIdentifierCallback(identifierCallback.getName());
          break;

        case DEVICE_IDENTIFIER:
          trusonaCallback = new DeviceIdentifierCallback(identifierCallback.getName());
          break;

        case TRUCODE_ID:
          try {
            trusonaCallback = new TrucodeIdCallback(UUID.fromString(identifierCallback.getName()));
          }
          catch (IllegalArgumentException e) {
            TrusonaDebug.getInstance().error("Trucode was not a UUID", e);
          }
          break;
      }
    }
    else if (callbacks.length == 5) {
      HiddenValueCallback trucodeIdCallback = (HiddenValueCallback) callbacks[1];
      UUID trucodeId = UUID.fromString(trucodeIdCallback.getValue());

      try {
        trusonaCallback = new TrucodeIdCallback(trucodeId);
      }
      catch (IllegalArgumentException e) {
        TrusonaDebug.getInstance().error("Trucode was not a UUID", e);
      }
    }

    return Optional.ofNullable(trusonaCallback);
  }

  @Override
  public ScriptTextOutputCallback getScriptCallback(String command) {
    StringBuilder scriptBuilder = new StringBuilder();

    try {
      scriptBuilder.append(Okio.buffer(Okio.source(getClass().getClassLoader().getResourceAsStream(JS_RESOURCE))).readUtf8());
    }
    catch (IOException e) {
      TrusonaDebug.getInstance().error("Error loading javascript", e);
    }

    scriptBuilder.append("\n");
    scriptBuilder.append(String.format("var app = new TrusonaFR(%s, '%s', '%s', jQuery);%n", webSdkConfig, deeplinkUrl, trucodeElementId));
    scriptBuilder.append(command);

    return new ScriptTextOutputCallback(scriptBuilder.toString());
  }

  @Override
  public RedirectCallback getRedirectCallback(String payload) {
    TrusonaDebug.getInstance().message("Building Redirect Callback with deeplinkUrl => {} payload => {}", deeplinkUrl, payload);

    return new RedirectCallback(
      String.format("%s?payload=%s", deeplinkUrl, payload),
      Collections.singletonMap("payload", payload),
      HTTP_GET);
  }

  @Override
  public String getCallbackValue(HiddenValueCallback callback) {
    return callback.getId().equals(callback.getValue()) ? "" : callback.getValue();
  }
}
package com.trusona.forgerock.node;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.sun.identity.sm.RequiredValueValidator;
import com.trusona.forgerock.auth.TrusonaEnvResolver;
import com.trusona.forgerock.auth.authenticator.Trusonaficator;
import com.trusona.forgerock.auth.callback.CallbackFactory;
import com.trusona.sdk.Trusona;
import com.trusona.sdk.TrusonaEnvironment;
import com.trusona.sdk.resources.exception.TrusonaException;
import org.forgerock.guava.common.collect.ImmutableList;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.annotations.sm.Attribute;
import org.forgerock.openam.auth.node.api.*;
import org.forgerock.openam.core.CoreWrapper;
import org.forgerock.openam.sm.annotations.adapters.Password;
import org.forgerock.util.i18n.PreferredLocales;

import java.util.List;

import static com.trusona.forgerock.node.Constants.CALLBACK_ZERO;
import static com.trusona.forgerock.node.TrusonaOutcomes.*;

@Node.Metadata(outcomeProvider = TrusonaDecisionNode.TrusonaOutcomeProvider.class,
  configClass = TrusonaDecisionNode.Config.class)
public class TrusonaDecisionNode implements Node {
  private final Config config;
  private final CoreWrapper coreWrapper;
  private final StateDelegate stateDelegate;


  @Inject
  public TrusonaDecisionNode(@Assisted Config config, CoreWrapper coreWrapper) {
    this.config = config;
    this.coreWrapper = coreWrapper;

    TrusonaEnvironment trusonaEnvironment = new TrusonaEnvResolver().getEnvironment();
    Trusona            trusona  = new Trusona(config.apiToken(), config.apiSecret(), trusonaEnvironment);

    String webSdkConfig;

    try {
      webSdkConfig = trusona.getWebSdkConfig();
    }
    catch (TrusonaException e) {
      throw new RuntimeException("Could not get Web SDK Config. Please verify your Trusona API Token", e);
    }

    stateDelegate = new StateDelegate(
      new CallbackFactory(webSdkConfig, config.deeplinkUrl(), CALLBACK_ZERO),
      new Trusonaficator(trusona, config.action(), config.resource()),
      trusona
      );
  }

  @Override
  public Action process(TreeContext treeContext) throws NodeProcessException {
    return stateDelegate.getState(treeContext).get();
  }

  interface Config {

    @Attribute(order = 100, validators = {RequiredValueValidator.class})
    String apiToken();

    @Attribute(order = 200, validators = {RequiredValueValidator.class})
    @Password
    String apiSecret();

    @Attribute(order = 300, validators = {RequiredValueValidator.class})
    String action();

    @Attribute(order = 400, validators = {RequiredValueValidator.class})
    String resource();

    @Attribute(order = 500)
    String deeplinkUrl();

  }
  public static class TrusonaOutcomeProvider implements OutcomeProvider {
    @Override
    public List<Outcome> getOutcomes(PreferredLocales preferredLocales, JsonValue jsonValue) {
      //TODO: Localization
      return ImmutableList.of(
        ACCEPTED_OUTCOME,
        REJECTED_OUTCOME,
        EXPIRED_OUTCOME,
        ERROR_OUTCOME
      );
    }
  }
}

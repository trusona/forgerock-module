package com.trusona.forgerock.node;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.sun.identity.sm.RequiredValueValidator;
import com.trusona.forgerock.auth.TrusonaEnvResolver;
import com.trusona.forgerock.auth.authenticator.Authenticator;
import com.trusona.forgerock.auth.callback.CallbackParser;
import com.trusona.forgerock.auth.callback.DefaultCallbackParser;
import com.trusona.sdk.Trusona;
import com.trusona.sdk.TrusonaEnvironment;
import com.trusona.sdk.resources.dto.TrusonaficationStatus;
import org.forgerock.guava.common.collect.ImmutableList;
import org.forgerock.json.JsonValue;
import org.forgerock.openam.annotations.sm.Attribute;
import org.forgerock.openam.auth.node.api.*;
import org.forgerock.openam.core.CoreWrapper;
import org.forgerock.openam.sm.annotations.adapters.Password;
import org.forgerock.util.i18n.PreferredLocales;

import java.util.List;

@Node.Metadata(outcomeProvider = TrusonaDecisionNode.TrusonaOutcomeProvider.class,
  configClass = TrusonaDecisionNode.Config.class)
public class TrusonaDecisionNode implements Node {
  private final Config config;
  private final CoreWrapper coreWrapper;

  @Inject
  public TrusonaDecisionNode(@Assisted Config config, CoreWrapper coreWrapper) {
    this.config = config;
    this.coreWrapper = coreWrapper;

    TrusonaEnvironment trusonaEnvironment = new TrusonaEnvResolver().getEnvironment();
    Trusona            trusona  = new Trusona(config.apiToken(), config.apiSecret(), trusonaEnvironment);

  }

  @Override
  public Action process(TreeContext treeContext) throws NodeProcessException {
    if (treeContext.hasCallbacks()) {
      //Not the initial entry.
    } else {
      //Our first time. Send callbacks
      return Action.send(

      )
    }
    return null;
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
        new Outcome(TrusonaficationStatus.ACCEPTED.name(), "Accepted"),
        new Outcome(TrusonaficationStatus.EXPIRED.name(), "Expired"),
        new Outcome(TrusonaficationStatus.REJECTED.name(), "Rejected"),
        new Outcome(TrusonaficationStatus.INVALID.name(), "Invalid")
      );
    }
  }
}

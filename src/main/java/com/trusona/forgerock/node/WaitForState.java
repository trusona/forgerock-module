package com.trusona.forgerock.node;

import com.sun.identity.shared.debug.Debug;
import com.trusona.sdk.resources.TrusonaApi;
import com.trusona.sdk.resources.dto.TrusonaficationResult;
import com.trusona.sdk.resources.exception.TrusonaException;
import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.authentication.callbacks.PollingWaitCallback;

import java.util.UUID;
import java.util.function.Supplier;

import static com.trusona.forgerock.node.TrusonaOutcomes.*;
import static com.trusona.sdk.resources.dto.TrusonaficationStatus.EXPIRED;
import static com.trusona.sdk.resources.dto.TrusonaficationStatus.IN_PROGRESS;
import static com.trusona.sdk.resources.dto.TrusonaficationStatus.REJECTED;

public class WaitForState implements Supplier<Action> {
  private static final String WAIT_TIME = "5000";

  private final TrusonaApi trusona;
  private final UUID       trusonaficationId;
  private final Debug      debug;

  public WaitForState(TrusonaApi trusona, UUID trusonaficationId, Debug debug) {
    this.trusona = trusona;
    this.trusonaficationId = trusonaficationId;
    this.debug = debug;
  }

  @Override
  public Action get() {
    try {
     return actionForResult(trusona.getTrusonaficationResult(trusonaficationId)).build();
    }
    catch (TrusonaException e) {
      debug.error("Got a Trusona API exception when trying to get TrusonaficationResult", e);
      return Action.goTo(ERROR_OUTCOME.id).build();
    }
  }

  private Action.ActionBuilder actionForResult(TrusonaficationResult result) {
    if (result.isSuccessful()) {
      return Action.goTo(ACCEPTED_OUTCOME.id);
    } else if (result.getStatus().equals(REJECTED)) {
      return Action.goTo(REJECTED_OUTCOME.id);
    } else if (result.getStatus().equals(EXPIRED)) {
      return Action.goTo(EXPIRED_OUTCOME.id);
    } else if (result.getStatus().equals(IN_PROGRESS)) {
      return Action.send(new PollingWaitCallback(WAIT_TIME));
    } else {
      debug.error("Got an unexpected Trusonafication Result: " + result.toString());
      return Action.goTo(ERROR_OUTCOME.id);
    }
  }
}

package com.trusona.forgerock.node;

import com.sun.identity.authentication.callbacks.HiddenValueCallback;
import com.trusona.forgerock.auth.callback.CallbackFactory;
import org.forgerock.openam.auth.node.api.Action;

import java.util.function.Supplier;

public class InitialState implements Supplier<Action> {
  private final CallbackFactory callbackFactory;

  public InitialState(CallbackFactory callbackFactory) {
    this.callbackFactory = callbackFactory;
  }

  @Override
  public Action get() {
    return Action.send(
      callbackFactory.makeScriptCallback("app.run();"),
      new HiddenValueCallback("trucodeId"),
      new HiddenValueCallback("error"),
      new HiddenValueCallback("payload"),
      new HiddenValueCallback("trusonaficationId")
    ).build();
  }
}

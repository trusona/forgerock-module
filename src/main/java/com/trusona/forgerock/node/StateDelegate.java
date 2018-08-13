package com.trusona.forgerock.node;

import com.sun.identity.authentication.callbacks.HiddenValueCallback;
import com.trusona.forgerock.auth.authenticator.Authenticator;
import com.trusona.forgerock.auth.callback.CallbackFactory;
import com.trusona.forgerock.auth.callback.DefaultCallbackParser;
import com.trusona.sdk.resources.TrusonaApi;
import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.TreeContext;

import javax.security.auth.callback.Callback;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class StateDelegate {
  private final CallbackFactory       callbackFactory;
  private final Authenticator         authenticator;
  private final TrusonaApi            trusona;

  public StateDelegate(CallbackFactory callbackFactory, Authenticator authenticator, TrusonaApi trusona) {
    this.callbackFactory = callbackFactory;
    this.authenticator = authenticator;
    this.trusona = trusona;
  }

  public Supplier<Action> getState(TreeContext treeContext) {

    if (treeContext.sharedState.isDefined("trusonaficationId")) {
      UUID trusonaficationId = UUID.fromString(treeContext.sharedState.get("trusonaficationId").asString());
      return new WaitForState(trusona, trusonaficationId);
    }

    List<? extends Callback> callbackList = treeContext.getAllCallbacks();
    if (callbackList.size() == 5) {
      Optional<String> trucodeCallback = getHiddenValueCallback(treeContext, "trucode_id");
      Optional<String> payloadCallback = getHiddenValueCallback(treeContext, "payload");
      Optional<String> errorCallback = getHiddenValueCallback(treeContext, "error");


      Optional<Supplier<Action>> state = errorCallback.map(ErrorState::new);

      return state.orElse(new TrucodeState(authenticator, callbackFactory, treeContext.sharedState,
        UUID.fromString(trucodeCallback.get()),
        payloadCallback.orElse(null)));
    }
    return new InitialState(callbackFactory);
  }

  private Optional<String> getHiddenValueCallback(TreeContext treeContext, String id) {
    return treeContext.getAllCallbacks().stream()
      .filter(cb -> cb instanceof HiddenValueCallback)
      .map(cb -> (HiddenValueCallback) cb)
      .filter(cb -> cb.getId() == id)
      .filter(cb -> ! cb.getId().equals(cb.getValue())) //Bug in ForgeRock that sets value == id when there is no value
      .map(cb -> Optional.ofNullable(cb.getValue()))
      .flatMap(o -> o.map(Stream::of).orElse(Stream.empty()))
      .findFirst();
  }
}
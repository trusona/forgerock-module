package com.trusona.forgerock.node;

import com.sun.identity.authentication.callbacks.HiddenValueCallback;
import com.sun.identity.shared.debug.Debug;
import com.trusona.forgerock.auth.TrusonaDebug;
import com.trusona.forgerock.auth.authenticator.Authenticator;
import com.trusona.forgerock.auth.callback.CallbackFactory;
import com.trusona.sdk.resources.TrusonaApi;
import org.apache.commons.lang3.StringUtils;
import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.TreeContext;

import javax.security.auth.callback.Callback;
import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.trusona.forgerock.node.Constants.*;

public class StateDelegate {
  private final CallbackFactory       callbackFactory;
  private final Authenticator         authenticator;
  private final TrusonaApi            trusona;
  private final Debug                  debug;

  public StateDelegate(CallbackFactory callbackFactory, Authenticator authenticator, TrusonaApi trusona) {
    this.callbackFactory = callbackFactory;
    this.authenticator = authenticator;
    this.trusona = trusona;
    this.debug = TrusonaDebug.getInstance();
  }

  public Supplier<Action> getState(TreeContext treeContext) {
    debug.message("in getState() with {} callbacks", treeContext.getAllCallbacks().size());
    debug.message("sharedState => {}", treeContext.sharedState);

    if (treeContext.sharedState.isDefined(TRUSONAFICATION_ID)) {
      Optional<UUID> trusonaficationId = parseUUID(treeContext.sharedState.get(TRUSONAFICATION_ID).asString());

      return trusonaficationId
        .map(t -> (Supplier<Action>) new WaitForState(trusona,t))
        .orElse(new ErrorState("A trusonafication ID was saved in the session state, but it is not a valid UUID"));
    }

    Supplier<Action> state = new ErrorState("We received unexpected input. Please try again.");

    List<? extends Callback> callbackList = treeContext.getAllCallbacks();
    if (callbackList.isEmpty()) {
      state = new InitialState(callbackFactory);
    } else if (callbackList.size() == 5) {
      Optional<String> errorCallback = getHiddenValueCallback(treeContext, ERROR)
        .filter(StringUtils::isNotBlank);

      if (errorCallback.isPresent()) {
        return new ErrorState(errorCallback.get());
      }

      String payload = getHiddenValueCallback(treeContext, PAYLOAD)
        .orElse(null);

      Optional<UUID> trucodeId = getHiddenValueCallback(treeContext, TRUCODE_ID)
        .flatMap(this::parseUUID);

      debug.message("trucode_id => {}", trucodeId.map(UUID::toString).orElse("EMPTY"));
      debug.message("error => {}", errorCallback.orElse("EMPTY"));
      debug.message("payload => {}", Optional.ofNullable(payload).orElse("EMPTY"));
      debug.message("trusonafication_id => {}", getHiddenValueCallback(treeContext, TRUSONAFICATION_ID).orElse("EMPTY"));

      if (trucodeId.isPresent()) {
        state = new TrucodeState(authenticator, callbackFactory, treeContext.sharedState, trucodeId.get(), payload);
      }
    }

    return state;
  }

  private Optional<String> getHiddenValueCallback(TreeContext treeContext, String id) {
    return treeContext.getAllCallbacks().stream()
      .filter(cb -> cb instanceof HiddenValueCallback)
      .map(cb -> (HiddenValueCallback) cb)
      .filter(cb -> hasId(cb, id))
      .filter(this::valueIsNotId) //Bug in ForgeRock that sets value == id when there is no value
      .map(cb -> Optional.ofNullable(cb.getValue()))
      .flatMap(o -> o.map(Stream::of).orElse(Stream.empty()))
      .findFirst();
  }

  private Optional<UUID> parseUUID(String s) {
    Optional<UUID> uuid = Optional.empty();

    try {
      uuid = Optional.of(UUID.fromString(s));
    } catch (IllegalArgumentException e) {
      TrusonaDebug.getInstance().error("Error parsing UUID", e);
    }

    return uuid;
  }

  private boolean hasId(HiddenValueCallback cb, String id) {
    boolean result = cb.getId().equals(id);
    debug.message("HiddenValueCallback[id={}, value={}] hasId => {}", cb.getId(), cb.getValue(), result);
    return result;
  }

  private boolean valueIsNotId(HiddenValueCallback cb) {
    boolean result = ! cb.getId().equals(cb.getValue());
    debug.message("HiddenValueCallback[id={}, value={}] valueIsNotId => {}", cb.getId(), cb.getValue(), result);
    return result;
  }
}
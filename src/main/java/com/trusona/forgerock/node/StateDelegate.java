package com.trusona.forgerock.node;

import com.sun.identity.authentication.callbacks.HiddenValueCallback;
import com.trusona.forgerock.auth.authenticator.Authenticator;
import com.trusona.forgerock.auth.callback.CallbackFactory;
import com.trusona.forgerock.auth.callback.DefaultCallbackParser;
import org.forgerock.openam.auth.node.api.Action;
import org.forgerock.openam.auth.node.api.TreeContext;

import javax.security.auth.callback.Callback;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class StateDelegate {
    CallbackFactory callbackFactory;
    Authenticator authenticator;
    DefaultCallbackParser defaultCallbackParser;

    StateDelegate(CallbackFactory callbackFactory, Authenticator authenticator,
                  DefaultCallbackParser defaultCallbackParser){
        this.callbackFactory = callbackFactory;
        this.authenticator = authenticator;
        this.defaultCallbackParser = defaultCallbackParser;
    }

    public Supplier<Action> getState(TreeContext treeContext){

        List<? extends Callback> callbackList = treeContext.getAllCallbacks();
        if(callbackList.size() == 5){

           Optional<HiddenValueCallback> trucodeCallback = callbackList.stream()
                    .filter(cb -> cb instanceof HiddenValueCallback)
                    .map(cb -> (HiddenValueCallback) cb)
                    .filter(cb -> cb.getId() == "trucode_id")
                    .findFirst();

            Optional<HiddenValueCallback> payloadCallback = callbackList.stream()
                    .filter(cb -> cb instanceof HiddenValueCallback)
                    .map(cb -> (HiddenValueCallback) cb)
                    .filter(cb -> cb.getId() == "payload")
                    .findFirst();


            return new TrucodeState(authenticator, callbackFactory, treeContext.sharedState,
                    UUID.fromString(defaultCallbackParser.getCallbackValue(trucodeCallback.get())),
                    defaultCallbackParser.getCallbackValue(payloadCallback.get()));
        }
        return new InitialState(callbackFactory);
    }
}
package com.trusona.forgerock.node

import com.sun.identity.authentication.callbacks.HiddenValueCallback
import com.sun.identity.authentication.callbacks.ScriptTextOutputCallback
import com.trusona.forgerock.auth.authenticator.Authenticator
import com.trusona.forgerock.auth.callback.CallbackFactory
import com.trusona.forgerock.auth.callback.DefaultCallbackParser
import com.trusona.sdk.resources.TrusonaApi
import org.forgerock.json.JsonValue
import org.forgerock.openam.auth.node.api.ExternalRequestContext
import org.forgerock.openam.auth.node.api.TreeContext
import spock.lang.Specification

class StateDelegateSpec extends Specification {

  StateDelegate sut

  def setup() {
    def callbackFactory = Mock(CallbackFactory)
    def authenticator = Mock(Authenticator)
    def trusona = Mock(TrusonaApi)

    sut = new StateDelegate(callbackFactory, authenticator, trusona)
  }

  def "should send initial state for new request"() {
    given:
    def jsonValue = new JsonValue([:])
    def externalRequestContext = new ExternalRequestContext.Builder().build()

    def treeContext = new TreeContext(jsonValue, externalRequestContext, [])

    when:
    def state = sut.getState(treeContext)

    then:
    state instanceof InitialState
  }

  def "should send trucode state after initial state"() {
    given:
    def uuid = UUID.randomUUID()
    def jsonValue = new JsonValue([:])
    def externalRequestContext = new ExternalRequestContext.Builder().build()


    def callbackList = [new ScriptTextOutputCallback("callback"),
                        new HiddenValueCallback("trucode_id", uuid.toString()),
                        new HiddenValueCallback("error"),
                        new HiddenValueCallback("payload"),
                        new HiddenValueCallback("trusonafication_id")]
    def treeContext = new TreeContext(jsonValue, externalRequestContext, callbackList)

    when:
    def state = sut.getState(treeContext)

    then:
    state instanceof TrucodeState
  }

  def "should send WaitForState when we have a trusonafication id"() {
    given:
    def trusonaficationId = UUID.randomUUID()
    def jsonValue = new JsonValue([ trusonaficationId: trusonaficationId.toString() ])
    def externalRequestContext = new ExternalRequestContext.Builder().build()

    def treeContext = new TreeContext(jsonValue, externalRequestContext, [])

    when:
    def state = sut.getState(treeContext)

    then:
    state instanceof WaitForState
  }

  def "should send ErrorState when we get a client side error"() {
    given:
    def uuid = UUID.randomUUID()
    def jsonValue = new JsonValue([:])
    def externalRequestContext = new ExternalRequestContext.Builder().build()


    def callbackList = [new ScriptTextOutputCallback("callback"),
                        new HiddenValueCallback("trucode_id", uuid.toString()),
                        new HiddenValueCallback("error", "some error"),
                        new HiddenValueCallback("payload"),
                        new HiddenValueCallback("trusonafication_id")]
    def treeContext = new TreeContext(jsonValue, externalRequestContext, callbackList)

    when:
    def state = sut.getState(treeContext)

    then:
    state instanceof ErrorState
  }
}
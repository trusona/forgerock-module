package com.trusona.forgerock.node

import com.sun.identity.authentication.callbacks.HiddenValueCallback
import com.sun.identity.authentication.callbacks.ScriptTextOutputCallback
import com.trusona.forgerock.auth.authenticator.Authenticator
import com.trusona.forgerock.auth.callback.CallbackFactory
import com.trusona.forgerock.auth.callback.DefaultCallbackParser
import org.forgerock.json.JsonValue
import org.forgerock.openam.auth.node.api.ExternalRequestContext
import org.forgerock.openam.auth.node.api.TreeContext
import spock.lang.Specification

class StateDelegateSpec extends Specification {

  StateDelegate sut

  def setup() {
    def callbackFactory = Mock(CallbackFactory)
    def authenticator = Mock(Authenticator)
    def defaultCallbackParser = new DefaultCallbackParser()
    sut = new StateDelegate(callbackFactory, authenticator, defaultCallbackParser)
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

  def "should send trucode state for new request"() {
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
}
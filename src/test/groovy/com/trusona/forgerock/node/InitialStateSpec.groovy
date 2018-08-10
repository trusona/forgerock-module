package com.trusona.forgerock.node

import com.sun.identity.authentication.callbacks.HiddenValueCallback
import com.sun.identity.authentication.callbacks.ScriptTextOutputCallback
import com.trusona.forgerock.auth.callback.CallbackFactory
import com.trusona.forgerock.auth.callback.CallbackParser
import retrofit2.Call
import spock.lang.Specification

class InitialStateSpec extends Specification {

  CallbackFactory callbackFactory = Mock(CallbackFactory)

  InitialState sut = new InitialState(callbackFactory)

  def "get() should send our initial callbacks"() {
    given:
    callbackFactory.makeScriptCallback(_ as String) >> { args -> new ScriptTextOutputCallback((String) args[0]) }

    when:
    def res = sut.get()

    then:
    res.sendingCallbacks()
    res.callbacks.findIndexOf { cb -> cb instanceof ScriptTextOutputCallback && cb.message == "app.run();" } == 0
    res.callbacks.findIndexOf { cb -> cb instanceof HiddenValueCallback && cb.id == "trucodeId" } == 1
    res.callbacks.findIndexOf { cb -> cb instanceof HiddenValueCallback && cb.id == "error" } == 2
    res.callbacks.findIndexOf { cb -> cb instanceof HiddenValueCallback && cb.id == "payload" } == 3
    res.callbacks.findIndexOf { cb -> cb instanceof HiddenValueCallback && cb.id == "trusonaficationId" } == 4
  }
}

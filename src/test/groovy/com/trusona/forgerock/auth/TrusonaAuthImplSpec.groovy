package com.trusona.forgerock.auth

import com.sun.identity.authentication.callbacks.HiddenValueCallback
import com.sun.identity.authentication.callbacks.ScriptTextOutputCallback
import com.sun.identity.authentication.internal.AuthPrincipal
import com.sun.identity.authentication.spi.AuthLoginException
import com.sun.identity.authentication.spi.RedirectCallback
import com.sun.identity.authentication.util.ISAuthConstants
import com.trusona.forgerock.auth.authenticator.Authenticator
import com.trusona.forgerock.auth.callback.CallbackParser
import com.trusona.forgerock.auth.callback.TrusonaCallback
import com.trusona.forgerock.auth.principal.PrincipalMapper
import com.trusona.sdk.resources.dto.TrusonaficationResult
import com.trusona.sdk.resources.dto.TrusonaficationStatus
import spock.lang.Specification

import javax.security.auth.callback.Callback

import static com.sun.identity.authentication.util.ISAuthConstants.LOGIN_START
import static com.trusona.forgerock.auth.TrusonaAuthImpl.*


class TrusonaAuthImplSpec extends Specification {

  TrusonaAuthImpl sut
  Authenticator mockAuthenticator
  CallbackParser mockCallbackParser
  PrincipalMapper mockPrincipalMapper

  TrusonaCallback mockCallback
  TriConsumer<Integer, Integer, Callback> mockCallbackUpdate

  def setup() {
    mockAuthenticator = Mock(Authenticator)
    mockCallbackParser = Mock(CallbackParser)
    mockPrincipalMapper = Mock(PrincipalMapper)
    mockCallback = Mock(TrusonaCallback)
    mockCallbackUpdate = Mock(TriConsumer)

    sut = new TrusonaAuthImpl(mockAuthenticator, mockCallbackParser, mockPrincipalMapper, mockCallbackUpdate)
  }

  def truCodeStateCallbacks(trucodeId, error, payload) {
    def callbacks = new Callback[5]

    def truCodeIdCallback = new HiddenValueCallback("trucodeId")
    def errorCallback = new HiddenValueCallback("error")
    def payloadCallback = new HiddenValueCallback("payload")
    def trusonaficationIdCallback = new HiddenValueCallback("trusonaficationId")

    truCodeIdCallback.setValue(trucodeId)
    errorCallback.setValue(error)
    payloadCallback.setValue(payload)

    callbacks[0] = new ScriptTextOutputCallback("something")
    callbacks[1] = truCodeIdCallback
    callbacks[2] = errorCallback
    callbacks[3] = payloadCallback
    callbacks[4] = trusonaficationIdCallback

    return callbacks
  }

  def "process should create a trusonafication in the IDENTIFIER_STATE"() {
    given:
    mockCallbackParser.getTrusonaCallback(_) >> Optional.of(mockCallback)
    mockAuthenticator.createTrusonafication(mockCallback) >> UUID.randomUUID()
    mockCallback.isValid() >> true

    when:
    def nextState = sut.process(new Callback[0], IDENTIFIER_STATE)

    then:
    nextState == COMPLETION_STATE
    1 * mockAuthenticator.createTrusonafication(mockCallback)
    sut.getPrincipal() == null
  }

  def "process should return LOGIN_SUCEEDED and set the principal after it processes the initial and completed state"() {
    given:
    def trusonaficationId = UUID.randomUUID()
    def trusonaficationResult = new TrusonaficationResult(trusonaficationId, TrusonaficationStatus.ACCEPTED, "jones", null)

    mockCallbackParser.getTrusonaCallback(_) >> Optional.of(mockCallback)
    mockCallback.isValid() >> true
    mockAuthenticator.createTrusonafication(mockCallback) >> trusonaficationId
    mockAuthenticator.getTrusonaficationResult(trusonaficationId) >> trusonaficationResult
    mockPrincipalMapper.mapPrincipal(trusonaficationResult) >> Optional.of(new AuthPrincipal("jones"))

    when:
    def nextState = sut.process(new Callback[0], IDENTIFIER_STATE)
    def finalState = sut.process(new Callback[0], nextState)

    then:
    finalState == ISAuthConstants.LOGIN_SUCCEED
    sut.getPrincipal().name == "jones"
  }

  def "process should throw an exception if we cannot get a callback in the initial state"() {
    given:
    mockCallbackParser.getTrusonaCallback(_) >> Optional.empty()

    when:
    sut.process(new Callback[0], IDENTIFIER_STATE)

    then:
    thrown(AuthLoginException)
  }

  def "process should throw an exception if we get an invalid callback in the initial state"() {
    given:
    mockCallbackParser.getTrusonaCallback(_) >> Optional.of(mockCallback)
    mockCallback.isValid() >> false

    when:
    sut.process(new Callback[0], IDENTIFIER_STATE)

    then:
    thrown(AuthLoginException)
  }

  def "process should throw an exception if we cannot map a principal in the completion stage"() {
    given:
    def trusonaficationId = UUID.randomUUID()
    def trusoResult = new TrusonaficationResult(trusonaficationId, TrusonaficationStatus.ACCEPTED, "jones", null)

    mockCallbackParser.getTrusonaCallback(_) >> Optional.of(mockCallback)
    mockCallback.isValid() >> true
    mockAuthenticator.createTrusonafication(mockCallback) >> trusonaficationId
    mockAuthenticator.getTrusonaficationResult(trusonaficationId) >> trusoResult
    mockPrincipalMapper.mapPrincipal(trusoResult) >> Optional.empty()

    when:
    def nextState = sut.process(new Callback[0], IDENTIFIER_STATE)
    def finalState = sut.process(new Callback[0], nextState)

    then:
    thrown(AuthLoginException)
  }

  def "process should update the script callback and go to the  trucode state when in initial state"() {
    given:
    def scriptCallback = new ScriptTextOutputCallback("foobar")
    mockCallbackParser.getScriptCallback(_) >> scriptCallback

    when:
    def nextState = sut.process(new Callback[0], LOGIN_START)

    then:
    nextState == TRUCODE_STATE
    1 * mockCallbackUpdate.accept(TRUCODE_STATE, 0, scriptCallback)
  }

  def "process should return the COMPLETION_STATE and create a trusonafication in the TRUCODE_STATE"() {
    given:
    mockCallbackParser.getTrusonaCallback(_) >> Optional.of(mockCallback)
    mockAuthenticator.createTrusonafication(mockCallback) >> UUID.randomUUID()
    mockCallback.isValid() >> true

    def callbacks = truCodeStateCallbacks(UUID.randomUUID().toString(), "", "")

    when:
    def nextState = sut.process(callbacks, IDENTIFIER_STATE)

    then:
    nextState == COMPLETION_STATE
    1 * mockAuthenticator.createTrusonafication(mockCallback)
    sut.getPrincipal() == null
  }

  def "process should return the IDENTIFIER_STATE when no trucode is provided in TRUCODE_STATE"() {
    given:
    def callbacks = truCodeStateCallbacks("", "", "")
    when:
    def nextState = sut.process(callbacks, TRUCODE_STATE)

    then:
    nextState == IDENTIFIER_STATE
  }

  def "process should substitute the redirect URL and go to REDIRECT_TO_DEEPLINK when there is a payload"() {
    given:
    def payload = "somepayload"
    def redirect = new RedirectCallback()
    def script = new ScriptTextOutputCallback("foo")

    def callbacks = truCodeStateCallbacks(UUID.randomUUID().toString(), "", payload)

    mockCallbackParser.getRedirectCallback(payload) >> redirect
    mockCallbackParser.getScriptCallback(_) >> script
    mockCallbackParser.getTrusonaCallback(_) >> Optional.of(mockCallback)
    mockCallbackParser.getCallbackValue(_ as HiddenValueCallback) >> { args -> args[0].getValue() }
    mockAuthenticator.createTrusonafication(mockCallback) >> UUID.randomUUID()
    mockCallback.isValid() >> true

    when:
    def nextState = sut.process(callbacks, TRUCODE_STATE)

    then:
    nextState == REDIRECT_TO_DEEPLINK
    1 * mockCallbackUpdate.accept(REDIRECT_TO_DEEPLINK, 0, script)
    1 * mockCallbackUpdate.accept(REDIRECT_TO_DEEPLINK, 1, redirect)
  }
}
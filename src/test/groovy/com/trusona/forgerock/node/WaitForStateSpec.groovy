package com.trusona.forgerock.node

import com.sun.identity.shared.debug.Debug
import com.trusona.sdk.resources.TrusonaApi
import com.trusona.sdk.resources.dto.TrusonaficationResult
import com.trusona.sdk.resources.dto.TrusonaficationStatus
import org.forgerock.openam.authentication.callbacks.PollingWaitCallback
import spock.lang.Specification

import static com.trusona.sdk.resources.dto.TrusonaficationStatus.*

class WaitForStateSpec extends Specification {

  TrusonaApi mockTrusona = Mock(TrusonaApi)
  UUID trusonaficationId = UUID.randomUUID()
  Debug debug = Mock(Debug)

  WaitForState sut = new WaitForState(mockTrusona, trusonaficationId, debug)

  def mockResult(TrusonaficationStatus status, String userIdentifier) {
    mockTrusona.getTrusonaficationResult(trusonaficationId) >>
      new TrusonaficationResult(trusonaficationId, status, userIdentifier, null)
  }

  def "should ask the client to wait if the trusonafication is IN_PROGRESS"() {
    given:
    mockResult(IN_PROGRESS, "")

    when:
    def res = sut.get()

    then:
    res.sendingCallbacks()
    res.callbacks[0] instanceof PollingWaitCallback
    res.callbacks[0].waitTime == "5000" // Yes it is a string, and it is in milliseconds
  }

  def "should go to ACCEPTED outcome if the trusonafication was successful"() {
    given:
    mockResult(status, "userId");

    when:
    def res = sut.get()

    then:
    ! res.sendingCallbacks()
    res.outcome == TrusonaOutcomes.ACCEPTED_OUTCOME.id

    where:
    status << [ ACCEPTED, ACCEPTED_AT_HIGHER_LEVEL ]
  }

  def "should go to REJECTED outcome if the trusonafication was rejected"() {
    given:
    mockResult(REJECTED, "userId")

    when:
    def res = sut.get()

    then:
    ! res.sendingCallbacks()
    res.outcome == TrusonaOutcomes.REJECTED_OUTCOME.id

  }

  def "should go to EXPIRED outcome if the trusonafication was expired"() {
    given:
    mockResult(EXPIRED, "userId")

    when:
    def res = sut.get()

    then:
    ! res.sendingCallbacks()
    res.outcome == TrusonaOutcomes.EXPIRED_OUTCOME.id
  }
}

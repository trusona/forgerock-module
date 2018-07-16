package com.trusona.forgerock.auth.principal

import com.trusona.sdk.resources.dto.TrusonaficationResult
import com.trusona.sdk.resources.dto.TrusonaficationStatus
import spock.lang.Specification

class TrusonaSdkPrincipalMapperSpec extends Specification {

  TrusonaSdkPrincipalMapper sut = new TrusonaSdkPrincipalMapper()

  def "mapPrincipal should return the userIdentifier"() {
    given:
    def trusoResult = new TrusonaficationResult(UUID.randomUUID(), TrusonaficationStatus.ACCEPTED, "jones", new Date())

    when:
    def res = sut.mapPrincipal(trusoResult)

    then:
    res.isPresent()
    res.get().name == "jones"
  }
}

package com.trusona.forgerock.auth.principal

import com.trusona.client.TrusonaClient
import spock.lang.Specification

class TrusonaAppPrincipalMapperSpec extends Specification {

  TrusonaAppPrincipalMapper sut
  TrusonaClient mockTrusonaClient

  def setup() {
    mockTrusonaClient = Mock(TrusonaClient)
    sut = new TrusonaAppPrincipalMapper(mockTrusonaClient)
  }

  def "mapPrincipal should return empty if user is not found"() {

  }

  def "mapPrincipal should return the email of the first one that matches a FR user"() {

  }
}

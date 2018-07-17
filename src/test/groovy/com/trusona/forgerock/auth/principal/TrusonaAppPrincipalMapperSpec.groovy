package com.trusona.forgerock.auth.principal

import com.sun.identity.idm.AMIdentity
import com.trusona.client.TrusonaClient
import com.trusona.client.dto.response.UserResponse
import com.trusona.sdk.resources.dto.TrusonaficationResult
import spock.lang.Specification

class TrusonaAppPrincipalMapperSpec extends Specification {

  TrusonaAppPrincipalMapper sut

  def setup() {
    sut = new TrusonaAppPrincipalMapper(Mock(TrusonaClient), Mock(IdentityFinder))
  }

  def "mapPrincipal should return first email if ForgeRock identity user is not found"() {
    when:
    def result = sut.mapPrincipal(Mock(TrusonaficationResult))

    then:
    1 * _.userIdentifier >> "trusonaId:0123456789"
    1 * _.getUser("0123456789") >> [(Mock(UserResponse))]
    (1.._) * _.emails >> ["jones@example.net", "bob@africa.com"]
    1 * _.findForgeRockUser('jones@example.net') >> null
    1 * _.findForgeRockUser('bob@africa.com') >> null
    0 * _

    and:
    result.get().name == "jones@example.net"
  }

  def "mapPrincipal should return ForgeRock identity when is found"() {
    when:
    def result = sut.mapPrincipal(Mock(TrusonaficationResult))

    then:
    1 * _.userIdentifier >> "trusonaId:0123456789"
    1 * _.getUser("0123456789") >> [(Mock(UserResponse))]
    (1.._) * _.emails >> ["jones@example.net", "bob@africa.com"]
    1 * _.findForgeRockUser('jones@example.net') >> null
    1 * _.findForgeRockUser('bob@africa.com') >> Mock(AMIdentity)
    1 * _.universalId >> "909090"
    0 * _

    and:
    result.get().name == "909090"
  }

  def "mapPrincipal should return an empty principal when the Trusona user is not found"() {
    when:
    def result = sut.mapPrincipal(Mock(TrusonaficationResult))

    then:
    1 * _.userIdentifier >> "trusonaId:0123456789"
    1 * _.getUser("0123456789") >> []
    0 * _

    and:
    !result.present
  }
}
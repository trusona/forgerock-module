package com.trusona.forgerock.auth.principal

import com.trusona.sdk.resources.dto.TrusonaficationResult
import com.trusona.sdk.resources.dto.TrusonaficationStatus
import spock.lang.Specification

class DefaultPrincipalMapperSpec extends Specification {

  DefaultPrincipalMapper sut
  Date farFuture

  def setup() {
    sut = new DefaultPrincipalMapper()
    farFuture = new Date(System.currentTimeMillis() + 60 * 60 * 1000)
  }

  def "mapPrincipal should return a principal when the trusonafication is successful"() {
    given:
    def trusoResult = new TrusonaficationResult(UUID.randomUUID(), TrusonaficationStatus.ACCEPTED, "jones", farFuture) {
      @Override
      boolean isSuccessful() {
        true
      }
    }

    when:
    def res = sut.mapPrincipal(trusoResult)

    then:
    res.isPresent()
    res.get().name == "jones"
  }

  def "mapPrincipal should return an empty optional when the trusonafication is not successful"() {
    given:
    def trusoResult = new TrusonaficationResult(UUID.randomUUID(), TrusonaficationStatus.ACCEPTED, "jones", farFuture) {
      @Override
      boolean isSuccessful() {
        false
      }
    }

    when:
    def res = sut.mapPrincipal(trusoResult)

    then:
    ! res.isPresent()
  }

  def "mapPrincipal should return an empty optional when the user identifier is null"() {
    given:
    def trusoResult = new TrusonaficationResult(UUID.randomUUID(), TrusonaficationStatus.ACCEPTED, null, farFuture) {
      @Override
      boolean isSuccessful() {
        true
      }
    }

    when:
    def res = sut.mapPrincipal(trusoResult)

    then:
    ! res.isPresent()
  }

  def "mapPrincipal should return an empty optional when the trusonafication expired more than a minute ago"() {
    given:
    def aWhileAgo = new Date(System.currentTimeMillis() - 60 * 60 * 1000)
    def trusoResult = new TrusonaficationResult(UUID.randomUUID(), TrusonaficationStatus.ACCEPTED, "jones", aWhileAgo)

    when:
    def res = sut.mapPrincipal(trusoResult)

    then:
    ! res.isPresent()
  }

  def "mapPrincipal should return a principal when the trusonafication is successful and expired about 30 seconds ago"() {
    given:
    def thirtySecondsAgo = new Date(System.currentTimeMillis() - 30 * 1000)
    def trusoResult = new TrusonaficationResult(UUID.randomUUID(), TrusonaficationStatus.ACCEPTED, "jones", thirtySecondsAgo)

    when:
    def res = sut.mapPrincipal(trusoResult)

    then:
    res.isPresent()
  }
}

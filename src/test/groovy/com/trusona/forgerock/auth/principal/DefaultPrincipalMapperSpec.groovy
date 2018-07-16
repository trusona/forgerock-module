package com.trusona.forgerock.auth.principal

import com.sun.identity.authentication.internal.AuthPrincipal
import com.trusona.sdk.resources.dto.TrusonaficationResult
import com.trusona.sdk.resources.dto.TrusonaficationStatus
import spock.lang.Specification

class DefaultPrincipalMapperSpec extends Specification {

  DefaultPrincipalMapper sut
  TrusonaAppPrincipalMapper mockAppMapper
  TrusonaSdkPrincipalMapper mockSdkMapper
  Date farFuture

  def setup() {
    mockAppMapper = Mock(TrusonaAppPrincipalMapper)
    mockSdkMapper = Mock(TrusonaSdkPrincipalMapper)
    sut = new DefaultPrincipalMapper(mockAppMapper, mockSdkMapper)
    farFuture = new Date(System.currentTimeMillis() + 60 * 60 * 1000)
  }

  def "mapPrincipal should return the mapped SDK principal if the user identifier is tilted"() {
    given:
    def trusoResult = new TrusonaficationResult(UUID.randomUUID(), TrusonaficationStatus.ACCEPTED, "jones", farFuture) {
      @Override
      boolean isSuccessful() {
        true
      }
    }

    mockSdkMapper.mapPrincipal(trusoResult) >> Optional.of(new AuthPrincipal("jones"))

    when:
    def res = sut.mapPrincipal(trusoResult)

    then:
    res.isPresent()
    res.get().name == "jones"
  }

  def "mapPrincipal should return the mapped APP principal if the user identifier is from the Trusona App"() {
    given:
    def trusoResult = new TrusonaficationResult(UUID.randomUUID(), TrusonaficationStatus.ACCEPTED, "trusonaId:123456789", farFuture) {
      @Override
      boolean isSuccessful() {
        true
      }
    }

    mockAppMapper.mapPrincipal(trusoResult) >> Optional.of(new AuthPrincipal("jones@example.org"))

    when:
    def res = sut.mapPrincipal(trusoResult)

    then:
    res.isPresent()
    res.get().name == "jones@example.org"
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
    mockSdkMapper.mapPrincipal(trusoResult) >> Optional.of(new AuthPrincipal("jones"))

    when:
    def res = sut.mapPrincipal(trusoResult)

    then:
    res.isPresent()
  }
}

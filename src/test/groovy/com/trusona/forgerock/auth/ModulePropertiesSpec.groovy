package com.trusona.forgerock.auth

import groovy.util.slurpersupport.GPathResult
import spock.lang.Specification

import static com.sun.identity.authentication.util.ISAuthConstants.*
import static com.trusona.forgerock.auth.TrusonaAuthImpl.*
import static com.trusona.forgerock.auth.TrusonaAuthImpl.IDENTIFIER_STATE

class ModulePropertiesSpec extends Specification {

  String resourceName = "/config/auth/default/TrusonaAuth.xml"

  XmlSlurper parser
  GPathResult sut

  def setup() {
    parser = new XmlSlurper()
    parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false)
    parser.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
    sut = parser.parse(this.getClass().getResourceAsStream(resourceName))
  }

  def getCallback(order) {
    sut.'*'.find { node -> node.@order == order }
  }

  def "TrusonaAuth should define 5 stages"() {
    when:
    def res = sut.children()

    then:
    res.size() == 5
  }

  def "TrusonAuth's identifier stage should include a NameCallback for the identifier"() {
    when:
    def callback = getCallback(IDENTIFIER_STATE)

    then:
    callback.NameCallback.Prompt.text() == "identifier"
  }

  def "TrusonaAuth's identifier stage should include a ChoiceCallback for the identifier type"() {
    when:
    def callback = getCallback(IDENTIFIER_STATE)

    then:
    callback.ChoiceCallback.Prompt.text() == "identifier_type"
    callback.ChoiceCallback.ChoiceValues.'*'.size() == 3
    callback.ChoiceCallback.ChoiceValues.'*'.collect { node -> node.Value.text() } == [ 'user_identifier', 'device_identifier', 'trucode_id']
  }

  def "TrusonaAuth's completion stage should not require any callbacks"() {
    when:
    def callback = getCallback(COMPLETION_STATE)

    then:
    callback.children().size() == 0
  }

  def "TrusonAuth's initial stage should not have any callbacks"() {
    when:
    def callback = getCallback(LOGIN_START)

    then:
    callback.children().size() == 0

  }

  def "TrusonaAuth's trucode stage should have a script text callback and three hidden value callbacks"() {
    when:
    def callback = getCallback(TRUCODE_STATE)

    then:
    callback.TextOutputCallback.@messageType == "script"
    callback.TextOutputCallback.text() == "PLACEHOLDER"

    callback.HiddenValueCallback.'*'.size() == 4
    callback.HiddenValueCallback.'*'.collect { node -> node.text() } == [ 'truCodeId', 'error', 'payload', 'trusonaficationId' ]

  }
}
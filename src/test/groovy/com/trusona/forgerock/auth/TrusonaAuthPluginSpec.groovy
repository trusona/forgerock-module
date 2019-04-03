package com.trusona.forgerock.auth

import org.forgerock.openam.plugins.PluginTools
import spock.lang.Specification

class TrusonaAuthPluginSpec extends Specification {

  TrusonaAuthPlugin authPlugin

  def setup() {
    authPlugin = new TrusonaAuthPlugin(Mock(PluginTools))
  }

  def "plugin version will not be unspecified"() {
    expect:
    authPlugin.pluginVersion != "unspecified"
  }

  def "plugin version will not be default value"() {
    expect:
    authPlugin.pluginVersion != "@project_version@"
  }

  def "plugin version will be in normal semver form"() {
    expect:
    authPlugin.pluginVersion.split("\\.").length == 3
  }
}
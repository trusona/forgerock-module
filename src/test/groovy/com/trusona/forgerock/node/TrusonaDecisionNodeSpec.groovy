package com.trusona.forgerock.node

import org.forgerock.openam.core.CoreWrapper
import spock.lang.Specification

class TrusonaDecisionNodeSpec extends Specification {

  TrusonaDecisionNode.Config config
  CoreWrapper coreWrapper
  TrusonaDecisionNode sut


  def setup() {
    coreWrapper = new CoreWrapper()
    config = new TrusonaDecisionNode.Config() {
      @Override
      String apiToken() {
        return "token"
      }

      @Override
      String apiSecret() {
        return "secret"
      }

      @Override
      String action() {
        return "action"
      }

      @Override
      String resource() {
        return "resource"
      }

      @Override
      String deeplinkUrl() {
        return "https://example.com/payload"
      }
    }

    sut = new TrusonaDecisionNode(config, coreWrapper)
  }
}

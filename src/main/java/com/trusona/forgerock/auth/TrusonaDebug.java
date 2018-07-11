package com.trusona.forgerock.auth;

import com.sun.identity.shared.debug.Debug;

public class TrusonaDebug {
  private static final String INSTANCE_NAME = "TrusonaAuth";

  private TrusonaDebug() {
  }

  public static Debug getInstance() {
    return Debug.getInstance(INSTANCE_NAME);
  }
}

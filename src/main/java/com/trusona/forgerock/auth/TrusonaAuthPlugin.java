package com.trusona.forgerock.auth;

import com.google.inject.Inject;
import org.forgerock.openam.plugins.AmPlugin;
import org.forgerock.openam.plugins.PluginException;
import org.forgerock.openam.plugins.PluginTools;

public class TrusonaAuthPlugin implements AmPlugin {
  private PluginTools pluginTools;

  @Inject
  public TrusonaAuthPlugin(PluginTools pluginTools) {
    this.pluginTools = pluginTools;
  }

  @Override
  public String getPluginVersion() {
    return "1.2.0";
  }

  @Override
  public void onInstall() throws PluginException {
    pluginTools.addAuthModule(TrusonaAuth.class,
      getClass().getClassLoader().getResourceAsStream("amAuthTrusonaAuth.xml"));
  }

  @Override
  public void upgrade(String fromVersion) throws PluginException {
    pluginTools.addAuthModule(TrusonaAuth.class,
      getClass().getClassLoader().getResourceAsStream("amAuthTrusonaAuth.xml"));
  }
}

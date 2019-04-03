package com.trusona.forgerock.auth;

import com.google.inject.Inject;
import java.io.IOException;
import java.util.Properties;
import org.forgerock.openam.plugins.AmPlugin;
import org.forgerock.openam.plugins.PluginException;
import org.forgerock.openam.plugins.PluginTools;

public class TrusonaAuthPlugin implements AmPlugin {

  private PluginTools pluginTools;
  private String version;

  @Inject
  public TrusonaAuthPlugin(PluginTools pluginTools) {
    this.pluginTools = pluginTools;
    this.version = initVersion();
  }

  @Override
  public String getPluginVersion() {
    return version;
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

  private String initVersion() {
    String path = "com/trusona/forgerock/auth/plugin-version.properties";
    Properties properties = new Properties();

    try {
      properties.load(getClass().getClassLoader().getResourceAsStream(path));
    }
    catch (NullPointerException | IOException e) {
      TrusonaDebug.getInstance().error("failed to load version", e);
    }

    return properties.getProperty("version", "unspecified");
  }
}
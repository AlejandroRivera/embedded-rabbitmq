package io.arivera.oss.embedded.rabbitmq.bin.plugins;

import org.junit.Test;

import java.util.Set;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertThat;

public class PluginStateTest {

  @Test
  public void pluginStateEmpty() throws Exception {
    assertThat(PluginDetails.PluginState.fromString("  "),
        hasItems(
            PluginDetails.PluginState.NOT_ENABLED,
            PluginDetails.PluginState.NOT_RUNNING));
  }

  @Test
  public void pluginStateExplicitRunning() throws Exception {
    Set<PluginDetails.PluginState> pluginStates = PluginDetails.PluginState.fromString("E*");
    assertThat(pluginStates, hasItems(
        PluginDetails.PluginState.RUNNING,
        PluginDetails.PluginState.ENABLED_EXPLICITLY));
  }

  @Test
  public void pluginStateImplicitRunning() throws Exception {
    Set<PluginDetails.PluginState> pluginStates = PluginDetails.PluginState.fromString("e*");
    assertThat(pluginStates, hasItems(
        PluginDetails.PluginState.RUNNING,
        PluginDetails.PluginState.ENABLED_IMPLICITLY));
  }
}
package io.arivera.oss.embedded.rabbitmq.bin.plugins;

import org.junit.Test;

import java.util.Set;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertThat;

public class StateTest {

  @Test
  public void pluginStateEmpty() throws Exception {
    assertThat(Plugin.State.fromString("  "),
        hasItems(
            Plugin.State.NOT_ENABLED,
            Plugin.State.NOT_RUNNING));
  }

  @Test
  public void pluginStateExplicitRunning() throws Exception {
    Set<Plugin.State> states = Plugin.State.fromString("E*");
    assertThat(states, hasItems(
        Plugin.State.RUNNING,
        Plugin.State.ENABLED_EXPLICITLY));
  }

  @Test
  public void pluginStateImplicitRunning() throws Exception {
    Set<Plugin.State> states = Plugin.State.fromString("e*");
    assertThat(states, hasItems(
        Plugin.State.RUNNING,
        Plugin.State.ENABLED_IMPLICITLY));
  }
}
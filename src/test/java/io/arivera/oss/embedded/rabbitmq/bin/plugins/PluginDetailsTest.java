package io.arivera.oss.embedded.rabbitmq.bin.plugins;

import org.junit.Test;

import java.util.EnumSet;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class PluginDetailsTest {

  public static final Pattern PATTERN = PluginDetails.LIST_OUTPUT_PATTERN;

  @Test
  public void testPatterns() throws Exception {
    assertTrue(PATTERN.matcher("[E*] rabbitmq_management               3.5.7").matches());
    assertTrue(PATTERN.matcher("[e ] mochiweb                          2.7.0-rmq3.5.7-git680dba8").matches());
    assertTrue(PATTERN.matcher("[  ] rabbitmq_federation_management    3.5.7").matches());
    assertFalse(PATTERN.matcher(" Configured: E = explicitly enabled; e = implicitly enabled").matches());
  }

  @Test
  public void testNotEnabledPluginLine() throws Exception {
    String output = "[  ] rabbitmq_federation_management    3.5.7";
    PluginDetails plugin = PluginDetails.fromString(output);

    assertThat(plugin.getName(), equalTo("rabbitmq_federation_management"));
    assertThat(plugin.getVersion(), equalTo("3.5.7"));
    assertThat(plugin.getState(), equalTo(
        EnumSet.of(PluginDetails.PluginState.NOT_ENABLED, PluginDetails.PluginState.NOT_RUNNING)));
  }

  @Test
  public void testExplicitlyEnabledPluginLine() throws Exception {
    String output = "[E*] rabbitmq_management               3.5.7";
    PluginDetails plugin = PluginDetails.fromString(output);
    assertThat(plugin.getName(), equalTo("rabbitmq_management"));
    assertThat(plugin.getVersion(), equalTo("3.5.7"));
    assertThat(plugin.getState(), hasItems(
        PluginDetails.PluginState.ENABLED_EXPLICITLY, PluginDetails.PluginState.RUNNING));
  }

  @Test
  public void testImplicitlyEnabledPluginLine() throws Exception {
    String output = "[e ] mochiweb                          2.7.0-rmq3.5.7-git680dba8";
    PluginDetails plugin = PluginDetails.fromString(output);
    assertThat(plugin.getName(), equalTo("mochiweb"));
    assertThat(plugin.getVersion(), equalTo("2.7.0-rmq3.5.7-git680dba8"));
    assertThat(plugin.getState(), hasItems(
        PluginDetails.PluginState.ENABLED_IMPLICITLY, PluginDetails.PluginState.NOT_RUNNING));
  }
}
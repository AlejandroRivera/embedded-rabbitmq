package io.arivera.oss.embedded.rabbitmq.bin.plugins;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PluginDetails implements Comparable<PluginDetails> {

  static final Pattern LIST_OUTPUT_PATTERN = Pattern.compile("\\s*\\[(.*)]\\s+(\\w+)\\s+(\\S+)\\s*");

  private static final Logger LOGGER = LoggerFactory.getLogger(PluginDetails.class);

  private String pluginName;
  private EnumSet<PluginState> status;
  private String version;

  private PluginDetails(String pluginName, EnumSet<PluginState> state, String version) {
    this.pluginName = pluginName;
    this.status = state;
    this.version = version;
  }

  /**
   * @param outputLine as generated by the command line {@code rabbitmq-plugins list}
   *
   * @return null if output can't be parsed.
   */
  public static PluginDetails fromString(String outputLine) {
    Matcher matcher = PluginDetails.LIST_OUTPUT_PATTERN.matcher(outputLine);
    if (!matcher.matches()) {
      return null;
    }
    String state = matcher.group(1);
    String pluginName = matcher.group(2);
    String version = matcher.group(3);

    return new PluginDetails(pluginName, PluginDetails.PluginState.fromString(state), version);
  }

  public String getName() {
    return pluginName;
  }

  public EnumSet<PluginState> getState() {
    return status;
  }

  public String getVersion() {
    return version;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other == null || getClass() != other.getClass()) {
      return false;
    }
    PluginDetails that = (PluginDetails) other;
    return Objects.equals(pluginName, that.pluginName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(pluginName);
  }

  @Override
  public int compareTo(PluginDetails other) {
    return this.pluginName.compareTo(other.pluginName);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("PluginDetails{");
    sb.append("pluginName='").append(pluginName).append('\'');
    sb.append(", status=").append(status);
    sb.append(", version='").append(version).append('\'');
    sb.append('}');
    return sb.toString();
  }

  public enum PluginState {
    ENABLED_EXPLICITLY, ENABLED_IMPLICITLY, NOT_ENABLED,
    RUNNING, NOT_RUNNING;

    /**
     * Parses a string as given by the command line output from {@code rabbitmq-plugins list} for the characters in
     * between brackets.
     */
    public static EnumSet<PluginState> fromString(String string) {
      EnumSet<PluginState> pluginStatuses = EnumSet.noneOf(PluginState.class);

      char[] chars = string.toCharArray();

      if (chars.length != 2) {
        LOGGER.warn("Parsing of Plugin State might not be accurate since we expect 2 symbols representing: {}",
            PluginState.values());
      }

      if (chars.length <= 0) {
        return pluginStatuses;
      }

      char enabledCharacter = chars[0];
      switch (enabledCharacter) {
        case ' ':
          pluginStatuses.add(NOT_ENABLED);
          break;
        case 'e':
          pluginStatuses.add(ENABLED_IMPLICITLY);
          break;
        case 'E':
          pluginStatuses.add(ENABLED_EXPLICITLY);
          break;
        default:
          LOGGER.warn("Could not parse symbol '{}' for enabled state in: {}", enabledCharacter, string);
      }

      if (chars.length < 2) {
        return pluginStatuses;
      }

      char runningCharacter = string.charAt(1);
      switch (runningCharacter) {
        case '*':
          pluginStatuses.add(RUNNING);
          break;
        case ' ':
          pluginStatuses.add(NOT_RUNNING);
          break;
        default:
          LOGGER.warn("Could not parse symbol '{}' for run state in: {}", runningCharacter, string);
      }

      return pluginStatuses;
    }

  }
}

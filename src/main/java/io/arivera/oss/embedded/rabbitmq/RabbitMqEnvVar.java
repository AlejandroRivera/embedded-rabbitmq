package io.arivera.oss.embedded.rabbitmq;

/**
 * A list of RabbitMQ environment variables used to configure the broker's behavior.
 *
 * @see <a href="https://www.rabbitmq.com/configure.html">https://www.rabbitmq.com/configure.html</a>
 */
public enum RabbitMqEnvVar {

  /**
   * Use this if you only want to bind to one network interface. To bind to two or more interfaces, use the
   * {@code tcp_listeners} key in {@code rabbitmq.config}.
   *
   * <p>Default value: empty string - meaning bind to all network interfaces</p>
   */
  NODE_IP_ADDRESS,

  /**
   * The port to bind this RabbitMQ broker node.
   *
   * <p>Default value: {@value DEFAULT_NODE_PORT}</p>
   */
  NODE_PORT,

  /**
   * Port to use for clustering. Ignored if your config file sets {@code inet_dist_listen_min} or
   * {@code inet_dist_listen_max}
   */
  DIST_PORT,

  /**
   * The node name should be unique per erlang-node-and-machine combination.
   *
   * <p>To run multiple nodes, see the clustering guide.</p>
   *
   * <p>Default:
   * <ul>
   *   <li>Unix: {@code rabbit@$HOSTNAME}</li>
   *   <li>Windows: {@code rabbit@%COMPUTERNAME%}</li>
   * </ul>
   */
  NODENAME,

  /**
   * Location of the file that contains environment variable definitions (without the {@code RABBITMQ_} prefix).
   *
   * <p>Note that the file name on Windows is different from other operating systems.</p>
   *
   * <p>Defaults: <br/>
   * Generic UNIX         - {@code $RABBITMQ_HOME/etc/rabbitmq/rabbitmq-env.conf}<br/>
   * Debian               - {@code /etc/rabbitmq/rabbitmq-env.conf}<br/>
   * RPM                  - {@code /etc/rabbitmq/rabbitmq-env.conf}<br/>
   * Mac OS X (Homebrew)  - {@code $\{install_prefix\}/etc/rabbitmq/rabbitmq-env.conf},
   *                        the Homebrew prefix is usually /usr/local<br/>
   * Windows              - {@code %APPDATA%\RabbitMQ\rabbitmq-env-conf.bat}<br/>
   * </p>
   */
  CONF_ENV_FILE,

  /**
   * When set to {@code true} this will cause RabbitMQ to use fully qualified names to identify nodes.
   *
   * <p>This may prove useful on EC2. Note that it is not possible to switch between using short and long names
   * without resetting the node.</p>
   */
  USE_LONGNAME,

  /**
   * The name of the installed service. This will appear in services.msc.
   *
   * <p>Default for Windows: {@code RabbitMQ}</p>
   */
  SERVICENAME,

  /**
   * Set this variable to new or reuse to redirect console output from the server to a file named
   * @{code %RABBITMQ_SERVICENAME%.debug} in the default {@code RABBITMQ_BASE} directory.
   *
   * <p>If not set, console output from the server will be discarded (default).
   * {@code new} - A new file will be created each time the service starts.
   * {@code reuse} - The file will be overwritten each time the service starts.
   * </p>
   */
  CONSOLE_LOG,

  /**
   * Parameters for the erl command used when invoking rabbitmqctl.
   *
   * <p>This should be overridden for debugging purposes only.</p>
   *
   * <p>Default: None</p>
   */
  CTL_ERL_ARGS,

  /**
   * Standard parameters for the erl command used when invoking the RabbitMQ Server.
   *
   * <p>This should be overridden for debugging purposes only. Overriding this variable replaces the default value.</p>
   *
   * <p>Defaults: <br/>
   * Unix*: {@code "+K true +A30 +P 1048576 -kernel inet_default_connect_options [{nodelay,true}]"} <br/>
   * Windows: None</p>
   */
  SERVER_ERL_ARGS,

  /**
   * Additional parameters for the erl command used when invoking the RabbitMQ Server.
   *
   * <p>The value of this variable is appended the default list of arguments ({@code RABBITMQ_SERVER_ERL_ARGS}).</p>
   *
   * <p>Defaults:<br/>
   * - Unix*: None<br/>
   * - Windows: None</p>
   *
   */
  SERVER_ADDITIONAL_ERL_ARGS,

  /**
   * Extra parameters for the erl command used when invoking the RabbitMQ Server.
   *
   * <p>This will not override {@code RABBITMQ_SERVER_ERL_ARGS}.</p>
   *
   * <p>Default: None</p>
   */
  SERVER_START_ARGS,

  /**
   * Defines the location of the RabbitMQ core configuration file.
   *
   * <p>The value should not contain the suffix {@code .config} since Erlang will append it automatically.</p>
   */
  CONFIG_FILE;

  private static final String ENV_VAR_PREFIX = "RABBITMQ_";

  public static final int DEFAULT_NODE_PORT = 5672;

  RabbitMqEnvVar() {
  }

  /**
   * @return environment variable name (with the prefix {@code RABBITMQ_}).
   */
  public String getEnvVarName() {
    return ENV_VAR_PREFIX + name();
  }
}

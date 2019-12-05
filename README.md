# Embedded RabbitMQ

<sup>**Compatibility**:</sup>
[![Unix](https://img.shields.io/badge/platform-unix-brightgreen.svg)]()
[![Linux](https://img.shields.io/badge/platform-linux-brightgreen.svg)]()
[![OS X](https://img.shields.io/badge/platform-os%20x-brightgreen.svg)]()
[![Windows](https://img.shields.io/badge/platform-windows-brightgreen.svg)]()
<br/>
<sup>**Builds**: Linux</sup>
[![CircleCI branch](https://img.shields.io/circleci/project/github/AlejandroRivera/embedded-rabbitmq/master.svg)](https://circleci.com/gh/AlejandroRivera/embedded-rabbitmq/tree/master)
<sup>OS X</sup> 
[![Build Status](https://travis-ci.org/AlejandroRivera/embedded-rabbitmq.svg?branch=master)](https://travis-ci.org/AlejandroRivera/embedded-rabbitmq)
<sup>Windows</sup> 
[![Build status](https://ci.appveyor.com/api/projects/status/r46o01l4ora7ppkf/branch/master?svg=true)](https://ci.appveyor.com/project/AlejandroRivera/embedded-rabbitmq)
<br/>
<sup>**Reports**:</sup>
[![Coverage Status](https://coveralls.io/repos/github/AlejandroRivera/embedded-rabbitmq/badge.svg)](https://coveralls.io/github/AlejandroRivera/embedded-rabbitmq)
[![Javadocs](http://www.javadoc.io/badge/io.arivera.oss/embedded-rabbitmq.svg?color=blue&label=javadoc)](http://www.javadoc.io/doc/io.arivera.oss/embedded-rabbitmq)
<br/>
<sup>**Dist**:</sup>
[![License](https://img.shields.io/github/license/AlejandroRivera/embedded-rabbitmq.svg)](./blob/master/LICENSE)
[![Snapshots](https://img.shields.io/badge/sonatype-SNAPSHOTS-blue.svg)](https://oss.sonatype.org/content/repositories/snapshots/io/arivera/oss/embedded-rabbitmq/)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.arivera.oss/embedded-rabbitmq/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.arivera.oss/embedded-rabbitmq)
<br/>
<sup>**Social**:</sup>
[![Gitter chat](https://badges.gitter.im/gitterHQ/gitter.png)](https://gitter.im/io-arivera-oss/embedded-rabbitmq)
[![PayPal donation](https://img.shields.io/badge/donate-PayPal-blue.svg)](https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=alejandro%2erivera%2elopez%40gmail%2ecom&lc=US&item_name=io%2earivera%2eoss&item_number=embedded%2drabbitmq&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHostedGuest)
[![Flattr donation](https://img.shields.io/badge/donate-Flattr-yellow.svg)](https://flattr.com/submit/auto?fid=lgx6kv&url=https%3A%2F%2Fgithub.com%2FAlejandroRivera%2Fembedded-rabbitmq)
[![GratiPay donation](https://img.shields.io/gratipay/team/embedded-rabbitmq.svg)](https://gratipay.com/embedded-rabbitmq/)

This library allows for the use of various RabbitMQ versions as if it was an embedded service that can be controlled 
from within the JVM.

The way it works is by downloading, from official repositories, the correct artifact for the given version and 
operating system, extracting it and starting the RabbitMQ Server with the specified configuration. 
The broker can then be administered from within the JVM by using equivalent commands to `rabbitmqctl` or `rabbitmq-plugins`.

## Pre-requisites:

 * This project requires Java 7+
 * RabbitMQ Broker requires Erlang to be installed. 

## Quick Start:

### 1. Add a dependency to this project

For Maven:
```xml
  <dependency>
      <groupId>io.arivera.oss</groupId>
      <artifactId>embedded-rabbitmq</artifactId>
      <version>X.Y.Z</version>
  </dependency>
```
For Ivy: ```<dependency org="io.arivera.oss" name="embedded-rabbitmq" rev="X.Y.Z" />```

For Gradle: ``` compile 'io.arivera.oss:embedded-rabbitmq:X.Y.Z' ```

For SBT: ``` libraryDependencies += "io.arivera.oss" % "embedded-rabbitmq" % "X.Y.Z" ```

`X.Y.Z` is the latest released version of this project. 
For more info visit [Maven Central repo](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22io.arivera.oss%22%20AND%20a%3A%22embedded-rabbitmq%22) 
or visit the [releases](https://github.com/AlejandroRivera/embedded-rabbitmq/releases) page.

For `SNAPSHOT` releases, add the SonaType repository to your build system: 
https://oss.sonatype.org/content/repositories/snapshots/

### 2. Start the RabbitMQ broker

```java
EmbeddedRabbitMqConfig config = new EmbeddedRabbitMqConfig.Builder().build();
EmbeddedRabbitMq rabbitMq = new EmbeddedRabbitMq(config);
rabbitMq.start();
```

When `start()` is invoked, the Embedded-RabbitMQ library will download the latest release from RabbitMQ.com that best matches 
your Operating System. The artifact will be decompressed into a temporary folder, and a new OS process will launch the RabbitMQ broker.

Read more about [how to customize](#Customization) your RabbitMQ broker.

### 3. Verify RabbitMQ is working as you'd expect
```java
ConnectionFactory connectionFactory = new ConnectionFactory();
connectionFactory.setHost("localhost");
connectionFactory.setVirtualHost("/");
connectionFactory.setUsername("guest");
connectionFactory.setPassword("guest");

Connection connection = connectionFactory.newConnection();
assertThat(connection.isOpen(), equalTo(true));
Channel channel = connection.createChannel();
assertThat(channel.isOpen(), equalTo(true));

channel.close();
connection.close();
```

### 4. Stop the RabbitMQ broker:
```java
rabbitMq.stop();
```

## Customization

Customization is done through the `EmbeddedRabbitMqConfig` and it's `Builder` class.
All snippets below will refer to this:
```java
EmbeddedRabbitMqConfig.Builder configBuilder = new EmbeddedRabbitMqConfig.Builder();
...
EmbeddedRabbitMqConfig config = configBuilder.build();
EmbeddedRabbitMq rabbitMq = new EmbeddedRabbitMq(config);
rabbitMq.start();
```

### Define a RabbitMQ version to use:
```java
configBuilder.version(PredefinedVersion.LATEST)
```
or 
```java
configBuilder.version(PredefinedVersion.V3_6_5)
```

By using the `version()` method, the download URL, executable paths, etc. will be pre-set for Unix/Mac/Windows Operating Systems.
You can change the download source from the official `rabbitmq.com` servers and Github by using the `downloadFrom()` method:

```java
configBuilder.downloadFrom(OfficialArtifactRepository.GITHUB)
```

Similarly, if you wish to download another version and/or use another server by specifying a URL:
```java
String url = "https://github.com/rabbitmq/rabbitmq-server/releases/download/rabbitmq_v3_6_6_milestone1/rabbitmq-server-mac-standalone-3.6.5.901.tar.xz";
configBuilder.downloadFrom(new URL(url), "rabbitmq_server-3.6.5.901")
```

or if you are okay with the existing artifact repositories but you just need a released version not listed in the `PredefinedVersion` enum:
```java
configBuilder.version(new BaseVersion("3.8.1"))
```

### Downloaded files:
By default, EmbeddedRabbitMq will attempt to save downloaded files to `~/.embeddedrabbitmq`. 
You can change this by making use of the `downloadTarget()` setter, which accepts both a directory or a file:
```java
configBuilder.downloadTarget(new File("/tmp/rabbitmq.tar.xz"))
...
// configBuilder.downloadTarget(new File("/tmp"))
```
_Warning:_ If a file with the same name already exists, it will be overwritten. 

The default behavior of this library is to re-use previously downloaded files. If you don't wish to use that behavior, disable it:
```java
configBuilder.useCachedDownload(false)
```

To ensure a corrupted or partially downloaded file isn't re-used, the default behavior is to delete it when the issue is detected.
This means that a fresh copy is downloaded next time. To disable this behavior do:
```java
configBuilder.deleteDownloadedFileOnErrors(false)
```

### Extraction path:
EmbeddedRabbitMq will decompress the downloaded file to a temporary folder. You can specify your own folder like so:
```java
configBuilder.extractionFolder(new File("/rabbits/"))
```
_Warning:_ The content of this folder will be overwritten every time by the newly extracted files/folders.

## Advanced RabbitMQ management

If you wish to control your RabbitMQ broker further, you can execute any of the commands available to you in the `/bin` 
directory, like so:

```java
RabbitMqCommand command = new RabbitMqCommand(config, "command", "arg1", "arg2", ...);
StartedProcess process = command.call();
ProcessResult result = process.getFuture().get();
boolean success = result.getExitValue() == 0;
if (success) {
  doSomething(result.getOutput());
}
```
where:
* `command` is something like `"rabbitmq-ctl"` (no need for `.bat` extension in Windows since it will be automatically appended).
* `args` is a variable-length array list, where each element is a word (eg. `"-n", "nodeName", "list_users"`)

See the JavaDocs for more information on `RabbitMqCommand` and other helper classes like `RabbitMqDiagnostics`, `RabbitMqCtl`, 
`RabbitMqPlugins` and `RabbitMqServer` which aim at making it easier to execute common commands.

### Enabling RabbitMQ Plugins:

To enable a plugin like `rabbitmq_management`, you can use the `RabbitMqPlugins` class like so:
```java
    RabbitMqPlugins rabbitMqPlugins = new RabbitMqPlugins(config);
    rabbitMqPlugins.enable("rabbitmq_management");
```
This call will block until the command is completed.

You can verify by executing the `list()` method:
```java
    Map<String, Plugin> plugins = rabbitMqPlugins.list();
    Plugin plugin = plugins.get("rabbitmq_management");
    assertThat(plugin, is(notNullValue()));
    assertThat(plugin.getState(), hasItem(Plugin.State.ENABLED_EXPLICITLY));
    assertThat(plugin.getState(), hasItem(Plugin.State.RUNNING));
```

You can also see which other plugins where enabled implicitly, by calling the `groupedList()`:
```java
    Map<Plugin.State, Set<Plugin>> groupedPlugins = rabbitMqPlugins.groupedList();
    Set<Plugin> plugins = groupedPlugins.get(Plugin.State.ENABLED_IMPLICITLY);
    assertThat(plugins.size(), is(not(equalTo(0))));
```

## FAQ:

## Troubleshooting:

##### Q: RabbitMQ fails to start due to `ERROR: node with name "rabbit" already running on "localhost"`. Why is this and what can I do?
  
A: This happens when RabbitMQ fails to be stopped correctly in a previous run. 
To resolve this issue, manually identify the process and terminate it. To avoid this from happening again, ensure the `stop()` 
method is invoked in your code.

##### Q: RabbitMQ fails to start with a message `erl command not found`. What's this about?

A: RabbitMQ requires an installation of Erlang to be present in the system. Please install it first.

##### Q: RabbitMQ fails to start with a message `{"init terminating in do_boot",{undef,[{rabbit_prelaunch,start,[]},{init,start_it,1},{init,start_em,1}]}}`

A: Most likely you don't have an updated version of Erlang installed. 

To check the version of Erlang in your system execute:
```
$ erl -eval 'erlang:display(erlang:system_info(otp_release)), halt().'  -noshell
```

RabbitMQ requires:
  * RabbitMQ v3.5.X requires Erlang `R13B03` at a minimum.
  * RabbitMQ v3.6.X requires Erlang `R16B03` at a minimum (or `17` if you're using SSL/TLS).

For example, if your version is `R14B04`, you can run RabbitMQ v3.5.X but not 3.6.X.

Read more here: http://www.rabbitmq.com/which-erlang.html

## Acknowledgements
This project was inspired from other excellent Open Source libraries, particularly [Embedded-MongoDB](https://github.com/flapdoodle-oss/de.flapdoodle.embed.mongo) 
and [Embedded-Redis](https://github.com/flapdoodle-oss/de.flapdoodle.embed.redis).

Big thanks to the following OSS projects that made this project possible:
 
 * [Apache Commons Compress](https://commons.apache.org/proper/commons-compress/) (and [Tukaani's XZ](http://tukaani.org/xz/) utils)
    for extraction of compressed files.
 * [Zero Turnaround's Exec framework](https://github.com/zeroturnaround/zt-exec)
    for execution of native OS processes.
 * [SLF4J API](http://www.slf4j.org/) 
    as a logging facade.

And of course, the biggest thanks to [Pivotal](https://pivotal.io/) and the [RabbitMQ](http://www.rabbitmq.com/) team for their hard work. 

## License

This project is released under [Apache License Version 2.0](http://www.apache.org/licenses/LICENSE-2.0), which basically means: 
> You can do what you like with the software, as long as you include the required notices. 
> This permissive license contains a patent license from the contributors of the code.
  
_Summary courtesy of: <https://tldrlegal.com/license/apache-license-2.0-(apache-2.0)>_ 

## Say thanks

If you want to say thanks, you can:
 * Star this project
 * Donate: 
   * [PayPal](https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=alejandro%2erivera%2elopez%40gmail%2ecom&lc=US&item_name=io%2earivera%2eoss&item_number=embedded%2drabbitmq&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHostedGuest)
 * Contribute improvements
 

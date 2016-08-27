# Embedded RabbitMQ

[![Build Status](https://travis-ci.org/AlejandroRivera/embedded-rabbitmq.svg?branch=master)](https://travis-ci.org/AlejandroRivera/embedded-rabbitmq)
[![Build status](https://ci.appveyor.com/api/projects/status/r46o01l4ora7ppkf?svg=true)](https://ci.appveyor.com/project/AlejandroRivera/embedded-rabbitmq)
[![Coverage Status](https://coveralls.io/repos/github/AlejandroRivera/embedded-rabbitmq/badge.svg)](https://coveralls.io/github/AlejandroRivera/embedded-rabbitmq)
[![Gitter chat](https://badges.gitter.im/gitterHQ/gitter.png)](https://gitter.im/io-arivera-oss/embedded-rabbitmq)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.arivera.oss/embedded-rabbitmq/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.arivera.oss/embedded-rabbitmq)

This library allows for the use of various RabbitMQ versions as if it was an embedded service that can be controlled 
from within the JVM.

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

For Ivy:
```xml
<dependency org="io.arivera.oss" name="embedded-rabbitmq" rev="X.Y.Z" />
```

For Gradle:
```
compile 'io.arivera.oss:embedded-rabbitmq:X.Y.Z'
```

For SBT:
```
libraryDependencies += "io.arivera.oss" % "embedded-rabbitmq" % "X.Y.Z"
```

`X.Y.Z` is the latest released version of this project. 
For more info visit [Maven Central repo](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22io.arivera.oss%22%20AND%20a%3A%22embedded-rabbitmq%22) 
or visit the [releases](https://github.com/AlejandroRivera/embedded-rabbitmq/releases) page.

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

### Define a version to use:
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

Similarly, if you wish to download another version and/or use another server:
```java
String url = "https://github.com/rabbitmq/rabbitmq-server/releases/download/rabbitmq_v3_6_6_milestone1/rabbitmq-server-mac-standalone-3.6.5.901.tar.xz";
configBuilder.downloadFrom(new URL(url), "rabbitmq_server-3.6.5.901")
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

## License

This project is released under [Apache License Version 2.0](http://www.apache.org/licenses/LICENSE-2.0), which basically means: 
> You can do what you like with the software, as long as you include the required notices. 
> This permissive license contains a patent license from the contributors of the code.
  
_Summary courtesy of: <https://tldrlegal.com/license/apache-license-2.0-(apache-2.0)>_ 

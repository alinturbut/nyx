---
title: Nyx Main
layout: single
toc: true
permalink: /guide/developer/java/nyx-main/
---

[![Maven Central](https://img.shields.io/maven-central/v/com.mooltiverse.oss.nyx/main.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.mooltiverse.oss.nyx%22%20AND%20a:%22main%22) [![javadoc](https://javadoc.io/badge2/com.mooltiverse.oss.nyx/main/javadoc.svg)](https://javadoc.io/doc/com.mooltiverse.oss.nyx/main)

You can use the main Nyx library to embed it into your project and use all or some of its features. The `com.mooltiverse.oss.nyx` package brings the [`Nyx`](https://javadoc.io/doc/com.mooltiverse.oss.nyx/main/latest/com/mooltiverse/oss/nyx/Nyx.html){:target="_blank"} class that is the entry point for all the available commands and features.

## Get the library

### Manual download
You can download the jar file directly from the [Maven Central](https://repo.maven.apache.org/maven2/com/mooltiverse/oss/nyx/main/){:target="_blank"} repository or by using the [Maven Central repository search engine](https://search.maven.org/artifact/com.mooltiverse.oss.nyx/main){:target="_blank"}. The [GitHub Packages](https://github.com/mooltiverse/nyx/packages/){:target="_blank"} repository is the other source where you can get it.

In order to also download the correct dependencies you should use one of the automatic tools below. If you really want to download manually take a look at the [`pom`](https://repo.maven.apache.org/maven2/com/mooltiverse/oss/nyx/main/{{ site.data.nyx.version }}/main-{{ site.data.nyx.version }}.pom){:target="_blank"} to see the dependencies to download.
{: .notice--info}

### Using Maven
When using Maven just add the following dependency to your `POM`:

```xml
<dependency>
  <groupId>com.mooltiverse.oss.nyx</groupId>
  <artifactId>main</artifactId>
  <version>{{ site.data.nyx.version }}</version>
</dependency>
```

Your local Maven setup will likely use the [Maven Central](https://repo.maven.apache.org/maven2/com/mooltiverse/oss/nyx/main/) repository by default but if you like to use the [GitHub Packages](https://github.com/mooltiverse/nyx/packages/) repository you can follow [these instructions](https://help.github.com/en/packages/using-github-packages-with-your-projects-ecosystem/configuring-apache-maven-for-use-with-github-packages).
{: .notice--info}

### Using Ivy
When using Ivy just add the following dependency:

```xml
<dependency org="com.mooltiverse.oss.nyx" name="main" rev="{{ site.data.nyx.version }}" />
```

### Using Gradle

If you're using the Groovy DLS add this to the dependencies your script:

```groovy
implementation 'com.mooltiverse.oss.nyx:main:{{ site.data.nyx.version }}'
```

while if you're using the Kotlin DSL use this dependency:

```kotlin
implementation("com.mooltiverse.oss.nyx:main:{{ site.data.nyx.version }}")
```

## API docs

Thanks to [javadoc.io](https://javadoc.io/) you can browse the Javadoc API at [this URL](https://javadoc.io/doc/com.mooltiverse.oss.nyx/main).

## Using the library

Using the library is simple. You just need to create a [`Nyx`](https://javadoc.io/doc/com.mooltiverse.oss.nyx/main/latest/com/mooltiverse/oss/nyx/Nyx.html){:target="_blank"} instance and run the `publish` command. It takes just one line of code, like:

```java
import com.mooltiverse.oss.nyx.Nyx;

public class Test {
    static void main(String[] args)
        throws Exception {
        new Nyx().publish(); // This is it!
    }
}
```

In this example Nyx loads the configuration from the files it optionally finds at their [default locations]({{ site.baseurl }}{% link _pages/guide/user/02.introduction/configuration-methods.md %}#evaluation-order) and runs the `publish` command, which also implies `infer`, `mark` and `make`.

You can get more control on the behavior by injecting some configuration programmatically and running tasks one by one. You can also start Nyx in a specific directory, get access to the internal Git [repository](https://javadoc.io/doc/com.mooltiverse.oss.nyx/main/latest/com/mooltiverse/oss/nyx/git/Repository.html){:target="_blank"} object and even the internal [state](https://javadoc.io/doc/com.mooltiverse.oss.nyx/main/latest/com/mooltiverse/oss/nyx/state/State.html){:target="_blank"}, like in this example:

```java
import com.mooltiverse.oss.nyx.Nyx;
import com.mooltiverse.oss.nyx.configuration.SimpleConfigurationLayer;

public class Test {
    static void main(String[] args)
        throws Exception {
        File customDirectory = new File("~/project"); 
        Nyx nyx = new Nyx(customDirectory); // Nyx now runs on the '~/project' directory

        // Create a new configuration layer, set some options, and add it on top
        // of other layers at the 'command line' layer level
        SimpleConfigurationLayer configurationLayer = new SimpleConfigurationLayer();
        configurationLayer.setDryRun​(Boolean.TRUE); // make it run dry
        configurationLayer.setReleasePrefix​("rel"); // make it use 'rel' as the prefix for generated versions
        nyx.configuration().withCommandLineConfiguration(configurationLayer); // inject the configuration

        nyx.infer(); // let Nyx infer values from the Git repository

        // now we have plenty of values in the State, let's read some...
        System.out.println(nyx.state().getBranch());
        System.out.println(nyx.state().getVersion());

        // it might be a good place to run some custom tasks of yours, i.e. using the Git Repository
        // let's say you create a RELEASE_NOTES.md file and want to commit it
        nyx.repository().commit​("Adding RELEASE_NOTES.md");

        // then run the remaining tasks one by one
        nyx.make();
        nyx.mark();
        nyx.publish();
    }
}
```

### Logging

Nyx uses [SLF4J](http://www.slf4j.org/) for logging and since it's an adapter to various [logging frameworks](http://www.slf4j.org/manual.html#swapping) it doesn't address any implementation specific setting or feature. What Nyx does is just sending log messages to SLF4J, but what actually happens behind SLF4J is out of Nyx's scope.

This means that if you're using one of the SLF4J [supported frameworks](http://www.slf4j.org/manual.html#swapping) you can have Nyx emit its logs conforming to the rest of your application. If you don't, just deploy and configure one of the supported frameworks along with Nyx.

This is why the [`verbosity`]({{ site.baseurl }}{% link _pages/guide/user/03.configuration-reference/global-options.md %}#verbosity) configuration option is ignored by the Java implementation of Nyx.

Log events are decorated with markers to let you categorize, colorize and filter them if you wish. The list of used markers is modelled in the [`Markers` class](https://javadoc.io/doc/com.mooltiverse.oss.nyx/main/latest/com/mooltiverse/oss/nyx/log/Markers.html).

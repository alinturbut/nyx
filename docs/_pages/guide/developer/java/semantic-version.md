---
title: Semantic Version
layout: single
toc: true
permalink: /guide/developer/java/semantic-version/
---

[![Maven Central](https://img.shields.io/maven-central/v/com.mooltiverse.oss.nyx/version.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.mooltiverse.oss.nyx%22%20AND%20a:%22version%22) [![Javadoc](https://javadoc.io/badge2/com.mooltiverse.oss.nyx/version/Javadoc.svg)](https://javadoc.io/doc/com.mooltiverse.oss.nyx/version)

The `com.mooltiverse.oss.nyx.version` package provides you with a fully featured implementation of the [Semantic Versioning (SemVer)](https://semver.org/) specification without any external dependency. Full support for parsing, ordering, bumping and manipulating versions and their identifiers is provided.

This library is used at the Nyx core but yet is completely independent from its logic so is perfectly suitable for use in ano other context.

## Get the library

### Manual download
You can download the jar file directly from the [Maven Central](https://repo.maven.apache.org/maven2/com/mooltiverse/oss/nyx/version/){:target="_blank"} repository or by using the [Maven Central repository search engine](https://search.maven.org/artifact/com.mooltiverse.oss.nyx/version){:target="_blank"}. The [GitHub Packages](https://github.com/mooltiverse/nyx/packages/){:target="_blank"} repository is the other source where you can get it.

### Using Maven
When using Maven just add the following dependency to your `POM`:

```xml
<dependency>
  <groupId>com.mooltiverse.oss.nyx</groupId>
  <artifactId>version</artifactId>
  <version>{{ site.data.nyx.version }}</version>
</dependency>
```

Your local Maven setup will likely use the [Maven Central](https://repo.maven.apache.org/maven2/com/mooltiverse/oss/nyx/version/) repository by default but if you like to use the [GitHub Packages](https://github.com/mooltiverse/nyx/packages/) repository you can follow [these instructions](https://help.github.com/en/packages/using-github-packages-with-your-projects-ecosystem/configuring-apache-maven-for-use-with-github-packages).
{: .notice--info}

### Using Ivy
When using Ivy just add the following dependency:

```xml
<dependency org="com.mooltiverse.oss.nyx" name="version" rev="{{ site.data.nyx.version }}" />
```

### Using Gradle

If you're using the Groovy DLS add this to the dependencies your script:

```groovy
implementation 'com.mooltiverse.oss.nyx:version:{{ site.data.nyx.version }}'
```

while if you're using the Kotlin DSL use this dependency:

```kotlin
implementation("com.mooltiverse.oss.nyx:version:{{ site.data.nyx.version }}")
```

## API docs

Thanks to [javadoc.io](https://javadoc.io/) you can browse the Javadoc API at [this URL](https://javadoc.io/doc/com.mooltiverse.oss.nyx/version).

## Using the library

Using the library is extremely simple. Just import the [`SemanticVersion`](https://javadoc.io/doc/com.mooltiverse.oss.nyx/version/latest/com/mooltiverse/oss/nyx/version/SemanticVersion.html){:target="_blank"} class and use it as in the following example.

**Remember the `SemanticVersion` class is immutable** so every time you invoke a method that changes the number you actually get a new object representing the new state.

```java
import com.mooltiverse.oss.nyx.version.SemanticVersion;

public class Test {
    static void main(String[] args){
        // parse any string as a semantic version, in this case we use the default initial version "0.1.0"
        SemanticVersion v1 = SemanticVersion.valueOf(SemanticVersion.DEFAULT_INITIAL_VERSION);
        // bump the MINOR number > "0.2.0"
        SemanticVersion v2 = v1.bumpMinor();
        // bump the MAJOR number > "1.0.0"
        SemanticVersion v3 = v2.bumpMajor();
        // bump the new pre-release identifier named 'alpha' > "1.0.0-alpha.1"
        SemanticVersion v4 = v3.bumpPrerelease("alpha");
        // bump the 'alpha' pre-release identifier again > "1.0.0-alpha.2"
        SemanticVersion v5 = v4.bumpPrerelease("alpha");
        // add the 'build' identifier and give it the '123' value > "1.0.0-alpha.2+build.123"
        SemanticVersion v6 = v5.setBuild("build", "123")
        // and so on...
    }
}
```

The `SemanticVersion` class also provides additional useful methods to handle arbitrary identifiers so that it's even more expressive. For example:

```java
        SemanticVersion v1 = SemanticVersion.valueOf("2.0.3");
        SemanticVersion v2 = v1.setBuildAttribute("timestamp", "20200101T1500");    // v2 is now "2.0.3+timestamp.20200101T1500"
        // but in another run you want to overwrite the attribute with the same name, so...
        SemanticVersion v3 = v2.setBuildAttribute("timestamp", "20991201T2359");    // v3 is now "2.0.3+timestamp.20991201T2359"
        // or add a branch name and a tag value in the pre-release part, one as a simple attribute and the other as a named pair
        SemanticVersion v4 = v3.setPrereleaseAttribute("develop").setPrereleaseAttribute("tag", 3);
        // v4 is now "2.0.3-develop.tag.3+timestamp.20991201T2359"
```
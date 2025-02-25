---
title: Version Schemes
layout: single
toc: true
permalink: /guide/user/introduction/version-schemes/
---

## Background

One of the fundamental decisions you have to make about your project is about the version scheme to use. Version schemes are ultimately naming conventions that give clear semantics to version identifiers. Without a clear version scheme your releases may be inconsistent and not clear to the audience in terms of their order, what to expect in terms of changes, compatibility and stability.

## Version schemes

Nyx supports and complies with [Semantic Versioning](#semantic-versioning-semver) and [will support](https://github.com/mooltiverse/nyx/issues/4) the [Maven scheme](#maven) in the future. Unless you need to stick with Maven for historical reasons, you should definitely use SemVer.

The above statement is about the versioning strategy only. You can still use Maven as a build tool even when using Semantic Versioning.
{: .notice--info}

### Semantic Versioning (SemVer)

[Semantic Versioning](https://semver.org/) has become the standard *de-facto* as it's the most expressive scheme and defines clear rules about version ordering and expectations.

On the other hand *SemVer* is flexible enough to support extra identifiers to decorate a version so that it can show additional attributes intuitively, like flavors, maturity, timestamps and much more. What is important here is that naming rules are clear enough to be managed automatically.

*SemVer* is the default versioning scheme used by Nyx. This also implies that all presets are based on *SemVer*.

#### Default initial version

The [default initial version for Semantic Versioning](https://semver.org/#how-should-i-deal-with-revisions-in-the-0yz-initial-development-phase) is `0.1.0`.

#### Identifier names

As per the [Semantic Versioning](https://semver.org/) specification, a version always has at least the `core` identifier, which in turn is made of three simple identifiers: `major`, `minor` and `patch`. For example a version `1.2.3` has the identifiers:

* `core` = `1.2.3`
* `major` = `1`
* `minor` = `2`
* `patch` = `3`

Other optional identifiers are allowed in the `pre-release` and the `build` identifiers but they don't have specific names.

### Maven

Maven Versioning scheme is not supported yet. See [this issue](https://github.com/mooltiverse/nyx/issues/4){: .btn .btn--primary} to know more about the progress and schedule or vote.
{: .notice--warning}

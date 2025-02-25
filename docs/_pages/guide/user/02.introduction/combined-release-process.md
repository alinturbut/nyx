---
title: Combined Release Process
layout: single
toc: true
permalink: /guide/user/introduction/combined-release-process/
---

In a simplistic way the release process is atomic and happens at the end of the overall build process, meaning that the commit history can be inspected to infer values like the version number to release, apply a tag to the repository and publish to remote platforms.

Real world, on the other hand, is slightly different because most of the times you need the inferred values like the current version number in order to run other build tasks. For example you may need the version number to name your artifacts, render the documentation, send notifications and so on.

This means that the release process needs to be more granular to meet the needs of real world build processes and this is where most of the release tools out there fall short and where Nyx can make a difference.

Nyx offer you fine control on its tasks by running them within specific [commands]({{ site.baseurl }}{% link _pages/guide/user/02.introduction/how-nyx-works.md %}). Each commnand runs a subset of tasks but also makes sure that when running a command, its required tasks dependencies are executed first (unless they have already been) so you don't need to worry about the sequence you invoke them with.

Let's assume you have a typical scenario in which you need to:

1. infer the new version number based on the commit history
2. run other tasks (some of which may be conditional, depending on what has been inferred in step 1) to build your artifacts, documentation etc
3. tag (and optionally push) the Git repository
4. (optionally) publish the release

Building the actual artifacts is out of scope for Nyx and depends on your build process but all other tasks can be taken care of automatically.

To accomplish the above, you run the [Infer]({{ site.baseurl }}{% link _pages/guide/user/02.introduction/how-nyx-works.md %}#infer) command first to get a new version number. At this time Nyx doesn't apply any change to your repository nor publishes anything, it just reads the commit history to produce a new version number which is consistent with the commit history and the configuration. The details on how you can get the generated version number depend on the means you use Nyx and are detailed in the next sections.

If Nyx finds a previous State file from a previous run and the repository has no changes since, the previous State can be resumed from where it left.

Once *Infer* gave you the version number you can proceed with your own build tasks, optionally running Nyx [Make]({{ site.baseurl }}{% link _pages/guide/user/02.introduction/how-nyx-works.md %}#make) (i.e. to build the the [changelog]({{ site.baseurl }}{% link _pages/guide/user/03.configuration-reference/changelog.md %})). Once ready you can run Nyx [Publish]({{ site.baseurl }}{% link _pages/guide/user/02.introduction/how-nyx-works.md %}#publish) to complete the release process or just [Mark]({{ site.baseurl }}{% link _pages/guide/user/02.introduction/how-nyx-works.md %}#mark) to commit, tag and push changes. The actual actions that will execute depend on the configuration you give Nyx.

## Configuration

When running Nyx in separate stages you need to [enable]({{ site.baseurl }}{% link _pages/guide/user/03.configuration-reference/global-options.md %}#state-file) writing the [state file]({{ site.baseurl }}{% link _pages/guide/user/05.state-reference/index.md %}) and [resuming]({{ site.baseurl }}{% link _pages/guide/user/03.configuration-reference/global-options.md %}#resume) from where the process was left.

These two options are key to correlate separate executions, not just when using multi-stage builds but also when combining different flavors of Nyx.

## Using the command line or the Docker image

You can run Nyx at separate stages like:

```bash
$ nyx --state-file=nyx-state.json --resume infer
[run your build tasks here to create artifacts, docs etc]
$ nyx --state-file=nyx-state.json --resume publish
```

In case you need a structured way to fetch the data inferred at the first run you can get it from the [state file]({{ site.baseurl }}{% link _pages/guide/user/05.state-reference/index.md %}). Within the state file you can also find plenty of other structured information about the release process.

Another way to get inferred information is to read the console output from the [summary]({{ site.baseurl }}{% link _pages/guide/user/03.configuration-reference/global-options.md %}#summary) or read the [summary file]({{ site.baseurl }}{% link _pages/guide/user/03.configuration-reference/global-options.md %}#summary-file).

For the sake of this page, using the Docker image makes no difference as you just need to pass the same command line arguments to Nyx.

## Using the Gradle plugin

With Gradle you can run [tasks]({{ site.baseurl }}{% link _pages/guide/user/02.introduction/usage.md %}#core-tasks) explicitly or by means of dependencies for your own custom tasks to craft your build process.

The version generated by the [Infer]({{ site.baseurl }}{% link _pages/guide/user/02.introduction/how-nyx-works.md %}#infer) command is set as the [Gradle's standard `version` project property](https://docs.gradle.org/current/userguide/writing_build_scripts.html#sec:standard_project_properties) /see [Using the Gradle plugin]({{ site.baseurl }}{% link _pages/guide/user/02.introduction/usage.md %}#using-the-gradle-plugin)). All other insights can be accessed through the [`nyxState`]({{ site.baseurl }}{% link _pages/guide/user/02.introduction/usage.md %}#accessing-the-nyx-state-extra-project-property-from-build-scripts) property.

## Using the GitHub Action

You can run Nyx at separate steps within the same job like:

```yaml
jobs:
  job1:
    name: My job
    runs-on: ubuntu-latest
    steps:
    - name: Git checkout
      uses: actions/checkout@v3
      with:
        fetch-depth: 0
    - name: Run nyx Infer
      uses: mooltiverse/nyx-github-action@main
      with:
        command: infer
        resume: true
        stateFile: .nyx-state.json
    # Run other tasks here....
    - name: Run nyx Publish
      uses: mooltiverse/nyx-github-action@main
      with:
        command: publish
        resume: true
        stateFile: .nyx-state.json
```

Or using separate jobs (note that we also use the [cache action](https://github.com/actions/cache) to bring the state file across different jobs):

```yaml
jobs:
  job1:
    name: My job 1
    runs-on: ubuntu-latest
    steps:
    - name: Git checkout
      uses: actions/checkout@v3
      with:
        fetch-depth: 0
    - name: Set up the cache to store and retrieve the Nyx state
      uses: actions/cache@v3
      with:
        path: |
          .nyx-state.json
        key: ${{ github.run_id }}-nyx-state
        restore-keys: ${{ github.run_id }}-nyx-state
    - name: Run nyx Infer
      uses: mooltiverse/nyx-github-action@main
      with:
        command: infer
        resume: true
        stateFile: .nyx-state.json

  job2:
    name: My job 2
    needs: job1
    runs-on: ubuntu-latest
    steps:
    - name: Git checkout
      uses: actions/checkout@v3
      with:
        fetch-depth: 0
    - name: Set up the cache to store and retrieve the Nyx state
      uses: actions/cache@v3
      with:
        path: |
          .nyx-state.json
        key: ${{ github.run_id }}-nyx-state
        restore-keys: ${{ github.run_id }}-nyx-state
    - name: Run nyx Publish
      uses: mooltiverse/nyx-github-action@main
      with:
        command: publish
        resume: true
        stateFile: .nyx-state.json
```

Please refer to the [Nyx GitHub Action](https://github.com/mooltiverse/nyx-github-action#combined-release-process) for more.

## Using a combination of Nyx flavors

The above shows how Nyx makes available specific commands for different stages of a build process and how the overall process can mix Nyx and other tools. This includes the case where different flavors or Nyx may be needed together.

Thanks to the fact that [state files]({{ site.baseurl }}{% link _pages/guide/user/05.state-reference/index.md %}) and [configuration files and environment variables]({{ site.baseurl }}{% link _pages/guide/user/02.introduction/configuration-methods.md %}) are platform and flavor agnostic, the build process can use different flavors seamlessly.

Often times you don't need to repeat the same build steps using different flavors but in order to keep your build scripts tidy and easier to maintain you need Nyx to provide information the easy way, depending on the build environment and script.

For example you may need to use a mix of the [Gradle plugin]({{ site.baseurl }}{% link _pages/guide/user/02.introduction/usage.md %}#using-the-gradle-plugin) and the [GitHub Action]({{ site.baseurl }}{% link _pages/guide/user/02.introduction/usage.md %}#using-the-github-action) or the [command line]({{ site.baseurl }}{% link _pages/guide/user/02.introduction/usage.md %}#using-the-command-line) and the [Docker image]({{ site.baseurl }}{% link _pages/guide/user/02.introduction/usage.md %}#using-the-docker-image). Any combination is possible as long as you follow the steps above.

For concrete examples you can check out the [Combined use examples]({{ site.baseurl }}{% link _posts/2020-01-01-combined-use-example.md %}).

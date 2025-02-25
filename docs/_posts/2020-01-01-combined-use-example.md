---
layout: single
toc: true
title:  "Combined use examples"
date:   2020-01-01 00:00:00 +0000
categories: example user
tags: support configuration gradle github actions command line
---

Here are a few examples of using a [combination of Nyx flavors]({{ site.baseurl }}{% link _pages/guide/user/02.introduction/combined-release-process.md %}#using-a-combination-of-nyx-flavors) together in the same build process.

The following use cases are just examples and can be inverted and recombined in any case of multi-stage builds.

## GitHub Actions and Gradle

Here we show the usage of a [GitHub Actions]({{ site.baseurl }}{% link _pages/guide/user/02.introduction/usage.md %}#using-the-github-action) pipeline, running a combination of tasks defined in Gradle scripts (also using the Nyx [Gradle plugin]({{ site.baseurl }}{% link _pages/guide/user/02.introduction/usage.md %}#using-the-gradle-plugin)) and other Actions.

In this scenario we need Nyx both within Gradle scripts and the Actions pipeline because both need the [`version`]({{ site.baseurl }}{% link _pages/guide/user/05.state-reference/global-attributes.md %}#version) attribute inferred from Nyx and also use conditionals for jobs to run only when a [new release]({{ site.baseurl }}{% link _pages/guide/user/05.state-reference/global-attributes.md %}#new-release) has to be published.

Let's give Nyx a [configuration]({{ site.baseurl }}{% link _pages/guide/user/02.introduction/configuration-methods.md %}), using the standard name `.nyx.yaml` so we don't need to pass a custom [`configurationFile`]({{ site.baseurl }}{% link _pages/guide/user/03.configuration-reference/global-options.md %}#configuration-file).

```yaml
resume: true
stateFile: "build/.nyx-state.yml"
```

here we just give the two options that are [relevant for a combined use]({{ site.baseurl }}{% link _pages/guide/user/02.introduction/combined-release-process.md %}#configuration).

Now take a look at the Gradle scripts. Here we [apply the plugin]({{ site.baseurl }}{% link _pages/guide/user/02.introduction/usage.md %}#apply-the-plugin) to the `settings.gradle` file (using the `settings.gradle` runs [Infer]({{ site.baseurl }}{% link _pages/guide/user/02.introduction/how-nyx-works.md %}#infer) implicitly in the early stage of the build):

```groovy
plugins {
  id "com.mooltiverse.oss.nyx" version "{{ site.data.nyx.version }}"
}
```

and here in the `build.gradle` file we define a job that reads the [`version`]({{ site.baseurl }}{% link _pages/guide/user/05.state-reference/global-attributes.md %}#version) attribute from the global project properties but runs only when a [new release]({{ site.baseurl }}{% link _pages/guide/user/05.state-reference/global-attributes.md %}#new-release) has to be published (reading the flag from the [`nyxState`]({{ site.baseurl }}{% link _pages/guide/user/02.introduction/usage.md %}#accessing-the-nyx-state-extra-project-property-from-build-scripts)):

```groovy
// This task is just an example
task preRelease() {
    // This task will not run unless "newRelease" is set to true by Nyx
    onlyIf { rootProject.nyxState.newRelease }
    doLast {
        // Access the project version property
        logger.info("Project version is: ${rootProject.version}")
        // Run any other task you may need here
        ...
    }
}
```

The `rootProject` prefix is always safe to use but you can mot it unless you're working on a [multi-project build]({{ site.baseurl }}{% link _pages/guide/user/02.introduction/usage.md %}#multi-project-builds).

So far nothing new, we have a Gradle build script that we can [run locally or on any CI/CD platform]({{ site.baseurl }}{% link _pages/guide/user/06.best-practice/build-and-automation.md %}#cicd-vs-local-scripts). Now let's see what we can do in the GitHub Actions workflow:

```yaml
name: My workflow
on: [push]

jobs:
  one:
    name: Initialize
    runs-on: ubuntu-latest
    steps:
    # Checkout the entire repository and tags to let Nyx inspect the whole repository
    - name: Git checkout
      uses: actions/checkout@v3
      with:
        fetch-depth: 0
    # Java is required for Gradle, don't pay attention to the version
    - name: Set up JDK 19
      uses: actions/setup-java@v3
      with:
        distribution: 'temurin'
        java-version: 19
    # This job only runs nyxInfer to produce the initial version and write the state file
    # Since Nyx is applied as a settings plugin the nyxInfer task is not even needed, but Gradle needs a task to run
    - name: Nyx Infer
      run: ./gradlew nyxInfer
    # Here we use the Nyx GitHub Action just to read the data from its state file (generated in the above step)
    - name: Load Nyx data
      id: nyx
      uses: mooltiverse/nyx-github-action@main
      # The following parameters are here just to show but 'infer' is the default command while the other options are set in the configuration file
      #with:
      #  command: infer
      #  resume: true
      #  stateFile: .nyx-state.json
    # Run the Gradle task. This will not run Infer again because the state file has been produced in the previous step
    - name: Pre release
      run: ./gradlew preRelease
    # Run another task
    - name: Post release
      # This step will run only if Nyx has determined that a new release must be issued
      if: ${{ steps.nyx.outputs.newRelease }}
      run: <some other command here>
```

And here is a slightly modified version, showing the same steps but spread into multiple jobs. As you can see, using different jobs adds additional needs like a cache to bring the Nyx state ahead from one job to another and the declaration of job outputs:

```yaml
name: My workflow
on: [push]

jobs:
  one:
    name: Initialize
    runs-on: ubuntu-latest
    outputs:
      # Make the 'newRelease' outputs from the Nyx action available for other jobs
      newRelease: ${{ steps.nyx.outputs.newRelease }}
    steps:
    # Checkout the entire repository and tags to let Nyx inspect the whole repository
    - name: Git checkout
      uses: actions/checkout@v3
      with:
        fetch-depth: 0
    # Java is required for Gradle, don't pay attention to the version
    - name: Set up JDK 19
      uses: actions/setup-java@v3
      with:
        distribution: 'temurin'
        java-version: 19
    # Set up a cache that stores the Nyx state so that other jobs can retrieve it
    - name: Set up the Nyx state cache
      uses: actions/cache@v3
      with:
        path: |
          build/
        key: nyx-state
        #restore-keys: not used here, start from scratch
    # This job only runs nyxInfer to produce the initial version and write the state file
    # Since Nyx is applied as a settings plugin the nyxInfer task is not even needed, but Gradle needs a task to run
    - name: Nyx Infer
      run: ./gradlew nyxInfer
    # Here we use the Nyx GitHub Action just to read the data from its state file (generated in the above step)
    - name: Load Nyx data
      id: nyx
      uses: mooltiverse/nyx-github-action@main
      # The following parameters are here just to show but 'infer' is the default command while the other options are set in the configuration file
      #with:
      #  command: infer
      #  resume: true
      #  stateFile: .nyx-state.json

  two:
    name: Pre release
    needs: one
    # We don't actually need this conditional statement here because the gradle task uses the 'onlyIf' clause
    #if: ${{ needs.init.outputs.newRelease }}
    runs-on: ubuntu-latest
    steps:
    # Checkout the entire repository and tags to let Nyx inspect the whole repository
    - name: Git checkout
      uses: actions/checkout@v3
      with:
        fetch-depth: 0
    # Java is required for Gradle, don't pay attention to the version
    - name: Set up JDK 19
      uses: actions/setup-java@v3
      with:
        distribution: 'temurin'
        java-version: 19
    # Set up a cache that stores the Nyx state so that other jobs can retrieve it
    - name: Set up the Nyx state cache
      uses: actions/cache@v3
      with:
        path: |
          build/
        key: nyx-state
        restore-keys: |
          nyx-state
    # Run the Gradle task. This will not run Infer again because the state file, produced in the previous job, is retrieved from the cache
    - name: Pre release
      run: ./gradlew preRelease

  three:
    name: Post release
    needs: one
    # This job will run only if Nyx has determined that a new release must be issued
    if: ${{ needs.init.outputs.newRelease }}
    runs-on: ubuntu-latest
    steps:
    # Run another task
    - name: Post release
      run: <some other command here>
```

The two steps *Nyx Infer* and *Load Nyx data* in the above examples could be applied in any order as the first one actually creates the State file while the second loads it.
{: .notice--info}

When using conditionals in GitHub Actions you may also find the [`dorny/paths-filter`](https://github.com/dorny/paths-filter) Action useful.
{: .notice--success}

## Command line and Gradle

Here we run Nyx primarily from the command line but then we need an easy way to use its outputs from Gradle build scripts in a structured way instead of scraping the output or parsing the output file.

The Nyx and Gradle settings look just like the above example, so:

`.nyx.yaml`:

```yaml
resume: true
stateFile: "build/.nyx-state.yml"
```

`settings.gradle`:

```groovy
plugins {
  id "com.mooltiverse.oss.nyx" version "{{ site.data.nyx.version }}"
}
```

`build.gradle`:

```groovy
// This task is just an example
task preRelease() {
    // This task will not run unless "newRelease" is set to true by Nyx
    onlyIf { rootProject.nyxState.newRelease }
    doLast {
        // Access the project version property
        logger.info("Project version is: ${rootProject.version}")
        // Run any other task you may need here
        ...
    }
}
```

Then, when running:

```bash
$ nyx
$ ./gradlew preRelease
Project version is: 1.2.3
```
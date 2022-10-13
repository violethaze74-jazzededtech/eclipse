# Project Settings for Eclipse

This project contains the canonical set of Eclipse `.settings/`
files for Cloud Tools for Eclipse.  These settings configure Eclipse
to use the [Google Style for Java][google-style-java].

The settings currently reflect the following choices:

  - uses the [Google Java Format plugin for Eclipse](google-java-format),
    which requires Eclipse Oxygen (4.7) or later.
  - unnecessary declared exceptions on methods are _errors_
  - switch missing default case are _warnings_
  - resource leaks are _errors_
  - potential resource leaks are _warnings_
  - serializable classes without `serialVersionUID` are _ignored_
  - redundant generic type argument are _errors_

Our projects are configured to re-apply these changes frequently.

## Updating the Settings

The settings require installing the [Google Java Format plugin for
Eclipse][google-java-format].  Installing the format plugin requires
downloading the [latest release][google-java-format-release] (named
`google-java-format-eclipse-plugin_XXXX.jar`)and placing the jar
in your Eclipse installation's `dropins/` directory (on MacOS, in
`Eclipse.app/Content/Eclipse/dropins/`).

Updating and applying the settings files is a two-step process based
around the [`eclipse-settings-maven-plugin`][esmp] Maven plugin.

  [esmp]: https://github.com/BSI-Business-Systems-Integration-AG/eclipse-settings-maven-plugin
  [google-style-java]: https://google.github.io/styleguide/javaguide.html
  [google-java-format]: https://github.com/google/google-java-format
  [google-java-format-release]: https://github.com/google/google-java-format/releases

### Step 0: Change the Canonical Settings

To update the settings, change the settings for one project and
then copy in the changed files from its `.settings/` directory into
`files/`.  Note that if a new file is added, the new file must be
reflected into the `eclipse-settings-maven-plugin`'s configuration
(defined in `plugins/pom.xml`).

### Step 1: Publishing the Settings Files

This project (`eclipse/settings/`) publishes the settings files as
a Maven `jar` artifact.  This published jar is then used by the
`eclipse-settings-maven-plugin` as the source for the settings files.
The settings files themselves are found in in this project under
[`files/`](files/).

To publish the settings, update this project's version, commit, and
then run the following to assemble the jar and deploy it to the
canonical repository.

```sh
$ cd eclipse/settings
$ mvn deploy
```

### Step 2: Applying the Settings

Once the settings are available, applying the changes requires
updating the `eclipse-settings-maven-plugin` dependency version in
`plugins/pom.xml`.  M2Eclipse should then automatically trigger the
`eclipse-settings-maven-plugin:eclipse-settings` goal to apply the
changes.

sbt-imagej
==========

sbt-imagej is an [SBT](http://www.scala-sbt.org/) (Simple Build Tool) plugin that that helps with development of
[ImageJ](http://rsbweb.nih.gov/ij/) plugins (those are different than SBT plugins).

The main task `imagej-run`, or `imagejRun`, does following:

1. Builds your ImageJ plugin
2. Creates directory structure expected by ImageJ
3. Copies the plugin jar to ImageJ plugins directory, along with all dependencies
4. Starts ImageJ instance that is aware of the new plugin location,
   so you can interactively test your plugin.

Setup
-----

For sbt 0.12 and 0.13 add `sbt-imagej` as a dependency in `project/imagej.sbt`:

```scala
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

addSbtPlugin("net.sf.ij-plugins" % "sbt-imagej" % "1.0.0")
```

Usage
-----

### Applying the Plugin to a Project (Adding the `imagej-run` Task)

First, make sure that you've added the plugin to your build, as described above.


If you're using `build.sbt` add this:

```scala
import ImageJKeys._ // put this at the top of the file

imageJSettings
```

Now you'll have a new `imagej-run` task which will compile your project,
pack your class files and resources in a jar, copy that jar and dependencies to local
ImageJ's plugins directory, and run ImageJ

    > imagej-run

There is also a task that only copies the jar and dependencies to to the plugins directory

    > imagej-prepare-run

You can customize directory used to run ImageJ and load plugins:

* `imageJRuntimeDir` - Location of ImageJ runtime directory relative to base directory.
  Default value is `sandbox`
* `imageJPluginsSubDir` - Subdirectory of the `plugins` directory, where all `jar`s will be copied.
  Default is `jars`
* `imageJExclusions` - List of regex expressions that match JARs that will be excluded from the plugins directory.
  Default excludes ImageJ jar, source jars, and javadoc/scaladoc jars.

For example the name of the jar can be set as follows in build.sbt:

```scala
imageJRuntimeDir := "sandbox"

imageJPluginsSubDir := "my-plugin"

imageJExclusions += """some\.jar"""
```

The above configuration will copy your jar file and dependencies to
`sandbox/plugins/my-plugin`, and additionally exclude the `some.jar`.
ImageJ will be instructed to use `sandbox` as its home directory.

### Multi-Module Projects###

If you are using a multi-module projects and would like to include dependent project jars in the plugins directory
you need to take extra steps. When a SBT creates a classpath from dependent projects it exports a directory containing its
resources and compiled class files, but not the actual jar produced by that project.
If instead you want to export packaged jars, you need to use SBT option:

```scala
exportsJars := true
```

This is a standard [SBT option](http://www.scala-sbt.org/0.13.0/docs/Howto/package.html).
You need to add `exportsJars := true` to every dependent projects in your build.
(I know it looks tedious, if there is a better solution please let me know).

License
-------

Copyright (c) 2013 Jarek Sacha

Published under GPLv3, see LICENSE file.

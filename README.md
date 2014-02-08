sbt-imagej
==========

sbt-imagej is an [SBT](http://www.scala-sbt.org/) (Simple Build Tool) plugin that that helps with development of
[ImageJ](http://rsbweb.nih.gov/ij/) plugins (those are different than SBT plugins).

The main task `ijRun` does following:

1. Builds your ImageJ plugin
2. Creates directory structure expected by ImageJ
3. Copies the plugin jar to ImageJ plugins directory, along with all dependencies
4. Starts ImageJ instance that is aware of the new plugin location,
   so you can interactively test your plugin from within ImageJ.

Setup
-----

Add `sbt-imagej` as a dependency in `project/imagej.sbt`:

```scala
addSbtPlugin("net.sf.ij-plugins" % "sbt-imagej" % "1.1.0")
```

Usage
-----

### Using the Plugin to a Project

First, make sure that you've added the plugin to your build, as described above.


If you're using `build.sbt` add this:

```scala
import ImageJKeys._ // put this at the top of the file

ijSettings
```

Now you'll have a new `ijRun` task which will compile your project,
pack your class files and resources in a jar, copy that jar and dependencies to local
ImageJ's plugins directory, and run ImageJ

    > ijRun

There is also a task that only copies the jar and dependencies to to the plugins directory

    > ijPrepareRun

It is useful if you want to have your own run configuration, for instance executed by your IDE.
Look in the `example` directory to see how it can be used in IntelliJ IDEA or Eclipse.

There are a couple of settings you can use to customize directory used to run ImageJ and load plugins:

* `ijRuntimeSubDir` - Location of ImageJ runtime directory relative to base directory.
  Default value is `sandbox`.
* `ijPluginsSubDir` - Subdirectory of the `plugins` directory, where all `jar`s will be copied.
  Default is `jars`.
* `ijExclusions` - List of regex expressions that match JARs that will be excluded from the plugins directory.
  Default excludes ImageJ jar, source jars, and javadoc/scaladoc jars.

For example the name of the jar can be set as follows in build.sbt:

```scala
ijRuntimeSubDir := "sandbox"

ijPluginsSubDir := "my-plugin"

ijExclusions += """some\.jar"""
```

The above configuration will copy your jar file and dependencies to
`sandbox/plugins/my-plugin`, and additionally exclude the `some.jar`.
ImageJ will be instructed to use `sandbox` as its home directory.

You can use `ijPluginsDir` settings key to see full path to `plugins` subdirectory,
where all jars will be copied. `ijPluginsDir` is intended to be read-only. It can be used,
for instance, in `cleanFiles += ijPluginsDir.value`. By default, it is computed from
`ijPluginsSubDir` and `ijRuntimeSubDir`. Typically you should not reassign it.

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

Tips and Tricks
---------------

### Extend `clean` to remove content created by `ijRun` or `ijPrepareRun`

You can make the regular `clean` command to remove extra content by adding directory to SBT setting `cleanFiles`

```scala
cleanFiles += ijPluginsDir.value
```

### Copy additional files to plugins directory when `ijPrepareRun` is executed

Sometimes you want to copy some extra files to plugins directory.
You can extend `ijPrepareRun` to do the copy or any other tasks:

```scala
ijPrepareRun := ijPrepareRun.value ++ {
  // Files you want to copy
  val srcFiles = Seq(
    new java.io.File("file1"),
    new java.io.File("file2"),
    )
  val destDir = ijPluginsDir.value
  val destFiles = srcFiles.map(f => destDir / f.getName)
  srcFiles zip destFiles.foreach{ case (src, dest) => IO.copyFile(src, dest) }
  // The last statement here should return the collection of copied files
  destFiles
}
```

License
-------

Copyright (c) 2013 Jarek Sacha

Published under GPLv3, see LICENSE file.

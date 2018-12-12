sbt-imagej
==========

sbt-imagej is an [SBT](http://www.scala-sbt.org/) (Simple Build Tool) plugin that that helps with development of
[ImageJ](http://rsbweb.nih.gov/ij/) plugins (those are different than SBT plugins).
It works for Scala as well as Java, or mix of both.

The main task is `ijRun` it packages the ImageJ plugin and helps test the plugin from within ImageJ:

1. Builds your ImageJ plugin and packages it as jar.
2. Creates directory structure expected by ImageJ runtime.
3. Copies the plugin jar to ImageJ plugins directory, along with all dependencies.
4. Starts ImageJ instance that is aware of the new plugin location,
   so you can interactively test your plugin from within ImageJ.

The other task `ijPrepareRun` is intended for integration with IDEs, like IntelliJ IDEA and Eclipse.
See also blog post [Developing ImageJ plugins with SBT using sbt-imagej](https://codingonthestaircase.wordpress.com/2014/11/23/developing-imagej-plugins-with-sbt-using-sbt-imagej/).

`sbt-imagej` requires SBT 0.13.5 or newer.

Usage
-----

Add `sbt-imagej` as a dependency in `project/imagej.sbt`:

```scala
addSbtPlugin("net.sf.ij-plugins" % "sbt-imagej" % "2.0.1")
```

Once added to the project the plugin will be enabled by default.

Now you'll have a new `ijRun` task which will compile your project,
pack your class files and resources in a jar, copy that jar and dependencies to local
ImageJ's plugins directory, and run ImageJ

    > ijRun

There is also a task that only copies the jar and dependencies to to the plugins directory

    > ijPrepareRun

It is useful if you want to have your own run configuration, for instance executed by your IDE.
Look in the `example` directory to see how it can be used in IntelliJ IDEA or Eclipse.

### Configuration

There are a couple of settings you can use to customize `sbt-imagej` plugins:

* `ijRuntimeSubDir` - Location of ImageJ runtime directory relative to base directory.
  Default value is `sandbox`.
* `ijPluginsSubDir` - Subdirectory of the `plugins` directory, where all `jar`s will be copied.
  Default is `jars`.
* `ijExclusions` - List of regex expressions that match JARs that will be excluded from the plugins directory.
  Default excludes ImageJ jar, source jars, and javadoc/scaladoc jars.
* `ijCleanBeforePrepareRun` -  If `true` the plugins directory will be cleaned (deleted) before it
  is populated by `ijPrepareRun` task. This is useful if jar names change during build,
  for instance, due to versioning. If old jars with different names will not be removed ImageJ will
  complain about duplicate plugins. Default value is `false` (for safety).

Consider example settings:

```scala
ijRuntimeSubDir := "sandbox"

ijPluginsSubDir := "my-plugin"

ijExclusions += """some\.jar"""
```

This will set ImageJ runtime directory to `sandbox` and directory where your plugins will be
copied to `sandbox/plugins/my-plugin`. Additionally exclude the `some.jar` from being
copied to that directory. Note that for exclusions we used `+=` rather than `:=` this mean that
we want to add one more exclusion to existing default exclusions, Using `:=` would disable default
exclusions.

You can use `ijPluginsDir` settings key to see full path to `plugins` subdirectory,
where all jars will be copied. `ijPluginsDir` is intended to be read-only. It can be used,
for instance, in `cleanFiles += ijPluginsDir.value`. By default, it is computed from
`ijPluginsSubDir` and `ijRuntimeSubDir`. You should not reassign it.

### Multi-Module Projects

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


Example Project
---------------

You can find example project in sub-directory [example].
It contains SBT setup, two ImageJ plugins, and a workaround to run SBT tasks from IDEA and Eclipse.


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

### Running SBT tasks at part of IDEA or Eclipse build ###

[IntelliJ IDEA](https://www.jetbrains.com/idea/) can load SBT projects using its Scala plugin.
To execute SBT tasks before run/debug you will additionally need SBT plugin.
You can setup your run configuration as described in [example/README.md](example).

Eclipse does noy currently support SBT tasks, but you can execute them indirectly be calling them from Ant.

```xml
<target name="sbt-imagej-prepare-run"
        description="Run SBT task 'prepareRun' that prepares ImageJ plugins directory">
    <property environment="env"/>
    <fail unless="env.SBT_HOME"
          message="SBT_HOME system variable must be defined and point to directory containing 'sbt-launch.jar'"/>
    <property name="sbt-launch.jar" location="${env.SBT_HOME}/bin/sbt-launch.jar"/>

    <java dir="${basedir}"
          jar="${sbt-launch.jar}"
          fork="true"
          failonerror="true">
        <jvmarg line="-Dfile.encoding=UTF8 -Xmx1G -Xss1M -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=256m
                      -Djava.net.useSystemProxies=true"/>
        <arg line="ijPrepareRun"/>
    </java>
</target>
```

You can find complete [build.xml](example/build.xml) in the [example](example) project.


License
-------

Copyright (c) 2013-2018 Jarek Sacha

Published under GPLv3, see LICENSE file.

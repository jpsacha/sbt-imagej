sbt-imagej-example
==================

This is a sample project that illustrates use of the sbt-imagej for creating build files for
[ImageJ](http://rsbweb.nih.gov/ij/) plugins.


Files
-----

The SBT build is defined by three files

* `example/build.sbt` - SBT build definition
* `example/project/build.properties` - version of the SBT  to use
* `example/plugin.sbt` - declares SBT plugins used by the project, here the most important plugin
  is `sbt-imagej`

Source files contain implementation of two ImageJ plugins: `Sepia Tone` and `Vignette`. 
To illustrate that the `sbt-imagej` can be used for Scala and Java development, the first one is
implemented in Scala, the second in Java.

* `example/src/main/scala/effects/SepiaTonePlugIn.scala` - a plugin adding a sepia tone
(brownish tint) effect to a color image.
* `example/src/main/java/effects/Vignette.java` - a plugin adding darken border to a color image.
* `example/src/main/resources/plugins.config` - manifest that instructs ImageJ in which menu to
install the plugins.

There is also an Ant file that helps to integrate `sbt-imagej` into a build performed by an IDE
(till they can execute SBT tasks directly). See details in the next section.

* `example/build.xml` - Ant configuration that has a target to call SBT task `ijPrepareRun`

Running
-------

The simplest way to build and run the example is to use SBT. It also can be used from IntelliJ
IDEA or Eclipse. In all cases it assumes that you have [SBT](http://www.scala-sbt.org/) installed
on your system.

### Using SBT ###

1. Open command prompt
2. Change directory to one containing this project
3. Execute command `sbt ijRun`

### Using IntelliJ IDEA ###

We assume here that you have [IntelliJ IDEA](http://www.jetbrains.com/idea/) 13 installed together
with the [Scala plugin](http://confluence.jetbrains.com/display/SCA/Scala+Plugin+Nightly+Builds+for+Cardea).
Before proceeding make sure that you can build and run the project using SBT, as described above.

 1. If you do not have it, define environment variable `SBT_HOME` pointing to location of SBT
    installation.
 1. Start IDEA
 1. Select "Import Project" and load the `example/build.sbt`. This will let you edit and build
    the project.
 1. To run ImageJ with the custom plugin you need to invoke `ijPrepareRun` tasks before running ImageJ.
    Currently (Feb 8, 2014) IDEA does not support using SBT tasks yet, so we will do it indirectly,
    through Ant. Click on "Ant Build" tab and add `example/build.xml`
 1. Click `Run` > `Edit Configurations`
 1. Add new application configuration, call it for instance, `ImageJ`
 1. Set main class to `ij.ImageJ`
 1. Set "Working directory" to `example/sandbox`
 1. Click on "+" under "Before lunch", select "Run Ant target", select `sbt-imagej-prepare-run`
 1. Now you can run the new configuration. It will build the project, call Ant, Ant will call SBT
 to package and copy JARs, then run ImageJ with new plugins installed.


### Using Eclipse ###

You can generate Eclipse project using [sbteclipse](https://github.com/typesafehub/sbteclipse)
plugin. Then go through steps similar as described above for IDEA. The only significant difference
is that in Eclipse you add the `sbt-imagej-prepare-run` Ant target by defining new Ant builder.


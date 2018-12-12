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
To illustrate that the `sbt-imagej` can be used for different languages, the first one is
implemented in Scala, the second in Java.

* `example/src/main/scala/effects/SepiaTonePlugIn.scala` - a plugin adding a sepia tone
(brownish tint) effect to a color image.
* `example/src/main/java/effects/Vignette.java` - a plugin adding darken border to a color image.
* `example/src/main/resources/plugins.config` - manifest that instructs ImageJ in which menu to
install the plugins.

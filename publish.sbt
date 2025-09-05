// @formatter:off

publishMavenStyle := true

Test / publishArtifact := false

publishTo := version {
  version: String =>
    val nexus = "https://oss.sonatype.org/"
    if (version.contains("-SNAPSHOT"))
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}.value

pomIncludeRepository := { _ => false }

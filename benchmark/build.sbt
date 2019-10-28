organization := (organization in LocalRootProject).value
name := "jwo-benchmark"
version := (version in LocalRootProject).value
resourceDirectory in Compile := (resourceDirectory in(LocalRootProject, Test)).value
skip in publish := true
maintainer := (maintainer in LocalRootProject).value
mainClass := Some("net.woggioni.jwo.benchmark.Main")
fork := true
//    libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.9.6",
//    libraryDependencies += "org.tukaani" % "xz" % "1.8",
//    libraryDependencies += "com.beust" % "jcommander" % "1.72",
//    libraryDependencies += "org.projectlombok" % "lombok" % "1.18.8" % Provided

enablePlugins(JavaAppPackaging)
enablePlugins(UniversalPlugin)
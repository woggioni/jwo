name := "jwo"

organization := "net.woggioni"

maintainer := "oggioni.walter@gmail.com"

version := "1.0"
resolvers += Resolver.mavenLocal

git.useGitDescribe := true
crossPaths := false

autoScalaLibrary := false

libraryDependencies += "org.projectlombok" % "lombok" % "1.18.8" % Provided

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.28"

libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % Test
libraryDependencies += "org.apache.logging.log4j" % "log4j-core" % "2.12.1" % Test
libraryDependencies += "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.12.1" % Test

javacOptions in (Compile, compile) ++= Seq("-source", "1.8", "-target", "1.8")

Compile / packageBin / packageOptions +=
  Package.ManifestAttributes("Automatic-Module-Name" -> "net.woggioni.jwo")

enablePlugins(Delombok)
enablePlugins(DelombokJavadoc)
lazy val benchmark = (project in file("benchmark")).dependsOn(LocalRootProject)

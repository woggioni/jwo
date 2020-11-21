name := "jwo"

organization := "net.woggioni"

maintainer := "oggioni.walter@gmail.com"

version := "1.0"
resolvers += Resolver.mavenLocal
resolvers in ThisBuild += Resolver.jcenterRepo

git.useGitDescribe := true
crossPaths := false

autoScalaLibrary := false

libraryDependencies += "org.projectlombok" % "lombok" % Versions.lombok % Provided

libraryDependencies += "org.slf4j" % "slf4j-api" % Versions.slf4j

libraryDependencies += "net.aichler" % "jupiter-interface" % JupiterKeys.jupiterVersion.value % Test
libraryDependencies += "org.junit.jupiter" % "junit-jupiter-params" % JupiterKeys.junitJupiterVersion.value % Test

javacOptions in (Compile, compile) ++= Seq("--release", "8")

Compile / packageBin / packageOptions +=
  Package.ManifestAttributes("Automatic-Module-Name" -> "net.woggioni.jwo")

enablePlugins(Delombok)
enablePlugins(DelombokJavadoc)
lazy val benchmark = (project in file("benchmark")).dependsOn(LocalRootProject)

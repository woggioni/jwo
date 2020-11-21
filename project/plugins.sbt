resolvers += Resolver.jcenterRepo
addSbtPlugin("com.jsuereth"      % "sbt-pgp"      % "1.1.0")
addSbtPlugin("com.typesafe.sbt"  % "sbt-git"      % "0.9.3")
addSbtPlugin("de.heikoseeberger" % "sbt-header"   % "4.1.0")
addSbtPlugin("net.aichler" % "sbt-jupiter-interface" % "0.8.4-SNAPSHOT")
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.6")
addSbtPlugin("com.thoughtworks.sbt" % "delombokjavadoc" % "1.0.0+66-8fbcf18c")

//addSbtPlugin("com.thoughtworks.sbt" % "delombokjavadoc" % "latest.release")
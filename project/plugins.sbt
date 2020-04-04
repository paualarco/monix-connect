addSbtPlugin("org.scalariform"      % "sbt-scalariform"     % "1.8.2")
addSbtPlugin("com.typesafe.sbt"     % "sbt-native-packager" % "1.3.4")
addSbtPlugin("com.thesamet" % "sbt-protoc" % "0.99.14")
libraryDependencies ++= Seq(
  "com.thesamet.scalapb" %% "compilerplugin" % "0.10.1",
)
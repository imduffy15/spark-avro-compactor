lazy val buildSettings = Seq(
  name := "spark-avro-compactor",
  organization := "ie.ianduffy"
)

lazy val sparkVersion = "2.2.1"
val sparkDependencies = Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "org.apache.spark" %% "spark-hive" % sparkVersion,
  "org.apache.hadoop" % "hadoop-aws" % "2.8.0"
)

lazy val baseSettings = Seq(
  scalaVersion := "2.11.11",
  scalacOptions ++= Seq(
    "-encoding", "UTF-8",
    "-target:jvm-1.8",
    "-deprecation",
    "-feature",
    "-unchecked",
    "-Xlog-reflective-calls",
    "-Xlint"
  ),
  javacOptions in Compile ++= Seq(
    "-source", "1.8"
    , "-target", "1.8"
    , "-Xlint:unchecked"
    , "-Xlint:deprecation"
  ),

  fork in run := true,
  fork in Test := true,

  assemblyJarName := name.value + "_" + version.value + ".jar",
  assemblyMergeStrategy := {
    case PathList("META-INF", xs@_*) => MergeStrategy.discard
    case x => MergeStrategy.first
  },
  parallelExecution in Test := false,
  resolvers += "confluent" at "http://packages.confluent.io/maven",
  libraryDependencies ++= {
    Seq(
      "ch.qos.logback" % "logback-classic" % "1.1.3",
      "com.typesafe" % "config" % "1.2.1",
      "com.github.scopt" %% "scopt" % "3.7.0",
      "org.scalatest" %% "scalatest" % "3.0.4" % Test,
      "io.confluent"    % "kafka-schema-registry-client" % "3.3.0"  excludeAll(
        ExclusionRule(organization = "com.fasterxml.jackson.core"),
        ExclusionRule(organization = "com.fasterxml.jackson.dataformat"),
        ExclusionRule(organization = "com.fasterxml.jackson.datatype"),
        ExclusionRule(organization = "com.fasterxml.jackson.annotations"),
        ExclusionRule(organization = "com.fasterxml.jackson.module-paranamer"),
        ExclusionRule(organization = "com.fasterxml.jackson.module-scala")
      ),
      "com.holdenkarau" %% "spark-testing-base" % "2.2.0_0.8.0" % Test,
      "org.mockito" % "mockito-core" % "2.7.6" % Test
    ).map(_.exclude("org.slf4j", "slf4j-log4j12")) ++ sparkDependencies.map(dependency => dependency % Provided)
  },
  git.useGitDescribe := true
)

dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-annotations" % "2.6.5"
dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-databind" % "2.6.5"
dependencyOverrides += "com.fasterxml.jackson.module" % "jackson-module-paranamer" % "2.6.5"


lazy val root = (project in file("."))
  .enablePlugins(GitVersioning, JavaAppPackaging)
  .settings(baseSettings)
  .settings(buildSettings)

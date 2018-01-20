lazy val buildSettings = Seq(
  name := "spark-avro-compactor",
  organization := "ie.ianduffy.spark.avro.compactor"
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
  parallelExecution in Test := false,
  libraryDependencies ++= {
    lazy val sparkVersion = "2.2.0"
    Seq(
      "org.apache.spark" %% "spark-core" % sparkVersion % Provided,
      "org.apache.spark" %% "spark-sql" % sparkVersion % Provided,
      "org.apache.hadoop" % "hadoop-aws" % "2.8.0" % Provided,
      "ch.qos.logback" % "logback-classic" % "1.1.3",
      "com.github.scopt" %% "scopt" % "3.7.0",
      "org.scalatest" %% "scalatest" % "3.0.4" % Test,
      "com.databricks" %% "spark-avro" % "4.0.0"
    ).map(_.exclude("org.slf4j", "slf4j-log4j12"))
  },
  assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false),
  assemblyJarName in assembly := s"${name.value}-${version.value}.jar",
  mappings in Universal := {
    val universalMappings = (mappings in Universal).value
    val fatJar = (assembly in Compile).value
    val filtered = universalMappings filter {
      case (file, fileName) =>  ! fileName.endsWith(".jar") && ! fileName.endsWith(".bat")
    }
    filtered :+ (fatJar -> ("lib/" + fatJar.getName))
  },
  git.useGitDescribe := true
)

lazy val root = (project in file("."))
  .enablePlugins(GitVersioning, JavaAppPackaging)
  .settings(baseSettings)
  .settings(buildSettings)

bintrayOrganization := Some(System.getenv("BINTRAY_USER"))
licenses += ("Apache-2.0", url("http://opensource.org/licenses/Apache-2.0"))


artifact in (Compile, assembly) := {
  val art = (artifact in (Compile, assembly)).value
  art.withClassifier(Some("assembly"))
}

addArtifact(artifact in (Compile, assembly), assembly)
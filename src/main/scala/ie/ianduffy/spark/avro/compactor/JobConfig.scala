package ie.ianduffy.spark.avro.compactor

import scopt.OptionParser

case class JobConfig(input: String = null,
                     output: String = null,
                     schemaRegistryUrl: String = null,
                     schemaRegistrySubject: String = null,
                     overrideOutput: Boolean = false)

object JobConfig {

  val default = JobConfig()

  val parser = new OptionParser[JobConfig]("avro-concat") {
    opt[String]("input")
      .valueName("<input-path>")
      .text("the s3-path to the input")
      .required()
      .action((v, c) => c.copy(input = v))
    opt[String]("output")
      .valueName("<output-path>")
      .text("s3 path to the output")
      .required()
      .action((v, c) => c.copy(output = v))
    opt[String]("schema-registry-url")
      .valueName("<schema-registry-url>")
      .text("URL of confluent schema registry")
      .required()
      .action((v, c) => c.copy(schemaRegistryUrl = v))
    opt[String]("schema-registry-subject")
      .valueName("<schema-registry-subject>")
      .text("Schema Registry Subject")
      .required()
      .action((v, c) => c.copy(schemaRegistrySubject = v))
    opt[Boolean]("override-output")
      .valueName("<override-output>")
      .text("If set will override the files in the given output path should a conflict exist.")
      .optional()
      .action((v, c) => c.copy(overrideOutput = v))
  }

  def parse(args: Seq[String]): JobConfig = {
    parser.parse(args, JobConfig())
      .getOrElse(throw new IllegalArgumentException("Could not parse arguments"))
  }
}

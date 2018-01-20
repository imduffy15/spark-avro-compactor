package ie.ianduffy.spark.avro.compactor

import scopt.OptionParser

case class JobConfig(input: String = null,
                      output: String = null
                    )

object JobConfig {

  val default = JobConfig()

  val parser = new OptionParser[JobConfig]("avro-concat") {
    opt[String]('i', "input")
      .valueName("<input-path>")
      .text("the s3-path to the input")
      .required()
      .action((v, c) => c.copy(input = v))
    opt[String]('o', "output")
      .valueName("<output-path>")
      .text("s3 path to the output")
      .required()
      .action((v, c) => c.copy(output = v))
  }

  def parse(args: Seq[String]): JobConfig = {
    parser.parse(args, JobConfig())
      .getOrElse(throw new IllegalArgumentException("Could not parse arguments"))
  }
}

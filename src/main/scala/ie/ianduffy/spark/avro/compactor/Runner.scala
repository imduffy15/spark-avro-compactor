package ie.ianduffy.spark.avro.compactor

import com.amazonaws.auth.{AWSSessionCredentials, DefaultAWSCredentialsProviderChain}
import ie.ianduffy.spark.avro.compactor.Utils._
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}

object Runner extends App {

  private val log = LoggerFactory.getLogger(Runner.getClass.getName.replace("$", ""))

  private val config = JobConfig.parse(args)

  private val schemaRegistry = new CachedSchemaRegistryClient(config.schemaRegistryUrl, 10000)

  log.info(s"Running with application config $config")

  if (System.getenv("local") != null) {
    log.info(s"Running with embedded spark")
    runLocally(config)
  } else {
    log.info("Running with remote spark")
    run(config)
  }

  def runLocally(config: JobConfig) = {
    val credentials = new DefaultAWSCredentialsProviderChain().getCredentials.asInstanceOf[AWSSessionCredentials]
    System.setProperty("spark.master", "local[*]")
    System.setProperty("spark.app.name", "compactor")
    System.setProperty("spark.hadoop.fs.s3a.impl", "org.apache.hadoop.fs.s3a.S3AFileSystem")
    System.setProperty("spark.hadoop.fs.s3a.endpoint", "s3-eu-central-1.amazonaws.com")
    System.setProperty("spark.hadoop.fs.s3a.access.key", credentials.getAWSAccessKeyId)
    System.setProperty("spark.hadoop.fs.s3a.secret.key", credentials.getAWSSecretKey)
    System.setProperty("spark.hadoop.fs.s3a.session.token", credentials.getSessionToken)
    System.setProperty("spark.hadoop.fs.s3a.aws.credentials.provider", "org.apache.hadoop.fs.s3a.TemporaryAWSCredentialsProvider")
    System.setProperty("com.amazonaws.services.s3.enforceV4", "true")

    val spark = createSparkSession

    log.info(s"Running with spark configuration: ${spark.conf.getAll}")

    Try {
      Job.run(spark, schemaRegistry, config)
    } match {
      case Success(_) =>
        spark.close()
        System.exit(0)
      case Failure(e) =>
        spark.close()
        e.printStackTrace()
        System.exit(1)
    }
  }

  def run(config: JobConfig) = {
    val spark = createSparkSession
    log.info(s"Running with configuration: ${spark.conf.getAll}")
    Job.run(spark, schemaRegistry, config)
  }

}

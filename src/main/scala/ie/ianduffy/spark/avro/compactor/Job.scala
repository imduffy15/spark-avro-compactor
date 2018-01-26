package ie.ianduffy.spark.avro.compactor

import ie.ianduffy.spark.avro.compactor.Utils._
import io.confluent.kafka.schemaregistry.client.{SchemaMetadata, SchemaRegistryClient}
import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord
import org.apache.avro.mapred.AvroKey
import org.apache.avro.mapreduce.AvroKeyOutputFormat
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.io.NullWritable
import org.apache.spark.sql.SparkSession
import org.slf4j.LoggerFactory

object Job {

  private val log = LoggerFactory.getLogger(Job.getClass.getName.replace("$", ""))

  def run(spark: SparkSession, schemaRegistry: SchemaRegistryClient, jobConfig: JobConfig): Unit = {
    val schema: Schema = {
      val latestSchemaMetadata: SchemaMetadata = schemaRegistry.getLatestSchemaMetadata(jobConfig.schemaRegistrySubject)
      val id: Int = latestSchemaMetadata.getId
      schemaRegistry.getById(id)
    }

    implicit val sparkConfig: Configuration = spark.sparkContext.hadoopConfiguration
    sparkConfig.set("avro.schema.input.key", schema.toString())
    sparkConfig.set("avro.schema.output.key", schema.toString())

    val inputPath: Path = new Path(jobConfig.input)
    val outputPath: Path = new Path(jobConfig.output)

    val fs: FileSystem = inputPath.getFileSystem(sparkConfig)

    // avoid raising org.apache.hadoop.mapred.FileAlreadyExistsException
    if (jobConfig.overrideOutput) fs.delete(outputPath, true)

    // from fileSystem prefix with s3 the default is 64MB and can be overwitten by fs.s3.block.size
    // from fileSystem prefix with s3a the default is 32MB and can be overwitten by setting fs.s3a.block.size
    val outputBlocksize: Long = fs.getDefaultBlockSize(outputPath)

    // Where inputPath is of the form s3://some/path
    val inputPathSize: Long = fs.getContentSummary(inputPath).getSpaceConsumed

    val numPartitions: Int = Math.max(1, Math.floor((inputPathSize / CompressionRatio.AVRO_SNAPPY) / outputBlocksize).toInt)

    log.debug(
      s"""outputBlocksize: $outputBlocksize
         | inputPathSize: $inputPathSize
         | splitSize: $numPartitions
       """.stripMargin)

    val rdd = readHadoopFile(spark, inputPath.toString)

    rdd.coalesce(numPartitions)
      .saveAsNewAPIHadoopFile(
        outputPath.toString,
        classOf[AvroKey[GenericRecord]],
        classOf[NullWritable],
        classOf[AvroKeyOutputFormat[GenericRecord]],
        sparkConfig
      )
  }
}

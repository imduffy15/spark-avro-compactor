package ie.ianduffy.spark.avro.compactor

import org.apache.hadoop.fs.{FileStatus, FileSystem, Path}
import org.apache.spark.sql.SparkSession
import org.slf4j.LoggerFactory
object Job {

  private val log = LoggerFactory.getLogger(Job.getClass.getName.replace("$", ""))

  def run(spark: SparkSession, jobConfig: JobConfig): Unit = {
    val inputPath: Path = new Path(jobConfig.input)
    val outputPath: Path = new Path(jobConfig.output)

    val fs: FileSystem = inputPath.getFileSystem(spark.sparkContext.hadoopConfiguration)

    val fsArray: Array[FileStatus] = fs.globStatus(inputPath).filterNot { fileStatus => {
      val fileName = fileStatus.getPath.getName
      fileName.startsWith("_") || fileName.startsWith(".")
    }}

    val outputBlocksize: Long = fs.getDefaultBlockSize(outputPath)

    val inputPathSize: Long = fsArray.map(file => fs.getContentSummary(file.getPath).getSpaceConsumed).sum

    val splitSize: Int = Math.floor((inputPathSize / CompressionRatio.AVRO_SNAPPY) / outputBlocksize).toInt

    log.debug(
      s"""outputBlocksize: $outputBlocksize
         | inputPathSize: $inputPathSize
         | splitSize: $splitSize
       """.stripMargin)

    val sqlContext = spark.sqlContext

    sqlContext.read.format("com.databricks.spark.avro").load(inputPath.toString)
      .coalesce(splitSize)
      .write
      .format("com.databricks.spark.avro")
      .save(outputPath.toString)
  }
}

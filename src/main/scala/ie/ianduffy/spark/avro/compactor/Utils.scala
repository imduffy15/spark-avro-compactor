package ie.ianduffy.spark.avro.compactor

import org.apache.avro.generic.GenericRecord
import org.apache.avro.mapred.AvroKey
import org.apache.avro.mapreduce.AvroKeyInputFormat
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.io.NullWritable
import org.apache.spark.sql.SparkSession

object Utils {

  def createSparkSession: SparkSession =
    SparkSession
      .builder
      .appName("avro-compactor")
      .getOrCreate


  def readHadoopFile(spark: SparkSession, path: String)(implicit sparkConfig: Configuration) = {
    spark.sparkContext.newAPIHadoopFile(
      path,
      classOf[AvroKeyInputFormat[GenericRecord]],
      classOf[AvroKey[GenericRecord]],
      classOf[NullWritable],
      sparkConfig
    )
  }

}

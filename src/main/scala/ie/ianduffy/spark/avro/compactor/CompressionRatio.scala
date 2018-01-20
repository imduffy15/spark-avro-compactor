package ie.ianduffy.spark.avro.compactor

object CompressionRatio {
  val SNAPPY = 1.7
  val AVRO = 1.6
  val AVRO_SNAPPY = SNAPPY * AVRO
}

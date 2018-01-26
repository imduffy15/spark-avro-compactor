# spark-avro-compactor

Spark job for compacting avro files together


## Running locally 

1. `local=true sbt`
1. `runMain ie.ianduffy.spark.avro.compactor.Runner -i inputPath --output  outputPath --schema-registry-url https://confluent-schema-registry --schema-registry-subject schema-subject` 


## Contributors 

 1. [Kevin Eid](https://github.com/kevllino)
 1. [Peter Barron](https://github.com/pbarron)
 1. [Ian Duffy](https://github.com/imduffy15)

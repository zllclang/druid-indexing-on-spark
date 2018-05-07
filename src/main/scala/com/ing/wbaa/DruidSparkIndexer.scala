package com.ing.wbaa

import com.google.common.hash.Hashing
import io.druid.indexer.InputRowSerde
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions._
import org.joda.time.DateTime

object DruidSparkIndexer extends App {

  case class Segment(shardNum: Int, dateTime: Long, partitionNum: Int)

  val hashFunction = Hashing.murmur3_128

  val spark = SparkSession
    .builder()
    .appName("DruidSparkIndexer")
    .master("local[*]")
    .getOrCreate()

  val df = spark
    .read
    .json("/Users/fokkodriesprong/Desktop/docker-druid/ingestion/wikiticker-2015-09-12-sampled.json")

  import spark.implicits._

  val window = Window
    .partitionBy("shardNum", "dateTime", "partitionNum")
    .orderBy(WikitickerConfig.dimension.map(d => col(d)):_*)
//
//  df.withColumn("shardNum", lit(0))
//    .withColumn("dateTime", lit(new DateTime("2015-09-12T00:47:08Z").getMillis))
//    .withColumn("partitionNum", lit(0))
//    .with

  val out = df.map(row => {
    val sparkRow = new SparkBasedInputRow(row)

    val serializedInputRow = InputRowSerde
      .toBytes(InputRowSerde.getTypeHelperMap(
        WikitickerConfig.dimensionSpec
      ),
        sparkRow,
        Array()
      )

    val timestamp = new DateTime(row.getAs[String](WikitickerConfig.timeDimension)).getMillis

    Segment(0, timestamp, 0) -> serializedInputRow
    //
    //    (
    //      new SortableBytes(
    //        new Bucket(0, new DateTime("2015-09-12T00:47:08Z"), 0).toGroupKey(),
    //        // sort rows by truncated timestamp and hashed dimensions to help reduce spilling on the reducer side
    //        ByteBuffer.allocate(java.lang.Long.BYTES + hashedDimensions.length)
    //          .putLong(timestamp)
    //          .put(hashedDimensions)
    //          .array()
    //      ),
    //      serializedInputRow
    //    ).toString()
  }).groupByKey(row => {
    val segment = row._1


  })


  val v = out
}

package net.kyivstar.dmp

import org.apache.log4j.{Level, LogManager, Logger}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{SQLContext, SparkSession}


trait InitSpark {

  val conf = new SparkConf(true)
//    .set("spark.cassandra.connection.host", "localhost")
//    .set("spark.cassandra.connection.port", "9042")
    //    .set("spark.cassandra.connection.host", "10.49.198.55, 10.49.198.57, 10.49.198.235")
//    .set("spark.cassandra.connection.port", "10042")
//    .set("spark.cassandra.auth.username", "")
//    .set("spark.cassandra.auth.password", "")
    .set("spark.executor.memory", "4G")
    .setAppName("Spark example")
    .setMaster("local[4]")
  //    .setMaster("spark://bdms-test-whg1:7077")
  //    .set("spark.driver.port","8199")

//  private val spark: SparkSession = SparkSession.builder()
//    .appName("Spark example")
//    .master("local[4]")
//    .config(conf)
//    .getOrCreate()

  val sc = new SparkContext(conf)
  val spark = SparkSession.builder.config(sc.getConf).getOrCreate()
  val sqlContext = spark.sqlContext

  private def init = {
    sc.setLogLevel("ERROR")
    Logger.getLogger("org").setLevel(Level.ERROR)
    Logger.getLogger("akka").setLevel(Level.ERROR)
    LogManager.getRootLogger.setLevel(Level.ERROR)
  }

  init

  def close = {
//    spark.close()
  }
}

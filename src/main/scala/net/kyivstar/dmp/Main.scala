package net.kyivstar.dmp

import com.datastax.spark.connector._
import org.apache.spark.SparkContext
import org.apache.spark.sql.functions._
import org.apache.spark.sql.cassandra._

final case class Person(firstName: String, lastName: String,
                        country: String, age: Int)

object Main extends InitSpark {

  def main(args: Array[String]) = {
    import spark.implicits._
    //
    cassandraTable
    //
    cassandraFormat
    //
    cassandraSql
    //
    cassandraFormatOptions
    close
  }

  private def cassandraFormatOptions = {
    val e = sqlContext
      .read
      .format("org.apache.spark.sql.cassandra")
      .options(Map("table" -> "subs_profile_daily", "keyspace" -> "bdms_bdms_data", "inferSchema" -> "false"))
      .load
      .cache()

    e.show()
  }

  private def cassandraSql = {
    val createDDL =
      """CREATE TEMPORARY VIEW subs_profile_daily
     USING org.apache.spark.sql.cassandra
     OPTIONS (
     table "subs_profile_daily",
     keyspace "bdms_bdms_data",
     cluster "Test Cluster",
     pushdown "true")"""

    spark.sql(createDDL) // Creates Catalog Entry registering an existing Cassandra Table
    spark.sql("SELECT count(*) as cnt FROM subs_profile_daily where charge_3m = 0").show
    spark.sql("SELECT * FROM subs_profile_daily WHERE msisdn = '380675485507'").show
  }

  private def cassandraFormat = {
    val df = spark
      .read
      .cassandraFormat(table = "subs_profile_daily", keyspace = "bdms_bdms_data")
      .option("header", "true")
      .load()

    df.show()
  }

  private def cassandraTable = {
    val version = spark.version
    println("SPARK VERSION = " + version)
    val rdd = sc.cassandraTable("bdms_bdms_data", "subs_profile_daily")
      .select("charge_3m")
      .groupBy(r => r.getFloat("charge_3m"))

    rdd.foreach(println)
  }
}

package net.kyivstar.dmp

import java.io.{BufferedWriter, FileWriter}

import au.com.bytecode.opencsv.CSVWriter
import com.datastax.spark.connector._
import org.apache.spark.SparkContext
import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.functions._
import org.apache.spark.sql.cassandra._
import org.apache.spark.sql.types.StructField
import com.datastax.spark.connector.writer._
import org.apache.spark.rdd.RDD

import scala.collection.JavaConversions._
import scala.collection.mutable.ListBuffer

final case class Person(firstName: String, lastName: String,
                        country: String, age: Int)

object Main extends InitSpark {

  def main(args: Array[String]) = {
    import spark.implicits._
    //
    //    cassandraTable
    //
    //    cassandraFormat
    //
    cassandraSql
    //    cassandraFormatOptions
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
     table "abonent_profile",
     keyspace "bdms_bdms_data",
     cluster "Test Cluster",
     pushdown "true")"""

    spark.sql(createDDL).cache() // Creates Catalog Entry registering an existing Cassandra Table

    //CHARGE_3M is null
    val res = spark.sql("SELECT msisdn, charge_3m  FROM subs_profile_daily where charge_3m is null")
    writeScv(fileName = "./charge_3m_is_null.scv", dataFrame = res)

    //Check Tariff Plan
    //    spark.sql("SELECT *  FROM subs_profile_daily where price_plan_name_ua is null or segment_name_ua is null").show()


    //Check OS_HANDSET is null
    //    spark.sql("SELECT *  FROM subs_profile_daily where os_handset is null").show()

    //Group by OS_HANDSET
    //    spark.sql("SELECT os_handset, count(*) as cnt FROM subs_profile_daily group by os_handset").show()

    //Group by pref_city_key
    //    spark.sql("SELECT pref_city_key, count(*) as cnt FROM subs_profile_daily group by pref_city_key").show()

    //Group by pref_city_key
    //    spark.sql("SELECT min(total_mobile_data_mb_30_amt) as min, max(total_mobile_data_mb_30_amt) as max " +
    //      "FROM subs_profile_daily where type_handset not in ('Mobile M2M/IOT','Data-only')").show()

    //    spark.sql("SELECT * FROM subs_profile_daily WHERE msisdn = '380675485507'").show
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

  def writeScv(fileName: String, dataFrame: DataFrame): Unit = {
    var fruits = new ListBuffer[Array[String]]()
    val out = new BufferedWriter(new FileWriter(fileName));
    val writer = new CSVWriter(out);
    val employeeSchema = dataFrame.columns.mkString(",")
    //      Array("name", "age", "dept")
    val listOfRecords = new ListBuffer[String]()
    listOfRecords += employeeSchema
    //        val res: RDD[String] = dataFrame.rdd.map(e => e.mkString(","))
    dataFrame.rdd.map(e => listOfRecords += e.mkString(","))

    val employee1 = Array("piyush", "23", "computerscience")

    val employee2 = Array("neel", "24", "computerscience")

    val employee3 = Array("aayush", "27", "computerscience")

    //    var listOfRecords = List(employeeSchema, employee1, employee2, employee3)
    //    val listOfRecords: List[Array[String]] = List[Array](employeeSchema,  )
    for (elem <- listOfRecords.toList) {
      println(elem)
      //      writer.writeNext(elem.mkString(","))
    }

    //    writer.writeAll(listOfRecords)
    out.close()
  }
}

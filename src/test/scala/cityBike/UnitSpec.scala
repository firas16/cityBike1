package cityBike

import java.io.File
import java.util.{Locale, TimeZone}

import org.apache.commons.io.FileUtils
import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkContext
import org.apache.spark.sql._
import org.scalatest._

abstract class SharedContext extends FlatSpecLike with BeforeAndAfterAll with BeforeAndAfterEach {
  self: Suite =>

  Logger.getRootLogger.setLevel(Level.WARN)
  Logger.getLogger("org").setLevel(Level.WARN)
  Logger.getLogger("akka").setLevel(Level.WARN)
  Logger.getLogger("/executors").setLevel(Level.FATAL)

  Locale.setDefault(Locale.US)
  TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

  private var _spark: SparkSession = _
  protected val debug: Boolean = false

  protected def spark: SparkSession = _spark
  protected def sqlContext: SQLContext = _spark.sqlContext
  protected def sc: SparkContext = _spark.sparkContext


  protected override def beforeAll(): Unit = {
    if(_spark == null) {
      _spark = SparkSession
        .builder()
        .appName("Tests")
        .master("local[4]")
        .config("spark.sql.testkey", "true")
        .config("spark.default.parallelism", 2)
        .config("spark.sql.shuffle.partitions", 2)
        .getOrCreate()
    }

    super.beforeAll()
  }

  override def beforeEach(): Unit = {
    FileUtils.deleteDirectory(new File("target/test"))
    super.beforeEach()
  }

  override def afterAll() {
    try {
      if(_spark != null) {
        _spark.stop()
        _spark = null
      }
    } finally {
      FileUtils.deleteDirectory(new File("target/test/"))
      super.afterAll()
    }
  }
}



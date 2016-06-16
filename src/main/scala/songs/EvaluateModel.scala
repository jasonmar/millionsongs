package songs

import org.apache.spark.ml.regression.{LinearRegression, LinearRegressionModel}
import org.apache.spark.mllib.evaluation.RegressionMetrics
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SQLContext
import org.slf4j.LoggerFactory

object EvaluateModel {

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setAppName(Config.appName)
    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)
    val logger = LoggerFactory.getLogger(getClass.getName)

    logger.info(s"Loading Linear Regression Model from ${Config.modelOut}")
    val model = LinearRegressionModel.load(Config.modelOut)

    logger.info("Loading datasets")
    val datasets = SongML.loadModelData(sqlContext = sqlContext)
    val pipelineModel = SongML.transformPipeline.fit(datasets.test)
    val testData = pipelineModel.transform(datasets.test)

    logger.info("Calculating Regression Metrics")
    val testFeatures = testData.select(SongML.labelColumn,SongML.featuresColumn)
    val testPredictions = model.transform(testFeatures)
      .select(SongML.labelColumn,SongML.predictionColumn)
      .map(r => (r.getAs[Double](SongML.predictionColumn), r.getAs[Double](SongML.labelColumn)))
    val rm = new RegressionMetrics(testPredictions)

    logger.info(SongML.printStats(model,rm,"Testing"))

    logger.info("Exiting")
    sc.stop()
  }

}

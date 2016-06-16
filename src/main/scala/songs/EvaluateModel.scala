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

    logger.info("Printing weights and intercept for Linear Regression Model")
    val colWeights = SongML.featureColumns.zip(model.coefficients.toArray)
    logger.info(s"Weights: $colWeights")
    logger.info(s"Intercept: ${model.intercept}")

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

    logger.info("Model coefficients:")
    model.coefficients.toArray.zip(SongML.featureColumns).foreach{t =>
      logger.info(s"${t._2}:\t${t._1}")
    }
    logger.info("Test Metrics")
    logger.info("Test Explained Variance:")
    logger.info(s"${rm.explainedVariance}")
    logger.info("Test R^2:")
    logger.info(s"${rm.r2}")
    logger.info("Test MSE:")
    logger.info(s"${rm.meanSquaredError}")
    logger.info("Test RMSE:")
    logger.info(s"${rm.rootMeanSquaredError}")

    logger.info("Exiting")
    sc.stop()
  }

}

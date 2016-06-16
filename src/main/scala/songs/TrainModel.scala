package songs

import org.apache.spark.ml.PipelineModel
import org.apache.spark.ml.regression.LinearRegressionModel
import org.apache.spark.mllib.evaluation.RegressionMetrics
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SQLContext
import org.slf4j.LoggerFactory

object TrainModel {

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setAppName(Config.appName)
    val sc = new SparkContext(conf)
    val sqlContext = new SQLContext(sc)
    val logger = LoggerFactory.getLogger(getClass.getName)

    logger.info("Loading datasets from parquet format")
    val data = SongML.loadModelData(sqlContext = sqlContext)

    logger.info("Showing summary stats for training data")
    val summary = data.training.describe(SongML.allColumns:_*)
    summary.show(1000)

    logger.info("Training Linear Regression Model")
    val startTime = System.nanoTime()

    val pipeline = SongML.trainingPipeline.fit(data.training)

    val elapsedTime = (System.nanoTime() - startTime) / 1e9
    logger.info(s"Training time: $elapsedTime seconds")

    logger.info("Calculating Regression Metrics")
    val bestModel = pipeline.bestModel.asInstanceOf[PipelineModel]
    val testPredictions: RDD[(Double,Double)] = bestModel.transform(data.training)
      .select(SongML.predictionColumn, SongML.labelColumn)
      .map(r => (r.getAs[Double](SongML.predictionColumn), r.getAs[Double](SongML.labelColumn)))

    val rm = new RegressionMetrics(testPredictions)

    val model = bestModel.stages(3).asInstanceOf[LinearRegressionModel]

    logger.info("Model coefficients:")
    model.coefficients.toArray.zip(SongML.featureColumns).foreach{t =>
      logger.info(s"${t._2}:\t${t._1}")
    }
    logger.info("Training Metrics")
    logger.info("Training Explained Variance:")
    logger.info(s"${rm.explainedVariance}")
    logger.info("Training R^2:")
    logger.info(s"${rm.r2}")
    logger.info("Training MSE:")
    logger.info(s"${rm.meanSquaredError}")
    logger.info("Training RMSE:")
    logger.info(s"${rm.rootMeanSquaredError}")

    logger.info(s"Saving model to ${Config.modelOut}")
    model.write.overwrite().save(Config.modelOut)

    logger.info("Exiting")
    sc.stop()
  }
}

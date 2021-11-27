package edu.uci.ics.amber.engine.common

import scala.concurrent.duration._

object Constants {
  val defaultBatchSize = 400
  val remoteHDFSPath = "hdfs://10.138.0.2:8020"
  val remoteHDFSIP = "10.138.0.2"
  var defaultNumWorkers = 0
  var dataset = 0
  var masterNodeAddr: String = null

  var dataVolumePerNode = 10
  var defaultTau: FiniteDuration = 10.milliseconds

  var numWorkerPerNode = 4
  // join-skew reserach related
  val gcpExp: Boolean = true
  val samplingResetFrequency: Int = 2000
  val startDetection: FiniteDuration = 100.milliseconds // 100.milliseconds
  val detectionPeriod: FiniteDuration = 2.seconds
  val printResultsInConsole: Boolean = true

  // sort-skew research related
  val eachTransferredListSize: Int = 10000
  val lowerLimit: Float = 0f
  val upperLimit: Float = 600000f // 30gb

  val sortExperiment: Boolean = false
  val onlyDetectSkew: Boolean = true
  var threshold: Int = 100
  var freeSkewedThreshold: Int = 30000000 // 300
  val firstPhaseNum = 1 // 9
  val firstPhaseDen = 2 // 10

  val singleIterationOnly: Boolean = true // set freeSkewedThreshold to large number
  val dynamicThreshold: Boolean = false
  val controllerHistoryLimitPerWorker: Int = 10000
  val fixedThresholdIncrease: Int = 50
  val upperErrorLimit: Int = 15
  val lowerErrorLimit: Int = 12

  type joinType = String
}

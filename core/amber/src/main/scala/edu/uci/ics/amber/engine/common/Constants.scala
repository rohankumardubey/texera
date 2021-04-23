package edu.uci.ics.amber.engine.common

import scala.concurrent.duration._

object Constants {
  val defaultBatchSize = 200
  val remoteHDFSPath = "hdfs://10.138.0.2:8020"
  val remoteHDFSIP = "10.138.0.2"
  var defaultNumWorkers = 0
  var dataset = 0
  var masterNodeAddr: String = null

  var numWorkerPerNode = 1
  var dataVolumePerNode = 10
  var defaultTau: FiniteDuration = 10.milliseconds

}

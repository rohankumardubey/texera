package edu.uci.ics.amber.engine.recovery

import java.io.{DataInputStream, DataOutputStream, InputStream, OutputStream}
import java.net.URI

import edu.uci.ics.amber.engine.recovery.HDFSLogStorage.hdfs
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}

object HDFSLogStorage{
  val hostAddress = "hdfs://128.195.52.129:9871/"
  val hdfsConf = new Configuration()
  hdfsConf.set("dfs.client.block.write.replace-datanode-on-failure.enable", "false")
  val hdfs = FileSystem.get(new URI(hostAddress),hdfsConf)
}

class HDFSLogStorage[T](logName: String) extends FileLogStorage[T] {

  private lazy val path = new Path(s"./logs/$logName.logfile")

  override def getInputStream: DataInputStream = hdfs.open(path)

  override def getOutputStream: DataOutputStream = {
    if(fileExists){
      hdfs.append(path)
    }else{
      hdfs.create(path)
    }
  }

  override def fileExists: Boolean = hdfs.exists(path)

  override def createDirectories(): Unit = hdfs.mkdirs(path.getParent)

  override def deleteFile(): Unit = hdfs.delete(path,false)
}

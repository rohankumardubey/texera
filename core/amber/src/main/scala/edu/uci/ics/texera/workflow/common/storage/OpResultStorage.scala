package edu.uci.ics.texera.workflow.common.storage

import java.util.concurrent.ConcurrentHashMap

import com.typesafe.scalalogging.LazyLogging
import edu.uci.ics.texera.workflow.common.tuple.schema.Schema
import edu.uci.ics.texera.workflow.operators.sink.storage.{
  MemoryStorage,
  MongoDBStorage,
  SinkStorage
}

/**
  * Public class of operator result storage.
  */
class OpResultStorage(mode: String = "memory") extends Serializable with LazyLogging {

  val cache: ConcurrentHashMap[String, SinkStorage] = new ConcurrentHashMap[String, SinkStorage]()

//  /**
//    * Put the result of an operator to OpResultStorage.
//    * @param key The key used for storage and retrieval.
//    *            Currently it is the uuid inside the cache source or cache sink operator.
//    * @param records The results.
//    */
//  def put(key: String, records: List[Tuple]): Unit

  /**
    * Retrieve the result of an operator from OpResultStorage
    * @param key The key used for storage and retrieval.
    *            Currently it is the uuid inside the cache source or cache sink operator.
    * @return The storage of this operator.
    */
  def get(key: String): SinkStorage = {
    cache.get(key)
  }

  def create(key: String, schema: Schema): SinkStorage = {
    val storage =
      if (mode == "memory") {
        new MemoryStorage()
      } else {
        new MongoDBStorage(key, schema)
      }
    cache.put(key, storage)
    storage
  }

  def contains(key: String): Boolean = {
    cache.containsKey(key)
  }

  /**
    * Manually remove an entry from the cache.
    * @param key The key used for storage and retrieval.
    *            Currently it is the uuid inside the cache source or cache sink operator.
    */
  def remove(key: String): Unit = {
    logger.debug(s"remove $key start")
    if (cache.contains(key)) {
      cache.get(key).clear()
    }
    cache.remove(key)
    logger.debug(s"remove $key end")
  }

  /**
    * Dump everything in result storage. Called when the system exits.
    */
  def dump(): Unit = {
    throw new NotImplementedError()
  }

  /**
    * Load and initialize result storage. Called when the system init.
    */
  def load(): Unit = {
    throw new NotImplementedError()
  }

  /**
    * Close this storage. Used for system termination.
    */
  def close(): Unit = {
    cache.clear()
  }

}
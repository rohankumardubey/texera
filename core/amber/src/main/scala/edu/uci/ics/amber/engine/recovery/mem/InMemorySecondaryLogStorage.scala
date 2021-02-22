package edu.uci.ics.amber.engine.recovery.mem

import edu.uci.ics.amber.engine.common.virtualidentity.ActorVirtualIdentity
import edu.uci.ics.amber.engine.recovery.SecondaryLogStorage

class InMemorySecondaryLogStorage(id: ActorVirtualIdentity) extends SecondaryLogStorage {

  private val idString: String = id.toString

  override def persistCurrentDataCursor(dataCursor: Long): Unit = {
    InMemoryLogStorage.getSecondaryLogOf(idString).enqueue(dataCursor)
  }

  override def load(): Iterable[Long] = {
    InMemoryLogStorage.getSecondaryLogOf(idString)
  }

  override def clear(): Unit = {
    InMemoryLogStorage.clearSecondaryLogOf(idString)
  }
}
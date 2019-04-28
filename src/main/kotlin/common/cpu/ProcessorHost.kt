package therealfarfetchd.retrocomputers.common.cpu

import therealfarfetchd.retrocomputers.common.block.wire.Device

interface ProcessorHost {
  var targetBus: Byte
  val isBusConnected: Boolean
  fun bus(): Device?

  fun resetBusState()
  var allowWrite: Boolean

  var writePos: Short

  fun memRead(at: Short): Byte
  fun memStore(at: Short, data: Byte)
}
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package therealfarfetchd.retrocomputers.common.util

import kotlin.experimental.and
import kotlin.experimental.or

fun Float.toIntBits() = java.lang.Float.floatToIntBits(this)

fun Float.toRawIntBits() = java.lang.Float.floatToRawIntBits(this)
fun Int.toFloatBits() = java.lang.Float.intBitsToFloat(this)

fun Double.toLongBits() = java.lang.Double.doubleToLongBits(this)
fun Double.toRawLongBits() = java.lang.Double.doubleToRawLongBits(this)
fun Long.toDoubleBits() = java.lang.Double.longBitsToDouble(this)

fun List<Int>.nibbles(): List<Byte> {
  var list: List<Byte> = emptyList()
  var inList: List<Int> = this
  if (size % 2 != 0) inList += 0
  for (i in 0 until inList.size / 2) {
    val part1 = inList[i * 2].toByte() and 0x0F
    val part2 = inList[i * 2 + 1].toByte() and 0x0F
    list += part1 or (part2 shl 4)
  }
  return list
}

fun List<Byte>.unpackNibbles(): List<Int> {
  var list: List<Int> = emptyList()
  for (b in this) {
    list += (b and 0x0F).unsigned
    list += (b ushr 4).unsigned
  }
  return list
}

private val Boolean.bitmask: Byte
  get() = if (this) -1 else 0

private val Boolean.bitmaskS: Short
  get() = if (this) -1 else 0

private val Boolean.bitmaskI: Int
  get() = if (this) -1 else 0

private fun BooleanArray.firstN(i: Int): BooleanArray {
  val arr = BooleanArray(i)
  this.copyInto(arr, endIndex = minOf(size, i))
  return arr
}

fun BooleanArray.flip(index: Int) {
  check(index < size) { "Index out of range (expected 0..$size, got $index)" }
  this[index] = !this[index]
}

fun packByte(vararg bools: Boolean): Byte {
  val b = bools.firstN(8)
  return (b[0].bitmask and 1) or
    (b[1].bitmask and 2) or
    (b[2].bitmask and 4) or
    (b[3].bitmask and 8) or
    (b[4].bitmask and 16) or
    (b[5].bitmask and 32) or
    (b[6].bitmask and 64) or
    (b[7].bitmask and -128)
}

fun unpack(b: Byte): BooleanArray {
  val arr = BooleanArray(8)
  for (i in 0..7) {
    arr[i] = (b and (1 shl i).toByte()) != 0.toByte()
  }
  return arr
}

fun packShort(vararg bools: Boolean): Short {
  val b = bools.firstN(16)
  return (b[0].bitmaskS and 1) or
    (b[1].bitmaskS and 2) or
    (b[2].bitmaskS and 4) or
    (b[3].bitmaskS and 8) or
    (b[4].bitmaskS and 16) or
    (b[5].bitmaskS and 32) or
    (b[6].bitmaskS and 64) or
    (b[7].bitmaskS and 128) or
    (b[8].bitmaskS and 256) or
    (b[9].bitmaskS and 512) or
    (b[10].bitmaskS and 1024) or
    (b[11].bitmaskS and 2048) or
    (b[12].bitmaskS and 4096) or
    (b[13].bitmaskS and 8192) or
    (b[14].bitmaskS and 16384) or
    (b[15].bitmaskS and -32768)
}

fun unpack(b: Short): BooleanArray {
  val arr = BooleanArray(16)
  for (i in 0..15) {
    arr[i] = (b and (1 shl i).toShort()) != 0.toShort()
  }
  return arr
}

fun packInt(vararg bools: Boolean): Int {
  val b = bools.firstN(32)
  return (b[0].bitmaskI and 1) or
    (b[1].bitmaskI and 2) or
    (b[2].bitmaskI and 4) or
    (b[3].bitmaskI and 8) or
    (b[4].bitmaskI and 16) or
    (b[5].bitmaskI and 32) or
    (b[6].bitmaskI and 64) or
    (b[7].bitmaskI and 128) or
    (b[8].bitmaskI and 256) or
    (b[9].bitmaskI and 512) or
    (b[10].bitmaskI and 1024) or
    (b[11].bitmaskI and 2048) or
    (b[12].bitmaskI and 4096) or
    (b[13].bitmaskI and 8192) or
    (b[14].bitmaskI and 16384) or
    (b[15].bitmaskI and 32768) or
    (b[16].bitmaskI and 65536) or
    (b[17].bitmaskI and 131072) or
    (b[18].bitmaskI and 262144) or
    (b[19].bitmaskI and 524288) or
    (b[20].bitmaskI and 1048576) or
    (b[21].bitmaskI and 2097152) or
    (b[22].bitmaskI and 4194304) or
    (b[23].bitmaskI and 8388608) or
    (b[24].bitmaskI and 16777216) or
    (b[25].bitmaskI and 33554432) or
    (b[26].bitmaskI and 67108864) or
    (b[27].bitmaskI and 134217728) or
    (b[28].bitmaskI and 268435456) or
    (b[29].bitmaskI and 536870912) or
    (b[30].bitmaskI and 1073741824) or
    (b[31].bitmaskI and -2147483648)
}

fun unpack(b: Int): BooleanArray {
  val arr = BooleanArray(32)
  for (i in 0..31) {
    arr[i] = (b and (1 shl i)) != 0
  }
  return arr
}
package net.dblsaiko.retrocomputers.common.util

val Byte.unsigned: Int
  get() = this.toInt() and 0xFF

val Short.unsigned: Int
  get() = this.toInt() and 0xFFFF

val Int.unsigned: Long
  get() = this.toLong() and 0xFFFFFFFF

infix fun Int.pmod(i: Int): Int = (this % i).let { if (it < 0) it + i else it }
infix fun Long.pmod(i: Int): Int = (this % i).let { if (it < 0) (it + i).toInt() else it.toInt() }
infix fun Long.pmod(l: Long): Long = (this % l).let { if (it < 0) it + l else it }

infix fun Byte.pmod(i: Int): Byte = (this % i).let { if (it < 0) (it + i).toByte() else it.toByte() }

infix fun Byte.shr(i: Int): Byte = (this.toInt() shr i).toByte()
infix fun Byte.ushr(i: Int): Byte = (this.unsigned ushr i).toByte()
infix fun Byte.shl(i: Int): Byte = (this.toInt() shl i).toByte()

infix fun Short.shr(i: Int): Short = (this.toInt() shr i).toShort()
infix fun Short.ushr(i: Int): Short = (this.unsigned ushr i).toShort()
infix fun Short.shl(i: Int): Short = (this.toInt() shl i).toShort()
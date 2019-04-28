package therealfarfetchd.retrocomputers.common.cpu

import net.minecraft.nbt.CompoundTag
import therealfarfetchd.retrocomputers.common.init.Resources
import therealfarfetchd.retrocomputers.common.util.packByte
import therealfarfetchd.retrocomputers.common.util.packShort
import therealfarfetchd.retrocomputers.common.util.unpack
import therealfarfetchd.retrocomputers.common.util.unsigned
import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class Processor(val host: ProcessorHost) {
  private var rB by _8bit()
  private var rI by _16bit()
  private var sp by _16bit()
  private var rp by _16bit()
  var pc by _16bit()
  private var resetAddr by _16bit()
  private var brkAddr by _16bit()
  val flags = BooleanArray(9)

  private var busOffset: Short = 0
  var busEnabled: Boolean = false

  var rA by _8or16bit { flags[M] }
  private var rD by _8or16bit { flags[M] }
  var rX by _8or16bit { flags[X] }
  var rY by _8or16bit { flags[X] }

  var isRunning = false
  var timeout = false
  var error = false
  var wait = false
  var stop = false

  init {
    val loader = Resources.bootloader()
    for (i in 0 until 0x100) host.memStore((i + 0x400).toShort(), loader[i])

    resetAddr = 0x0400
    brkAddr = 0x2000
    pc = resetAddr

    reset()
  }

  fun reset() {
    rA = 0
    rB = 0
    rX = 0
    rY = 0
    rI = 0
    rD = 0

    unpack(0.toShort()).copyInto(flags, endIndex = 9)
    flags[E] = true
    flags[M] = true
    flags[X] = true

    sp = 0x01FF
    rp = 0x02FF
  }

  fun toTag(tag: CompoundTag): CompoundTag {
    tag.putShort("a", rA.toShort())
    tag.putByte("b", rB.toByte())
    tag.putShort("x", rX.toShort())
    tag.putShort("y", rX.toShort())
    tag.putShort("i", rI.toShort())
    tag.putShort("d", rD.toShort())
    tag.putShort("sp", sp.toShort())
    tag.putShort("rp", rp.toShort())
    tag.putShort("pc", pc.toShort())
    tag.putShort("ra", resetAddr.toShort())
    tag.putShort("ba", brkAddr.toShort())
    tag.putShort("f", packShort(*flags))
    tag.putBoolean("t", timeout)
    tag.putShort("bo", busOffset)
    tag.putBoolean("be", busEnabled)
    tag.putBoolean("run", isRunning)
    tag.putBoolean("e", error)
    tag.putBoolean("stp", stop)
    tag.putBoolean("wai", wait)
    return tag
  }

  fun fromTag(tag: CompoundTag) {
    rA = tag.getShort("a").unsigned
    rB = tag.getByte("b").unsigned
    rX = tag.getShort("x").unsigned
    rY = tag.getShort("y").unsigned
    rI = tag.getShort("i").unsigned
    rD = tag.getShort("d").unsigned
    sp = tag.getShort("sp").unsigned
    rp = tag.getShort("rp").unsigned
    pc = tag.getShort("pc").unsigned
    resetAddr = tag.getShort("ra").unsigned
    brkAddr = tag.getShort("ba").unsigned
    unpack(tag.getShort("f")).copyInto(flags, endIndex = 9)
    timeout = tag.getBoolean("t")
    busOffset = tag.getShort("bo")
    busEnabled = tag.getBoolean("be")
    isRunning = tag.getBoolean("run")
    error = tag.getBoolean("e")
    stop = tag.getBoolean("stp")
    wait = tag.getBoolean("wai")
  }

  fun next() {
    stop = false
    wait = false
    val insn = pc1()
    // println("%02x @ %04x".format(insn, pc - 1))
    when (insn) {
      0x00 -> {
        // brk
        push2(pc)
        push1(packShort(*flags).unsigned)
        pc = brkAddr
      }
      0x01 -> {
        // ora (ind, x)
        _i.ora(pc2IX())
      }
      0x02 -> {
        // nxt
        pc = peek2(rI)
        rI += 2
      }
      0x03 -> {
        // ora r, S
        _i.ora(peekM(pc1S()))
      }
      0x04 -> {
        // tsb zp
        _i.tsb(peekM(pc1()))
      }
      0x05 -> {
        // ora zp
        _i.ora(peekM(pc1()))
      }
      0x08 -> {
        // php
        push1(packByte(*flags).unsigned)
      }
      0x09 -> {
        // ora #
        _i.ora(pcM())
      }
      0x0B -> {
        // rhi
        pushr2(rI)
      }
      0x0F -> {
        // mul zp
        _i.mul(peekM(pc1()))
      }
      0x18 -> {
        // clc
        flags[C] = false
      }
      0x1A -> {
        // inc a
        rA += 1
        sNZ(rA)
      }
      0x20 -> {
        // jsr abs
        val addr = pc2()
        push2(pc)
        pc = addr
      }
      0x22 -> {
        // ent
        pushr2(rI)
        rI = pc + 2
        pc = pc2()
      }
      0x23 -> {
        // and r, S
        _i.and(peekM(pc1S()))
      }
      0x28 -> {
        // plp
        setFlags(pop1())
      }
      0x2A -> {
        // rol a
        rA = _i.rol(rA)
      }
      0x2B -> {
        // rli
        rI = popr2()
        sNXZ(rI)
      }
      0x30 -> {
        // bmi rel
        _i.bra(pc1(), flags[N])
      }
      0x3D -> {
        // and abs, x
        _i.and(peekM(pc2X()))
      }
      0x38 -> {
        // sec
        flags[C] = true
      }
      0x3A -> {
        // dec a
        rA -= 1
        sNZ(rA)
      }
      0x3F -> {
        // mul abs, x
        _i.mul(peekM(pc2X()))
      }
      0x42 -> {
        // nxa
        rA = peekM(rI)
        rI += (if (flags[M]) 1 else 2)
      }
      0x43 -> {
        // eor r, S
        _i.eor(pc1S())
      }
      0x45 -> {
        // eor zp
        _i.eor(peekM(pc1()))
      }
      0x48 -> {
        // pha
        pushM(rA)
      }
      0x49 -> {
        // eor #
        _i.eor(pcM())
      }
      0x4B -> {
        // rha
        pushrM(rA)
      }
      0x4C -> {
        // jmp abs
        pc = pc2()
      }
      0x50 -> {
        // bvc rel
        _i.bra(pc1(), !flags[V])
      }
      0x5A -> {
        // phy
        pushX(rY)
      }
      0x5B -> {
        // rhy
        pushrX(rY)
      }
      0x5C -> {
        // txi
        rI = rX
        sNXZ(rX)
      }
      0x60 -> {
        // rts
        pc = pop2()
      }
      0x63 -> {
        // adc r, S
        _i.adc(peekM(pc1S()))
      }
      0x64 -> {
        // stz zp
        _i.stz(pc1())
      }
      0x65 -> {
        // adc zp
        _i.adc(peekM(pc1()))
      }
      0x68 -> {
        // pla
        rA = popM()
        sNZ(rA)
      }
      0x69 -> {
        // adc #
        _i.adc(pcM())
      }
      0x6A -> {
        // ror a
        rA = _i.ror(rA)
      }
      0x6B -> {
        // rla
        rA = poprM()
        sNZ(rA)
      }
      0x6D -> {
        // adc abs
        _i.adc(peekM(pc2()))
      }
      0x70 -> {
        // bvs rel
        _i.bra(pc1(), flags[V])
      }
      0x74 -> {
        // stz zp, x
        _i.stz(pc1X())
      }
      0x7A -> {
        // ply
        rY = popX()
        sNXZ(rY)
      }
      0x7B -> {
        // rly
        rY = poprX()
        sNXZ(rY)
      }
      0x7C -> {
        // jmp (ind, x)
        pc = pc2IX()
      }
      0x7D -> {
        // adc abs, x
        _i.adc(peekM(pc2X()))
      }
      0x7F -> {
        // div abs, x
        _i.div(peekM(pc2X()))
      }
      0x80 -> {
        // bra rel
        _i.bra(pc1(), true)
      }
      0x84 -> {
        // sty zp
        _i.sty(pc1())
      }
      0x85 -> {
        // sta zp
        _i.sta(pc1())
      }
      0x86 -> {
        // stx zp
        _i.stx(pc1())
      }
      0x88 -> {
        // dey
        rY -= 1
        sNXZ(rY)
      }
      0x89 -> {
        // bit #
        flags[Z] = (rA and pcM()) == 0
      }
      0x8A -> {
        // txa
        rA = rX
        sNZ(rA)
      }
      0x8B -> {
        // txr
        rp = rX
        sNXZ(rX)
      }
      0x8C -> {
        // sty abs
        _i.sty(pc2())
      }
      0x8D -> {
        // sta abs
        _i.sta(pc2())
      }
      0x8E -> {
        // stx abs
        _i.stx(pc2())
      }
      0x90 -> {
        // bcc rel
        _i.bra(pc1(), !flags[C])
      }
      0x91 -> {
        // sta (ind), y
        _i.sta(pc2IY())
      }
      0x92 -> {
        // sta (ind)
        _i.sta(pc2I())
      }
      0x93 -> {
        // sta (r, S), y
        _i.sta(pc2ISY())
      }
      0x94 -> {
        // sty zp, x
        _i.sty(pc1X())
      }
      0x95 -> {
        // sta zp, x
        _i.sta(pc1X())
      }
      0x98 -> {
        // tya
        rA = rY
      }
      0x99 -> {
        // sta abs, y
        _i.sta(pc2Y())
      }
      0x9A -> {
        // txs
        sp = rX
      }
      0x9C -> {
        // stz abs
        _i.stz(pc2())
      }
      0x9D -> {
        // sta abs, x
        _i.sta(pc2X())
      }
      0x9E -> {
        // stz abs, x
        _i.stz(pc2X())
      }
      0x9F -> {
        // sea
        rD = 0
        if ((flags[M] && rA.toByte() < 0) || (!flags[M] && rA.toShort() < 0)) rD = maskM
      }
      0xA0 -> {
        // ldy #
        _i.ldy(pcX())
      }
      0xA2 -> {
        // ldx #
        _i.ldx(pcX())
      }
      0xA3 -> {
        // lda r, S
        _i.lda(peekM(pc1S()))
      }
      0xA5 -> {
        // lda zp
        _i.lda(peekM(pc1()))
      }
      0xA7 -> {
        // lda r, R
        _i.lda(peekM(pc1R()))
      }
      0xA8 -> {
        // tay
        rY = rA
        sNXZ(rY)
      }
      0xA9 -> {
        // lda #
        _i.lda(pcM())
      }
      0xAA -> {
        // tax
        rX = rA
        sNXZ(rX)
      }
      0xAD -> {
        // lda abs
        _i.lda(peekM(pc2()))
      }
      0xAE -> {
        // ldx abs
        _i.ldx(peekM(pc2()))
      }
      0xB0 -> {
        // bcs rel
        _i.bra(pc1(), flags[C])
      }
      0xB1 -> {
        // lda (ind), y
        _i.lda(peekM(pc2IY()))
      }
      0xB3 -> {
        // lda (r, S), y
        _i.lda(peekM(pc2ISY()))
      }
      0xB9 -> {
        // lda abs, y
        _i.lda(peekM(pc2Y()))
      }
      0xBA -> {
        // tsx
        rX = sp
        sNXZ(rX)
      }
      0xBD -> {
        // lda abs, x
        _i.lda(peekM(pc2X()))
      }
      0xC0 -> {
        // cpy #
        _i.cmpx(rY, pcX())
      }
      0xC2 -> {
        // rep #
        _i.rep(pc1())
      }
      0xC3 -> {
        // cmp r, S
        _i.cmp(rA, peekM(pc1S()))
      }
      0xC6 -> {
        // dec zp
        _i.dec(pc1())
      }
      0xC8 -> {
        // iny
        rY += 1
        sNXZ(rY)
      }
      0xC9 -> {
        // cmp #
        _i.cmp(rA, pcM())
      }
      0xCA -> {
        // dex
        rX -= 1
        sNXZ(rX)
      }
      0xCB -> {
        // wai
        timeout = true
        wait = true
      }
      0xCF -> {
        // pld
        rD = popM()
      }
      0xD0 -> {
        // bne rel
        _i.bra(pc1(), !flags[Z])
      }
      0xDA -> {
        // phx
        pushX(rX)
      }
      0xDB -> {
        // stp
        isRunning = false
        stop = true
      }
      0xDC -> {
        // tix
        rX = rI
        sNXZ(rX)
      }
      0xDD -> {
        // cmp abs, x
        _i.cmp(rA, peekM(pc2X()))
      }
      0xDE -> {
        // dec abs, x
        _i.dec(pc2X())
      }
      0xDF -> {
        // phd
        pushM(rD)
      }
      0xE2 -> {
        // sep #
        _i.sep(pc1())
      }
      0xE3 -> {
        // sbc s, R
        _i.sbc(peekM(pc1S()))
      }
      0xE6 -> {
        // inc zp
        _i.inc(pc1())
      }
      0xEB -> {
        // xba
        if (flags[M]) {
          val b = rB
          rB = rA
          rA = b
        } else {
          val a = rA shl 8
          val b = ub(rA shr 8)
          rA = a or b
        }
      }
      0xEE -> {
        // inc abs
        _i.inc(pc2())
      }
      0xEF -> {
        // mmu
        _i.mmu(pc1())
      }
      0xF0 -> {
        // beq rel
        _i.bra(pc1(), flags[Z])
      }
      0xFA -> {
        // plx
        rX = popX()
        sNXZ(rX)
      }
      0xFB -> {
        // xce
        if (flags[C] != flags[E]) {
          if (flags[C]) {
            flags[C] = false
            flags[E] = true
            flags[X] = true
            if (!flags[M]) rB = rA shr 8
            flags[M] = true
          } else {
            flags[C] = true
            flags[E] = false
          }
        }
      }
      0xFE -> {
        // inc abs, x
        _i.inc(pc2X())
      }
      else -> {
        error = true
        // println("Invalid opcode: %02x at %04x%n".format(insn, pc - 1))
        // mem.halt()
      }
    }
  }

  // Read from PC

  private fun pc1(): Int {
    pc += 1
    return peek1(pc - 1)
  }

  private fun pc2(): Int = pc1() or (pc1() shl 8)

  private fun pc2X(): Int = us(pc2() + rX)

  private fun pc2Y(): Int = us(pc2() + rY)

  private fun pc1S(): Int = us(pc1() + sp)

  private fun pc1R(): Int = us(pc1() + rp)

  private fun pcM(): Int = if (flags[M]) pc1() else pc2()

  private fun pcX(): Int = if (flags[X]) pc1() else pc2()

  private fun pc1X(): Int = uX(pc1() + rX)

  private fun pc1Y(): Int = uX(pc1() + rY)

  private fun pcMX(): Int = uX(pcM() + rX)

  private fun pcXX(): Int = uX(pcX() + rX)

  private fun pc2I(): Int = peek2(pc2())

  private fun pc2IX(): Int = peek2(pc2() + rX)

  private fun pc2IY(): Int = us(peek2(pc2()) + rY)

  private fun pc2SY(): Int = us(peek2(pc1S()) + rY)

  private fun pc2RY(): Int = us(peek2(pc1R()) + rY)

  private fun pc2ISY(): Int = us(peek2(pc1S()) + rY)

  // Read from memory address

  private fun peek1(addr: Int): Int {
    val uaddr = addr and 0xFFFF
    val ubusOff = busOffset.unsigned
    return if (busEnabled && uaddr in ubusOff..ubusOff + 0x00FF) {
      val bus = host.bus()
      if (bus == null) {
        timeout = true
        0
      } else bus.read((addr - ubusOff).toByte()).unsigned
    } else host.memRead(addr.toShort()).unsigned
  }

  fun peek2(addr: Int): Int = peek1(addr) or (peek1(addr + 1) shl 8)

  private fun peekM(addr: Int): Int = if (flags[M]) peek1(addr) else peek2(addr)

  private fun peekX(addr: Int): Int = if (flags[X]) peek1(addr) else peek2(addr)

  // Write to memory address

  fun poke1(addr: Int, b: Int) {
    val uaddr = addr and 0xFFFF
    val ubusOff = busOffset.unsigned
    if (busEnabled && uaddr in ubusOff..ubusOff + 0x00FF) {
      val bus = host.bus()
      if (bus == null) timeout = true
      else bus.store((addr - ubusOff).toByte(), b.toByte())
    } else host.memStore(addr.toShort(), b.toByte())
  }

  fun poke2(addr: Int, s: Int) {
    poke1(addr, s)
    poke1(addr + 1, s shr 8)
  }

  private fun pokeM(addr: Int, s: Int) = if (flags[M]) poke1(addr, s) else poke2(addr, s)

  private fun pokeX(addr: Int, s: Int) = if (flags[X]) poke1(addr, s) else poke2(addr, s)

  // Parameter stack ops

  private fun push1(b: Int) {
    poke1(sp, b)
    sp -= 1
  }

  private fun push2(s: Int) {
    push1(s shr 8)
    push1(s)
  }

  private fun pushM(s: Int) {
    if (flags[M]) push1(s)
    else push2(s)
  }

  private fun pushX(s: Int) {
    if (flags[X]) push1(s)
    else push2(s)
  }

  private fun pop1(): Int {
    sp += 1
    return peek1(sp)
  }

  private fun pop2(): Int {
    return pop1() or (pop1() shl 8)
  }

  private fun popM(): Int {
    return if (flags[M]) pop1()
    else pop2()
  }

  private fun popX(): Int {
    return if (flags[X]) pop1()
    else pop2()
  }

  // Return stack ops

  private fun pushr1(b: Int) {
    poke1(rp, b)
    rp -= 1
  }

  private fun pushr2(s: Int) {
    pushr1(s shr 8)
    pushr1(s)
  }

  private fun pushrM(s: Int) {
    if (flags[M]) pushr1(s)
    else pushr2(s)
  }

  private fun pushrX(s: Int) {
    if (flags[X]) pushr1(s)
    else pushr2(s)
  }

  private fun popr1(): Int {
    rp += 1
    return peek1(rp)
  }

  private fun popr2(): Int {
    return popr1() or (popr1() shl 8)
  }

  private fun poprM(): Int {
    return if (flags[M]) popr1()
    else popr2()
  }

  private fun poprX(): Int {
    return if (flags[X]) popr1()
    else popr2()
  }

  // Masks

  private val maskM: Int
    get() = if (flags[M]) 0xFF else 0xFFFF

  private val negM: Int
    get() = if (flags[M]) 0x80 else 0x8000

  private val maskX: Int
    get() = if (flags[X]) 0xFF else 0xFFFF

  private val negX: Int
    get() = if (flags[X]) 0x80 else 0x8000

  // Set flags based on value

  private fun sNZ(_s: Int) {
    val s = uM(_s)
    flags[Z] = s == 0
    flags[N] = s and negM != 0
  }

  private fun sNXZ(_s: Int) {
    val s = uX(_s)
    flags[Z] = s == 0
    flags[N] = s and negX != 0
  }

  private fun sNZC(s: Int) {
    flags[C] = sM(s) >= 0
    sNZ(s)
  }

  private fun sNXZC(s: Int) {
    flags[C] = sX(s) >= 0
    sNXZ(s)
  }

  // BCD conversion

  private fun toBCD(s: Int): Int {
    val i = us(s)
    return us(i.toString().toInt(16))
  }

  private fun fromBCD(s: Int): Int {
    val i = us(s)
    return us(i.toString(16).toIntOrNull() ?: { error = true; 0 }())
  }

  // Instruction impl

  private val _i by lazy { InsnImpl() }

  private inner class InsnImpl {
    fun adc(data: Int) {
      val i = if (flags[D]) {
        val a = fromBCD(rA)
        val d = fromBCD(data)
        toBCD(a + d + if (flags[C]) 1 else 0)
      } else {
        rA + data + if (flags[C]) 1 else 0
      }
      flags[C] = uM(i) != i
      flags[V] = ((rA xor data) and (rA xor i) and negM) != 0
      rA = i
      sNZ(rA)
    }

    fun sbc(data: Int) {
      adc(uM(data.inv()))
    }

    fun inc(addr: Int) {
      val data = peekM(addr) + 1
      pokeM(addr, data)
      sNZ(data)
    }

    fun dec(addr: Int) {
      val data = peekM(addr) - 1
      pokeM(addr, data)
      sNZ(data)
    }

    fun mul(data: Int) {
      if (flags[C]) {
        if (flags[M]) {
          val c: Int = (rA and 0xFF) * (data and 0xFF)
          rA = c
          rD = c shr 8
          flags[N] = false
          flags[Z] = c == 0
          flags[V] = (c and -0x00010000) != 0 // 0xFFFF0000 because kotlin is a special snowflake
        } else {
          val c: Long = (rA.toLong() and 0xFFFF) * (data.toLong() and 0xFFFF)
          rA = c.toInt()
          rD = (c shr 16).toInt()
          flags[N] = false
          flags[Z] = c == 0L
          flags[V] = (c and -0x0000000100000000L) != 0L // 0xFFFFFFFF00000000L because kotlin is a special snowflake
        }
      } else {
        if (flags[M]) {
          val c: Int = sM(rA) * sM(data)
          rA = c
          rD = c shr 8
          flags[N] = c < 0
          flags[Z] = c == 0
          flags[V] = (c and -0x00010000) != 0 // 0xFFFF0000 because kotlin is a special snowflake
        } else {
          val c: Long = sM(rA).toLong() * sM(data).toLong()
          rA = c.toInt()
          rD = (c shr 16).toInt()
          flags[N] = c < 0
          flags[Z] = c == 0L
          flags[V] = (c and -0x0000000100000000L) != 0L // 0xFFFFFFFF00000000L because kotlin is a special snowflake
        }
      }
    }

    fun div(data: Int) {
      if (data == 0) {
        flags[V] = true
        rA = 0
        rD = 0
        sNZ(0)
        error = true
      } else {
        if (flags[C]) {
          if (flags[M]) {
            val a = (rA or (rD shl 8)) and 0xFFFF
            rA = a / data
            rD = a % data
          } else {
            val a = (rA.toLong() or (rD.toLong() shl 16)) and 0xFFFFFFFFL
            rA = (a / data).toInt()
            rD = (a % data).toInt()
          }
        } else {
          if (flags[M]) {
            val a: Short = (rA or (rD shl 8)).toShort()
            val b: Byte = data.toByte()
            rA = a / b
            rD = a % b
          } else {
            val a = rA or (rD shl 16)
            rA = a / data
            rD = a % data
          }
        }
        sNZ(rA)
        flags[V] = rD != 0
      }
    }

    fun and(data: Int) {
      rA = rA and data
      sNZ(rA)
    }

    fun ora(data: Int) {
      rA = rA or data
      sNZ(rA)
    }

    fun eor(data: Int) {
      rA = rA xor data
      sNZ(rA)
    }

    fun rol(data: Int): Int {
      val i = us((data shl 1) or (if (flags[C]) 1 else 0))
      flags[C] = (data and negM) != 0
      sNZ(i)
      return i
    }

    fun ror(data: Int): Int {
      val i = us((data ushr 1) or (if (flags[C]) negM else 0))
      flags[C] = (data and 1) != 0
      sNZ(i)
      return i
    }

    fun tsb(data: Int) {
      flags[Z] = (data and rA) != 0
      rA = rA or data
    }

    fun bra(off: Int, b: Boolean) {
      if (b) pc += sb(off)
    }

    fun stz(addr: Int) {
      poke1(addr, 0)
      if (!flags[M]) poke1(addr + 1, 0)
    }

    fun sta(addr: Int) = pokeM(addr, rA)

    fun stx(addr: Int) = pokeX(addr, rX)

    fun sty(addr: Int) = pokeX(addr, rY)

    fun lda(data: Int) {
      rA = data
      sNZ(rA)
    }

    fun ldx(data: Int) {
      rX = data
      sNXZ(rX)
    }

    fun ldy(data: Int) {
      rY = data
      sNXZ(rY)
    }

    fun cmp(a: Int, b: Int) {
      sNZC(a - b)
    }

    fun cmpx(a: Int, b: Int) {
      sNXZC(a - b)
    }

    fun sep(data: Int) = setFlags((packShort(*flags) or data.toShort()).unsigned)

    fun rep(data: Int) = setFlags((packShort(*flags) and data.toShort().inv()).unsigned)

    fun mmu(data: Int) {
      when (ub(data)) {
        0x00 -> {
          if (host.targetBus != rA.toByte()) timeout = true
          host.targetBus = rA.toByte()
        }
        0x80 -> rA = host.targetBus.unsigned

        0x01 -> busOffset = rA.toShort()
        0x81 -> rA = busOffset.unsigned

        0x02 -> busEnabled = true
        0x82 -> busEnabled = false

        0x03 -> host.allowWrite = true
        0x04 -> host.allowWrite = false

        0x05 -> brkAddr = rA
        0x85 -> rA = brkAddr

        0x06 -> resetAddr = rA
        0x86 -> rA = resetAddr

        else -> error = true
      }
    }
  }

  private fun setFlags(b: Int) {
    var f = 0.toShort()
    f = f or ((b and (Nf + Vf + Df + If + Zf + Cf)).toShort())
    if (!flags[E]) {
      f = f or ((b and Xf).toShort())
      if (flags[M] != ((b and Mf) != 0)) {
        if (flags[M]) {
          rA = rA or (rB shl 8)
        } else {
          rB = rA shr 8
        }
      }
      f = f or ((b and Mf).toShort())
    }
    unpack(f).copyInto(flags, endIndex = 9)
  }

  private fun _8bit(): ReadWriteProperty<Processor, Int> = object : ReadWriteProperty<Processor, Int> {
    var value: Byte = 0

    override fun getValue(thisRef: Processor, property: KProperty<*>): Int = value.unsigned

    override fun setValue(thisRef: Processor, property: KProperty<*>, value: Int) {
      this.value = value.toByte()
    }
  }

  private fun _16bit(): ReadWriteProperty<Processor, Int> = object : ReadWriteProperty<Processor, Int> {
    var value: Short = 0

    override fun getValue(thisRef: Processor, property: KProperty<*>): Int = value.unsigned

    override fun setValue(thisRef: Processor, property: KProperty<*>, value: Int) {
      this.value = value.toShort()
    }
  }

  private fun _8or16bit(op: () -> Boolean): ReadWriteProperty<Processor, Int> = object : ReadWriteProperty<Processor, Int> {
    var value: Short = 0

    override fun getValue(thisRef: Processor, property: KProperty<*>): Int = value.unsigned and (if (op()) 0xFF else 0xFFFF)

    override fun setValue(thisRef: Processor, property: KProperty<*>, value: Int) {
      this.value = value.toShort()
    }
  }

  /**
   * Convert to signed byte
   */
  private fun sb(i: Int): Int = i.toByte().toInt()

  /**
   * Convert to unsigned byte
   */
  private fun ub(i: Int): Int = i.toByte().unsigned

  /**
   * Convert to signed short
   */
  private fun ss(i: Int): Int = i.toShort().toInt()

  /**
   * Convert to unsigned short
   */
  private fun us(i: Int): Int = i.toShort().unsigned

  private fun sM(i: Int): Int = if (flags[M]) sb(i) else ss(i)
  private fun sX(i: Int): Int = if (flags[X]) sb(i) else ss(i)
  private fun uM(i: Int): Int = if (flags[M]) ub(i) else us(i)
  private fun uX(i: Int): Int = if (flags[X]) ub(i) else us(i)

  companion object {
    const val C = 0
    const val Z = 1
    const val I = 2
    const val D = 3
    const val X = 4
    const val M = 5
    const val V = 6
    const val N = 7
    const val E = 8

    const val Cf = 1 shl C
    const val Zf = 1 shl Z
    const val If = 1 shl I
    const val Df = 1 shl D
    const val Xf = 1 shl X
    const val Mf = 1 shl M
    const val Vf = 1 shl V
    const val Nf = 1 shl N
    const val Ef = 1 shl E
  }
}
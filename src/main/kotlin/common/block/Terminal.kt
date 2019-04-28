package therealfarfetchd.retrocomputers.common.block

import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.CompoundTag
import net.minecraft.util.Hand
import net.minecraft.util.Tickable
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.BlockView
import net.minecraft.world.World
import therealfarfetchd.retrocomputers.RetroComputers
import therealfarfetchd.retrocomputers.common.init.BlockEntityTypes
import therealfarfetchd.retrocomputers.common.init.Resources
import therealfarfetchd.retrocomputers.common.util.unsigned
import kotlin.experimental.xor

class TerminalBlock : BaseBlock() {

  override fun activate(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): Boolean {
    val te = world.getBlockEntity(pos) as? TerminalEntity ?: return false
    if (world.isClient) RetroComputers.proxy.openTerminalScreen(te)
    return true
  }

  override fun createBlockEntity(view: BlockView) = TerminalEntity()

}

class TerminalEntity : BaseBlockEntity(BlockEntityTypes.Terminal), Tickable {

  override var busId: Byte = 1

  val screen = ByteArray(80 * 50)
  val charset = Resources.charset()
  val kb = ByteArray(16)

  var command: Byte = 0

  var row = 0
  var cx = 0
  var cy = 0
  var cm = 2
  var kbs = 0
  var kbp = 0

  var bx1 = 0
  var by1 = 0
  var bx2 = 0
  var by2 = 0
  var bw = 0
  var bh = 0

  var char = 0

  override fun tick() {
    val world = getWorld() ?: return
    if (world.isClient) return

    var error = false

    when (command.unsigned) {
      1 -> getIndices(bx2, by2, bw, bh).forEach { screen[it] = bx1.toByte() }
      2 -> getIndices(bx2, by2, bw, bh).forEach { screen[it] = screen[it] xor 0x80.toByte() }
      3 -> getIndices(bx2, by2, bw, bh).zip(getIndices(bx1, by1, bw, bh)).forEach { (dest, src) -> screen[dest] = screen[src] }
      4 -> Resources.charset().copyInto(charset)
      255 -> Unit
      else -> error = true
    }

    if (command in 1..4) world.updateListeners(getPos(), cachedState, cachedState, 3)

    command = if (error) -1 else 0
  }

  fun pushKey(byte: Byte): Boolean {
    return if ((kbp + 1) % 16 != kbs) {
      kb[kbp] = byte
      kbp = (kbp + 1) % 16
      true
    } else false
  }

  private fun getIndices(x1: Int, y1: Int, w: Int, h: Int): Sequence<Int> = sequence {
    for (i in 0 until h) for (j in 0 until w) {
      val x = j + x1
      val y = i + y1

      if (x in 0 until 80 && y in 0 until 60)
        yield(x + 80 * y)
    }
  }

  override fun readData(at: Byte): Byte {
    return when (val at = at.unsigned) {
      0x00 -> row.toByte()
      0x01 -> cx.toByte()
      0x02 -> cy.toByte()
      0x03 -> cm.toByte()
      0x04 -> kbs.toByte()
      0x05 -> kbp.toByte()
      0x06 -> kb[kbs]
      0x07 -> command
      0x08 -> bx1.toByte()
      0x09 -> by1.toByte()
      0x0A -> bx2.toByte()
      0x0B -> by2.toByte()
      0x0C -> bw.toByte()
      0x0D -> bh.toByte()
      0x0E -> char.toByte()
      in 0x10..0x5F -> screen[row * 80 + at - 0x10]
      in 0x60..0x67 -> charset[char * 8 + at - 0x60]
      else -> 0
    }
  }

  override fun storeData(at: Byte, data: Byte) {
    when (val at = at.unsigned) {
      0x00 -> row = data.unsigned % 50
      0x01 -> cx = data.unsigned % 80
      0x02 -> cy = data.unsigned % 50
      0x03 -> cm = data.unsigned % 3
      0x04 -> kbs = data.unsigned % 16
      0x05 -> kbp = data.unsigned % 16
      0x06 -> kb[kbs] = data
      0x07 -> command = data
      0x08 -> bx1 = data.unsigned % 80
      0x09 -> by1 = data.unsigned % 50
      0x0A -> bx2 = data.unsigned % 80
      0x0B -> by2 = data.unsigned % 50
      0x0C -> bw = data.unsigned
      0x0D -> bh = data.unsigned
      0x0E -> char = data.unsigned
      in 0x10..0x5F -> screen[row * 80 + at - 0x10] = data
      in 0x60..0x67 -> charset[char * 8 + at - 0x60] = data
    }

    val needsClientUpdate = at.unsigned in setOf(0x01, 0x02, 0x03) + (0x10..0x67)
    if (needsClientUpdate)
      getWorld()?.updateListeners(getPos(), cachedState, cachedState, 3)
    markDirty()
  }

  override fun toClientTag(tag: CompoundTag): CompoundTag {
    // these are big, TODO: only send changed data
    tag.putByteArray("screen", screen)
    tag.putByteArray("charset", charset)
    tag.putByte("cx", cx.toByte())
    tag.putByte("cy", cy.toByte())
    tag.putByte("cm", cm.toByte())
    return super.toClientTag(tag)
  }

  override fun fromClientTag(tag: CompoundTag) {
    super.fromClientTag(tag)
    tag.getByteArray("screen").copyInto(screen)
    tag.getByteArray("charset").copyInto(charset)
    cx = tag.getByte("cx").unsigned
    cy = tag.getByte("cy").unsigned
    cm = tag.getByte("cm").unsigned
  }

  override fun toTag(tag: CompoundTag): CompoundTag {
    tag.putByteArray("screen", screen)
    tag.putByteArray("charset", charset)
    tag.putByteArray("kb", kb)
    tag.putByte("command", command)
    tag.putByte("row", row.toByte())
    tag.putByte("cx", cx.toByte())
    tag.putByte("cy", cy.toByte())
    tag.putByte("cm", cm.toByte())
    tag.putByte("kbs", kbs.toByte())
    tag.putByte("kbp", kbp.toByte())
    tag.putByte("bx1", bx1.toByte())
    tag.putByte("by1", by1.toByte())
    tag.putByte("bx2", bx2.toByte())
    tag.putByte("by2", by2.toByte())
    tag.putByte("bw", bw.toByte())
    tag.putByte("bh", bh.toByte())
    tag.putByte("char", char.toByte())
    return super.toTag(tag)
  }

  override fun fromTag(tag: CompoundTag) {
    super.fromTag(tag)
    tag.getByteArray("screen").copyInto(screen)
    tag.getByteArray("charset").copyInto(charset)
    tag.getByteArray("kb").copyInto(kb)
    command = tag.getByte("command")
    row = tag.getByte("row").unsigned
    cx = tag.getByte("cx").unsigned
    cy = tag.getByte("cy").unsigned
    cm = tag.getByte("cm").unsigned
    kbs = tag.getByte("kbs").unsigned
    kbp = tag.getByte("kbp").unsigned
    bx1 = tag.getByte("bx1").unsigned
    by1 = tag.getByte("by1").unsigned
    bx2 = tag.getByte("bx2").unsigned
    by2 = tag.getByte("by2").unsigned
    bw = tag.getByte("bw").unsigned
    bh = tag.getByte("bh").unsigned
    char = tag.getByte("char").unsigned
  }

}
package net.dblsaiko.retrocomputers.common.block

import net.dblsaiko.hctm.common.util.ext.makeStack
import net.dblsaiko.retrocomputers.common.init.BlockEntityTypes
import net.dblsaiko.retrocomputers.common.item.ext.ItemDisk
import net.dblsaiko.retrocomputers.common.util.shr
import net.dblsaiko.retrocomputers.common.util.unsigned
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.StateManager.Builder
import net.minecraft.state.property.BooleanProperty
import net.minecraft.util.ActionResult
import net.minecraft.util.ActionResult.FAIL
import net.minecraft.util.ActionResult.PASS
import net.minecraft.util.ActionResult.SUCCESS
import net.minecraft.util.Hand
import net.minecraft.util.ItemScatterer
import net.minecraft.util.Tickable
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.BlockView
import net.minecraft.world.World
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.math.min

class DiskDriveBlock : BaseBlock() {

  init {
    defaultState = defaultState.with(DiskDriveProperties.HasDisk, false)
  }

  override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
    val ent = world.getBlockEntity(pos) as? DiskDriveEntity ?: return FAIL
    return if (ent.ejectDisk() || ent.insertDisk(player.getStackInHand(hand))) SUCCESS else PASS
  }

  override fun onBlockRemoved(state: BlockState, world: World, pos: BlockPos, newState: BlockState, boolean_1: Boolean) {
    if (state.block != newState.block) {
      (world.getBlockEntity(pos) as? DiskDriveEntity)?.ejectDisk(breakBlock = true)
    }

    super.onBlockRemoved(state, world, pos, newState, boolean_1)
  }

  override fun appendProperties(b: Builder<Block, BlockState>) {
    super.appendProperties(b)
    b.add(DiskDriveProperties.HasDisk)
  }

  override fun createBlockEntity(view: BlockView) = DiskDriveEntity()

}

object DiskDriveProperties {
  val HasDisk = BooleanProperty.of("has_disk")
}

class DiskDriveEntity : BaseBlockEntity(BlockEntityTypes.DiskDrive), Tickable {

  override var busId: Byte = 2

  private var stack = Items.AIR.makeStack()

  private val buffer = ByteArray(128)
  private var sector: Short = 0
  private var command: Byte = 0

  override fun tick() {
    val world = getWorld() as? ServerWorld ?: return
    if (world.isClient) return

    var error = false
    val disk = disk()

    val command = command.unsigned
    val sector = sector.unsigned

    if (command in 1..5 && disk == null) {
      error = true
    } else if (command !in setOf(0, 255)) {
      markDirty()
      when (command) {
        1 -> {
          disk!!
          val arr = disk.getLabel(stack).toByteArray(Charsets.US_ASCII)
          val len = min(arr.size, 126)
          arr.copyInto(buffer, endIndex = len)
          buffer[len] = 0
        }
        2 -> {
          disk!!
          val bytes = buffer.takeWhile { it != 0.toByte() }.toByteArray()
          val str = String(bytes, Charsets.US_ASCII)
          disk.setLabel(stack, str)
        }
        3 -> {
          disk!!
          val arr = disk.getUuid(stack).toString().toByteArray(Charsets.US_ASCII)
          val len = min(arr.size, 126)
          arr.copyInto(buffer, endIndex = len)
          buffer[len] = 0
        }
        4 -> {
          disk!!
          val data = disk.sector(stack, world, sector)
          if (sector >= 2048 || data == null || data.isEmpty()) {
            error = true
          } else data.use {
            it.data.copyInto(buffer)
          }
        }
        5 -> {
          disk!!
          val data = disk.sector(stack, world, sector)
          if (sector >= 2048 || data == null) {
            error = true
          } else data.use {
            buffer.copyInto(it.data)
          }
        }
        6 -> {
          buffer.fill(0)
        }
        else -> {
          error = true
        }
      }
    }

    this.command = if (error) -1 else 0
  }

  fun disk(): ItemDisk? = stack.item as? ItemDisk

  fun ejectDisk(breakBlock: Boolean = false): Boolean {
    val world = getWorld() ?: return false
    if (!hasDisk()) return false
    if (world.isClient) return true

    if (!breakBlock) {
      val dirVec = Vec3d(cachedState[BaseBlock.Direction].vector)
      val pos = Vec3d(pos)
        .add(0.5, 0.5, 0.5)
        .add(dirVec.multiply(0.75))
      val item = ItemEntity(world, pos.x, pos.y, pos.z, stack)
      item.velocity = dirVec.multiply(0.1)
      world.spawnEntity(item)
      world.setBlockState(getPos(), cachedState.with(DiskDriveProperties.HasDisk, false))
    } else {
      ItemScatterer.spawn(world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), stack)
    }

    stack = Items.AIR.makeStack()

    return true
  }

  private fun hasDisk(): Boolean {
    return if ((getWorld() ?: return false).isClient) cachedState[DiskDriveProperties.HasDisk] else !stack.isEmpty
  }

  fun insertDisk(stack: ItemStack): Boolean {
    val world = getWorld() ?: return false
    if (stack.isEmpty || stack.item !is ItemDisk) return false

    this.stack = stack.split(1)
    world.setBlockState(getPos(), cachedState.with(DiskDriveProperties.HasDisk, true))

    return true
  }

  override fun readData(at: Byte): Byte {
    return when (val at = at.unsigned) {
      in 0x00..0x7F -> buffer[at]
      0x80 -> sector.toByte()
      0x81 -> (sector shr 8).toByte()
      0x82 -> command
      else -> 0
    }
  }

  override fun storeData(at: Byte, data: Byte) {
    markDirty()
    when (val at = at.unsigned) {
      in 0x00..0x7F -> buffer[at] = data
      0x80 -> sector = (sector and 0xFF00.toShort()) or data.unsigned.toShort()
      0x81 -> sector = (sector and 0x00FF.toShort()) or (data.unsigned shl 8).toShort()
      0x82 -> command = data
    }
  }

  override fun toTag(tag: CompoundTag): CompoundTag {
    tag.put("item", stack.toTag(CompoundTag()))
    return super.toTag(tag)
  }

  override fun fromTag(tag: CompoundTag) {
    super.fromTag(tag)
    stack = ItemStack.fromTag(tag.getCompound("item"))
  }

}
package net.dblsaiko.retrocomputers.common.block

import net.dblsaiko.retrocomputers.RetroComputers
import net.dblsaiko.retrocomputers.common.block.wire.Device
import net.dblsaiko.retrocomputers.common.block.wire.accessIoNet
import net.dblsaiko.retrocomputers.common.cpu.Processor
import net.dblsaiko.retrocomputers.common.cpu.ProcessorHost
import net.dblsaiko.retrocomputers.common.util.unsigned
import net.minecraft.block.AbstractBlock
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.ActionResult
import net.minecraft.util.ActionResult.SUCCESS
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

class ComputerBlock(settings: AbstractBlock.Settings) : BaseBlock(settings), BlockEntityTicker<ComputerEntity> {

    override fun createBlockEntity(pos: BlockPos, state: BlockState) = ComputerEntity(pos, state)

    override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
        if (world.isClient) return SUCCESS

        val ent = world.getBlockEntity(pos) as? ComputerEntity ?: return SUCCESS
        ent.cpu.isRunning = !ent.cpu.isRunning

        return SUCCESS
    }

    override fun tick(world: World, pos: BlockPos, state: BlockState, be: ComputerEntity) {
        if (world.isClient) return

        be.cpu.timeout = false
        be.resetBusState()

        // FIXME run on other thread; use coroutines?

        val speed = 100000
        val cyclesPerTick = speed / 20
        var counter = 0

        for (i in 0 until cyclesPerTick) {
            if (!be.cpu.isRunning) break
            if (be.cpu.timeout) break
            be.cpu.next()
            counter++
        }

        if (counter > 0) be.markDirty()
    }

    override fun <T : BlockEntity> getTicker(world: World, state: BlockState, type: BlockEntityType<T>): BlockEntityTicker<T>? {
        return if (type == RetroComputers.blockEntityTypes.computer) {
            @Suppress("UNCHECKED_CAST")
            this as BlockEntityTicker<T>
        } else {
            null
        }
    }
}

class ComputerEntity(pos: BlockPos, state: BlockState) : BaseBlockEntity(RetroComputers.blockEntityTypes.computer, pos, state), ProcessorHost {

    val mem = ByteArray(8192)
    val cpu = Processor(this) // TODO we don't need this on the client

    override var busId: Byte = 0

    override var targetBus: Byte = 0

    override val isBusConnected: Boolean
        get() = cachedBus != null

  private var cachedBus: Device? = null

  private var busFailed = false

  init {
    mem[0] = 2 // Disk Drive (bus id $02)
    mem[1] = 1 // Terminal (bus id $01)
  }

  override fun readData(at: Byte): Byte {
    if (!allowWrite) return 0
    return memRead((at.unsigned + writePos.unsigned).toShort())
  }

  override fun storeData(at: Byte, data: Byte) {
    if (!allowWrite) return
    memStore((at.unsigned + writePos.unsigned).toShort(), data)
  }

  override fun bus(): Device? {
    cachedBus?.also { return it }
    val world = getWorld() ?: return null
    val net = accessIoNet(world, getPos(), MachinePartExt) ?: return null
    return net.device(targetBus).also { cachedBus = it }
  }

  override fun resetBusState() {
    busFailed = false
    cachedBus = null
  }

  override var allowWrite: Boolean = true

  override var writePos: Short = 0

  override fun memRead(at: Short): Byte {
    val addr = at.unsigned
    val memBank = addr / 8192
    val localAddr = addr % 8192
    return if (memBank == 0) {
      mem[localAddr]
    } else {
      // TODO: implement backplanes
      0
    }
  }

  override fun memStore(at: Short, data: Byte) {
      val addr = at.unsigned
      val memBank = addr / 8192
      val localAddr = addr % 8192
      return if (memBank == 0) {
          mem[localAddr] = data
      } else {
          // TODO: implement backplanes
      }
  }

    override fun writeNbt(tag: NbtCompound): NbtCompound {
        tag.put("cpu", cpu.toTag(NbtCompound()))
        tag.putByteArray("mem", mem)
        tag.putByte("target_bus", targetBus)
        return super.writeNbt(tag)
    }

    override fun readNbt(tag: NbtCompound) {
        super.readNbt(tag)
        cpu.fromTag(tag.getCompound("cpu"))
        tag.getByteArray("mem").copyInto(mem)
        targetBus = tag.getByte("target_bus")
    }

}
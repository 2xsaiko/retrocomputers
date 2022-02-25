package net.dblsaiko.retrocomputers.common.block

import net.dblsaiko.hctm.common.util.ext.makeStack
import net.dblsaiko.retrocomputers.RetroComputers
import net.dblsaiko.retrocomputers.common.item.ext.ItemDisk
import net.dblsaiko.retrocomputers.common.util.shr
import net.dblsaiko.retrocomputers.common.util.unsigned
import net.minecraft.block.AbstractBlock
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.StateManager.Builder
import net.minecraft.state.property.BooleanProperty
import net.minecraft.util.ActionResult
import net.minecraft.util.ActionResult.FAIL
import net.minecraft.util.ActionResult.PASS
import net.minecraft.util.ActionResult.SUCCESS
import net.minecraft.util.Hand
import net.minecraft.util.ItemScatterer
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import kotlin.experimental.and
import kotlin.experimental.or
import kotlin.math.min

class DiskDriveBlock(settings: AbstractBlock.Settings) : BaseBlock(settings), BlockEntityTicker<DiskDriveEntity> {

    init {
        defaultState = defaultState.with(DiskDriveProperties.HAS_DISK, false)
    }

    override fun onUse(state: BlockState, world: World, pos: BlockPos, player: PlayerEntity, hand: Hand, hit: BlockHitResult): ActionResult {
        val ent = world.getBlockEntity(pos) as? DiskDriveEntity ?: return FAIL
        return if (ent.ejectDisk() || ent.insertDisk(player.getStackInHand(hand))) SUCCESS else PASS
    }

    override fun tick(world: World, pos: BlockPos, state: BlockState, be: DiskDriveEntity) {
        val world = world as? ServerWorld ?: return

        var error = false
        val disk = be.disk()

        val command = be.command.unsigned
        val sector = be.sector.unsigned

        if (command in 1..5 && disk == null) {
            error = true
        } else if (command !in setOf(0, 255)) {
            be.markDirty()
            when (command) {
                1 -> {
                    disk!!
                    val arr = disk.getLabel(be.stack).toByteArray(Charsets.US_ASCII)
                    val len = min(arr.size, 126)
                    arr.copyInto(be.buffer, endIndex = len)
                    be.buffer[len] = 0
                }
                2 -> {
                    disk!!
                    val bytes = be.buffer.takeWhile { it != 0.toByte() }.toByteArray()
                    val str = String(bytes, Charsets.US_ASCII)
                    disk.setLabel(be.stack, str)
                }
                3 -> {
                    disk!!
                    val arr = disk.getUuid(be.stack).toString().toByteArray(Charsets.US_ASCII)
                    val len = min(arr.size, 126)
                    arr.copyInto(be.buffer, endIndex = len)
                    be.buffer[len] = 0
                }
                4 -> {
                    disk!!
                    val data = disk.sector(be.stack, world, sector)
                    if (sector >= 2048 || data == null || data.isEmpty()) {
                        error = true
                    } else data.use {
                        it.data.copyInto(be.buffer)
                    }
                }
                5 -> {
                    disk!!
                    val data = disk.sector(be.stack, world, sector)
                    if (sector >= 2048 || data == null) {
                        error = true
                    } else data.use {
                        be.buffer.copyInto(it.data)
                    }
                }
                6 -> {
                    be.buffer.fill(0)
                }
                else -> {
                    error = true
                }
            }
        }

        be.command = if (error) -1 else 0
    }

    override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos, newState: BlockState, boolean_1: Boolean) {
        if (state.block != newState.block) {
            (world.getBlockEntity(pos) as? DiskDriveEntity)?.ejectDisk(breakBlock = true)
        }

        super.onStateReplaced(state, world, pos, newState, boolean_1)
    }

    override fun appendProperties(b: Builder<Block, BlockState>) {
        super.appendProperties(b)
        b.add(DiskDriveProperties.HAS_DISK)
    }

    override fun createBlockEntity(pos: BlockPos, state: BlockState) = DiskDriveEntity(pos, state)

    override fun <T : BlockEntity> getTicker(world: World, state: BlockState, type: BlockEntityType<T>): BlockEntityTicker<T>? {
        return if (world is ServerWorld && !world.isClient && type == RetroComputers.blockEntityTypes.diskDrive) {
            @Suppress("UNCHECKED_CAST")
            this as BlockEntityTicker<T>
        } else {
            null
        }
    }

}

object DiskDriveProperties {
    val HAS_DISK = BooleanProperty.of("has_disk")
}

class DiskDriveEntity(pos: BlockPos, state: BlockState) : BaseBlockEntity(RetroComputers.blockEntityTypes.diskDrive, pos, state) {

    override var busId: Byte = 2

    internal var stack = Items.AIR.makeStack()

    internal val buffer = ByteArray(128)
    internal var sector: Short = 0
    internal var command: Byte = 0

    fun disk(): ItemDisk? = stack.item as? ItemDisk

    fun ejectDisk(breakBlock: Boolean = false): Boolean {
        val world = getWorld() ?: return false
        if (!hasDisk()) return false
        if (world.isClient) return true

        if (!breakBlock) {
            val dirVec = Vec3d.of(cachedState[BaseBlock.DIRECTION].vector)
            val pos = Vec3d.ofCenter(pos)
                .add(dirVec.multiply(0.75))
            val item = ItemEntity(world, pos.x, pos.y, pos.z, stack)
            item.velocity = dirVec.multiply(0.1)
            world.spawnEntity(item)
            world.setBlockState(getPos(), cachedState.with(DiskDriveProperties.HAS_DISK, false))
        } else {
            ItemScatterer.spawn(world, pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble(), stack)
        }

        stack = Items.AIR.makeStack()

        return true
    }

    private fun hasDisk(): Boolean {
        return if ((getWorld() ?: return false).isClient) cachedState[DiskDriveProperties.HAS_DISK] else !stack.isEmpty
    }

    fun insertDisk(stack: ItemStack): Boolean {
        val world = getWorld() ?: return false
        if (stack.isEmpty || stack.item !is ItemDisk) return false

        this.stack = stack.split(1)
        world.setBlockState(getPos(), cachedState.with(DiskDriveProperties.HAS_DISK, true))

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

    override fun writeNbt(tag: NbtCompound) {
        super.writeNbt(tag)
        tag.put("item", stack.writeNbt(NbtCompound()))
    }

    override fun readNbt(tag: NbtCompound) {
        super.readNbt(tag)
        stack = ItemStack.fromNbt(tag.getCompound("item"))
    }

}
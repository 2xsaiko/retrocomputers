package net.dblsaiko.retrocomputers.common.block

import net.dblsaiko.hctm.block.BlockBundledCableIo
import net.dblsaiko.hctm.common.wire.PartExt
import net.dblsaiko.retrocomputers.RetroComputers
import net.minecraft.block.AbstractBlock
import net.minecraft.block.BlockState
import net.minecraft.block.ShapeContext
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Direction.DOWN
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World

class RedstonePortBlock(settings: AbstractBlock.Settings) : BaseBlock(settings), BlockBundledCableIo {

    override fun createBlockEntity(pos: BlockPos, state: BlockState) = RedstonePortEntity(pos, state)

    override fun getPartsInBlock(world: World, pos: BlockPos, state: BlockState): Set<PartExt> {
        return super.getPartsInBlock(world, pos, state) // TODO
    }

    override fun canBundledConnectTo(state: BlockState, world: World, pos: BlockPos, side: Direction, edge: Direction): Boolean {
        return state[DIRECTION] == side && edge == DOWN
    }

    override fun getBundledOutput(state: BlockState, world: World, pos: BlockPos, side: Direction, edge: Direction): Short {
        return (world.getBlockEntity(pos) as? RedstonePortEntity)?.output?.toShort() ?: 0
    }

    override fun onBundledInputChange(data: Short, state: BlockState, world: World, pos: BlockPos, side: Direction, edge: Direction) {
        (world.getBlockEntity(pos) as? RedstonePortEntity)?.input = data.toUShort()
    }

    override fun getOutlineShape(state: BlockState, world: BlockView, pos: BlockPos, context: ShapeContext): VoxelShape {
        return BOX
    }

    companion object {
        val BOX = VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, 0.5, 1.0)
    }

}

class RedstonePortEntity(pos: BlockPos, state: BlockState) : BaseBlockEntity(RetroComputers.blockEntityTypes.redstonePort, pos, state) {

    override var busId: Byte = 3

    var output: UShort = 0u
    var input: UShort = 0u

    override fun readData(at: Byte): Byte = when (at.toUByte().toUInt()) {
        0x00u -> input.toByte()
        0x01u -> (input.toInt() shr 8).toByte()
        0x02u -> output.toByte()
        0x03u -> (output.toInt() shr 8).toByte()
        else -> 0
    }

    override fun storeData(at: Byte, data: Byte) {
        when (at.toUByte().toUInt()) {
            0x02u -> output = (output and 0xFF00u) or data.toUByte().toUShort()
            0x03u -> output = (output and 0x00FFu) or (data.toInt() shl 8).toUShort()
            else -> Unit
        }
        getWorld()?.updateNeighbor(getPos().offset(cachedState[BaseBlock.DIRECTION].opposite), cachedState.block, getPos())
    }

    override fun writeNbt(tag: NbtCompound): NbtCompound {
        tag.putShort("output", output.toShort())
        tag.putShort("input", input.toShort())
        return super.writeNbt(tag)
    }

    override fun readNbt(tag: NbtCompound) {
        super.readNbt(tag)
        output = tag.getShort("output").toUShort()
        input = tag.getShort("input").toUShort()
    }

}

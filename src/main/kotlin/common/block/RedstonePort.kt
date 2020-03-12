package net.dblsaiko.retrocomputers.common.block

import net.dblsaiko.hctm.common.api.BlockBundledCableIo
import net.dblsaiko.hctm.common.wire.PartExt
import net.dblsaiko.retrocomputers.common.init.BlockEntityTypes
import net.minecraft.block.BlockState
import net.minecraft.entity.EntityContext
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Direction.DOWN
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.World

class RedstonePortBlock : BaseBlock(), BlockBundledCableIo {

  override fun createBlockEntity(world: BlockView?): RedstonePortEntity {
    return RedstonePortEntity()
  }

  override fun getPartsInBlock(world: World, pos: BlockPos, state: BlockState): Set<PartExt> {
    return super.getPartsInBlock(world, pos, state) // TODO
  }

  override fun canBundledConnectTo(state: BlockState, world: World, pos: BlockPos, side: Direction, edge: Direction): Boolean {
    return state[Direction] == side && edge == DOWN
  }

  override fun getBundledOutput(state: BlockState, world: World, pos: BlockPos, side: Direction, edge: Direction): UShort {
    return (world.getBlockEntity(pos) as? RedstonePortEntity)?.output ?: 0u
  }

  override fun onBundledInputChange(data: UShort, state: BlockState, world: World, pos: BlockPos, side: Direction, edge: Direction) {
    (world.getBlockEntity(pos) as? RedstonePortEntity)?.input = data
    super.onBundledInputChange(data, state, world, pos, side, edge)
  }

  override fun getOutlineShape(state: BlockState, world: BlockView, pos: BlockPos, context: EntityContext): VoxelShape {
    return Box
  }

  companion object {
    val Box = VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, 0.5, 1.0)
  }

}

class RedstonePortEntity : BaseBlockEntity(BlockEntityTypes.RedstonePort) {

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

  override fun storeData(at: Byte, data: Byte) = when (at.toUByte().toUInt()) {
    0x02u -> output = (output and 0xFF00u) or data.toUByte().toUShort()
    0x03u -> output = (output and 0x00FFu) or (data.toInt() shl 8).toUShort()
    else -> Unit
  }

}

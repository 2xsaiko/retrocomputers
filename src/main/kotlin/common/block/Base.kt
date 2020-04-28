package net.dblsaiko.retrocomputers.common.block

import net.dblsaiko.hctm.common.wire.BlockPartProvider
import net.dblsaiko.hctm.common.wire.ConnectionDiscoverers
import net.dblsaiko.hctm.common.wire.ConnectionFilter
import net.dblsaiko.hctm.common.wire.FullBlockPartExtType
import net.dblsaiko.hctm.common.wire.NetNode
import net.dblsaiko.hctm.common.wire.NodeView
import net.dblsaiko.hctm.common.wire.PartExt
import net.dblsaiko.hctm.common.wire.find
import net.dblsaiko.hctm.common.wire.getWireNetworkState
import net.dblsaiko.retrocomputers.common.block.wire.PartIoCarrier
import net.dblsaiko.retrocomputers.common.block.wire.PartIoProvider
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable
import net.minecraft.block.AbstractBlock
import net.minecraft.block.Block
import net.minecraft.block.BlockRenderType.MODEL
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.block.HorizontalFacingBlock
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.item.ItemPlacementContext
import net.minecraft.nbt.ByteTag
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.server.world.ServerWorld
import net.minecraft.state.StateManager.Builder
import net.minecraft.util.BlockMirror
import net.minecraft.util.BlockRotation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Direction.NORTH
import net.minecraft.world.IWorld
import net.minecraft.world.World

abstract class BaseBlock(settings: AbstractBlock.Settings) : BlockWithEntity(settings), BlockPartProvider {

  init {
    this.defaultState = this.defaultState.with(Direction, NORTH)
  }

  override fun getPlacementState(ctx: ItemPlacementContext): BlockState? {
    return this.defaultState.with(Direction, ctx.playerFacing.opposite)
  }

  override fun getRenderType(p0: BlockState?) = MODEL

  override fun rotate(state: BlockState, rotation: BlockRotation): BlockState =
    state.with(Direction, rotation.rotate(state.get<Direction>(Direction) as Direction))

  override fun mirror(state: BlockState, mirror: BlockMirror): BlockState =
    state.rotate(mirror.getRotation(state.get<Direction>(Direction) as Direction))

  override fun appendProperties(b: Builder<Block, BlockState>) {
    b.add(Direction)
  }

  override fun prepare(state: BlockState, world: IWorld, pos: BlockPos, flags: Int) {
    if (!world.isClient && world is ServerWorld)
      world.getWireNetworkState().controller.onBlockChanged(world, pos, state)
  }

  override fun getPartsInBlock(world: World, pos: BlockPos, state: BlockState): Set<PartExt> {
    return setOf(MachinePartExt)
  }

  override fun createExtFromTag(tag: Tag): PartExt? {
    return MachinePartExt
  }

  companion object {
    val Direction = HorizontalFacingBlock.FACING
  }

}

object MachinePartExt : PartExt, FullBlockPartExtType, PartIoProvider {

  override fun tryConnect(self: NetNode, world: ServerWorld, pos: BlockPos, nv: NodeView): Set<NetNode> {
    return find(ConnectionDiscoverers.FullBlock, ConnectionFilter.forClass<PartIoCarrier>(), self, world, pos, nv)
  }

  override fun toTag(): Tag = ByteTag.of(0)

  private fun getBlockEnt(world: World, pos: BlockPos): BaseBlockEntity? {
    return world.getBlockEntity(pos) as? BaseBlockEntity
  }

  override fun isBusId(world: World, pos: BlockPos, busId: Byte): Boolean {
    return getBlockEnt(world, pos)?.busId == busId
  }

  override fun read(world: World, pos: BlockPos, at: Byte): Byte {
    return getBlockEnt(world, pos)?.readData(at) ?: 0
  }

  override fun store(world: World, pos: BlockPos, at: Byte, data: Byte) {
    getBlockEnt(world, pos)?.storeData(at, data)
  }

  override fun cached(world: World, pos: BlockPos): Cached? =
    getBlockEnt(world, pos)?.let(::Cached)

  override fun hashCode(): Int = super.hashCode()

  override fun equals(other: Any?): Boolean = super.equals(other)

  class Cached(val ent: BaseBlockEntity) : PartIoProvider.Cached {

    override fun read(at: Byte): Byte {
      return ent.readData(at)
    }

    override fun store(at: Byte, data: Byte) {
      ent.storeData(at, data)
    }

  }

}

abstract class BaseBlockEntity(type: BlockEntityType<*>) : BlockEntity(type), BlockEntityClientSerializable {

  abstract var busId: Byte

  abstract fun readData(at: Byte): Byte

  abstract fun storeData(at: Byte, data: Byte)

  override fun toClientTag(tag: CompoundTag): CompoundTag {
    tag.putByte("bus_id", busId)
    return tag
  }

  override fun fromClientTag(tag: CompoundTag) {
    busId = tag.getByte("bus_id")
  }

  override fun toTag(tag: CompoundTag): CompoundTag {
    tag.putByte("bus_id", busId)
    return super.toTag(tag)
  }

  override fun fromTag(state: BlockState, tag: CompoundTag) {
    super.fromTag(state, tag)
    busId = tag.getByte("bus_id")
  }

}
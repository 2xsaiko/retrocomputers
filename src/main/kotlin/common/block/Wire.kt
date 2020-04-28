package net.dblsaiko.retrocomputers.common.block

import net.dblsaiko.hctm.common.block.BaseWireBlockEntity
import net.dblsaiko.hctm.common.block.SingleBaseWireBlock
import net.dblsaiko.hctm.common.block.WireUtils
import net.dblsaiko.hctm.common.wire.ConnectionDiscoverers
import net.dblsaiko.hctm.common.wire.ConnectionFilter
import net.dblsaiko.hctm.common.wire.NetNode
import net.dblsaiko.hctm.common.wire.NodeView
import net.dblsaiko.hctm.common.wire.PartExt
import net.dblsaiko.hctm.common.wire.WirePartExtType
import net.dblsaiko.hctm.common.wire.find
import net.dblsaiko.retrocomputers.common.block.wire.PartIoCarrier
import net.dblsaiko.retrocomputers.common.init.BlockEntityTypes
import net.minecraft.block.AbstractBlock
import net.minecraft.nbt.ByteTag
import net.minecraft.nbt.Tag
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView

class RibbonCableBlock(settings: AbstractBlock.Settings) : SingleBaseWireBlock(settings, 1 / 16f) {

  override fun createPartExtFromSide(side: Direction) = RibbonCablePartExt(side)

  override fun createBlockEntity(view: BlockView) = BaseWireBlockEntity(BlockEntityTypes.RibbonCable)

}

data class RibbonCablePartExt(override val side: Direction) : PartExt, WirePartExtType, PartIoCarrier {
  override fun tryConnect(self: NetNode, world: ServerWorld, pos: BlockPos, nv: NodeView): Set<NetNode> {
    return find(ConnectionDiscoverers.Wire, ConnectionFilter.forClass<PartIoCarrier>(), self, world, pos, nv)
  }

  override fun onChanged(self: NetNode, world: ServerWorld, pos: BlockPos) {
    WireUtils.updateClient(world, pos)
  }

  override fun toTag(): Tag {
    return ByteTag.of(side.id.toByte())
  }
}
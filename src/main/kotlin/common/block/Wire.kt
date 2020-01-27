package therealfarfetchd.retrocomputers.common.block

import net.minecraft.block.Block
import net.minecraft.block.Material
import net.minecraft.nbt.ByteTag
import net.minecraft.nbt.Tag
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockView
import therealfarfetchd.hctm.common.block.BaseWireBlockEntity
import therealfarfetchd.hctm.common.block.SingleBaseWireBlock
import therealfarfetchd.hctm.common.block.WireUtils
import therealfarfetchd.hctm.common.wire.ConnectionDiscoverers
import therealfarfetchd.hctm.common.wire.ConnectionFilter
import therealfarfetchd.hctm.common.wire.NetNode
import therealfarfetchd.hctm.common.wire.NodeView
import therealfarfetchd.hctm.common.wire.PartExt
import therealfarfetchd.hctm.common.wire.WirePartExtType
import therealfarfetchd.hctm.common.wire.find
import therealfarfetchd.retrocomputers.common.block.wire.PartIoCarrier
import therealfarfetchd.retrocomputers.common.init.BlockEntityTypes

class RibbonCableBlock : SingleBaseWireBlock(Block.Settings.of(Material.STONE).noCollision().strength(0.25f, 0.25f), 1 / 16f) {

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
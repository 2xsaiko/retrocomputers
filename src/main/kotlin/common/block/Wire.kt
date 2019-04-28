package therealfarfetchd.retrocomputers.common.block

import net.minecraft.block.Block
import net.minecraft.block.Material
import net.minecraft.nbt.ByteTag
import net.minecraft.nbt.Tag
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import therealfarfetchd.hctm.common.wire.ConnectionHandlers
import therealfarfetchd.hctm.common.wire.Constraints
import therealfarfetchd.hctm.common.wire.NetNode
import therealfarfetchd.hctm.common.wire.NodeView
import therealfarfetchd.hctm.common.wire.PartExt
import therealfarfetchd.hctm.common.wire.WirePartExtType
import therealfarfetchd.retrocomputers.common.block.wire.PartIoCarrier
import net.minecraft.block.Blocks as MCBlocks

class WireBlock : BaseWireBlock(Block.Settings.of(Material.STONE).noCollision().strength(0.25f, 0.25f), 1 / 16f) {

  override fun createPartExtFromSide(side: Direction) = RibbonCablePartExt(side)

}

data class RibbonCablePartExt(override val side: Direction) : PartExt, WirePartExtType, PartIoCarrier {
  override fun tryConnect(self: NetNode, world: ServerWorld, pos: BlockPos, nv: NodeView): Set<NetNode> {
    return ConnectionHandlers.Wire.tryConnect(self, world, pos, nv, Constraints(PartIoCarrier::class))
  }

  override fun toTag(): Tag {
    return ByteTag(side.id.toByte())
  }
}
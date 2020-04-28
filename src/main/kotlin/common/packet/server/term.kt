package net.dblsaiko.retrocomputers.common.packet.server

import net.dblsaiko.retrocomputers.common.block.TerminalEntity
import net.fabricmc.fabric.api.network.PacketContext
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.math.Vec3d

fun onKeyTypedTerminal(context: PacketContext, buffer: PacketByteBuf) {
  val player = context.player
  val pos = buffer.readBlockPos()
  val k = buffer.readByte()

  val dist = player.getCameraPosVec(1f).squaredDistanceTo(Vec3d.method_24953(pos))
  if (dist > 10 * 10) return

  context.taskQueue.execute {
    val te = player.world.getBlockEntity(pos) as? TerminalEntity ?: return@execute
    te.pushKey(k)
  }
}
package net.dblsaiko.retrocomputers.common.packet.server

import net.dblsaiko.retrocomputers.common.block.TerminalEntity
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.Vec3d

fun onKeyTypedTerminal(server: MinecraftServer, player: ServerPlayerEntity, handler: ServerPlayNetworkHandler, buf: PacketByteBuf, responseSender: PacketSender) {
    val pos = buf.readBlockPos()
    val k = buf.readByte()

    val dist = player.getCameraPosVec(1f).squaredDistanceTo(Vec3d.ofCenter(pos))
    if (dist > 10 * 10) return

    server.execute {
        val te = player.world.getBlockEntity(pos) as? TerminalEntity ?: return@execute
        te.pushKey(k)
    }
}
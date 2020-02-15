package net.dblsaiko.retrocomputers.common.init

import net.dblsaiko.retrocomputers.MOD_ID
import net.dblsaiko.retrocomputers.common.packet.server.onKeyTypedTerminal
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry
import net.minecraft.util.Identifier

object Packets {

  object Client {
  }

  object Server {
    val TERMINAL_KEY_TYPED = Identifier(MOD_ID, "terminal_key")
  }

  fun register() {
    ServerSidePacketRegistry.INSTANCE.register(Server.TERMINAL_KEY_TYPED, ::onKeyTypedTerminal)
  }

}
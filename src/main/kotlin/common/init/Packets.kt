package net.dblsaiko.retrocomputers.common.init

import net.dblsaiko.retrocomputers.ModID
import net.dblsaiko.retrocomputers.common.packet.server.onKeyTypedTerminal
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry
import net.minecraft.util.Identifier

object Packets {

  object Client {
  }

  object Server {
    val TerminalKeyTyped = Identifier(ModID, "terminal_key")
  }

  fun register() {
    ServerSidePacketRegistry.INSTANCE.register(Server.TerminalKeyTyped, ::onKeyTypedTerminal)
  }

}
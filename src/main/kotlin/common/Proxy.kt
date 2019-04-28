package therealfarfetchd.retrocomputers.common

import net.minecraft.client.MinecraftClient
import therealfarfetchd.retrocomputers.client.gui.TerminalScreen
import therealfarfetchd.retrocomputers.common.block.TerminalEntity

interface Proxy {

  fun openTerminalScreen(te: TerminalEntity)

}

class ClientProxy : Proxy {

  val mc = MinecraftClient.getInstance()

  override fun openTerminalScreen(te: TerminalEntity) {
    mc.openScreen(TerminalScreen(te))
  }

}

class ServerProxy : Proxy {

  override fun openTerminalScreen(te: TerminalEntity) {}

}
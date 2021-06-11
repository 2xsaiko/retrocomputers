package net.dblsaiko.retrocomputers.common

import net.dblsaiko.retrocomputers.client.gui.TerminalScreen
import net.dblsaiko.retrocomputers.common.block.TerminalEntity
import net.minecraft.client.MinecraftClient

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
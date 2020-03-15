package net.dblsaiko.retrocomputers

import net.dblsaiko.retrocomputers.common.ClientProxy
import net.dblsaiko.retrocomputers.common.Proxy
import net.dblsaiko.retrocomputers.common.ServerProxy
import net.dblsaiko.retrocomputers.common.init.BlockEntityTypes
import net.dblsaiko.retrocomputers.common.init.Blocks
import net.dblsaiko.retrocomputers.common.init.Items
import net.dblsaiko.retrocomputers.common.init.Packets
import net.dblsaiko.retrocomputers.common.init.Resources
import net.fabricmc.api.EnvType.CLIENT
import net.fabricmc.api.EnvType.SERVER
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader

const val ModID = "retrocomputers"

object RetroComputers : ModInitializer {

  lateinit var proxy: Proxy

  override fun onInitialize() {
    proxy = when (FabricLoader.getInstance().environmentType!!) {
      CLIENT -> ClientProxy()
      SERVER -> ServerProxy()
    }

    Blocks.register()
    BlockEntityTypes.register()
    Items.register()
    Packets.register()
    Resources.register()

  }

}
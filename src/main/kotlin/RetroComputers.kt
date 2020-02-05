package net.dblsaiko.retrocomputers

import net.dblsaiko.hctm.common.util.ext.makeStack
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
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.util.Identifier

const val ModID = "retrocomputers"

object RetroComputers : ModInitializer {

  lateinit var proxy: Proxy

  override fun onInitialize() {
    proxy = when (FabricLoader.getInstance().environmentType!!) {
      CLIENT -> ClientProxy()
      SERVER -> ServerProxy()
    }

    Blocks
    BlockEntityTypes
    Items
    Packets
    Resources

    FabricItemGroupBuilder.create(Identifier(ModID, "all"))
      .icon { Items.Computer.makeStack() }
      .appendItems {
        it += Items.Computer.makeStack()
        it += Items.Terminal.makeStack()
        it += Items.DiskDrive.makeStack()
        it += Items.RibbonCable.makeStack()
        it += Items.UserDisk.makeStack()
        it += Items.SysDisks.map { it.makeStack() }
      }.build()
  }

}
package therealfarfetchd.retrocomputers

import net.fabricmc.api.EnvType.CLIENT
import net.fabricmc.api.EnvType.SERVER
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.util.Identifier
import therealfarfetchd.retrocomputers.common.ClientProxy
import therealfarfetchd.retrocomputers.common.Proxy
import therealfarfetchd.retrocomputers.common.ServerProxy
import therealfarfetchd.retrocomputers.common.init.BlockEntityTypes
import therealfarfetchd.retrocomputers.common.init.Blocks
import therealfarfetchd.retrocomputers.common.init.Items
import therealfarfetchd.retrocomputers.common.init.Packets
import therealfarfetchd.retrocomputers.common.init.Resources
import therealfarfetchd.retrocomputers.common.util.ext.makeStack

const val ModID = "retrocomputers"

object RetroComputers : ModInitializer {

  lateinit var proxy: Proxy

  override fun onInitialize() {
    proxy = when (FabricLoader.getInstance().environmentType!!) {
      CLIENT -> ClientProxy()
      SERVER -> ServerProxy()
    }

    BlockEntityTypes
    Blocks
    Items
    Packets
    Resources

    FabricItemGroupBuilder.create(Identifier(ModID, "all"))
      .icon { Items.Computer.makeStack() }
      .appendItems {
        it += Items.Computer.makeStack()
        it += Items.Terminal.makeStack()
        it += Items.DiskDrive.makeStack()
        it += Items.Wire.makeStack()
        it += Items.UserDisk.makeStack()
        it += Items.SysDisks.map { it.makeStack() }
      }.build()
  }

}
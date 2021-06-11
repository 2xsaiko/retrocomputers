package net.dblsaiko.retrocomputers

import net.dblsaiko.retrocomputers.common.ClientProxy
import net.dblsaiko.retrocomputers.common.Proxy
import net.dblsaiko.retrocomputers.common.ServerProxy
import net.dblsaiko.retrocomputers.common.init.BlockEntityTypes
import net.dblsaiko.retrocomputers.common.init.Blocks
import net.dblsaiko.retrocomputers.common.init.ItemGroups
import net.dblsaiko.retrocomputers.common.init.Items
import net.dblsaiko.retrocomputers.common.init.Packets
import net.dblsaiko.retrocomputers.common.init.Resources
import net.fabricmc.api.EnvType.CLIENT
import net.fabricmc.api.EnvType.SERVER
import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import org.apache.logging.log4j.LogManager

const val MOD_ID = "retrocomputers"

object RetroComputers : ModInitializer {
    internal var logger = LogManager.getLogger(MOD_ID)

    lateinit var proxy: Proxy

    val blocks = Blocks()
    val blockEntityTypes = BlockEntityTypes(blocks)
    val itemGroups = ItemGroups()
    val items = Items(blocks, itemGroups)
    var resources: Resources? = null

    override fun onInitialize() {
        proxy = when (FabricLoader.getInstance().environmentType!!) {
            CLIENT -> ClientProxy()
            SERVER -> ServerProxy()
        }

        this.blocks.register()
        this.blockEntityTypes.register()
        this.items.register()
        Packets.register()
        Resources.register()
    }
}

package net.dblsaiko.retrocomputers.common.init

import net.dblsaiko.hctm.common.util.ext.makeStack
import net.dblsaiko.retrocomputers.MOD_ID
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.minecraft.item.ItemGroup
import net.minecraft.util.Identifier

object ItemGroups {

  val All: ItemGroup = FabricItemGroupBuilder.create(Identifier(MOD_ID, "all"))
    .icon { Items.COMPUTER.makeStack() }
    .build()

}
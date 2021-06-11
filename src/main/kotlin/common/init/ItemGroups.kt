package net.dblsaiko.retrocomputers.common.init

import net.dblsaiko.hctm.common.util.ext.makeStack
import net.dblsaiko.retrocomputers.MOD_ID
import net.dblsaiko.retrocomputers.RetroComputers
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder
import net.minecraft.item.ItemGroup
import net.minecraft.util.Identifier

class ItemGroups {
    val all: ItemGroup = FabricItemGroupBuilder.create(Identifier(MOD_ID, "all"))
        .icon { RetroComputers.items.computer.makeStack() }
        .build()
}
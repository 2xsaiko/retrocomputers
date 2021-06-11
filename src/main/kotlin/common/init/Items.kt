package net.dblsaiko.retrocomputers.common.init

import net.dblsaiko.hctm.common.block.BaseWireItem
import net.dblsaiko.hctm.common.util.flatten
import net.dblsaiko.hctm.init.ItemRegistry
import net.dblsaiko.hctm.init.RegistryObject
import net.dblsaiko.retrocomputers.MOD_ID
import net.dblsaiko.retrocomputers.common.item.ImageDiskItem
import net.dblsaiko.retrocomputers.common.item.UserDiskItem
import net.minecraft.item.Item
import net.minecraft.item.Item.Settings
import net.minecraft.util.Identifier

class Items(blocks: Blocks, private val itemGroups: ItemGroups) {
    private val reg = ItemRegistry(MOD_ID, Settings().group(itemGroups.all))

    val sysDiskObjects = listOf(
        "forth",
        "extforth",
        "minforth",
        "decompiler",
        "radio",
        "retinal",
        "sortron"
    ).map(::createDisk)

    val computer by this.reg.create("computer", blocks.computerObject)
    val terminal by this.reg.create("terminal", blocks.terminalObject)
    val diskDrive by this.reg.create("disk_drive", blocks.diskDriveObject)
    val redstonePort by this.reg.create("redstone_port", blocks.redstonePortObject)

    val ribbonCable by this.reg.createThen("ribbon_cable") { BaseWireItem(blocks.ribbonCable, Settings().group(itemGroups.all)) }

    val sysDisks by this.sysDiskObjects.flatten()

    val userDisk by this.reg.create("user_disk", UserDiskItem())

    private fun createDisk(path: String): RegistryObject<ImageDiskItem> {
        return this.reg.create("disk_$path", ImageDiskItem(Identifier(MOD_ID, path), Item.Settings().maxCount(1).group(itemGroups.all)))
    }

    internal fun register() {
        this.reg.register()
    }
}
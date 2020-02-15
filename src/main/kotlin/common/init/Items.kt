package net.dblsaiko.retrocomputers.common.init

import net.dblsaiko.hctm.common.block.BaseWireItem
import net.dblsaiko.hctm.common.util.delegatedNotNull
import net.dblsaiko.hctm.common.util.flatten
import net.dblsaiko.retrocomputers.MOD_ID
import net.dblsaiko.retrocomputers.common.item.ImageDiskItem
import net.dblsaiko.retrocomputers.common.item.UserDiskItem
import net.minecraft.block.Block
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.Item.Settings
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import kotlin.properties.ReadOnlyProperty

object Items {

  private val tasks = mutableListOf<() -> Unit>()

  val COMPUTER by create(Blocks.COMPUTER, "computer")
  val TERMINAL by create(Blocks.TERMINAL, "terminal")
  val DISK_DRIVE by create(Blocks.DISK_DRIVE, "disk_drive")
  val REDSTONE_PORT by create(Blocks.REDSTONE_PORT, "redstone_port")

  val RIBBON_CABLE by create(BaseWireItem(Blocks.RIBBON_CABLE, Settings().group(ItemGroups.All)), "ribbon_cable")

  val SYS_DISKS by listOf(
    "forth",
    "extforth",
    "minforth",
    "decompiler",
    "radio",
    "retinal",
    "sortron"
  ).map(::createDisk).flatten()

  val USER_DISK by create(UserDiskItem(), "user_disk")

  private fun <T : Block> create(block: T, name: String): ReadOnlyProperty<Items, BlockItem> {
    return create(BlockItem(block, Settings().group(ItemGroups.All)), name)
  }

  private fun <T : Item> create(item: T, name: String): ReadOnlyProperty<Items, T> {
    var regItem: T? = null
    tasks += { regItem = Registry.register(Registry.ITEM, Identifier(MOD_ID, name), item) }
    return delegatedNotNull { regItem }
  }

  private fun createDisk(path: String): ReadOnlyProperty<Items, ImageDiskItem> {
    return create(ImageDiskItem(Identifier(MOD_ID, path), Item.Settings().maxCount(1).group(ItemGroups.All)), "disk_$path")
  }

  internal fun register() {
    tasks.forEach { it() }
    tasks.clear()
  }

}
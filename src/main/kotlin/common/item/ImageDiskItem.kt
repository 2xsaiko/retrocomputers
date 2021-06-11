package net.dblsaiko.retrocomputers.common.item

import net.dblsaiko.retrocomputers.RetroComputers
import net.dblsaiko.retrocomputers.common.item.ext.ItemDisk
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import java.util.*

class ImageDiskItem(val image: Identifier, settings: Item.Settings) : Item(settings), ItemDisk {

  override fun getLabel(stack: ItemStack): String = "System Disk"

  override fun setLabel(stack: ItemStack, str: String) {}

  override fun getUuid(stack: ItemStack): UUID = UUID(0L, 0L)

  override fun sector(stack: ItemStack, world: ServerWorld, index: Int): Sector? {
    val disk = RetroComputers.resources?.disks?.get(image) ?: return null
    if (disk.size < (index + 1) * 128) return null
    val sector = disk.copyOfRange(index * 128, (index + 1) * 128)
    return Sector(sector)
  }

  class Sector(override val data: ByteArray) : ItemDisk.Sector {
    override fun close() {}

    override fun isEmpty() = false
  }

}
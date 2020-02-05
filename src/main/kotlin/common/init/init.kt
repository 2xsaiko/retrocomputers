package net.dblsaiko.retrocomputers.common.init

import net.dblsaiko.hctm.common.block.BaseWireBlockEntity
import net.dblsaiko.hctm.common.block.BaseWireItem
import net.dblsaiko.retrocomputers.ModID
import net.dblsaiko.retrocomputers.common.block.ComputerBlock
import net.dblsaiko.retrocomputers.common.block.ComputerEntity
import net.dblsaiko.retrocomputers.common.block.DiskDriveBlock
import net.dblsaiko.retrocomputers.common.block.DiskDriveEntity
import net.dblsaiko.retrocomputers.common.block.RibbonCableBlock
import net.dblsaiko.retrocomputers.common.block.TerminalBlock
import net.dblsaiko.retrocomputers.common.block.TerminalEntity
import net.dblsaiko.retrocomputers.common.item.ImageDiskItem
import net.dblsaiko.retrocomputers.common.item.UserDiskItem
import net.dblsaiko.retrocomputers.common.packet.server.onKeyTypedTerminal
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry
import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.Item.Settings
import net.minecraft.item.ItemGroup
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import java.util.function.Supplier

object Blocks {

  val Computer = create(ComputerBlock(), "computer")
  val Terminal = create(TerminalBlock(), "terminal")
  val DiskDrive = create(DiskDriveBlock(), "disk_drive")

  val RibbonCable = create(RibbonCableBlock(), "ribbon_cable")

  private fun <T : Block> create(block: T, name: String): T {
    return Registry.register(Registry.BLOCK, Identifier(ModID, name), block)
  }

}

object BlockEntityTypes {

  val Computer = create(::ComputerEntity, "computer", Blocks.Computer)
  val Terminal = create(::TerminalEntity, "terminal", Blocks.Terminal)
  val DiskDrive = create(::DiskDriveEntity, "disk_drive", Blocks.DiskDrive)

  val RibbonCable = create(::BaseWireBlockEntity, "ribbon_cable", Blocks.RibbonCable)

  private fun <T : BlockEntity> create(builder: () -> T, name: String, vararg blocks: Block): BlockEntityType<T> {
    return Registry.register(Registry.BLOCK_ENTITY_TYPE, Identifier(ModID, name), BlockEntityType.Builder.create(Supplier(builder), *blocks).build(null))
  }

  private fun <T : BlockEntity> create(builder: (BlockEntityType<T>) -> T, name: String, vararg blocks: Block): BlockEntityType<T> {
    var type: BlockEntityType<T>? = null
    val s = Supplier { builder(type!!) }
    type = BlockEntityType.Builder.create(s, *blocks).build(null)
    return Registry.register(Registry.BLOCK_ENTITY_TYPE, Identifier(ModID, name), type)
  }

}

object Items {

  val Computer = create(Blocks.Computer, "computer")
  val Terminal = create(Blocks.Terminal, "terminal")
  val DiskDrive = create(Blocks.DiskDrive, "disk_drive")

  val RibbonCable = create(BaseWireItem(Blocks.RibbonCable, Item.Settings()), "ribbon_cable")

  val SysDisks = listOf(
    "forth",
    "extforth",
    "minforth",
    "decompiler",
    "radio",
    "retinal",
    "sortron"
  ).map(::createDisk)

  val UserDisk = create(UserDiskItem(), "user_disk")

  private fun <T : Block> create(block: T, name: String): BlockItem {
    return create(BlockItem(block, Settings().group(ItemGroup.REDSTONE)), name)
  }

  private fun <T : Item> create(item: T, name: String): T {
    return Registry.register(Registry.ITEM, Identifier(ModID, name), item)
  }

  private fun createDisk(path: String): ImageDiskItem {
    return create(ImageDiskItem(Identifier(ModID, path)), "disk_$path")
  }

}

object Packets {

  object Client {
  }

  object Server {
    val TerminalKeyTyped = Identifier(ModID, "terminal_key")
  }

  init {
    ServerSidePacketRegistry.INSTANCE.register(Server.TerminalKeyTyped, ::onKeyTypedTerminal)
  }

}
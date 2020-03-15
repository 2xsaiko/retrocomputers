package net.dblsaiko.retrocomputers.common.init

import net.dblsaiko.hctm.common.block.BaseWireBlockEntity
import net.dblsaiko.hctm.common.util.delegatedNotNull
import net.dblsaiko.retrocomputers.ModID
import net.dblsaiko.retrocomputers.common.block.ComputerEntity
import net.dblsaiko.retrocomputers.common.block.DiskDriveEntity
import net.dblsaiko.retrocomputers.common.block.RedstonePortEntity
import net.dblsaiko.retrocomputers.common.block.TerminalEntity
import net.minecraft.block.Block
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityType
import net.minecraft.block.entity.BlockEntityType.Builder
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import java.util.function.Supplier
import kotlin.properties.ReadOnlyProperty

object BlockEntityTypes {

  private val tasks = mutableListOf<() -> Unit>()

  val Computer by create(::ComputerEntity, "computer", Blocks.Computer)
  val Terminal by create(::TerminalEntity, "terminal", Blocks.Terminal)
  val DiskDrive by create(::DiskDriveEntity, "disk_drive", Blocks.DiskDrive)
  val RedstonePort by create(::RedstonePortEntity, "redstone_port", Blocks.RedstonePort)

  val RibbonCable by create(::BaseWireBlockEntity, "ribbon_cable", Blocks.RibbonCable)

  private fun <T : BlockEntity> create(builder: () -> T, name: String, vararg blocks: Block): ReadOnlyProperty<BlockEntityTypes, BlockEntityType<T>> {
    return create(Builder.create(Supplier(builder), *blocks).build(null), name)
  }

  private fun <T : BlockEntity> create(builder: (BlockEntityType<T>) -> T, name: String, vararg blocks: Block): ReadOnlyProperty<BlockEntityTypes, BlockEntityType<T>> {
    var type: BlockEntityType<T>? = null
    val s = Supplier { builder(type!!) }
    type = Builder.create(s, *blocks).build(null)
    return create(type, name)
  }

  private fun <T : BlockEntity> create(type: BlockEntityType<T>, name: String): ReadOnlyProperty<BlockEntityTypes, BlockEntityType<T>> {
    var regType: BlockEntityType<T>? = null
    tasks += { regType = Registry.register(Registry.BLOCK_ENTITY_TYPE, Identifier(ModID, name), type) }
    return delegatedNotNull { regType }
  }

  internal fun register() {
    tasks.forEach { it() }
    tasks.clear()
  }

}
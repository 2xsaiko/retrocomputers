package net.dblsaiko.retrocomputers.common.init

import net.dblsaiko.hctm.common.util.delegatedNotNull
import net.dblsaiko.retrocomputers.MOD_ID
import net.dblsaiko.retrocomputers.common.block.ComputerBlock
import net.dblsaiko.retrocomputers.common.block.DiskDriveBlock
import net.dblsaiko.retrocomputers.common.block.RedstonePortBlock
import net.dblsaiko.retrocomputers.common.block.RibbonCableBlock
import net.dblsaiko.retrocomputers.common.block.TerminalBlock
import net.minecraft.block.AbstractBlock
import net.minecraft.block.Block
import net.minecraft.block.Material
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import kotlin.properties.ReadOnlyProperty

object Blocks {

  private val tasks = mutableListOf<() -> Unit>()

  val COMPUTER by create(ComputerBlock(AbstractBlock.Settings.of(Material.METAL)), "computer")
  val TERMINAL by create(TerminalBlock(AbstractBlock.Settings.of(Material.METAL)), "terminal")
  val DISK_DRIVE by create(DiskDriveBlock(AbstractBlock.Settings.of(Material.METAL)), "disk_drive")
  val REDSTONE_PORT by create(RedstonePortBlock(AbstractBlock.Settings.of(Material.METAL)), "redstone_port")

  val RIBBON_CABLE by create(RibbonCableBlock(AbstractBlock.Settings.of(Material.STONE).noCollision().strength(0.25f, 0.25f)), "ribbon_cable")

  private fun <T : Block> create(block: T, name: String): ReadOnlyProperty<Blocks, T> {
    var regBlock: T? = null
    tasks += { regBlock = Registry.register(Registry.BLOCK, Identifier(MOD_ID, name), block) }
    return delegatedNotNull { regBlock }
  }

  internal fun register() {
    tasks.forEach { it() }
    tasks.clear()
  }

}

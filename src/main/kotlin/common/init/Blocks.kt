package net.dblsaiko.retrocomputers.common.init

import net.dblsaiko.hctm.init.BlockRegistry
import net.dblsaiko.retrocomputers.MOD_ID
import net.dblsaiko.retrocomputers.common.block.ComputerBlock
import net.dblsaiko.retrocomputers.common.block.DiskDriveBlock
import net.dblsaiko.retrocomputers.common.block.RedstonePortBlock
import net.dblsaiko.retrocomputers.common.block.RibbonCableBlock
import net.dblsaiko.retrocomputers.common.block.TerminalBlock
import net.minecraft.block.AbstractBlock
import net.minecraft.block.Material

class Blocks {
    private val reg = BlockRegistry(MOD_ID)

    val computerObject = this.reg.create("computer", ComputerBlock(AbstractBlock.Settings.of(Material.METAL)))
    val terminalObject = this.reg.create("terminal", TerminalBlock(AbstractBlock.Settings.of(Material.METAL)))
    val diskDriveObject = this.reg.create("disk_drive", DiskDriveBlock(AbstractBlock.Settings.of(Material.METAL)))
    val redstonePortObject = this.reg.create("redstone_port", RedstonePortBlock(AbstractBlock.Settings.of(Material.METAL)))

    val ribbonCableObject = this.reg.create("ribbon_cable", RibbonCableBlock(AbstractBlock.Settings.of(Material.STONE).noCollision().strength(0.25f, 0.25f)))

    val computer by this.computerObject
    val terminal by this.terminalObject
    val diskDrive by this.diskDriveObject
    val redstonePort by this.redstonePortObject
    val ribbonCable by this.ribbonCableObject

    fun register() {
        this.reg.register()
    }
}

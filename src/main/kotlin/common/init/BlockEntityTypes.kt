package net.dblsaiko.retrocomputers.common.init

import net.dblsaiko.hctm.common.block.BaseWireBlockEntity
import net.dblsaiko.hctm.init.BlockEntityTypeRegistry
import net.dblsaiko.retrocomputers.MOD_ID
import net.dblsaiko.retrocomputers.common.block.ComputerEntity
import net.dblsaiko.retrocomputers.common.block.DiskDriveEntity
import net.dblsaiko.retrocomputers.common.block.RedstonePortEntity
import net.dblsaiko.retrocomputers.common.block.TerminalEntity

class BlockEntityTypes(blocks: Blocks) {
    private val reg = BlockEntityTypeRegistry(MOD_ID)

    val computer by this.reg.create("computer", ::ComputerEntity, blocks.computerObject)
    val terminal by this.reg.create("terminal", ::TerminalEntity, blocks.terminalObject)
    val diskDrive by this.reg.create("disk_drive", ::DiskDriveEntity, blocks.diskDriveObject)
    val redstonePort by this.reg.create("redstone_port", ::RedstonePortEntity, blocks.redstonePortObject)

    val ribbonCable by this.reg.create("ribbon_cable", ::BaseWireBlockEntity, blocks.ribbonCableObject)

    fun register() {
        this.reg.register()
    }
}
package net.dblsaiko.retrocomputers.common.init

import net.dblsaiko.retrocomputers.MOD_ID
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceReloadListener.Synchronizer
import net.minecraft.resource.ResourceType.SERVER_DATA
import net.minecraft.util.Identifier
import net.minecraft.util.profiler.Profiler
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

object Resources {

  private lateinit var bootloader: ByteArray
  private lateinit var charset: ByteArray

  private lateinit var disks: Map<Identifier, ByteArray>

  fun bootloader() = bootloader.clone()

  fun charset() = charset.clone()

  fun disk(id: Identifier) = disks.getValue(id).clone()

  internal fun register() {
    ResourceManagerHelper.get(SERVER_DATA).registerReloadListener(object : IdentifiableResourceReloadListener {

      override fun reload(s: Synchronizer, rm: ResourceManager, profiler: Profiler, profiler1: Profiler, executor: Executor, executor1: Executor): CompletableFuture<Void> {
        return CompletableFuture.runAsync(Runnable {
          bootloader = rm.getResource(Identifier(MOD_ID, "bootldr.bin")).inputStream.use { it.readBytes() }
          charset = rm.getResource(Identifier(MOD_ID, "charset.bin")).inputStream.use { it.readBytes() }
          disks = Items.SYS_DISKS.associate { it.image to rm.getResource(Identifier(it.image.namespace, "disks/${it.image.path}.img")).inputStream.use { it.readBytes() } }
        }, executor).thenCompose<Void> { s.whenPrepared(null) }
      }

      override fun getFabricId(): Identifier = Identifier(MOD_ID, "data")
    })
  }

}
package net.dblsaiko.retrocomputers.common.init

import net.dblsaiko.retrocomputers.ModID
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

  private lateinit var Bootloader: ByteArray
  private lateinit var Charset: ByteArray

  private lateinit var Disks: Map<Identifier, ByteArray>

  fun bootloader() = Bootloader.clone()

  fun charset() = Charset.clone()

  fun disk(id: Identifier) = Disks.getValue(id).clone()

  init {
    ResourceManagerHelper.get(SERVER_DATA).registerReloadListener(object : IdentifiableResourceReloadListener {

      override fun reload(s: Synchronizer, rm: ResourceManager, profiler: Profiler, profiler1: Profiler, executor: Executor, executor1: Executor): CompletableFuture<Void> {
        return CompletableFuture.runAsync(Runnable {
          Bootloader = rm.getResource(Identifier(ModID, "bootldr.bin")).inputStream.use { it.readBytes() }
          Charset = rm.getResource(Identifier(ModID, "charset.bin")).inputStream.use { it.readBytes() }
          Disks = Items.SysDisks.associate { it.image to rm.getResource(Identifier(it.image.namespace, "disks/${it.image.path}.img")).inputStream.use { it.readBytes() } }
        }, executor).thenCompose<Void> { s.whenPrepared(null) }
      }

      override fun getFabricId(): Identifier = Identifier(ModID, "data")
    })
  }

}
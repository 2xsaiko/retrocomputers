package therealfarfetchd.retrocomputers.common.init

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceReloadListener.Helper
import net.minecraft.resource.ResourceType.DATA
import net.minecraft.util.Identifier
import net.minecraft.util.profiler.Profiler
import therealfarfetchd.retrocomputers.ModID
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
    ResourceManagerHelper.get(DATA).registerReloadListener(object : IdentifiableResourceReloadListener {

      override fun reload(helper: Helper, rm: ResourceManager, profiler: Profiler, profiler1: Profiler, executor: Executor, executor1: Executor): CompletableFuture<Void> {
        return CompletableFuture.runAsync(Runnable {
          Bootloader = rm.getResource(Identifier(ModID, "bootldr.bin")).inputStream.use { it.readBytes() }
          Charset = rm.getResource(Identifier(ModID, "charset.bin")).inputStream.use { it.readBytes() }
          Disks = Items.SysDisks.associate { it.image to rm.getResource(Identifier(it.image.namespace, "disks/${it.image.path}.img")).inputStream.use { it.readBytes() } }
        }, executor).thenCompose<Void> { helper.waitForAll(null) }
      }

      override fun getFabricId(): Identifier = Identifier(ModID, "data")
    })
  }

}
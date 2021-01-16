package net.dblsaiko.retrocomputers.client

import net.dblsaiko.hctm.client.render.model.UnbakedWireModel
import net.dblsaiko.retrocomputers.MOD_ID
import net.dblsaiko.retrocomputers.RetroComputers
import net.dblsaiko.retrocomputers.client.init.Shaders
import net.dblsaiko.retrocomputers.common.init.Blocks
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry
import net.fabricmc.fabric.api.client.model.ModelVariantProvider
import net.fabricmc.fabric.api.renderer.v1.RendererAccess
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import java.util.concurrent.ConcurrentHashMap

object RetroComputersClient : ClientModInitializer {

  override fun onInitializeClient() {
    Shaders.init()

    ModelLoadingRegistry.INSTANCE.registerVariantProvider {
      val r = RendererAccess.INSTANCE.renderer
      if (r != null) {
        val model = UnbakedWireModel(r, Identifier(MOD_ID, "block/ribbon_cable"), 0.5f, 0.0625f, 32.0f, ConcurrentHashMap())
        ModelVariantProvider { modelId, _ -> model.takeIf { Identifier(modelId.namespace, modelId.path) == Registry.BLOCK.getId(Blocks.RIBBON_CABLE) } }
      } else {
        RetroComputers.logger.error("Could not find Renderer API implementation. Rendering for Ribbon Cables will not be available.")
        ModelVariantProvider { _, _ -> null }
      }
    }
  }

}
package net.dblsaiko.retrocomputers.client

import net.dblsaiko.hctm.client.render.model.UnbakedWireModel
import net.dblsaiko.retrocomputers.ModID
import net.dblsaiko.retrocomputers.client.init.Shaders
import net.dblsaiko.retrocomputers.common.init.Blocks
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry
import net.fabricmc.fabric.api.client.model.ModelVariantProvider
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

object RetroComputersClient : ClientModInitializer {

  override fun onInitializeClient() {
    Shaders

    ModelLoadingRegistry.INSTANCE.registerVariantProvider {
      val model = UnbakedWireModel(Identifier(ModID, "block/ribbon_cable"), 0.5f, 0.0625f, 32.0f)
      ModelVariantProvider { modelId, _ -> model.takeIf { Identifier(modelId.namespace, modelId.path) == Registry.BLOCK.getId(Blocks.RibbonCable) } }
    }
  }

}
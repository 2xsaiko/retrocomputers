package therealfarfetchd.retrocomputers.client

import net.fabricmc.api.ClientModInitializer
import therealfarfetchd.retrocomputers.client.init.Shaders

object RetroComputersClient : ClientModInitializer {

  override fun onInitializeClient() {
    Shaders
  }

}
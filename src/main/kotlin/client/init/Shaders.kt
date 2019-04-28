package therealfarfetchd.retrocomputers.client.init

import com.mojang.blaze3d.platform.GLX
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener
import net.fabricmc.fabric.api.resource.ResourceManagerHelper
import net.minecraft.resource.ResourceManager
import net.minecraft.resource.ResourceReloadListener.Helper
import net.minecraft.resource.ResourceType.ASSETS
import net.minecraft.util.Identifier
import net.minecraft.util.profiler.Profiler
import org.lwjgl.opengl.GL11
import therealfarfetchd.retrocomputers.ModID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

object Shaders {

  private var screen = 0

  fun screen() = screen

  init {
    ResourceManagerHelper.get(ASSETS).registerReloadListener(object : IdentifiableResourceReloadListener {

      override fun reload(helper: Helper, rm: ResourceManager, profiler: Profiler, profiler1: Profiler, executor: Executor, executor1: Executor): CompletableFuture<Void> {
        return CompletableFuture.runAsync(Runnable {
          if (screen != 0) GLX.glDeleteProgram(screen)

          screen = loadShader(rm, "screen")
        }, executor1).thenCompose<Void> { helper.waitForAll(null) }
      }

      override fun getFabricId(): Identifier = Identifier(ModID, "shaders")

    })
  }

  private fun loadShader(rm: ResourceManager, id: String): Int {
    val vshs = rm.getResource(Identifier(ModID, "shaders/$id.vert")).use { it.inputStream.bufferedReader().readText() }
    val fshs = rm.getResource(Identifier(ModID, "shaders/$id.frag")).use { it.inputStream.bufferedReader().readText() }

    val vsh = GLX.glCreateShader(GLX.GL_VERTEX_SHADER)
    val fsh = GLX.glCreateShader(GLX.GL_FRAGMENT_SHADER)
    val prog = GLX.glCreateProgram()

    // No goto? I'll make my own.
    error@ do {
      GLX.glShaderSource(vsh, vshs)
      GLX.glShaderSource(fsh, fshs)

      GLX.glCompileShader(vsh)
      if (GLX.glGetShaderi(vsh, GLX.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
        // TODO use logger
        val log = GLX.glGetShaderInfoLog(vsh, 32768)
        println("Failed to compile vertex shader '$id'")
        for (line in log.lineSequence()) println(line)
        break@error
      }

      GLX.glCompileShader(fsh)
      if (GLX.glGetShaderi(fsh, GLX.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
        // TODO use logger
        val log = GLX.glGetShaderInfoLog(fsh, 32768)
        println("Failed to compile fragment shader '$id'")
        for (line in log.lineSequence()) println(line)
        break@error
      }

      GLX.glAttachShader(prog, vsh)
      GLX.glAttachShader(prog, fsh)
      GLX.glLinkProgram(prog)

      if (GLX.glGetProgrami(prog, GLX.GL_LINK_STATUS) == GL11.GL_FALSE) {
        // TODO use logger
        val log = GLX.glGetProgramInfoLog(prog, 32768)
        println("Failed to link program '$id'")
        for (line in log.lineSequence()) println(line)
        break@error
      }

      GLX.glDeleteShader(vsh)
      GLX.glDeleteShader(fsh)
      return prog
    } while (false)

    GLX.glDeleteShader(vsh)
    GLX.glDeleteShader(fsh)
    GLX.glDeleteProgram(prog)
    return 0
  }

}
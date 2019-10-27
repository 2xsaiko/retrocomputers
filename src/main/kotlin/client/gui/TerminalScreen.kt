package therealfarfetchd.retrocomputers.client.gui

import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import io.netty.buffer.Unpooled
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gl.GlFramebuffer
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.render.Tessellator
import net.minecraft.client.render.VertexFormats
import net.minecraft.client.texture.TextureUtil
import net.minecraft.text.TranslatableText
import net.minecraft.util.PacketByteBuf
import net.minecraft.util.math.Vec3d
import org.lwjgl.BufferUtils
import org.lwjgl.glfw.GLFW
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.GL_FLOAT
import org.lwjgl.opengl.GL11.GL_TRIANGLES
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30
import therealfarfetchd.retrocomputers.client.init.Shaders
import therealfarfetchd.retrocomputers.common.block.TerminalEntity
import therealfarfetchd.retrocomputers.common.init.Packets
import kotlin.experimental.xor
import kotlin.math.round

private val buf = BufferUtils.createByteBuffer(16384)

private val vbo = GL30.glGenBuffers()
private val vao = GL30.glGenVertexArrays()
private val screenTex = createTexture()
private val charsetTex = createTexture()

private const val scale = 8

class TerminalScreen(val te: TerminalEntity) : Screen(TranslatableText("block.retrocomputers.terminal")) {

  private var uCharset = 0
  private var uScreen = 0
  private var aXyz = 0
  private var aUv = 0

  private var fb: GlFramebuffer? = null

  override fun tick() {
    val minecraft = minecraft ?: return
    val dist = minecraft.player?.getCameraPosVec(1f)?.squaredDistanceTo(Vec3d(te.pos).add(0.5, 0.5, 0.5)) ?: Double.POSITIVE_INFINITY
    if (dist > 10 * 10) minecraft.openScreen(null)
  }

  override fun render(mouseX: Int, mouseY: Int, delta: Float) {
    renderBackground()

    val sh = Shaders.screen()
    val fb = fb ?: return
    val mc = minecraft ?: return

    fb.setTexFilter(if ((mc.window.scaleFactor.toInt() % 2) == 0) GL11.GL_NEAREST else GL11.GL_LINEAR)

    fb.beginWrite(true)
    RenderSystem.matrixMode(GL11.GL_PROJECTION)
    RenderSystem.pushMatrix()
    RenderSystem.loadIdentity()
    RenderSystem.ortho(0.0, 1.0, 1.0, 0.0, -1.0, 1.0)
    RenderSystem.matrixMode(GL11.GL_MODELVIEW)
    RenderSystem.pushMatrix()
    RenderSystem.loadIdentity()

    GL30.glUseProgram(sh)
    GL30.glBindVertexArray(vao)
    GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vbo)

    GL20.glEnableVertexAttribArray(aXyz)
    GL20.glEnableVertexAttribArray(aUv)

    RenderSystem.activeTexture(GL13.GL_TEXTURE0)
    RenderSystem.enableTexture()
    RenderSystem.bindTexture(screenTex)
    GL30.glUniform1i(uScreen, 0)

    buf.clear()
    buf.put(te.screen)

    if (te.cm == 1 || (te.cm == 2 && (System.currentTimeMillis() / 500) % 2 == 0L)) {
      val ci = te.cx + te.cy * 80
      buf.put(ci, te.screen[ci] xor 0x80.toByte())
    }

    buf.rewind()
    GlStateManager.texImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_R16I, 80, 60, 0, GL30.GL_RED_INTEGER, GL11.GL_UNSIGNED_BYTE, buf.asIntBuffer())

    RenderSystem.activeTexture(GL13.GL_TEXTURE2)
    RenderSystem.enableTexture()
    RenderSystem.bindTexture(charsetTex)
    GL30.glUniform1i(uCharset, 2)

    buf.clear()
    buf.put(te.charset)
    buf.rewind()
    GlStateManager.texImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_R16I, 8, 256, 0, GL30.GL_RED_INTEGER, GL11.GL_UNSIGNED_BYTE, buf.asIntBuffer())

    GL11.glDrawArrays(GL_TRIANGLES, 0, 6)

    GL20.glDisableVertexAttribArray(aXyz)
    GL20.glDisableVertexAttribArray(aUv)

    GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0)
    GL30.glBindVertexArray(0)
    GL30.glUseProgram(0)

    RenderSystem.bindTexture(0)
    RenderSystem.disableTexture()
    RenderSystem.activeTexture(GL13.GL_TEXTURE0)
    RenderSystem.bindTexture(0)

    mc.framebuffer.beginWrite(true)
    fb.beginRead()

    RenderSystem.matrixMode(GL11.GL_PROJECTION)
    RenderSystem.popMatrix()
    RenderSystem.matrixMode(GL11.GL_MODELVIEW)
    RenderSystem.popMatrix()

    RenderSystem.color4f(1f, 1f, 1f, 1f)

    val swidth = 8 * 80 * 0.5
    val sheight = 8 * 50 * 0.5
    val x1 = round(width / 2.0 - swidth / 2.0)
    val y1 = round(height / 2.0 - sheight / 2.0)

    val t = Tessellator.getInstance()
    val buf = t.bufferBuilder
    buf.begin(GL11.GL_QUADS, VertexFormats.POSITION_UV)
    buf.vertex(x1, y1, 0.0).texture(0f, 1f).next()
    buf.vertex(x1, y1 + sheight, 0.0).texture(0f, 0f).next()
    buf.vertex(x1 + swidth, y1 + sheight, 0.0).texture(1f, 0f).next()
    buf.vertex(x1 + swidth, y1, 0.0).texture(1f, 1f).next()
    t.draw()

  }

  override fun keyPressed(key: Int, scancode: Int, modifiers: Int): Boolean {
    if (super.keyPressed(key, scancode, modifiers)) return true

    val result: Byte? = when (key) {
      GLFW.GLFW_KEY_BACKSPACE -> 0x08
      GLFW.GLFW_KEY_ENTER -> 0x0D
      GLFW.GLFW_KEY_HOME -> 0x80
      GLFW.GLFW_KEY_END -> 0x81
      GLFW.GLFW_KEY_UP -> 0x82
      GLFW.GLFW_KEY_DOWN -> 0x83
      GLFW.GLFW_KEY_LEFT -> 0x84
      GLFW.GLFW_KEY_RIGHT -> 0x85
      else -> null
    }?.toByte()

    if (result != null) pushKey(result)

    return result != null
  }

  override fun charTyped(c: Char, modifiers: Int): Boolean {
    if (super.charTyped(c, modifiers)) return true

    val result: Byte? = when (c) {
      in '\u0001'..'\u007F' -> c
      else -> null
    }?.toByte()

    if (result != null) pushKey(result)

    return result != null
  }

  private fun pushKey(c: Byte) {
    val buffer = PacketByteBuf(Unpooled.buffer())
    buffer.writeBlockPos(te.pos)
    buffer.writeByte(c.toInt())
    ClientSidePacketRegistry.INSTANCE.sendToServer(Packets.Server.TerminalKeyTyped, buffer)
  }

  override fun init() {
    minecraft!!.keyboard.enableRepeatEvents(true)

    initDrawData()
    initFb()
  }

  private fun initDrawData() {
    val sh = Shaders.screen()

    GL30.glUseProgram(sh)
    GL30.glBindVertexArray(vao)
    GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vbo)

    uCharset = GL30.glGetUniformLocation(sh, "charset")
    uScreen = GL30.glGetUniformLocation(sh, "screen")

    aXyz = GL30.glGetAttribLocation(sh, "xyz")
    aUv = GL30.glGetAttribLocation(sh, "uv")

    GL20.glVertexAttribPointer(aXyz, 3, GL_FLOAT, false, 20, 0)
    GL20.glVertexAttribPointer(aUv, 2, GL_FLOAT, false, 20, 12)

    buf.clear()

    floatArrayOf(
      0f, 0f, 0f, 0f, 0f,
      1f, 1f, 0f, 1f, 1f,
      1f, 0f, 0f, 1f, 0f,

      0f, 0f, 0f, 0f, 0f,
      0f, 1f, 0f, 0f, 1f,
      1f, 1f, 0f, 1f, 1f
    ).forEach { buf.putFloat(it) }

    buf.rewind()

    GL30.glBufferData(GL30.GL_ARRAY_BUFFER, buf, GL15.GL_STATIC_DRAW)

    GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, 0)
    GL30.glBindVertexArray(0)
    GL30.glUseProgram(0)
  }

  private fun initFb() {
    fb?.delete()
    val scale = 4
    fb = GlFramebuffer(80 * 8 * scale, 50 * 8 * scale, false, MinecraftClient.IS_SYSTEM_MAC)
  }

  override fun removed() {
    minecraft!!.keyboard.enableRepeatEvents(false)
    fb?.delete()
    fb = null
  }

  override fun isPauseScreen() = false

}

private fun createTexture(): Int {
  val tex = TextureUtil.generateTextureId()
  RenderSystem.bindTexture(tex)
  RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST)
  RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST)
  RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT)
  RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT)
  RenderSystem.bindTexture(0)
  return tex
}
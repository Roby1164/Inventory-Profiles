/*
 * Inventory Profiles Next
 *
 *   Copyright (c) 2019-2020 jsnimda <7615255+jsnimda@users.noreply.github.com>
 *   Copyright (c) 2021-2022 Plamen K. Kosseff <p.kosseff@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.anti_ad.mc.common.vanilla.render

import org.anti_ad.mc.common.math2d.Rectangle
import org.anti_ad.mc.common.math2d.intersect
import org.anti_ad.mc.common.vanilla.alias.DstFactor
import org.anti_ad.mc.common.vanilla.alias.MatrixStack
import org.anti_ad.mc.common.vanilla.alias.RenderSystem
import org.anti_ad.mc.common.vanilla.alias.SrcFactor
import org.anti_ad.mc.common.vanilla.render.glue.__glue_rClearDepth
import org.anti_ad.mc.common.vanilla.render.glue.__glue_rStandardGlState
import org.anti_ad.mc.common.vanilla.render.glue.rFillRect
import org.anti_ad.mc.common.vanilla.render.glue.rScreenBounds
import org.lwjgl.opengl.GL11

fun initGLGlue() {
    __glue_rStandardGlState = ::rStandardGlState
    __glue_rClearDepth = ::rClearDepth
}

// ============
// api
// ============
// at Screen.render()
// do: rStandardGlState(); rClearDepth()
private fun rStandardGlState() { // reset to standard state (for screen rendering)

    rEnableBlend()
    //gDisableDiffuse()
    gEnableDepthTest()
    RenderSystem.depthMask(true)


}

// ============
// depth
// ============

private fun rClearDepth() {
    gEnableDepthTest()
    RenderSystem.depthMask(true)
    RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT,
                       false)
    rOverwriteDepth(rScreenBounds)
    depthBounds.clear() // added this
}

inline fun rDepthMask(bounds: Rectangle,
                      block: () -> Unit) {
    //rDrawOutline(bounds, -6710887)
    rCreateDepthMask(bounds)
    block()
    rRemoveDepthMask()
}

private val depthBounds = mutableListOf<Rectangle>()

//https://stackoverflow.com/questions/13742556/best-approach-to-draw-clipped-ui-elements-in-opengl
// can it be done without stencil?
// (maybe yes, if rectangle mask only)
fun rCreateDepthMask(bounds: Rectangle) {
    rStandardGlState() // added this
    if (depthBounds.isEmpty()) {
        rCreateDepthMaskNoCheck(bounds)
    } else {
        //rCreateDepthMaskNoCheck(depthBounds.last().intersect(bounds))
        rCreateDepthMaskNoCheck(depthBounds.last().intersect(bounds))
    }
}

private fun rCreateDepthMaskNoCheck(bounds: Rectangle) {
    depthBounds.add(bounds)
    // GL11.glMatrixMode(GL11.GL_PROJECTION)
    val a = RenderSystem.getModelViewStack()
    a.pushPose()
    a.translate(.0,
                .0,
                -400.0)
    rOverwriteDepth(bounds)
    //a.pop()
}

fun rRemoveDepthMask() {
    //rStandardGlState() // added this
    //gPopMatrix() this has already been done the 1.17 way
    val a = RenderSystem.getModelViewStack()
    a.popPose()
    rOverwriteDepth(depthBounds.removeLast())
}

private fun rOverwriteDepth(bounds: Rectangle) {
//  rEnableDepth()
    gDepthFunc(GL11.GL_ALWAYS)

    rFillRect(bounds,
              0)
    gDepthFunc(GL11.GL_LEQUAL)
}

fun rDisableDepth() { // todo see if same with disableDepthTest (?)
    gDepthFunc(GL11.GL_ALWAYS)
    RenderSystem.depthMask(false)
}

fun rEnableDepth() {
    RenderSystem.depthMask(true)
    gDepthFunc(GL11.GL_LEQUAL)
}

// ============
// matrix
// ============

var rMatrixStack = MatrixStack()


// ============
// internal
// ============
private fun rEnableBlend() {
    // ref: AbstractButtonWidget.renderButton()
    RenderSystem.enableBlend()
    RenderSystem.defaultBlendFunc()
    RenderSystem.blendFunc(SrcFactor.SRC_ALPHA,
                           DstFactor.ONE_MINUS_SRC_ALPHA)
    RenderSystem.setShaderColor(1f,
                                1f,
                                1f,
                                1f)

}

// ============
// GlStateManager
// ============


private fun gEnableDepthTest() = RenderSystem.enableDepthTest()
private fun gDepthFunc(value: Int) { // default = GL_LEQUAL = 515
    RenderSystem.depthFunc(value)
}

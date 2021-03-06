package imgui.internal.api

import glm_.f
import glm_.func.common.max
import glm_.glm
import glm_.vec2.Vec2
import glm_.vec4.Vec4
import imgui.*
import imgui.ImGui.calcTextSize
import imgui.ImGui.currentWindow
import imgui.ImGui.getColorU32
import imgui.ImGui.logText
import imgui.ImGui.style
import imgui.api.g
import imgui.classes.DrawList
import imgui.internal.classes.Rect
import imgui.internal.*
import imgui.internal.api.internal.Companion.alphaBlendColor
import kotlin.math.max

/** Render helpers
 *  AVOID USING OUTSIDE OF IMGUI.CPP! NOT FOR PUBLIC CONSUMPTION. THOSE FUNCTIONS ARE A MESS. THEIR SIGNATURE AND BEHAVIOR WILL CHANGE, THEY NEED TO BE REFACTORED INTO SOMETHING DECENT.
 *  NB: All position are in absolute pixels coordinates (we are never using window coordinates internally) */
internal interface renderHelpers {

    fun renderText(pos: Vec2, text: String, textEnd: Int = text.length, hideTextAfterHash: Boolean = true) {

        val window = g.currentWindow!!

        // Hide anything after a '##' string
        val textDisplayEnd = when {
            hideTextAfterHash -> findRenderedTextEnd(text, textEnd)
            textEnd == -1 -> text.length
            else -> textEnd
        }

        if (textDisplayEnd > 0) {
            window.drawList.addText(g.font, g.fontSize, pos, Col.Text.u32, text.toCharArray(), textDisplayEnd)
            if (g.logEnabled)
                logRenderedText(pos, text, textDisplayEnd)
        }
    }

    fun renderTextWrapped(pos: Vec2, text: String, textEnd_: Int?, wrapWidth: Float) {

        val window = g.currentWindow!!

        val textEnd = textEnd_ ?: text.length // FIXME-OPT

        if (textEnd > 0) {
            window.drawList.addText(g.font, g.fontSize, pos, Col.Text.u32, text.toCharArray(), textEnd, wrapWidth)
            if (g.logEnabled) logRenderedText(pos, text, textEnd)
        }
    }

    fun renderTextClipped(posMin: Vec2, posMax: Vec2, text: String, textEnd: Int = -1, textSizeIfKnown: Vec2? = null,
                          align: Vec2 = Vec2(), clipRect: Rect? = null) {
        // Hide anything after a '##' string
        val textDisplayEnd = findRenderedTextEnd(text, textEnd)
        if (textDisplayEnd == 0) return

        val window = g.currentWindow!!
        renderTextClippedEx(window.drawList, posMin, posMax, text, textDisplayEnd, textSizeIfKnown, align, clipRect)
        if (g.logEnabled)
            logRenderedText(posMax, text, textDisplayEnd)
    }

    /** Default clipRect uses (pos_min,pos_max)
     *  Handle clipping on CPU immediately (vs typically let the GPU clip the triangles that are overlapping the clipping
     *  rectangle edges)    */
    fun renderTextClippedEx(drawList: DrawList, posMin: Vec2, posMax: Vec2, text: String, textDisplayEnd: Int = -1,
                            textSizeIfKnown: Vec2? = null, align: Vec2 = Vec2(), clipRect: Rect? = null) {

        // Perform CPU side clipping for single clipped element to avoid using scissor state
        val pos = Vec2(posMin)
        val textSize = textSizeIfKnown ?: calcTextSize(text, textDisplayEnd, false, 0f)

        val clipMin = clipRect?.min ?: posMin
        val clipMax = clipRect?.max ?: posMax
        var needClipping = (pos.x + textSize.x >= clipMax.x) || (pos.y + textSize.y >= clipMax.y)
        clipRect?.let {
            // If we had no explicit clipping rectangle then pos==clipMin
            needClipping = needClipping || (pos.x < clipMin.x || pos.y < clipMin.y)
        }

        // Align whole block. We should defer that to the better rendering function when we'll have support for individual line alignment.
        if (align.x > 0f) pos.x = glm.max(pos.x, pos.x + (posMax.x - pos.x - textSize.x) * align.x)
        if (align.y > 0f) pos.y = glm.max(pos.y, pos.y + (posMax.y - pos.y - textSize.y) * align.y)

        // Render
        if (needClipping) {
            val fineClipRect = Vec4(clipMin.x, clipMin.y, clipMax.x, clipMax.y)
            drawList.addText(null, 0f, pos, Col.Text.u32, text.toCharArray(), textDisplayEnd, 0f, fineClipRect)
        } else
            drawList.addText(null, 0f, pos, Col.Text.u32, text.toCharArray(), textDisplayEnd, 0f, null)
    }

    /** Another overly complex function until we reorganize everything into a nice all-in-one helper.
     *  This is made more complex because we have dissociated the layout rectangle (pos_min..pos_max) which define _where_ the ellipsis is, from actual clipping of text and limit of the ellipsis display.
     *  This is because in the context of tabs we selectively hide part of the text when the Close Button appears, but we don't want the ellipsis to move. */
    fun renderTextEllipsis(drawList: DrawList, posMin: Vec2, posMax: Vec2, clipMaxX: Float, ellipsisMaxX: Float,
                           text: String, textEndFull_: Int, textSizeIfKnown: Vec2?) {

        val textEndFull = if (textEndFull_ == -1) findRenderedTextEnd(text) else textEndFull_
        val textSize = textSizeIfKnown ?: calcTextSize(text, textEndFull, false, 0f)

        //draw_list->AddLine(ImVec2(pos_max.x, pos_min.y - 4), ImVec2(pos_max.x, pos_max.y + 4), IM_COL32(0, 0, 255, 255));
        //draw_list->AddLine(ImVec2(ellipsis_max_x, pos_min.y-2), ImVec2(ellipsis_max_x, pos_max.y+2), IM_COL32(0, 255, 0, 255));
        //draw_list->AddLine(ImVec2(clip_max_x, pos_min.y), ImVec2(clip_max_x, pos_max.y), IM_COL32(255, 0, 0, 255));
        // FIXME: We could technically remove (last_glyph->AdvanceX - last_glyph->X1) from text_size.x here and save a few pixels.
        if (textSize.x > posMax.x - posMin.x) {
            /*
                Hello wo...
                |       |   |
                min   max   ellipsis_max
                         <-> this is generally some padding value
             */
            val font = drawList._data.font!!
            val fontSize = drawList._data.fontSize
            val textEndEllipsis = intArrayOf(-1)

            var ellipsisChar = font.ellipsisChar
            var ellipsisCharCount = 1
            if (ellipsisChar == '\uffff') {
                ellipsisChar = '.'
                ellipsisCharCount = 3
            }
            val glyph = font.findGlyph(ellipsisChar)!!

            var ellipsisGlyphWidth = glyph.x1       // Width of the glyph with no padding on either side
            var ellipsisTotalWidth = ellipsisGlyphWidth  // Full width of entire ellipsis

            if (ellipsisCharCount > 1) {
                // Full ellipsis size without free spacing after it.
                val spacingBetweenDots = 1f * (drawList._data.fontSize / font.fontSize)
                ellipsisGlyphWidth = glyph.x1 - glyph.x0 + spacingBetweenDots
                ellipsisTotalWidth = ellipsisGlyphWidth * ellipsisCharCount.f - spacingBetweenDots
            }

            // We can now claim the space between pos_max.x and ellipsis_max.x
            val textAvailWidth = ((max(posMax.x, ellipsisMaxX) - ellipsisTotalWidth) - posMin.x) max 1f
            var textSizeClippedX = font.calcTextSizeA(fontSize, textAvailWidth, 0f, text, textEndFull, textEndEllipsis).x
            if (0 == textEndEllipsis[0] && textEndEllipsis[0] < textEndFull) {
                // Always display at least 1 character if there's no room for character + ellipsis
                textEndEllipsis[0] = text.countUtf8BytesFromChar(textEndFull)
                textSizeClippedX = font.calcTextSizeA(fontSize, Float.MAX_VALUE, 0f, text, textEndEllipsis[0]).x
            }
            while (textEndEllipsis[0] > 0 && text[textEndEllipsis[0] - 1].isBlankA) {
                // Trim trailing space before ellipsis (FIXME: Supporting non-ascii blanks would be nice, for this we need a function to backtrack in UTF-8 text)
                textEndEllipsis[0]--
                textSizeClippedX -= font.calcTextSizeA(fontSize, Float.MAX_VALUE, 0f, text.substring(textEndEllipsis[0]), textEndEllipsis[0] + 1).x // Ascii blanks are always 1 byte
            }

            // Render text, render ellipsis
            renderTextClippedEx(drawList, posMin, Vec2(clipMaxX, posMax.y), text, textEndEllipsis[0], textSize, Vec2())
            var ellipsisX = posMin.x + textSizeClippedX
            if (ellipsisX + ellipsisTotalWidth <= ellipsisMaxX)
                for (i in 0 until ellipsisCharCount) {
                    font.renderChar(drawList, fontSize, Vec2(ellipsisX, posMin.y), Col.Text.u32, ellipsisChar)
                    ellipsisX += ellipsisGlyphWidth
                }
        } else
            renderTextClippedEx(drawList, posMin, Vec2(clipMaxX, posMax.y), text, textEndFull, textSize, Vec2())

        if (g.logEnabled)
            logRenderedText(posMin, text, textEndFull)
    }

    /** Render a rectangle shaped with optional rounding and borders    */
    fun renderFrame(pMin: Vec2, pMax: Vec2, fillCol: Int, border: Boolean = true, rounding: Float = 0f) {

        val window = g.currentWindow!!

        window.drawList.addRectFilled(pMin, pMax, fillCol, rounding)
        val borderSize = style.frameBorderSize
        if (border && borderSize > 0f) {
            window.drawList.addRect(pMin + 1, pMax + 1, Col.BorderShadow.u32, rounding, DrawCornerFlag.All.i, borderSize)
            window.drawList.addRect(pMin, pMax, Col.Border.u32, rounding, 0.inv(), borderSize)
        }
    }

    fun renderFrameBorder(pMin: Vec2, pMax: Vec2, rounding: Float = 0f) = with(g.currentWindow!!) {
        val borderSize = style.frameBorderSize
        if (borderSize > 0f) {
            drawList.addRect(pMin + 1, pMax + 1, Col.BorderShadow.u32, rounding, DrawCornerFlag.All.i, borderSize)
            drawList.addRect(pMin, pMax, Col.Border.u32, rounding, 0.inv(), borderSize)
        }
    }

    /** NB: This is rather brittle and will show artifact when rounding this enabled if rounded corners overlap multiple cells.
     *  Caller currently responsible for avoiding that.
     *  I spent a non reasonable amount of time trying to getting this right for ColorButton with rounding + anti-aliasing +
     *  ColorEditFlags.HalfAlphaPreview flag + various grid sizes and offsets, and eventually gave up...
     *  probably more reasonable to disable rounding alltogether.   */
    fun renderColorRectWithAlphaCheckerboard(pMin: Vec2, pMax: Vec2, col: Int, gridStep: Float, gridOff: Vec2, rounding: Float = 0f,
                                             roundingCornerFlags: Int = 0.inv()) {
        val window = currentWindow
        if (((col and COL32_A_MASK) ushr COL32_A_SHIFT) < 0xFF) {
            val colBg1 = getColorU32(alphaBlendColor(COL32(204, 204, 204, 255), col))
            val colBg2 = getColorU32(alphaBlendColor(COL32(128, 128, 128, 255), col))
            window.drawList.addRectFilled(pMin, pMax, colBg1, rounding, roundingCornerFlags)

            var yi = 0
            var y = pMin.y + gridOff.y
            while (y < pMax.y) {
                val y1 = glm.clamp(y, pMin.y, pMax.y)
                val y2 = glm.min(y + gridStep, pMax.y)
                if (y2 > y1) {
                    var x = pMin.x + gridOff.x + (yi and 1) * gridStep
                    while (x < pMax.x) {
                        val x1 = glm.clamp(x, pMin.x, pMax.x)
                        val x2 = glm.min(x + gridStep, pMax.x)
                        x += gridStep * 2f
                        if (x2 <= x1) continue
                        var roundingCornersFlagsCell = 0
                        if (y1 <= pMin.y) {
                            if (x1 <= pMin.x) roundingCornersFlagsCell = roundingCornersFlagsCell or DrawCornerFlag.TopLeft
                            if (x2 >= pMax.x) roundingCornersFlagsCell = roundingCornersFlagsCell or DrawCornerFlag.TopRight
                        }
                        if (y2 >= pMax.y) {
                            if (x1 <= pMin.x) roundingCornersFlagsCell = roundingCornersFlagsCell or DrawCornerFlag.BotLeft
                            if (x2 >= pMax.x) roundingCornersFlagsCell = roundingCornersFlagsCell or DrawCornerFlag.BotRight
                        }
                        roundingCornersFlagsCell = roundingCornersFlagsCell and roundingCornerFlags
                        val r = if (roundingCornersFlagsCell != 0) rounding else 0f
                        window.drawList.addRectFilled(Vec2(x1, y1), Vec2(x2, y2), colBg2, r, roundingCornersFlagsCell)
                    }
                }
                y += gridStep
                yi++
            }
        } else
            window.drawList.addRectFilled(pMin, pMax, col, rounding, roundingCornerFlags)
    }

    fun renderCheckMark(pos: Vec2, col: Int, sz_: Float) {

        val window = g.currentWindow!!

        val thickness = glm.max(sz_ / 5f, 1f)
        val sz = sz_ - thickness * 0.5f
        pos += thickness * 0.25f

        val third = sz / 3f
        val bx = pos.x + third
        val by = pos.y + sz - third * 0.5f
        window.drawList.pathLineTo(Vec2(bx - third, by - third))
        window.drawList.pathLineTo(Vec2(bx, by))

        window.drawList.pathLineTo(Vec2(bx + third * 2, by - third * 2))
        window.drawList.pathStroke(col, false, thickness)
    }

    /** Navigation highlight
     * @param flags: NavHighlightFlag  */
    fun renderNavHighlight(bb: Rect, id: ID, flags: NavHighlightFlags = NavHighlightFlag.TypeDefault.i) {

        if (id != g.navId) return
        if (g.navDisableHighlight && flags hasnt NavHighlightFlag.AlwaysDraw) return
        val window = currentWindow
        if (window.dc.navHideHighlightOneFrame) return

        val rounding = if (flags hasnt NavHighlightFlag.NoRounding) 0f else g.style.frameRounding
        val displayRect = Rect(bb)
        displayRect clipWith window.clipRect
        if (flags has NavHighlightFlag.TypeDefault) {
            val THICKNESS = 2f
            val DISTANCE = 3f + THICKNESS * 0.5f
            displayRect expand Vec2(DISTANCE)
            val fullyVisible = displayRect in window.clipRect
            if (!fullyVisible)
                window.drawList.pushClipRect(displayRect) // check order here down
            window.drawList.addRect(displayRect.min + (THICKNESS * 0.5f), displayRect.max - (THICKNESS * 0.5f),
                    Col.NavHighlight.u32, rounding, DrawCornerFlag.All.i, THICKNESS)
            if (!fullyVisible)
                window.drawList.popClipRect()
        }
        if (flags has NavHighlightFlag.TypeThin)
            window.drawList.addRect(displayRect.min, displayRect.max, Col.NavHighlight.u32, rounding, 0.inv(), 1f)
    }

    /** Find the optional ## from which we stop displaying text.    */
    fun findRenderedTextEnd(text: String, textEnd_: Int = -1): Int { // TODO function extension?
        var textDisplayEnd = 0
        val textEnd = if (textEnd_ == -1) text.length else textEnd_
        while (textDisplayEnd < textEnd && text[textDisplayEnd] != NUL && (text[textDisplayEnd + 0] != '#' || text[textDisplayEnd + 1] != '#'))
            textDisplayEnd++
        return textDisplayEnd
    }

    fun logRenderedText(refPos: Vec2?, text: String, textEnd_: Int = text.length) {
        val window = g.currentWindow!!

        val textEnd = if (textEnd_ == 0) findRenderedTextEnd(text) else textEnd_

        val logNewLine = refPos?.let { it.y > g.logLinePosY + 1 } ?: false

        refPos?.let { g.logLinePosY = it.y }
        if (logNewLine)
            g.logLineFirstItem = true

        var textRemaining = text
        if (g.logDepthRef > window.dc.treeDepth)
            g.logDepthRef = window.dc.treeDepth

        val treeDepth = window.dc.treeDepth - g.logDepthRef

        //TODO: make textEnd aware
        while (true) {
            /*  Split the string. Each new line (after a '\n') is followed by spacing corresponding to the current depth of our log entry.
                We don't add a trailing \n to allow a subsequent item on the same line to be captured.
            */
            val lineStart = textRemaining
            val lineEnd = if (lineStart.indexOf('\n') == -1) lineStart.length else lineStart.indexOf('\n')
            val isFirstLine = text.startsWith(lineStart)
            val isLastLine = text.endsWith(lineStart.substring(0, lineEnd))
            if (!isLastLine or lineStart.isNotEmpty()) {
                val charCount = lineStart.length
                when {
                    logNewLine or !isFirstLine -> logText("%s%s", "", lineStart)
                    g.logLineFirstItem -> logText("%s%s", "", lineStart)
                    else -> logText("%s", lineStart)
                }
            } else if (logNewLine) {
                // An empty "" string at a different Y position should output a carriage return.
                logText("\n")
                break
            }


            if (isLastLine)
                break
            textRemaining = textRemaining.substring(lineEnd + 1)
        }
    }

    // Render helpers (those functions don't access any ImGui state!)
    // these are all in the DrawList class
}
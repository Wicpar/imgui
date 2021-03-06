package imgui.internal.api

import gli_.hasnt
import glm_.*
import glm_.func.common.max
import glm_.vec2.Vec2
import imgui.*
import imgui.ImGui.buttonBehavior
import imgui.ImGui.calcTextSize
import imgui.ImGui.calcTypematicRepeatAmount
import imgui.ImGui.clearActiveId
import imgui.ImGui.currentWindow
import imgui.ImGui.dragBehaviorT
import imgui.ImGui.findRenderedTextEnd
import imgui.ImGui.focusWindow
import imgui.ImGui.hoveredId
import imgui.ImGui.indent
import imgui.ImGui.io
import imgui.ImGui.isItemHovered
import imgui.ImGui.isMouseClicked
import imgui.ImGui.itemAdd
import imgui.ImGui.itemHoverable
import imgui.ImGui.itemSize
import imgui.ImGui.logRenderedText
import imgui.ImGui.markItemEdited
import imgui.ImGui.mouseCursor
import imgui.ImGui.navMoveRequestCancel
import imgui.ImGui.renderFrame
import imgui.ImGui.renderNavHighlight
import imgui.ImGui.renderText
import imgui.ImGui.renderTextClipped
import imgui.ImGui.setActiveId
import imgui.ImGui.setFocusId
import imgui.ImGui.setItemAllowOverlap
import imgui.ImGui.sliderBehaviorT
import imgui.ImGui.style
import imgui.api.g
import imgui.internal.classes.Rect
import imgui.internal.*
import imgui.internal.ButtonFlag as Bf
import imgui.TreeNodeFlag as Tnf
import kotlin.math.max
import kotlin.reflect.KMutableProperty0
import kool.getValue
import kool.setValue

/** Widgets low-level behaviors */
internal interface widgetsLowLevelBehaviors {

    /** @return []pressed, hovered, held] */
    fun buttonBehavior(bb: Rect, id: ID, flag: Bf) = buttonBehavior(bb, id, flag.i)

    /** @return []pressed, hovered, held]
     *
     *  The ButtonBehavior() function is key to many interactions and used by many/most widgets.
     *  Because we handle so many cases (keyboard/gamepad navigation, drag and drop) and many specific behavior (via ImGuiButtonFlags_),
     *  this code is a little complex.
     *  By far the most common path is interacting with the Mouse using the default ImGuiButtonFlags_PressedOnClickRelease button behavior.
     *  See the series of events below and the corresponding state reported by dear imgui:
     *  ------------------------------------------------------------------------------------------------------------------------------------------------
     *  with PressedOnClickRelease:             return-value  IsItemHovered()  IsItemActive()  IsItemActivated()  IsItemDeactivated()  IsItemClicked()
     *    Frame N+0 (mouse is outside bb)        -             -                -               -                  -                    -
     *    Frame N+1 (mouse moves inside bb)      -             true             -               -                  -                    -
     *    Frame N+2 (mouse button is down)       -             true             true            true               -                    true
     *    Frame N+3 (mouse button is down)       -             true             true            -                  -                    -
     *    Frame N+4 (mouse moves outside bb)     -             -                true            -                  -                    -
     *    Frame N+5 (mouse moves inside bb)      -             true             true            -                  -                    -
     *    Frame N+6 (mouse button is released)   true          true             -               -                  true                 -
     *    Frame N+7 (mouse button is released)   -             true             -               -                  -                    -
     *    Frame N+8 (mouse moves outside bb)     -             -                -               -                  -                    -
     *  ------------------------------------------------------------------------------------------------------------------------------------------------
     *  with PressedOnClick:                    return-value  IsItemHovered()  IsItemActive()  IsItemActivated()  IsItemDeactivated()  IsItemClicked()
     *    Frame N+2 (mouse button is down)       true          true             true            true               -                    true
     *    Frame N+3 (mouse button is down)       -             true             true            -                  -                    -
     *    Frame N+6 (mouse button is released)   -             true             -               -                  true                 -
     *    Frame N+7 (mouse button is released)   -             true             -               -                  -                    -
     *  ------------------------------------------------------------------------------------------------------------------------------------------------
     *  with PressedOnRelease:                  return-value  IsItemHovered()  IsItemActive()  IsItemActivated()  IsItemDeactivated()  IsItemClicked()
     *    Frame N+2 (mouse button is down)       -             true             -               -                  -                    true
     *    Frame N+3 (mouse button is down)       -             true             -               -                  -                    -
     *    Frame N+6 (mouse button is released)   true          true             -               -                  -                    -
     *    Frame N+7 (mouse button is released)   -             true             -               -                  -                    -
     *  ------------------------------------------------------------------------------------------------------------------------------------------------
     *  with PressedOnDoubleClick:              return-value  IsItemHovered()  IsItemActive()  IsItemActivated()  IsItemDeactivated()  IsItemClicked()
     *    Frame N+0 (mouse button is down)       -             true             -               -                  -                    true
     *    Frame N+1 (mouse button is down)       -             true             -               -                  -                    -
     *    Frame N+2 (mouse button is released)   -             true             -               -                  -                    -
     *    Frame N+3 (mouse button is released)   -             true             -               -                  -                    -
     *    Frame N+4 (mouse button is down)       true          true             true            true               -                    true
     *    Frame N+5 (mouse button is down)       -             true             true            -                  -                    -
     *    Frame N+6 (mouse button is released)   -             true             -               -                  true                 -
     *    Frame N+7 (mouse button is released)   -             true             -               -                  -                    -
     *  ------------------------------------------------------------------------------------------------------------------------------------------------
     *  Note that some combinations are supported,
     *  - PressedOnDragDropHold can generally be associated with any flag.
     *  - PressedOnDoubleClick can be associated by PressedOnClickRelease/PressedOnRelease, in which case the second release event won't be reported.
     *  ------------------------------------------------------------------------------------------------------------------------------------------------
     *  The behavior of the return-value changes when ImGuiButtonFlags_Repeat is set:
     *                                          Repeat+                  Repeat+           Repeat+             Repeat+
     *                                          PressedOnClickRelease    PressedOnClick    PressedOnRelease    PressedOnDoubleClick
     *  -------------------------------------------------------------------------------------------------------------------------------------------------
     *    Frame N+0 (mouse button is down)       -                        true              -                   true
     *    ...                                    -                        -                 -                   -
     *    Frame N + RepeatDelay                  true                     true              -                   true
     *    ...                                    -                        -                 -                   -
     *    Frame N + RepeatDelay + RepeatRate*N   true                     true              -                   true
     *  -------------------------------------------------------------------------------------------------------------------------------------------------   */
    fun buttonBehavior(bb: Rect, id: ID, flags_: ButtonFlags = 0): BooleanArray {

        val window = currentWindow
        var flags = flags_

        if (flags has Bf.Disabled) {
            if (g.activeId == id) clearActiveId()
            return BooleanArray(3)
        }

        // Default behavior requires click+release on same spot
        if (flags hasnt (Bf.PressedOnClickRelease or Bf.PressedOnClick or Bf.PressedOnRelease or Bf.PressedOnDoubleClick))
            flags = flags or Bf.PressedOnClickRelease

        val backupHoveredWindow = g.hoveredWindow
        val flattenHoveredChildren = flags has Bf.FlattenChildren && g.hoveredRootWindow === window
        if (flattenHoveredChildren)
            g.hoveredWindow = window

        if (IMGUI_ENABLE_TEST_ENGINE && id != 0 && window.dc.lastItemId != id)
            ImGuiTestEngineHook_ItemAdd(bb, id)

        var pressed = false
        var hovered = itemHoverable(bb, id)

        // Drag source doesn't report as hovered
        if (hovered && g.dragDropActive && g.dragDropPayload.sourceId == id && g.dragDropSourceFlags hasnt DragDropFlag.SourceNoDisableHover)
            hovered = false

        // Special mode for Drag and Drop where holding button pressed for a long time while dragging another item triggers the button
        if (g.dragDropActive && flags has Bf.PressedOnDragDropHold && g.dragDropSourceFlags hasnt DragDropFlag.SourceNoHoldToOpenOthers)
            if (isItemHovered(HoveredFlag.AllowWhenBlockedByActiveItem)) {
                hovered = true
                hoveredId = id
                if (calcTypematicRepeatAmount(g.hoveredIdTimer + 0.0001f - io.deltaTime, g.hoveredIdTimer + 0.0001f - io.deltaTime, 0.7f, 0f) != 0) {
                    pressed = true
                    focusWindow(window)
                }
            }

        if (flattenHoveredChildren)
            g.hoveredWindow = backupHoveredWindow

        /*  AllowOverlap mode (rarely used) requires previous frame hoveredId to be null or to match. This allows using
            patterns where a later submitted widget overlaps a previous one.         */
        if (hovered && flags has Bf.AllowItemOverlap && g.hoveredIdPreviousFrame != id && g.hoveredIdPreviousFrame != 0)
            hovered = false

        // Mouse
        if (hovered) {
            if (flags hasnt Bf.NoKeyModifiers || (!io.keyCtrl && !io.keyShift && !io.keyAlt)) {

                if (flags has Bf.PressedOnClickRelease && io.mouseClicked[0]) {
                    setActiveId(id, window)
                    if (flags hasnt Bf.NoNavFocus)
                        setFocusId(id, window)
                    focusWindow(window)
                }
                if ((flags has Bf.PressedOnClick && io.mouseClicked[0]) || (flags has Bf.PressedOnDoubleClick && io.mouseDoubleClicked[0])) {
                    pressed = true
                    if (flags has Bf.NoHoldingActiveID)
                        clearActiveId()
                    else
                        setActiveId(id, window) // Hold on ID
                    focusWindow(window)
                }
                if (flags has Bf.PressedOnRelease && io.mouseReleased[0]) {
                    // Repeat mode trumps <on release>
                    if (!(flags has Bf.Repeat && io.mouseDownDurationPrev[0] >= io.keyRepeatDelay))
                        pressed = true
                    clearActiveId()
                }

                /*  'Repeat' mode acts when held regardless of _PressedOn flags (see table above).
                Relies on repeat logic of IsMouseClicked() but we may as well do it ourselves if we end up exposing
                finer RepeatDelay/RepeatRate settings.  */
                if (flags has Bf.Repeat && g.activeId == id && io.mouseDownDuration[0] > 0f && isMouseClicked(MouseButton.Left, true))
                    pressed = true
            }

            if (pressed)
                g.navDisableHighlight = true
        }

        /*  Gamepad/Keyboard navigation
            We report navigated item as hovered but we don't set g.HoveredId to not interfere with mouse.         */
        if (g.navId == id && !g.navDisableHighlight && g.navDisableMouseHover && (g.activeId == 0 || g.activeId == id || g.activeId == window.moveId))
            if (flags hasnt Bf.NoHoveredOnNav)
                hovered = true

        if (g.navActivateDownId == id) {
            val navActivatedByCode = g.navActivateId == id
            val navActivatedByInputs = NavInput.Activate.isTest(if (flags has Bf.Repeat) InputReadMode.Repeat else InputReadMode.Pressed)
            if (navActivatedByCode || navActivatedByInputs)
                pressed = true
            if (navActivatedByCode || navActivatedByInputs || g.activeId == id) {
                // Set active id so it can be queried by user via IsItemActive(), equivalent of holding the mouse button.
                g.navActivateId = id // This is so SetActiveId assign a Nav source
                setActiveId(id, window)
                if ((navActivatedByCode || navActivatedByInputs) && flags hasnt Bf.NoNavFocus)
                    setFocusId(id, window)
            }
        }
        var held = false
        if (g.activeId == id) {
            if (pressed)
                g.activeIdHasBeenPressedBefore = true
            if (g.activeIdSource == InputSource.Mouse) {
                if (g.activeIdIsJustActivated)
                    g.activeIdClickOffset = io.mousePos - bb.min
                if (io.mouseDown[0])
                    held = true
                else {
                    if (hovered && flags has Bf.PressedOnClickRelease && !g.dragDropActive) {
                        val isDoubleClickRelease = flags has Bf.PressedOnDoubleClick && io.mouseDownWasDoubleClick[0]
                        val isRepeatingAlready = flags has Bf.Repeat && io.mouseDownDurationPrev[0] >= io.keyRepeatDelay // Repeat mode trumps <on release>
                        if (!isDoubleClickRelease && !isRepeatingAlready)
                            pressed = true
                    }
                    clearActiveId()
                }
                if (flags hasnt Bf.NoNavFocus)
                    g.navDisableHighlight = true
            } else if (g.activeIdSource == InputSource.Nav)
                if (g.navActivateDownId != id)
                    clearActiveId()
        }
        return booleanArrayOf(pressed, hovered, held)
    }

    fun dragBehavior(id: ID, dataType: DataType, pV: FloatArray, ptr: Int, vSpeed: Float, pMin: Float?, pMax: Float?,
                     format: String, power: Float, flag: DragFlag): Boolean =
            withFloat(pV, ptr) { dragBehavior(id, DataType.Float, it, vSpeed, pMin, pMax, format, power, flag) }

    fun <N : Number> dragBehavior(id: ID, dataType: DataType, pV: KMutableProperty0<N>, vSpeed: Float, pMin: Number?,
                                  pMax: Number?, format: String, power: Float, flag: DragFlag): Boolean {

        if (g.activeId == id)
            if (g.activeIdSource == InputSource.Mouse && !io.mouseDown[0])
                clearActiveId()
            else if (g.activeIdSource == InputSource.Nav && g.navActivatePressedId == id && !g.activeIdIsJustActivated)
                clearActiveId()

        var v by pV

        return when {
            g.activeId == id -> when (dataType) {
                DataType.Byte -> {
                    _i = (v as Byte).i
                    val min = pMin?.let { it as Byte } ?: Byte.MIN_VALUE
                    val max = pMax?.let { it as Byte } ?: Byte.MAX_VALUE
                    dragBehaviorT(dataType, ::_i, vSpeed, min.i, max.i, format, power, flag)
                            .also { v = _i.b as N }
                }
//                DataType.Ubyte -> {
//                    _i = (v as Ubyte).v.i
//                    val min = vMin?.let { it as Ubyte } ?: Ub
//                    val max = vMax?.let { it as Ubyte } ?: Byte.MAX_VALUE
//                    dragBehaviorT(dataType, ::_i, vSpeed, min.i, max.i, format, power, flags)
//                            .also { v = _i.b as N }
//                }
                DataType.Short -> {
                    _i = (v as Short).i
                    val min = pMin?.let { it as Short } ?: Short.MIN_VALUE
                    val max = pMax?.let { it as Short } ?: Short.MAX_VALUE
                    dragBehaviorT(dataType, ::_i, vSpeed, min.i, max.i, format, power, flag)
                            .also { v = _i.s as N }
                }
//                is Ubyte -> {
//                    _i = v.i
//                    val min = vMin?.i ?: Ubyte.MIN_VALUE
//                    val max = vMax?.i ?: Ubyte.MAX_VALUE
//                    dragBehaviorT(dataType, ::_i, vSpeed, min.i, max.i, format, power, flags)
//                            .also { (v as Ubyte).v = _i.b }
//                }
                DataType.Int -> {
                    _i = v as Int
                    val min = pMin?.let { it as Int } ?: Int.MIN_VALUE
                    val max = pMax?.let { it as Int } ?: Int.MAX_VALUE
                    dragBehaviorT(dataType, ::_i, vSpeed, min.i, max.i, format, power, flag)
                            .also { v = _i as N }
                }
//                is Ubyte -> {
//                    _i = v.i
//                    val min = vMin?.i ?: Ubyte.MIN_VALUE
//                    val max = vMax?.i ?: Ubyte.MAX_VALUE
//                    dragBehaviorT(dataType, ::_i, vSpeed, min.i, max.i, format, power, flags)
//                            .also { (v as Ubyte).v = _i.b }
//                }
                DataType.Long -> {
                    _L = v as Long
                    val min = pMin?.let { it as Long } ?: Long.MIN_VALUE
                    val max = pMax?.let { it as Long } ?: Long.MAX_VALUE
                    dragBehaviorT(dataType, ::_L, vSpeed, min.L, max.L, format, power, flag)
                            .also { v = _L as N }
                }
//                is Ubyte -> {
//                    _i = v.i
//                    val min = vMin?.i ?: Ubyte.MIN_VALUE
//                    val max = vMax?.i ?: Ubyte.MAX_VALUE
//                    dragBehaviorT(dataType, ::_i, vSpeed, min.i, max.i, format, power, flags)
//                            .also { (v as Ubyte).v = _i.b }
//                }
                DataType.Float -> {
                    _f = v as Float
                    val min = pMin?.let { it as Float } ?: Float.MIN_VALUE
                    val max = pMax?.let { it as Float } ?: Float.MAX_VALUE
                    dragBehaviorT(dataType, ::_f, vSpeed, min.f, max.f, format, power, flag)
                            .also { v = _f as N }
                }
                DataType.Double -> {
                    _d = v as Double
                    val min = pMin?.let { it as Double } ?: Double.MIN_VALUE
                    val max = pMax?.let { it as Double } ?: Double.MAX_VALUE
                    dragBehaviorT(dataType, ::_d, vSpeed, min.d, max.d, format, power, flag)
                            .also { v = _d as N }
                }
                else -> error("Invalid")
            }
            else -> false
        }
    }

    /** For 32-bits and larger types, slider bounds are limited to half the natural type range.
     *  So e.g. an integer Slider between INT_MAX-10 and INT_MAX will fail, but an integer Slider between INT_MAX/2-10 and INT_MAX/2 will be ok.
     *  It would be possible to lift that limitation with some work but it doesn't seem to be worth it for sliders. */
    fun sliderBehavior(bb: Rect, id: ID, pV: FloatArray, pMin: Float, pMax: Float, format: String, power: Float,
                       flag: SliderFlag, outGrabBb: Rect) =
            sliderBehavior(bb, id, pV, 0, pMin, pMax, format, power, flag, outGrabBb)

    fun sliderBehavior(bb: Rect, id: ID, pV: FloatArray, ptr: Int, pMin: Float, pMax: Float, format: String,
                       power: Float, flag: SliderFlag, outGrabBb: Rect): Boolean =
            withFloat(pV, ptr) {
                sliderBehavior(bb, id, DataType.Float, it, pMin, pMax, format, power, flag, outGrabBb)
            }

//    fun <N> sliderBehavior(bb: Rect, id: ID,
//                           v: KMutableProperty0<N>,
//                           vMin: Float, vMax: Float,
//                           format: String, power: Float,
//                           flags: SliderFlags, outGrabBb: Rect): Boolean where N : Number, N : Comparable<N> =
//            sliderBehavior(bb, id, DataType.Float, v, vMin, vMax, format, power, flags, outGrabBb)

    fun <N> sliderBehavior(bb: Rect, id: ID,
                           dataType: DataType, pV: KMutableProperty0<N>,
                           pMin: N, pMax: N,
                           format: String, power: Float,
                           flag: SliderFlag, outGrabBb: Rect): Boolean where N : Number, N : Comparable<N> = when (dataType) {

        DataType.Byte -> {
            _i = (pV() as Byte).i
            sliderBehaviorT(bb, id, dataType, ::_i, pMin.i, pMax.i, format, power, flag, outGrabBb)
                    .also { pV.set(_i.b as N) }
        }
        DataType.Short -> {
            _i = (pV() as Short).i
            sliderBehaviorT(bb, id, dataType, ::_i, pMin.i, pMax.i, format, power, flag, outGrabBb)
                    .also { pV.set(_i.s as N) }
        }
        DataType.Int -> {
            assert(pMin as Int >= Int.MIN_VALUE / 2 && pMax as Int <= Int.MAX_VALUE / 2)
            sliderBehaviorT(bb, id, dataType, pV, pMin as N, pMax, format, power, flag, outGrabBb)
        }
        DataType.Long -> {
            assert(pMin as Long >= Long.MIN_VALUE / 2 && pMax as Long <= Long.MAX_VALUE / 2)
            sliderBehaviorT(bb, id, dataType, pV, pMin as N, pMax, format, power, flag, outGrabBb)
        }
        DataType.Float -> {
            assert(pMin as Float >= -Float.MAX_VALUE / 2f && pMax as Float <= Float.MAX_VALUE / 2f)
            sliderBehaviorT(bb, id, dataType, pV, pMin as N, pMax, format, power, flag, outGrabBb)
        }
        DataType.Double -> {
            assert(pMin as Double >= -Double.MAX_VALUE / 2f && pMax as Double <= Double.MAX_VALUE / 2f)
            sliderBehaviorT(bb, id, dataType, pV, pMin as N, pMax, format, power, flag, outGrabBb)
        }
        else -> throw Error()
    }

    /** Using 'hover_visibility_delay' allows us to hide the highlight and mouse cursor for a short time, which can be convenient to reduce visual noise. */
    fun splitterBehavior(bb: Rect, id: ID, axis: Axis, size1ptr: KMutableProperty0<Float>, size2ptr: KMutableProperty0<Float>,
                         minSize1: Float, minSize2: Float, hoverExtend: Float = 0f, hoverVisibilityDelay: Float): Boolean {

        var size1 by size1ptr
        var size2 by size2ptr
        val window = g.currentWindow!!

        val itemFlagsBackup = window.dc.itemFlags

        window.dc.itemFlags = window.dc.itemFlags or (ItemFlag.NoNav or ItemFlag.NoNavDefaultFocus)

        val itemAdd = itemAdd(bb, id)
        window.dc.itemFlags = itemFlagsBackup
        if (!itemAdd) return false

        val bbInteract = Rect(bb)
        bbInteract expand if (axis == Axis.Y) Vec2(0f, hoverExtend) else Vec2(hoverExtend, 0f)
        val (_, hovered, held) = buttonBehavior(bbInteract, id, Bf.FlattenChildren or Bf.AllowItemOverlap)
        if (g.activeId != id) setItemAllowOverlap()

        if (held || (g.hoveredId == id && g.hoveredIdPreviousFrame == id && g.hoveredIdTimer >= hoverVisibilityDelay))
            mouseCursor = if (axis == Axis.Y) MouseCursor.ResizeNS else MouseCursor.ResizeEW

        val bbRender = Rect(bb)
        if (held) {
            val mouseDelta2d = io.mousePos - g.activeIdClickOffset - bbInteract.min
            var mouseDelta = if (axis == Axis.Y) mouseDelta2d.y else mouseDelta2d.x

            // Minimum pane size
            val size1MaximumDelta = max(0f, size1 - minSize1)
            val size2MaximumDelta = max(0f, size2 - minSize2)
            if (mouseDelta < -size1MaximumDelta)
                mouseDelta = -size1MaximumDelta
            if (mouseDelta > size2MaximumDelta)
                mouseDelta = size2MaximumDelta


            // Apply resize
            if (mouseDelta != 0f) {
                if (mouseDelta < 0f)
                    assert(size1 + mouseDelta >= minSize1)
                else if (mouseDelta > 0f)
                    assert(size2 - mouseDelta >= minSize2)
                size1 = size1 + mouseDelta // cant += because of https://youtrack.jetbrains.com/issue/KT-14833
                size2 = size2 - mouseDelta
                bbRender translate if (axis == Axis.X) Vec2(mouseDelta, 0f) else Vec2(0f, mouseDelta)
                markItemEdited(id)
            }
            bbRender translate if (axis == Axis.X) Vec2(mouseDelta, 0f) else Vec2(0f, mouseDelta)

            markItemEdited(id)
        }

        // Render
        val col = when {
            held -> Col.SeparatorActive
            hovered && g.hoveredIdTimer >= hoverVisibilityDelay -> Col.SeparatorHovered
            else -> Col.Separator
        }
        window.drawList.addRectFilled(bbRender.min, bbRender.max, col.u32, 0f)

        return held
    }

    fun treeNodeBehavior(id: ID, flags: TreeNodeFlags, label: String, labelEnd_: Int = -1): Boolean {

        val window = currentWindow
        if (window.skipItems) return false

        val displayFrame = flags has Tnf.Framed
        val padding = when {
            displayFrame || flags has Tnf.FramePadding -> Vec2(style.framePadding)
            else -> Vec2(style.framePadding.x, window.dc.currLineTextBaseOffset min style.framePadding.y)
        }

        val labelEnd = if (labelEnd_ == -1) findRenderedTextEnd(label) else labelEnd_
        val labelSize = calcTextSize(label, labelEnd, false)

        // We vertically grow up to current line height up the typical widget height.
        val frameHeight = glm.max(glm.min(window.dc.currLineSize.y, g.fontSize + style.framePadding.y * 2), labelSize.y + padding.y * 2)
        val frameBb = Rect(
                x1 = if (flags has Tnf.SpanFullWidth) window.workRect.min.x else window.dc.cursorPos.x,
                y1 = window.dc.cursorPos.y,
                x2 = window.workRect.max.x,
                y2 = window.dc.cursorPos.y + frameHeight)
        if (displayFrame) {
            // Framed header expand a little outside the default padding, to the edge of InnerClipRect
            // (FIXME: May remove this at some point and make InnerClipRect align with WindowPadding.x instead of WindowPadding.x*0.5f)
            frameBb.min.x -= floor(window.windowPadding.x * 0.5f - 1f)
            frameBb.max.x += floor(window.windowPadding.x * 0.5f)
        }

        val textOffsetX = g.fontSize + padding.x * if (displayFrame) 3 else 2                   // Collapser arrow width + Spacing
        val textOffsetY = padding.y max window.dc.currLineTextBaseOffset                        // Latch before ItemSize changes it
        val textWidth = g.fontSize + if (labelSize.x > 0f) labelSize.x + padding.x * 2 else 0f  // Include collapser
        val textPos = Vec2(window.dc.cursorPos.x + textOffsetX, window.dc.cursorPos.y + textOffsetY)
        itemSize(Vec2(textWidth, frameHeight), padding.y)

        // For regular tree nodes, we arbitrary allow to click past 2 worth of ItemSpacing
        val interactBb = Rect(frameBb)
        if (!displayFrame && flags hasnt (Tnf.SpanAvailWidth or Tnf.SpanFullWidth))
            interactBb.max.x = frameBb.min.x + textWidth + style.itemSpacing.x * 2f

        /*  Store a flag for the current depth to tell if we will allow closing this node when navigating one of its child.
            For this purpose we essentially compare if g.NavIdIsAlive went from 0 to 1 between TreeNode() and TreePop().
            This is currently only support 32 level deep and we are fine with (1 << Depth) overflowing into a zero. */
        val isLeaf = flags has Tnf.Leaf
        var isOpen = treeNodeBehaviorIsOpen(id, flags)
        if (isOpen && !g.navIdIsAlive && flags has Tnf.NavLeftJumpsBackHere && flags hasnt Tnf.NoTreePushOnOpen)
            window.dc.treeMayJumpToParentOnPopMask = window.dc.treeMayJumpToParentOnPopMask or (1 shl window.dc.treeDepth)

        val itemAdd = itemAdd(interactBb, id)
        window.dc.lastItemStatusFlags = window.dc.lastItemStatusFlags or ItemStatusFlag.HasDisplayRect
        window.dc.lastItemDisplayRect put frameBb

        if (!itemAdd) {
            if (isOpen && flags hasnt Tnf.NoTreePushOnOpen)
                treePushOverrideID(id)
            ImGuiTestEngineHook_ItemInfo(window.dc.lastItemId, label, window.dc.itemFlags or (if (isLeaf) ItemStatusFlag.None else ItemStatusFlag.Openable) or if (isOpen) ItemStatusFlag.Opened else ItemStatusFlag.None)
            return isOpen
        }

        /*  Flags that affects opening behavior:
                - 0 (default) .................... single-click anywhere to open
                - OpenOnDoubleClick .............. double-click anywhere to open
                - OpenOnArrow .................... single-click on arrow to open
                - OpenOnDoubleClick|OpenOnArrow .. single-click on arrow or double-click anywhere to open   */
        var buttonFlags: ButtonFlags = Bf.None.i
        if (flags has Tnf.AllowItemOverlap)
            buttonFlags = buttonFlags or Bf.AllowItemOverlap
        if (flags has Tnf.OpenOnDoubleClick)
            buttonFlags = buttonFlags or Bf.PressedOnDoubleClick or (if (flags has Tnf.OpenOnArrow) Bf.PressedOnClickRelease else Bf.None)
        if (!isLeaf)
            buttonFlags = buttonFlags or Bf.PressedOnDragDropHold

        // We allow clicking on the arrow section with keyboard modifiers held, in order to easily
        // allow browsing a tree while preserving selection with code implementing multi-selection patterns.
        // When clicking on the rest of the tree node we always disallow keyboard modifiers.
        val hitPaddingX = style.touchExtraPadding.x
        val arrowHitX1 = (textPos.x - textOffsetX) - hitPaddingX
        val arrowHitX2 = (textPos.x - textOffsetX) + (g.fontSize + padding.x * 2f) + hitPaddingX
        if (window !== g.hoveredWindow || !(io.mousePos.x >= arrowHitX1 && io.mousePos.x < arrowHitX2))
            buttonFlags = buttonFlags or Bf.NoKeyModifiers

        val selected = flags has Tnf.Selected
        val wasSelected = selected

        val (pressed, hovered, held) = buttonBehavior(interactBb, id, buttonFlags)
        if (!isLeaf) {
            var toggled = false
            if (pressed) {
                if (flags hasnt (Tnf.OpenOnArrow or Tnf.OpenOnDoubleClick) || g.navActivateId == id)
                    toggled = true
                if (flags has Tnf.OpenOnArrow)
                    toggled = ((io.mousePos.x >= arrowHitX1 && io.mousePos.x < arrowHitX2) && !g.navDisableMouseHover) || toggled // Lightweight equivalent of IsMouseHoveringRect() since ButtonBehavior() already did the job
                if (flags has Tnf.OpenOnDoubleClick && io.mouseDoubleClicked[0])
                    toggled = true
                // When using Drag and Drop "hold to open" we keep the node highlighted after opening, but never close it again.
                if (g.dragDropActive && isOpen)
                    toggled = false
            }

            if (g.navId == id && g.navMoveRequest && g.navMoveDir == Dir.Left && isOpen) {
                toggled = true
                navMoveRequestCancel()
            }
            // If there's something upcoming on the line we may want to give it the priority?
            if (g.navId == id && g.navMoveRequest && g.navMoveDir == Dir.Right && !isOpen) {
                toggled = true
                navMoveRequestCancel()
            }
            if (toggled) {
                isOpen = !isOpen
                window.dc.stateStorage[id] = isOpen
                window.dc.lastItemStatusFlags = window.dc.lastItemStatusFlags or ItemStatusFlag.ToggledOpen
            }
        }
        if (flags has Tnf.AllowItemOverlap)
            setItemAllowOverlap()

        // In this branch, TreeNodeBehavior() cannot toggle the selection so this will never trigger.
        if (selected != wasSelected)
            window.dc.lastItemStatusFlags = window.dc.lastItemStatusFlags or ItemStatusFlag.ToggledSelection

        // Render
        val textCol = Col.Text.u32
        val navHighlightFlags: NavHighlightFlags = NavHighlightFlag.TypeThin.i
        if (displayFrame) {
            // Framed type
            val bgCol = if (held && hovered) Col.HeaderActive else if (hovered) Col.HeaderHovered else Col.Header
            renderFrame(frameBb.min, frameBb.max, bgCol.u32, true, style.frameRounding)
            renderNavHighlight(frameBb, id, navHighlightFlags)
            if (flags has Tnf.Bullet)
                window.drawList.renderBullet(Vec2(textPos.x - textOffsetX * 0.6f, textPos.y + g.fontSize * 0.5f), textCol)
            else if (!isLeaf)
                window.drawList.renderArrow(Vec2(textPos.x - textOffsetX + padding.x, textPos.y), textCol, if (isOpen) Dir.Down else Dir.Right, 1f)
            else // Leaf without bullet, left-adjusted text
                textPos.x -= textOffsetX
            if (flags has Tnf._ClipLabelForTrailingButton)
                frameBb.max.x -= g.fontSize + style.framePadding.x
            if (g.logEnabled) {
                /*  NB: '##' is normally used to hide text (as a library-wide feature), so we need to specify the text
                    range to make sure the ## aren't stripped out here.                 */
                logRenderedText(textPos, "\n##", 3)
                renderTextClipped(textPos, frameBb.max, label, labelEnd, labelSize)
                logRenderedText(textPos, "#", 2) // TODO check me
            } else
                renderTextClipped(textPos, frameBb.max, label, labelEnd, labelSize)
        } else {
            // Unframed typed for tree nodes
            if (hovered || selected) {
                val bgCol = if (held && hovered) Col.HeaderActive else if (hovered) Col.HeaderHovered else Col.Header
                renderFrame(frameBb.min, frameBb.max, bgCol.u32, false)
                renderNavHighlight(frameBb, id, navHighlightFlags)
            }
            if (flags has Tnf.Bullet)
                window.drawList.renderBullet(Vec2(textPos.x - textOffsetX * 0.5f, textPos.y + g.fontSize * 0.5f), textCol)
            else if (!isLeaf)
                window.drawList.renderArrow(Vec2(textPos.x - textOffsetX + padding.x, textPos.y + g.fontSize * 0.15f), textCol, if (isOpen) Dir.Down else Dir.Right, 0.7f)
            if (g.logEnabled)
                logRenderedText(textPos, ">")
            renderText(textPos, label, labelEnd, false)
        }

        if (isOpen && flags hasnt Tnf.NoTreePushOnOpen)
            treePushOverrideID(id)
        ImGuiTestEngineHook_ItemInfo(id, label, window.dc.itemFlags or (if (isLeaf) ItemStatusFlag.None else ItemStatusFlag.Openable) or if (isOpen) ItemStatusFlag.Opened else ItemStatusFlag.None)
        return isOpen
    }

    /** Consume previous SetNextItemOpen() data, if any. May return true when logging */
    fun treeNodeBehaviorIsOpen(id: ID, flags: TreeNodeFlags = Tnf.None.i): Boolean {

        if (flags has Tnf.Leaf) return true

        // We only write to the tree storage if the user clicks (or explicitly use the SetNextItemOpen function)
        val window = g.currentWindow!!
        val storage = window.dc.stateStorage

        var isOpen: Boolean
        if (g.nextItemData.flags has NextItemDataFlag.HasOpen) {
            if (g.nextItemData.openCond == Cond.Always) {
                isOpen = g.nextItemData.openVal
                storage[id] = isOpen
            } else {
                /*  We treat ImGuiSetCondition_Once and ImGuiSetCondition_FirstUseEver the same because tree node state
                    are not saved persistently.                 */
                val storedValue = storage.int(id, -1)
                if (storedValue == -1) {
                    isOpen = g.nextItemData.openVal
                    storage[id] = isOpen
                } else
                    isOpen = storedValue != 0
            }
        } else
            isOpen = storage.int(id, if (flags has Tnf.DefaultOpen) 1 else 0) != 0 // TODO rename back

        /*  When logging is enabled, we automatically expand tree nodes (but *NOT* collapsing headers.. seems like
            sensible behavior).
            NB- If we are above max depth we still allow manually opened nodes to be logged.    */
        if (g.logEnabled && flags hasnt Tnf.NoAutoOpenOnLog && (window.dc.treeDepth - g.logDepthRef) < g.logDepthToExpand)
            isOpen = true

        return isOpen
    }

    fun treePushOverrideID(id: ID) {
        val window = currentWindow
        indent()
        window.dc.treeDepth++
        window.idStack.push(id)
    }
}
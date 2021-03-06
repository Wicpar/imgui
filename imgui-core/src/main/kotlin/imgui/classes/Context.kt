package imgui.classes

import glm_.vec2.Vec2
import glm_.vec4.Vec4
import imgui.*
import imgui.ImGui.saveIniSettingsToDisk
import imgui.api.g
import imgui.api.gImGui
import imgui.font.Font
import imgui.font.FontAtlas
import imgui.internal.*
import imgui.internal.classes.*
import java.io.File
import java.nio.ByteBuffer
import java.util.*
import kotlin.collections.ArrayList

/** Main Dear ImGui context
 *
 *  Dear ImGui context (opaque structure, unless including imgui_internal.h)
 *
 *  ~CreateContext */
class Context(sharedFontAtlas: FontAtlas? = null) {

    var initialized = false
    /** Io.Fonts-> is owned by the ImGuiContext and will be destructed along with it.   */
    var fontAtlasOwnedByContext = sharedFontAtlas != null

    var io = IO(sharedFontAtlas)

    var style = Style()

    lateinit var font: Font
    /** (Shortcut) == FontBaseSize * g.CurrentWindow->FontWindowScale == window->FontSize(). Text height for current window. */
    var fontSize = 0f
    /** (Shortcut) == IO.FontGlobalScale * Font->Scale * Font->FontSize. Base text height.    */
    var fontBaseSize = 0f

    var drawListSharedData = DrawListSharedData()

    var time = 0.0

    var frameCount = 0

    var frameCountEnded = -1

    var frameCountRendered = -1
    /** Set by NewFrame(), cleared by EndFrame() */
    var withinFrameScope = false
    /** Set by NewFrame(), cleared by EndFrame() when the implicit debug window has been pushed */
    var withinFrameScopeWithImplicitWindow = false
    /** Set within EndChild() */
    var withinEndChild = false


    // Windows state

    /** Windows, sorted in display order, back to front */
    val windows = ArrayList<Window>()
    /** Windows, sorted in focus order, back to front */
    val windowsFocusOrder = ArrayList<Window>()

    val windowsSortBuffer = ArrayList<Window>()

    val currentWindowStack = Stack<Window>()
    /** Map window's ImGuiID to ImGuiWindow* */
    val windowsById = mutableMapOf<Int, Window>()
    /** Number of unique windows submitted by frame */
    var windowsActiveCount = 0
    /** Window being drawn into    */
    var currentWindow: Window? = null
    /** Will catch mouse inputs */
    var hoveredWindow: Window? = null
    /** Will catch mouse inputs (for focus/move only)   */
    var hoveredRootWindow: Window? = null
    /** Track the window we clicked on (in order to preserve focus). The actually window that is moved is generally MovingWindow->RootWindow. */
    var movingWindow: Window? = null
    /** Track the window we started mouse-wheeling on. Until a timer elapse or mouse has moved, generally keep scrolling the same window even if during the course of scrolling the mouse ends up hovering a child window. */
    var wheelingWindow: Window? = null

    var wheelingWindowRefMousePos: Vec2 = Vec2()

    var wheelingWindowTimer = 0f


    // Item/widgets state and tracking information

    /** Hovered widget  */
    var hoveredId: ID = 0

    var hoveredIdAllowOverlap = false

    var hoveredIdPreviousFrame: ID = 0
    /** Measure contiguous hovering time */
    var hoveredIdTimer = 0f
    /** Measure contiguous hovering time where the item has not been active */
    var hoveredIdNotActiveTimer = 0f
    /** Active widget   */
    var activeId: ID = 0

    /** Active widget has been seen this frame (we can't use a bool as the ActiveId may change within the frame) */
    var activeIdIsAlive: ID = 0

    var activeIdTimer = 0f
    /** Set at the time of activation for one frame */
    var activeIdIsJustActivated = false
    /** Active widget allows another widget to steal active id (generally for overlapping widgets, but not always)   */
    var activeIdAllowOverlap = false
    /** Track whether the active id led to a press (this is to allow changing between PressOnClick and PressOnRelease without pressing twice). Used by range_select branch. */
    var activeIdHasBeenPressedBefore = false
    /** Was the value associated to the widget edited over the course of the Active state. */
    var activeIdHasBeenEditedBefore = false

    var activeIdHasBeenEditedThisFrame = false
    /** Active widget will want to read those directional navigation requests (e.g. can activate a button and move away from it) */
    var activeIdUsingNavDirMask = 0
    /** Active widget will want to read those nav inputs. */
    var activeIdUsingNavInputMask = 0
    /** Active widget will want to read those key inputs. When we grow the ImGuiKey enum we'll need to either to order the enum to make useful keys come first, either redesign this into e.g. a small array. */
    var activeIdUsingKeyInputMask = 0L
    /** Clicked offset from upper-left corner, if applicable (currently only set by ButtonBehavior) */
    var activeIdClickOffset = Vec2(-1)

    var activeIdWindow: Window? = null
    /** Activating with mouse or nav (gamepad/keyboard) */
    var activeIdSource = InputSource.None

    var activeIdPreviousFrame: ID = 0

    var activeIdPreviousFrameIsAlive = false

    var activeIdPreviousFrameHasBeenEdited = false

    var activeIdPreviousFrameWindow: Window? = null
    /** Store the last non-zero ActiveId, useful for animation. */
    var lastActiveId: ID = 0
    /** Store the last non-zero ActiveId timer since the beginning of activation, useful for animation. */
    var lastActiveIdTimer = 0f


    // Next window/item data

    /** Storage for SetNextWindow** functions   */
    val nextWindowData = NextWindowData()
    /** Storage for SetNextItem** functions */
    var nextItemData = NextItemData()


    /** Stack for PushStyleColor()/PopStyleColor()  */
    var colorModifiers = Stack<ColorMod>()
    /** Stack for PushStyleVar()/PopStyleVar()  */
    val styleModifiers = Stack<StyleMod>()
    /** Stack for PushFont()/PopFont()  */
    val fontStack = Stack<Font>()
    /** Which popups are open (persistent)  */
    val openPopupStack = Stack<PopupData>()
    /** Which level of BeginPopup() we are in (reset every frame)   */
    val beginPopupStack = Stack<PopupData>()


    //------------------------------------------------------------------
    // Navigation data (for gamepad/keyboard)
    //------------------------------------------------------------------

    /** Focused window for navigation. Could be called 'FocusWindow'    */
    var navWindow: Window? = null
    /** Focused item for navigation */
    var navId: ID = 0
    /** ~~ (g.activeId == 0) && NavInput.Activate.isPressed() ? navId : 0, also set when calling activateItem() */
    var navActivateId: ID = 0
    /** ~~ isNavInputDown(NavInput.Activate) ? navId : 0   */
    var navActivateDownId: ID = 0
    /** ~~ NavInput.Activate.isPressed() ? navId : 0    */
    var navActivatePressedId: ID = 0
    /** ~~ NavInput.Input.isPressed() ? navId : 0   */
    var navInputId: ID = 0
    /** Just tabbed to this id. */
    var navJustTabbedId: ID = 0
    /** Just navigated to this id (result of a successfully MoveRequest)    */
    var navJustMovedToId: ID = 0
    /** Just navigated to this select scope id (result of a successfully MoveRequest). */
    var navJustMovedToMultiSelectScopeId: ID = 0
    /** Set by ActivateItem(), queued until next frame  */
    var navNextActivateId: ID = 0
    /** Keyboard or Gamepad mode? THIS WILL ONLY BE None or NavGamepad or NavKeyboard.  */
    var navInputSource = InputSource.None
    /** Rectangle used for scoring, in screen space. Based of window.dc.navRefRectRel[], modified for directional navigation scoring.  */
    var navScoringRectScreen = Rect()
    /** Metrics for debugging   */
    var navScoringCount = 0
    /** When selecting a window (holding Menu+FocusPrev/Next, or equivalent of CTRL-TAB) this window is temporarily displayed top-most.   */
    var navWindowingTarget: Window? = null
    /** Record of last valid NavWindowingTarget until DimBgRatio and NavWindowingHighlightAlpha becomes 0f */
    var navWindowingTargetAnim: Window? = null

    val navWindowingList = ArrayList<Window>()

    var navWindowingTimer = 0f

    var navWindowingHighlightAlpha = 0f

    var navWindowingToggleLayer = false
    /** Layer we are navigating on. For now the system is hard-coded for 0 = main contents and 1 = menu/title bar,
     *  may expose layers later. */
    var navLayer = NavLayer.Main
    /** == NavWindow->DC.FocusIdxTabCounter at time of NavId processing */
    var navIdTabCounter = Int.MAX_VALUE
    /** Nav widget has been seen this frame ~~ NavRefRectRel is valid   */
    var navIdIsAlive = false
    /** When set we will update mouse position if (io.ConfigFlag & ConfigFlag.NavMoveMouse) if set (NB: this not enabled by default) */
    var navMousePosDirty = false
    /** When user starts using mouse, we hide gamepad/keyboard highlight (NB: but they are still available, which is why
     *  NavDisableHighlight isn't always != NavDisableMouseHover)   */
    var navDisableHighlight = true
    /** When user starts using gamepad/keyboard, we hide mouse hovering highlight until mouse is touched again. */
    var navDisableMouseHover = false
    /** ~~ navMoveRequest || navInitRequest */
    var navAnyRequest = false
    /** Init request for appearing window to select first item  */
    var navInitRequest = false

    var navInitRequestFromMove = false

    var navInitResultId: ID = 0

    var navInitResultRectRel = Rect()
    /** Set by manual scrolling, if we scroll to a point where NavId isn't visible we reset navigation from visible items   */
    var navMoveFromClampedRefRect = false
    /** Move request for this frame */
    var navMoveRequest = false

    var navMoveRequestFlags: NavMoveFlags = 0
    /** None / ForwardQueued / ForwardActive (this is used to navigate sibling parent menus from a child menu)  */
    var navMoveRequestForward = NavForward.None
    /** Direction of the move request (left/right/up/down), direction of the previous move request  */
    var navMoveDir = Dir.None
    /** Direction of the move request (left/right/up/down), direction of the previous move request  */
    var navMoveDirLast = Dir.None
    /** FIXME-NAV: Describe the purpose of this better. Might want to rename? */
    var navMoveClipDir = Dir.None
    /** Best move request candidate within NavWindow    */
    var navMoveResultLocal = NavMoveResult()
    /** Best move request candidate within NavWindow that are mostly visible (when using NavMoveFlags.AlsoScoreVisibleSet flag) */
    val navMoveResultLocalVisibleSet = NavMoveResult()
    /** Best move request candidate within NavWindow's flattened hierarchy (when using WindowFlags.NavFlattened flag)   */
    var navMoveResultOther = NavMoveResult()

    // Tabbing system (older than Nav, active even if Nav is disabled. FIXME-NAV: This needs a redesign!)

    var focusRequestCurrWindow: Window? = null

    var focusRequestNextWindow: Window? = null
    /** Any item being requested for focus, stored as an index (we on layout to be stable between the frame pressing TAB and the next frame, semi-ouch) */
    var focusRequestCurrCounterAll = Int.MAX_VALUE
    /** Tab item being requested for focus, stored as an index */
    var focusRequestCurrCounterTab = Int.MAX_VALUE
    /** Stored for next frame */
    var focusRequestNextCounterAll = Int.MAX_VALUE
    /** Stored for next frame */
    var focusRequestNextCounterTab = Int.MAX_VALUE

    var focusTabPressed = false


    // ------------------------------------------------------------------
    // Render
    //------------------------------------------------------------------

    /** Main ImDrawData instance to pass render information to the user */
    var drawData = DrawData()

    val drawDataBuilder = DrawDataBuilder()
    /** 0.0..1.0 animation when fading in a dimming background (for modal window and CTRL+TAB list) */
    var dimBgRatio = 0f

    var backgroundDrawList: DrawList = DrawList(drawListSharedData).apply {
        _ownerName = "##Background" // Give it a name for debugging
    }
    /** Optional software render of mouse cursors, if io.MouseDrawCursor is set + a few debug overlays  */
    var foregroundDrawList: DrawList = DrawList(drawListSharedData).apply {
        _ownerName = "##Foreground" // Give it a name for debugging
    }

    var mouseCursor = MouseCursor.Arrow


    //------------------------------------------------------------------
    // Drag and Drop
    //------------------------------------------------------------------
    var dragDropActive = false

    var dragDropWithinSourceOrTarget = false

    var dragDropSourceFlags: DragDropFlags = 0

    var dragDropSourceFrameCount = -1

    var dragDropMouseButton = MouseButton.None // -1 at start

    var dragDropPayload = Payload()

    var dragDropTargetRect = Rect()

    var dragDropTargetId: ID = 0

    var dragDropAcceptFlags: DragDropFlags = 0
    /** Target item surface (we resolve overlapping targets by prioritizing the smaller surface) */
    var dragDropAcceptIdCurrRectSurface = 0f
    /** Target item id (set at the time of accepting the payload) */
    var dragDropAcceptIdCurr: ID = 0
    /** Target item id from previous frame (we need to store this to allow for overlapping drag and drop targets) */
    var dragDropAcceptIdPrev: ID = 0
    /** Last time a target expressed a desire to accept the source */
    var dragDropAcceptFrameCount = -1
    /** We don't expose the ImVector<> directly */
    var dragDropPayloadBufHeap = ByteBuffer.allocate(0)
    /** Local buffer for small payloads */
    var dragDropPayloadBufLocal = ByteBuffer.allocate(16)


    // Tab bars
    var currentTabBar: TabBar? = null
    val tabBars = TabBarPool()
    val currentTabBarStack = Stack<PtrOrIndex>()
    val shrinkWidthBuffer = ArrayList<ShrinkWidthItem>()

    //------------------------------------------------------------------
    // Widget state
    //------------------------------------------------------------------

    var lastValidMousePos = Vec2()

    var inputTextState = TextEditState()

    var inputTextPasswordFont = Font()
    /** Temporary text input when CTRL+clicking on a slider, etc.   */
    var tempInputTextId: ID = 0
    /** Store user options for color edit widgets   */
    var colorEditOptions: ColorEditFlags = ColorEditFlag._OptionsDefault.i
    /** Backup of last Hue associated to LastColor[3], so we can restore Hue in lossy RGB<>HSV round trips */
    var colorEditLastHue = 0f
    /** Backup of last Saturation associated to LastColor[3], so we can restore Saturation in lossy RGB<>HSV round trips */
    var colorEditLastSat = 0f

    var colorEditLastColor = FloatArray(3) { Float.MAX_VALUE }
    /** Initial/reference color at the time of opening the color picker. */
    val colorPickerRef = Vec4()

    var dragCurrentAccumDirty = false
    /** Accumulator for dragging modification. Always high-precision, not rounded by end-user precision settings */
    var dragCurrentAccum = 0f
    /** If speed == 0.0f, uses (max-min) * DragSpeedDefaultRatio    */
    var dragSpeedDefaultRatio = 1f / 100f

    /** Distance between mouse and center of grab box, normalized in parent space. Use storage? */
    var scrollbarClickDeltaToGrabCenter = 0f

    var tooltipOverrideCount = 0
    /** If no custom clipboard handler is defined   */
    var privateClipboard = ""

    // Range-Select/Multi-Select
    // [This is unused in this branch, but left here to facilitate merging/syncing multiple branches]
    var multiSelectScopeId: ID = 0

    // Platform support

    /** Cursor position request to the OS Input Method Editor   */
    var platformImePos = Vec2(Float.MAX_VALUE)
    /** Last cursor position passed to the OS Input Method Editor   */
    var platformImeLastPos = Vec2(Float.MAX_VALUE)

    //------------------------------------------------------------------
    // Settings
    //------------------------------------------------------------------

    var settingsLoaded = false
    /** Save .ini Settings to memory when time reaches zero   */
    var settingsDirtyTimer = 0f
    /** In memory .ini Settings for Window  */
    var settingsIniData = ""
    /** ImGuiWindow .ini settings entries (parsed from the last loaded .ini file and maintained on saving) */
    val settingsWindows = ArrayList<WindowSettings>()

    //------------------------------------------------------------------
    // Capture/Logging
    //------------------------------------------------------------------

    var logEnabled = false

    var logType = LogType.None
    /** If != NULL log to stdout/ file  */
    var logFile: File? = null
    /** Accumulation buffer when log to clipboard. This is pointer so our GImGui static constructor doesn't call heap allocators.   */
    var logBuffer = StringBuilder()

    var logLinePosY = Float.MAX_VALUE

    var logLineFirstItem = false

    var logDepthRef = 0

    var logDepthToExpand = 2

    var logDepthToExpandDefault = 2

    // Debug Tools

    var debugItemPickerActive = false
    /** Will call IM_DEBUG_BREAK() when encountering this id */
    var debugItemPickerBreakId: ID = 0


    //------------------------------------------------------------------
    // Misc
    //------------------------------------------------------------------

    /** Calculate estimate of framerate for user over the last 2 seconds.    */
    val framerateSecPerFrame = FloatArray(120)

    var framerateSecPerFrameIdx = 0

    var framerateSecPerFrameAccum = 0f
    /** Explicit capture via CaptureKeyboardFromApp()/CaptureMouseFromApp() sets those flags   */
    var wantCaptureMouseNextFrame = -1

    var wantCaptureKeyboardNextFrame = -1

    var wantTextInputNextFrame = -1

//    char                    TempBuffer[1024*3+1];               // Temporary text buffer

    /*  Context creation and access
        Each context create its own ImFontAtlas by default. You may instance one yourself and pass it to Context()
        to share a font atlas between imgui contexts.
        None of those functions is reliant on the current context.
        ~CreateContext */
    init {
        if (gImGui == null)
            setCurrent()

        initialize()
    }

    fun initialize() {
        assert(!g.initialized && !g.settingsLoaded)
        g.initialized = true
    }

    fun setCurrent() {
        gImGui = this
    }

    /** Destroy current context
     *  ~DestroyContext */
    fun destroy() {
        shutdown()
        if (gImGui === this)
            gImGui = null // SetCurrentContext(NULL);
    }

    /** This function is merely here to free heap allocations.
     *  ~Shutdown(ImGuiContext* context)    */
    fun shutdown() {

        /*  The fonts atlas can be used prior to calling NewFrame(), so we clear it even if g.Initialized is FALSE
            (which would happen if we never called NewFrame)         */
        if (g.fontAtlasOwnedByContext)
            io.fonts.locked = false
        io.fonts.clear()

        // Cleanup of other data are conditional on actually having initialized Dear ImGui.
        if (!g.initialized) return

        // Save settings (unless we haven't attempted to load them: CreateContext/DestroyContext without a call to NewFrame shouldn't save an empty file)
        if (g.settingsLoaded)
            io.iniFilename?.let(::saveIniSettingsToDisk)

        // Clear everything else
        g.apply {
            windows.forEach { it.destroy() }
            windows.clear()
            windowsFocusOrder.clear()
            windowsSortBuffer.clear()
            currentWindow = null
            currentWindowStack.clear()
            windowsById.clear()
            navWindow = null
            hoveredWindow = null
            hoveredRootWindow = null
            activeIdWindow = null
            activeIdPreviousFrameWindow = null
            movingWindow = null
            settingsWindows.clear()
            colorModifiers.clear()
            styleModifiers.clear()
            fontStack.clear()
            openPopupStack.clear()
            beginPopupStack.clear()
            drawDataBuilder.clear()
            backgroundDrawList.clearFreeMemory()
            foregroundDrawList.clearFreeMemory()

            tabBars.clear()
            currentTabBarStack.clear()
            shrinkWidthBuffer.clear()

            privateClipboard = ""
            inputTextState.textW = charArrayOf()
            inputTextState.initialTextA = charArrayOf()
            inputTextState.textA = charArrayOf()

            if (logFile != null) {
                logFile = null
            }
            logBuffer.setLength(0)

            initialized = false
        }
    }
}
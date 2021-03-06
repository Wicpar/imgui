package imgui.demo.showExampleApp

import glm_.vec2.Vec2
import imgui.Cond
import imgui.ImGui.begin
import imgui.ImGui.beginChild
import imgui.ImGui.button
import imgui.ImGui.combo
import imgui.ImGui.end
import imgui.ImGui.endChild
import imgui.ImGui.popStyleVar
import imgui.ImGui.pushStyleVar
import imgui.ImGui.sameLine
import imgui.ImGui.setNextWindowSize
import imgui.ImGui.text
import imgui.ImGui.textEx
import imgui.classes.ListClipper
import imgui.StyleVar
import kotlin.reflect.KMutableProperty0

object LongText {

    var testType = 0
    val log = StringBuilder()
    var lines = 0

    /** Demonstrate/test rendering huge amount of text, and the incidence of clipping.  */
    operator fun invoke(open: KMutableProperty0<Boolean>) {

        setNextWindowSize(Vec2(520, 600), Cond.FirstUseEver)
        if (!begin("Example: Long text display, TODO", open)) {
            end()
            return
        }

        text("Printing unusually long amount of text.")
        combo("Test type", ::testType, "Single call to TextUnformatted()\u0000Multiple calls to Text(), clipped\u0000Multiple calls to Text(), not clipped (slow)\u0000")
        text("Buffer contents: %d lines, %d bytes", lines, log.length)
        if (button("Clear")) {
            log.clear(); lines = 0; }
        sameLine()
        if (button("Add 1000 lines")) {
            for (i in 0..999)
                log.append("%d The quick brown fox jumps over the lazy dog\n".format(lines + i))
            lines += 1000
        }
        beginChild("Log")

        when (testType) {
            0 -> textEx(log.toString())
            1 -> {
                pushStyleVar(StyleVar.ItemSpacing, Vec2(0))
                val clipper = ListClipper(lines)
                while (clipper.step()) {
                    for (i in clipper.display) {
                        text("%d The quick brown fox jumps over the lazy dog".format(i))
                    }
                }
                popStyleVar()
            }
            2 -> {
                pushStyleVar(StyleVar.ItemSpacing, Vec2(0, 1))
                for (i in 0 until lines) {
                    text("%d The quick brown fox jumps over the lazy dog".format(i))
                }
                popStyleVar()
            }
        }

        endChild()
        end()
    }
}
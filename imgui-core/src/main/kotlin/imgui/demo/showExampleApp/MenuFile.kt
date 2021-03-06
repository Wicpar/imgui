package imgui.demo.showExampleApp

import glm_.vec2.Vec2
import imgui.Col
import imgui.ImGui.beginChild
import imgui.ImGui.checkbox
import imgui.ImGui.combo
import imgui.ImGui.cursorScreenPos
import imgui.ImGui.dummy
import imgui.ImGui.endChild
import imgui.ImGui.inputFloat
import imgui.ImGui.menuItem
import imgui.ImGui.sameLine
import imgui.ImGui.separator
import imgui.ImGui.sliderFloat
import imgui.ImGui.text
import imgui.ImGui.textLineHeight
import imgui.ImGui.windowDrawList
import imgui.dsl.menu

object MenuFile {

    var enabled = true
    var float = 0.5f
    var combo = 0
    var check = true

    /** Note that shortcuts are currently provided for display only (future version will add flags to BeginMenu to process shortcuts) */
    operator fun invoke() {

        menuItem("(dummy menu)", "", false, false)
        menuItem("New")
        menuItem("Open", "Ctrl+O")
        menu("Open Recent") {
            menuItem("fish_hat.c")
            menuItem("fish_hat.inl")
            menuItem("fish_hat.h")
            menu("More..") {
                menuItem("Hello")
                menuItem("Sailor")
                menu("Recurse..") { MenuFile() }
            }
        }
        menuItem("Save", "Ctrl+S")
        menuItem("Save As..")
        separator()
        menu("Options") {
            menuItem("Enabled", "", ::enabled)
            beginChild("child", Vec2(0, 60), true)
            for (i in 0 until 10)
                text("Scrolling Text $i")
            endChild()
            sliderFloat("Value", ::float, 0f, 1f)
            inputFloat("Input", ::float, 0.1f)
            combo("Combo", ::combo, "Yes\u0000No\u0000Maybe\u0000\u0000")
            checkbox("Check", ::check)
        }
        menu("Colors") {
            val sz = textLineHeight
            for (col in Col.values()) {
                val name = col.name
                val p = Vec2(cursorScreenPos)
                windowDrawList.addRectFilled(p, Vec2(p.x + sz, p.y + sz), col.u32)
                dummy(Vec2(sz))
                sameLine()
                menuItem(name)
            }

        }
        menu("Disabled", false) { assert(false) { "Disabled" } }
        menuItem("Checked", selected = true)
        menuItem("Quit", "Alt+F4")
    }
}
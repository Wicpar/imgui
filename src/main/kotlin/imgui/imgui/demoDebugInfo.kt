package imgui.imgui

import glm_.BYTES
import glm_.f
import glm_.glm
import glm_.i
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec4.Vec4
import imgui.*
import imgui.ImGui.alignFirstTextHeightToWidgets
import imgui.ImGui.begin
import imgui.ImGui.beginChild
import imgui.ImGui.beginMenu
import imgui.ImGui.beginTooltip
import imgui.ImGui.bullet
import imgui.ImGui.bulletText
import imgui.ImGui.button
import imgui.ImGui.checkbox
import imgui.ImGui.closeCurrentPopup
import imgui.ImGui.collapsingHeader
import imgui.ImGui.columns
import imgui.ImGui.combo
import imgui.ImGui.dragFloat
import imgui.ImGui.end
import imgui.ImGui.endChild
import imgui.ImGui.endMenu
import imgui.ImGui.endTooltip
import imgui.ImGui.inputFloat
import imgui.ImGui.isItemHovered
import imgui.ImGui.listBox
import imgui.ImGui.menu
import imgui.ImGui.menuBar
import imgui.ImGui.menuItem
import imgui.ImGui.nextColumn
import imgui.ImGui.openPopup
import imgui.ImGui.popId
import imgui.ImGui.popItemWidth
import imgui.ImGui.popStyleVar
import imgui.ImGui.popTextWrapPos
import imgui.ImGui.popupModal
import imgui.ImGui.pushId
import imgui.ImGui.pushItemWidth
import imgui.ImGui.pushStyleVar
import imgui.ImGui.pushTextWrapPos
import imgui.ImGui.sameLine
import imgui.ImGui.selectable
import imgui.ImGui.separator
import imgui.ImGui.setNextWindowPos
import imgui.ImGui.setNextWindowSize
import imgui.ImGui.setNextWindowSizeConstraints
import imgui.ImGui.setWindowSize
import imgui.ImGui.sliderFloat
import imgui.ImGui.sliderInt
import imgui.ImGui.spacing
import imgui.ImGui.text
import imgui.ImGui.textColored
import imgui.ImGui.textDisabled
import imgui.ImGui.textUnformatted
import imgui.ImGui.textWrapped
import imgui.ImGui.treeNode
import imgui.ImGui.treePop
import imgui.ImGui.version
import imgui.ImGui.windowDrawList
import imgui.ImGui.withStyleVar
import imgui.ImGui.withWindow
import imgui.internal.Rect
import imgui.internal.Window
import java.util.*
import imgui.Context as g

interface imgui_demoDebugInfo {

    /** Create demo/test window.
     *  Demonstrate most ImGui features (big function!)
     *  Call this to learn about the library! try to make it always available in your application!   */
    fun showTestWindow(pOpen: BooleanArray) {

        if (showApp.mainMenuBar[0]) showExampleAppMainMenuBar()
        if (showApp.console[0]) showExampleAppConsole(showApp.console)
        if (showApp.log[0]) showExampleAppLog(showApp.log)
        if (showApp.layout[0]) showExampleAppLayout(showApp.layout)
        if (showApp.propertyEditor[0]) showExampleAppPropertyEditor(showApp.propertyEditor)
        if (showApp.longText[0]) showExampleAppLongText(showApp.longText)
        if (showApp.autoResize[0]) showExampleAppAutoResize(showApp.autoResize)
        if (showApp.constrainedResize[0]) showExampleAppConstrainedResize(showApp.constrainedResize)
        if (showApp.fixedOverlay[0]) showExampleAppFixedOverlay(showApp.fixedOverlay)
        if (showApp.manipulatingWindowTitle[0]) showExampleAppManipulatingWindowTitle(showApp.manipulatingWindowTitle)
        if (showApp.customRendering[0]) showExampleAppCustomRendering(showApp.customRendering)
        if (showApp.metrics[0]) ImGui.showMetricsWindow(showApp.metrics)
        if (showApp.styleEditor[0]) {
            begin("Style Editor", pOpen = showApp.styleEditor)
            showStyleEditor()
            end()
        }
        if (showApp.about[0])
            withWindow("About ImGui", showApp.about, WindowFlags.AlwaysAutoResize.i) {
                text("JVM ImGui, %s", version)
                separator()
                text("Original by Omar Cornut, ported by Giuseppe Barbieri and all github contributors.")
                text("ImGui is licensed under the MIT License, see LICENSE for more information.")
            }

        // Demonstrate the various window flags. Typically you would just use the default.
        var windowFlags = 0
        if (noTitlebar[0]) windowFlags = windowFlags or WindowFlags.NoTitleBar
        if (!noBorder[0]) windowFlags = windowFlags or WindowFlags.ShowBorders
        if (noResize[0]) windowFlags = windowFlags or WindowFlags.NoResize
        if (noMove[0]) windowFlags = windowFlags or WindowFlags.NoMove
        if (noScrollbar[0]) windowFlags = windowFlags or WindowFlags.NoScrollbar
        if (noCollapse[0]) windowFlags = windowFlags or WindowFlags.NoCollapse
        if (!noMenu[0]) windowFlags = windowFlags or WindowFlags.MenuBar
        setNextWindowSize(Vec2(550, 680), SetCond.FirstUseEver)
        if (!begin("ImGui Demo", pOpen, windowFlags)) {
            end()   // Early out if the window is collapsed, as an optimization.
            return
        }

        //pushItemWidth(getWindowWidth() * 0.65f);    // 2/3 of the space for widget and 1/3 for labels
        pushItemWidth(-140f)                                 // Right align, keep 140 pixels for labels

        text("JVM ImGui says hello.")

        // Menu
        menuBar {
            menu("Menu") { showExampleMenuFile() }
            menu("Examples") {
                menuItem("Main menu bar", pSelected = showApp.mainMenuBar)
                menuItem("Console", pSelected = showApp.console)
                menuItem("Log", pSelected = showApp.log)
                menuItem("Simple layout", pSelected = showApp.layout)
                menuItem("Property editor", pSelected = showApp.propertyEditor)
                menuItem("Long text display", "", showApp.longText)
                menuItem("Auto-resizing window", "", showApp.autoResize)
                menuItem("Constrained-resizing window", "", showApp.constrainedResize)
//                menuItem("Simple overlay", NULL, &show_app_fixed_overlay)
//                menuItem("Manipulating window title", NULL, &show_app_manipulating_window_title)
//                menuItem("Custom rendering", NULL, &show_app_custom_rendering)
            }
            menu("Help") {
                menuItem("Metrics", pSelected = showApp.metrics)
//                MenuItem("Style Editor", NULL, &show_app_style_editor)
                menuItem("About ImGui", "", showApp.about)
            }
        }

        spacing()
        collapsingHeader("Help") {
            textWrapped("This window is being created by the ShowTestWindow() function. Please refer to the code " +
                    "for programming reference.\n\nUser Guide:")
            showUserGuide()
        }

        collapsingHeader("Window options") {

            checkbox("No titlebar", noTitlebar); sameLine(150f)
            checkbox("No border", noBorder); sameLine(300f)
            checkbox("No resize", noResize)
            checkbox("No move", noMove); sameLine(150f)
            checkbox("No scrollbar", noScrollbar); sameLine(300f)
            checkbox("No collapse", noCollapse)
            checkbox("No menu", noMenu)

            treeNode("Style") { showStyleEditor() }

            treeNode("Logging") {
                textWrapped("The logging API redirects all text output so you can easily capture the content of a " +
                        "window or a block. Tree nodes can be automatically expanded. You can also call LogText() to " +
                        "output directly to the log without a visual output.")
//                logButtons() TODO
            }
        }

        collapsingHeader("Widgets") {

            //            if (ImGui::TreeNode("Trees"))
//            {
//                if (ImGui::TreeNode("Basic trees"))
//                {
//                    for (int i = 0; i < 5; i++)
//                    if (ImGui::TreeNode((void*)(intptr_t)i, "Child %d", i))
//                    {
//                        ImGui::Text("blah blah");
//                        ImGui::SameLine();
//                        if (ImGui::SmallButton("print")) printf("Child %d pressed", i);
//                        ImGui::TreePop();
//                    }
//                    ImGui::TreePop();
//                }
//
//                if (ImGui::TreeNode("Advanced, with Selectable nodes"))
//                {
//                    ShowHelpMarker("This is a more standard looking tree with selectable nodes.\nClick to select, CTRL+Click to toggle, click on arrows or double-click to open.");
//                    static bool align_label_with_current_x_position = false;
//                    ImGui::Checkbox("Align label with current X position)", &align_label_with_current_x_position);
//                    ImGui::Text("Hello!");
//                    if (align_label_with_current_x_position)
//                        ImGui::Unindent(ImGui::GetTreeNodeToLabelSpacing());
//
//                    static int selection_mask = (1 << 2); // Dumb representation of what may be user-side selection state. You may carry selection state inside or outside your objects in whatever format you see fit.
//                    int node_clicked = -1;                // Temporary storage of what node we have clicked to process selection at the end of the loop. May be a pointer to your own node type, etc.
//                    ImGui::PushStyleVar(ImGuiStyleVar_IndentSpacing, ImGui::GetFontSize()*3); // Increase spacing to differentiate leaves from expanded contents.
//                    for (int i = 0; i < 6; i++)
//                    {
//                        // Disable the default open on single-click behavior and pass in Selected flag according to our selection state.
//                        ImGuiTreeNodeFlags node_flags = ImGuiTreeNodeFlags_OpenOnArrow | ImGuiTreeNodeFlags_OpenOnDoubleClick | ((selection_mask & (1 << i)) ? ImGuiTreeNodeFlags_Selected : 0);
//                        if (i < 3)
//                        {
//                            // Node
//                            bool node_open = ImGui::TreeNodeEx((void*)(intptr_t)i, node_flags, "Selectable Node %d", i);
//                            if (ImGui::IsItemClicked())
//                                node_clicked = i;
//                            if (node_open)
//                            {
//                                ImGui::Text("Blah blah\nBlah Blah");
//                                ImGui::TreePop();
//                            }
//                        }
//                        else
//                        {
//                            // Leaf: The only reason we have a TreeNode at all is to allow selection of the leaf. Otherwise we can use BulletText() or TreeAdvanceToLabelPos()+Text().
//                            ImGui::TreeNodeEx((void*)(intptr_t)i, node_flags | ImGuiTreeNodeFlags_Leaf | ImGuiTreeNodeFlags_NoTreePushOnOpen, "Selectable Leaf %d", i);
//                            if (ImGui::IsItemClicked())
//                                node_clicked = i;
//                        }
//                    }
//                    if (node_clicked != -1)
//                    {
//                        // Update selection state. Process outside of tree loop to avoid visual inconsistencies during the clicking-frame.
//                        if (ImGui::GetIO().KeyCtrl)
//                            selection_mask ^= (1 << node_clicked);          // CTRL+click to toggle
//                        else //if (!(selection_mask & (1 << node_clicked))) // Depending on selection behavior you want, this commented bit preserve selection when clicking on item that is part of the selection
//                        selection_mask = (1 << node_clicked);           // Click to single-select
//                    }
//                    ImGui::PopStyleVar();
//                    if (align_label_with_current_x_position)
//                        ImGui::Indent(ImGui::GetTreeNodeToLabelSpacing());
//                    ImGui::TreePop();
//                }
//                ImGui::TreePop();
//            }
//
//            if (ImGui::TreeNode("Collapsing Headers"))
//            {
//                static bool closable_group = true;
//                if (ImGui::CollapsingHeader("Header"))
//                {
//                    ImGui::Checkbox("Enable extra group", &closable_group);
//                    for (int i = 0; i < 5; i++)
//                    ImGui::Text("Some content %d", i);
//                }
//                if (ImGui::CollapsingHeader("Header with a close button", &closable_group))
//                {
//                    for (int i = 0; i < 5; i++)
//                    ImGui::Text("More content %d", i);
//                }
//                ImGui::TreePop();
//            }
//
//            if (ImGui::TreeNode("Bullets"))
//            {
//                ImGui::BulletText("Bullet point 1");
//                ImGui::BulletText("Bullet point 2\nOn multiple lines");
//                ImGui::Bullet(); ImGui::Text("Bullet point 3 (two calls)");
//                ImGui::Bullet(); ImGui::SmallButton("Button");
//                ImGui::TreePop();
//            }
//
//            if (ImGui::TreeNode("Colored Text"))
//            {
//                // Using shortcut. You can use PushStyleColor()/PopStyleColor() for more flexibility.
//                ImGui::TextColored(ImVec4(1.0f,0.0f,1.0f,1.0f), "Pink");
//                ImGui::TextColored(ImVec4(1.0f,1.0f,0.0f,1.0f), "Yellow");
//                ImGui::TextDisabled("Disabled");
//                ImGui::TreePop();
//            }
//
//            if (ImGui::TreeNode("Word Wrapping"))
//            {
//                // Using shortcut. You can use PushTextWrapPos()/PopTextWrapPos() for more flexibility.
//                ImGui::TextWrapped("This text should automatically wrap on the edge of the window. The current implementation for text wrapping follows simple rules suitable for English and possibly other languages.");
//                ImGui::Spacing();
//
//                static float wrap_width = 200.0f;
//                ImGui::SliderFloat("Wrap width", &wrap_width, -20, 600, "%.0f");
//
//                ImGui::Text("Test paragraph 1:");
//                ImVec2 pos = ImGui::GetCursorScreenPos();
//                ImGui::GetWindowDrawList()->AddRectFilled(ImVec2(pos.x + wrap_width, pos.y), ImVec2(pos.x + wrap_width + 10, pos.y + ImGui::GetTextLineHeight()), IM_COL32(255,0,255,255));
//                ImGui::PushTextWrapPos(ImGui::GetCursorPos().x + wrap_width);
//                ImGui::Text("The lazy dog is a good dog. This paragraph is made to fit within %.0f pixels. Testing a 1 character word. The quick brown fox jumps over the lazy dog.", wrap_width);
//                ImGui::GetWindowDrawList()->AddRect(ImGui::GetItemRectMin(), ImGui::GetItemRectMax(), IM_COL32(255,255,0,255));
//                ImGui::PopTextWrapPos();
//
//                ImGui::Text("Test paragraph 2:");
//                pos = ImGui::GetCursorScreenPos();
//                ImGui::GetWindowDrawList()->AddRectFilled(ImVec2(pos.x + wrap_width, pos.y), ImVec2(pos.x + wrap_width + 10, pos.y + ImGui::GetTextLineHeight()), IM_COL32(255,0,255,255));
//                ImGui::PushTextWrapPos(ImGui::GetCursorPos().x + wrap_width);
//                ImGui::Text("aaaaaaaa bbbbbbbb, c cccccccc,dddddddd. d eeeeeeee   ffffffff. gggggggg!hhhhhhhh");
//                ImGui::GetWindowDrawList()->AddRect(ImGui::GetItemRectMin(), ImGui::GetItemRectMax(), IM_COL32(255,255,0,255));
//                ImGui::PopTextWrapPos();
//
//                ImGui::TreePop();
//            }
//
//            if (ImGui::TreeNode("UTF-8 Text"))
//            {
//                // UTF-8 test with Japanese characters
//                // (needs a suitable font, try Arial Unicode or M+ fonts http://mplus-fonts.sourceforge.jp/mplus-outline-fonts/index-en.html)
//                // Most compiler appears to support UTF-8 in source code (with Visual Studio you need to save your file as 'UTF-8 without signature')
//                // However for the sake for maximum portability here we are *not* including raw UTF-8 character in this source file, instead we encode the string with hexadecimal constants.
//                // In your own application be reasonable and use UTF-8 in source or retrieve the data from file system!
//                // Note that characters values are preserved even if the font cannot be displayed, so you can safely copy & paste garbled characters into another application.
//                ImGui::TextWrapped("CJK text will only appears if the font was loaded with the appropriate CJK character ranges. Call io.Font->LoadFromFileTTF() manually to load extra character ranges.");
//                ImGui::Text("Hiragana: \xe3\x81\x8b\xe3\x81\x8d\xe3\x81\x8f\xe3\x81\x91\xe3\x81\x93 (kakikukeko)");
//                ImGui::Text("Kanjis: \xe6\x97\xa5\xe6\x9c\xac\xe8\xaa\x9e (nihongo)");
//                static char buf[32] = "\xe6\x97\xa5\xe6\x9c\xac\xe8\xaa\x9e";
//                ImGui::InputText("UTF-8 input", buf, IM_ARRAYSIZE(buf));
//                ImGui::TreePop();
//            }
//
//            if (ImGui::TreeNode("Images"))
//            {
//                ImGui::TextWrapped("Below we are displaying the font texture (which is the only texture we have access to in this demo). Use the 'ImTextureID' type as storage to pass pointers or identifier to your own texture data. Hover the texture for a zoomed view!");
//                ImVec2 tex_screen_pos = ImGui::GetCursorScreenPos();
//                float tex_w = (float)ImGui::GetIO().Fonts->TexWidth;
//                float tex_h = (float)ImGui::GetIO().Fonts->TexHeight;
//                ImTextureID tex_id = ImGui::GetIO().Fonts->TexID;
//                ImGui::Text("%.0fx%.0f", tex_w, tex_h);
//                ImGui::Image(tex_id, ImVec2(tex_w, tex_h), ImVec2(0,0), ImVec2(1,1), ImColor(255,255,255,255), ImColor(255,255,255,128));
//                if (ImGui::IsItemHovered())
//                {
//                    ImGui::BeginTooltip();
//                    float focus_sz = 32.0f;
//                    float focus_x = ImGui::GetMousePos().x - tex_screen_pos.x - focus_sz * 0.5f; if (focus_x < 0.0f) focus_x = 0.0f; else if (focus_x > tex_w - focus_sz) focus_x = tex_w - focus_sz;
//                    float focus_y = ImGui::GetMousePos().y - tex_screen_pos.y - focus_sz * 0.5f; if (focus_y < 0.0f) focus_y = 0.0f; else if (focus_y > tex_h - focus_sz) focus_y = tex_h - focus_sz;
//                    ImGui::Text("Min: (%.2f, %.2f)", focus_x, focus_y);
//                    ImGui::Text("Max: (%.2f, %.2f)", focus_x + focus_sz, focus_y + focus_sz);
//                    ImVec2 uv0 = ImVec2((focus_x) / tex_w, (focus_y) / tex_h);
//                    ImVec2 uv1 = ImVec2((focus_x + focus_sz) / tex_w, (focus_y + focus_sz) / tex_h);
//                    ImGui::Image(tex_id, ImVec2(128,128), uv0, uv1, ImColor(255,255,255,255), ImColor(255,255,255,128));
//                    ImGui::EndTooltip();
//                }
//                ImGui::TextWrapped("And now some textured buttons..");
//                static int pressed_count = 0;
//                for (int i = 0; i < 8; i++)
//                {
//                    ImGui::PushID(i);
//                    int frame_padding = -1 + i;     // -1 = uses default padding
//                    if (ImGui::ImageButton(tex_id, ImVec2(32,32), ImVec2(0,0), ImVec2(32.0f/tex_w,32/tex_h), frame_padding, ImColor(0,0,0,255)))
//                        pressed_count += 1;
//                    ImGui::PopID();
//                    ImGui::SameLine();
//                }
//                ImGui::NewLine();
//                ImGui::Text("Pressed %d times.", pressed_count);
//                ImGui::TreePop();
//            }
//
//            if (ImGui::TreeNode("Selectables"))
//            {
//                if (ImGui::TreeNode("Basic"))
//                {
//                    static bool selected[4] = { false, true, false, false };
//                    ImGui::Selectable("1. I am selectable", &selected[0]);
//                    ImGui::Selectable("2. I am selectable", &selected[1]);
//                    ImGui::Text("3. I am not selectable");
//                    ImGui::Selectable("4. I am selectable", &selected[2]);
//                    if (ImGui::Selectable("5. I am double clickable", selected[3], ImGuiSelectableFlags_AllowDoubleClick))
//                        if (ImGui::IsMouseDoubleClicked(0))
//                            selected[3] = !selected[3];
//                    ImGui::TreePop();
//                }
//                if (ImGui::TreeNode("Rendering more text into the same block"))
//                {
//                    static bool selected[3] = { false, false, false };
//                    ImGui::Selectable("main.c", &selected[0]);    ImGui::SameLine(300); ImGui::Text(" 2,345 bytes");
//                    ImGui::Selectable("Hello.cpp", &selected[1]); ImGui::SameLine(300); ImGui::Text("12,345 bytes");
//                    ImGui::Selectable("Hello.h", &selected[2]);   ImGui::SameLine(300); ImGui::Text(" 2,345 bytes");
//                    ImGui::TreePop();
//                }
//                if (ImGui::TreeNode("In columns"))
//                {
//                    ImGui::Columns(3, NULL, false);
//                    static bool selected[16] = { 0 };
//                    for (int i = 0; i < 16; i++)
//                    {
//                        char label[32]; sprintf(label, "Item %d", i);
//                        if (ImGui::Selectable(label, &selected[i])) {}
//                        ImGui::NextColumn();
//                    }
//                    ImGui::Columns(1);
//                    ImGui::TreePop();
//                }
//                if (ImGui::TreeNode("Grid"))
//                {
//                    static bool selected[16] = { true, false, false, false, false, true, false, false, false, false, true, false, false, false, false, true };
//                    for (int i = 0; i < 16; i++)
//                    {
//                        ImGui::PushID(i);
//                        if (ImGui::Selectable("Sailor", &selected[i], 0, ImVec2(50,50)))
//                        {
//                            int x = i % 4, y = i / 4;
//                            if (x > 0) selected[i - 1] ^= 1;
//                            if (x < 3) selected[i + 1] ^= 1;
//                            if (y > 0) selected[i - 4] ^= 1;
//                            if (y < 3) selected[i + 4] ^= 1;
//                        }
//                        if ((i % 4) < 3) ImGui::SameLine();
//                        ImGui::PopID();
//                    }
//                    ImGui::TreePop();
//                }
//                ImGui::TreePop();
//            }
//
//            if (ImGui::TreeNode("Filtered Text Input"))
//            {
//                static char buf1[64] = ""; ImGui::InputText("default", buf1, 64);
//                static char buf2[64] = ""; ImGui::InputText("decimal", buf2, 64, ImGuiInputTextFlags_CharsDecimal);
//                static char buf3[64] = ""; ImGui::InputText("hexadecimal", buf3, 64, ImGuiInputTextFlags_CharsHexadecimal | ImGuiInputTextFlags_CharsUppercase);
//                static char buf4[64] = ""; ImGui::InputText("uppercase", buf4, 64, ImGuiInputTextFlags_CharsUppercase);
//                static char buf5[64] = ""; ImGui::InputText("no blank", buf5, 64, ImGuiInputTextFlags_CharsNoBlank);
//                struct TextFilters { static int FilterImGuiLetters(ImGuiTextEditCallbackData* data) { if (data->EventChar < 256 && strchr("imgui", (char)data->EventChar)) return 0; return 1; } };
//                static char buf6[64] = ""; ImGui::InputText("\"imgui\" letters", buf6, 64, ImGuiInputTextFlags_CallbackCharFilter, TextFilters::FilterImGuiLetters);
//
//                ImGui::Text("Password input");
//                static char bufpass[64] = "password123";
//                ImGui::InputText("password", bufpass, 64, ImGuiInputTextFlags_Password | ImGuiInputTextFlags_CharsNoBlank);
//                ImGui::SameLine(); ShowHelpMarker("Display all characters as '*'.\nDisable clipboard cut and copy.\nDisable logging.\n");
//                ImGui::InputText("password (clear)", bufpass, 64, ImGuiInputTextFlags_CharsNoBlank);
//
//                ImGui::TreePop();
//            }
//
//            if (ImGui::TreeNode("Multi-line Text Input"))
//            {
//                static bool read_only = false;
//                static char text[1024*16] =
//                        "/*\n"
//                " The Pentium F00F bug, shorthand for F0 0F C7 C8,\n"
//                " the hexadecimal encoding of one offending instruction,\n"
//                " more formally, the invalid operand with locked CMPXCHG8B\n"
//                " instruction bug, is a design flaw in the majority of\n"
//                " Intel Pentium, Pentium MMX, and Pentium OverDrive\n"
//                " processors (all in the P5 microarchitecture).\n"
//                "*/\n\n"
//                "label:\n"
//                "\tlock cmpxchg8b eax\n";
//
//                ImGui::PushStyleVar(ImGuiStyleVar_FramePadding, ImVec2(0,0));
//                ImGui::Checkbox("Read-only", &read_only);
//                ImGui::PopStyleVar();
//                ImGui::InputTextMultiline("##source", text, IM_ARRAYSIZE(text), ImVec2(-1.0f, ImGui::GetTextLineHeight() * 16), ImGuiInputTextFlags_AllowTabInput | (read_only ? ImGuiInputTextFlags_ReadOnly : 0));
//                ImGui::TreePop();
//            }
//
//            static bool a=false;
//            if (ImGui::Button("Button")) { printf("Clicked\n"); a ^= 1; }
//            if (a)
//            {
//                ImGui::SameLine();
//                ImGui::Text("Thanks for clicking me!");
//            }
//
//            static bool check = true;
//            ImGui::Checkbox("checkbox", &check);
//
//            static int e = 0;
//            ImGui::RadioButton("radio a", &e, 0); ImGui::SameLine();
//            ImGui::RadioButton("radio b", &e, 1); ImGui::SameLine();
//            ImGui::RadioButton("radio c", &e, 2);
//
//            // Color buttons, demonstrate using PushID() to add unique identifier in the ID stack, and changing style.
//            for (int i = 0; i < 7; i++)
//            {
//                if (i > 0) ImGui::SameLine();
//                ImGui::PushID(i);
//                ImGui::PushStyleColor(ImGuiCol_Button, ImColor::HSV(i/7.0f, 0.6f, 0.6f));
//                ImGui::PushStyleColor(ImGuiCol_ButtonHovered, ImColor::HSV(i/7.0f, 0.7f, 0.7f));
//                ImGui::PushStyleColor(ImGuiCol_ButtonActive, ImColor::HSV(i/7.0f, 0.8f, 0.8f));
//                ImGui::Button("Click");
//                ImGui::PopStyleColor(3);
//                ImGui::PopID();
//            }
//
//            ImGui::Text("Hover over me");
//            if (ImGui::IsItemHovered())
//                ImGui::SetTooltip("I am a tooltip");
//
//            ImGui::SameLine();
//            ImGui::Text("- or me");
//            if (ImGui::IsItemHovered())
//            {
//                ImGui::BeginTooltip();
//                ImGui::Text("I am a fancy tooltip");
//                static float arr[] = { 0.6f, 0.1f, 1.0f, 0.5f, 0.92f, 0.1f, 0.2f };
//                ImGui::PlotLines("Curve", arr, IM_ARRAYSIZE(arr));
//                ImGui::EndTooltip();
//            }
//
//            // Testing IMGUI_ONCE_UPON_A_FRAME macro
//            //for (int i = 0; i < 5; i++)
//            //{
//            //  IMGUI_ONCE_UPON_A_FRAME
//            //  {
//            //      ImGui::Text("This will be displayed only once.");
//            //  }
//            //}
//
//            ImGui::Separator();
//
//            ImGui::LabelText("label", "Value");
//
//            static int item = 1;
//            ImGui::Combo("combo", &item, "aaaa\0bbbb\0cccc\0dddd\0eeee\0\0");   // Combo using values packed in a single constant string (for really quick combo)
//
//            const char* items[] = { "AAAA", "BBBB", "CCCC", "DDDD", "EEEE", "FFFF", "GGGG", "HHHH", "IIII", "JJJJ", "KKKK" };
//            static int item2 = -1;
//            ImGui::Combo("combo scroll", &item2, items, IM_ARRAYSIZE(items));   // Combo using proper array. You can also pass a callback to retrieve array value, no need to create/copy an array just for that.
//
//            {
//                static char str0[128] = "Hello, world!";
//                static int i0=123;
//                static float f0=0.001f;
//                ImGui::InputText("input text", str0, IM_ARRAYSIZE(str0));
//                ImGui::SameLine(); ShowHelpMarker("Hold SHIFT or use mouse to select text.\n" "CTRL+Left/Right to word jump.\n" "CTRL+A or double-click to select all.\n" "CTRL+X,CTRL+C,CTRL+V clipboard.\n" "CTRL+Z,CTRL+Y undo/redo.\n" "ESCAPE to revert.\n");
//
//                ImGui::InputInt("input int", &i0);
//                ImGui::SameLine(); ShowHelpMarker("You can apply arithmetic operators +,*,/ on numerical values.\n  e.g. [ 100 ], input \'*2\', result becomes [ 200 ]\nUse +- to subtract.\n");
//
//                ImGui::InputFloat("input float", &f0, 0.01f, 1.0f);
//
//                static float vec4a[4] = { 0.10f, 0.20f, 0.30f, 0.44f };
//                ImGui::InputFloat3("input float3", vec4a);
//            }
//
//            {
//                static int i1=50, i2=42;
//                ImGui::DragInt("drag int", &i1, 1);
//                ImGui::SameLine(); ShowHelpMarker("Click and drag to edit value.\nHold SHIFT/ALT for faster/slower edit.\nDouble-click or CTRL+click to input value.");
//
//                ImGui::DragInt("drag int 0..100", &i2, 1, 0, 100, "%.0f%%");
//
//                static float f1=1.00f, f2=0.0067f;
//                ImGui::DragFloat("drag float", &f1, 0.005f);
//                ImGui::DragFloat("drag small float", &f2, 0.0001f, 0.0f, 0.0f, "%.06f ns");
//            }
//
//            {
//                static int i1=0;
//                ImGui::SliderInt("slider int", &i1, -1, 3);
//                ImGui::SameLine(); ShowHelpMarker("CTRL+click to input value.");
//
//                static float f1=0.123f, f2=0.0f;
//                ImGui::SliderFloat("slider float", &f1, 0.0f, 1.0f, "ratio = %.3f");
//                ImGui::SliderFloat("slider log float", &f2, -10.0f, 10.0f, "%.4f", 3.0f);
//                static float angle = 0.0f;
//                ImGui::SliderAngle("slider angle", &angle);
//            }
//
//            static float col1[3] = { 1.0f,0.0f,0.2f };
//            static float col2[4] = { 0.4f,0.7f,0.0f,0.5f };
//            ImGui::ColorEdit3("color 1", col1);
//            ImGui::SameLine(); ShowHelpMarker("Click on the colored square to change edit mode.\nCTRL+click on individual component to input value.\n");
//
//            ImGui::ColorEdit4("color 2", col2);

            val listboxItems = arrayOf("Apple", "Banana", "Cherry", "Kiwi", "Mango", "Orange", "Pineapple",
                    "Strawberry", "Watermelon")
            listBox("listbox\n(single select)", listboxItemCurrent, listboxItems, 4)

//            //static int listbox_item_current2 = 2;
//            //ImGui::PushItemWidth(-1);
//            //ImGui::ListBox("##listbox2", &listbox_item_current2, listbox_items, IM_ARRAYSIZE(listbox_items), 4);
//            //ImGui::PopItemWidth();
//
//            if (ImGui::TreeNode("Range Widgets"))
//            {
//                ImGui::Unindent();
//
//                static float begin = 10, end = 90;
//                static int begin_i = 100, end_i = 1000;
//                ImGui::DragFloatRange2("range", &begin, &end, 0.25f, 0.0f, 100.0f, "Min: %.1f %%", "Max: %.1f %%");
//                ImGui::DragIntRange2("range int (no bounds)", &begin_i, &end_i, 5, 0, 0, "Min: %.0f units", "Max: %.0f units");
//
//                ImGui::Indent();
//                ImGui::TreePop();
//            }
//
//            if (ImGui::TreeNode("Multi-component Widgets"))
//            {
//                ImGui::Unindent();
//
//                static float vec4f[4] = { 0.10f, 0.20f, 0.30f, 0.44f };
//                static int vec4i[4] = { 1, 5, 100, 255 };
//
//                ImGui::InputFloat2("input float2", vec4f);
//                ImGui::DragFloat2("drag float2", vec4f, 0.01f, 0.0f, 1.0f);
//                ImGui::SliderFloat2("slider float2", vec4f, 0.0f, 1.0f);
//                ImGui::DragInt2("drag int2", vec4i, 1, 0, 255);
//                ImGui::InputInt2("input int2", vec4i);
//                ImGui::SliderInt2("slider int2", vec4i, 0, 255);
//                ImGui::Spacing();
//
//                ImGui::InputFloat3("input float3", vec4f);
//                ImGui::DragFloat3("drag float3", vec4f, 0.01f, 0.0f, 1.0f);
//                ImGui::SliderFloat3("slider float3", vec4f, 0.0f, 1.0f);
//                ImGui::DragInt3("drag int3", vec4i, 1, 0, 255);
//                ImGui::InputInt3("input int3", vec4i);
//                ImGui::SliderInt3("slider int3", vec4i, 0, 255);
//                ImGui::Spacing();
//
//                ImGui::InputFloat4("input float4", vec4f);
//                ImGui::DragFloat4("drag float4", vec4f, 0.01f, 0.0f, 1.0f);
//                ImGui::SliderFloat4("slider float4", vec4f, 0.0f, 1.0f);
//                ImGui::InputInt4("input int4", vec4i);
//                ImGui::DragInt4("drag int4", vec4i, 1, 0, 255);
//                ImGui::SliderInt4("slider int4", vec4i, 0, 255);
//
//                ImGui::Indent();
//                ImGui::TreePop();
//            }
//
//            if (ImGui::TreeNode("Vertical Sliders"))
//            {
//                ImGui::Unindent();
//                const float spacing = 4;
//                ImGui::PushStyleVar(ImGuiStyleVar_ItemSpacing, ImVec2(spacing, spacing));
//
//                static int int_value = 0;
//                ImGui::VSliderInt("##int", ImVec2(18,160), &int_value, 0, 5);
//                ImGui::SameLine();
//
//                static float values[7] = { 0.0f, 0.60f, 0.35f, 0.9f, 0.70f, 0.20f, 0.0f };
//                ImGui::PushID("set1");
//                for (int i = 0; i < 7; i++)
//                {
//                    if (i > 0) ImGui::SameLine();
//                    ImGui::PushID(i);
//                    ImGui::PushStyleColor(ImGuiCol_FrameBg, ImColor::HSV(i/7.0f, 0.5f, 0.5f));
//                    ImGui::PushStyleColor(ImGuiCol_FrameBgHovered, ImColor::HSV(i/7.0f, 0.6f, 0.5f));
//                    ImGui::PushStyleColor(ImGuiCol_FrameBgActive, ImColor::HSV(i/7.0f, 0.7f, 0.5f));
//                    ImGui::PushStyleColor(ImGuiCol_SliderGrab, ImColor::HSV(i/7.0f, 0.9f, 0.9f));
//                    ImGui::VSliderFloat("##v", ImVec2(18,160), &values[i], 0.0f, 1.0f, "");
//                    if (ImGui::IsItemActive() || ImGui::IsItemHovered())
//                        ImGui::SetTooltip("%.3f", values[i]);
//                    ImGui::PopStyleColor(4);
//                    ImGui::PopID();
//                }
//                ImGui::PopID();
//
//                ImGui::SameLine();
//                ImGui::PushID("set2");
//                static float values2[4] = { 0.20f, 0.80f, 0.40f, 0.25f };
//                const int rows = 3;
//                const ImVec2 small_slider_size(18, (160.0f-(rows-1)*spacing)/rows);
//                for (int nx = 0; nx < 4; nx++)
//                {
//                    if (nx > 0) ImGui::SameLine();
//                    ImGui::BeginGroup();
//                    for (int ny = 0; ny < rows; ny++)
//                    {
//                        ImGui::PushID(nx*rows+ny);
//                        ImGui::VSliderFloat("##v", small_slider_size, &values2[nx], 0.0f, 1.0f, "");
//                        if (ImGui::IsItemActive() || ImGui::IsItemHovered())
//                            ImGui::SetTooltip("%.3f", values2[nx]);
//                        ImGui::PopID();
//                    }
//                    ImGui::EndGroup();
//                }
//                ImGui::PopID();
//
//                ImGui::SameLine();
//                ImGui::PushID("set3");
//                for (int i = 0; i < 4; i++)
//                {
//                    if (i > 0) ImGui::SameLine();
//                    ImGui::PushID(i);
//                    ImGui::PushStyleVar(ImGuiStyleVar_GrabMinSize, 40);
//                    ImGui::VSliderFloat("##v", ImVec2(40,160), &values[i], 0.0f, 1.0f, "%.2f\nsec");
//                    ImGui::PopStyleVar();
//                    ImGui::PopID();
//                }
//                ImGui::PopID();
//                ImGui::PopStyleVar();
//
//                ImGui::Indent();
//                ImGui::TreePop();
//            }
        }

        collapsingHeader("Graphs widgets") {

            //            static bool animate = true;
//            ImGui::Checkbox("Animate", &animate);
//
//            static float arr[] = { 0.6f, 0.1f, 1.0f, 0.5f, 0.92f, 0.1f, 0.2f };
//            ImGui::PlotLines("Frame Times", arr, IM_ARRAYSIZE(arr));
//
//            // Create a dummy array of contiguous float values to plot
//            // Tip: If your float aren't contiguous but part of a structure, you can pass a pointer to your first float and the sizeof() of your structure in the Stride parameter.
//            static float values[90] = { 0 };
//            static int values_offset = 0;
//            if (animate)
//            {
//                static float refresh_time = ImGui::GetTime(); // Create dummy data at fixed 60 hz rate for the demo
//                for (; ImGui::GetTime() > refresh_time + 1.0f/60.0f; refresh_time += 1.0f/60.0f)
//                {
//                    static float phase = 0.0f;
//                    values[values_offset] = cosf(phase);
//                    values_offset = (values_offset+1) % IM_ARRAYSIZE(values);
//                    phase += 0.10f*values_offset;
//                }
//            }
//            ImGui::PlotLines("Lines", values, IM_ARRAYSIZE(values), values_offset, "avg 0.0", -1.0f, 1.0f, ImVec2(0,80));
//            ImGui::PlotHistogram("Histogram", arr, IM_ARRAYSIZE(arr), 0, NULL, 0.0f, 1.0f, ImVec2(0,80));
//
//            // Use functions to generate output
//            // FIXME: This is rather awkward because current plot API only pass in indices. We probably want an API passing floats and user provide sample rate/count.
//            struct Funcs
//                    {
//                        static float Sin(void*, int i) { return sinf(i * 0.1f); }
//                        static float Saw(void*, int i) { return (i & 1) ? 1.0f : 0.0f; }
//                    };
//            static int func_type = 0, display_count = 70;
//            ImGui::Separator();
//            ImGui::PushItemWidth(100); ImGui::Combo("func", &func_type, "Sin\0Saw\0"); ImGui::PopItemWidth();
//            ImGui::SameLine();
//            ImGui::SliderInt("Sample count", &display_count, 1, 400);
//            float (*func)(void*, int) = (func_type == 0) ? Funcs::Sin : Funcs::Saw;
//            ImGui::PlotLines("Lines", func, NULL, display_count, 0, NULL, -1.0f, 1.0f, ImVec2(0,80));
//            ImGui::PlotHistogram("Histogram", func, NULL, display_count, 0, NULL, -1.0f, 1.0f, ImVec2(0,80));
//            ImGui::Separator();
//
//            // Animate a simple progress bar
//            static float progress = 0.0f, progress_dir = 1.0f;
//            if (animate)
//            {
//                progress += progress_dir * 0.4f * ImGui::GetIO().DeltaTime;
//                if (progress >= +1.1f) { progress = +1.1f; progress_dir *= -1.0f; }
//                if (progress <= -0.1f) { progress = -0.1f; progress_dir *= -1.0f; }
//            }
//
//            // Typically we would use ImVec2(-1.0f,0.0f) to use all available width, or ImVec2(width,0.0f) for a specified width. ImVec2(0.0f,0.0f) uses ItemWidth.
//            ImGui::ProgressBar(progress, ImVec2(0.0f,0.0f));
//            ImGui::SameLine(0.0f, ImGui::GetStyle().ItemInnerSpacing.x);
//            ImGui::Text("Progress Bar");
//
//            float progress_saturated = (progress < 0.0f) ? 0.0f : (progress > 1.0f) ? 1.0f : progress;
//            char buf[32];
//            sprintf(buf, "%d/%d", (int)(progress_saturated*1753), 1753);
//            ImGui::ProgressBar(progress, ImVec2(0.f,0.f), buf);
        }

        collapsingHeader("Layout") {

            //            if (ImGui::TreeNode("Child regions"))
//            {
//                ImGui::Text("Without border");
//                static int line = 50;
//                bool goto_line = ImGui::Button("Goto");
//                ImGui::SameLine();
//                ImGui::PushItemWidth(100);
//                goto_line |= ImGui::InputInt("##Line", &line, 0, 0, ImGuiInputTextFlags_EnterReturnsTrue);
//                ImGui::PopItemWidth();
//                ImGui::BeginChild("Sub1", ImVec2(ImGui::GetWindowContentRegionWidth() * 0.5f,300), false, ImGuiWindowFlags_HorizontalScrollbar);
//                for (int i = 0; i < 100; i++)
//                {
//                    ImGui::Text("%04d: scrollable region", i);
//                    if (goto_line && line == i)
//                        ImGui::SetScrollHere();
//                }
//                if (goto_line && line >= 100)
//                    ImGui::SetScrollHere();
//                ImGui::EndChild();
//
//                ImGui::SameLine();
//
//                ImGui::PushStyleVar(ImGuiStyleVar_ChildWindowRounding, 5.0f);
//                ImGui::BeginChild("Sub2", ImVec2(0,300), true);
//                ImGui::Text("With border");
//                ImGui::Columns(2);
//                for (int i = 0; i < 100; i++)
//                {
//                    if (i == 50)
//                        ImGui::NextColumn();
//                    char buf[32];
//                    sprintf(buf, "%08x", i*5731);
//                    ImGui::Button(buf, ImVec2(-1.0f, 0.0f));
//                }
//                ImGui::EndChild();
//                ImGui::PopStyleVar();
//
//                ImGui::TreePop();
//            }
//
//            if (ImGui::TreeNode("Widgets Width"))
//            {
//                static float f = 0.0f;
//                ImGui::Text("PushItemWidth(100)");
//                ImGui::SameLine(); ShowHelpMarker("Fixed width.");
//                ImGui::PushItemWidth(100);
//                ImGui::DragFloat("float##1", &f);
//                ImGui::PopItemWidth();
//
//                ImGui::Text("PushItemWidth(GetWindowWidth() * 0.5f)");
//                ImGui::SameLine(); ShowHelpMarker("Half of window width.");
//                ImGui::PushItemWidth(ImGui::GetWindowWidth() * 0.5f);
//                ImGui::DragFloat("float##2", &f);
//                ImGui::PopItemWidth();
//
//                ImGui::Text("PushItemWidth(GetContentRegionAvailWidth() * 0.5f)");
//                ImGui::SameLine(); ShowHelpMarker("Half of available width.\n(~ right-cursor_pos)\n(works within a column set)");
//                ImGui::PushItemWidth(ImGui::GetContentRegionAvailWidth() * 0.5f);
//                ImGui::DragFloat("float##3", &f);
//                ImGui::PopItemWidth();
//
//                ImGui::Text("PushItemWidth(-100)");
//                ImGui::SameLine(); ShowHelpMarker("Align to right edge minus 100");
//                ImGui::PushItemWidth(-100);
//                ImGui::DragFloat("float##4", &f);
//                ImGui::PopItemWidth();
//
//                ImGui::Text("PushItemWidth(-1)");
//                ImGui::SameLine(); ShowHelpMarker("Align to right edge");
//                ImGui::PushItemWidth(-1);
//                ImGui::DragFloat("float##5", &f);
//                ImGui::PopItemWidth();
//
//                ImGui::TreePop();
//            }
//
//            if (ImGui::TreeNode("Basic Horizontal Layout"))
//            {
//                ImGui::TextWrapped("(Use ImGui::SameLine() to keep adding items to the right of the preceding item)");
//
//                // Text
//                ImGui::Text("Two items: Hello"); ImGui::SameLine();
//                ImGui::TextColored(ImVec4(1,1,0,1), "Sailor");
//
//                // Adjust spacing
//                ImGui::Text("More spacing: Hello"); ImGui::SameLine(0, 20);
//                ImGui::TextColored(ImVec4(1,1,0,1), "Sailor");
//
//                // Button
//                ImGui::AlignFirstTextHeightToWidgets();
//                ImGui::Text("Normal buttons"); ImGui::SameLine();
//                ImGui::Button("Banana"); ImGui::SameLine();
//                ImGui::Button("Apple"); ImGui::SameLine();
//                ImGui::Button("Corniflower");
//
//                // Button
//                ImGui::Text("Small buttons"); ImGui::SameLine();
//                ImGui::SmallButton("Like this one"); ImGui::SameLine();
//                ImGui::Text("can fit within a text block.");
//
//                // Aligned to arbitrary position. Easy/cheap column.
//                ImGui::Text("Aligned");
//                ImGui::SameLine(150); ImGui::Text("x=150");
//                ImGui::SameLine(300); ImGui::Text("x=300");
//                ImGui::Text("Aligned");
//                ImGui::SameLine(150); ImGui::SmallButton("x=150");
//                ImGui::SameLine(300); ImGui::SmallButton("x=300");
//
//                // Checkbox
//                static bool c1=false,c2=false,c3=false,c4=false;
//                ImGui::Checkbox("My", &c1); ImGui::SameLine();
//                ImGui::Checkbox("Tailor", &c2); ImGui::SameLine();
//                ImGui::Checkbox("Is", &c3); ImGui::SameLine();
//                ImGui::Checkbox("Rich", &c4);
//
//                // Various
//                static float f0=1.0f, f1=2.0f, f2=3.0f;
//                ImGui::PushItemWidth(80);
//                const char* items[] = { "AAAA", "BBBB", "CCCC", "DDDD" };
//                static int item = -1;
//                ImGui::Combo("Combo", &item, items, IM_ARRAYSIZE(items)); ImGui::SameLine();
//                ImGui::SliderFloat("X", &f0, 0.0f,5.0f); ImGui::SameLine();
//                ImGui::SliderFloat("Y", &f1, 0.0f,5.0f); ImGui::SameLine();
//                ImGui::SliderFloat("Z", &f2, 0.0f,5.0f);
//                ImGui::PopItemWidth();
//
//                ImGui::PushItemWidth(80);
//                ImGui::Text("Lists:");
//                static int selection[4] = { 0, 1, 2, 3 };
//                for (int i = 0; i < 4; i++)
//                {
//                    if (i > 0) ImGui::SameLine();
//                    ImGui::PushID(i);
//                    ImGui::ListBox("", &selection[i], items, IM_ARRAYSIZE(items));
//                    ImGui::PopID();
//                    //if (ImGui::IsItemHovered()) ImGui::SetTooltip("ListBox %d hovered", i);
//                }
//                ImGui::PopItemWidth();
//
//                // Dummy
//                ImVec2 sz(30,30);
//                ImGui::Button("A", sz); ImGui::SameLine();
//                ImGui::Dummy(sz); ImGui::SameLine();
//                ImGui::Button("B", sz);
//
//                ImGui::TreePop();
//            }
//
//            if (ImGui::TreeNode("Groups"))
//            {
//                ImGui::TextWrapped("(Using ImGui::BeginGroup()/EndGroup() to layout items. BeginGroup() basically locks the horizontal position. EndGroup() bundles the whole group so that you can use functions such as IsItemHovered() on it.)");
//                ImGui::BeginGroup();
//                {
//                    ImGui::BeginGroup();
//                    ImGui::Button("AAA");
//                    ImGui::SameLine();
//                    ImGui::Button("BBB");
//                    ImGui::SameLine();
//                    ImGui::BeginGroup();
//                    ImGui::Button("CCC");
//                    ImGui::Button("DDD");
//                    ImGui::EndGroup();
//                    if (ImGui::IsItemHovered())
//                        ImGui::SetTooltip("Group hovered");
//                    ImGui::SameLine();
//                    ImGui::Button("EEE");
//                    ImGui::EndGroup();
//                }
//                // Capture the group size and create widgets using the same size
//                ImVec2 size = ImGui::GetItemRectSize();
//                const float values[5] = { 0.5f, 0.20f, 0.80f, 0.60f, 0.25f };
//                ImGui::PlotHistogram("##values", values, IM_ARRAYSIZE(values), 0, NULL, 0.0f, 1.0f, size);
//
//                ImGui::Button("ACTION", ImVec2((size.x - ImGui::GetStyle().ItemSpacing.x)*0.5f,size.y));
//                ImGui::SameLine();
//                ImGui::Button("REACTION", ImVec2((size.x - ImGui::GetStyle().ItemSpacing.x)*0.5f,size.y));
//                ImGui::EndGroup();
//                ImGui::SameLine();
//
//                ImGui::Button("LEVERAGE\nBUZZWORD", size);
//                ImGui::SameLine();
//
//                ImGui::ListBoxHeader("List", size);
//                ImGui::Selectable("Selected", true);
//                ImGui::Selectable("Not Selected", false);
//                ImGui::ListBoxFooter();
//
//                ImGui::TreePop();
//            }
//
//            if (ImGui::TreeNode("Text Baseline Alignment"))
//            {
//                ImGui::TextWrapped("(This is testing the vertical alignment that occurs on text to keep it at the same baseline as widgets. Lines only composed of text or \"small\" widgets fit in less vertical spaces than lines with normal widgets)");
//
//                ImGui::Text("One\nTwo\nThree"); ImGui::SameLine();
//                ImGui::Text("Hello\nWorld"); ImGui::SameLine();
//                ImGui::Text("Banana");
//
//                ImGui::Text("Banana"); ImGui::SameLine();
//                ImGui::Text("Hello\nWorld"); ImGui::SameLine();
//                ImGui::Text("One\nTwo\nThree");
//
//                ImGui::Button("HOP##1"); ImGui::SameLine();
//                ImGui::Text("Banana"); ImGui::SameLine();
//                ImGui::Text("Hello\nWorld"); ImGui::SameLine();
//                ImGui::Text("Banana");
//
//                ImGui::Button("HOP##2"); ImGui::SameLine();
//                ImGui::Text("Hello\nWorld"); ImGui::SameLine();
//                ImGui::Text("Banana");
//
//                ImGui::Button("TEST##1"); ImGui::SameLine();
//                ImGui::Text("TEST"); ImGui::SameLine();
//                ImGui::SmallButton("TEST##2");
//
//                ImGui::AlignFirstTextHeightToWidgets(); // If your line starts with text, call this to align it to upcoming widgets.
//                ImGui::Text("Text aligned to Widget"); ImGui::SameLine();
//                ImGui::Button("Widget##1"); ImGui::SameLine();
//                ImGui::Text("Widget"); ImGui::SameLine();
//                ImGui::SmallButton("Widget##2");
//
//                // Tree
//                const float spacing = ImGui::GetStyle().ItemInnerSpacing.x;
//                ImGui::Button("Button##1");
//                ImGui::SameLine(0.0f, spacing);
//                if (ImGui::TreeNode("Node##1")) { for (int i = 0; i < 6; i++) ImGui::BulletText("Item %d..", i); ImGui::TreePop(); }    // Dummy tree data
//
//                ImGui::AlignFirstTextHeightToWidgets();         // Vertically align text node a bit lower so it'll be vertically centered with upcoming widget. Otherwise you can use SmallButton (smaller fit).
//                bool node_open = ImGui::TreeNode("Node##2");  // Common mistake to avoid: if we want to SameLine after TreeNode we need to do it before we add child content.
//                ImGui::SameLine(0.0f, spacing); ImGui::Button("Button##2");
//                if (node_open) { for (int i = 0; i < 6; i++) ImGui::BulletText("Item %d..", i); ImGui::TreePop(); }   // Dummy tree data
//
//                // Bullet
//                ImGui::Button("Button##3");
//                ImGui::SameLine(0.0f, spacing);
//                ImGui::BulletText("Bullet text");
//
//                ImGui::AlignFirstTextHeightToWidgets();
//                ImGui::BulletText("Node");
//                ImGui::SameLine(0.0f, spacing); ImGui::Button("Button##4");
//
//                ImGui::TreePop();
//            }
//
//            if (ImGui::TreeNode("Scrolling"))
//            {
//                ImGui::TextWrapped("(Use SetScrollHere() or SetScrollFromPosY() to scroll to a given position.)");
//                static bool track = true;
//                static int track_line = 50, scroll_to_px = 200;
//                ImGui::Checkbox("Track", &track);
//                ImGui::PushItemWidth(100);
//                ImGui::SameLine(130); track |= ImGui::DragInt("##line", &track_line, 0.25f, 0, 99, "Line %.0f");
//                bool scroll_to = ImGui::Button("Scroll To");
//                ImGui::SameLine(130); scroll_to |= ImGui::DragInt("##pos_y", &scroll_to_px, 1.00f, 0, 9999, "y = %.0f px");
//                ImGui::PopItemWidth();
//                if (scroll_to) track = false;
//
//                for (int i = 0; i < 5; i++)
//                {
//                    if (i > 0) ImGui::SameLine();
//                    ImGui::BeginGroup();
//                    ImGui::Text("%s", i == 0 ? "Top" : i == 1 ? "25%" : i == 2 ? "Center" : i == 3 ? "75%" : "Bottom");
//                    ImGui::BeginChild(ImGui::GetID((void*)(intptr_t)i), ImVec2(ImGui::GetWindowWidth() * 0.17f, 200.0f), true);
//                    if (scroll_to)
//                        ImGui::SetScrollFromPosY(ImGui::GetCursorStartPos().y + scroll_to_px, i * 0.25f);
//                    for (int line = 0; line < 100; line++)
//                    {
//                        if (track && line == track_line)
//                        {
//                            ImGui::TextColored(ImColor(255,255,0), "Line %d", line);
//                            ImGui::SetScrollHere(i * 0.25f); // 0.0f:top, 0.5f:center, 1.0f:bottom
//                        }
//                        else
//                        {
//                            ImGui::Text("Line %d", line);
//                        }
//                    }
//                    ImGui::EndChild();
//                    ImGui::EndGroup();
//                }
//                ImGui::TreePop();
//            }
//
//            if (ImGui::TreeNode("Horizontal Scrolling"))
//            {
//                ImGui::Bullet(); ImGui::TextWrapped("Horizontal scrolling for a window has to be enabled explicitly via the ImGuiWindowFlags_HorizontalScrollbar flag.");
//                ImGui::Bullet(); ImGui::TextWrapped("You may want to explicitly specify content width by calling SetNextWindowContentWidth() before Begin().");
//                static int lines = 7;
//                ImGui::SliderInt("Lines", &lines, 1, 15);
//                ImGui::PushStyleVar(ImGuiStyleVar_FrameRounding, 3.0f);
//                ImGui::PushStyleVar(ImGuiStyleVar_FramePadding, ImVec2(2.0f, 1.0f));
//                ImGui::BeginChild("scrolling", ImVec2(0, ImGui::GetItemsLineHeightWithSpacing()*7 + 30), true, ImGuiWindowFlags_HorizontalScrollbar);
//                for (int line = 0; line < lines; line++)
//                {
//                    // Display random stuff (for the sake of this trivial demo we are using basic Button+SameLine. If you want to create your own time line for a real application you may be better off
//                    // manipulating the cursor position yourself, aka using SetCursorPos/SetCursorScreenPos to position the widgets yourself. You may also want to use the lower-level ImDrawList API)
//                    int num_buttons = 10 + ((line & 1) ? line * 9 : line * 3);
//                    for (int n = 0; n < num_buttons; n++)
//                    {
//                        if (n > 0) ImGui::SameLine();
//                        ImGui::PushID(n + line * 1000);
//                        char num_buf[16];
//                        const char* label = (!(n%15)) ? "FizzBuzz" : (!(n%3)) ? "Fizz" : (!(n%5)) ? "Buzz" : (sprintf(num_buf, "%d", n), num_buf);
//                        float hue = n*0.05f;
//                        ImGui::PushStyleColor(ImGuiCol_Button, ImColor::HSV(hue, 0.6f, 0.6f));
//                        ImGui::PushStyleColor(ImGuiCol_ButtonHovered, ImColor::HSV(hue, 0.7f, 0.7f));
//                        ImGui::PushStyleColor(ImGuiCol_ButtonActive, ImColor::HSV(hue, 0.8f, 0.8f));
//                        ImGui::Button(label, ImVec2(40.0f + sinf((float)(line + n)) * 20.0f, 0.0f));
//                        ImGui::PopStyleColor(3);
//                        ImGui::PopID();
//                    }
//                }
//                ImGui::EndChild();
//                ImGui::PopStyleVar(2);
//                float scroll_x_delta = 0.0f;
//                ImGui::SmallButton("<<"); if (ImGui::IsItemActive()) scroll_x_delta = -ImGui::GetIO().DeltaTime * 1000.0f;
//                ImGui::SameLine(); ImGui::Text("Scroll from code"); ImGui::SameLine();
//                ImGui::SmallButton(">>"); if (ImGui::IsItemActive()) scroll_x_delta = +ImGui::GetIO().DeltaTime * 1000.0f;
//                if (scroll_x_delta != 0.0f)
//                {
//                    ImGui::BeginChild("scrolling"); // Demonstrate a trick: you can use Begin to set yourself in the context of another window (here we are already out of your child window)
//                    ImGui::SetScrollX(ImGui::GetScrollX() + scroll_x_delta);
//                    ImGui::End();
//                }
//                ImGui::TreePop();
//            }
//
//            if (ImGui::TreeNode("Clipping"))
//            {
//                static ImVec2 size(100, 100), offset(50, 20);
//                ImGui::TextWrapped("On a per-widget basis we are occasionally clipping text CPU-side if it won't fit in its frame. Otherwise we are doing coarser clipping + passing a scissor rectangle to the renderer. The system is designed to try minimizing both execution and CPU/GPU rendering cost.");
//                ImGui::DragFloat2("size", (float*)&size, 0.5f, 0.0f, 200.0f, "%.0f");
//                ImGui::TextWrapped("(Click and drag)");
//                ImVec2 pos = ImGui::GetCursorScreenPos();
//                ImVec4 clip_rect(pos.x, pos.y, pos.x+size.x, pos.y+size.y);
//                ImGui::InvisibleButton("##dummy", size);
//                if (ImGui::IsItemActive() && ImGui::IsMouseDragging()) { offset.x += ImGui::GetIO().MouseDelta.x; offset.y += ImGui::GetIO().MouseDelta.y; }
//                ImGui::GetWindowDrawList()->AddRectFilled(pos, ImVec2(pos.x+size.x,pos.y+size.y), ImColor(90,90,120,255));
//                ImGui::GetWindowDrawList()->AddText(ImGui::GetFont(), ImGui::GetFontSize()*2.0f, ImVec2(pos.x+offset.x,pos.y+offset.y), ImColor(255,255,255,255), "Line 1 hello\nLine 2 clip me!", NULL, 0.0f, &clip_rect);
//                ImGui::TreePop();
//            }
        }

        collapsingHeader("Popups & Modal windows") {

            treeNode("Popups") {

                //                ImGui::TextWrapped("When a popup is active, it inhibits interacting with windows that are behind the popup. Clicking outside the popup closes it.");
//
//                static int selected_fish = -1;
//                const char* names[] = { "Bream", "Haddock", "Mackerel", "Pollock", "Tilefish" };
//                static bool toggles[] = { true, false, false, false, false };
//
//                // Simple selection popup
//                // (If you want to show the current selection inside the Button itself, you may want to build a string using the "###" operator to preserve a constant ID with a variable label)
//                if (ImGui::Button("Select.."))
//                    ImGui::OpenPopup("select");
//                ImGui::SameLine();
//                ImGui::Text(selected_fish == -1 ? "<None>" : names[selected_fish]);
//                if (ImGui::BeginPopup("select"))
//                {
//                    ImGui::Text("Aquarium");
//                    ImGui::Separator();
//                    for (int i = 0; i < IM_ARRAYSIZE(names); i++)
//                    if (ImGui::Selectable(names[i]))
//                        selected_fish = i;
//                    ImGui::EndPopup();
//                }
//
//                // Showing a menu with toggles
//                if (ImGui::Button("Toggle.."))
//                    ImGui::OpenPopup("toggle");
//                if (ImGui::BeginPopup("toggle"))
//                {
//                    for (int i = 0; i < IM_ARRAYSIZE(names); i++)
//                    ImGui::MenuItem(names[i], "", &toggles[i]);
//                    if (ImGui::BeginMenu("Sub-menu"))
//                    {
//                        ImGui::MenuItem("Click me");
//                        ImGui::EndMenu();
//                    }
//
//                    ImGui::Separator();
//                    ImGui::Text("Tooltip here");
//                    if (ImGui::IsItemHovered())
//                        ImGui::SetTooltip("I am a tooltip over a popup");
//
//                    if (ImGui::Button("Stacked Popup"))
//                        ImGui::OpenPopup("another popup");
//                    if (ImGui::BeginPopup("another popup"))
//                    {
//                        for (int i = 0; i < IM_ARRAYSIZE(names); i++)
//                        ImGui::MenuItem(names[i], "", &toggles[i]);
//                        if (ImGui::BeginMenu("Sub-menu"))
//                        {
//                            ImGui::MenuItem("Click me");
//                            ImGui::EndMenu();
//                        }
//                        ImGui::EndPopup();
//                    }
//                    ImGui::EndPopup();
//                }
//
//                if (ImGui::Button("Popup Menu.."))
//                    ImGui::OpenPopup("FilePopup");
//                if (ImGui::BeginPopup("FilePopup"))
//                {
//                    ShowExampleMenuFile();
//                    ImGui::EndPopup();
//                }
//
//                ImGui::Spacing();
//                ImGui::TextWrapped("Below we are testing adding menu items to a regular window. It's rather unusual but should work!");
//                ImGui::Separator();
//                // NB: As a quirk in this very specific example, we want to differentiate the parent of this menu from the parent of the various popup menus above.
//                // To do so we are encloding the items in a PushID()/PopID() block to make them two different menusets. If we don't, opening any popup above and hovering our menu here
//                // would open it. This is because once a menu is active, we allow to switch to a sibling menu by just hovering on it, which is the desired behavior for regular menus.
//                ImGui::PushID("foo");
//                ImGui::MenuItem("Menu item", "CTRL+M");
//                if (ImGui::BeginMenu("Menu inside a regular window"))
//                {
//                    ShowExampleMenuFile();
//                    ImGui::EndMenu();
//                }
//                ImGui::PopID();
//                ImGui::Separator();
            }

            treeNode("Context menus") {

                //                static float value = 0.5f;
//                ImGui::Text("Value = %.3f (<-- right-click here)", value);
//                if (ImGui::BeginPopupContextItem("item context menu"))
//                {
//                    if (ImGui::Selectable("Set to zero")) value = 0.0f;
//                    if (ImGui::Selectable("Set to PI")) value = 3.1415f;
//                    ImGui::EndPopup();
//                }
//
//                static ImVec4 color = ImColor(0.8f, 0.5f, 1.0f, 1.0f);
//                ImGui::ColorButton(color);
//                if (ImGui::BeginPopupContextItem("color context menu"))
//                {
//                    ImGui::Text("Edit color");
//                    ImGui::ColorEdit3("##edit", (float*)&color);
//                    if (ImGui::Button("Close"))
//                        ImGui::CloseCurrentPopup();
//                    ImGui::EndPopup();
//                }
//                ImGui::SameLine(); ImGui::Text("(<-- right-click here)");
            }

            treeNode("Modals") {

                textWrapped("Modal windows are like popups but the user cannot close them by clicking outside the window.")

                if (button("Delete..")) {
                    openPopup("Delete?")
                }
                popupModal("Delete?", null, WindowFlags.AlwaysAutoResize.i) {

                    text("All those beautiful files will be deleted.\nThis operation cannot be undone!\n\n")
                    separator()

                    //static int dummy_i = 0;
                    //ImGui::Combo("Combo", &dummy_i, "Delete\0Delete harder\0");

                    withStyleVar(StyleVar.FramePadding, Vec2()) { checkbox("Don't ask me next time", dontAskMeNextTime) }

                    button("OK", Vec2(120, 0)) { closeCurrentPopup() }
                    sameLine()
                    button("Cancel", Vec2(120, 0)) { closeCurrentPopup() }
                }

                button("Stacked modals..") { openPopup("Stacked 1") }
                popupModal("Stacked 1") {

                    text("Hello from Stacked The First")

                    button("Another one..") { openPopup("Stacked 2") }
                    popupModal("Stacked 2") {
                        text("Hello from Stacked The Second")
                        button("Close") { closeCurrentPopup() }
                    }

                    button("Close") { closeCurrentPopup() }
                }
            }
        }
        end()
    }

    /** create metrics window. display ImGui internals: browse window list, draw commands, individual vertices, basic
     *  internal state, etc.    */
    fun showMetricsWindow(pOpen: BooleanArray) = with(ImGui) {

        if (begin("ImGui Metrics", pOpen)) {
            text("ImGui $version")
            text("Application average %.3f ms/frame (%.1f FPS)", 1000f / IO.framerate, IO.framerate)
            text("%d vertices, %d indices (%d triangles)", IO.metricsRenderVertices, IO.metricsRenderIndices, IO.metricsRenderIndices / 3)
            text("%d allocations", IO.metricsAllocs)
            checkbox("Show clipping rectangles when hovering a ImDrawCmd", showClipRects)
            separator()

            Funcs.nodeWindows(g.windows, "Windows")
            if (treeNode("DrawList", "Active DrawLists (${g.renderDrawLists[0].size})")) {
                for (i in g.renderDrawLists[0])
                    Funcs.nodeDrawList(i, "DrawList")
                treePop()
            }
            if (treeNode("Popups", "Open Popups Stack (${g.openPopupStack.size})")) {
                for (popup in g.openPopupStack) {
                    val window = popup.window
                    val childWindow = if (window != null && window.flags has WindowFlags.ChildWindow) " ChildWindow" else ""
                    val childMenu = if (window != null && window.flags has WindowFlags.ChildMenu) " ChildMenu" else ""
                    bulletText("PopupID: %08x, Window: '${window?.name}'$childWindow$childMenu", popup.popupId)
                }
                treePop()
            }
            if (treeNode("Basic state")) {
                text("FocusedWindow: '${g.focusedWindow?.name}'")
                text("HoveredWindow: '${g.hoveredWindow?.name}'")
                text("HoveredRootWindow: '${g.hoveredWindow?.name}'")
                /*  Data is "in-flight" so depending on when the Metrics window is called we may see current frame
                    information or not                 */
                text("HoveredID: 0x%08X/0x%08X", g.hoveredId, g.hoveredIdPreviousFrame)
                text("ActiveID: 0x%08X/0x%08X", g.activeId, g.activeIdPreviousFrame)
                treePop()
            }
        }
        end()
    }

    object Funcs {

        fun nodeDrawList(drawList: DrawList, label: String) {

            val nodeOpen = treeNode(drawList, "$label: '${drawList._ownerName}' ${drawList.vtxBuffer.size} vtx, " +
                    "${drawList.idxBuffer.size} indices, ${drawList.cmdBuffer.size} cmds")
            if (drawList === windowDrawList) {
                sameLine()
                // Can't display stats for active draw list! (we don't have the data double-buffered)
                textColored(Vec4.fromColor(255, 100, 100), "CURRENTLY APPENDING")
                if (nodeOpen) treePop()
                return
            }
            if (!nodeOpen)
                return

            val overlayDrawList = g.overlayDrawList   // Render additional visuals into the top-most draw list
            overlayDrawList.pushClipRectFullScreen()
            var elemOffset = 0
            for (i in drawList.cmdBuffer.indices) {
                val cmd = drawList.cmdBuffer[i]
                if (cmd.userCallback != null) {
                    TODO()
//                        ImGui::BulletText("Callback %p, user_data %p", pcmd->UserCallback, pcmd->UserCallbackData)
//                        continue
                }
                val idxBuffer = drawList.idxBuffer.takeIf { it.isNotEmpty() }
                val mode = if (drawList.idxBuffer.isNotEmpty()) "indexed" else "non-indexed"
                val cmdNodeOpen = treeNode(i, "Draw %-4d $mode vtx, tex = ${cmd.textureId}, clip_rect = (%.0f,%.0f)..(%.0f,%.0f)",
                        cmd.elemCount, cmd.clipRect.x, cmd.clipRect.y, cmd.clipRect.z, cmd.clipRect.w)
                if (showClipRects[0] && isItemHovered()) {
                    val clipRect = Rect(cmd.clipRect)
                    val vtxsRect = Rect()
                    for (e in elemOffset until elemOffset + cmd.elemCount)
                        vtxsRect.add(drawList.vtxBuffer[idxBuffer?.get(e) ?: e].pos)
                    clipRect.floor(); overlayDrawList.addRect(clipRect.min, clipRect.max, COL32(255, 255, 0, 255))
                    vtxsRect.floor(); overlayDrawList.addRect(vtxsRect.min, vtxsRect.max, COL32(255, 0, 255, 255))
                }
                if (!cmdNodeOpen) continue
                // Manually coarse clip our print out of individual vertices to save CPU, only items that may be visible.
                val clipper = ListClipper(cmd.elemCount / 3)
                while (clipper.step()) {
                    var vtxI = elemOffset + clipper.display.start * 3
                    for (prim in clipper.display.start until clipper.display.last) {
                        val buf = CharArray(300)
                        var bufP = 0
                        val trianglesPos = arrayListOf(Vec2(), Vec2(), Vec2())
                        for (n in 0 until 3) {
                            val v = drawList.vtxBuffer[idxBuffer?.get(vtxI) ?: vtxI]
                            trianglesPos[n] = v.pos
                            val name = if (n == 0) "vtx" else "   "
                            val string = "$name %04d { pos = (%8.2f,%8.2f), uv = (%.6f,%.6f), col = %08X }\n".format(Style.locale,
                                    vtxI, v.pos.x, v.pos.y, v.uv.x, v.uv.y, v.col)
                            string.toCharArray(buf, bufP)
                            bufP += string.length
                            vtxI++
                        }
                        selectable(buf.joinToString("", limit = bufP, truncated = ""), false)
                        if (isItemHovered())
                        // Add triangle without AA, more readable for large-thin triangle
                            overlayDrawList.addPolyline(trianglesPos, COL32(255, 255, 0, 255), true, 1f, false)
                    }
                }
                treePop()
                elemOffset += cmd.elemCount
            }
            overlayDrawList.popClipRect()
            treePop()
        }

        fun nodeWindows(windows: ArrayList<Window>, label: String) {
            if (!treeNode(label, "$label (${windows.size})"))
                return
            for (i in 0 until windows.size)
                nodeWindow(windows[i], "Window")
            treePop()
        }

        fun nodeWindow(window: Window, label: String) {
            val active = if (window.active or window.wasActive) "active" else "inactive"
            if (!treeNode(window, "$label '${window.name}', $active @ 0x%X", System.identityHashCode(window)))
                return
            nodeDrawList(window.drawList, "DrawList")
            bulletText("Pos: (%.1f,%.1f)", window.pos.x.f, window.pos.y.f)
            bulletText("Size: (%.1f,%.1f), SizeContents (%.1f,%.1f)", window.size.x, window.size.y, window.sizeContents.x, window.sizeContents.y)
            bulletText("Scroll: (%.2f,%.2f)", window.scroll.x, window.scroll.y)
            if (window.rootWindow !== window) nodeWindow(window.rootWindow, "RootWindow")
            if (window.dc.childWindows.isNotEmpty()) nodeWindows(window.dc.childWindows, "ChildWindows")
            bulletText("Storage: %d bytes", window.stateStorage.data.size * Int.BYTES * 2)
            treePop()
        }

        fun showDummyObject(prefix: String, uid: Int) {
//            println("showDummyObject $prefix _$uid")
            //  Use object uid as identifier. Most commonly you could also use the object pointer as a base ID.
            pushId(uid)
            /*  Text and Tree nodes are less high than regular widgets, here we add vertical spacing to make the tree
                lines equal high.             */
            alignFirstTextHeightToWidgets()
            val nodeOpen = treeNode("Object", "${prefix}_$uid")
            nextColumn()
            alignFirstTextHeightToWidgets()
            text("my sailor is rich")
            nextColumn()
            if (nodeOpen) {
                for (i in 0..7) {
                    pushId(i) // Use field index as identifier.
                    if (i < 2)
                        showDummyObject("Child", 424242)
                    else {
                        alignFirstTextHeightToWidgets()
                        // Here we use a Selectable (instead of Text) to highlight on hover
                        //Text("Field_%d", i);
                        bullet()
                        selectable("Field_$i")
                        nextColumn()
                        pushItemWidth(-1f)
                        if (i >= 5)
                            inputFloat("##value", dummyMembers, i, 1f)
                        else
                            dragFloat("##value", dummyMembers, i, 0.01f)
                        popItemWidth()
                        nextColumn()
                    }
                    popId()
                }
                treePop()
            }
            popId()
        }

        val dummyMembers = floatArrayOf(0f, 0f, 1f, 3.1416f, 100f, 999f, 0f, 0f, 0f)
    }

    fun showStyleEditor() {
        TODO()
//        ImGuiStyle& style = ImGui::GetStyle();
//
//        // You can pass in a reference ImGuiStyle structure to compare to, revert to and save to (else it compares to the default style)
//        const ImGuiStyle default_style; // Default style
//        if (ImGui::Button("Revert Style"))
//            style = ref ? *ref : default_style;
//
//        if (ref)
//        {
//            ImGui::SameLine();
//            if (ImGui::Button("Save Style"))
//            *ref = style;
//        }
//
//        ImGui::PushItemWidth(ImGui::GetWindowWidth() * 0.55f);
//
//        if (ImGui::TreeNode("Rendering"))
//        {
//            ImGui::Checkbox("Anti-aliased lines", &style.AntiAliasedLines);
//            ImGui::Checkbox("Anti-aliased shapes", &style.AntiAliasedShapes);
//            ImGui::PushItemWidth(100);
//            ImGui::DragFloat("Curve Tessellation Tolerance", &style.CurveTessellationTol, 0.02f, 0.10f, FLT_MAX, NULL, 2.0f);
//            if (style.CurveTessellationTol < 0.0f) style.CurveTessellationTol = 0.10f;
//            ImGui::DragFloat("Global Alpha", &style.Alpha, 0.005f, 0.20f, 1.0f, "%.2f"); // Not exposing zero here so user doesn't "lose" the UI (zero alpha clips all widgets). But application code could have a toggle to switch between zero and non-zero.
//            ImGui::PopItemWidth();
//            ImGui::TreePop();
//        }
//
//        if (ImGui::TreeNode("Settings"))
//        {
//            ImGui::SliderFloat2("WindowPadding", (float*)&style.WindowPadding, 0.0f, 20.0f, "%.0f");
//            ImGui::SliderFloat("WindowRounding", &style.WindowRounding, 0.0f, 16.0f, "%.0f");
//            ImGui::SliderFloat("ChildWindowRounding", &style.ChildWindowRounding, 0.0f, 16.0f, "%.0f");
//            ImGui::SliderFloat2("FramePadding", (float*)&style.FramePadding, 0.0f, 20.0f, "%.0f");
//            ImGui::SliderFloat("FrameRounding", &style.FrameRounding, 0.0f, 16.0f, "%.0f");
//            ImGui::SliderFloat2("ItemSpacing", (float*)&style.ItemSpacing, 0.0f, 20.0f, "%.0f");
//            ImGui::SliderFloat2("ItemInnerSpacing", (float*)&style.ItemInnerSpacing, 0.0f, 20.0f, "%.0f");
//            ImGui::SliderFloat2("TouchExtraPadding", (float*)&style.TouchExtraPadding, 0.0f, 10.0f, "%.0f");
//            ImGui::SliderFloat("IndentSpacing", &style.IndentSpacing, 0.0f, 30.0f, "%.0f");
//            ImGui::SliderFloat("ScrollbarSize", &style.ScrollbarSize, 1.0f, 20.0f, "%.0f");
//            ImGui::SliderFloat("ScrollbarRounding", &style.ScrollbarRounding, 0.0f, 16.0f, "%.0f");
//            ImGui::SliderFloat("GrabMinSize", &style.GrabMinSize, 1.0f, 20.0f, "%.0f");
//            ImGui::SliderFloat("GrabRounding", &style.GrabRounding, 0.0f, 16.0f, "%.0f");
//            ImGui::Text("Alignment");
//            ImGui::SliderFloat2("WindowTitleAlign", (float*)&style.WindowTitleAlign, 0.0f, 1.0f, "%.2f");
//            ImGui::SliderFloat2("ButtonTextAlign", (float*)&style.ButtonTextAlign, 0.0f, 1.0f, "%.2f"); ImGui::SameLine(); ShowHelpMarker("Alignment applies when a button is larger than its text content.");
//            ImGui::TreePop();
//        }
//
//        if (ImGui::TreeNode("Colors"))
//        {
//            static int output_dest = 0;
//            static bool output_only_modified = false;
//            if (ImGui::Button("Copy Colors"))
//            {
//                if (output_dest == 0)
//                    ImGui::LogToClipboard();
//                else
//                    ImGui::LogToTTY();
//                ImGui::LogText("ImGuiStyle& style = ImGui::GetStyle();" IM_NEWLINE);
//                for (int i = 0; i < ImGuiCol_COUNT; i++)
//                {
//                    const ImVec4& col = style.Colors[i];
//                    const char* name = ImGui::GetStyleColName(i);
//                    if (!output_only_modified || memcmp(&col, (ref ? &ref->Colors[i] : &default_style.Colors[i]), sizeof(ImVec4)) != 0)
//                    ImGui::LogText("style.Colors[ImGuiCol_%s]%*s= ImVec4(%.2ff, %.2ff, %.2ff, %.2ff);" IM_NEWLINE, name, 22 - (int)strlen(name), "", col.x, col.y, col.z, col.w);
//                }
//                ImGui::LogFinish();
//            }
//            ImGui::SameLine(); ImGui::PushItemWidth(120); ImGui::Combo("##output_type", &output_dest, "To Clipboard\0To TTY\0"); ImGui::PopItemWidth();
//            ImGui::SameLine(); ImGui::Checkbox("Only Modified Fields", &output_only_modified);
//
//            static ImGuiColorEditMode edit_mode = ImGuiColorEditMode_RGB;
//            ImGui::RadioButton("RGB", &edit_mode, ImGuiColorEditMode_RGB);
//            ImGui::SameLine();
//            ImGui::RadioButton("HSV", &edit_mode, ImGuiColorEditMode_HSV);
//            ImGui::SameLine();
//            ImGui::RadioButton("HEX", &edit_mode, ImGuiColorEditMode_HEX);
//            //ImGui::Text("Tip: Click on colored square to change edit mode.");
//
//            static ImGuiTextFilter filter;
//            filter.Draw("Filter colors", 200);
//
//            ImGui::BeginChild("#colors", ImVec2(0, 300), true, ImGuiWindowFlags_AlwaysVerticalScrollbar);
//            ImGui::PushItemWidth(-160);
//            ImGui::ColorEditMode(edit_mode);
//            for (int i = 0; i < ImGuiCol_COUNT; i++)
//            {
//                const char* name = ImGui::GetStyleColName(i);
//                if (!filter.PassFilter(name))
//                    continue;
//                ImGui::PushID(i);
//                ImGui::ColorEdit4(name, (float*)&style.Colors[i], true);
//                if (memcmp(&style.Colors[i], (ref ? &ref->Colors[i] : &default_style.Colors[i]), sizeof(ImVec4)) != 0)
//                {
//                    ImGui::SameLine(); if (ImGui::Button("Revert")) style.Colors[i] = ref ? ref->Colors[i] : default_style.Colors[i];
//                    if (ref) { ImGui::SameLine(); if (ImGui::Button("Save")) ref->Colors[i] = style.Colors[i]; }
//                }
//                ImGui::PopID();
//            }
//            ImGui::PopItemWidth();
//            ImGui::EndChild();
//
//            ImGui::TreePop();
//        }
//
//        if (ImGui::TreeNode("Fonts", "Fonts (%d)", ImGui::GetIO().Fonts->Fonts.Size))
//        {
//            ImGui::SameLine(); ShowHelpMarker("Tip: Load fonts with io.Fonts->AddFontFromFileTTF()\nbefore calling io.Fonts->GetTex* functions.");
//            ImFontAtlas* atlas = ImGui::GetIO().Fonts;
//            if (ImGui::TreeNode("Atlas texture", "Atlas texture (%dx%d pixels)", atlas->TexWidth, atlas->TexHeight))
//            {
//                ImGui::Image(atlas->TexID, ImVec2((float)atlas->TexWidth, (float)atlas->TexHeight), ImVec2(0,0), ImVec2(1,1), ImColor(255,255,255,255), ImColor(255,255,255,128));
//                ImGui::TreePop();
//            }
//            ImGui::PushItemWidth(100);
//            for (int i = 0; i < atlas->Fonts.Size; i++)
//            {
//                ImFont* font = atlas->Fonts[i];
//                ImGui::BulletText("Font %d: \'%s\', %.2f px, %d glyphs", i, font->ConfigData ? font->ConfigData[0].Name : "", font->FontSize, font->Glyphs.Size);
//                ImGui::TreePush((void*)(intptr_t)i);
//                ImGui::SameLine(); if (ImGui::SmallButton("Set as default")) ImGui::GetIO().FontDefault = font;
//                ImGui::PushFont(font);
//                ImGui::Text("The quick brown fox jumps over the lazy dog");
//                ImGui::PopFont();
//                if (ImGui::TreeNode("Details"))
//                {
//                    ImGui::DragFloat("Font scale", &font->Scale, 0.005f, 0.3f, 2.0f, "%.1f");   // Scale only this font
//                    ImGui::SameLine(); ShowHelpMarker("Note than the default embedded font is NOT meant to be scaled.\n\nFont are currently rendered into bitmaps at a given size at the time of building the atlas. You may oversample them to get some flexibility with scaling. You can also render at multiple sizes and select which one to use at runtime.\n\n(Glimmer of hope: the atlas system should hopefully be rewritten in the future to make scaling more natural and automatic.)");
//                    ImGui::Text("Ascent: %f, Descent: %f, Height: %f", font->Ascent, font->Descent, font->Ascent - font->Descent);
//                    ImGui::Text("Fallback character: '%c' (%d)", font->FallbackChar, font->FallbackChar);
//                    ImGui::Text("Texture surface: %d pixels (approx) ~ %dx%d", font->MetricsTotalSurface, (int)sqrtf((float)font->MetricsTotalSurface), (int)sqrtf((float)font->MetricsTotalSurface));
//                    for (int config_i = 0; config_i < font->ConfigDataCount; config_i++)
//                    {
//                        ImFontConfig* cfg = &font->ConfigData[config_i];
//                        ImGui::BulletText("Input %d: \'%s\', Oversample: (%d,%d), PixelSnapH: %d", config_i, cfg->Name, cfg->OversampleH, cfg->OversampleV, cfg->PixelSnapH);
//                    }
//                    if (ImGui::TreeNode("Glyphs", "Glyphs (%d)", font->Glyphs.Size))
//                    {
//                        // Display all glyphs of the fonts in separate pages of 256 characters
//                        const ImFont::Glyph* glyph_fallback = font->FallbackGlyph; // Forcefully/dodgily make FindGlyph() return NULL on fallback, which isn't the default behavior.
//                        font->FallbackGlyph = NULL;
//                        for (int base = 0; base < 0x10000; base += 256)
//                        {
//                            int count = 0;
//                            for (int n = 0; n < 256; n++)
//                            count += font->FindGlyph((ImWchar)(base + n)) ? 1 : 0;
//                            if (count > 0 && ImGui::TreeNode((void*)(intptr_t)base, "U+%04X..U+%04X (%d %s)", base, base+255, count, count > 1 ? "glyphs" : "glyph"))
//                            {
//                                float cell_spacing = style.ItemSpacing.y;
//                                ImVec2 cell_size(font->FontSize * 1, font->FontSize * 1);
//                                ImVec2 base_pos = ImGui::GetCursorScreenPos();
//                                ImDrawList* draw_list = ImGui::GetWindowDrawList();
//                                for (int n = 0; n < 256; n++)
//                                {
//                                    ImVec2 cell_p1(base_pos.x + (n % 16) * (cell_size.x + cell_spacing), base_pos.y + (n / 16) * (cell_size.y + cell_spacing));
//                                    ImVec2 cell_p2(cell_p1.x + cell_size.x, cell_p1.y + cell_size.y);
//                                    const ImFont::Glyph* glyph = font->FindGlyph((ImWchar)(base+n));;
//                                    draw_list->AddRect(cell_p1, cell_p2, glyph ? IM_COL32(255,255,255,100) : IM_COL32(255,255,255,50));
//                                    font->RenderChar(draw_list, cell_size.x, cell_p1, ImGui::GetColorU32(ImGuiCol_Text), (ImWchar)(base+n)); // We use ImFont::RenderChar as a shortcut because we don't have UTF-8 conversion functions available to generate a string.
//                                    if (glyph && ImGui::IsMouseHoveringRect(cell_p1, cell_p2))
//                                    {
//                                        ImGui::BeginTooltip();
//                                        ImGui::Text("Codepoint: U+%04X", base+n);
//                                        ImGui::Separator();
//                                        ImGui::Text("XAdvance+1: %.1f", glyph->XAdvance);
//                                        ImGui::Text("Pos: (%.2f,%.2f)->(%.2f,%.2f)", glyph->X0, glyph->Y0, glyph->X1, glyph->Y1);
//                                        ImGui::Text("UV: (%.3f,%.3f)->(%.3f,%.3f)", glyph->U0, glyph->V0, glyph->U1, glyph->V1);
//                                        ImGui::EndTooltip();
//                                    }
//                                }
//                                ImGui::Dummy(ImVec2((cell_size.x + cell_spacing) * 16, (cell_size.y + cell_spacing) * 16));
//                                ImGui::TreePop();
//                            }
//                        }
//                        font->FallbackGlyph = glyph_fallback;
//                        ImGui::TreePop();
//                    }
//                    ImGui::TreePop();
//                }
//                ImGui::TreePop();
//            }
//            static float window_scale = 1.0f;
//            ImGui::DragFloat("this window scale", &window_scale, 0.005f, 0.3f, 2.0f, "%.1f");              // scale only this window
//            ImGui::DragFloat("global scale", &ImGui::GetIO().FontGlobalScale, 0.005f, 0.3f, 2.0f, "%.1f"); // scale everything
//            ImGui::PopItemWidth();
//            ImGui::SetWindowFontScale(window_scale);
//            ImGui::TreePop();
//        }
//
//        ImGui::PopItemWidth();
    }

    fun showUserGuide() {
        bulletText("Double-click on title bar to collapse window.")
        bulletText("Click and drag on lower right corner to resize window.")
        bulletText("Click and drag on any empty space to move window.")
        bulletText("Mouse Wheel to scroll.")
        if (IO.fontAllowUserScaling)
            bulletText("CTRL+Mouse Wheel to zoom window contents.")
        bulletText("TAB/SHIFT+TAB to cycle through keyboard editable fields.")
        bulletText("CTRL+Click on a slider or drag box to input text.")
        bulletText(
                "While editing text:\n" +
                        "- Hold SHIFT or use mouse to select text\n" +
                        "- CTRL+Left/Right to word jump\n" +
                        "- CTRL+A or double-click to select all\n" +
                        "- CTRL+X,CTRL+C,CTRL+V clipboard\n" +
                        "- CTRL+Z,CTRL+Y undo/redo\n" +
                        "- ESCAPE to revert\n" +
                        "- You can apply arithmetic operators +,*,/ on numerical values.\n" +
                        "  Use +- to subtract.\n")
    }

    companion object {

        fun showHelpMarker(desc: String) {
            textDisabled("(?)")
            if (isItemHovered()) {
                beginTooltip()
                pushTextWrapPos(450f)
                textUnformatted(desc)
                popTextWrapPos()
                endTooltip()
            }
        }

        /** Demonstrate creating a fullscreen menu bar and populating it.   */
        fun showExampleAppMainMenuBar() = with(ImGui) {

            if (beginMainMenuBar()) {
                if (beginMenu("File")) {
                    showExampleMenuFile()
                    endMenu()
                }
                if (beginMenu("Edit")) {
                    if (menuItem("Undo", "CTRL+Z")) {
                    }
                    if (menuItem("Redo", "CTRL+Y", false, false)) { // Disabled item
                    }
                    separator()
                    if (menuItem("Cut", "CTRL+X")) {
                    }
                    if (menuItem("Copy", "CTRL+C")) {
                    }
                    if (menuItem("Paste", "CTRL+V")) {
                    }
                    endMenu()
                }
                endMainMenuBar()
            }
        }

        fun showExampleMenuFile() {

            menuItem("(dummy menu)", "", false, false)
            if (menuItem("New")) {
            }
            if (menuItem("Open", "Ctrl+O")) {
            }
            if (beginMenu("Open Recent")) {
                menuItem("fish_hat.c")
                menuItem("fish_hat.inl")
                menuItem("fish_hat.h")
                if (beginMenu("More..")) {
                    menuItem("Hello")
                    menuItem("Sailor")
                    if (beginMenu("Recurse..")) {
                        showExampleMenuFile()
                        endMenu()
                    }
                    endMenu()
                }
                endMenu()
            }
            if (menuItem("Save", "Ctrl+S")) {
            }
            if (menuItem("Save As..")) {
            }
            separator()
            if (beginMenu("Options")) {
                menuItem("Enabled", "", enabled)
                beginChild("child", Vec2(0, 60), true)
                for (i in 0 until 10)
                    text("Scrolling Text %d", i)
                endChild()
                sliderFloat("Value", f, 0f, 1f)
                inputFloat("Input", f, 0.1f, 0f, 2)
                combo("Combo", n, "Yes\u0000No\u0000Maybe\u0000\u0000")
                endMenu()
            }
            if (beginMenu("Colors")) {
                for (col in Col.values())
                    menuItem(col.toString())
                endMenu()
            }
            if (beginMenu("Disabled", false)) // Disabled
                assert(false)
            if (menuItem("Checked", selected = true)) {
            }
            if (menuItem("Quit", "Alt+F4")) {
            }
        }

        var enabled = booleanArrayOf(true)
        var f = floatArrayOf(0.5f)
        var n = intArrayOf(0)

        /** Demonstrate creating a window which gets auto-resized according to its content. */
        fun showExampleAppAutoResize(pOpen: BooleanArray) {

            if (!begin("Example: Auto-resizing window", pOpen, WindowFlags.AlwaysAutoResize.i)) {
                end()
                return
            }

            text("Window will resize every-frame to the size of its content.\nNote that you probably don't want to " +
                    "query the window size to\noutput your content because that would create a feedback loop.")
            sliderInt("Number of lines", lines, 1, 20)
            for (i in 0 until lines[0])
                text(" ".repeat(i * 4) + "This is line $i") // Pad with space to extend size horizontally
            end()
        }

        var lines = intArrayOf(10)

        /** Demonstrate creating a window with custom resize constraints.   */
        fun showExampleAppConstrainedResize(pOpen: BooleanArray) {

            when (type[0]) {
                0 -> setNextWindowSizeConstraints(Vec2(-1, 0), Vec2(-1, Float.MAX_VALUE))      // Vertical only
                1 -> setNextWindowSizeConstraints(Vec2(0, -1), Vec2(Float.MAX_VALUE, -1))      // Horizontal only
                2 -> setNextWindowSizeConstraints(Vec2(100), Vec2(Float.MAX_VALUE)) // Width > 100, Height > 100
                3 -> setNextWindowSizeConstraints(Vec2(300, 0), Vec2(400, Float.MAX_VALUE))     // Width 300-400
                4 -> setNextWindowSizeConstraints(Vec2(), Vec2(Float.MAX_VALUE), CustomConstraints.square)          // Always Square
                5 -> setNextWindowSizeConstraints(Vec2(), Vec2(Float.MAX_VALUE), CustomConstraints.step, 100)// Fixed Step
            }

            if (begin("Example: Constrained Resize", pOpen)) {
                val desc = arrayOf("Resize vertical only", "Resize horizontal only", "Width > 100, Height > 100",
                        "Width 300-400", "Custom: Always Square", "Custom: Fixed Steps (100)")
                combo("Constraint", type, desc)
                button("200x200") { setWindowSize(Vec2(200)); sameLine() }
                button("500x500") { setWindowSize(Vec2(500)); sameLine() }
                button("800x200") { setWindowSize(Vec2(800, 200)) }
                for (i in 0 until 10)
                    text("Hello, sailor! Making this line long enough for the example.")
            }
            end()
        }

        // Helper functions to demonstrate programmatic constraints
        object CustomConstraints {
            val square: SizeConstraintCallback = { _: Any?, _: Vec2i, _: Vec2, desiredSize: Vec2 ->
                desiredSize put glm.max(desiredSize.x, desiredSize.y)
            }
            val step: SizeConstraintCallback = { userData: Any?, _: Vec2i, _: Vec2, desiredSize: Vec2 ->
                val step = (userData as Int).f
                desiredSize.x = (desiredSize.x / step + 0.5f).i * step
                desiredSize.y = (desiredSize.y / step + 0.5f).i * step
            }
        }

        var type = intArrayOf(0)

        /** Demonstrate creating a simple static window with no decoration. */
        fun showExampleAppFixedOverlay(pOpen: BooleanArray) {

            setNextWindowPos(Vec2(10))
            val flags = WindowFlags.NoTitleBar or WindowFlags.NoResize or WindowFlags.NoMove or WindowFlags.NoSavedSettings
            if (!begin("Example: Fixed Overlay", pOpen, Vec2(), 0.3f, flags)) {
                end()
                return
            }
            text("Simple overlay\non the top-left side of the screen.")
            separator()
            text("Mouse Position: (%.1f,%.1f)", IO.mousePos.x, IO.mousePos.y)
            end()
        }

        /** Demonstrate using "##" and "###" in identifiers to manipulate ID generation.
         *  Read section "How can I have multiple widgets with the same label? Can I have widget without a label? (Yes).
         *  A primer on the purpose of labels/IDs." about ID.   */
        fun showExampleAppManipulatingWindowTitle(p: BooleanArray) {
            TODO()
            // By default, Windows are uniquely identified by their title.
            // You can use the "##" and "###" markers to manipulate the display/ID.

            // Using "##" to display same title but have unique identifier.
//            ImGui::SetNextWindowPos(ImVec2(100,100), ImGuiSetCond_FirstUseEver);
//            ImGui::Begin("Same title as another window##1");
//            ImGui::Text("This is window 1.\nMy title is the same as window 2, but my identifier is unique.");
//            ImGui::End();
//
//            ImGui::SetNextWindowPos(ImVec2(100,200), ImGuiSetCond_FirstUseEver);
//            ImGui::Begin("Same title as another window##2");
//            ImGui::Text("This is window 2.\nMy title is the same as window 1, but my identifier is unique.");
//            ImGui::End();
//
//            // Using "###" to display a changing title but keep a static identifier "AnimatedTitle"
//            char buf[128];
//            sprintf(buf, "Animated title %c %d###AnimatedTitle", "|/-\\"[(int)(ImGui::GetTime()/0.25f)&3], rand());
//            ImGui::SetNextWindowPos(ImVec2(100,300), ImGuiSetCond_FirstUseEver);
//            ImGui::Begin(buf);
//            ImGui::Text("This window has a changing title.");
//            ImGui::End();
        }

        /** Demonstrate using the low-level ImDrawList to draw custom shapes.   */
        fun showExampleAppCustomRendering(pOpen: BooleanArray) {
//            ImGui::SetNextWindowSize(ImVec2(350,560), ImGuiSetCond_FirstUseEver);
//            if (!ImGui::Begin("Example: Custom rendering", p_open))
//            {
//                ImGui::End();
//                return;
//            }
//
//            // Tip: If you do a lot of custom rendering, you probably want to use your own geometrical types and benefit of overloaded operators, etc.
//            // Define IM_VEC2_CLASS_EXTRA in imconfig.h to create implicit conversions between your types and ImVec2/ImVec4.
//            // ImGui defines overloaded operators but they are internal to imgui.cpp and not exposed outside (to avoid messing with your types)
//            // In this example we are not using the maths operators!
//            ImDrawList* draw_list = ImGui::GetWindowDrawList();
//
//            // Primitives
//            ImGui::Text("Primitives");
//            static float sz = 36.0f;
//            static ImVec4 col = ImVec4(1.0f,1.0f,0.4f,1.0f);
//            ImGui::DragFloat("Size", &sz, 0.2f, 2.0f, 72.0f, "%.0f");
//            ImGui::ColorEdit3("Color", &col.x);
//            {
//                const ImVec2 p = ImGui::GetCursorScreenPos();
//                const ImU32 col32 = ImColor(col);
//                float x = p.x + 4.0f, y = p.y + 4.0f, spacing = 8.0f;
//                for (int n = 0; n < 2; n++)
//                {
//                    float thickness = (n == 0) ? 1.0f : 4.0f;
//                    draw_list->AddCircle(ImVec2(x+sz*0.5f, y+sz*0.5f), sz*0.5f, col32, 20, thickness); x += sz+spacing;
//                    draw_list->AddRect(ImVec2(x, y), ImVec2(x+sz, y+sz), col32, 0.0f, ~0, thickness); x += sz+spacing;
//                    draw_list->AddRect(ImVec2(x, y), ImVec2(x+sz, y+sz), col32, 10.0f, ~0, thickness); x += sz+spacing;
//                    draw_list->AddTriangle(ImVec2(x+sz*0.5f, y), ImVec2(x+sz,y+sz-0.5f), ImVec2(x,y+sz-0.5f), col32, thickness); x += sz+spacing;
//                    draw_list->AddLine(ImVec2(x, y), ImVec2(x+sz, y   ), col32, thickness); x += sz+spacing;
//                    draw_list->AddLine(ImVec2(x, y), ImVec2(x+sz, y+sz), col32, thickness); x += sz+spacing;
//                    draw_list->AddLine(ImVec2(x, y), ImVec2(x,    y+sz), col32, thickness); x += spacing;
//                    draw_list->AddBezierCurve(ImVec2(x, y), ImVec2(x+sz*1.3f,y+sz*0.3f), ImVec2(x+sz-sz*1.3f,y+sz-sz*0.3f), ImVec2(x+sz, y+sz), col32, thickness);
//                    x = p.x + 4;
//                    y += sz+spacing;
//                }
//                draw_list->AddCircleFilled(ImVec2(x+sz*0.5f, y+sz*0.5f), sz*0.5f, col32, 32); x += sz+spacing;
//                draw_list->AddRectFilled(ImVec2(x, y), ImVec2(x+sz, y+sz), col32); x += sz+spacing;
//                draw_list->AddRectFilled(ImVec2(x, y), ImVec2(x+sz, y+sz), col32, 10.0f); x += sz+spacing;
//                draw_list->AddTriangleFilled(ImVec2(x+sz*0.5f, y), ImVec2(x+sz,y+sz-0.5f), ImVec2(x,y+sz-0.5f), col32); x += sz+spacing;
//                draw_list->AddRectFilledMultiColor(ImVec2(x, y), ImVec2(x+sz, y+sz), ImColor(0,0,0), ImColor(255,0,0), ImColor(255,255,0), ImColor(0,255,0));
//                ImGui::Dummy(ImVec2((sz+spacing)*8, (sz+spacing)*3));
//            }
//            ImGui::Separator();
//            {
//                static ImVector<ImVec2> points;
//                static bool adding_line = false;
//                ImGui::Text("Canvas example");
//                if (ImGui::Button("Clear")) points.clear();
//                if (points.Size >= 2) { ImGui::SameLine(); if (ImGui::Button("Undo")) { points.pop_back(); points.pop_back(); } }
//                ImGui::Text("Left-click and drag to add lines,\nRight-click to undo");
//
//                // Here we are using InvisibleButton() as a convenience to 1) advance the cursor and 2) allows us to use IsItemHovered()
//                // However you can draw directly and poll mouse/keyboard by yourself. You can manipulate the cursor using GetCursorPos() and SetCursorPos().
//                // If you only use the ImDrawList API, you can notify the owner window of its extends by using SetCursorPos(max).
//                ImVec2 canvas_pos = ImGui::GetCursorScreenPos();            // ImDrawList API uses screen coordinates!
//                ImVec2 canvas_size = ImGui::GetContentRegionAvail();        // Resize canvas to what's available
//                if (canvas_size.x < 50.0f) canvas_size.x = 50.0f;
//                if (canvas_size.y < 50.0f) canvas_size.y = 50.0f;
//                draw_list->AddRectFilledMultiColor(canvas_pos, ImVec2(canvas_pos.x + canvas_size.x, canvas_pos.y + canvas_size.y), ImColor(50,50,50), ImColor(50,50,60), ImColor(60,60,70), ImColor(50,50,60));
//                draw_list->AddRect(canvas_pos, ImVec2(canvas_pos.x + canvas_size.x, canvas_pos.y + canvas_size.y), ImColor(255,255,255));
//
//                bool adding_preview = false;
//                ImGui::InvisibleButton("canvas", canvas_size);
//                ImVec2 mouse_pos_in_canvas = ImVec2(ImGui::GetIO().MousePos.x - canvas_pos.x, ImGui::GetIO().MousePos.y - canvas_pos.y);
//                if (adding_line)
//                {
//                    adding_preview = true;
//                    points.push_back(mouse_pos_in_canvas);
//                    if (!ImGui::GetIO().MouseDown[0])
//                        adding_line = adding_preview = false;
//                }
//                if (ImGui::IsItemHovered())
//                {
//                    if (!adding_line && ImGui::IsMouseClicked(0))
//                    {
//                        points.push_back(mouse_pos_in_canvas);
//                        adding_line = true;
//                    }
//                    if (ImGui::IsMouseClicked(1) && !points.empty())
//                    {
//                        adding_line = adding_preview = false;
//                        points.pop_back();
//                        points.pop_back();
//                    }
//                }
//                draw_list->PushClipRect(canvas_pos, ImVec2(canvas_pos.x+canvas_size.x, canvas_pos.y+canvas_size.y));      // clip lines within the canvas (if we resize it, etc.)
//                for (int i = 0; i < points.Size - 1; i += 2)
//                draw_list->AddLine(ImVec2(canvas_pos.x + points[i].x, canvas_pos.y + points[i].y), ImVec2(canvas_pos.x + points[i+1].x, canvas_pos.y + points[i+1].y), IM_COL32(255,255,0,255), 2.0f);
//                draw_list->PopClipRect();
//                if (adding_preview)
//                    points.pop_back();
//            }
//            ImGui::End();
        }

        fun showExampleAppConsole(pOpen: BooleanArray) = console.draw("Example: Console", pOpen)

        val console = ExampleAppConsole()

        /** Demonstrate creating a simple log window with basic filtering.  */
        fun showExampleAppLog(pOpen: BooleanArray) {

            // Demo fill
            val time = ImGui.time
            if (time - lastTime >= 0.3f) {
                val s = randomWords[rand % randomWords.size]
                val t = "%.1f".format(Style.locale, time)
                log.addLog("[$s] Hello, time is $t, rand() $rand\n")
                lastTime = time
            }
            log.draw("Example: Log (Filter not yet implemented)", pOpen)
        }

        val log = ExampleAppLog()
        var lastTime = -1f
        val randomWords = arrayOf("system", "info", "warning", "error", "fatal", "notice", "log")
        val random = Random()
        val rand get() = glm.abs(random.nextInt() / 100_000)

        /** Demonstrate create a window with multiple child windows.    */
        fun showExampleAppLayout(pOpen: BooleanArray) = with(ImGui) {

            setNextWindowSize(Vec2(500, 440), SetCond.FirstUseEver)
            if (begin("Example: Layout", pOpen, WindowFlags.MenuBar.i)) {
                if (beginMenuBar()) {
                    if (beginMenu("File")) {
                        if (menuItem("Close")) pOpen[0] = false
                        endMenu()
                    }
                    endMenuBar()
                }

                // left
                beginChild("left pane", Vec2(150, 0), true)
                repeat(100) {
                    if (selectable("MyObject $it", selected == it))
                        selected = it
                }
                endChild()
                sameLine()

                // right
                beginGroup()
                beginChild("item view", Vec2(0, -itemsLineHeightWithSpacing)) // Leave room for 1 line below us
                text("MyObject: %d", selected)
                separator()
                textWrapped("Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor " +
                        "incididunt ut labore et dolore magna aliqua. ")
                endChild()
                beginChild("buttons")
                if (button("Revert")) {
                }
                sameLine()
                if (button("Save")) {
                }
                endChild()
                endGroup()
            }
            end()
        }

        var selected = 0

        /** Demonstrate create a simple property editor.    */
        fun showExampleAppPropertyEditor(pOpen: BooleanArray) {

            setNextWindowSize(Vec2(430, 450), SetCond.FirstUseEver)
            if (!begin("Example: Property editor", pOpen)) {
                end()
                return
            }

            showHelpMarker("This example shows how you may implement a property editor using two columns.\n" +
                    "All objects/fields data are dummies here.\n" +
                    "Remember that in many simple cases, you can use ImGui::SameLine(xxx) to position\n" +
                    "your cursor horizontally instead of using the Columns() API.")

            pushStyleVar(StyleVar.FramePadding, Vec2(2))
            columns(2)
            separator()


            // Iterate dummy objects with dummy members (all the same data)
            for (objI in 0..2)
                Funcs.showDummyObject("Object", objI)

            columns(1)
            separator()
            popStyleVar()
            end()
        }

        /** Demonstrate/test rendering huge amount of text, and the incidence of clipping.  */
        fun showExampleAppLongText(pOpen: BooleanArray) {

            setNextWindowSize(Vec2(520, 600), SetCond.FirstUseEver)
            if (!begin("Example: Long text display, to implement", pOpen)) {
                end()
                return
            }

//            static int test_type = 0;
//            static ImGuiTextBuffer log;
//            static int lines = 0;
//            ImGui::Text("Printing unusually long amount of text.");
//            ImGui::Combo("Test type", &test_type, "Single call to TextUnformatted()\0Multiple calls to Text(), clipped manually\0Multiple calls to Text(), not clipped\0");
//            ImGui::Text("Buffer contents: %d lines, %d bytes", lines, log.size());
//            if (ImGui::Button("Clear")) { log.clear(); lines = 0; }
//            ImGui::SameLine();
//            if (ImGui::Button("Add 1000 lines"))
//            {
//                for (int i = 0; i < 1000; i++)
//                log.append("%i The quick brown fox jumps over the lazy dog\n", lines+i);
//                lines += 1000;
//            }
//            ImGui::BeginChild("Log");
//            switch (test_type)
//            {
//                case 0:
//                // Single call to TextUnformatted() with a big buffer
//                ImGui::TextUnformatted(log.begin(), log.end());
//                break;
//                case 1:
//                {
//                    // Multiple calls to Text(), manually coarsely clipped - demonstrate how to use the ImGuiListClipper helper.
//                    ImGui::PushStyleVar(ImGuiStyleVar_ItemSpacing, ImVec2(0,0));
//                    ImGuiListClipper clipper(lines);
//                    while (clipper.Step())
//                        for (int i = clipper.DisplayStart; i < clipper.DisplayEnd; i++)
//                    ImGui::Text("%i The quick brown fox jumps over the lazy dog", i);
//                    ImGui::PopStyleVar();
//                    break;
//                }
//                case 2:
//                // Multiple calls to Text(), not clipped (slow)
//                ImGui::PushStyleVar(ImGuiStyleVar_ItemSpacing, ImVec2(0,0));
//                for (int i = 0; i < lines; i++)
//                ImGui::Text("%i The quick brown fox jumps over the lazy dog", i);
//                ImGui::PopStyleVar();
//                break;
//            }
//            ImGui::EndChild();
            end()
        }

        object showApp {
            // Examples apps
            var mainMenuBar = booleanArrayOf(false)
            var console = booleanArrayOf(false)
            var log = booleanArrayOf(false)
            var layout = booleanArrayOf(false)
            var propertyEditor = booleanArrayOf(false)
            var longText = booleanArrayOf(false)
            var autoResize = booleanArrayOf(false)
            var constrainedResize = booleanArrayOf(false)
            var fixedOverlay = booleanArrayOf(false)
            var manipulatingWindowTitle = booleanArrayOf(false)
            var customRendering = booleanArrayOf(false)
            var styleEditor = booleanArrayOf(false)

            var metrics = booleanArrayOf(false)
            var about = booleanArrayOf(false)
        }

        var noTitlebar = booleanArrayOf(false)
        var noBorder = booleanArrayOf(true)
        var noResize = booleanArrayOf(false)
        var noMove = booleanArrayOf(false)
        var noScrollbar = booleanArrayOf(false)
        var noCollapse = booleanArrayOf(false)
        var noMenu = booleanArrayOf(false)


        val showClipRects = booleanArrayOf(true)

        val listboxItemCurrent = intArrayOf(1)

        val dontAskMeNextTime = booleanArrayOf(false)
    }

    /** Demonstrating creating a simple console window, with scrolling, filtering, completion and history.
     *  For the console example, here we are using a more C++ like approach of declaring a class to hold the data and
     *  the functions.  */
    class ExampleAppConsole {
        //        char                  InputBuf[256];
//        ImVector<char*>       Items;
//        bool                  ScrollToBottom;
//        ImVector<char*>       History;
//        int                   HistoryPos;    // -1: new line, 0..History.Size-1 browsing history.
//        ImVector<const char*> Commands;
//
//        ExampleAppConsole()
//        {
//            ClearLog();
//            memset(InputBuf, 0, sizeof(InputBuf));
//            HistoryPos = -1;
//            Commands.push_back("HELP");
//            Commands.push_back("HISTORY");
//            Commands.push_back("CLEAR");
//            Commands.push_back("CLASSIFY");  // "classify" is here to provide an example of "C"+[tab] completing to "CL" and displaying matches.
//            AddLog("Welcome to ImGui!");
//        }
//        ~ExampleAppConsole()
//    {
//        ClearLog();
//        for (int i = 0; i < History.Size; i++)
//        free(History[i]);
//    }
//
//        // Portable helpers
//        static int   Stricmp(const char* str1, const char* str2)         { int d; while ((d = toupper(*str2) - toupper(*str1)) == 0 && *str1) { str1++; str2++; } return d; }
//        static int   Strnicmp(const char* str1, const char* str2, int n) { int d = 0; while (n > 0 && (d = toupper(*str2) - toupper(*str1)) == 0 && *str1) { str1++; str2++; n--; } return d; }
//        static char* Strdup(const char *str)                             { size_t len = strlen(str) + 1; void* buff = malloc(len); return (char*)memcpy(buff, (const void*)str, len); }
//
//        void    ClearLog()
//        {
//            for (int i = 0; i < Items.Size; i++)
//            free(Items[i]);
//            Items.clear();
//            ScrollToBottom = true;
//        }
//
//        void    AddLog(const char* fmt, ...) IM_PRINTFARGS(2)
//        {
//            char buf[1024];
//            va_list args;
//            va_start(args, fmt);
//            vsnprintf(buf, IM_ARRAYSIZE(buf), fmt, args);
//            buf[IM_ARRAYSIZE(buf)-1] = 0;
//            va_end(args);
//            Items.push_back(Strdup(buf));
//            ScrollToBottom = true;
//        }
//
        fun draw(title: String, pOpen: BooleanArray) = with(ImGui) {

            setNextWindowSize(Vec2(520, 600), SetCond.FirstUseEver)
            if (!begin(title, pOpen)) {
                end()
                return
            }

            textWrapped("This example is not yet implemented, you are welcome to contribute")
//            textWrapped("This example implements a console with basic coloring, completion and history. A more elaborate implementation may want to store entries along with extra data such as timestamp, emitter, etc.");
//            ImGui::TextWrapped("Enter 'HELP' for help, press TAB to use text completion.");
//
//            // TODO: display items starting from the bottom
//
//            if (ImGui::SmallButton("Add Dummy Text")) { AddLog("%d some text", Items.Size); AddLog("some more text"); AddLog("display very important message here!"); } ImGui::SameLine();
//            if (ImGui::SmallButton("Add Dummy Error")) AddLog("[error] something went wrong"); ImGui::SameLine();
//            if (ImGui::SmallButton("Clear")) ClearLog(); ImGui::SameLine();
//            if (ImGui::SmallButton("Scroll to bottom")) ScrollToBottom = true;
//            //static float t = 0.0f; if (ImGui::GetTime() - t > 0.02f) { t = ImGui::GetTime(); AddLog("Spam %f", t); }
//
//            ImGui::Separator();
//
//            ImGui::PushStyleVar(ImGuiStyleVar_FramePadding, ImVec2(0,0));
//            static ImGuiTextFilter filter;
//            filter.Draw("Filter (\"incl,-excl\") (\"error\")", 180);
//            ImGui::PopStyleVar();
//            ImGui::Separator();
//
//            ImGui::BeginChild("ScrollingRegion", ImVec2(0,-ImGui::GetItemsLineHeightWithSpacing()), false, ImGuiWindowFlags_HorizontalScrollbar);
//            if (ImGui::BeginPopupContextWindow())
//            {
//                if (ImGui::Selectable("Clear")) ClearLog();
//                ImGui::EndPopup();
//            }
//
//            // Display every line as a separate entry so we can change their color or add custom widgets. If you only want raw text you can use ImGui::TextUnformatted(log.begin(), log.end());
//            // NB- if you have thousands of entries this approach may be too inefficient and may require user-side clipping to only process visible items.
//            // You can seek and display only the lines that are visible using the ImGuiListClipper helper, if your elements are evenly spaced and you have cheap random access to the elements.
//            // To use the clipper we could replace the 'for (int i = 0; i < Items.Size; i++)' loop with:
//            //     ImGuiListClipper clipper(Items.Size);
//            //     while (clipper.Step())
//            //         for (int i = clipper.DisplayStart; i < clipper.DisplayEnd; i++)
//            // However take note that you can not use this code as is if a filter is active because it breaks the 'cheap random-access' property. We would need random-access on the post-filtered list.
//            // A typical application wanting coarse clipping and filtering may want to pre-compute an array of indices that passed the filtering test, recomputing this array when user changes the filter,
//            // and appending newly elements as they are inserted. This is left as a task to the user until we can manage to improve this example code!
//            // If your items are of variable size you may want to implement code similar to what ImGuiListClipper does. Or split your data into fixed height items to allow random-seeking into your list.
//            ImGui::PushStyleVar(ImGuiStyleVar_ItemSpacing, ImVec2(4,1)); // Tighten spacing
//            for (int i = 0; i < Items.Size; i++)
//            {
//                const char* item = Items[i];
//                if (!filter.PassFilter(item))
//                    continue;
//                ImVec4 col = ImVec4(1.0f,1.0f,1.0f,1.0f); // A better implementation may store a type per-item. For the sample let's just parse the text.
//                if (strstr(item, "[error]")) col = ImColor(1.0f,0.4f,0.4f,1.0f);
//                else if (strncmp(item, "# ", 2) == 0) col = ImColor(1.0f,0.78f,0.58f,1.0f);
//                ImGui::PushStyleColor(ImGuiCol_Text, col);
//                ImGui::TextUnformatted(item);
//                ImGui::PopStyleColor();
//            }
//            if (ScrollToBottom)
//                ImGui::SetScrollHere();
//            ScrollToBottom = false;
//            ImGui::PopStyleVar();
//            ImGui::EndChild();
//            ImGui::Separator();
//
//            // Command-line
//            if (ImGui::InputText("Input", InputBuf, IM_ARRAYSIZE(InputBuf), ImGuiInputTextFlags_EnterReturnsTrue|ImGuiInputTextFlags_CallbackCompletion|ImGuiInputTextFlags_CallbackHistory, &TextEditCallbackStub, (void*)this))
//            {
//                char* input_end = InputBuf+strlen(InputBuf);
//                while (input_end > InputBuf && input_end[-1] == ' ') input_end--; *input_end = 0;
//                if (InputBuf[0])
//                    ExecCommand(InputBuf);
//                strcpy(InputBuf, "");
//            }
//
//            // Demonstrate keeping auto focus on the input box
//            if (ImGui::IsItemHovered() || (ImGui::IsRootWindowOrAnyChildFocused() && !ImGui::IsAnyItemActive() && !ImGui::IsMouseClicked(0)))
//                ImGui::SetKeyboardFocusHere(-1); // Auto focus previous widget
//
//            ImGui::End();
        }
//
//        void    ExecCommand(const char* command_line)
//        {
//            AddLog("# %s\n", command_line);
//
//            // Insert into history. First find match and delete it so it can be pushed to the back. This isn't trying to be smart or optimal.
//            HistoryPos = -1;
//            for (int i = History.Size-1; i >= 0; i--)
//            if (Stricmp(History[i], command_line) == 0)
//            {
//                free(History[i]);
//                History.erase(History.begin() + i);
//                break;
//            }
//            History.push_back(Strdup(command_line));
//
//            // Process command
//            if (Stricmp(command_line, "CLEAR") == 0)
//            {
//                ClearLog();
//            }
//            else if (Stricmp(command_line, "HELP") == 0)
//            {
//                AddLog("Commands:");
//                for (int i = 0; i < Commands.Size; i++)
//                AddLog("- %s", Commands[i]);
//            }
//            else if (Stricmp(command_line, "HISTORY") == 0)
//            {
//                for (int i = History.Size >= 10 ? History.Size - 10 : 0; i < History.Size; i++)
//                AddLog("%3d: %s\n", i, History[i]);
//            }
//            else
//            {
//                AddLog("Unknown command: '%s'\n", command_line);
//            }
//        }
//
//        static int TextEditCallbackStub(ImGuiTextEditCallbackData* data) // In C++11 you are better off using lambdas for this sort of forwarding callbacks
//        {
//            ExampleAppConsole* console = (ExampleAppConsole*)data->UserData;
//            return console->TextEditCallback(data);
//        }
//
//        int     TextEditCallback(ImGuiTextEditCallbackData* data)
//        {
//            //AddLog("cursor: %d, selection: %d-%d", data->CursorPos, data->SelectionStart, data->SelectionEnd);
//            switch (data->EventFlag)
//            {
//                case ImGuiInputTextFlags_CallbackCompletion:
//                {
//                    // Example of TEXT COMPLETION
//
//                    // Locate beginning of current word
//                    const char* word_end = data->Buf + data->CursorPos;
//                    const char* word_start = word_end;
//                    while (word_start > data->Buf)
//                    {
//                        const char c = word_start[-1];
//                        if (c == ' ' || c == '\t' || c == ',' || c == ';')
//                            break;
//                        word_start--;
//                    }
//
//                    // Build a list of candidates
//                    ImVector<const char*> candidates;
//                    for (int i = 0; i < Commands.Size; i++)
//                    if (Strnicmp(Commands[i], word_start, (int)(word_end-word_start)) == 0)
//                        candidates.push_back(Commands[i]);
//
//                    if (candidates.Size == 0)
//                    {
//                        // No match
//                        AddLog("No match for \"%.*s\"!\n", (int)(word_end-word_start), word_start);
//                    }
//                    else if (candidates.Size == 1)
//                        {
//                            // Single match. Delete the beginning of the word and replace it entirely so we've got nice casing
//                            data->DeleteChars((int)(word_start-data->Buf), (int)(word_end-word_start));
//                            data->InsertChars(data->CursorPos, candidates[0]);
//                            data->InsertChars(data->CursorPos, " ");
//                        }
//                    else
//                    {
//                        // Multiple matches. Complete as much as we can, so inputing "C" will complete to "CL" and display "CLEAR" and "CLASSIFY"
//                        int match_len = (int)(word_end - word_start);
//                        for (;;)
//                        {
//                            int c = 0;
//                            bool all_candidates_matches = true;
//                            for (int i = 0; i < candidates.Size && all_candidates_matches; i++)
//                            if (i == 0)
//                                c = toupper(candidates[i][match_len]);
//                            else if (c == 0 || c != toupper(candidates[i][match_len]))
//                                all_candidates_matches = false;
//                            if (!all_candidates_matches)
//                                break;
//                            match_len++;
//                        }
//
//                        if (match_len > 0)
//                            {
//                                data->DeleteChars((int)(word_start - data->Buf), (int)(word_end-word_start));
//                                data->InsertChars(data->CursorPos, candidates[0], candidates[0] + match_len);
//                            }
//
//                        // List matches
//                        AddLog("Possible matches:\n");
//                        for (int i = 0; i < candidates.Size; i++)
//                        AddLog("- %s\n", candidates[i]);
//                    }
//
//                    break;
//                }
//                case ImGuiInputTextFlags_CallbackHistory:
//                {
//                    // Example of HISTORY
//                    const int prev_history_pos = HistoryPos;
//                    if (data->EventKey == ImGuiKey_UpArrow)
//                    {
//                        if (HistoryPos == -1)
//                            HistoryPos = History.Size - 1;
//                        else if (HistoryPos > 0)
//                            HistoryPos--;
//                    }
//                    else if (data->EventKey == ImGuiKey_DownArrow)
//                    {
//                        if (HistoryPos != -1)
//                            if (++HistoryPos >= History.Size)
//                                HistoryPos = -1;
//                    }
//
//                    // A better implementation would preserve the data on the current input line along with cursor position.
//                    if (prev_history_pos != HistoryPos)
//                        {
//                            data->CursorPos = data->SelectionStart = data->SelectionEnd = data->BufTextLen = (int)snprintf(data->Buf, (size_t)data->BufSize, "%s", (HistoryPos >= 0) ? History[HistoryPos] : "");
//                            data->BufDirty = true;
//                        }
//                }
//            }
//            return 0;
//        }
    }

    /** Usage:
     *      static ExampleAppLog my_log;
     *      my_log.AddLog("Hello %d world\n", 123);
     *      my_log.Draw("title");   */
    class ExampleAppLog {

        val buf = StringBuilder()
        val filter = TextFilter()// TODO
        //        ImVector<int>       LineOffsets;        // Index to lines offset
        var scrollToBottom = false

        fun addLog(fmt: String) {
            buf.append(fmt)
            scrollToBottom = true
        }

        fun clear() = buf.setLength(0)

        fun draw(title: String, pOpen: BooleanArray? = null) = with(ImGui) {

            setNextWindowSize(Vec2(500, 400), SetCond.FirstUseEver)
            begin(title, pOpen)
            if (button("Clear")) clear()
            sameLine()
            val copy = button("Copy")
            sameLine()
            filter.draw("Filter", -100f)
            separator()
            beginChild("scrolling", Vec2(0, 0), false, WindowFlags.HorizontalScrollbar.i)
            if (copy) logToClipboard()

//      TODO      if (Filter.IsActive())
//            {
//                const char* buf_begin = Buf.begin()
//                const char* line = buf_begin
//                for (int line_no = 0; line != NULL; line_no++)
//                {
//                    const char* line_end = (line_no < LineOffsets.Size) ? buf_begin + LineOffsets[line_no] : NULL
//                    if (Filter.PassFilter(line, line_end))
//                        ImGui::TextUnformatted(line, line_end)
//                    line = line_end && line_end[1] ? line_end + 1 : NULL
//                }
//            }
//            else
            textUnformatted(buf.toString())

            if (scrollToBottom)
                setScrollHere(1f)
            scrollToBottom = false
            endChild()
            end()
        }
    }
}
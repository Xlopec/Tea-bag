package io.github.xlopec.tea.time.travel.plugin.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.intellij.ide.ui.LafManagerListener
import com.intellij.openapi.application.ApplicationManager
import io.kanro.compose.jetbrains.color.ButtonColors
import io.kanro.compose.jetbrains.color.CheckBoxColors
import io.kanro.compose.jetbrains.color.FieldColors
import io.kanro.compose.jetbrains.color.PanelColors
import io.kanro.compose.jetbrains.color.ScrollColors
import io.kanro.compose.jetbrains.color.SelectionColors
import io.kanro.compose.jetbrains.color.TabColors
import io.kanro.compose.jetbrains.color.TextColors
import io.kanro.compose.jetbrains.color.ToolBarColors
import javax.swing.UIManager
import java.awt.Color as AWTColor

val LocalPluginColors = compositionLocalOf<PluginThemeColors> { error("theme colors aren't provided") }

interface PluginThemeColors {
    val toolbarColors: ToolBarColors
    val buttonColors: ButtonColors
    val checkBoxColors: CheckBoxColors
    val panelColors: PanelColors
    val textColors: TextColors
    val fieldColors: FieldColors
    val tabColors: TabColors
    val selectionColors: SelectionColors
    val scrollColors: ScrollColors
    val contrastBorderColor: Color
}

@Composable
fun PluginThemeColors(): PluginThemeColors {
    val swingColors = remember { PluginThemeColorsImpl() }

    val messageBus = remember {
        ApplicationManager.getApplication().messageBus.connect()
    }

    remember(messageBus) {
        messageBus.subscribe(
            LafManagerListener.TOPIC,
            ThemeChangeListener(swingColors::updateCurrentColors)
        )
    }

    DisposableEffect(messageBus) {
        onDispose {
            messageBus.disconnect()
        }
    }

    return swingColors
}

private class PluginThemeColorsImpl : PluginThemeColors {
    private val _fieldColors = mutableStateOf(getFieldColors)
    private val _tabColors = mutableStateOf(getTabColors)
    private val _checkBoxColors = mutableStateOf(getCheckBoxColors)
    private val _selectionColors = mutableStateOf(getSelectionColors)
    private val _buttonColors = mutableStateOf(getButtonColors)
    private val _toolbarColors = mutableStateOf(getToolbarColors)
    private val _contrastBorderColor = mutableStateOf(getContrastBorderColor)
    private val _textColors = mutableStateOf(getTextColors)
    private val _panelColors = mutableStateOf(getPanelColors)
    private val _scrollColors = mutableStateOf(getScrollColors)

    override val fieldColors: FieldColors
        get() = _fieldColors.value
    override val tabColors: TabColors
        get() = _tabColors.value
    override val checkBoxColors: CheckBoxColors
        get() = _checkBoxColors.value
    override val selectionColors: SelectionColors
        get() = _selectionColors.value
    override val buttonColors: ButtonColors
        get() = _buttonColors.value
    override val toolbarColors: ToolBarColors
        get() = _toolbarColors.value
    override val contrastBorderColor: Color
        get() = _contrastBorderColor.value
    override val textColors: TextColors
        get() = _textColors.value
    override val panelColors: PanelColors
        get() = _panelColors.value
    override val scrollColors: ScrollColors
        get() = _scrollColors.value

    private val getContrastBorderColor
        get() = getColor("Borders.ContrastBorderColor")

    private val getScrollColors
        get() = ScrollColors(
            bg = getColor("ScrollBar.track")
        )

    private val getPanelColors
        get() = PanelColors(
            border = getColor("Component.borderColor"),
            bgContent = getColor("Panel.background"),
            bgDialog = getColor("Panel.background"),
        )

    private val getToolbarColors
        get() = ToolBarColors(
            buttonPressed = getColor("ActionButton.pressedBackground"),
            buttonHover = getColor("ActionButton.hoverBorderColor"),
            iconSplitBorder = getColor("Separator.foreground")
        )

    private val getFieldColors
        get() = FieldColors(
            bg = getColor("TextField.background"),
            border = getColor("Component.borderColor"),
            borderFocused = getColor("Component.focusColor"),
            comboboxButton = Color.Yellow,
            bgDisabled = getColor("TextField.disabledBackground"),
            borderDisabled = getColor("Component.disabledBorderColor"),
            borderError = getColor("Component.errorFocusColor")
        )

    private val getTabColors
        get() = TabColors(
            selection = getColor("TabbedPane.foreground"),
            focus = getColor("TabbedPane.focus"),
            selectionInactive = getColor("TabbedPane.background"),
            hover = getColor("TabbedPane.hoverColor"),
            selectionDisabled = getColor("TabbedPane.disabledForeground"),
            bgSelected = getColor("TabbedPane.background"),
        )

    private val getCheckBoxColors
        get() = CheckBoxColors(
            bg = getColor("CheckBox.background"),
            bgSelected = getColor("CheckBox.background"),
            bgDisabled = getColor("CheckBox.background"),
            border = getColor("Component.borderColor"),
            borderSelected = getColor("Component.borderColor"),
            borderFocused = getColor("Button.default.focusedBorderColor"),
            borderDisabled = getColor("Button.disabledBorderColor"),
        )

    private val getSelectionColors: SelectionColors
        get() = SelectionColors(
            active = getColor("Tree.selectionBackground"),
            inactive = getColor("Tree.selectionInactiveBackground"),
            hover = getColor("ActionButton.hoverBackground"),
            lightActive = getColor("Tree.selectionBackground"),
            lightInactive = getColor("Tree.selectionBackground"),
            completionPopup = getColor("Panel.background")
        )

    private val getButtonColors
        get() = ButtonColors(
            bg = getColor("Button.background"),
            border = getColor("Button.startBorderColor"),
            borderRegularFocused = getColor("Button.default.focusedBorderColor"),
            defaultStart = getColor("Button.startBackground"),
            defaultEnd = getColor("Button.endBackground"),
            borderDefaultStart = getColor("Button.default.startBorderColor"),
            borderDefaultEnd = getColor("Button.default.endBorderColor"),
            borderDefaultFocused = getColor("Button.default.focusedBorderColor"),
            bgDisabled = getColor("Button.disabledText"),
            borderDisabled = getColor("Button.disabledBorderColor")
        )

    private val getTextColors
        get() = TextColors(
            default = getColor("text"),
            disabled = getColor("Label.disabledForeground"),
            white = Color.White,
            link = getColor("Hyperlink.linkColor"),
            infoPanel = Color.Magenta, // not used
            infoInput = Color.Cyan, // not used
            error = getColor("Component.errorFocusColor"),
            success = Color.Yellow, // not used
        )

    fun updateCurrentColors() {
        _fieldColors.value = getFieldColors
        _tabColors.value = getTabColors
        _checkBoxColors.value = getCheckBoxColors
        _selectionColors.value = getSelectionColors
        _buttonColors.value = getButtonColors
        _toolbarColors.value = getToolbarColors
        _contrastBorderColor.value = getContrastBorderColor
        _textColors.value = getTextColors
        _panelColors.value = getPanelColors
        _scrollColors.value = getScrollColors
    }
}

private fun getColor(key: String): Color = UIManager.getColor(key).toComposeColor()

private fun AWTColor.toComposeColor(): Color = Color(red, green, blue, alpha)

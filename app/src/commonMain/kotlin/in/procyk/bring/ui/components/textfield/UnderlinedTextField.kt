package `in`.procyk.bring.ui.components.textfield

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import `in`.procyk.bring.ui.BringAppTheme
import `in`.procyk.bring.ui.components.textfield.base.*

@Composable
internal fun UnderlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = BringAppTheme.typography.input,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    placeholder: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    label: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    colors: TextFieldColors = UnderlinedTextFieldDefaults.colors(),
    cursorBrush: Brush = SolidColor(colors.cursorColor(isError).value),
) {
    val textColor =
        textStyle.color.takeOrElse {
            colors.textColor(enabled, isError, interactionSource).value
        }
    val mergedTextStyle = textStyle.merge(TextStyle(color = textColor))

    CompositionLocalProvider(LocalTextSelectionColors provides colors.selectionColors) {
        BasicTextField(
            modifier =
                modifier
                    .defaultMinSize(
                        minHeight = UnderlinedTextFieldDefaults.MinHeight,
                    )
                    .fillMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            readOnly = readOnly,
            textStyle = mergedTextStyle,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            visualTransformation = visualTransformation,
            onTextLayout = onTextLayout,
            interactionSource = interactionSource,
            cursorBrush = cursorBrush,
            decorationBox = @Composable { innerTextField ->
                UnderlinedTextFieldDefaults.DecorationBox(
                    value = value,
                    innerTextField = innerTextField,
                    visualTransformation = visualTransformation,
                    label = label,
                    placeholder = placeholder,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    prefix = prefix,
                    suffix = suffix,
                    supportingText = supportingText,
                    enabled = enabled,
                    isError = isError,
                    interactionSource = interactionSource,
                    colors = UnderlinedTextFieldDefaults.colors(),
                )
            },
        )
    }
}

@Composable
internal fun UnderlinedTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = BringAppTheme.typography.input,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    placeholder: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    label: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    colors: TextFieldColors = UnderlinedTextFieldDefaults.colors(),
    cursorBrush: Brush = SolidColor(colors.cursorColor(isError).value),
) {
    val textColor =
        textStyle.color.takeOrElse {
            colors.textColor(enabled, isError, interactionSource).value
        }
    val mergedTextStyle = textStyle.merge(TextStyle(color = textColor))

    CompositionLocalProvider(LocalTextSelectionColors provides colors.selectionColors) {
        BasicTextField(
            modifier =
                modifier
                    .defaultMinSize(
                        minHeight = UnderlinedTextFieldDefaults.MinHeight,
                    )
                    .fillMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            readOnly = readOnly,
            textStyle = mergedTextStyle,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            visualTransformation = visualTransformation,
            onTextLayout = onTextLayout,
            interactionSource = interactionSource,
            cursorBrush = cursorBrush,
            decorationBox = @Composable { innerTextField ->
                UnderlinedTextFieldDefaults.DecorationBox(
                    value = value.text,
                    innerTextField = innerTextField,
                    visualTransformation = visualTransformation,
                    label = label,
                    placeholder = placeholder,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    prefix = prefix,
                    suffix = suffix,
                    supportingText = supportingText,
                    enabled = enabled,
                    isError = isError,
                    interactionSource = interactionSource,
                    colors = UnderlinedTextFieldDefaults.colors(),
                )
            },
        )
    }
}

@Immutable
internal object UnderlinedTextFieldDefaults {
    val MinHeight = TextFieldMinHeight

    private fun contentPadding(
        start: Dp = 0.dp,
        end: Dp = 0.dp,
        top: Dp = TextFieldVerticalPadding,
        bottom: Dp = TextFieldVerticalPadding,
    ): PaddingValues = PaddingValues(start, top, end, bottom)

    private fun labelPadding(
        start: Dp = 0.dp,
        top: Dp = 0.dp,
        end: Dp = 0.dp,
        bottom: Dp = 0.dp,
    ): PaddingValues = PaddingValues(start, top, end, bottom)

    private fun supportingTextPadding(
        start: Dp = 0.dp,
        top: Dp = SupportingTopPadding,
        end: Dp = TextFieldHorizontalPadding,
        bottom: Dp = 0.dp,
    ): PaddingValues = PaddingValues(start, top, end, bottom)

    @Composable
    private fun leadingIconPadding(
        start: Dp = 0.dp,
        top: Dp = 0.dp,
        end: Dp = HorizontalIconPadding / 2,
        bottom: Dp = 0.dp,
    ): PaddingValues = PaddingValues(start, top, end, bottom)

    @Composable
    private fun trailingIconPadding(
        start: Dp = HorizontalIconPadding / 2,
        top: Dp = 0.dp,
        end: Dp = 0.dp,
        bottom: Dp = 0.dp,
    ): PaddingValues = PaddingValues(start, top, end, bottom)

    @Composable
    fun containerBorderThickness(
        interactionSource: InteractionSource,
    ): Dp {
        val focused by interactionSource.collectIsFocusedAsState()

        return if (focused) FocusedOutlineThickness else UnfocusedOutlineThickness
    }

    @Composable
    fun DecorationBox(
        value: String,
        innerTextField: @Composable () -> Unit,
        enabled: Boolean,
        visualTransformation: VisualTransformation,
        interactionSource: InteractionSource,
        isError: Boolean = false,
        label: @Composable (() -> Unit)? = null,
        placeholder: @Composable (() -> Unit)? = null,
        leadingIcon: @Composable (() -> Unit)? = null,
        trailingIcon: @Composable (() -> Unit)? = null,
        prefix: @Composable (() -> Unit)? = null,
        suffix: @Composable (() -> Unit)? = null,
        supportingText: @Composable (() -> Unit)? = null,
        colors: TextFieldColors = colors(),
        container: @Composable () -> Unit = {
            ContainerBox(enabled, isError, interactionSource, colors)
        },
    ) {
        CommonDecorationBox(
            value = value,
            innerTextField = innerTextField,
            visualTransformation = visualTransformation,
            placeholder = placeholder,
            label = label,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            prefix = prefix,
            suffix = suffix,
            supportingText = supportingText,
            enabled = enabled,
            isError = isError,
            interactionSource = interactionSource,
            colors = colors,
            contentPadding = contentPadding(),
            labelPadding = labelPadding(),
            supportingTextPadding = supportingTextPadding(),
            leadingIconPadding = leadingIconPadding(),
            trailingIconPadding = trailingIconPadding(),
            container = container,
        )
    }

    @Composable
    fun ContainerBox(
        enabled: Boolean,
        isError: Boolean,
        interactionSource: InteractionSource,
        colors: TextFieldColors,
        borderThickness: Dp = containerBorderThickness(interactionSource),
    ) {
        Box(
            Modifier
                .background(colors.containerColor(enabled, isError, interactionSource).value)
                .containerUnderline(enabled, isError, interactionSource, colors, borderThickness),
        )
    }

    @Composable
    fun colors(): TextFieldColors {
        return TextFieldColors(
            focusedTextColor = BringAppTheme.colors.text,
            unfocusedTextColor = BringAppTheme.colors.text,
            disabledTextColor = BringAppTheme.colors.onDisabled,
            errorTextColor = BringAppTheme.colors.text,
            focusedContainerColor = BringAppTheme.colors.transparent,
            unfocusedContainerColor = BringAppTheme.colors.transparent,
            disabledContainerColor = BringAppTheme.colors.transparent,
            errorContainerColor = BringAppTheme.colors.transparent,
            cursorColor = BringAppTheme.colors.primary,
            errorCursorColor = BringAppTheme.colors.error,
            textSelectionColors = LocalTextSelectionColors.current,
            focusedOutlineColor = BringAppTheme.colors.primary,
            unfocusedOutlineColor = BringAppTheme.colors.secondary,
            disabledOutlineColor = BringAppTheme.colors.disabled,
            errorOutlineColor = BringAppTheme.colors.error,
            focusedLeadingIconColor = BringAppTheme.colors.primary,
            unfocusedLeadingIconColor = BringAppTheme.colors.primary,
            disabledLeadingIconColor = BringAppTheme.colors.onDisabled,
            errorLeadingIconColor = BringAppTheme.colors.primary,
            focusedTrailingIconColor = BringAppTheme.colors.primary,
            unfocusedTrailingIconColor = BringAppTheme.colors.primary,
            disabledTrailingIconColor = BringAppTheme.colors.onDisabled,
            errorTrailingIconColor = BringAppTheme.colors.primary,
            focusedLabelColor = BringAppTheme.colors.primary,
            unfocusedLabelColor = BringAppTheme.colors.primary,
            disabledLabelColor = BringAppTheme.colors.textDisabled,
            errorLabelColor = BringAppTheme.colors.error,
            focusedPlaceholderColor = BringAppTheme.colors.textSecondary,
            unfocusedPlaceholderColor = BringAppTheme.colors.textSecondary,
            disabledPlaceholderColor = BringAppTheme.colors.textDisabled,
            errorPlaceholderColor = BringAppTheme.colors.textSecondary,
            focusedSupportingTextColor = BringAppTheme.colors.primary,
            unfocusedSupportingTextColor = BringAppTheme.colors.primary,
            disabledSupportingTextColor = BringAppTheme.colors.textDisabled,
            errorSupportingTextColor = BringAppTheme.colors.error,
            focusedPrefixColor = BringAppTheme.colors.primary,
            unfocusedPrefixColor = BringAppTheme.colors.primary,
            disabledPrefixColor = BringAppTheme.colors.onDisabled,
            errorPrefixColor = BringAppTheme.colors.primary,
            focusedSuffixColor = BringAppTheme.colors.primary,
            unfocusedSuffixColor = BringAppTheme.colors.primary,
            disabledSuffixColor = BringAppTheme.colors.onDisabled,
            errorSuffixColor = BringAppTheme.colors.primary,
        )
    }
}

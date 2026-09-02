package com.example.pkl_finance.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pkl_finance.ui.theme.*

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    helperText: String? = null,
    errorText: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    isPassword: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    enabled: Boolean = true,
    singleLine: Boolean = true,
) {
    var passwordVisible by remember { mutableStateOf(false) }

    val isError    = errorText != null
    val focusBorder   = if (isError) ErrorRed else Blue500
    val unfocusBorder = if (isError) ErrorRed.copy(alpha = 0.6f) else ColorBorder

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, fontSize = 14.sp) },
            placeholder = {
                if (placeholder.isNotEmpty()) {
                    Text(placeholder, fontSize = 14.sp, color = SlateGray.copy(alpha = 0.6f))
                }
            },
            leadingIcon = if (leadingIcon != null) {
                {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = if (isError) ErrorRed else Blue400,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else null,
            trailingIcon = when {
                isPassword -> {
                    {
                        val icon = if (passwordVisible) Icons.Default.Visibility
                                   else Icons.Default.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(icon, contentDescription = null, tint = SlateGray)
                        }
                    }
                }
                trailingIcon != null -> {
                    {
                        Icon(
                            imageVector = trailingIcon,
                            contentDescription = null,
                            tint = if (isError) ErrorRed else SlateGray
                        )
                    }
                }
                else -> null
            },
            visualTransformation = if (isPassword && !passwordVisible)
                PasswordVisualTransformation() else VisualTransformation.None,
            isError = isError,
            enabled = enabled,
            singleLine = singleLine,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor      = focusBorder,
                focusedLabelColor       = focusBorder,
                focusedLeadingIconColor = focusBorder,
                unfocusedBorderColor    = unfocusBorder,
                unfocusedLabelColor     = SlateGray,
                errorBorderColor        = ErrorRed,
                errorLabelColor         = ErrorRed,
                errorLeadingIconColor   = ErrorRed,
                errorTrailingIconColor  = ErrorRed,
                disabledBorderColor     = ColorBorder,
                disabledLabelColor      = SlateGray.copy(alpha = 0.5f),
                cursorColor             = Blue500,
                focusedContainerColor   = ExpressiveSurfaceTint,
                unfocusedContainerColor = AppWhite,
                disabledContainerColor  = ColorBorder.copy(alpha = 0.3f),
            ),
            modifier = Modifier.fillMaxWidth()
        )

        val supportText = errorText ?: helperText
        if (supportText != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = supportText,
                fontSize = 12.sp,
                color = if (isError) ErrorRed else SlateGray,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

package com.example.pkl_finance.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.pkl_finance.data.AppState
import com.example.pkl_finance.data.Screen
import com.example.pkl_finance.data.Transaction
import com.example.pkl_finance.ui.components.AppButton
import com.example.pkl_finance.ui.components.AppButtonVariant
import com.example.pkl_finance.ui.components.AppTopBar
import com.example.pkl_finance.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class RupiahVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val rawText = text.text
        if (rawText.isEmpty()) {
            return TransformedText(
                AnnotatedString(""),
                OffsetMapping.Identity
            )
        }
        val parsed = rawText.toLongOrNull() ?: 0L
        val formatted = NumberFormat.getNumberInstance(Locale("in", "ID")).format(parsed)

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                var transformedOffset = 0
                var digitCount = 0
                for (char in formatted) {
                    if (char.isDigit()) {
                        digitCount++
                    }
                    transformedOffset++
                    if (digitCount == offset) {
                        break
                    }
                }
                return transformedOffset
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 0) return 0
                val safeOffset = offset.coerceAtMost(formatted.length)
                var originalOffset = 0
                for (i in 0 until safeOffset) {
                    if (formatted[i].isDigit()) {
                        originalOffset++
                    }
                }
                return originalOffset
            }
        }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }
}

@Composable
fun AnimatedCheckmark(
    modifier: Modifier = Modifier,
    color: Color = SuccessGreen,
    size: Dp = 56.dp
) {
    val animProgress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 650,
                easing = FastOutSlowInEasing
            )
        )
    }

    Canvas(modifier = modifier.size(size)) {
        val width = size.toPx()
        val center = width / 2
        val radius = width / 2

        // Draw Circle background
        drawCircle(
            color = SuccessGreenLight,
            radius = radius * animProgress.value
        )

        // Draw Circle border
        drawCircle(
            color = color,
            radius = radius * animProgress.value,
            style = Stroke(width = 3.dp.toPx())
        )

        // Draw Checkmark path
        if (animProgress.value > 0.4f) {
            val pathProgress = (animProgress.value - 0.4f) / 0.6f
            val path = Path().apply {
                moveTo(width * 0.32f, width * 0.5f)
                lineTo(width * 0.46f, width * 0.64f)
                lineTo(width * 0.68f, width * 0.36f)
            }

            val pathMeasure = PathMeasure()
            pathMeasure.setPath(path, false)

            val drawPath = Path()
            pathMeasure.getSegment(0f, pathMeasure.length * pathProgress, drawPath, true)

            drawPath(
                path = drawPath,
                color = color,
                style = Stroke(
                    width = 4.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}

@Composable
fun DashedDivider(
    color: Color,
    thickness: Dp,
    modifier: Modifier = Modifier,
    dashLength: Dp = 6.dp,
    gapLength: Dp = 4.dp
) {
    Canvas(modifier = modifier.height(thickness)) {
        val strokeWidth = thickness.toPx()
        val dash = dashLength.toPx()
        val gap = gapLength.toPx()
        val pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash, gap), 0f)

        drawLine(
            color = color,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = strokeWidth,
            pathEffect = pathEffect
        )
    }
}

fun getFullBankName(bankId: String): String {
    return when (bankId.uppercase()) {
        "BCA" -> "PT. BANK CENTRAL ASIA TBK."
        "MANDIRI" -> "PT. BANK MANDIRI (PERSERO) TBK."
        "BNI" -> "PT. BANK NEGARA INDONESIA (PERSERO) TBK."
        "BRI" -> "PT. BANK RAKYAT INDONESIA (PERSERO) TBK."
        "BSI" -> "PT. BANK SYARIAH INDONESIA TBK."
        else -> "PT. BANK $bankId TBK."
    }
}

fun maskAccountNumber(accNo: String): String {
    if (accNo.length <= 4) return accNo
    val last4 = accNo.takeLast(4)
    return "****** $last4"
}

@Composable
fun TarikSaldoScreen(
    appState: AppState,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var inputAmountStr by remember { mutableStateOf("") }

    val rawAmt = inputAmountStr.toDoubleOrNull() ?: 0.0
    val isExcessive = rawAmt > appState.mainBalance
    val isValid = rawAmt > 0.0 && !isExcessive

    val presets = remember {
        listOf(
            10000L to "Rp10.000",
            25000L to "Rp25.000",
            50000L to "Rp50.000",
            100000L to "Rp100.000",
            250000L to "Rp250.000",
            500000L to "Rp500.000"
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        AppTopBar(
            title = "Tarik Saldo",
            onBack = { appState.navigateTo(Screen.Home) },
            showDivider = false
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Merchant balance banner
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F6FE)),
                border = BorderStroke(1.dp, Color(0xFFD6E4FC)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Saldo merchant",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Blue500
                    )
                    Text(
                        text = appState.formatRupiah(appState.mainBalance),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Blue800
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Input Card
            Card(
                colors = CardDefaults.cardColors(containerColor = AppWhite),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Centered Input Box overlay
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        val formattedDisplay = if (inputAmountStr.isEmpty()) "0" else {
                            val parsed = inputAmountStr.toLongOrNull() ?: 0L
                            NumberFormat.getNumberInstance(Locale("in", "ID")).format(parsed)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Rp",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Medium,
                                color = SlateGray,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = formattedDisplay,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkNavy
                            )
                        }

                        BasicTextField(
                            value = inputAmountStr,
                            onValueChange = { newVal ->
                                val clean = newVal.filter { it.isDigit() }
                                if (clean.length <= 9) {
                                    inputAmountStr = clean
                                }
                            },
                            textStyle = TextStyle(
                                color = Color.Transparent,
                                fontSize = 32.sp,
                                textAlign = TextAlign.Center
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                             cursorBrush = androidx.compose.ui.graphics.SolidColor(Color.Transparent),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(45.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(20.dp))

                    // Preset buttons grid
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            presets.take(3).forEach { (amt, label) ->
                                val isSelected = inputAmountStr == amt.toString()
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (isSelected) Color.Transparent else AppWhite,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .border(
                                            BorderStroke(1.dp, if (isSelected) PrimaryBlue else ColorBorder),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable { inputAmountStr = amt.toString() }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) PrimaryBlue else SlateGray
                                    )
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            presets.drop(3).forEach { (amt, label) ->
                                val isSelected = inputAmountStr == amt.toString()
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (isSelected) Color.Transparent else AppWhite,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .border(
                                            BorderStroke(1.dp, if (isSelected) PrimaryBlue else ColorBorder),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable { inputAmountStr = amt.toString() }
                                        .padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) PrimaryBlue else SlateGray
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)
                        Spacer(modifier = Modifier.height(12.dp))

                        val isTarikSemuaSelected = inputAmountStr == appState.mainBalance.toLong().toString()
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isTarikSemuaSelected) PrimaryBlue else AppWhite,
                                    RoundedCornerShape(12.dp)
                                )
                                .border(
                                    BorderStroke(1.dp, if (isTarikSemuaSelected) Color.Transparent else ColorBorder),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    inputAmountStr = appState.mainBalance.toLong().toString()
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                            ) {
                            Text(
                                text = "Tarik Semua",
                                fontSize = 12.sp,
                                fontWeight = if (isTarikSemuaSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isTarikSemuaSelected) Color.White else SlateGray
                            )
                        }
                    }
                }
            }

            // Balance Error Notification
            if (isExcessive) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = ErrorRedLight),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Error",
                            tint = ErrorRed,
                            modifier = Modifier.size(16.dp).padding(top = 1.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Saldo merchant tidak mencukupi untuk penarikan ini",
                            fontSize = 12.sp,
                            color = ErrorRed,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

        }

        // Pinned Bottom CTA Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(24.dp)
        ) {
            AppButton(
                text = "Cairkan",
                onClick = {
                    if (isValid) {
                        appState.withdraw(rawAmt) { trans ->
                            inputAmountStr = ""
                            appState.selectedTransactionReceipt = trans
                            appState.navigateTo(Screen.ReceiptDetail)
                        }
                    }
                },
                variant = if (isValid) AppButtonVariant.Primary else AppButtonVariant.Disabled,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun DetailReceiptRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = SlateGray
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = DarkNavy,
            textAlign = TextAlign.End,
            modifier = Modifier.widthIn(max = 200.dp)
        )
    }
}

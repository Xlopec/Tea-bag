package io.github.xlopec.reader.app.ui.theme

import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import tea_bag.samples.shared_app_lib.generated.resources.Res
import tea_bag.samples.shared_app_lib.generated.resources.roboto_bold
import tea_bag.samples.shared_app_lib.generated.resources.roboto_medium
import tea_bag.samples.shared_app_lib.generated.resources.roboto_regular

@Composable
actual fun typography(): Typography {
    val fontFamily = FontFamily(
        Font(Res.font.roboto_regular),
        Font(Res.font.roboto_medium),
        Font(Res.font.roboto_bold)
    )

    return remember {
        Typography(
            h4 = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.W600,
                fontSize = 30.sp
            ),
            h5 = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.W600,
                fontSize = 24.sp
            ),
            h6 = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.W600,
                fontSize = 20.sp
            ),
            subtitle1 = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.W600,
                fontSize = 16.sp
            ),
            subtitle2 = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.W500,
                fontSize = 14.sp
            ),
            body1 = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp
            ),
            body2 = TextStyle(
                fontFamily = fontFamily,
                fontSize = 14.sp
            ),
            button = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.W500,
                fontSize = 14.sp
            ),
            caption = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp
            ),
            overline = TextStyle(
                fontFamily = fontFamily,
                fontWeight = FontWeight.W500,
                fontSize = 12.sp
            )
        )
    }
}
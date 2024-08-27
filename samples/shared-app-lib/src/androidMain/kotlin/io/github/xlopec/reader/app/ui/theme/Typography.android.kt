/*
 * MIT License
 *
 * Copyright (c) 2022. Maksym Oliinyk.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.xlopec.reader.app.ui.theme

import androidx.compose.material.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import io.github.xlopec.shared.R

private val GmsFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private val Roboto = FontFamily(
    androidx.compose.ui.text.googlefonts.Font(
        googleFont = GoogleFont("Roboto"),
        fontProvider = GmsFontProvider,
        weight = FontWeight.Normal
    ),
    androidx.compose.ui.text.googlefonts.Font(
        googleFont = GoogleFont("Roboto"),
        fontProvider = GmsFontProvider,
        weight = FontWeight.Bold
    ),
    androidx.compose.ui.text.googlefonts.Font(
        googleFont = GoogleFont("Roboto"),
        fontProvider = GmsFontProvider,
        weight = FontWeight.Medium
    ),
)

@Composable
internal actual fun typography(): Typography {
    return remember {
        Typography(
            h4 = TextStyle(
                fontFamily = Roboto,
                fontWeight = FontWeight.W600,
                fontSize = 30.sp
            ),
            h5 = TextStyle(
                fontFamily = Roboto,
                fontWeight = FontWeight.W600,
                fontSize = 24.sp
            ),
            h6 = TextStyle(
                fontFamily = Roboto,
                fontWeight = FontWeight.W600,
                fontSize = 20.sp
            ),
            subtitle1 = TextStyle(
                fontFamily = Roboto,
                fontWeight = FontWeight.W600,
                fontSize = 16.sp
            ),
            subtitle2 = TextStyle(
                fontFamily = Roboto,
                fontWeight = FontWeight.W500,
                fontSize = 14.sp
            ),
            body1 = TextStyle(
                fontFamily = Roboto,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp
            ),
            body2 = TextStyle(
                fontFamily = Roboto,
                fontSize = 14.sp
            ),
            button = TextStyle(
                fontFamily = Roboto,
                fontWeight = FontWeight.W500,
                fontSize = 14.sp
            ),
            caption = TextStyle(
                fontFamily = Roboto,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp
            ),
            overline = TextStyle(
                fontFamily = Roboto,
                fontWeight = FontWeight.W500,
                fontSize = 12.sp
            )
        )
    }
}

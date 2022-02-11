package com.oliynick.max.reader.app.feature.storage

import platform.UIKit.UIApplication
import platform.UIKit.UIUserInterfaceStyle
import platform.UIKit.UIViewController

fun UIViewController.isSystemInDarkMode(): Boolean =
    traitCollection.userInterfaceStyle == UIUserInterfaceStyle.UIUserInterfaceStyleDark

fun isSystemInDarkMode(): Boolean =
    UIApplication.sharedApplication.keyWindow?.rootViewController?.isSystemInDarkMode() == true

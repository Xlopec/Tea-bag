package com.oliynick.max.reader.domain

import platform.Foundation.NSDate

actual typealias Date = NSDate

actual fun now(): Date = NSDate()
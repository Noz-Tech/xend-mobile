package com.noztek.xend.core.utils

fun String.capitalizeWords(): String {
    if (isEmpty()) return this

    val result = StringBuilder(length)
    var shouldCapitalize = true

    for (char in this) {
        when {
            char.isWhitespace() -> {
                result.append(char)
                shouldCapitalize = true
            }

            shouldCapitalize -> {
                result.append(char.uppercaseChar())
                shouldCapitalize = false
            }

            else -> {
                result.append(char.lowercaseChar())
            }
        }
    }

    return result.toString()
}

package com.realityexpander.stockmarketapp.util

fun isJUnitTestRunning(): Boolean {
    for (element in Thread.currentThread().stackTrace) {
        if (element.className.startsWith("org.junit.")) {
            return true
        }
    }
    return false
}

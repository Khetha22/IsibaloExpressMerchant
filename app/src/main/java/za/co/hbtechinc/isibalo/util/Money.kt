package za.co.hbtechinc.isibalo.util

import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToLong

fun Long.rands(): String = "R" + String.format(Locale.US, "%,.2f", this / 100.0)

fun String.toCents(): Long? =
    trim().replace(',', '.').toDoubleOrNull()?.let { (it * 100).roundToLong() }

/** Calculates platform transaction commission (e.g., 2.5% fee on digital payments). */
fun Long.calculateCommissionCents(): Long = (this * 0.025).roundToLong()

fun startOfDay(): Long = Calendar.getInstance().apply {
    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
}.timeInMillis

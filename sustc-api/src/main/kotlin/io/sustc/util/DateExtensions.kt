package io.sustc.util

private val datePtn = Regex("^(\\d{1,2})月(\\d{1,2})日$")

fun String?.isValidDate(): Boolean {
    if (this == null) return true

    val matchResult = datePtn.find(this) ?: return false
    val (month, day) = matchResult.destructured
    val monthInt = month.toInt()
    val dayInt = day.toInt()

    val daysInMonth = when (monthInt) {
        1, 3, 5, 7, 8, 10, 12 -> 31
        4, 6, 9, 11 -> 30
        2 -> 29
        else -> return false
    }
    return dayInt in 1..daysInMonth
}

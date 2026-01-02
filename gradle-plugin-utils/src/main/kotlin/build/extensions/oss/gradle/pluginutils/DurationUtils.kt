package build.extensions.oss.gradle.pluginutils

import java.time.Duration
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit


private enum class TimeUnit(
    val chronoUnit: ChronoUnit,
    val groupName: String,
    val suffix: String
) {
    DAYS(ChronoUnit.DAYS, "days", "d"),
    HOURS(ChronoUnit.HOURS, "hours", "h"),
    MINUTES(ChronoUnit.MINUTES, "minutes", "m"),
    SECONDS(ChronoUnit.SECONDS, "seconds", "s"),
    MILLIS(ChronoUnit.MILLIS, "millis", "ms"),
    MICROS(ChronoUnit.MICROS, "micros", "us"),
    NANOS(ChronoUnit.NANOS, "nanos", "ns")
}


/**
 * Allows parsing durations in an alternative, more readable format like `1m30s`.
 */
object SimpleDurationParser {

    private val regex =
        TimeUnit.values().joinToString("", prefix = "^-?", postfix = "$") { timeUnit ->
            "(?:(?<${timeUnit.groupName}>[0-9]+)${timeUnit.suffix})?"
        }.toRegex()


    fun tryParseSimple(input: String): Duration? {

        val trimmed = input.trim()
        if (trimmed == "0") return Duration.ZERO

        return regex.matchEntire(input)?.let { match ->
            TimeUnit.values()
                .fold(Duration.ZERO) { duration, timeUnit ->
                    val amount = match.groups[timeUnit.groupName]?.value?.toLong() ?: 0L
                    if (amount == 0L) duration else duration.plus(amount, timeUnit.chronoUnit)
                }
        }?.let {
            if (trimmed.startsWith('-')) it.negated() else it
        }
    }


    fun parseSimple(input: String): Duration {
        return tryParseSimple(input)
            ?: throw IllegalArgumentException("Invalid duration: \"$input\"")
    }


    fun tryParseAutoDetect(input: String): Duration? =
        if (input.startsWith('P')) {
            try {
                Duration.parse(input)
            } catch (ignored: DateTimeParseException) {
                null
            }
        } else {
            tryParseSimple(input)
        }


    fun parseAutoDetect(input: String): Duration =
        if (input.startsWith('P')) {
            Duration.parse(input)
        } else {
            parseSimple(input)
        }
}


/**
 * Formats a [Duration] in an alternative, more readable format like `1m30s`.
 */
fun Duration.toSimpleString(): String {

    if (this.isZero) return "0"

    if (this.isNegative) {
        return "-${negated().toSimpleString()}"
    }

    var majorTimeLeft = this@toSimpleString.seconds
    var minorTimeLeft = this@toSimpleString.nano

    return buildString {

        if (majorTimeLeft > 0) {
            // seconds
            val seconds = (majorTimeLeft % 60).toInt()
            majorTimeLeft /= 60
            if (majorTimeLeft > 0) {
                // minutes
                val minutes = (majorTimeLeft % 60).toInt()
                majorTimeLeft /= 60
                if (majorTimeLeft > 0) {
                    // hours
                    val hours = (majorTimeLeft % 24).toInt()
                    majorTimeLeft /= 24
                    if (majorTimeLeft > 0) {
                        // days
                        val days = majorTimeLeft.toInt()
                        append(days).append(TimeUnit.DAYS.suffix)
                    }
                    if (hours > 0) {
                        append(hours).append(TimeUnit.HOURS.suffix)
                    }
                }
                if (minutes > 0) {
                    append(minutes).append(TimeUnit.MINUTES.suffix)
                }
            }
            if (seconds > 0) {
                append(seconds).append(TimeUnit.SECONDS.suffix)
            }
        }

        // nanos
        if (minorTimeLeft > 0) {
            val nanos = minorTimeLeft % 1000
            minorTimeLeft /= 1000
            if (minorTimeLeft > 0) {
                val micros = minorTimeLeft % 1000
                minorTimeLeft /= 1000
                if (minorTimeLeft > 0) {
                    val millis = minorTimeLeft
                    append(millis).append(TimeUnit.MILLIS.suffix)
                }
                if (micros > 0) {
                    append(micros).append(TimeUnit.MICROS.suffix)
                }
            }
            if (nanos > 0) {
                append(nanos).append(TimeUnit.NANOS.suffix)
            }
        }
    }
}

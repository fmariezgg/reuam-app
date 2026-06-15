package ni.uam.edu.models.validation

import java.util.UUID

const val EMAIL_REGEX = """^[\w-.]+@([\w-]+\.)+[\w-]{2,4}$"""

fun ValidationObject<String?>.required(): ValidationObject<String?> = useValidation {
    if (value.isNullOrBlank()) "Field is required" else null
}

fun ValidationObject<String?>.email(): ValidationObject<String?> = useValidation {
    if (value.isNullOrBlank()) return@useValidation null
    if (!Regex(EMAIL_REGEX).matches(value)) "Must be a valid email" else null
}

fun ValidationObject<String?>.min(minimum: Int): ValidationObject<String?> = useValidation {
    if (value == null) return@useValidation null
    if (value.length < minimum) "Must be at least $minimum characters long" else null
}

fun ValidationObject<String?>.max(maximum: Int): ValidationObject<String?> = useValidation {
    if (value == null) return@useValidation null
    if (value.length > maximum) "Must be at most $maximum characters long" else null
}

fun ValidationObject<String?>.uuid(): ValidationObject<String?> = useValidation {
    if (value.isNullOrBlank()) return@useValidation null
    try {
        UUID.fromString(value)
        null
    } catch (_: IllegalArgumentException) {
        "Must be a valid UUID"
    }
}

fun ValidationObject<Long?>.minValue(minimum: Long): ValidationObject<Long?> = useValidation {
    if (value == null) return@useValidation null
    if (value < minimum) "Must be greater than or equal to $minimum" else null
}

fun ValidationObject<Int?>.minValue(minimum: Int): ValidationObject<Int?> = useValidation {
    if (value == null) return@useValidation null
    if (value < minimum) "Must be greater than or equal to $minimum" else null
}

fun <T> ValidationObject<List<T>?>.maxItems(maximum: Int): ValidationObject<List<T>?> = useValidation {
    if (value == null) return@useValidation null
    if (value.size > maximum) "Must contain at most $maximum items" else null
}

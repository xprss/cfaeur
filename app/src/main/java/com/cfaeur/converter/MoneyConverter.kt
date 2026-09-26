package com.cfaeur.converter

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Locale

enum class Direction(val source: String, val target: String) {
    CFA_TO_EUR("FCFA", "EUR"),
    EUR_TO_CFA("EUR", "FCFA");

    fun reversed(): Direction = if (this == CFA_TO_EUR) EUR_TO_CFA else CFA_TO_EUR
}

object MoneyConverter {
    val francsPerEuro: BigDecimal = BigDecimal("655.957")

    fun parse(raw: String, direction: Direction): BigDecimal? {
        val normalized = raw.trim().replace(',', '.')
        val pattern = if (direction == Direction.CFA_TO_EUR) {
            Regex("[0-9]+")
        } else {
            Regex("[0-9]+(\\.[0-9]{0,2})?")
        }
        if (!pattern.matches(normalized)) return null
        return normalized.removeSuffix(".").toBigDecimalOrNull()
    }

    fun convert(amount: BigDecimal, direction: Direction): BigDecimal =
        if (direction == Direction.CFA_TO_EUR) {
            amount.divide(francsPerEuro, 2, RoundingMode.HALF_UP)
        } else {
            amount.multiply(francsPerEuro).setScale(0, RoundingMode.HALF_UP)
        }

    fun format(amount: BigDecimal, currency: String): String {
        val decimals = if (currency == "EUR") 2 else 0
        val formatter = NumberFormat.getNumberInstance(Locale.ITALY)
        formatter.minimumFractionDigits = decimals
        formatter.maximumFractionDigits = decimals
        return formatter.format(amount)
    }

    fun formatInput(input: String): String {
        val parts = input.split('.', limit = 2)
        val integer = parts[0].reversed().chunked(3).joinToString(".").reversed()
        return if (parts.size == 2) "$integer,${parts[1]}" else integer
    }
}

data class WidgetValue(
    val input: String = "0",
    val direction: Direction = Direction.CFA_TO_EUR,
) {
    val amount: BigDecimal get() = MoneyConverter.parse(input, direction) ?: BigDecimal.ZERO
    val output: BigDecimal get() = MoneyConverter.convert(amount, direction)

    fun press(key: String): WidgetValue {
        return when (key) {
        "CLEAR" -> copy(input = "0")
        "DELETE" -> copy(input = input.dropLast(1).ifEmpty { "0" })
        "SWAP" -> copy(
            input = output.stripTrailingZeros().toPlainString(),
            direction = direction.reversed(),
        )
        "DECIMAL" -> if (direction == Direction.EUR_TO_CFA && '.' !in input) {
            copy(input = "$input.")
        } else this
        else -> {
            if (!key.matches(Regex("[0-9]{1,2}"))) return this
            val candidate = if (input == "0" && '.' !in input) {
                key.trimStart('0').ifEmpty { "0" }
            } else "$input$key"
            val digitsBeforeDecimal = candidate.substringBefore('.').length
            if (digitsBeforeDecimal <= 12 && MoneyConverter.parse(candidate, direction) != null) {
                copy(input = candidate)
            } else this
        }
        }
    }
}

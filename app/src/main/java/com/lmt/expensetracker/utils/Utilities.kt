package com.lmt.expensetracker.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility functions for date formatting and validation
 */

object DateUtils {
    private const val DATE_FORMAT = "yyyy-MM-dd"
    private const val DISPLAY_DATE_FORMAT = "MMM dd, yyyy"
    private const val DATE_FORMAT_DDMMYYYY = "dd/MM/yyyy"

    fun getCurrentDate(): String {
        val sdf = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
        return sdf.format(Date())
    }

    fun formatDateForDisplay(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
            val outputFormat = SimpleDateFormat(DISPLAY_DATE_FORMAT, Locale.getDefault())
            val date = inputFormat.parse(dateString)
            if (date != null) {
                outputFormat.format(date)
            } else {
                dateString
            }
        } catch (e: Exception) {
            dateString
        }
    }

    fun isValidDate(dateString: String): Boolean {
        return try {
            val sdf = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
            sdf.isLenient = false
            sdf.parse(dateString)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun isValidDateDDMMYYYY(dateString: String): Boolean {
        return try {
            val sdf = SimpleDateFormat(DATE_FORMAT_DDMMYYYY, Locale.getDefault())
            sdf.isLenient = false
            sdf.parse(dateString)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getDateValidationError(dateString: String): String? {
        return when {
            dateString.isBlank() -> "Date is required"
            !isValidDateDDMMYYYY(dateString) -> "Date must be in DD/MM/YYYY format (e.g., 25/12/2024)"
            else -> null
        }
    }

    fun isStartDateBeforeEndDate(startDate: String, endDate: String): Boolean {
        return try {
            val sdf = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
            val start = sdf.parse(startDate)
            val end = sdf.parse(endDate)
            start != null && end != null && start.before(end)
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * Utility functions for currency formatting
 */
object CurrencyUtils {
    fun formatAmount(amount: Double, currency: String = "USD"): String {
        val symbol = when (currency) {
            "USD" -> "$"
            "EUR" -> "€"
            "GBP" -> "£"
            "JPY" -> "¥"
            "VND" -> "₫"
            else -> currency
        }
        return "$symbol${String.format("%.2f", amount)}"
    }

    fun parseAmount(amountString: String): Double? {
        return amountString.toDoubleOrNull()
    }

    fun isValidAmount(amountString: String): Boolean {
        return try {
            val amount = amountString.toDouble()
            amount > 0
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * Utility functions for validation
 */
object ValidationUtils {
    fun isValidProjectName(name: String): Boolean {
        return name.isNotBlank() && name.length >= 3
    }

    fun isValidManager(manager: String): Boolean {
        return manager.isNotBlank() && manager.length >= 2
    }

    fun isValidDescription(description: String): Boolean {
        return description.isNotBlank() && description.length >= 5
    }

    fun isValidBudget(budget: String): Boolean {
        return try {
            val amount = budget.toDouble()
            amount > 0
        } catch (e: Exception) {
            false
        }
    }

    fun isValidClaimant(claimant: String): Boolean {
        return claimant.isNotBlank() && claimant.length >= 2
    }

    fun getValidationErrorMessage(field: String, value: String): String? {
        return when {
            value.isBlank() -> "$field is required"
            field.equals("Name", ignoreCase = true) && value.length < 3 -> "Name must be at least 3 characters"
            field.equals("Budget", ignoreCase = true) && !CurrencyUtils.isValidAmount(value) -> "Budget must be a positive number"
            else -> null
        }
    }
}

/**
 * Utility functions for string operations
 */
object StringUtils {
    fun truncateString(text: String, length: Int = 50): String {
        return if (text.length > length) {
            text.substring(0, length) + "..."
        } else {
            text
        }
    }

    fun capitalize(text: String): String {
        return text.replaceFirstChar { it.uppercase() }
    }

    fun toCamelCase(text: String): String {
        val words = text.split(" ", "-", "_")
        return words.mapIndexed { index, word ->
            if (index == 0) word.lowercase() else word.replaceFirstChar { it.uppercase() }
        }.joinToString("")
    }
}

/**
 * Utility functions for list operations
 */
object ListUtils {
    fun <T> List<T>.sumByProperty(property: (T) -> Double): Double {
        return this.sumOf { property(it) }
    }

    fun <T> List<T>.groupByProperty(property: (T) -> String): Map<String, List<T>> {
        return this.groupBy { property(it) }
    }

    fun <T> List<T>.filterByPropertyValue(property: (T) -> String, value: String): List<T> {
        return this.filter { property(it).equals(value, ignoreCase = true) }
    }
}

/**
 * Utility functions for logging
 */
object LoggerUtils {
    private const val TAG = "ExpenseTracker"

    fun logInfo(message: String) {
        println("[$TAG] INFO: $message")
    }

    fun logError(message: String, exception: Exception? = null) {
        println("[$TAG] ERROR: $message")
        exception?.printStackTrace()
    }

    fun logDebug(message: String) {
        println("[$TAG] DEBUG: $message")
    }
}

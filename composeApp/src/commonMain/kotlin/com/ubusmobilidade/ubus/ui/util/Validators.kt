package com.ubusmobilidade.ubus.ui.util

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/** Formats digits as (99) 9 9999-9999 */
class PhoneVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.filter { it.isDigit() }.take(11)
        val out = buildString {
            digits.forEachIndexed { i, c ->
                when (i) {
                    0 -> append("(")
                    2 -> append(") ")
                    3 -> append(" ")
                    7 -> append("-")
                }
                append(c)
            }
        }
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val o = offset.coerceIn(0, digits.length)
                return when {
                    o == 0 -> 0
                    o <= 2 -> o + 1        // inside (XX
                    o == 3 -> o + 3        // after ") "
                    o <= 7 -> o + 4        // after space: " X XXXX"
                    o <= 11 -> o + 5       // after dash
                    else -> out.length
                }
            }
            override fun transformedToOriginal(offset: Int): Int {
                val t = offset.coerceIn(0, out.length)
                return when {
                    t <= 1 -> 0
                    t <= 3 -> t - 1
                    t <= 5 -> 2
                    t == 6 -> 3
                    t <= 10 -> t - 4
                    t == 11 -> 7
                    t <= 16 -> t - 5
                    else -> digits.length
                }.coerceIn(0, digits.length)
            }
        }
        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}

/** Formats digits as 000.000.000-00 */
class CpfVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.filter { it.isDigit() }.take(11)
        val out = buildString {
            digits.forEachIndexed { i, c ->
                when (i) {
                    3, 6 -> append('.')
                    9 -> append('-')
                }
                append(c)
            }
        }
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val o = offset.coerceIn(0, digits.length)
                return when {
                    o <= 3 -> o
                    o <= 6 -> o + 1
                    o <= 9 -> o + 2
                    o <= 11 -> o + 3
                    else -> out.length
                }
            }
            override fun transformedToOriginal(offset: Int): Int {
                val t = offset.coerceIn(0, out.length)
                return when {
                    t <= 3 -> t
                    t <= 7 -> t - 1
                    t <= 11 -> t - 2
                    t <= 14 -> t - 3
                    else -> digits.length
                }.coerceIn(0, digits.length)
            }
        }
        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}

/** Brazilian CPF validation (mod-11 algorithm) */
fun isValidCpf(cpf: String): Boolean {
    val digits = cpf.filter { it.isDigit() }
    if (digits.length != 11) return false
    if (digits.all { it == digits[0] }) return false

    fun calcDigit(slice: String, weights: IntArray): Int {
        val sum = slice.mapIndexed { i, c -> c.digitToInt() * weights[i] }.sum()
        val remainder = sum % 11
        return if (remainder < 2) 0 else 11 - remainder
    }

    val w1 = intArrayOf(10, 9, 8, 7, 6, 5, 4, 3, 2)
    val w2 = intArrayOf(11, 10, 9, 8, 7, 6, 5, 4, 3, 2)

    val d1 = calcDigit(digits.substring(0, 9), w1)
    val d2 = calcDigit(digits.substring(0, 9) + d1, w2)

    return digits[9].digitToInt() == d1 && digits[10].digitToInt() == d2
}

data class PasswordStrength(val score: Int, val label: String) {
    companion object {
        fun evaluate(password: String): PasswordStrength {
            if (password.isEmpty()) return PasswordStrength(0, "")
            var score = 0
            if (password.length >= 6) score++
            if (password.length >= 8) score++
            if (password.any { it.isUpperCase() }) score++
            if (password.any { it.isDigit() }) score++
            if (password.any { !it.isLetterOrDigit() }) score++
            return when {
                score <= 1 -> PasswordStrength(1, "Fraca")
                score <= 2 -> PasswordStrength(2, "Razoável")
                score <= 3 -> PasswordStrength(3, "Boa")
                score <= 4 -> PasswordStrength(4, "Forte")
                else -> PasswordStrength(5, "Muito forte")
            }
        }
    }
}

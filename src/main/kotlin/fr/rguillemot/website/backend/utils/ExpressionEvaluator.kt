package fr.rguillemot.website.backend.util

import org.springframework.stereotype.Component
import kotlin.math.abs

@Component
class ExpressionEvaluator {

    fun evaluate(input: String): Double {
        val p = Parser(input)
        val result = p.parseExpression()
        p.skipWhitespace()
        if (!p.end()) error("Caractère inattendu à la position ${p.pos}: '${p.peekChar()}'")
        return result
    }

    // Grammaire:
    // expression := term (('+' | '-') term)*
    // term       := factor (('*' | '/') factor)*
    // factor     := NUMBER | '(' expression ')' | ('+'|'-') factor
    private class Parser(val s: String) {
        var pos = 0

        fun end() = pos >= s.length
        fun peekChar(): Char? = if (end()) null else s[pos]
        fun nextChar(): Char? = if (end()) null else s[pos++]
        fun skipWhitespace() { while (!end() && s[pos].isWhitespace()) pos++ }

        fun parseExpression(): Double {
            var value = parseTerm()
            while (true) {
                skipWhitespace()
                val c = peekChar() ?: break
                if (c == '+' || c == '-') {
                    nextChar()
                    val rhs = parseTerm()
                    value = if (c == '+') value + rhs else value - rhs
                } else break
            }
            return value
        }

        fun parseTerm(): Double {
            var value = parseFactor()
            while (true) {
                skipWhitespace()
                val c = peekChar() ?: break
                if (c == '*' || c == '/') {
                    nextChar()
                    val rhs = parseFactor()
                    value = if (c == '*') value * rhs else value / rhs
                } else break
            }
            return value
        }

        fun parseFactor(): Double {
            skipWhitespace()
            val c = peekChar() ?: error("Expression incomplète")
            return when {
                c == '(' -> {
                    nextChar()
                    val inner = parseExpression()
                    skipWhitespace()
                    if (nextChar() != ')') error("Parenthèse fermante manquante à la position $pos")
                    inner
                }
                c == '+' -> { nextChar(); parseFactor() }
                c == '-' -> { nextChar(); -parseFactor() }
                c.isDigit() || c == '.' -> parseNumber()
                else -> error("Caractère inattendu '$c' à la position $pos")
            }
        }

        fun parseNumber(): Double {
            val start = pos
            var dotSeen = false
            while (!end()) {
                val c = s[pos]
                if (c.isDigit()) {
                    pos++
                } else if (c == '.' && !dotSeen) {
                    dotSeen = true
                    pos++
                } else break
            }
            val token = s.substring(start, pos)
            if (token.isEmpty() || token == "." ) error("Nombre invalide à la position $start")
            return token.toDouble()
        }
    }
}

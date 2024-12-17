package com.miiaCourse.calculator

import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.sin
import kotlin.math.tan

/**
 * Tokenizes a mathematical expression into individual tokens.
 *
 * @param input The mathematical expression string.
 */
class ExpressionTokenizer(val input: String) {
    var position = 0 // Current position in the input string
    var currentToken: String? = null // The current token being processed

    /**
     * Advances to the next token in the input string.
     * Skips whitespace and identifies the next token.
     */
    fun advanceToNextToken() {
        // Skip whitespace characters
        while (position < input.length && input[position].isWhitespace()) {
            position++
        }

        // If reached the end of the input, set currentToken to null
        if (position == input.length) {
            currentToken = null
            return
        }

        // Check for numbers (integers, decimals, e, π)
        if (input[position].isDigit() || input[position] == '.' || input[position].toString() == "e" || input[position].toString() == "π") {
            val numberBuilder = StringBuilder()
            while (position < input.length && (input[position].isDigit() || input[position] == '.' || input[position].toString() == "e" || input[position].toString() == "π")) {
                numberBuilder.append(input[position])
                position++
            }
            currentToken = numberBuilder.toString()
            return
        }

        // Check for operators and parentheses
        if ("+-×÷()".contains(input[position])) {
            currentToken = input[position].toString()
            position++
            return
        }

        // Check for functions (log, ln, sin, cos, tan)
        if (input.startsWith("log", position)) {
            currentToken = "log"
            position += 3
            return
        }

        if (input.startsWith("ln", position)) {
            currentToken = "ln"
            position += 2
            return
        }

        if (input.startsWith("sin", position)) {
            currentToken = "sin"
            position += 3
            return
        }

        if (input.startsWith("cos", position)) {
            currentToken = "cos"
            position += 3
            return
        }

        if (input.startsWith("tan", position)) {
            currentToken = "tan"
            position += 3
            return
        }

        // If none of the above, throw an exception for invalid character
        throw IllegalArgumentException("Invalid character encountered: ${input[position]}")
    }
}

/**
 * Evaluates a mathematical expression and returns the result.
 *
 * @param expression The mathematical expression string.
 * @return The calculated result of the expression.
 */
fun evaluate(expression: String): Double {
    val tokenizer = ExpressionTokenizer(expression)
    tokenizer.advanceToNextToken()
    return parseAddSubtractOperation(tokenizer)
}

/**
 * Parses addition and subtraction operations.
 *
 * @param tokenizer The ExpressionTokenizer instance.
 * @return The result of the addition/subtraction operation.
 */
fun parseAddSubtractOperation(tokenizer: ExpressionTokenizer): Double {
    var result = parseMultiplyDivideOperation(tokenizer)

    while (tokenizer.currentToken in listOf("+", "-")) {
        val operator = tokenizer.currentToken!!
        tokenizer.advanceToNextToken()
        val nextTerm = parseMultiplyDivideOperation(tokenizer)

        result = when (operator) {
            "+" -> result + nextTerm
            "-" -> result - nextTerm
            else -> throw IllegalStateException("Unexpected operator: $operator")
        }
    }
    return result
}

/**
 * Parses multiplication and division operations.
 *
 * @param tokenizer The ExpressionTokenizer instance.
 * @return The result of the multiplication/division operation.
 */
fun parseMultiplyDivideOperation(tokenizer: ExpressionTokenizer): Double {
    var result = parseSingleExpression(tokenizer)

    while (tokenizer.currentToken in listOf("×", "÷")) {
        val operator = tokenizer.currentToken!!
        tokenizer.advanceToNextToken()
        val nextFactor = parseSingleExpression(tokenizer)

        result = when (operator) {
            "×" -> result * nextFactor
            "÷" -> result / nextFactor
            else -> throw IllegalStateException("Unexpected operator: $operator")
        }
    }
    return result
}

/**
 * Parses single expressions, including numbers, parentheses, and unary operations.
 *
 * @param tokenizer The ExpressionTokenizer instance.
 * @return The result of the single expression.
 */
fun parseSingleExpression(tokenizer: ExpressionTokenizer): Double {
    if (tokenizer.currentToken == null) {
        throw IllegalArgumentException("Expected a number or expression but found null.")
    }

    // Check for numbers
    if (tokenizer.currentToken!!.toDoubleOrNull() != null) {
        val value = tokenizer.currentToken!!.toDouble()
        tokenizer.advanceToNextToken()
        return value
    }

    // Check for 'e' (Euler's number)
    if (tokenizer.currentToken == "e") {
        tokenizer.advanceToNextToken()
        return Math.E
    }

    // Check for 'π' (pi)
    if (tokenizer.currentToken == "π") {
        tokenizer.advanceToNextToken()
        return Math.PI
    }

    // Check for unary plus/minus
    if (tokenizer.currentToken in listOf("+", "-")) {
        val operator = tokenizer.currentToken!!
        tokenizer.advanceToNextToken()
        val nextFactor = parseSingleExpression(tokenizer)
        return if (operator == "+") nextFactor else -nextFactor
    }

    // Check for parentheses
    if (tokenizer.currentToken == "(" && tokenizer.input.indexOf(")", tokenizer.position) != -1) {
        tokenizer.advanceToNextToken()
        val innerValue = parseAddSubtractOperation(tokenizer)

        if (tokenizer.currentToken == ")") {
            tokenizer.advanceToNextToken()
            return innerValue
        } else {
            throw IllegalArgumentException("Expected closing parenthesis.")
        }
    }

    // Check for functions (log, ln, sin, cos, tan)
    if (tokenizer.currentToken in listOf("log", "ln", "sin", "cos", "tan")) {
        val function = tokenizer.currentToken!!
        tokenizer.advanceToNextToken()

        if (tokenizer.currentToken == "(") {
            tokenizer.advanceToNextToken()
            val innerValue = parseAddSubtractOperation(tokenizer)

            if (tokenizer.currentToken == ")") {
                tokenizer.advanceToNextToken()

                return when (function) {
                    "log" -> log10(innerValue)
                    "ln" -> ln(innerValue)
                    "sin" -> sin(Math.toRadians(innerValue))
                    "cos" -> cos(Math.toRadians(innerValue))
                    "tan" -> tan(Math.toRadians(innerValue))
                    else -> throw IllegalStateException("Unknown function: $function")
                }
            } else {
                throw IllegalArgumentException("Expected closing parenthesis after function argument.")
            }
        } else {
            throw IllegalArgumentException("Expected opening parenthesis after function name.")
        }
    }

    // If none of the above, throw an exception for unexpected token
    throw IllegalArgumentException("Unexpected token encountered: ${tokenizer.currentToken}")
}
package com.miiaCourse.calculator

// Class for breaking down mathematical expressions into tokens
class ExpressionTokenizer(val input: String) {
    var position = 0 // Tracks the current character position
    var currentToken: String? = null // Holds the current token

    // Advances to the next token in the input
    fun advanceToNextToken() {
        while (position < input.length && input[position].isWhitespace()) {
            position++
        }

        if (position == input.length) {
            currentToken = null
            return
        }

        // Parse numbers (integers or decimals)
        if (input[position].isDigit() || input[position] == '.') {
            val numberBuilder = StringBuilder()
            while (position < input.length && (input[position].isDigit() || input[position] == '.')) {
                numberBuilder.append(input[position])
                position++
            }
            currentToken = numberBuilder.toString()
            return
        }

        // Parse operators and parentheses
        if ("+-×÷()".contains(input[position])) {
            currentToken = input[position].toString()
            position++
            return
        }

        throw IllegalArgumentException("Invalid character encountered: ${input[position]}")
    }
}

// Evaluates a mathematical expression and returns the result
fun evaluate(expression: String): Double {
    val tokenizer = ExpressionTokenizer(expression)
    tokenizer.advanceToNextToken()
    return parseAddSubtractOperation(tokenizer)
}

// Handles addition and subtraction operations in the expression
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

// Handles multiplication and division operations in the expression
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

// Handles numbers, parentheses, and unary operations
fun parseSingleExpression(tokenizer: ExpressionTokenizer): Double {
    if (tokenizer.currentToken == null) {
        throw IllegalArgumentException("Expected a number or expression but found null.")
    }

    if (tokenizer.currentToken!!.toDoubleOrNull() != null) {
        val value = tokenizer.currentToken!!.toDouble()
        tokenizer.advanceToNextToken()
        return value
    }

    if (tokenizer.currentToken in listOf("+", "-")) {
        val operator = tokenizer.currentToken!!
        tokenizer.advanceToNextToken()
        val nextFactor = parseSingleExpression(tokenizer)
        return if (operator == "+") nextFactor else -nextFactor
    }

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

    throw IllegalArgumentException("Unexpected token encountered: ${tokenizer.currentToken}")
}

//package com.miiaCourse.calculator
//
//class CalculatorEngine {
//
//    private val precedence = mapOf(
//        "+" to 1,
//        "-" to 1,
//        "*" to 2,
//        "/" to 2,
//        "^" to 3,
//        "sin" to 4,
//        "cos" to 4,
//        "tan" to 4,
//        "log" to 4,
//        "ln" to 4,
//        "√" to 4,
//        "e" to 4 // We treat 'e' like a constant with function-level precedence for convenience
//    )
//
//    // Public function to evaluate an expression string
//    fun evaluate(expression: String): Double {
//        val tokens = tokenize(expression)
//        val rpn = infixToRPN(tokens)
//        return evaluateRPN(rpn)
//    }
//
//    // Tokenize the input string into numbers, operators, functions, parentheses
//    private fun tokenize(expression: String): List<String> {
//        val tokens = mutableListOf<String>()
//        var i = 0
//        while (i < expression.length) {
//            val c = expression[i]
//
//            when {
//                c.isWhitespace() -> {
//                    i++
//                }
//                c.isDigit() || c == '.' -> {
//                    // Parse number
//                    val start = i
//                    var hasDot = (c == '.')
//                    i++
//                    while (i < expression.length && (expression[i].isDigit() || expression[i] == '.')) {
//                        if (expression[i] == '.') {
//                            if (hasDot) {
//                                throw IllegalArgumentException("Invalid number format")
//                            }
//                            hasDot = true
//                        }
//                        i++
//                    }
//                    tokens.add(expression.substring(start, i))
//                }
//                c == '(' || c == ')' -> {
//                    tokens.add(c.toString())
//                    i++
//                }
//                // Check for multi-character functions or constants
//                else -> {
//                    // Potential operator or function
//                    // Functions: sin, cos, tan, log, ln, e, √
//                    // Operators: + - * / ^
//
//                    // Try to match a function or operator
//                    // We know single-char operators: + - * / ^ are straightforward.
//                    // Functions: sin, cos, tan, log, ln, √, and also 'e' as a constant
//                    // We'll try to match longest possible token.
//                    val possibleFuncs = listOf("sin", "cos", "tan", "log", "ln", "√", "e")
//                    val singleCharOps = listOf("+", "-", "*", "/", "^")
//
//                    // Check if starts with a known function
//                    var matched = false
//                    for (func in possibleFuncs) {
//                        if (expression.regionMatches(i, func, 0, func.length, ignoreCase = false)) {
//                            tokens.add(func)
//                            i += func.length
//                            matched = true
//                            break
//                        }
//                    }
//
//                    if (!matched) {
//                        // Check single char operator
//                        val op = c.toString()
//                        if (op in singleCharOps) {
//                            tokens.add(op)
//                            i++
//                            matched = true
//                        }
//                    }
//
//                    if (!matched) {
//                        throw IllegalArgumentException("Unknown token starting at: ${expression.substring(i)}")
//                    }
//                }
//            }
//        }
//        return tokens
//    }
//
//    private fun infixToRPN(tokens: List<String>): List<String> {
//        val output = mutableListOf<String>()
//        val operators = ArrayDeque<String>() // use ArrayDeque as a stack
//
//        for (token in tokens) {
//            when {
//                isNumber(token) -> {
//                    output.add(token)
//                }
//                token == "e" -> {
//                    // e as a constant
//                    output.add(Math.E.toString())
//                }
//                isFunction(token) -> {
//                    operators.push(token)
//                }
//                isOperator(token) -> {
//                    while (operators.isNotEmpty() &&
//                        precedence[operators.peek()] != null &&
//                        precedence[operators.peek()]!! >= precedence[token]!!) {
//                        output.add(operators.pop())
//                    }
//                    operators.push(token)
//                }
//                token == "(" -> {
//                    operators.push(token)
//                }
//                token == ")" -> {
//                    while (operators.isNotEmpty() && operators.peek() != "(") {
//                        output.add(operators.pop())
//                    }
//                    if (operators.isEmpty() || operators.peek() != "(") {
//                        throw IllegalArgumentException("Mismatched parentheses")
//                    }
//                    operators.pop() // pop "("
//                }
//            }
//        }
//
//        while (operators.isNotEmpty()) {
//            val op = operators.pop()
//            if (op == "(" || op == ")") {
//                throw IllegalArgumentException("Mismatched parentheses")
//            }
//            output.add(op)
//        }
//
//        return output
//    }
//
//    private fun evaluateRPN(tokens: List<String>): Double {
//        val stack = ArrayDeque<Double>()
//
//        for (token in tokens) {
//            when {
//                isNumber(token) -> {
//                    stack.push(token.toDouble())
//                }
//                isFunction(token) -> {
//                    if (stack.isEmpty()) throw IllegalArgumentException("Not enough operands for function $token")
//                    val value = stack.pop()
//                    val result = when (token) {
//                        "sin" -> Math.sin(Math.toRadians(value))
//                        "cos" -> Math.cos(Math.toRadians(value))
//                        "tan" -> Math.tan(Math.toRadians(value))
//                        "log" -> {
//                            if (value <= 0) throw IllegalArgumentException("log domain error")
//                            Math.log10(value)
//                        }
//                        "ln" -> {
//                            if (value <= 0) throw IllegalArgumentException("ln domain error")
//                            Math.log(value)
//                        }
//                        "√" -> {
//                            if (value < 0) throw IllegalArgumentException("sqrt domain error")
//                            Math.sqrt(value)
//                        }
//                        else -> throw IllegalArgumentException("Unknown function: $token")
//                    }
//                    stack.push(result)
//                }
//                isOperator(token) -> {
//                    if (stack.size < 2) throw IllegalArgumentException("Not enough operands for operator $token")
//                    val b = stack.pop()
//                    val a = stack.pop()
//                    val result = when (token) {
//                        "+" -> a + b
//                        "-" -> a - b
//                        "*" -> a * b
//                        "/" -> {
//                            if (b == 0.0) throw ArithmeticException("Division by zero")
//                            a / b
//                        }
//                        "^" -> Math.pow(a, b)
//                        else -> throw IllegalArgumentException("Unknown operator: $token")
//                    }
//                    stack.push(result)
//                }
//                else -> {
//                    throw IllegalArgumentException("Invalid token encountered: $token")
//                }
//            }
//        }
//
//        if (stack.size != 1) {
//            throw IllegalArgumentException("Invalid expression")
//        }
//        return stack.pop()
//    }
//
//    private fun isNumber(token: String): Boolean {
//        return try {
//            token.toDouble()
//            true
//        } catch (e: NumberFormatException) {
//            false
//        }
//    }
//
//    private fun isOperator(token: String): Boolean {
//        return "+-*/^".contains(token)
//    }
//
//    private fun isFunction(token: String): Boolean {
//        // Check against known functions
//        return token in listOf("sin", "cos", "tan", "ln", "log", "√")
//    }
//}

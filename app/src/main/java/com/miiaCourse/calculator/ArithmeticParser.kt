package com.miiaCourse.calculator

import android.util.Log
import com.miiaCourse.calculator.CalculatorViewModel.Companion.functions
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

object ArithmeticParser {
    lateinit var viewModel: CalculatorViewModel
    /**
     * Parses a subexpression enclosed by the given open and close characters.
     * For example, given an expression like "2+sin(30)", calling
     * parseSubexpression("2+sin(30)", startIndex after '(', '(', ')')
     * would return the tokens inside the parentheses and the index after ')'.
     *
     * This function performs a recursive-like tokenize on the substring between the delimiters.
     */
    private fun parseSubexpression(
        expression: String,
        startIndex: Int,
        openChar: Char,
        closeChar: Char
    ): Pair<List<String>, Int> {
        val subTokens = mutableListOf<String>()
        var i = startIndex
        while (i < expression.length) {
            val c = expression[i]

            when {
                c.isWhitespace() -> {
                    i++
                }

                c.isDigit() || c == '.' -> {
                    // Parse number
                    val start = i
                    while (i < expression.length && (expression[i].isDigit() || expression[i] == '.')) i++
                    subTokens.add(expression.substring(start, i))
                    continue
                }

                c == 'e' -> {
                    subTokens.add(Math.E.toString())
                    i++
                }

                c == 'π' -> {
                    subTokens.add(Math.PI.toString())
                    i++
                }

                c in "+-×÷^()" -> {
                    when (c) {
                        '(' -> {
                            // Parse nested ()
                            val (innerTokens, newIndex) = parseSubexpression(
                                expression,
                                i + 1,
                                '(',
                                ')'
                            )

                            /**
                             * innerTokens is already a complete set of subtokens
                             * To maintain structure, we can directly add these tokens into subTokens
                             * Alternatively, we could add "(" ...tokens... ")" to keep the structure clear
                             * Here we directly add the tokens and leave "(" ")" outside for easier postfix processing
                             */

                            subTokens.add("(")
                            subTokens.addAll(innerTokens)
                            subTokens.add(")")
                            i = newIndex
                        }

                        ')' -> {
                            // Return upon encountering the corresponding closing parenthesis
                            if (closeChar != c) throw IllegalArgumentException("Mismatched parentheses.")
                            return Pair(subTokens, i + 1)
                        }

                        else -> {
                            // Regular operator
                            subTokens.add(c.toString())
                            i++
                        }
                    }
                }
                expression.startsWith("!",i)->{
                    subTokens.add("!")
                    i++
                }
                // Function handling: sin, cos, tan, log, ln, √
                expression.startsWith("Ans", i) -> {
                    subTokens.add("Ans")
                    i += 3
                }
                expression.startsWith("sin", i) -> {
                    subTokens.add("sin")
                    i += 3
                }

                expression.startsWith("cos", i) -> {
                    subTokens.add("cos")
                    i += 3
                }

                expression.startsWith("tan", i) -> {
                    subTokens.add("tan")
                    i += 3
                }

                expression.startsWith("ln", i) -> {
                    subTokens.add("ln")
                    i += 2
                }
                expression.startsWith("√[",i) -> {
                    subTokens.add("sqrt_base")
                    val (sqrtBaseTokens, sqrtBaseEnd) = parseSubexpression(expression, i + 2, '[', ']')
                    subTokens.add("(")
                    subTokens.addAll(sqrtBaseTokens)
                    subTokens.add(")")
                    i = sqrtBaseEnd
                }
                expression.startsWith("√", i) -> {
                    subTokens.add("√")
                    i++
                }

                expression.startsWith("log[", i) -> {
                    subTokens.add("log_base")
                    // Parse the base part of log[...]
                    val (logBaseTokens, logBaseEnd) = parseSubexpression(expression, i + 4, '[', ']')
                    // Enclose the base with "(" ")"
                    subTokens.add("(")
                    subTokens.addAll(logBaseTokens)
                    subTokens.add(")")
                    i = logBaseEnd

                    // Next we should encounter ( ), parse the b part of log[a](b)
                    if (i < expression.length && expression[i] == '(') {
                        val (argTokens, argEnd) = parseSubexpression(expression, i + 1, '(', ')')
                        // Enclose the b tokens with "(" ")"
                        subTokens.add("(")
                        subTokens.addAll(argTokens)
                        subTokens.add(")")
                        i = argEnd
                    } else {
                        throw IllegalArgumentException("Missing '(...)' after log[...] base specification.")
                    }
                }
                
                expression.startsWith("log", i) -> {
                    subTokens.add("log")
                    i += 3
                }
                expression.startsWith("C[", i) -> {  // 處理 "C["
                    // 解析 a 的值
                    val (aTokens, aEnd) = parseSubexpression(expression, i + 2, '[', ',')  //  解析 C[a,
                    // 檢查 aTokens 是否為空，如果為空則丟擲異常
                    if (aTokens.isEmpty()) {
                        throw IllegalArgumentException("Missing first value for C[,] operation.")
                    }
                    // 處理 aTokens，例如將其轉換為 Int
                    val aValue = aTokens.joinToString("").toIntOrNull()
                        ?: throw IllegalArgumentException("Invalid first value for C[,] operation.")

                    // 解析 b 的值
                        if (aEnd < expression.length && expression[aEnd] == ',') {
                        val (bTokens, bEnd) = parseSubexpression(expression, aEnd + 1, ',', ']')
                        // 檢查 bTokens 是否為空，如果為空則丟擲異常
                        if (bTokens.isEmpty()) {
                            throw IllegalArgumentException("Missing second value for C[,] operation.")
                        }
                        // 處理 bTokens，例如將其轉換為 Int
                        val bValue = bTokens.joinToString("").toIntOrNull()
                            ?: throw IllegalArgumentException("Invalid second value for C[,] operation.")

                        // 將 a 和 b 的值添加到 subTokens 列表中
                        subTokens.add(aValue.toString())  // 添加 a 的值
                        subTokens.add(bValue.toString())  // 添加 b 的值
                        subTokens.add("C")  // 使用 "C" 表示組合數運算

                        i = bEnd + 1  // 跳過 ']'
                    } else {
                        throw IllegalArgumentException("Invalid format for C[,] operation.")
                    }
                }
                // Return upon encountering the closing character
                c == closeChar -> {
                    return Pair(subTokens, i + 1)
                }
                // Throw errors if there is any invalid character
                else -> {
                    throw IllegalArgumentException("Invalid character at position $i: ${expression[i]}")
                }
            }
        }


        if (closeChar != '\u0000') {
            /**
             * If a closing character (like ) or ]) is expected but not found at the end of the string,
             * it indicates mismatched parentheses/brackets
             */
            throw IllegalArgumentException("Mismatched $openChar and $closeChar in expression.")
        }

        return Pair(subTokens, expression.length)
    }


    /**
     * Tokenizes the given expression into a list of tokens.
     * This updated version can handle log[a](b) structures, where a and b are subexpressions.
     */
    fun tokenize(expression: String): List<String> {
        Log.d("CalculatorViewModel", "Tokenizing expression: $expression")
        /**
         * Use parseSubexpression to generically handle the entire expression
         * We can assume no specific enclosing characters at the outermost level, so we use '\u0000' to represent no explicit closing character.
         */
        val (tokens, _) = parseSubexpression(expression, 0, '\u0000', '\u0000')
        Log.d("CalculatorViewModel", "Tokens: $tokens")
        return tokens
    }
     private fun factorial(n: Int): Int {
        if (n == 0) return 1
        return n * factorial(n - 1)
     }
     private fun nCr(n: Int, r: Int): Int {
        if (r == 0 || n == r) return 1
        return nCr(n - 1, r - 1) + nCr(n - 1, r)
     }
    /**
     * Inserts implicit multiplication into the list of tokens where applicable.
     * For example:
     * - `2(3)` becomes `2×(3)`
     * - `3sin(45)` becomes `3×sin(45)`
     * - `(2)(3)` becomes `(2)×(3)`
     */
    fun insertImplicitMultiplication(tokens: List<String>): List<String> {
        Log.d("CalculatorViewModel", "Inserting implicit multiplication...")
        if (tokens.isEmpty()) return tokens
        val result = mutableListOf<String>()
        val functions = setOf("sin", "cos", "tan", "log", "ln", "√", "log_base", "sqrt_base")

        for ((index, token) in tokens.withIndex()) {
            result.add(token)
            if (index < tokens.lastIndex) {
                val next = tokens[index + 1]

                val currentIsValue =
                    token.matches(Regex("[0-9.]+")) || token == ")" || token == Math.E.toString() || token == Math.PI.toString() || token == "Ans"
                val nextIsValueOrFunc =
                    next.matches(Regex("[0-9.]+")) || next == "(" || next in functions || next == Math.E.toString() || next == Math.PI.toString()

                /**
                Condition to insert implicit multiplication:
                1. Current token is a value (number, ')', e, pi)
                2. Next token is a value or a function
                3. Next token is not ')'
                4. Current token is not '('
                5. Additional Conditions:
                a. Do not insert '×' if current token is ')' and next token is '('
                b. Do not insert '×' if current token is a function (e.g., 'log_base') and next token is '('
                 */
                val shouldInsertMultiplication =
                    (currentIsValue || token == "!") && nextIsValueOrFunc && next != ")" && token != "(" &&  //  將 token == "!" 添加到 currentIsValue 的條件中
                            !(token == ")" && next == "(") &&
                            !(token in functions && next == "(") &&
                            !(token == "]" && next == "√")

                if (shouldInsertMultiplication) {
                    result.add("×")  // Insert implicit multiplication
                    Log.d(
                        "CalculatorViewModel",
                        "Inserted implicit multiplication '×' between '$token' and '$next'"
                    )
                }else{
                    Log.d(
                        "CalculatorViewModel",
                        "Not Inserted implicit multiplication '×' between '$token' and '$next'"
                    )
                }
            }
        }
        Log.d("CalculatorViewModel", "Tokens after implicit multiplication: $result")
        return result
    }

    /**
     * Converts an infix expression to postfix notation using the Shunting-yard algorithm.
     * Supports standard operators (+, -, ×, ÷, ^), constants (e, π), and functions (sin, cos, tan, log, ln, √).
     */
    fun infixToPostfix(tokens: List<String>): List<String> {
        Log.d("CalculatorViewModel", "Converting to postfix...")
        val output = mutableListOf<String>()    // List to store the output postfix expression
        val operators = ArrayDeque<String>()    // Stack to hold operators and functions

        // Operator precedence and associativity
        val precedence = mapOf(
            "+" to 1, "-" to 1,
            "×" to 2, "÷" to 2,
            "^" to 3,                            // Exponentiation has higher precedence
            "sin" to 4, "cos" to 4, "tan" to 4, "log" to 4, "ln" to 4, "√" to 4,
            "log_base" to 4, "sqrt_base" to 4, "!" to 4, "C" to 4
        )

        // Process each token in the infix expression
        for (token in tokens) {
            when {
                // Numbers or constants (e, π) go directly to the output
                token.matches(Regex("[0-9.]+")) -> output.add(token)
                token == Math.E.toString() || token == Math.PI.toString() -> output.add(token)

                token == "Ans" -> output.add(viewModel.Ans.value)
                token.isEmpty() -> {  // 處理空字串
                    if (viewModel.Ans.value.isNotEmpty()) {  // 檢查 viewModel.Ans.value 是否為空
                        output.add(viewModel.Ans.value)  // 添加上一次的答案
                    }
                }

                // Functions (sin, cos, tan, log, ln, √) are pushed to the stack
                token in listOf("sin", "cos", "tan", "log", "ln", "√", "log_base", "sqrt_base", "!", "C") -> {
                    operators.addLast(token)
                }

                // Left parenthesis: push to the stack; Right parenthesis: pop operators until a left parenthesis is found
                token == "(" || token == "[" -> operators.addLast(token)

                token == ")" || token == "]" -> {
                    val matchingLeftParenthesis = if (token == ")") "(" else "["
                    while (operators.isNotEmpty() && operators.last() != matchingLeftParenthesis) {
                        output.add(operators.removeLast())
                    }
                    if (operators.isEmpty()) throw IllegalArgumentException("Mismatched parentheses.")
                    operators.removeLast() // remove the matching left parenthesis

                    // If a function is on the stack, pop it to the output
                    if (operators.isNotEmpty() && operators.last() in functions) {
                        output.add(operators.removeLast())
                    }
                }

                // Operators (+, -, ×, ÷, ^)
                token in listOf("+", "-", "×", "÷", "^", "log_base") -> {
                    // Handle right-associativity for ^ (exponentiation)
                    val tokenPrecedence = precedence[token]!!
                    while (operators.isNotEmpty()) {
                        val top = operators.last()
                        val topPrecedence = precedence.getOrDefault(top, 0)
                        if (top == "(") break  // Stop at left parenthesis
                        if (token == "^") {
                            if (topPrecedence > tokenPrecedence) {
                                output.add(operators.removeLast())
                            } else break
                        } else {
                            if (topPrecedence >= tokenPrecedence) {
                                output.add(operators.removeLast())
                            } else break
                        }
                    }
                    operators.addLast(token)  // Push the current operator onto the stack
                }
                // Invalid token
                else -> throw IllegalArgumentException("Invalid token: $token")
            }
        }

        // Pop any remaining operators from the stack to the output
        while (operators.isNotEmpty()) {
            val op = operators.removeLast()
            if (op == "(" || op == ")") throw IllegalArgumentException("Mismatched parentheses.")
            output.add(op)
        }
        Log.d("CalculatorViewModel", "Postfix tokens: $output")
        return output
    }

    /**
     * Evaluates a postfix expression and returns the result as a formatted string.
     */
    fun evaluatePostfix(postfix: List<String>): String {
        Log.d("CalculatorViewModel", "Evaluating postfix: $postfix")
        val stack = ArrayDeque<Double>()

        // Process each token in the postfix expression
        for (token in postfix) {
            Log.d("CalculatorViewModel", "Processing token: $token")

            when (token) {
                "+" -> stack.addLast(stack.removeLast() + stack.removeLast())
                "-" -> stack.addLast(-stack.removeLast() + stack.removeLast())
                "×" -> stack.addLast(stack.removeLast() * stack.removeLast())
                "÷" -> {
                    val b = stack.removeLast()
                    val a = stack.removeLast()
                    stack.addLast(a / b)
                }

                "^" -> {
                    val b = stack.removeLast()
                    val a = stack.removeLast()
                    stack.addLast(a.pow(b))
                }

                "!" -> {  //  處理階乘
                    val operand = stack.removeLast().toInt()  //  取得運算元，並轉換為 Int
                    if (operand < 0) {
                        throw IllegalArgumentException("Factorial is not defined for negative numbers.")
                    }
                    stack.addLast(factorial(operand).toDouble())  //  計算階乘，並將結果推回堆疊
                }

                "sin" -> stack.addLast(sin(Math.toRadians(stack.removeLast())))
                "cos" -> stack.addLast(cos(Math.toRadians(stack.removeLast())))
                "tan" -> stack.addLast(tan(Math.toRadians(stack.removeLast())))
                "log" -> stack.addLast(log10(stack.removeLast()))
                "ln" -> stack.addLast(ln(stack.removeLast()))
                "√" -> stack.addLast(sqrt(stack.removeLast()))

                // Process log_base: Pop the base and value, and calculate log(b)/log(a)
                "log_base" -> {
                    val b = stack.removeLast() // value
                    val a = stack.removeLast() // base
                    if (a <= 0 || b <= 0) throw IllegalArgumentException("Logarithm base and value must be positive")
                    stack.addLast(log10(b) / log10(a))
                }
                "sqrt_base" -> {
                    val b = stack.removeLast() // value
                    val a = stack.removeLast() // base
                    if (a == 0.0) {
                        throw IllegalArgumentException("Cannot calculate 0th root.")
                    }
                    stack.addLast(b.pow(1.0 / a))
                }
                "C" -> {
                    val b = stack.removeLast().toInt()  //  取得 b 的值
                    val a = stack.removeLast().toInt()  //  取得 a 的值
                    // 檢查 a 和 b 的值是否有效，例如 a >= b, a >= 0, b >= 0 等等
                    if (a < 0 || b < 0 || a < b) {
                        throw IllegalArgumentException("Invalid values for C operation.")
                    }
                    stack.addLast(nCr(a, b).toDouble())  //  計算 C(a, b)，並將結果推回堆疊
                }
                // Default case: Assume the token is a number and push it onto the stack
                else -> {
                    try {
                        stack.addLast(token.toDouble())
                    } catch (e: NumberFormatException) {
                        throw IllegalArgumentException("Invalid token in postfix expression: $token")
                    }
                }
            }
        }


        // Ensure only one result remains on the stack
        if (stack.size != 1) throw IllegalArgumentException("Invalid postfix expression.")

        // Retrieve the final result
        val result = stack.last()
        Log.d("CalculatorViewModel", "Final result: $result")

        // Format the result to remove trailing zeros for cleaner output
        val formattedResult = if (result % 1 == 0.0) {
            result.toInt().toString() // Convert to integer if the result is whole
        } else {
            String.format("%.12f", result).trimEnd('0').trimEnd('.')  // Trim unnecessary trailing zeros
        }

        return formattedResult
    }
}
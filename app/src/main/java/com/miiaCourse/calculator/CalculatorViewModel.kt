package com.miiaCourse.calculator

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import kotlin.math.*

/**
 * CalculatorViewModel handles the logic of a calculator app,
 * including expression parsing, implicit multiplication, infix-to-postfix conversion,
 * and evaluating postfix expressions. This class uses state management to dynamically
 * update and reflect calculation results.
 */
class CalculatorViewModel : ViewModel() {

    // The current expression being edited by the user.
    var currentExpression by mutableStateOf(TextFieldValue(""))

    // Holds the automatically evaluated result of the current expression.
    val evaluationResult = mutableStateOf("")

    /**
     * Clears the current expression and evaluation result.
     */
    fun clear() {
        Log.d("CalculatorViewModel", "Clearing expression and result")
        currentExpression = TextFieldValue("")
        evaluationResult.value = ""
    }

    /**
     * Adds a character (e.g., numbers, operators, functions) to the current expression
     * at the current cursor position.
     * @param character The character to be added.
     */
    fun addCharacterToExpression(character: String) {
        val currentText = currentExpression.text
        val cursorPosition = currentExpression.selection.start

        val newText = StringBuilder(currentText)
        newText.insert(cursorPosition, character)

        currentExpression = TextFieldValue(
            newText.toString(),
            selection = androidx.compose.ui.text.TextRange(cursorPosition + character.length)
        )
        Log.d("CalculatorViewModel", "Updated expression: $newText")
        autoCalculateResult()
    }

    /**
     * Removes the last character before the cursor from the current expression.
     */
    fun removeLastCharacter() {
        val currentText = currentExpression.text
        val cursorPosition = currentExpression.selection.start
        if (currentText.isEmpty() || cursorPosition == 0) return

        val updatedText = StringBuilder(currentText)
        updatedText.deleteCharAt(cursorPosition - 1)

        currentExpression = TextFieldValue(
            updatedText.toString(),
            selection = androidx.compose.ui.text.TextRange(cursorPosition - 1)
        )

        Log.d("CalculatorViewModel", "Expression after removal: $updatedText")
        autoCalculateResult()
    }

    /**
     * Performs a full calculation of the current expression.
     * If successful, updates the expression with the calculated result.
     * If any error occurs, the result is marked as invalid.
     */
    fun calculateResult() {
        try {
            Log.d("CalculatorViewModel", "Starting calculation for expression: ${currentExpression.text}")

            val expr = currentExpression.text
            val tokens = tokenize(expr)
            Log.d("CalculatorViewModel", "Tokenized expression: $tokens")

            val finalTokens = insertImplicitMultiplication(tokens)
            Log.d("CalculatorViewModel", "After implicit multiplication: $finalTokens")

            val postfix = infixToPostfix(finalTokens)
            Log.d("CalculatorViewModel", "Postfix notation: $postfix")

            val result = evaluatePostfix(postfix)
            Log.d("CalculatorViewModel", "Final result: $result")

            currentExpression = TextFieldValue(result, selection = androidx.compose.ui.text.TextRange(result.length))
            evaluationResult.value = ""
        } catch (e: Exception) {
            Log.e("CalculatorViewModel", "Error during calculation: ${e.message}")
            evaluationResult.value = "Invalid Syntaxes"
        }
    }

    /**
     * Automatically calculates the result of the current expression as the user types.
     */
    private fun autoCalculateResult() {
        try {
            val expr = currentExpression.text
            if (expr.isBlank()) {
                evaluationResult.value = ""
                return
            }
            Log.d("CalculatorViewModel", "Auto-calculating for expression: $expr")

            val tokens = tokenize(expr)
            Log.d("CalculatorViewModel", "Tokenized expression: $tokens")

            val finalTokens = insertImplicitMultiplication(tokens)
            Log.d("CalculatorViewModel", "After implicit multiplication: $finalTokens")

            val postfix = infixToPostfix(finalTokens)
            Log.d("CalculatorViewModel", "Postfix notation: $postfix")

            val result = evaluatePostfix(postfix)
            Log.d("CalculatorViewModel", "Auto-calculated result: $result")

            evaluationResult.value = result
        } catch (e: Exception) {
            Log.e("CalculatorViewModel", "Auto-calculation error: ${e.message}")
            evaluationResult.value = ""
        }
    }

    /**
     * Parses a subexpression enclosed by the given open and close characters.
     * For example, given an expression like "2+sin(30)", calling
     * parseSubexpression("2+sin(30)", startIndex after '(', '(', ')')
     * would return the tokens inside the parentheses and the index after ')'.
     *
     * This function performs a recursive-like tokenize on the substring between the delimiters.
     */
    private fun parseSubexpression(expression: String, startIndex: Int, openChar: Char, closeChar: Char): Pair<List<String>, Int> {
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
                            val (innerTokens, newIndex) = parseSubexpression(expression, i+1, '(', ')')

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
                            return Pair(subTokens, i+1)
                        }
                        else -> {
                            // Regular operator
                            subTokens.add(c.toString())
                            i++
                        }
                    }
                }
                // Function handling: sin, cos, tan, log, ln, √
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
                expression.startsWith("√", i) -> {
                    subTokens.add("√")
                    i++
                }
                expression.startsWith("log[", i) -> {
                    subTokens.add("log_base")
                    // Parse the base part of log[...]
                    val (baseTokens, baseEnd) = parseSubexpression(expression, i+4, '[', ']')
                    // Enclose the base with "(" ")"
                    subTokens.add("(")
                    subTokens.addAll(baseTokens)
                    subTokens.add(")")
                    i = baseEnd

                    // Next we should encounter ( ), parse the b part of log[a](b)
                    if (i < expression.length && expression[i] == '(') {
                        val (argTokens, argEnd) = parseSubexpression(expression, i+1, '(', ')')
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

                // Return upon encountering the closing character
                c == closeChar -> {
                    return Pair(subTokens, i+1)
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
    private fun tokenize(expression: String): List<String> {
        Log.d("CalculatorViewModel", "Tokenizing expression: $expression")
        /**
         * Use parseSubexpression to generically handle the entire expression
         * We can assume no specific enclosing characters at the outermost level, so we use '\u0000' to represent no explicit closing character.
        */
        val (tokens, _) = parseSubexpression(expression, 0, '\u0000', '\u0000')
        Log.d("CalculatorViewModel", "Tokens: $tokens")
        return tokens
    }


    /**
     * Inserts implicit multiplication into the list of tokens where applicable.
     * For example:
     * - `2(3)` becomes `2×(3)`
     * - `3sin(45)` becomes `3×sin(45)`
     * - `(2)(3)` becomes `(2)×(3)`
     */
    private fun insertImplicitMultiplication(tokens: List<String>): List<String> {
        Log.d("CalculatorViewModel", "Inserting implicit multiplication...")
        if (tokens.isEmpty()) return tokens
        val result = mutableListOf<String>()
        val functions = setOf("sin","cos","tan","log","ln","√","log_base")

        for ((index, token) in tokens.withIndex()) {
            result.add(token)
            if (index < tokens.lastIndex) {
                val next = tokens[index + 1]

                val currentIsValue =
                    token.matches(Regex("[0-9.]+")) || token == ")" || token == Math.E.toString() || token == Math.PI.toString()
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
                val shouldInsertMultiplication = currentIsValue && nextIsValueOrFunc && next != ")" && token != "(" &&
                        !(token == ")" && next == "(") &&
                        !(token in functions && next == "(")

                if (shouldInsertMultiplication) {
                    result.add("×")  // Insert implicit multiplication
                    Log.d("CalculatorViewModel", "Inserted implicit multiplication '×' between '$token' and '$next'")
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
    private fun infixToPostfix(tokens: List<String>): List<String> {
        Log.d("CalculatorViewModel", "Converting to postfix...")
        val output = mutableListOf<String>()    // List to store the output postfix expression
        val operators = ArrayDeque<String>()    // Stack to hold operators and functions

        // Operator precedence and associativity
        val precedence = mapOf(
            "+" to 1, "-" to 1,
            "×" to 2, "÷" to 2,
            "^" to 3,                            // Exponentiation has higher precedence
            "sin" to 4, "cos" to 4, "tan" to 4, "log" to 4, "ln" to 4, "√" to 4,
            "log_base" to 4
        )

        // Process each token in the infix expression
        for (token in tokens) {
            when {
                // Numbers or constants (e, π) go directly to the output
                token.matches(Regex("[0-9.]+")) -> output.add(token)
                token == Math.E.toString() || token == Math.PI.toString() -> output.add(token)

                // Functions (sin, cos, tan, log, ln, √) are pushed to the stack
                token in listOf("sin","cos","tan","log","ln","√","log_base") -> {
                    operators.addLast(token)
                }

                // Left parenthesis: push to the stack; Right parenthesis: pop operators until a left parenthesis is found
                token == "(" -> operators.addLast(token)
                token == "[" -> operators.addLast(token)

                token == ")" -> {
                    while (operators.isNotEmpty() && operators.last() != "(") {
                        output.add(operators.removeLast())
                    }
                    if (operators.isEmpty()) throw IllegalArgumentException("Mismatched parentheses.")
                    operators.removeLast() // remove "("
                    // If a function is on the stack, pop it to the output
                    if (operators.isNotEmpty() && operators.last() in functions) {
                        output.add(operators.removeLast())
                    }
                }
                token == "]" -> {
                    while (operators.isNotEmpty() && operators.last() != "(") {
                        output.add(operators.removeLast())
                    }
                    if (operators.isEmpty()) throw IllegalArgumentException("Mismatched parentheses.")
                    operators.removeLast() // remove "("
                    // If a function is on the stack, pop it to the output
                    if (operators.isNotEmpty() && operators.last() in functions) {
                        output.add(operators.removeLast())
                    }
                }

                // Operators (+, -, ×, ÷, ^)
                token in listOf("+","-","×","÷","^","log_base") -> {
                    // Handle right-associativity for ^ (exponentiation)
                    val tokenPrecedence = precedence[token]!!
                    while (operators.isNotEmpty()) {
                        val top = operators.last()
                        val topPrecedence = precedence.getOrDefault(top,0)
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
    private fun evaluatePostfix(postfix: List<String>): String {
        Log.d("CalculatorViewModel", "Evaluating postfix: $postfix")
        val stack = ArrayDeque<Double>()

        // Process each token in the postfix expression
        for (token in postfix) {
            Log.d("CalculatorViewModel", "Processing token: $token")

            when(token) {
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
            String.format("%.10f", result).trimEnd('0')  // Trim unnecessary trailing zeros
        }

        return formattedResult
    }

    companion object {
        private val functions = setOf("sin","cos","tan","log","ln","√","log[")
    }
}

package com.miiaCourse.calculator

import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * ViewModel for the Calculator screen.
 * Handles the logic for calculations and manages the state of the expression and result.
 */
class CalculatorViewModel : ViewModel() {
    /**
     * The current expression being entered by the user.
     * Uses TextFieldValue to manage text and cursor position.
     */
    var currentExpression by mutableStateOf(TextFieldValue(""))

    /**
     * The result of the evaluation of the current expression.
     * Updated whenever the expression changes or the user requests calculation.
     */
    val evaluationResult = mutableStateOf("")

    /**
     * Clears the current expression and the evaluation result.
     */
    fun clear() {
        Log.d(TAG, "Clearing expression and result.")
        currentExpression = TextFieldValue("")
        evaluationResult.value = ""
    }

    /**
     * Adds a character to the current expression at the cursor position.
     *
     * @param character The character to be added.
     */
    fun addCharacterToExpression(character: String) {
        val currentText = currentExpression.text
        val cursorPosition = currentExpression.selection.start

        Log.d(TAG, "Adding character: $character at position: $cursorPosition in expression: $currentText")

        val newText = StringBuilder(currentText)

        // Logic to insert a multiplication symbol
        if (character == "(") {
            if (cursorPosition > 0) {
                val prevChar = currentText[cursorPosition - 1]

                // If the previous character is a digit or a right parenthesis, insert a multiplication symbol before "("
                if (prevChar.isDigit() || prevChar == ')') {
                    newText.insert(cursorPosition, "×")
                }
            }
        }

        // Check for ')' case, insert multiplication if a digit is entered after ')'
        if (character.first().isDigit()) {
            if (cursorPosition > 0) {
                val prevChar = currentText[cursorPosition - 1]

                // If the previous character is ')', insert a multiplication symbol before the digit
                if (prevChar == ')') {
                    newText.insert(cursorPosition, "×")
                }
            }
        }

        // Insert the current character
        newText.insert(cursorPosition, character)

        // Update currentExpression
        currentExpression = TextFieldValue(
            newText.toString(),
            selection = androidx.compose.ui.text.TextRange(cursorPosition + character.length)
        )

        Log.d(TAG, "Updated expression: $newText")

        autoCalculateResult()
    }

    /**
     * Removes the last character from the current expression.
     */
    fun removeLastCharacter() {
        val currentText = currentExpression.text
        val cursorPosition = currentExpression.selection.start

        if (currentText.isEmpty() || cursorPosition == 0) return

        // Define all supported function names (including functions that need parentheses)
        val functions = listOf("sin(", "cos(", "tan(", "log(", "ln(", "√(")

        val updatedText = StringBuilder(currentText)
        var newPosition = cursorPosition - 1

        // Check if there is a function name and parenthesis before the cursor (e.g., sin(, cos()
        var deletedFunction = false
        for (function in functions) {
            val functionLength = function.length
            if (newPosition - functionLength + 1 >= 0 &&
                currentText.substring(newPosition - functionLength + 1, newPosition + 1) == function
            ) {
                updatedText.delete(newPosition - functionLength + 1, newPosition + 1)
                newPosition -= functionLength
                deletedFunction = true
                break
            }
        }

        // If it's not a function name, delete a single character
        if (!deletedFunction) {
            updatedText.deleteCharAt(newPosition)
            newPosition--
        }

        // Update currentExpression and the cursor position
        currentExpression = TextFieldValue(
            updatedText.toString(),
            selection = androidx.compose.ui.text.TextRange(maxOf(newPosition + 1, 0))
        )

        Log.d("CalculatorViewModel", "Updated expression after removal: ${currentExpression.text}")
        autoCalculateResult()
    }

    /**
     * Calculates the result of the current expression.
     * Updates the currentExpression with the result and clears the evaluationResult.
     */
    fun calculateResult() {
        try {
            Log.d(TAG, "Calculating result for expression: ${currentExpression.text}")
            val postfixExpression = infixToPostfix(currentExpression.text)
            val result = evaluatePostfix(postfixExpression)

            Log.d(TAG, "Postfix expression: $postfixExpression")
            Log.d(TAG, "Calculated result: $result")

            // Update currentExpression with the result and clear evaluationResult
            currentExpression = TextFieldValue(
                result,
                selection = androidx.compose.ui.text.TextRange(result.length)
            )
            evaluationResult.value = ""
        } catch (e: Exception) {
            Log.e(TAG, "Error calculating result: ${e.message}")
            evaluationResult.value = "Invalid Syntaxes"
        }
    }

    /**
     * Automatically calculates the result of the current expression as the user types.
     * Updates the evaluationResult with the calculated value.
     */
    private fun autoCalculateResult() {
        try {
            Log.d(TAG, "Auto-calculating for expression: ${currentExpression.text}")
            val postfixExpression = infixToPostfix(currentExpression.text)
            val result = evaluatePostfix(postfixExpression)

            Log.d(TAG, "Auto-calculated postfix: $postfixExpression")
            Log.d(TAG, "Auto-calculated result: $result")

            evaluationResult.value = result
        } catch (e: Exception) {
            // If an error occurs during calculation, clear the evaluationResult
            Log.e(TAG, "Error during auto-calculation: ${e.message}")
            evaluationResult.value = ""
        }
    }

    /**
     * Converts an infix expression to postfix notation.
     *
     * @param expression The infix expression string.
     * @return A list of tokens representing the postfix expression.
     */
    private fun infixToPostfix(expression: String): List<String> {
        Log.d(TAG, "Converting to postfix: $expression")
        val output = mutableListOf<String>()
        val operators = ArrayDeque<String>()
        // Define operator precedence
        val precedence = mapOf('+' to 1, '-' to 1, '×' to 2, '÷' to 2, '^' to 3, "log" to 4, "ln" to 4, "sin" to 4, "cos" to 4, "tan" to 4, "√" to 4)

        var number = ""
        var i = 0
        var lastWasNumberOrRightParenthesis = false

        while (i < expression.length) {
            val char = expression[i]

            when {
                // Handle numbers and decimal points
                char.isDigit() || char == '.' -> {
                    number += char
                    lastWasNumberOrRightParenthesis = true

                }

                // Handle function names, such as sin, cos, tan
                expression.startsWith("sin", i) || expression.startsWith("cos", i) ||
                        expression.startsWith("tan", i) || expression.startsWith("log", i) ||
                        expression.startsWith("ln", i) || expression.startsWith("√", i) -> {
                    if (number.isNotEmpty()) {
                        output.add(number)
                        number = ""

                        if (lastWasNumberOrRightParenthesis) {
                            operators.addLast("×")
                        }
                    }
                    val functionName = when {
                        expression.startsWith("sin", i) -> "sin"
                        expression.startsWith("cos", i) -> "cos"
                        expression.startsWith("tan", i) -> "tan"
                        expression.startsWith("log", i) -> "log"
                        expression.startsWith("ln", i) -> "ln"
                        expression.startsWith("√", i) -> "√"
                        else -> ""
                    }
                    operators.addLast(functionName) // Push the function onto the stack
                    i += functionName.length - 1 // Skip the length of the function name
                }

                // Handle left parenthesis
                char == '(' -> {
                    if (number.isNotEmpty()) {
                        output.add(number)
                        number = ""
                    }
                    operators.addLast(char.toString())
                }

                // Handle right parenthesis
                char == ')' -> {
                    if (number.isNotEmpty()) {
                        output.add(number)
                        number = ""
                    }
                    while (operators.isNotEmpty() && operators.last() != "(") {
                        output.add(operators.removeLast())
                    }
                    operators.removeLast() // Remove the left parenthesis
                    if (operators.isNotEmpty() && operators.last() in precedence.keys) {
                        output.add(operators.removeLast()) // If there is a function name, put it in the output
                    }
                }

                // Handle operators
                else -> {
                    if (number.isNotEmpty()) {
                        output.add(number)
                        number = ""
                    }
                    while (operators.isNotEmpty() &&
                        precedence.getOrDefault(operators.last(), 0) >= precedence.getOrDefault(char.toString(), 0)
                    ) {
                        output.add(operators.removeLast())
                    }
                    operators.addLast(char.toString())
                }
            }
            i++
        }

        if (number.isNotEmpty()) output.add(number)
        while (operators.isNotEmpty()) output.add(operators.removeLast())

        Log.d("CalculatorViewModel", "Postfix result: $output")
        return output
    }

    /**
     * Evaluates a postfix expression and returns the result.
     *
     * @param postfix The postfix expression as a list of tokens.
     * @return The calculated result of the expression.
     */
    private fun evaluatePostfix(postfix: List<String>): String {
        Log.d(TAG, "Evaluating postfix expression: $postfix")
        val stack = ArrayDeque<Double>()
        for (token in postfix) {
            Log.d(TAG, "Processing token: $token")
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

                "sin" -> stack.addLast(sin(Math.toRadians(stack.removeLast())))
                "cos" -> stack.addLast(cos(Math.toRadians(stack.removeLast())))
                "tan" -> stack.addLast(tan(Math.toRadians(stack.removeLast())))
                "log" -> stack.addLast(log10(stack.removeLast()))
                "ln" -> stack.addLast(ln(stack.removeLast()))
                "√" -> stack.addLast(sqrt(stack.removeLast()))
                else -> stack.addLast(token.toDouble())
            }
        }

        val result = stack.last()
        Log.d(TAG, "Final result: $result")

        val formattedResult = if (result % 1 == 0.0) {
            result.toInt().toString()
        } else {
            String.format("%.10f", result).trimEnd('0')
        }
        Log.d(TAG, "formattedResult: $formattedResult")
        return formattedResult
    }
}
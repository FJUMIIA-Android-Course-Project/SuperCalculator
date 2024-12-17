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

        val newText = StringBuilder(currentText).insert(cursorPosition, character).toString()
        currentExpression = TextFieldValue(newText, selection = androidx.compose.ui.text.TextRange(cursorPosition + character.length))

        // Trigger automatic calculation of the result
        autoCalculateResult()
    }

    /**
     * Removes the last character from the current expression.
     */
    fun removeLastCharacter() {
        val currentText = currentExpression.text
        val cursorPosition = currentExpression.selection.start
        Log.d(TAG, "Removing character at position: $cursorPosition in expression: $currentText")

        if (currentText.isNotEmpty() && cursorPosition > 0) {
            val newText = currentText.removeRange(cursorPosition - 1, cursorPosition)
            currentExpression = TextFieldValue(
                newText,
                selection = androidx.compose.ui.text.TextRange(cursorPosition - 1)
            )
            Log.d(TAG, "Updated expression after removal: $newText")
        }
        // Trigger automatic calculation of the result
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
        val operators = ArrayDeque<Char>()
        // Define operator precedence
        val precedence = mapOf('+' to 1, '-' to 1, '×' to 2, '÷' to 2, '^' to 3, "log" to 4, "ln" to 4, "sin" to 4, "cos" to 4, "tan" to 4, "√" to 4)

        var lastWasNumberOrRightParenthesis = false
        var number = ""

        for (i in expression.indices) {
            val char = expression[i]
            Log.d(TAG, "Processing char: $char at index: $i")

            when {
                char.isDigit() || char == '.' -> {
                    // Handle numbers
                    number += char
                    lastWasNumberOrRightParenthesis = true
                }
                char.toString() == "e" -> {
                    // Handle Euler's number 'e'
                    number += Math.E
                    lastWasNumberOrRightParenthesis = true
                }
                char.toString() == "π" -> {
                    // Handle pi 'π'
                    number += Math.PI
                    lastWasNumberOrRightParenthesis = true
                }
                char == '-' && (i == 0 || expression[i - 1] == '(') -> {
                    // Handle negative numbers (e.g., -5 or -(3+2))
                    // Determine if it's the start of a negative number
                }
                char == '(' -> {
                    // Handle opening parenthesis
                    if (lastWasNumberOrRightParenthesis) {
                        // Insert implicit multiplication if needed (e.g., 2(3) becomes 2×(3))
                        while (operators.isNotEmpty() && operators.last() == '*') {
                            output.add(operators.removeLast().toString())
                        }
                        operators.addLast('×')
                    }
                    if (number.isNotEmpty()) {
                        output.add(number)
                        number = ""
                    }
                    operators.addLast(char)
                    lastWasNumberOrRightParenthesis = false
                }
                char == ')' -> {
                    // Handle closing parenthesis
                    if (number.isNotEmpty()) {
                        output.add(number)
                        number = ""
                    }
                    while (operators.isNotEmpty() && operators.last() != '(') {
                        output.add(operators.removeLast().toString())
                    }
                    if (operators.isNotEmpty()) operators.removeLast() // Remove the opening parenthesis
                    lastWasNumberOrRightParenthesis = true
                }
                else -> {
                    // Handle operators
                    if (number.isNotEmpty()) {
                        output.add(number)
                        number = ""
                    }
                    while (operators.isNotEmpty() && precedence.getOrDefault(operators.last(), 0) >= precedence.getOrDefault(char, 0)) {
                        output.add(operators.removeLast().toString())
                    }
                    operators.addLast(char)
                    lastWasNumberOrRightParenthesis = false
                }
            }
        }

        if (number.isNotEmpty()) output.add(number)
        while (operators.isNotEmpty()) {
            output.add(operators.removeLast().toString())
        }
        Log.d(TAG, "Postfix result: $output")
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
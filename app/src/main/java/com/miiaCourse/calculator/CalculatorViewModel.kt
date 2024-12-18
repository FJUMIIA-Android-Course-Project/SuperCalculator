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

    var currentExpression by mutableStateOf(TextFieldValue(""))
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
     * Automatically calculates the result of the current expression as the user types.
     */
    fun autoCalculateResult() {
        try {
            val expr = currentExpression.text
            if (expr.isBlank()) {
                evaluationResult.value = ""
                return
            }
            Log.d("CalculatorViewModel", "Auto-calculating for expression: $expr")

            val tokens = ArithmeticParser.tokenize(currentExpression.text)
            Log.d("CalculatorViewModel", "Tokenized expression: $tokens")

            val finalTokens = ArithmeticParser.insertImplicitMultiplication(tokens)
            Log.d("CalculatorViewModel", "After implicit multiplication: $finalTokens")

            val postfix = ArithmeticParser.infixToPostfix(finalTokens)
            Log.d("CalculatorViewModel", "Postfix notation: $postfix")

            val result = ArithmeticParser.evaluatePostfix(postfix)
            Log.d("CalculatorViewModel", "Auto-calculated result: $result")

            evaluationResult.value = result
        } catch (e: Exception) {
            Log.e("CalculatorViewModel", "Auto-calculation error: ${e.message}")
            evaluationResult.value = ""
        }
    }

    companion object {
        val functions = setOf("sin", "cos", "tan", "log", "ln", "√", "log[")
    }
}

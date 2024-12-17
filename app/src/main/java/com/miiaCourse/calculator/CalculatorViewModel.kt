package com.miiaCourse.calculator

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel

class CalculatorViewModel : ViewModel() {
    var currentExpression by mutableStateOf(TextFieldValue(""))
    val evaluationResult = mutableStateOf("")

    fun clear() {
        currentExpression = TextFieldValue("")
        evaluationResult.value = ""
    }

    // Insert the characters
    fun addCharacterToExpression(character: String) {
        val currentText = currentExpression.text
        val cursorPosition = currentExpression.selection.start

        val newText = StringBuilder(currentText).insert(cursorPosition, character).toString()
        currentExpression = TextFieldValue(newText, selection = androidx.compose.ui.text.TextRange(cursorPosition + character.length))

        // Automatically calculate the result
        autoCalculateResult()
    }

    // Delete the characters
    fun removeLastCharacter() {
        val currentText = currentExpression.text
        val cursorPosition = currentExpression.selection.start

        if (currentText.isNotEmpty() && cursorPosition > 0) {
            val newText = currentText.removeRange(cursorPosition - 1, cursorPosition)
            currentExpression = TextFieldValue(newText, selection = androidx.compose.ui.text.TextRange(cursorPosition - 1))
        }

        autoCalculateResult()
    }

    // Calculate the result when the equals sign '=' is pressed
    fun calculateResult() {
        try {
            val postfixExpression = infixToPostfix(currentExpression.text)
            val result = evaluatePostfix(postfixExpression)

            // Update `currentExpression` and clear `evaluationResult`
            currentExpression = TextFieldValue(result.toString(), selection = androidx.compose.ui.text.TextRange(result.toString().length))
            evaluationResult.value = ""
        } catch (e: Exception) {
            evaluationResult.value = "Error"
        }
    }


    // Automatically calculate the result as long as the user is inputting
    private fun autoCalculateResult() {
        try {
            val postfixExpression = infixToPostfix(currentExpression.text)
            val result = evaluatePostfix(postfixExpression)
            evaluationResult.value = result.toString()
        } catch (e: Exception) {
            evaluationResult.value = "Syntax Error"
        }
    }

    // Convert to the postfix expression
    private fun infixToPostfix(expression: String): List<String> {
        val output = mutableListOf<String>()
        val operators = ArrayDeque<Char>()
        val precedence = mapOf('+' to 1, '-' to 1, '×' to 2, '÷' to 2)

        var lastWasNumberOrRightParenthesis = false

        var number = ""

        for (i in expression.indices) {
            val char = expression[i]
            when {
                char.isDigit() || char == '.' -> { // Handle numbers
                    number += char
                    lastWasNumberOrRightParenthesis = true
                }
                char == '-' && (i == 0 || expression[i - 1] == '(') -> {
                    // Determine if it's the start of a negative number
                }
                char == '(' -> {
                    if (lastWasNumberOrRightParenthesis) {
                        // If the previous character was a number or right parenthesis, insert an implicit multiplication symbol
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
                    if (number.isNotEmpty()) {
                        output.add(number)
                        number = ""
                    }
                    while (operators.isNotEmpty() && operators.last() != '(') {
                        output.add(operators.removeLast().toString())
                    }
                    if (operators.isNotEmpty()) operators.removeLast()
                    lastWasNumberOrRightParenthesis = true
                }
                else -> { // Handle operators
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
        return output
    }


    // Postfix evaluation
    private fun evaluatePostfix(postfix: List<String>): Double {
        val stack = ArrayDeque<Double>()
        for (token in postfix) {
            when (token) {
                "+" -> stack.addLast(stack.removeLast() + stack.removeLast())
                "-" -> stack.addLast(-stack.removeLast() + stack.removeLast())
                "×" -> stack.addLast(stack.removeLast() * stack.removeLast())
                "÷" -> {
                    val b = stack.removeLast()
                    val a = stack.removeLast()
                    stack.addLast(a / b)
                }
                else -> stack.addLast(token.toDouble())
            }
        }
        return stack.last()
    }
}
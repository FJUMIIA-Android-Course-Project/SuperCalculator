package com.miiaCourse.calculator

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class CalculatorViewModel : ViewModel() {
    val currentExpression = mutableStateOf("")
    val evaluationResult = mutableStateOf("")

    fun clear() {
        currentExpression.value = ""
        evaluationResult.value = ""
    }

    fun addCharacterToExpression(character: String) {
        Log.d(
            "CalculatorViewModel",
            "Adding character: $character to expression: ${currentExpression.value}"
        )

        if (character in "0123456789") {
            currentExpression.value += character
        } else if (character in "+-×÷") {
            if (currentExpression.value.isNotEmpty()) {
                val lastChar = currentExpression.value.last()
                if (lastChar in "+-×÷") {
                    currentExpression.value = currentExpression.value.dropLast(1)
                }
            }
            currentExpression.value += character
        } else if (character == ".") {
            if (currentExpression.value.isNotEmpty()) {
                val lastChar = currentExpression.value.last()
                if (lastChar != '.') {
                    if (lastChar in "+-×÷") {
                        currentExpression.value += "0"
                    }
                    currentExpression.value += character
                }
            }
        } else if (character == "(") {
            if (currentExpression.value.isNotEmpty()) {
                val lastChar = currentExpression.value.last()
                if (lastChar !in "+-×÷") {
                    currentExpression.value += "×"
                }
            }
            currentExpression.value += character
        } else if (character == ")") {
            currentExpression.value += character
        }
    }

    fun removeLastCharacter() {
        if (currentExpression.value.isNotEmpty()) {
            currentExpression.value = currentExpression.value.dropLast(1)
        }
    }

    fun calculateResult() {
        try {
            val resultValue = evaluate(currentExpression.value)
            evaluationResult.value = resultValue.toString()
        } catch (e: Exception) {
            evaluationResult.value = "Error"
        }
    }
}
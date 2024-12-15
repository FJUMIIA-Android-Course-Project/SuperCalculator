package com.miiaCourse.calculator

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel

class CalculatorViewModel : ViewModel() {
    var currentExpression by mutableStateOf(TextFieldValue(""))
    val evaluationResult = mutableStateOf("")
    var cursorPosition by mutableStateOf(0)

    fun clear() {
        currentExpression = TextFieldValue("") // 使用 TextFieldValue 清除
        evaluationResult.value = ""
        cursorPosition = 0 // 重置游標位置
    }

    fun addCharacterToExpression(character: String) {
        Log.d(
            "CalculatorViewModel",
            "Adding character: $character to expression: ${currentExpression}"
        )

        val currentText = currentExpression.text // 获取 TextFieldValue 中的文本
        val newText = StringBuilder(currentText).insert(cursorPosition, character).toString()

        // 更新 TextFieldValue 和游標位置
        currentExpression = TextFieldValue(newText, selection = androidx.compose.ui.text.TextRange(cursorPosition + character.length))
        cursorPosition += character.length
    }

    fun removeLastCharacter() {
        val currentText = currentExpression.text
        if (currentText.isNotEmpty() && cursorPosition > 0) { // 檢查游標位置是否大於 0
            val newText = StringBuilder(currentText).deleteCharAt(cursorPosition - 1).toString() // 刪除游標左邊的字元
            cursorPosition -= 1 // 更新游標位置
            currentExpression = TextFieldValue(newText, selection = androidx.compose.ui.text.TextRange(cursorPosition))
        }
    }

    fun calculateResult() {
        try {
            val resultValue = evaluate(currentExpression.text) // 获取 TextFieldValue 中的文本
            evaluationResult.value = resultValue.toString()
        } catch (e: Exception) {
            evaluationResult.value = "Error"
        }
    }

}
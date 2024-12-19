package com.miiaCourse.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.SideEffect
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.miiaCourse.calculator.ui.theme.CalculatorTheme
import com.miiaCourse.calculator.ui.theme.DarkGray

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalculatorTheme {
                val viewModel = CalculatorViewModel()
                val systemUiController = rememberSystemUiController()

                SideEffect {
                    systemUiController.setSystemBarsColor(
                        color = DarkGray,
                        darkIcons = false
                    )
                }
                CalculatorUI(viewModel = viewModel)
            }
        }
    }
}

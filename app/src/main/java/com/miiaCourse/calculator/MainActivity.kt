package com.miiaCourse.calculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberCoroutineScope
import com.miiaCourse.calculator.ui.theme.CalculatorTheme
import com.miiaCourse.calculator.ui.theme.DarkGray
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalculatorTheme {
                val viewModel = CalculatorViewModel()
                val systemUiController = rememberSystemUiController()
                val drawerState = rememberDrawerState(DrawerValue.Closed) // Add drawerState
                val coroutineScope = rememberCoroutineScope() // Add coroutineScope

                SideEffect {
                    systemUiController.setSystemBarsColor(
                        color = DarkGray,
                        darkIcons = false
                    )
                }

                // Replace this...
                // CalculatorWithDrawer(
                //     viewModel = viewModel,
                //     openDrawer = TODO()
                // )

                // ...with something like this for now:
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        // Drawer content goes here (e.g., a list of items)
                        Text("Drawer Content")
                    },
                    content = {
                        CalculatorWithDrawer(
                            viewModel = viewModel,
                            openDrawer = {
                                coroutineScope.launch { drawerState.open() }
                            }
                        )
                    }
                )
            }
        }
    }
}

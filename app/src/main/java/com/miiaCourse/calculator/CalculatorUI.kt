package com.miiaCourse.calculator

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miiaCourse.calculator.ui.theme.DarkGray
import com.miiaCourse.calculator.ui.theme.DarkRed
import com.miiaCourse.calculator.ui.theme.MediumGray
import com.miiaCourse.calculator.ui.theme.PrussianBlue

@Preview(showBackground = true)
@Composable
fun PreviewCalculatorUI() {
    val mockViewModel = CalculatorViewModel()
    CalculatorUI(viewModel = mockViewModel)
}

// Composable function for the Calculator UI
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorUI(
    viewModel: CalculatorViewModel,
) {
    // FocusRequester for managing focus on the input field
    val focusRequester = remember { FocusRequester() }
    // State variables for current expression and evaluation result
    val currentExpression = viewModel.currentExpression
    val evaluationResult = viewModel.evaluationResult
    // Spacing between buttons
    val buttonSpacing = 8.dp

    // Log the current expression for debugging
    Log.d("CalculatorUI", "Expression: ${viewModel.currentExpression}")

    // Scaffold provides the basic structure for the UI
    Scaffold(
        // Top app bar with the title "Calculator"
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Calculator",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = PrussianBlue
                )
            )
        },
        // Content of the Scaffold
        content = { paddingValues ->
            // Column to arrange UI elements vertically
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkGray)
                    .padding(paddingValues)
            ) {
                // Input field for the expression
                TextField(
                    value = currentExpression,
                    // Update the expression in the ViewModel when the input changes
                    onValueChange = {
                        viewModel.currentExpression = it
                    },
                    readOnly = true, // Input field is read-only
                    modifier = Modifier
                        .fillMaxWidth()
                        // Request focus when clicked
                        .clickable {
                            focusRequester.requestFocus()
                        }
                        .focusRequester(focusRequester), // Assign the FocusRequester
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 40.sp,
                        textAlign = TextAlign.End,
                        color = Color.White
                    ),
                    singleLine = true, // Input field is single-line
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = Color.White,
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.None)
                )
                // Launch an effect to request focus on the input field after a delay
                LaunchedEffect(key1 = Unit) {
                    delay(100)
                    focusRequester.requestFocus()
                }
                // Display the evaluation result
                LazyRow(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth(),
                    reverseLayout = true // Display result from right to left
                ) {
                    item {
                        Text(
                            text = evaluationResult.value,
                            textAlign = TextAlign.End,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp, horizontal = 8.dp),
                            fontWeight = FontWeight.Light,
                            fontSize = 80.sp,
                            color = Color.White,
                            maxLines = 1 // Limit the result to a single line
                        )
                    }
                }

                // Divider to separate the input and button sections
                Divider(
                    color = MediumGray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                // Section for calculator buttons
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(buttonSpacing)
                ) {
                    // First row of buttons (AC, (, ), ÷)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
                    ) {
                        // Button for clearing the expression (AC)
                        CalculatorButton(
                            symbol = "AC",
                            color = DarkRed,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .weight(1f)
                                .clickable { viewModel.clear() }
                        )
                        // Button for opening parenthesis (()
                        CalculatorButton(
                            symbol = "(",
                            color = PrussianBlue,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .weight(1f)
                                .clickable { viewModel.addCharacterToExpression("(") }
                        )
                        // Button for closing parenthesis ())
                        CalculatorButton(
                            symbol = ")",
                            color = PrussianBlue,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .weight(1f)
                                .clickable { viewModel.addCharacterToExpression(")") }
                        )
                        // Button for division (÷)
                        CalculatorButton(
                            symbol = "÷",
                            color = PrussianBlue,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .weight(1f)
                                .clickable { viewModel.addCharacterToExpression("÷") }
                        )
                    }
                    // Additional Rows of Buttons...
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
                    ) {
                        CalculatorButton(
                            symbol = "7",
                            color = MediumGray,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .weight(1f)
                                .clickable { viewModel.addCharacterToExpression("7") }
                        )
                        CalculatorButton(
                            symbol = "8",
                            color = MediumGray,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .weight(1f)
                                .clickable { viewModel.addCharacterToExpression("8") }
                        )
                        CalculatorButton(
                            symbol = "9",
                            color = MediumGray,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .weight(1f)
                                .clickable { viewModel.addCharacterToExpression("9") }
                        )
                        CalculatorButton(
                            symbol = "×",
                            color = PrussianBlue,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .weight(1f)
                                .clickable { viewModel.addCharacterToExpression("×") }
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
                    ) {
                        CalculatorButton(
                            symbol = "4",
                            color = MediumGray,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .weight(1f)
                                .clickable {
                                    viewModel.addCharacterToExpression("4")
                                }
                        )
                        CalculatorButton(
                            symbol = "5",
                            color = MediumGray,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .weight(1f)
                                .clickable {
                                    viewModel.addCharacterToExpression("5")
                                }
                        )
                        CalculatorButton(
                            symbol = "6",
                            color = MediumGray,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .weight(1f)
                                .clickable {
                                    viewModel.addCharacterToExpression("6")
                                }
                        )
                        CalculatorButton(
                            symbol = "-",
                            color = PrussianBlue,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .weight(1f)
                                .clickable {
                                    viewModel.addCharacterToExpression("-")
                                }
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
                    ) {
                        CalculatorButton(
                            symbol = "1",
                            color = MediumGray,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .weight(1f)
                                .clickable {
                                    viewModel.addCharacterToExpression("1")
                                }
                        )
                        CalculatorButton(
                            symbol = "2",
                            color = MediumGray,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .weight(1f)

                                .clickable {
                                    viewModel.addCharacterToExpression("2")
                                }
                        )
                        CalculatorButton(
                            symbol = "3",
                            color = MediumGray,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .weight(1f)
                                .clickable {
                                    viewModel.addCharacterToExpression("3")
                                }
                        )
                        CalculatorButton(
                            symbol = "+",
                            color = PrussianBlue,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .weight(1f)
                                .clickable {
                                    viewModel.addCharacterToExpression("+")
                                }
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
                    ) {
                        CalculatorButton(
                            symbol = "0",
                            color = MediumGray,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .weight(1f)
                                .clickable {
                                    viewModel.addCharacterToExpression("0")
                                }
                        )
                        CalculatorButton(
                            symbol = ".",
                            color = MediumGray,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .weight(1f)
                                .clickable {
                                    viewModel.addCharacterToExpression(".")
                                }
                        )
                        CalculatorButton(
                            symbol = "Del",
                            color = DarkRed,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .weight(1f)
                                .clickable {
                                    viewModel.removeLastCharacter()
                                }
                        )
                        CalculatorButton(
                            symbol = "=",
                            color = Color(0xFF003175),
                            modifier = Modifier
                                .aspectRatio(1f)
                                .weight(1f)
                                .clickable {
                                    viewModel.calculateResult()
                                }
                        )
                    }
                }

            }
        }
    )
}
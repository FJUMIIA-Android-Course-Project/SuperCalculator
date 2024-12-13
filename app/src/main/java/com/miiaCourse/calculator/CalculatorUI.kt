package com.miiaCourse.calculator

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorUI(
    viewModel: CalculatorViewModel,
) {
    val currentExpression = viewModel.currentExpression
    val evaluationResult = viewModel.evaluationResult
    val buttonSpacing = 8.dp

    Log.d("CalculatorUI", "Expression: ${viewModel.currentExpression.value}")

    Scaffold(
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
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkGray)
                    .padding(paddingValues)
            ) {
                // Input Field for Expression
                TextField(
                    value = currentExpression.value,
                    onValueChange = { viewModel.currentExpression.value = it },
                    modifier = Modifier
                        .fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 40.sp,
                        textAlign = TextAlign.End
                    ),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        cursorColor = Color.Yellow,
                    )
                )

                // Display Evaluation Result
                LazyRow(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth(),
                    reverseLayout = true
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
                            maxLines = 1
                        )
                    }
                }

                // Divider
                Divider(
                    color = MediumGray,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )

                // Buttons Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(buttonSpacing)
                ) {
                    // First Row of Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
                    ) {
                        CalculatorButton(
                            symbol = "AC",
                            color = DarkRed,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .weight(1f)
                                .clickable { viewModel.clear() }
                        )
                        CalculatorButton(
                            symbol = "(",
                            color = PrussianBlue,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .weight(1f)
                                .clickable { viewModel.addCharacterToExpression("(") }
                        )
                        CalculatorButton(
                            symbol = ")",
                            color = PrussianBlue,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .weight(1f)
                                .clickable { viewModel.addCharacterToExpression(")") }
                        )
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
// MainActivity.kt
package com.example.chekadcalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.round

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalculatorTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    CalculatorApp()
                }
            }
        }
    }
}


@Composable
fun CalculatorApp() {
    var expression by remember { mutableStateOf("") }        // رشته ورودی/نمایش
    var result by remember { mutableStateOf<String?>(null) } // نتیجه در صورت محاسبه

    val buttonSpacing = 12.dp
    val screenW = LocalConfiguration.current.screenWidthDp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = if (expression.isEmpty()) "0" else expression,
                fontSize = 36.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp),
                textAlign = TextAlign.End
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = result ?: "",
                fontSize = 24.sp,
                color = Color.Gray,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp),
                textAlign = TextAlign.End
            )
            Spacer(modifier = Modifier.height(16.dp))
        }


        val buttons = listOf(
            listOf("AC", "(", ")", "⌫"),
            listOf("7", "8", "9", "÷"),
            listOf("4", "5", "6", "×"),
            listOf("1", "2", "3", "-"),
            listOf("%", "0", ".", "+"),
            listOf("=",)
        )

        Column(modifier = Modifier.fillMaxWidth()) {
            for (row in buttons) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = buttonSpacing),
                    horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
                ) {
                    if (row.size == 1 && row[0] == "=") {

                        CalculatorButton(
                            text = "=",
                            modifier = Modifier
                                .height(64.dp)
                                .fillMaxWidth(),
                            backgroundColor = Color(0xFF2E7D32),
                            contentColor = Color.White
                        ) {

                            try {
                                val eval = evaluateExpression(expression)
                                result = formatDouble(eval)
                                expression = result ?: ""
                            } catch (e: Exception) {
                                result = "خطا"
                            }
                        }
                    } else {

                        for (b in row) {
                            val weight = 1f
                            CalculatorButton(
                                text = b,
                                modifier = Modifier
                                    .height(64.dp)
                                    .weight(weight),
                                backgroundColor = getButtonColor(b),
                                contentColor = getContentColor(b)
                            ) {
                                when (b) {
                                    "AC" -> {
                                        expression = ""
                                        result = null
                                    }
                                    "⌫" -> {
                                        if (expression.isNotEmpty()) {
                                            expression = expression.dropLast(1)
                                        }
                                    }
                                    "=" -> {
                                        try {
                                            val eval = evaluateExpression(expression)
                                            result = formatDouble(eval)
                                            expression = result ?: ""
                                        } catch (e: Exception) {
                                            result = "خطا"
                                        }
                                    }
                                    "%" -> {

                                        expression = applyPercentToLastNumber(expression)
                                    }
                                    else -> {

                                        expression = appendToExpression(expression, b)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalculatorButton(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.LightGray,
    contentColor: Color = Color.Black,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .background(backgroundColor, shape = CircleShape)
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = text,
                color = contentColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}



fun getButtonColor(label: String): Color {
    return when (label) {
        "AC", "⌫" -> Color(0xFFEF5350) // قرمز برای پاک‌سازی
        "+", "-", "×", "÷", "%" -> Color(0xFF1976D2) // آبی برای عملگرها
        "=" -> Color(0xFF2E7D32) // سبز
        else -> Color(0xFFEEEEEE) // خاکستری برای اعداد
    }
}

fun getContentColor(label: String): Color {
    return when (label) {
        "AC", "⌫", "+", "-", "×", "÷", "%", "=" -> Color.White
        else -> Color.Black
    }
}

fun appendToExpression(current: String, input: String): String {

    if (input.matches("[0-9.]".toRegex())) {
        if (input == ".") {

            val lastNumber = current.takeLastWhile { it != '+' && it != '-' && it != '×' && it != '÷' && it != '(' && it != ')' }
            if (lastNumber.contains('.')) return current
            if (lastNumber.isEmpty()) {

                return current + "0."
            }
        }
        return current + input
    } else {
        if (input in listOf("+", "-", "×", "÷")) {
            if (current.isEmpty()) {

                return if (input == "-") "-" else current
            }

            val last = current.last()
            if (last == '+' || last == '-' || last == '×' || last == '÷') {
                return current.dropLast(1) + input
            }
        }
        return current + input
    }
}

fun applyPercentToLastNumber(expr: String): String {
    if (expr.isEmpty()) return expr
    // جدا کردن آخرین عدد
    val idx = expr.length - 1
    var i = idx
    while (i >= 0 && (expr[i].isDigit() || expr[i] == '.')) i--
    val lastNumber = expr.substring(i + 1)
    if (lastNumber.isEmpty()) return expr
    return try {
        val num = lastNumber.toDouble()
        val replaced = (num / 100.0).toString()
        expr.substring(0, i + 1) + replaced
    } catch (e: Exception) {
        expr
    }
}



fun evaluateExpression(input: String): Double {
    val tokens = tokenizeExpression(input)
    val rpn = shuntingYard(tokens)
    return evaluateRPN(rpn)
}

fun tokenizeExpression(input: String): List<String> {
    val tokens = mutableListOf<String>()
    var i = 0
    while (i < input.length) {
        val c = input[i]
        when {
            c.isWhitespace() -> i++
            c.isDigit() || c == '.' -> {
                val start = i
                i++
                while (i < input.length && (input[i].isDigit() || input[i] == '.')) i++
                tokens.add(input.substring(start, i))
            }
            c == '+' || c == '-' || c == '×' || c == '÷' || c == '(' || c == ')' -> {
                tokens.add(c.toString())
                i++
            }
            else -> {

                i++
            }
        }
    }
    return tokens
}

fun precedence(op: String): Int {
    return when (op) {
        "+", "-" -> 1
        "×", "÷" -> 2
        else -> 0
    }
}

fun isOperator(tok: String) = tok in setOf("+", "-", "×", "÷")

fun shuntingYard(tokens: List<String>): List<String> {
    val output = mutableListOf<String>()
    val ops = ArrayDeque<String>()

    var i = 0
    while (i < tokens.size) {
        val token = tokens[i]
        when {
            token.toDoubleOrNull() != null -> output.add(token)
            isOperator(token) -> {

                if (token == "-" && (i == 0 || tokens[i - 1] == "(" || isOperator(tokens[i - 1]))) {

                    output.add("0")
                }
                while (ops.isNotEmpty() && isOperator(ops.first()) &&
                    precedence(ops.first()) >= precedence(token)
                ) {
                    output.add(ops.removeFirst())
                }
                ops.addFirst(token)
            }
            token == "(" -> ops.addFirst(token)
            token == ")" -> {
                while (ops.isNotEmpty() && ops.first() != "(") {
                    output.add(ops.removeFirst())
                }
                if (ops.isNotEmpty() && ops.first() == "(") ops.removeFirst()
            }
        }
        i++
    }
    while (ops.isNotEmpty()) output.add(ops.removeFirst())
    return output
}

fun evaluateRPN(rpn: List<String>): Double {
    val stack = ArrayDeque<Double>()
    for (tok in rpn) {
        val num = tok.toDoubleOrNull()
        if (num != null) {
            stack.addFirst(num)
        } else if (isOperator(tok)) {
            if (stack.size < 2) throw IllegalArgumentException("Invalid expression")
            val b = stack.removeFirst()
            val a = stack.removeFirst()
            val res = when (tok) {
                "+" -> a + b
                "-" -> a - b
                "×" -> a * b
                "÷" -> {
                    if (b == 0.0) throw ArithmeticException("Division by zero")
                    a / b
                }
                else -> throw IllegalArgumentException("Unknown op")
            }
            stack.addFirst(res)
        } else {
            throw IllegalArgumentException("Unknown token $tok")
        }
    }
    if (stack.size != 1) throw IllegalArgumentException("Invalid expression")
    return stack.first()
}

fun formatDouble(value: Double): String {
    val rounded = (round(value * 1e12) / 1e12)
    return if (rounded % 1.0 == 0.0) {
        rounded.toLong().toString()
    } else {

        var s = rounded.toString()
        if (s.length > 20) s = String.format("%.10f", rounded)
        s.trimEnd('0').trimEnd('.')
    }
}

/* ----------------- Theme ----------------- */

private val LightColors = lightColorScheme(
    primary = Color(0xFF1976D2),
    onPrimary = Color.White,
    background = Color(0xFFF7F7F7),
    surface = Color.White,
    onSurface = Color.Black
)

@Composable
fun CalculatorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography(),
        content = content
    )
}

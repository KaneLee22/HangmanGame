package com.example.hangmangame

import android.content.res.Configuration
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

@Composable
fun HangmanGameScreen() {
    val words = listOf("Boston", "University", "Computer", "Science")

    var currentWord by rememberSaveable { mutableStateOf(words.random()) }
    var guessedLetters by rememberSaveable { mutableStateOf("") }
    var remainingAttempts by rememberSaveable { mutableStateOf(6) }
    var hintUsageCount by rememberSaveable { mutableStateOf(0) }
    var isGameOver by rememberSaveable { mutableStateOf(false) }
    var didWin by rememberSaveable { mutableStateOf(false) }

    fun newGame() {
        currentWord = words.random()
        guessedLetters = ""
        remainingAttempts = 6
        hintUsageCount = 0
        isGameOver = false
        didWin = false
    }

    fun guessLetter(letter: Char) {
        if (letter in guessedLetters || isGameOver) return
        guessedLetters += letter
        if (letter !in currentWord) {
            remainingAttempts--
            if (remainingAttempts <= 0) {
                isGameOver = true
                didWin = false
            }
        } else {
            // 全部猜出则胜
            val allRevealed = currentWord.all { it in guessedLetters }
            if (allRevealed) {
                isGameOver = true
                didWin = true
            }
        }
    }

    fun useHint(): Boolean {
        if (isGameOver) return false
        if (hintUsageCount >= 3) return false
        if (remainingAttempts <= 1) return false

        hintUsageCount++
        if (hintUsageCount > 1) {
            remainingAttempts--
            if (remainingAttempts <= 0) {
                isGameOver = true
                didWin = false
                return false
            }
        }
        return true
    }

    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    if (isPortrait) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HangmanMainPanel(
                currentWord = currentWord,
                guessedLetters = guessedLetters,
                remainingAttempts = remainingAttempts,
                isGameOver = isGameOver,
                didWin = didWin,
                onNewGame = { newGame() },
                modifier = Modifier.weight(1f)
            )

            HintPanel(
                hintUsageCount = hintUsageCount,
                onHintClicked = { useHint() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            LettersPanel(
                guessedLetters = guessedLetters,
                onLetterClicked = { guessLetter(it) },
                modifier = Modifier.weight(1f)
            )
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LettersPanel(
                guessedLetters = guessedLetters,
                onLetterClicked = { guessLetter(it) },
                modifier = Modifier.weight(1f)
            )

            HintPanel(
                hintUsageCount = hintUsageCount,
                onHintClicked = { useHint() },
                modifier = Modifier.weight(1f)
            )

            HangmanMainPanel(
                currentWord = currentWord,
                guessedLetters = guessedLetters,
                remainingAttempts = remainingAttempts,
                isGameOver = isGameOver,
                didWin = didWin,
                onNewGame = { newGame() },
                modifier = Modifier.weight(2f)
            )
        }
    }
}

@Composable
fun LettersPanel(
    guessedLetters: String,
    onLetterClicked: (Char) -> Unit,
    modifier: Modifier = Modifier
) {
    val allLetters = ('A'..'Z')

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Choose a Letter", style = MaterialTheme.typography.titleMedium)

        val chunkedLetters = allLetters.chunked(7)
        chunkedLetters.forEach { rowLetters ->
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowLetters.forEach { letter ->
                    val isDisabled = letter in guessedLetters
                    Button(
                        onClick = { onLetterClicked(letter) },
                        enabled = !isDisabled,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDisabled)
                                MaterialTheme.colorScheme.surfaceVariant
                            else
                                MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(letter.toString())
                    }
                }
            }
        }
    }
}

@Composable
fun HintPanel(
    hintUsageCount: Int,
    onHintClicked: () -> Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Hints", style = MaterialTheme.typography.titleMedium)
        Button(onClick = {
            val ok = onHintClicked()
        }) {
            when (hintUsageCount) {
                0 -> Text("Show Hint")
                1 -> Text("Disable Half Letters (Costs a turn)")
                2 -> Text("Reveal Vowels (Costs a turn)")
                else -> Text("No Hints Left")
            }
        }
    }
}

@Composable
fun HangmanMainPanel(
    currentWord: String,
    guessedLetters: String,
    remainingAttempts: Int,
    isGameOver: Boolean,
    didWin: Boolean,
    onNewGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Remaining Attempts: $remainingAttempts")

            val revealed = currentWord.map { c ->
                if (c in guessedLetters) c else '_'
            }.joinToString(" ")
            Text(
                text = revealed,
                style = MaterialTheme.typography.headlineMedium
            )

            if (isGameOver) {
                val resultText = if (didWin) "You Won!" else "You Lost!"
                Text(
                    resultText,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(onClick = { onNewGame() }) {
                Text("New Game")
            }
        }
    }
}

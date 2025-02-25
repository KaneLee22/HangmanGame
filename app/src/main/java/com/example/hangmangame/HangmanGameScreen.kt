package com.example.hangmangame

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun HangmanGameScreen() {

    val words = listOf("BOSTON", "UNIVERSITY", "COMPUTER", "SCIENCE")


    var currentWord by rememberSaveable { mutableStateOf(words.random()) }
    var guessedLetters by rememberSaveable { mutableStateOf("") }
    var disabledLetters by rememberSaveable { mutableStateOf("") }
    var remainingAttempts by rememberSaveable { mutableStateOf(6) }
    var hintUsageCount by rememberSaveable { mutableStateOf(0) }
    var isGameOver by rememberSaveable { mutableStateOf(false) }
    var didWin by rememberSaveable { mutableStateOf(false) }


    fun newGame() {
        currentWord = words.random()
        guessedLetters = ""
        disabledLetters = ""
        remainingAttempts = 6
        hintUsageCount = 0
        isGameOver = false
        didWin = false
    }

    fun guessLetter(letter: Char) {
        if (isGameOver || letter in guessedLetters || letter in disabledLetters) return

        guessedLetters += letter
        if (letter !in currentWord) {
            remainingAttempts--
            if (remainingAttempts <= 0) {
                isGameOver = true
                didWin = false
            }
        } else {
            val allRevealed = currentWord.all { it in guessedLetters }
            if (allRevealed) {
                isGameOver = true
                didWin = true
            }
        }
    }

    val context = LocalContext.current
    fun useHint(): Boolean {
        if (isGameOver) return false
        if (hintUsageCount >= 3) return false

        if (remainingAttempts <= 1) {
            Toast.makeText(context, "Hint not available", Toast.LENGTH_SHORT).show()
            return false
        }

        hintUsageCount++
        when (hintUsageCount) {
            1 -> {
                Toast.makeText(context, "First Hint: It's a secret!", Toast.LENGTH_SHORT).show()
            }
            2 -> {
                    val wrongUnpicked = ('A'..'Z').filter {
                    it !in currentWord && it !in guessedLetters && it !in disabledLetters
                }
                val halfCount = wrongUnpicked.size / 2
                val toDisable = wrongUnpicked.shuffled().take(halfCount)
                disabledLetters += toDisable.joinToString("")
                remainingAttempts--
                if (remainingAttempts <= 0) {
                    isGameOver = true
                    didWin = false
                    return false
                }
            }
            3 -> {
                val vowels = listOf('A', 'E', 'I', 'O', 'U')
                currentWord.forEach { c ->
                    if (c in vowels && c !in guessedLetters) {
                        guessedLetters += c
                    }
                }
                disabledLetters += vowels.joinToString("")
                remainingAttempts--
                if (remainingAttempts <= 0) {
                    isGameOver = true
                    didWin = false
                    return false
                }
                val allRevealed = currentWord.all { it in guessedLetters }
                if (allRevealed) {
                    isGameOver = true
                    didWin = true
                }
            }
        }
        return true
    }

    val orientation = LocalConfiguration.current.orientation
    val isPortrait = (orientation == Configuration.ORIENTATION_PORTRAIT)

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
                onNewGame = ::newGame,
                modifier = Modifier.weight(1f)
            )

            LettersPanel(
                guessedLetters = guessedLetters,
                disabledLetters = disabledLetters,
                onLetterClicked = ::guessLetter,
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
                disabledLetters = disabledLetters,
                onLetterClicked = ::guessLetter,
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
                onNewGame = ::newGame,
                modifier = Modifier.weight(2f)
            )
        }
    }
}

@Composable
fun LettersPanel(
    guessedLetters: String,
    disabledLetters: String,
    onLetterClicked: (Char) -> Unit,
    modifier: Modifier = Modifier
) {
    val allLetters = ('A'..'Z')
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Choose a Letter", style = MaterialTheme.typography.titleMedium)
        val chunked = allLetters.chunked(7)
        chunked.forEach { rowLetters ->
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowLetters.forEach { letter ->
                    val isDisabled = letter in guessedLetters || letter in disabledLetters
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
        Text("Hint Panel", style = MaterialTheme.typography.titleMedium)
        Button(onClick = {
            val ok = onHintClicked()
        }) {
            when (hintUsageCount) {
                0 -> Text("Show Hint (no cost)")
                1 -> Text("Disable Half Wrong (cost 1 turn)")
                2 -> Text("Reveal Vowels (cost 1 turn)")
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
            Text(text = "Remaining Attempts: $remainingAttempts")

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
                    text = resultText,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(onClick = onNewGame) {
                Text("New Game")
            }
        }
    }
}




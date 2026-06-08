package ni.edu.uam.reuam.presentation.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ni.edu.uam.reuam.presentation.components.ReUAMButton

@Composable
fun WelcomeScreen(
    onStartClick: () -> Unit,
    onLoginClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Dale una segunda vida a lo que ya no usas",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Encuentra, dona, intercambia o presta artículos dentro de la comunidad UAM.",
            modifier = Modifier.padding(top = 12.dp),
            style = MaterialTheme.typography.bodyLarge
        )

        ReUAMButton(
            text = "Comenzar",
            onClick = onStartClick,
            modifier = Modifier.padding(top = 32.dp)
        )

        TextButton(
            onClick = onLoginClick,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Iniciar sesión")
        }
    }
}
package ni.edu.uam.reuam.presentation.articles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ni.edu.uam.reuam.presentation.components.ReUAMButton
import ni.edu.uam.reuam.presentation.components.ReUAMTextField

@Composable
fun PublishArticleScreen(
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Publicar artículo",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )

        ReUAMTextField(
            value = "",
            onValueChange = {},
            label = "Título del artículo",
            modifier = Modifier.padding(top = 24.dp)
        )

        ReUAMTextField(
            value = "",
            onValueChange = {},
            label = "Descripción",
            modifier = Modifier.padding(top = 12.dp),
            singleLine = false
        )

        ReUAMButton(
            text = "Publicar artículo",
            onClick = {},
            modifier = Modifier.padding(top = 24.dp)
        )

        OutlinedButton(
            onClick = onBackClick,
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text("Volver")
        }
    }
}
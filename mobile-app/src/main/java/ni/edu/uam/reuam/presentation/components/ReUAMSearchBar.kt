package ni.edu.uam.reuam.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ni.edu.uam.reuam.ui.theme.*

@Composable
fun ReUAMSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Buscar artículos, libros, tecnología...",
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = placeholder,
                color = ReUAMTextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = "Buscar",
                tint = ReUAMTextSecondary
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = ReUAMSurface,
            unfocusedContainerColor = ReUAMSurface,
            focusedBorderColor = ReUAMGreen,
            unfocusedBorderColor = ReUAMGreenSoft,
            cursorColor = ReUAMGreen,
            focusedTextColor = ReUAMTextPrimary,
            unfocusedTextColor = ReUAMTextPrimary
        ),
        textStyle = MaterialTheme.typography.bodyMedium
    )
}
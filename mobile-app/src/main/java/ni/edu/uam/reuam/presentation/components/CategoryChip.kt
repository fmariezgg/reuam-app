package ni.edu.uam.reuam.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ni.edu.uam.reuam.ui.theme.*

@Composable
fun CategoryChip(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val containerColor = if (isActive) ReUAMGreen else ReUAMSurface
    val contentColor = if (isActive) ReUAMSurface else ReUAMTextSecondary

    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = containerColor,
        shadowElevation = if (isActive) 2.dp else 0.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = contentColor
                )
            }

            Text(
                text = label,
                fontSize = 13.sp,
                color = contentColor,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}
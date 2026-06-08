package ni.edu.uam.reuam.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ni.edu.uam.reuam.R

@Composable
fun ReUAMLogo(
    modifier: Modifier = Modifier,
    size: Dp = 150.dp
) {
    Image(
        painter = painterResource(id = R.drawable.reuam_icon),
        contentDescription = "Logo de ReUAM",
        modifier = modifier.size(size),
        contentScale = ContentScale.Fit
    )
}
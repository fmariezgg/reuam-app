package ni.edu.uam.reuam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ni.edu.uam.reuam.presentation.home.HomeScreen
import ni.edu.uam.reuam.ui.theme.ReUAMTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ReUAMTheme {
                HomeScreen()
            }
        }
    }
}
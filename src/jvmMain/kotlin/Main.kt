import androidx.compose.material.MaterialTheme
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

/**
 * An application to filter through and compare countries.
 *
 * They are mapped into regions, which are continents (obviously),
 * and users can select countries to view their flag, stats, etc.
 *
 * The user is also able to search through all countries
 * or region based countries to find a specific one.
 *
 * Users are also able to compare two countries and see the diffs
 * of their specific stats, for example:
 *
 * United States: 15,000,000 population
 * Canada: 16,000,000 population (diff -> 1,000,000)
 *
 * Users can also sort countries by their statistics and
 * view "leaderboards" for them.
 *
 * @author GrowlyX
 * @since 4/3/2023
 */
@Composable
@Preview
fun App()
{
    var text by remember { mutableStateOf("efe, World!") }

    MaterialTheme {
        Button(onClick = {
            text = "dc, Desktop!"
        }) {
            Text(text)
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}

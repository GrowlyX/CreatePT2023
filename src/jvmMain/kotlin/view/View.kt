package view

import Country
import VerticalScrollbar
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import countries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.URL
import kotlin.reflect.KProperty

/**
 * @author Subham Kumar, JetBrains
 * @since 4/4/2023
 * @see (https://github.com/JetBrains/compose-multiplatform/blob/master/examples/issues)
 */
@Composable
@Preview
fun App()
{
    val countryViewing = remember {
        mutableStateOf<Country?>(null)
    }

    MaterialTheme {
        Row(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxWidth(0.4f),
                contentAlignment = Alignment.Center
            ) {
                CountryList(countryViewing)
            }

            CurrentCountry(countryViewing.value)
        }
    }
}

@Composable
fun CountryList(countryViewing: MutableState<Country?>)
{
    val scroll = rememberScrollState()
    val searchQuery = remember {
        mutableStateOf("")
    }
    // TODO: add region filters

    Column {
        Scaffold(
            topBar = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TopAppBar(
                        title = {
                            Text(
                                text = "Country Discovery",
                                textAlign = TextAlign.Center
                            )
                        }
                    )

                    Spacer(Modifier.height(15.dp))

                    TextField(
                        value = searchQuery.value,
                        onValueChange = {
                            searchQuery.value = it
                        },
                        label = {
                            Text("Search...")
                        }
                    )

                    Spacer(Modifier.height(15.dp))
                    Divider()
                }
            },
            content = {
                Column {
                    ListBody(
                        scroll,
                        countryViewing = countryViewing,
                        searchQuery = searchQuery
                    )
                }
            }
        )
    }
}

@Composable
fun ListBody(
    scroll: ScrollState,
    countryViewing: MutableState<Country?>,
    searchQuery: MutableState<String>,
)
{
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.verticalScroll(scroll)) {
            countries
                .filter {
                    it.value.name.lowercase().contains(searchQuery.value.lowercase())
                }
                .forEach {
                    Box(
                        modifier = Modifier.clickable {
                            countryViewing.value = it.value
                        },
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Card(
                            modifier = Modifier
                                .padding(10.dp)
                                .fillMaxWidth()
                        ) {
                            Text(text = it.value.name)
                        }
                    }
                }
        }

        VerticalScrollbar(
            Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            scroll
        )
    }

}

@Composable
fun CurrentCountryStatus(
    content: @Composable () -> Unit
)
{
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun CurrentCountry(
    country: Country?
)
{
    when (country)
    {
        null -> CurrentCountryStatus { Text("Select country") }
        else -> CurrentCountryActive(country)
    }
}

@Composable
fun CurrentCountryActive(country: Country)
{
    Box(Modifier.fillMaxSize()) {
        val state = rememberScrollState()

        Column(
            modifier = Modifier
                .padding(15.dp)
                .fillMaxSize()
                .verticalScroll(state),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                load = { loadImageBitmap("https://flagsapi.com/${country.twoLetterCode}/shiny/64.png") },
                painterFor = { BitmapPainter(it) },
                contentDescription = "Flag",
                modifier = Modifier.width(64.dp),
                watchChangeKey = country.twoLetterCode
            )

            SelectionContainer {
                Text(
                    text = country.name,
                    style = MaterialTheme.typography.h5
                )
            }

            Row(horizontalArrangement = Arrangement.Center) {
                Text("Population of ${
                    "Population of %,.0f".format(country.population.toFloat())
                }")
            }

            Spacer(Modifier.height(8.dp))

            SelectionContainer {
                Text(
                    text = "Regions: ${country.regions.joinToString(", ")}",
                    modifier = Modifier.padding(4.dp),
                    style = MaterialTheme.typography.body1
                )
            }

            Spacer(Modifier.height(5.dp))
            Divider()
            Spacer(Modifier.height(5.dp))

            country::class.members
                .filterIsInstance<KProperty<*>>()
                .forEach {
                    SelectionContainer {
                        Text("${it.name}: ${it.call(country)}")
                    }
                }

            Button({
                // TODO: asdf
            }) {
                Text(text = "Click to compare!")
            }
        }

        VerticalScrollbar(
            Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            state
        )
    }
}

fun loadImageBitmap(url: String): ImageBitmap =
    URL(url).openStream().buffered().use(::loadImageBitmap)

@Composable
fun <T> AsyncImage(
    load: suspend () -> T,
    painterFor: @Composable (T) -> Painter,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    watchChangeKey: Any?
) {
    val image: T? by produceState<T?>(null, watchChangeKey) {
        value = withContext(Dispatchers.IO) {
            try {
                load()
            } catch (e: IOException) {
                // instead of printing to console, you can also write this to log,
                // or show some error placeholder
                e.printStackTrace()
                null
            }
        }
    }

    if (image != null) {
        Image(
            painter = painterFor(image!!),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    }
}

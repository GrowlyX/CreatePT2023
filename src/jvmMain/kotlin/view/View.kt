package view

import Country
import VerticalScrollbar
import androidx.compose.desktop.ui.tooling.preview.Preview
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import countries

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
                .verticalScroll(state)
        ) {
            SelectionContainer {
                Text(
                    text = country.name,
                    style = MaterialTheme.typography.h5
                )
            }

            Row(horizontalArrangement = Arrangement.Center) {
                Text("Population of ${country.population}")
            }

            Spacer(Modifier.height(8.dp))

            SelectionContainer {
                Text(
                    text = country.regions.toString(),
                    modifier = Modifier.padding(4.dp),
                    style = MaterialTheme.typography.body1
                )
            }
        }

        VerticalScrollbar(
            Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            state
        )
    }
}

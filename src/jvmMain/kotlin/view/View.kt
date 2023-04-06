package view

import Country
import IncomeLevel
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
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
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
        mutableStateOf<Pair<Country?, Country?>>(null to null)
    }

    val compareSelectionActive = remember {
        mutableStateOf(false)
    }

    MaterialTheme {
        Row(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxWidth(0.4f),
                contentAlignment = Alignment.Center
            ) {
                CountryList(countryViewing, compareSelectionActive)
            }

            CurrentCountry(countryViewing, compareSelectionActive)
        }
    }
}

@Composable
fun CountryList(
    countryViewing: MutableState<Pair<Country?, Country?>>,
    compareSelectionActive: MutableState<Boolean>
)
{
    val scroll = rememberScrollState()

    val searchQuery = remember { mutableStateOf("") }
    val regionFilters = remember { mutableStateOf<List<String>>(emptyList()) }
    val dropdownEnabled = remember { mutableStateOf(false) }

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

                    Row(
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TextField(
                            value = searchQuery.value,
                            onValueChange = {
                                searchQuery.value = it
                            },
                            label = {
                                Text("Search...")
                            }
                        )

                        val aggregatedRegions = countries.values
                            .flatMap { it.regions }
                            .toSet()

                        IconButton(
                            onClick = {
                                dropdownEnabled.value = !dropdownEnabled.value
                            }
                        ) {
                            Icon(Icons.Default.Edit, "Edit filters.")
                        }

                        DropdownMenu(
                            expanded = dropdownEnabled.value,
                            onDismissRequest = {
                                dropdownEnabled.value = false
                            },
                            focusable = true
                        ) {
                            aggregatedRegions
                                .forEach {
                                    DropdownMenuItem(
                                        onClick = context@{
                                            if (regionFilters.value.contains(it))
                                            {
                                                val mutable = regionFilters.value.toMutableList()
                                                mutable.remove(it)

                                                regionFilters.value = mutable
                                                return@context
                                            }

                                            val mutable = regionFilters.value.toMutableList()
                                            mutable.add(it)

                                            regionFilters.value = mutable
                                        }
                                    ) {
                                        Text(
                                            "$it${
                                                if (regionFilters.value.contains(it))
                                                    " (enabled)" else ""
                                            }"
                                        )
                                    }
                                }
                        }
                    }

                    Spacer(Modifier.height(15.dp))
                    Divider()
                }
            },
            content = {
                Column {
                    ListBody(
                        scroll,
                        countryViewing = countryViewing,
                        searchQuery = searchQuery,
                        compareSelectionActive = compareSelectionActive,
                        regionFilters = regionFilters
                    )
                }
            }
        )
    }
}

@Composable
fun ListBody(
    scroll: ScrollState,
    countryViewing: MutableState<Pair<Country?, Country?>>,
    searchQuery: MutableState<String>,
    compareSelectionActive: MutableState<Boolean>,
    regionFilters: MutableState<List<String>>
)
{
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.verticalScroll(scroll)) {
            countries
                .filter {
                    it.value.name
                        .contains(
                            searchQuery.value,
                            ignoreCase = true
                        )
                }
                .filter {
                    regionFilters.value.isEmpty() || it.value.regions
                        .any { region ->
                            region in regionFilters.value
                        }
                }
                .forEach {
                    Box(
                        modifier = Modifier.clickable {
                            if (compareSelectionActive.value)
                            {
                                countryViewing.value = countryViewing.value.first to it.value
                                return@clickable
                            }

                            countryViewing.value = it.value to null
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
    country: MutableState<Pair<Country?, Country?>>,
    compareSelectionActive: MutableState<Boolean>
)
{
    when (country.value.first)
    {
        null -> CurrentCountryStatus { Text("Select country") }
        else -> CurrentCountryActive(country, compareSelectionActive)
    }
}

@Composable
fun CurrentCountryActive(
    pair: MutableState<Pair<Country?, Country?>>,
    compareSelectionActive: MutableState<Boolean>
)
{
    val country = pair.value.first!!

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

            Text(
                "Population of ${
                    "Population of %,.0f".format(country.population.toFloat())
                }"
            )

            if (pair.value.second != null)
            {
                Text(
                    "Comparing against ${pair.value.second!!.name}...",
                    color = Color.Gray
                )
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
                        if (pair.value.second != null)
                        {
                            Text(text = buildAnnotatedString {
                                val originalCountryCall = it.call(country)
                                pushStyle(style = SpanStyle(color = Color.Gray))
                                append("$originalCountryCall")
                                pop()

                                append(" - ${it.name} - ")

                                val comparisonCountryCall = it.call(pair.value.second)
                                pushStyle(style = SpanStyle(color = Color.Gray))
                                append("$comparisonCountryCall")
                                pop()

                                fun embedDiffs(diff: Float, gt: Boolean, eq: Boolean) =
                                    if (!eq)
                                    {
                                        if (gt)
                                        {
                                            pushStyle(style = SpanStyle(color = Color.Green))
                                            append(" (+${"%,.2f".format(diff)})")
                                        } else
                                        {
                                            pushStyle(style = SpanStyle(color = Color.Red))
                                            append(" (${"%,.2f".format(diff)})")
                                        }
                                    } else
                                    {
                                        pushStyle(style = SpanStyle(color = Color.DarkGray))
                                        append(" (==)")
                                    }

                                // Since kotlin doesn't have any minus, gte extensions for
                                // the Number class, we have to do this:
                                if (originalCountryCall is Double && comparisonCountryCall is Double)
                                {
                                    val diff = comparisonCountryCall - originalCountryCall
                                    embedDiffs(diff.toFloat(), diff > 0, diff == 0.0)
                                }

                                if (originalCountryCall is Long && comparisonCountryCall is Long)
                                {
                                    val diff = comparisonCountryCall - originalCountryCall
                                    embedDiffs(diff.toFloat(), diff > 0, diff == 0L)
                                }

                                if (originalCountryCall is IncomeLevel && comparisonCountryCall is IncomeLevel)
                                {
                                    val diff = comparisonCountryCall.ordinal - originalCountryCall.ordinal
                                    embedDiffs(diff.toFloat(), diff > 0, diff == 0)
                                }
                            })
                        } else
                        {
                            Text("${it.name}: ${it.call(country)}")
                        }
                    }
                }

            if (pair.value.second == null)
            {
                Button({
                    compareSelectionActive.value = true
                }) {
                    Text(
                        text = if (!compareSelectionActive.value)
                            "Click to compare" else "Click a country on the sidebar to compare..."
                    )
                }
            } else
            {
                Button(
                    onClick = {
                        pair.value = pair.value.first to null
                        compareSelectionActive.value = false
                    },
                    colors = ButtonDefaults
                        .buttonColors(
                            backgroundColor = MaterialTheme
                                .colors.onError
                        )
                ) {
                    Text(
                        text = "Clear comparison!"
                    )
                }
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
)
{
    val image: T? by produceState<T?>(
        initialValue = null, watchChangeKey
    ) {
        value = withContext(Dispatchers.IO) {
            try
            {
                load()
            } catch (e: IOException)
            {
                // instead of printing to console, you can also write this to log,
                // or show some error placeholder
                e.printStackTrace()
                null
            }
        }
    }

    if (image != null)
    {
        Image(
            painter = painterFor(image!!),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )
    }
}

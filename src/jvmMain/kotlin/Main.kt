// imports, not real code
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
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
import androidx.compose.foundation.rememberScrollbarAdapter
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.URL
import kotlin.reflect.KProperty

val countries = mutableListOf<Country>()

fun main() = application {
    // Load in our CSV file from the resources embedded
    // into our Jar file as a mutable list.
    val csvLines = this::class.java.classLoader
        .getResourceAsStream("countries.csv")
        ?.bufferedReader()?.readLines()?.toMutableList()
        ?: throw IllegalStateException(
            "Failed to load in countries from resources!"
        )

    // Remove the first element of the list as that
    // indicates the field name for each column.
    csvLines.subList(1, csvLines.size - 1)
        .forEach {
            val country = parseCommaSeparatedValue(it)
            countries.add(country)
        }

    println(countries.size)

    // Create the window that will open on our desktop
    Window(
        onCloseRequest = ::exitApplication,
        title = "Discover Countries"
    ) {
        App()
    }
}

fun parseCommaSeparatedValue(csv: String): Country
{
    val commaSplit = csv.split(",")
    val quoteCompensated = mutableListOf<String>()

    // Kotlin does not allow us to mutate index variables in
    // for loops, so we'll use a while loop here.
    var index = 0

    while (index < commaSplit.size)
    {
        val component = commaSplit[index]

        // we can move on if the component isn't quoted
        if (!component.startsWith("\""))
        {
            quoteCompensated += component
            index += 1
            continue
        }

        // remove quote prefix from the current component
        var composite = "${component.substring(1)},"

        // continue onto the next few to find the quotation ending token
        while (!commaSplit[index++].endsWith("\""))
        {
            // add middle quote component & add back the comma
            // which we previously delimited
            composite += "${commaSplit[index]},"
        }

        // remove the quote suffix and the final comma
        quoteCompensated += composite
            .substring(0, composite.length - 2)
    }

    // ensure a value is still returned if parsing to double fails
    fun String.safeDouble() = toDoubleOrNull() ?: -1.0

    return Country(
        id = quoteCompensated[0].toInt(),
        name = quoteCompensated[1],
        threeLetterCode = quoteCompensated[2],
        twoLetterCode = quoteCompensated[3],
        regions = quoteCompensated[4].split(" and ").toList(),
        incomeLevel = IncomeLevel.LOWERCASE[
            quoteCompensated[5].lowercase()
                .split(" ")
                .joinToString("")
        ]!!,
        population = quoteCompensated[6].toLongOrNull() ?: 0,
        // We will add preconditions when accessing these & displaying to the user-
        // if the double/long value is -1, then we'll display something else to make sure they don't get confused.
        fertilityRate = quoteCompensated[7].safeDouble(),
        unemploymentRate = quoteCompensated[8].safeDouble(),
        gdpPerCapita = quoteCompensated[9].safeDouble(),
        percentUsingInternet = quoteCompensated[10].safeDouble(),
        percentRenewableEnergy = quoteCompensated[11].safeDouble(),
        co2Emissions = quoteCompensated[12].safeDouble()
    )
}

/**
 * Data class mapping for CSV entries in countries.csv.
 */
data class Country(
    val id: Int,
    val name: String,
    val threeLetterCode: String,
    val twoLetterCode: String,
    // In our CSV model, some countries share two
    // regions: "Europe and Central Asia"

    // To make it easier for us to match countries to a region
    // when the user tries to search based on region, we'll
    // split the region value in CSV with " and " to get all shared regions.
    val regions: List<String>,
    val incomeLevel: IncomeLevel,
    val population: Long,
    val fertilityRate: Double,
    val unemploymentRate: Double,
    val gdpPerCapita: Double,
    val percentUsingInternet: Double,
    val percentRenewableEnergy: Double,
    val co2Emissions: Double
)

/**
 * Enum class for income level mappings.
 */
enum class IncomeLevel
{
    // We're keeping the "Income" suffix as it'll reduce the amount of operationns when we parse the CSV lines.
    LowIncome, LowerMiddleIncome, UpperMiddleIncome, HighIncome;

    companion object
    {
        // mark as a static field (java static)
        @JvmStatic
        val LOWERCASE = values()
            // associate into a new Map<String, IncomeLevel> so we can
            // access the levels by the format given in the CSV file
            .associateBy { it.name.lowercase() }
    }
}


// Window logic below.
// A lot of the window code below is scrapped from open-source code on GitHub from Jetbrains:
// https://github.com/JetBrains/compose-multiplatform/tree/master/examples/issues

@Composable
@Preview
fun App()
{
    val countryViewing = remember {
        // Use the framework to remember
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
                // show country list on the left
                CountryList(countryViewing, compareSelectionActive)
            }

            // current country on the right
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
                        // search bar to edit the search query
                        TextField(
                            value = searchQuery.value,
                            onValueChange = {
                                searchQuery.value = it
                            },
                            label = {
                                Text("Search...")
                            }
                        )

                        // filter the countries by their region
                        val aggregatedRegions = countries
                            .flatMap { it.regions }
                            .toSet()

                        // button to open the dropdown menu for editing filters.
                        IconButton(
                            onClick = {
                                dropdownEnabled.value = !dropdownEnabled.value
                            }
                        ) {
                            Icon(Icons.Default.Edit, "Edit filters.")
                        }

                        // dropdown menu for editing filters
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
                // make sure the country name has the search query in it
                .filter {
                    it.name.contains(searchQuery.value, ignoreCase = true)
                }
                .filter {
                    regionFilters.value.isEmpty() || it.regions
                        .any { region ->
                            region in regionFilters.value
                        }
                }
                .forEach {
                    Box(
                        modifier = Modifier.clickable {
                            if (compareSelectionActive.value)
                            {
                                countryViewing.value = countryViewing.value.first to it
                                return@clickable
                            }

                            countryViewing.value = it to null
                        },
                        contentAlignment = Alignment.CenterStart
                    ) {
                        // add a card with the country name as the text
                        Card(
                            modifier = Modifier
                                .padding(10.dp)
                                .fillMaxWidth()
                        ) {
                            Text(text = it.name)
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
fun CurrentCountry(
    country: MutableState<Pair<Country?, Country?>>,
    compareSelectionActive: MutableState<Boolean>
)
{
    // if there is no initial country selected, show text telling the user to select the country
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
            // if there is a comparison, show both flags, else, only show one
            if (pair.value.second != null)
            {
                Row(
                    horizontalArrangement = Arrangement.Center
                ) {
                    AsyncImage(
                        load = { loadImageBitmap("https://flagsapi.com/${country.twoLetterCode}/shiny/64.png") },
                        painterFor = { BitmapPainter(it) },
                        contentDescription = "Flag",
                        modifier = Modifier.width(64.dp),
                        watchChangeKey = country.twoLetterCode
                    )

                    Text("vs.")

                    AsyncImage(
                        load = { loadImageBitmap("https://flagsapi.com/${pair.value.second!!.twoLetterCode}/shiny/64.png") },
                        painterFor = { BitmapPainter(it) },
                        contentDescription = "Flag",
                        modifier = Modifier.width(64.dp),
                        watchChangeKey = pair.value.second!!.twoLetterCode
                    )
                }
            } else
            {
                AsyncImage(
                    load = { loadImageBitmap("https://flagsapi.com/${country.twoLetterCode}/shiny/64.png") },
                    painterFor = { BitmapPainter(it) },
                    contentDescription = "Flag",
                    modifier = Modifier.width(64.dp),
                    watchChangeKey = country.twoLetterCode
                )
            }

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

            // for all of the properties (data values like countryName, etc), compare it and show the difference in values
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
                            // There is no comparison, so go ahead and show the name and value
                            Text("${it.name}: ${it.call(country)}")
                        }
                    }
                }

            // if there is no comparison, ask it to compare. Else, ask to clear comparison
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

// Load image from URL and then convert into a format that the framework can understand.
// Created by JetBrains
// https://github.com/JetBrains/compose-multiplatform/tree/master/tutorials/Image_And_Icons_Manipulations
fun loadImageBitmap(url: String): ImageBitmap =
    URL(url).openStream().buffered().use(::loadImageBitmap)

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

// A utility that uses Kotlin coroutines to load an image in
// the background, so the user can interact with the UI without it freezing.
// Created by JetBrains
// https://github.com/JetBrains/compose-multiplatform/tree/master/tutorials/Image_And_Icons_Manipulations
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

/**
 * @author JetBrains
 * @see (https://github.com/JetBrains/compose-multiplatform/blob/master/examples/issues)
 */
@Composable
fun VerticalScrollbar(
    modifier: Modifier,
    scrollState: ScrollState
) = androidx.compose.foundation.VerticalScrollbar(
    rememberScrollbarAdapter(scrollState),
    modifier
)

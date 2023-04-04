import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
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
 * To parse country data, we traverse through a CSV file that
 * contains a single array. We then map these CSVs to our [Country] model
 *
 * Country data comes from code.org:
 * https://docs.google.com/spreadsheets/d/11wuKDgkrSjVZCdWx5cALA2rJ0dhsNLGSoZrJwgPDrLI/edit#gid=1112641907
 *
 * @author Subham
 * @since 4/3/2023
 */
data class Country(
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
    // We're keeping the "Income" suffix as it'll reduce ops when we parse the CSV lines.
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

// We are mapping countries by their ID as we'll have
// quicker lookup times when searching by ID, ~O(1).
val countries = mutableMapOf<Int, Country>()

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
    // Load in our CSV file from the resources embedded
    // into our Jar file as a mutable list.
    val csvLines = this::class.java.classLoader
        .getResourceAsStream("countries.csv")
        ?.bufferedReader()?.readLines()?.toMutableList()
        ?: throw IllegalStateException(
            "Failed to load in countries from resources!"
        )

    // Remove the first element of the list as that
    // indicate the field name of each column.
    csvLines.subList(1, csvLines.size - 1)
        .forEach {
            // A comma splits CSV data, so we're
            // splitting it to get an array of strings that
            // we can then parse and map to the fields in our Country model.
            val commaSplit = it.split(",")
            val quoteCompensated = mutableListOf<String>()

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

            countries[quoteCompensated[0].toInt()] =
                Country(
                    name = quoteCompensated[1],
                    threeLetterCode = quoteCompensated[2],
                    twoLetterCode = quoteCompensated[3],
                    regions = quoteCompensated[4].split(" and ").toList(),
                    incomeLevel = IncomeLevel.LOWERCASE[quoteCompensated[5].lowercase().split(" ").joinToString("")]!!,
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

    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}

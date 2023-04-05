import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import view.App

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
 * @author Subham Kumar
 * @since 4/3/2023
 */
// We are mapping countries by their ID as we'll have
// quicker lookup times when searching by ID, ~O(1).
val countries = mutableMapOf<Int, Country>()

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
            // A comma splits CSV data, so we're
            // splitting it to get an array of strings that
            // we can then parse and map to the fields in our Country model.
            val commaSplit = it.split(",")

            /**
             * Since we have complex tokens with quotes in our CSVs, we need to
             * compensate as splitting with the comma would not work properly.
             *
             * For example, if we have a token: "12,\"Dubai, UAE\""
             * Without compensation, we'd get the following tokens: ["12", "\"Dubai,", " UAE\""]
             *
             * To fix this, if we find a token that starts with "\"", we'll continue onto the
             * next few until we find a token that ends with a quote ending, and combine them
             * to get the complete token.
             *
             * With compensation, we should get the following result:
             * ["12", "Dubai, UAE"]
             */
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

    Window(
        onCloseRequest = ::exitApplication,
        title = "Discover Countries"
    ) {
        App()
    }
}

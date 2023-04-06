/**
 * Data class mapping for CSV entries in countries.csv.
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

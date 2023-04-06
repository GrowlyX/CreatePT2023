/**
 * Annotation to expose stat parameters.
 */
@Target(AnnotationTarget.FIELD)
annotation class Expose

/**
 * Data class mapping for CSV entries in countries.csv.
 */
data class Country(
    val name: String,
    @Expose val threeLetterCode: String,
    @Expose val twoLetterCode: String,
    // In our CSV model, some countries share two
    // regions: "Europe and Central Asia"

    // To make it easier for us to match countries to a region
    // when the user tries to search based on region, we'll
    // split the region value in CSV with " and " to get all shared regions.
    val regions: List<String>,
    @Expose val incomeLevel: IncomeLevel,
    @Expose val population: Long,
    @Expose val fertilityRate: Double,
    @Expose val unemploymentRate: Double,
    @Expose val gdpPerCapita: Double,
    @Expose val percentUsingInternet: Double,
    @Expose val percentRenewableEnergy: Double,
    @Expose val co2Emissions: Double
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

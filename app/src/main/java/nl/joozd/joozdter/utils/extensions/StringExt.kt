package nl.joozd.joozdter.utils.extensions


/**
 * Replace all instances found from a regex with something else, keeping the first value of that regex
 * Example:
 *  val r = "(Hoi|Hee)".toRegex()
 *  "Hee ouwe, Hoi Buurman!".replaceWithValue { v -> "$v!" } will become "Hee! ouwe, Hoi! Buurman"
 */
fun String.replaceWithValue(regex: Regex, newValue: (String) -> String): String{
    var result = this
    regex.findAll(this).forEach{
        val r = if (it.groupValues.size > 1) it.groupValues[1] else it.value
        result = result.replace (it.groupValues[0], newValue(r))
    }
    return result
}

fun String.splitByRegex(re: Regex, includeRegexResult: Boolean): List<String>{
    if (!includeRegexResult) error("Only supports with include, use String.split() instead.")
    else {
        val results = ArrayList<String>()

        var workingString = this
        var previousResult = ""

        while (workingString.isNotEmpty()) {
            val nextResult = re.find(workingString)?.value ?: return results + listOf(previousResult + workingString).filter { it.isNotEmpty() }
            val index = workingString.indexOf(nextResult)
            results.add(previousResult + workingString.take(index))
            previousResult = nextResult
            workingString = workingString.drop(index + nextResult.length)
        }
        return results.filter { it.isNotEmpty() }
    }
}

/**
 * Split a string into words
 */
fun String.words() = this.split(" ").filter { it.isNotBlank() }.map { it.trim() }
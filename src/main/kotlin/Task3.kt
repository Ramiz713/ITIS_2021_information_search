import com.bpodgursky.jbool_expressions.*
import com.bpodgursky.jbool_expressions.parsers.ExprParser
import com.bpodgursky.jbool_expressions.rules.RuleSet
import com.google.gson.Gson
import java.io.File
import java.io.FileOutputStream

data class InvertedIndex(val term: String, val locations: MutableList<Location>)

data class Location(val fileName: String, val lineIndex: Int) {
    override fun toString() = "{$fileName, line index $lineIndex}"
}


val invertedIndex = mutableMapOf<String, MutableList<Location>>()
lateinit var variables: Map<String, String>
lateinit var locations: Set<String>

fun task3(expression: String, map: Map<String, String>) {
    val files = File("outputTask2").listFiles()
    files?.forEach { indexFile(it) }
    val gson = Gson()
    val outputDirectory = File("outputTask3").apply {
        mkdirs()
        listFiles()?.forEach { it.deleteRecursively() }
    }
    val file = File(outputDirectory, "inverted_index.txt").apply { createNewFile() }
    FileOutputStream(file, true).bufferedWriter().use { output ->
        val invertedIndexList = invertedIndex.map { InvertedIndex(it.key, it.value) }
        output.appendLine(gson.toJson(invertedIndexList))
    }
    locations = invertedIndex.values.flatten().map { it.fileName }.toSet()
    variables = map
    val expr = ExprParser.parse(expression)
    val simplified = RuleSet.simplify(expr)
    val set = iterateExpression(simplified)
    set.forEach { println(it) }
    println(set.size)
}

fun iterateExpression(expression: Expression<String>): Set<String> {
    return when (expression) {
        is Not -> locations.minus(findWordDocuments((expression.e as Variable).value)).toSet()
        is Or -> expression.children.map { iterateExpression(it) }.unionFlatten()
        is And -> expression.children.map { iterateExpression(it) }.intersectFlatten()
        is Variable -> findWordDocuments(expression.value)
        else -> setOf()
    }
}

fun indexFile(file: File) {
    val fileName = file.name
    file.readLines().forEachIndexed { index, word ->
        var locations = invertedIndex[word]
        if (locations == null) {
            locations = mutableListOf()
            invertedIndex[word] = locations
        }
        locations.add(Location(fileName, index + 1))
    }
}

fun findWordDocuments(wordToken: String): Set<String> {
    return findWordLocations(wordToken).map { it.fileName }.toSet()
}

fun findWordLocations(wordToken: String): List<Location> {
    val w = variables[wordToken]?.toLowerCase()
    return invertedIndex[w]?.toList() ?: listOf()
}

fun <T> Iterable<Iterable<T>>.unionFlatten(): Set<T> {
    var result = setOf<T>()
    for (element in this) {
        result = result.union(element)
    }
    return result
}

fun <T> Iterable<Iterable<T>>.intersectFlatten(): Set<T> {
    var result = first().toSet()
    for (element in drop(1)) {
        result = result.intersect(element)
    }
    return result
}

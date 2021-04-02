import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.apache.lucene.morphology.russian.RussianAnalyzer
import java.io.File
import java.io.FileReader
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.pow
import kotlin.math.sqrt

val gson = Gson()
val itemStopWordsType = object : TypeToken<List<String>>() {}.type
val russianStopWords: List<String> =
    gson.fromJson(FileReader("stop_words_russian.json"), itemStopWordsType)

fun task5(termsWeight: TermsWeight, query: String) {
    val itemInvertedIndexType = object : TypeToken<List<InvertedIndex>>() {}.type
    val invertedIndexFile = File(File("outputTask3"), "inverted_index.txt")
    invertedIndexList = gson.fromJson(invertedIndexFile.readText(), itemInvertedIndexType)
    val documents = File("outputTask2").listFiles()
    val lemmas = RussianAnalyzer().lemmatizeTokens(query)
    val queryVector = lemmas.map { term ->
        val termIndex = invertedIndexList.indexOfFirst { it.term == term }
        val tf = lemmas.count { it == term } / lemmas.size.toDouble()
        val idf = termsWeight.idfArray.getOrNull(termIndex) ?: 0.0
        val tfIdf = tf * idf
        tfIdf
    }.toDoubleArray()
    if (queryVector.all { it == 0.0 }) {
        println("По запросу \"$query\" ничего не найдено.")
        return
    }
    val docVector = Array(documents.size) { DoubleArray(lemmas.size) }
    lemmas.forEachIndexed { lemmaIndex, term ->
        val termIndex = invertedIndexList.indexOfFirst { it.term == term }
        (termsWeight.tfIdfMatrix.getOrNull(termIndex) ?: DoubleArray(100))
            .forEachIndexed { docIndex, d ->
                docVector[docIndex][lemmaIndex] = d
            }
    }
    val df = DecimalFormat("#.#####")
    df.roundingMode = RoundingMode.CEILING

    println("По запросу \"$query\" найдены следующие документы:")
    docVector
        .mapIndexed { index, doubles -> documents[index].name to doubles }
        .filter { !it.second.all { d -> d == 0.0 } }
        .map { it.first to cosineSimilarity(it.second, queryVector) }
        .sortedByDescending { it.second }
        .forEach { println("${it.first}: ${df.format(it.second)}") }
}

fun cosineSimilarity(vectorA: DoubleArray, vectorB: DoubleArray): Double {
    var dotProduct = 0.0
    var normA = 0.0
    var normB = 0.0
    for (i in vectorA.indices) {
        dotProduct += vectorA[i] * vectorB[i]
        normA += vectorA[i].pow(2.0)
        normB += vectorB[i].pow(2.0)
    }
    return dotProduct / (sqrt(normA) * sqrt(normB))
}

fun Analyzer.lemmatizeTokens(query: String): List<String> {
    //Приведение к нижнему регистру
    val tokens = query.toLowerCase()
        //Замена всех знаков препинания и прочих символов на пробелы
        //Каждое слово объявляется отдельным токеном
        .replace(Regex("[^a-zA-Zа-яА-Я ]"), " ")
        //Разбиваем слова на список по пробелу
        .split(" ")
        //Фильтруем пробелы и пустые строки
        .filter { it != " " && it != "" }
        //Фильтруем стоп-слова
        .filter { !russianStopWords.contains(it) }
        //Конкатенируем все в одну строку для подачи на вход в библиотеку
        .joinToString(" ")

    val stream = tokenStream("sampleField", tokens)
    val lemmas = mutableListOf<String>()
    stream.reset()
    stream.use {
        while (stream.incrementToken()) {
            val lemma = stream.getAttribute(CharTermAttribute::class.java).toString()
            lemmas.add(lemma)
        }
    }
    return lemmas
}

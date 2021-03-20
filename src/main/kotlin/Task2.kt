import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.apache.lucene.morphology.english.EnglishAnalyzer
import org.apache.lucene.morphology.russian.RussianAnalyzer
import java.io.File
import java.io.FileOutputStream
import java.time.LocalTime
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.FileReader

fun task2() {
    val startTimestamp = LocalTime.now().toNanoOfDay()
    val textFilesDirectory = File("outputTask1/textFiles")

    val tokenizedDocs = textFilesDirectory.listFiles()?.toList()?.map { file ->
        //Токенизация
        val text = file.bufferedReader().use { it.readText() }
        val tokens = tokenizeText(text)
        EnglishAndRussianDocTokens(
            englishWordTokensList = tokens
                .filter { Character.UnicodeBlock.of(it.first()) == Character.UnicodeBlock.BASIC_LATIN },
            russianWordTokensList = tokens
                .filter { Character.UnicodeBlock.of(it.first()) == Character.UnicodeBlock.CYRILLIC },
            documentName = file.name
        )
    }

    val outputDirectory = File("outputTask2").apply {
        mkdirs()
        listFiles()?.forEach { it.deleteRecursively() }
    }
    tokenizedDocs?.forEach {
        val resultFile = File(outputDirectory, it.documentName).apply { createNewFile() }
        //Лемматизация
        RussianAnalyzer().lemmatizeTokens(it.getFilteredWithStopWordsRussianTokens(), resultFile)
        EnglishAnalyzer().lemmatizeTokens(it.getFilteredWithStopWordsEnglishTokens(), resultFile)
    }
    val endTimestamp = LocalTime.now().toNanoOfDay()
    println("Lemmatisation time is ${(endTimestamp - startTimestamp) / 1000000000.0} seconds.")
}

//https://ru.coursera.org/lecture/data-analysis-applications/priedobrabotka-tieksta-6pei3
private fun tokenizeText(text: String): List<String> {
    //Приведение к нижнему регистру
    return text.toLowerCase()
        //Замена всех знаков препинания и прочих символов на пробелы
        //Каждое слово объявляется отдельным токеном
        .replace(Regex("[^a-zA-Zа-яА-Я ]"), " ")
        //Разбиваем слова на список по пробелу
        .split(" ")
        //Фильтруем пробелы и пустые строки
        .filter { it != " " && it != "" }
}

private fun Analyzer.lemmatizeTokens(tokens: String, outputFile: File) {
    val stream = tokenStream(outputFile.name, tokens)
    stream.reset()
    FileOutputStream(outputFile, true).bufferedWriter().use { output ->
        stream.use {
            while (stream.incrementToken()) {
                val lemma = stream.getAttribute(CharTermAttribute::class.java).toString()
                output.appendLine(lemma)
            }
        }
    }
}

//Класс для работы с токенами английского и русского языка
data class EnglishAndRussianDocTokens(
    private val englishWordTokensList: List<String>,
    private val russianWordTokensList: List<String>,
    val documentName: String
) {
    private val englishStopWords: List<String>
    private val russianStopWords: List<String>

    init {
        val gson = Gson()
        val itemType = object : TypeToken<List<String>>() {}.type
        englishStopWords =
            gson.fromJson(FileReader("stop_words_english.json"), itemType)
        russianStopWords =
            gson.fromJson(FileReader("stop_words_russian.json"), itemType)
    }

    //Фильтруем стоп-слова и конкатенируем все в одну строку для подачи на вход в библиотеку
    fun getFilteredWithStopWordsEnglishTokens(): String = englishWordTokensList
        .filter { !englishStopWords.contains(it) }
        .joinToString(" ")

    fun getFilteredWithStopWordsRussianTokens(): String = russianWordTokensList
        .filter { !russianStopWords.contains(it) }
        .joinToString(" ")
}
import models.EnglishAndRussianDocTokens
import org.apache.lucene.analysis.Analyzer
import org.jsoup.nodes.Document
import java.io.File
import java.io.FileOutputStream
import java.time.LocalTime
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute
import org.apache.lucene.morphology.english.EnglishAnalyzer
import org.apache.lucene.morphology.russian.RussianAnalyzer


fun main() {
    val url = "https://habr.com/ru/"
    //task1(url)
    task2()
}

fun task1(url: String) {
    val startTimestamp = LocalTime.now().toNanoOfDay()
    val outputDirectory = File("outputTask1").apply {
        mkdirs()
        listFiles()?.forEach { it.deleteRecursively() }
    }
    val indexFile = File(outputDirectory, "index.txt").apply { createNewFile() }
    val textFilesDirectory = File(outputDirectory, "textFiles").apply { mkdirs() }
    val predicate = { doc: Document -> doc.body().text().split(' ').size >= 1000 }
    val crawler = WebCrawler(100, predicate)
    crawler.startCrawl(url)
    crawler.getPages().forEachIndexed { index, page ->
        FileOutputStream(indexFile, true).bufferedWriter()
            .use { it.append("$index - ${page.first}\n") }
        File(textFilesDirectory, "$index.txt").apply {
            createNewFile()
            val docText = page.second
            writeText(docText)
        }
    }
    val endTimestamp = LocalTime.now().toNanoOfDay()
    println("Crawling time is ${(endTimestamp - startTimestamp) / 1000000000.0} seconds.")
}

fun task2() {
    val startTimestamp = LocalTime.now().toNanoOfDay()
    val textFilesDirectory = File("outputTask1/textFiles")

    val englishWordTokensList = mutableListOf<String>()
    val russianWordTokensList = mutableListOf<String>()
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

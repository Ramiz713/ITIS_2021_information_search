import org.jsoup.nodes.Document
import java.io.File
import java.io.FileOutputStream
import java.time.LocalTime

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
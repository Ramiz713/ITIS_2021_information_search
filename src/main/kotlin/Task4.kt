import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.lang.StringBuilder
import kotlin.math.log2

import java.math.RoundingMode
import java.text.DecimalFormat

data class TermsWeight(
    val idfArray: DoubleArray,
    val tfIdfMatrix: Array<DoubleArray>
)

var invertedIndexList: List<InvertedIndex> = listOf()

fun task4(withFileAppend: Boolean = false): TermsWeight {
    val gson = Gson()
    val itemType = object : TypeToken<List<InvertedIndex>>() {}.type
    val invertedIndexFile = File(File("outputTask3"), "inverted_index.txt")
    invertedIndexList = gson.fromJson(invertedIndexFile.readText(), itemType)
    val documents = File("outputTask2").listFiles().map { it.name to it.readLines() }
    val documentsCount = documents.size

    val tfMatrix = Array(invertedIndexList.size) { DoubleArray(documentsCount) }
    val idfArray = DoubleArray(invertedIndexList.size)
    val tfIdfMatrix = Array(invertedIndexList.size) { DoubleArray(documentsCount) }

    invertedIndexList.forEachIndexed { termIndex, invertedIndex ->
        //Для начала получаем idf для каждого термина
        idfArray[termIndex] = getIdf(invertedIndex, documentsCount)
        documents.forEachIndexed { docIndex, it ->
            val key = it.first to invertedIndex.term
            //Затем получаем tf для связки термин - документ
            tfMatrix[termIndex][docIndex] = getTf(invertedIndex, it.first, it.second)
            //Получаем tf-idf для связки термин - документ как
            //произведение idf(термин) * tf(термин - документ)
            tfIdfMatrix[termIndex][docIndex] =
                tfMatrix[termIndex][docIndex] * idfArray[termIndex]
        }
    }
    val outputDirectory = File("outputTask4").apply {
        mkdirs()
        listFiles()?.forEach { it.deleteRecursively() }
    }
    if (withFileAppend) {
        saveResultInFiles(outputDirectory, tfMatrix, idfArray, tfIdfMatrix, documents)
    }
    return TermsWeight(idfArray, tfIdfMatrix)
}

fun saveResultInFiles(
    outputDirectory: File,
    tfMatrix: Array<DoubleArray>,
    idfArray: DoubleArray,
    tfIdfMatrix: Array<DoubleArray>,
    documents: List<Pair<String, List<String>>>
) {
    val df = DecimalFormat("#.#####")
    df.roundingMode = RoundingMode.CEILING
    val tfFile = File(outputDirectory, "tfTable.txt")
    val idfFile = File(outputDirectory, "idfTable.txt")
    val tfIdfFile = File(outputDirectory, "tfIdfTable.txt")
    val maxCount = invertedIndexList.maxOf { it.term.length } + 1
    val documentRow = StringBuilder("".padEnd(maxCount))
    documents.forEach {
        documentRow.append(it.first.padEnd(8))
    }
    documentRow.appendLine()
    val tfTable = StringBuilder(documentRow)
    val idfTable = StringBuilder()
    val tfIdfTable = StringBuilder(documentRow)

    tfMatrix.forEachIndexed { index, doubles ->
        val rowBuilder = StringBuilder(invertedIndexList[index].term.padEnd(maxCount))
        doubles.forEach {
            rowBuilder.append(df.format(it).padEnd(8))
        }
        tfTable.appendLine(rowBuilder.toString())
    }

    idfArray.forEachIndexed { index, d ->
        idfTable.append(invertedIndexList[index].term.padEnd(maxCount))
            .append(df.format(d).padEnd(8))
            .appendLine()
    }

    tfIdfMatrix.forEachIndexed { index, doubles ->
        val rowBuilder = StringBuilder(invertedIndexList[index].term.padEnd(maxCount))
        doubles.forEach {
            rowBuilder.append(df.format(it).padEnd(8))
        }
        tfIdfTable.appendLine(rowBuilder.toString())
    }
    tfFile.writeText(tfTable.toString())
    idfFile.writeText(idfTable.toString())
    tfIdfFile.writeText(tfIdfTable.toString())
}

fun getTf(invertedIndex: InvertedIndex, docName: String, doc: List<String>): Double {
    val wordCountInDoc = invertedIndex.locations
        .filter { it.fileName == docName }.size.toDouble()
    return wordCountInDoc / doc.size
}

fun getIdf(invertedIndex: InvertedIndex, documentsCount: Int): Double {
    val docsWithWordCount = invertedIndex.locations.distinctBy { it.fileName }.size
    return log2(documentsCount.toDouble() / docsWithWordCount)
}

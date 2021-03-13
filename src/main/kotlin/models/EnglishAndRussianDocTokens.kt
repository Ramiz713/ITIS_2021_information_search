package models

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.FileReader

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

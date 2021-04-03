fun main() {
    val url = "https://habr.com/ru/"
//    task1(url)
//    task2()
    val map = mapOf(
        "A" to "мобильный",
        "B" to "разработчик",
        "C" to "программист",
        "D" to "Android",
        "F" to "IOS",
    )
    val expression = "A & (B | C) & D"
//    task3(expression, map)
    val termsWeight = task4()
    val query = "вакансии android разработчик"
//    val query = "хочу стать разработчиком java"
//    val query = "коронавирус в России статистика"
//    val query = "бик зур самокат"
    task5(termsWeight, query)
}

fun main() {
    val url = "https://habr.com/ru/"
    //task1(url)
//    task2()
    val map = mapOf(
        "A" to "мобильный",
        "B" to "разработчик",
        "C" to "программист",
        "D" to "Android",
        "F" to "IOS",
        )
    val expression = "A & (B | C) & !D"
//    val expression = "F"
//    val expression = "!F"
    task3(expression, map)
}

package com.example.novaplustrader.ui.model

enum class Currency(
    val code: String,
    val symbol: String,
    val nameString: String
) {
    USD(code = "USD", symbol = "$", nameString = "US Dollar"),
    KES(code = "KES", symbol = "KES ", nameString = "Kenyan Shilling")
}

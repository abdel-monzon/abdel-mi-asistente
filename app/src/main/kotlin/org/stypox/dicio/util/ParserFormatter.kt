package org.stypox.dicio.util

import org.dicio.numbers.unit.NumberFormatter
import org.dicio.numbers.unit.NumberParser
import org.dicio.numbers.lang.en.EnglishFormatter
import org.dicio.numbers.lang.en.EnglishParser
import org.dicio.numbers.lang.es.SpanishFormatter
import org.dicio.numbers.lang.es.SpanishParser
import org.dicio.numbers.lang.it.ItalianFormatter
import org.dicio.numbers.lang.it.ItalianParser
import java.util.Locale

class ParserFormatter(val parser: NumberParser, val formatter: NumberFormatter)

fun getParserFormatter(locale: Locale): ParserFormatter? {
    return when (locale.language) {
        "en" -> ParserFormatter(EnglishParser(), EnglishFormatter())
        "it" -> ParserFormatter(ItalianParser(), ItalianFormatter())
        "es" -> ParserFormatter(SpanishParser(), SpanishFormatter())  // Línea corregida
        else -> null
    }
}

fun getParserFormatter(language: String): ParserFormatter? {
    return getParserFormatter(Locale(language))
}


package org.stypox.dicio.util

import org.dicio.numbers.Formatter
import org.dicio.numbers.Parser
import org.dicio.numbers.lang.en.EnglishFormatter
import org.dicio.numbers.lang.en.EnglishParser
import org.dicio.numbers.lang.es.SpanishFormatter
import org.dicio.numbers.lang.es.SpanishParser
import org.dicio.numbers.lang.it.ItalianFormatter
import org.dicio.numbers.lang.it.ItalianParser
import java.util.Locale

class ParserFormatter(val parser: Parser, val formatter: Formatter)

fun getParserFormatter(locale: Locale): ParserFormatter? {
    return when (locale.language) {
        "en" -> ParserFormatter(EnglishParser(), EnglishFormatter())
        "it" -> ParserFormatter(ItalianParser(), ItalianFormatter())
        "es" -> ParserFormatter(SpanishParser(), SpanishFormatter())  // ¡NUEVA LÍNEA!
        else -> null
    }
}

fun getParserFormatter(language: String): ParserFormatter? {
    return getParserFormatter(Locale(language))
}

package org.stypox.dicio.skills.reminder

import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.regex.Pattern

object TimeParser {
    
    /**
     * Parsea una expresión de tiempo natural (ej: "en 2 horas", "mañana a las 3", "en 30 minutos")
     * Retorna el Instant correspondiente o null si no se pudo parsear
     */
    fun parseTimeExpression(expression: String, locale: Locale = Locale.getDefault()): Instant? {
        val lowerExpression = expression.lowercase(locale).trim()
        
        // Patrones comunes en español
        return when {
            // "en X [horas|minutos|segundos]"
            parseRelativeTime(lowerExpression) != null -> parseRelativeTime(lowerExpression)
            
            // "a las X [de la tarde|de la mañana]"
            parseAbsoluteTime(lowerExpression) != null -> parseAbsoluteTime(lowerExpression)
            
            // "mañana", "pasado mañana"
            parseDayKeywords(lowerExpression) != null -> parseDayKeywords(lowerExpression)
            
            // Fechas específicas "el 15 de diciembre"
            parseDateExpression(lowerExpression) != null -> parseDateExpression(lowerExpression)
            
            else -> null
        }
    }
    
    private fun parseRelativeTime(expression: String): Instant? {
        val patterns = listOf(
            Pattern.compile("en\\s+(\\d+)\\s+horas?"),
            Pattern.compile("en\\s+(\\d+)\\s+minutos?"),
            Pattern.compile("en\\s+(\\d+)\\s+segundos?"),
            Pattern.compile("dentro\\s+de\\s+(\\d+)\\s+horas?"),
            Pattern.compile("dentro\\s+de\\s+(\\d+)\\s+minutos?")
        )
        
        for (pattern in patterns) {
            val matcher = pattern.matcher(expression)
            if (matcher.find()) {
                val value = matcher.group(1).toIntOrNull() ?: continue
                
                return when {
                    expression.contains("hora") -> Instant.now().plus(Duration.ofHours(value.toLong()))
                    expression.contains("minuto") -> Instant.now().plus(Duration.ofMinutes(value.toLong()))
                    expression.contains("segundo") -> Instant.now().plus(Duration.ofSeconds(value.toLong()))
                    else -> null
                }
            }
        }
        return null
    }
    
    private fun parseAbsoluteTime(expression: String): Instant? {
        val patterns = listOf(
            Pattern.compile("a\\s+las\\s+(\\d{1,2})(?::(\\d{2}))?\\s*(?:de la (tarde|mañana|noche))?"),
            Pattern.compile("a\\s+la\\s+una\\s*(?:de la (tarde|mañana|noche))?"),
            Pattern.compile("a\\s+las\\s+(\\d{1,2})\\s*y\\s+(\\d{2})\\s*(?:de la (tarde|mañana|noche))?")
        )
        
        for (pattern in patterns) {
            val matcher = pattern.matcher(expression)
            if (matcher.find()) {
                val hour: Int
                val minute: Int
                
                if (expression.contains("una")) {
                    hour = 1
                    minute = 0
                } else {
                    hour = matcher.group(1).toIntOrNull() ?: continue
                    minute = matcher.group(2)?.toIntOrNull() ?: 0
                }
                
                val period = matcher.group(3)
                var adjustedHour = hour
                
                // Ajustar para formato de 24 horas
                if (period != null) {
                    when (period) {
                        "tarde", "noche" -> {
                            if (adjustedHour in 1..11) adjustedHour += 12
                        }
                        "mañana" -> {
                            if (adjustedHour == 12) adjustedHour = 0
                        }
                    }
                }
                
                // Crear LocalDateTime para hoy con la hora especificada
                val now = LocalDateTime.now()
                var targetTime = now.withHour(adjustedHour).withMinute(minute).withSecond(0)
                
                // Si la hora ya pasó hoy, programar para mañana
                if (targetTime.isBefore(now)) {
                    targetTime = targetTime.plusDays(1)
                }
                
                return targetTime.atZone(java.time.ZoneId.systemDefault()).toInstant()
            }
        }
        return null
    }
    
    private fun parseDayKeywords(expression: String): Instant? {
        val now = LocalDateTime.now()
        
        return when {
            expression.contains("mañana") -> now.plusDays(1)
                .withHour(9).withMinute(0).withSecond(0)
                .atZone(java.time.ZoneId.systemDefault()).toInstant()
            
            expression.contains("pasado mañana") -> now.plusDays(2)
                .withHour(9).withMinute(0).withSecond(0)
                .atZone(java.time.ZoneId.systemDefault()).toInstant()
            
            expression.contains("hoy") -> now
                .withHour(18).withMinute(0).withSecond(0)
                .atZone(java.time.ZoneId.systemDefault()).toInstant()
            
            else -> null
        }
    }
    
    private fun parseDateExpression(expression: String): Instant? {
        // Implementación básica para fechas como "15 de diciembre"
        try {
            val formatter = DateTimeFormatter.ofPattern("d 'de' MMMM", Locale("es"))
            val dateString = expression.substringAfter("el ").substringBefore(" a las")
            
            val date = java.time.LocalDate.parse(dateString, formatter)
                .withYear(LocalDateTime.now().year)
            
            // Si la fecha ya pasó este año, usar el próximo año
            val now = LocalDateTime.now()
            val targetDate = if (date.atStartOfDay().isBefore(now)) {
                date.plusYears(1).atStartOfDay()
            } else {
                date.atStartOfDay()
            }
            
            // Verificar si hay hora específica
            val hourPattern = Pattern.compile("a las (\\d{1,2})")
            val hourMatcher = hourPattern.matcher(expression)
            if (hourMatcher.find()) {
                val hour = hourMatcher.group(1).toInt()
                return targetDate.withHour(hour)
                    .atZone(java.time.ZoneId.systemDefault()).toInstant()
            }
            
            return targetDate.atZone(java.time.ZoneId.systemDefault()).toInstant()
        } catch (e: Exception) {
            return null
        }
    }
}

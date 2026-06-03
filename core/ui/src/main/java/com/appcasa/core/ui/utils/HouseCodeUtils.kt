package com.appcasa.core.ui.utils

import java.util.Locale
import kotlin.random.Random

object HouseCodeUtils {
    /**
     * Genera un código de hogar amigable para humanos.
     * Ejemplo: CASA-7X2W
     */
    fun generateHouseCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789" // Excluimos I, O, 0, 1 por legibilidad
        val randomPart = (1..4)
            .map { chars[Random.nextInt(chars.length)] }
            .joinToString("")
        return "CASA-$randomPart".uppercase(Locale.getDefault())
    }
}

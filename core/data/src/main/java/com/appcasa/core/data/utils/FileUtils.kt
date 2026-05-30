package com.appcasa.core.data.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.*

object FileUtils {
    /**
     * Copia una imagen desde una URI externa al almacenamiento interno de la app
     * para que persista incluso si la app se cierra o el permiso temporal de la URI expira.
     */
    fun saveImageLocally(context: Context, uriString: String?): String? {
        if (uriString == null) return null
        if (uriString.startsWith("file://") || uriString.startsWith("/data/")) return uriString // Ya es local
        
        return try {
            val uri = Uri.parse(uriString)
            val inputStream = context.contentResolver.openInputStream(uri)
            val file = File(context.filesDir, "member_${UUID.randomUUID()}.jpg")
            val outputStream = FileOutputStream(file)
            
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

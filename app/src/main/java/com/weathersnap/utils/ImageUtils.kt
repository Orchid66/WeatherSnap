package com.weathersnap.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream

object ImageUtils {

    // Compress an image file and return the compressed file path + sizes
    fun compressImage(context: Context, originalFilePath: String): CompressResult {
        val originalFile = File(originalFilePath)
        val originalSizeKb = originalFile.length() / 1024

        val bitmap = BitmapFactory.decodeFile(originalFilePath)

        // Scale down if too large
        val scaledBitmap = scaleBitmap(bitmap, 1024, 1024)

        // Save compressed version to a new file
        val compressedFile = File(context.filesDir, "compressed_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(compressedFile)
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
        outputStream.flush()
        outputStream.close()

        val compressedSizeKb = compressedFile.length() / 1024

        return CompressResult(
            compressedFilePath = compressedFile.absolutePath,
            originalSizeKb = originalSizeKb,
            compressedSizeKb = compressedSizeKb
        )
    }

    private fun scaleBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxWidth && height <= maxHeight) return bitmap

        val ratio = minOf(maxWidth.toFloat() / width, maxHeight.toFloat() / height)
        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    data class CompressResult(
        val compressedFilePath: String,
        val originalSizeKb: Long,
        val compressedSizeKb: Long
    )
}

package com.studentidphotocapture.app.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import java.util.Locale

object BarcodeEncoder {

    private val PATTERNS = mapOf(
        '0' to "000110100",
        '1' to "100100001",
        '2' to "001100001",
        '3' to "101100000",
        '4' to "000110001",
        '5' to "100110000",
        '6' to "001110000",
        '7' to "000100101",
        '8' to "100100100",
        '9' to "001100100",
        'A' to "100001001",
        'B' to "001001001",
        'C' to "101001000",
        'D' to "000011001",
        'E' to "100011000",
        'F' to "001011000",
        'G' to "000001101",
        'H' to "100001100",
        'I' to "001001100",
        'J' to "000011100",
        'K' to "100000011",
        'L' to "001000011",
        'M' to "101000010",
        'N' to "000010011",
        'O' to "100010010",
        'P' to "001010010",
        'Q' to "000000111",
        'R' to "100000110",
        'S' to "001000110",
        'T' to "000010110",
        'U' to "110000001",
        'V' to "011000001",
        'W' to "111000000",
        'X' to "010010001",
        'Y' to "110010000",
        'Z' to "011010000",
        '-' to "010000101",
        '.' to "110000100",
        ' ' to "011000100",
        '$' to "010101000",
        '/' to "010100010",
        '+' to "010001010",
        '%' to "000101010",
        '*' to "010010100"
    )

    /**
     * Generates a Code 39 barcode bitmap for the given text.
     */
    fun generateCode39(text: String, width: Int, height: Int): Bitmap? {
        val uppercaseText = text.uppercase(Locale.US)
        
        // Filter text to contain only supported Code 39 characters
        val filteredText = StringBuilder()
        for (char in uppercaseText) {
            if (PATTERNS.containsKey(char) && char != '*') {
                filteredText.append(char)
            } else if (char == '-' || char == '_' || char == '/') {
                filteredText.append(char)
            } else {
                filteredText.append(' ') // replace invalid character with space
            }
        }
        
        // Frame with start and stop characters
        val fullString = "*$filteredText*"
        val length = fullString.length

        // Total modules = Quiet zones (2 * 10) + L * 15 (elements) + (L - 1) * 1 (gaps)
        val totalModules = 20 + 16 * length - 1
        
        val w = (width / totalModules).coerceAtLeast(1)
        val actualBarcodeWidth = totalModules * w
        
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Fill canvas with transparent background
        canvas.drawColor(Color.TRANSPARENT)
        
        val paintBlack = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.FILL
        }
        val paintWhite = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        
        // Center the barcode horizontally
        var startX = (width - actualBarcodeWidth) / 2
        
        // Draw left quiet zone (white background)
        canvas.drawRect(startX.toFloat(), 0f, (startX + 10 * w).toFloat(), height.toFloat(), paintWhite)
        startX += 10 * w
        
        for (charIndex in 0 until length) {
            val char = fullString[charIndex]
            val pattern = PATTERNS[char] ?: PATTERNS[' ']!! // Fallback to space pattern if missing
            
            for (elementIndex in 0..8) {
                val isBar = elementIndex % 2 == 0
                val isWide = pattern[elementIndex] == '1'
                val elementWidth = if (isWide) 3 * w else w
                
                val endX = startX + elementWidth
                
                val paint = if (isBar) paintBlack else paintWhite
                canvas.drawRect(startX.toFloat(), 0f, endX.toFloat(), height.toFloat(), paint)
                
                startX = endX
            }
            
            // Draw inter-character gap
            if (charIndex < length - 1) {
                val endX = startX + w
                canvas.drawRect(startX.toFloat(), 0f, endX.toFloat(), height.toFloat(), paintWhite)
                startX = endX
            }
        }
        
        // Draw right quiet zone
        val endX = startX + 10 * w
        canvas.drawRect(startX.toFloat(), 0f, endX.toFloat(), height.toFloat(), paintWhite)
        
        return bitmap
    }
}

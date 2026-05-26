package com.studentidphotocapture.app.util

import android.content.Context
import android.graphics.Color
import java.io.File

object TemplateConfigManager {
    private const val PREFS_NAME = "id_card_template_prefs"

    fun getTemplateFile(context: Context, schoolCode: String): File {
        val dir = File(context.filesDir, "templates")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "template_$schoolCode.jpg")
    }

    fun hasCustomTemplate(context: Context, schoolCode: String): Boolean {
        return getTemplateFile(context, schoolCode).exists()
    }

    fun getPhotoShape(context: Context, schoolCode: String): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString("photo_shape_$schoolCode", "circle") ?: "circle"
    }

    fun setPhotoShape(context: Context, schoolCode: String, shape: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString("photo_shape_$schoolCode", shape).apply()
    }

    fun getPhotoSize(context: Context, schoolCode: String): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt("photo_size_$schoolCode", 156)
    }

    fun setPhotoSize(context: Context, schoolCode: String, sizeDp: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt("photo_size_$schoolCode", sizeDp).apply()
    }

    fun getPhotoY(context: Context, schoolCode: String): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt("photo_y_$schoolCode", 120)
    }

    fun setPhotoY(context: Context, schoolCode: String, yDp: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt("photo_y_$schoolCode", yDp).apply()
    }

    fun getTextY(context: Context, schoolCode: String): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt("text_y_$schoolCode", 284)
    }

    fun setTextY(context: Context, schoolCode: String, yDp: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt("text_y_$schoolCode", yDp).apply()
    }

    fun getBarcodeY(context: Context, schoolCode: String): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt("barcode_y_$schoolCode", 38)
    }

    fun setBarcodeY(context: Context, schoolCode: String, yDp: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt("barcode_y_$schoolCode", yDp).apply()
    }

    fun getTextColor(context: Context, schoolCode: String): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString("text_color_$schoolCode", "#FFFFFF") ?: "#FFFFFF"
    }

    fun setTextColor(context: Context, schoolCode: String, hexColor: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString("text_color_$schoolCode", hexColor).apply()
    }

    // Helper functions for density conversions
    fun dpToPx(context: Context, dp: Int): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }

    fun pxToDp(context: Context, px: Int): Int {
        val density = context.resources.displayMetrics.density
        return if (density == 0f) 0 else (px / density).toInt()
    }
}

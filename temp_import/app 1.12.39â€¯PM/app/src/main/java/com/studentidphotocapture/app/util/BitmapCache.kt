package com.studentidphotocapture.app.util

import android.graphics.Bitmap

object BitmapCache {
    private var cachedBitmap: Bitmap? = null
    
    fun setBitmap(bitmap: Bitmap) {
        cachedBitmap = bitmap
    }
    
    fun getBitmap(): Bitmap? {
        return cachedBitmap
    }
    
    fun clear() {
        cachedBitmap = null
    }
}

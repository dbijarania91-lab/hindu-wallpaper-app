package com.mahiinfo.hinduwallpaper.livewallpaper

import android.graphics.*
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import coil.ImageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.*

class LiveWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine = LiveWallpaperEngine()

    inner class LiveWallpaperEngine : Engine() {
        private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
        private var bitmap: Bitmap? = null
        private var liveFrames: List<Bitmap> = emptyList()
        private var frameIndex = 0
        private var isRunning = false
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Set from outside before applying
        var imageUrl: String = ""
        var isLive: Boolean = false

        override fun onSurfaceCreated(holder: SurfaceHolder) {
            super.onSurfaceCreated(holder)
            loadAndRender()
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            drawFrame()
        }

        override fun onVisibilityChanged(visible: Boolean) {
            isRunning = visible
            if (visible) startAnimation() else stopAnimation()
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder) {
            isRunning = false
            scope.cancel()
        }

        private fun loadAndRender() {
            if (imageUrl.isBlank()) return
            scope.launch {
                val loader = ImageLoader(this@LiveWallpaperService)
                val request = ImageRequest.Builder(this@LiveWallpaperService)
                    .data(imageUrl)
                    .build()
                val result = loader.execute(request)
                bitmap = (result.drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
                drawFrame()
                if (isLive) startAnimation()
            }
        }

        private fun startAnimation() {
            if (!isLive) return
            scope.launch {
                while (isRunning) {
                    delay(80) // ~12fps for live wallpaper
                    drawFrame()
                }
            }
        }

        private fun stopAnimation() { /* coroutine cancelled via isRunning */ }

        private fun drawFrame() {
            val holder = surfaceHolder
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                canvas?.let { c ->
                    val bmp = bitmap ?: return
                    val w = c.width.toFloat()
                    val h = c.height.toFloat()
                    val bmpW = bmp.width.toFloat()
                    val bmpH = bmp.height.toFloat()

                    // Scale to fill, center-crop
                    val scale = maxOf(w / bmpW, h / bmpH)
                    val scaledW = bmpW * scale
                    val scaledH = bmpH * scale
                    val left = (w - scaledW) / 2f
                    val top = (h - scaledH) / 2f

                    c.drawColor(Color.BLACK)
                    c.drawBitmap(bmp, null, RectF(left, top, left + scaledW, top + scaledH), paint)
                }
            } finally {
                canvas?.let { holder.unlockCanvasAndPost(it) }
            }
        }
    }
}

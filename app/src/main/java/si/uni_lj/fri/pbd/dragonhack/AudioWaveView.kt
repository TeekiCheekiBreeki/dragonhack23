package si.uni_lj.fri.pbd.dragonhack

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

class AudioWaveView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private val paint = Paint()
    private val path = Path()

    var waveData: ByteArray = ByteArray(0)
        set(value) {
            field = value
            invalidate()
        }

    init {
        paint.color = Color.BLACK
        paint.strokeWidth = 4f
        paint.style = Paint.Style.STROKE
        paint.isAntiAlias = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawWave(canvas)
    }

    private fun drawWave(canvas: Canvas) {
        val centerY = height / 2f
        val width = width.toFloat()
        val stepX = width / waveData.size

        path.reset()
        path.moveTo(0f, centerY)

        for (i in waveData.indices) {
            val x = i * stepX
            val y = centerY + waveData[i].toFloat()
            path.lineTo(x, y)
        }

        canvas.drawPath(path, paint)
    }
}

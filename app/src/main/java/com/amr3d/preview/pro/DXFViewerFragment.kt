package com.amr3d.preview.pro

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlin.math.min
import kotlin.math.sqrt

class DXFViewerFragment : Fragment() {

    private lateinit var dxfCanvas: DXFCanvasView
    private lateinit var coordsDisplay: TextView
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = FrameLayout(requireContext()).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        setBackgroundColor(0xFF000.toInt())
        
        dxfCanvas = DXFCanvasView(requireContext())
        addView(dxfCanvas)
        
        coordsDisplay = TextView(requireContext()).apply {
            textSize = 12f
            setTextColor(0xFF88FF88.toInt())
            setPadding(16, 16, 16, 16)
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { gravity = android.view.Gravity.TOP or android.view.Gravity.LEFT }
            text = "X: 0.00, Y: 0.00"
            setBackgroundColor(0x88000000.toInt())
        }
        addView(coordsDisplay)
    }
    
    fun loadDXFFile(data: DXFData) {
        dxfCanvas.setDXFData(data)
        dxfCanvas.coordDisplayCallback = { x, y ->
            coordsDisplay.text = String.format("X: %.2f, Y: %.2f", x, y)
        }
        Toast.makeText(context, "تم تحميل ملف DXF: ${data.lines.size} خط", Toast.LENGTH_SHORT).show()
    }
}

data class DXFData(
    val lines: List<DXFLine>,
    val circles: List<DXFCircle> = emptyList(),
    val arcs: List<DXFArc> = emptyList()
)

data class DXFLine(val x1: Float, val y1: Float, val x2: Float, val y2: Float, val color: Int = 0xFFFFFFFF.toInt())
data class DXFCircle(val cx: Float, val cy: Float, val r: Float, val color: Int = 0xFFFFFFFF.toInt())
data class DXFArc(val cx: Float, val cy: Float, val r: Float, val start: Float, val end: Float, val color: Int = 0xFFFFFFFF.toInt())

class DXFCanvasView(context: Context) : View(context) {

    private var dxfData: DXFData? = null
    var coordDisplayCallback: ((Float, Float) -> Unit)? = null
    
    private var zoom = 1f
    private var panX = 0f
    private var panY = 0f
    
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF333.toInt()
        strokeWidth = 0.5f
    }
    
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }
    
    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 1.5f
        style = Paint.Style.STROKE
    }
    
    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean = true
        
        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
            panX -= distanceX / zoom
            panY -= distanceY / zoom
            invalidate()
            return true
        }
        
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            val worldX = (e.x / zoom) - panX
            val worldY = (e.y / zoom) - panY
            coordDisplayCallback?.invoke(worldX, worldY)
            return true
        }
        
        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean = false
    })
    
    private val scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.OnScaleGestureListener {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            zoom *= detector.scaleFactor
            zoom = zoom.coerceIn(0.1f, 10f)
            invalidate()
            return true
        }
        override fun onScaleBegin(detector: ScaleGestureDetector) = true
        override fun onScaleEnd(detector: ScaleGestureDetector) {}
    })
    
    init {
        setBackgroundColor(0xFF000.toInt())
    }
    
    fun setDXFData(data: DXFData) {
        dxfData = data
        // استنى لما الـ View ياخد المقاس وبعدين اعمل fit
        post { fitToScreen() }
        invalidate()
    }
    
    private fun fitToScreen() {
        val data = dxfData ?: return
        if (width == 0 || height == 0) return
        
        var minX = Float.MAX_VALUE
        var maxX = -Float.MAX_VALUE
        var minY = Float.MAX_VALUE
        var maxY = -Float.MAX_VALUE
        
        data.lines.forEach { line ->
            minX = minOf(minX, line.x1, line.x2)
            maxX = maxOf(maxX, line.x1, line.x2)
            minY = minOf(minY, line.y1, line.y2)
            maxY = maxOf(maxY, line.y1, line.y2)
        }
        data.circles.forEach { c ->
            minX = minOf(minX, c.cx - c.r)
            maxX = maxOf(maxX, c.cx + c.r)
            minY = minOf(minY, c.cy - c.r)
            maxY = maxOf(maxY, c.cy + c.r)
        }
        
        if (minX == Float.MAX_VALUE) return
        
        val width = maxX - minX
        val height = maxY - minY
        if (width == 0f || height == 0f) return
        
        val scale = min(this.width / (width * 1.1f), this.height / (height * 1.1f))
        zoom = scale.coerceIn(0.1f, 10f)
        panX = -(minX + width / 2) + this.width / (2 * zoom)
        panY = -(minY + height / 2) + this.height / (2 * zoom)
    }
    
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            scaleDetector.onTouchEvent(event)
            gestureDetector.onTouchEvent(event)
        }
        return true
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawGrid(canvas)
        
        dxfData?.let { data ->
            data.lines.forEach { line ->
                linePaint.color = line.color
                val x1 = (line.x1 + panX) * zoom
                val y1 = (line.y1 + panY) * zoom
                val x2 = (line.x2 + panX) * zoom
                val y2 = (line.y2 + panY) * zoom
                canvas.drawLine(x1, y1, x2, y2, linePaint)
            }
            
            data.circles.forEach { circle ->
                circlePaint.color = circle.color
                val cx = (circle.cx + panX) * zoom
                val cy = (circle.cy + panY) * zoom
                val r = circle.r * zoom
                canvas.drawCircle(cx, cy, r, circlePaint)
            }
            
            data.arcs.forEach { arc ->
                circlePaint.color = arc.color
                val cx = (arc.cx + panX) * zoom
                val cy = (arc.cy + panY) * zoom
                val r = arc.r * zoom
                val rectF = RectF(cx - r, cy - r, cx + r, cy + r)
                canvas.drawArc(rectF, arc.start, arc.end - arc.start, false, circlePaint)
            }
        }
    }
    
    private fun drawGrid(canvas: Canvas) {
        if (zoom < 0.01f) return
        val gridSpacing = 50 * zoom
        
        var x = (panX * zoom) % gridSpacing
        while (x < width) {
            canvas.drawLine(x, 0f, x, height.toFloat(), gridPaint)
            x += gridSpacing
        }
        
        var y = (panY * zoom) % gridSpacing
        while (y < height) {
            canvas.drawLine(0f, y, width.toFloat(), y, gridPaint)
            y += gridSpacing
        }
    }
}

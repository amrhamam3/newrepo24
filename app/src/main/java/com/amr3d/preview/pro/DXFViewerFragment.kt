package com.amr3d.preview.pro

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycleScope
import kotlinx.coroutines.*
import android.content.Context
import android.graphics.*
import android.view.GestureDetector.OnGestureListener

class DXFViewerFragment : Fragment(), OnGestureListener {

    private lateinit var dxfView: DXFView
    private lateinit var gestureDetector: GestureDetector
    private var dxfData: DXFData? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        dxfView = DXFView(requireContext())
        gestureDetector = GestureDetector(requireContext(), this)
        dxfView.setOnTouchListener { _, event -> gestureDetector.onTouchEvent(event) }
        return dxfView
    }

    fun loadDXFFile(data: DXFData) {
        dxfData = data
        dxfView.setData(data)
    }

    // GestureDetector callbacks
    override fun onDown(e: MotionEvent): Boolean = true
    override fun onShowPress(e: MotionEvent) {}
    override fun onSingleTapUp(e: MotionEvent): Boolean = false
    override fun onLongPress(e: MotionEvent) {}

    override fun onScroll(
        e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float
    ): Boolean {
        dxfView.pan(distanceX, distanceY)
        return true
    }

    override fun onFling(
        e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float
    ): Boolean {
        dxfView.zoom(velocityX, velocityY)
        return true
    }
}

// View بسيطة للرسم
class DXFView(context: Context) : View(context) {
    private val paint = Paint().apply { color = Color.WHITE; strokeWidth = 3f; style = Paint.Style.STROKE }
    private var data: DXFData? = null
    private var offsetX = 0f
    private var offsetY = 0f
    private var scale = 1f

    fun setData(d: DXFData) {
        data = d
        invalidate()
    }

    fun pan(dx: Float, dy: Float) {
        offsetX += dx
        offsetY += dy
        invalidate()
    }

    fun zoom(vx: Float, vy: Float) {
        scale *= 1.1f
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.BLACK)
        canvas.save()
        canvas.translate(width / 2f + offsetX, height / 2f + offsetY)
        canvas.scale(scale, scale)
        data?.let { d ->
            d.lines.forEach { line ->
                canvas.drawLine(line.x1, line.y1, line.x2, line.y2, paint)
            }
        }
        canvas.restore()
    }
}

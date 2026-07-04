package com.amr3d.preview.pro

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.*

/**
 * خلفية Wireframe ثلاثية الأبعاد — مستوحاة من بيئة عمل برامج 3D.
 * تستجيب لللمس وتحرّك المشهد بناءً عليه.
 */
class WireframeSplashView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    var accentColor: Int = Color.parseColor("#FF8A1E")
    var touchForce: Float = 0f

    private var wfAngle = 0f
    private var wfAngleX = 0f
    private var wfAngleY = 0f
    private var targetAngleX = 0f
    private var targetAngleY = 0f

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val bgPaint = Paint()

    // نقاط عشوائية للنجوم
    private data class Star(var x: Float, var y: Float, val r: Float, var alpha: Float, val dAlpha: Float, val vx: Float, val vy: Float)
    private val stars = mutableListOf<Star>()

    private val cubes = listOf(
        floatArrayOf(-90f,-40f,30f,25f,0.3f),
        floatArrayOf(80f,-25f,-40f,18f,-0.4f),
        floatArrayOf(-60f,15f,-90f,30f,0.2f),
        floatArrayOf(100f,-60f,50f,14f,0.5f),
        floatArrayOf(-110f,25f,-25f,20f,-0.3f),
        floatArrayOf(50f,35f,-110f,26f,0.35f)
    )

    init {
        setWillNotDraw(false)
    }

    fun initStars(w: Int, h: Int) {
        stars.clear()
        repeat(70) {
            stars.add(Star(
                x = (Math.random() * w).toFloat(),
                y = (Math.random() * h).toFloat(),
                r = (Math.random() * 1.5f + 0.3f).toFloat(),
                alpha = Math.random().toFloat(),
                dAlpha = (0.005f + Math.random().toFloat() * 0.01f) * if (Math.random() < 0.5) 1f else -1f,
                vx = ((Math.random() - 0.5) * 0.3).toFloat(),
                vy = ((Math.random() - 0.5) * 0.3).toFloat()
            ))
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(w, h, oldW, oldH)
        if (stars.isEmpty()) initStars(w, h)
    }

    private fun project3D(x: Float, y: Float, z: Float): FloatArray {
        val W = width.toFloat(); val H = height.toFloat()
        val fov = 320f; val cx = W / 2f; val cy = H * 0.52f

        val cosY = cos(wfAngleY); val sinY = sin(wfAngleY)
        val rx = x * cosY - z * sinY
        val rz = x * sinY + z * cosY

        val cosX = cos(wfAngleX); val sinX = sin(wfAngleX)
        val ry = y * cosX - rz * sinX
        val rz2 = y * sinX + rz * cosX

        val scale = fov / (fov + rz2 + 250f)
        return floatArrayOf(cx + rx * scale, cy - ry * scale, scale)
    }

    private fun drawLine3D(canvas: Canvas, x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float, alpha: Float) {
        val p1 = project3D(x1, y1, z1)
        val p2 = project3D(x2, y2, z2)
        linePaint.alpha = (alpha * (p1[2] + p2[2]) * 0.5f * 255f).toInt().coerceIn(0, 255)
        canvas.drawLine(p1[0], p1[1], p2[0], p2[1], linePaint)
    }

    fun updatePhysics() {
        wfAngleY += (targetAngleY - wfAngleY) * 0.08f
        wfAngleX += (targetAngleX - wfAngleX) * 0.08f
        wfAngle += 0.003f + touchForce * 0.03f
        touchForce *= 0.88f

        val W = width.toFloat(); val H = height.toFloat()
        stars.forEach { s ->
            s.x = (s.x + s.vx + W) % W
            s.y = (s.y + s.vy + H) % H
            var a = s.alpha + s.dAlpha
            if (a < 0f || a > 1f) { a = a.coerceIn(0f, 1f) }
        }
    }

    override fun onDraw(canvas: Canvas) {
        val W = width.toFloat(); val H = height.toFloat()

        // خلفية
        val bgShader = RadialGradient(W/2, H/2, H*0.8f,
            intArrayOf(Color.parseColor("#0A0C10"), Color.BLACK),
            floatArrayOf(0f, 1f), Shader.TileMode.CLAMP)
        bgPaint.shader = bgShader
        canvas.drawRect(0f, 0f, W, H, bgPaint)

        // النجوم المتصلة
        val accentHex = String.format("%06X", accentColor and 0xFFFFFF)
        linePaint.strokeWidth = 0.5f
        linePaint.color = accentColor
        stars.forEachIndexed { i, s ->
            dotPaint.color = accentColor
            dotPaint.alpha = (s.alpha * 200f).toInt()
            canvas.drawCircle(s.x, s.y, s.r, dotPaint)

            for (j in i+1 until stars.size) {
                val s2 = stars[j]
                val d = hypot(s.x - s2.x, s.y - s2.y)
                if (d < 80f) {
                    linePaint.alpha = ((1f - d/80f) * 50f).toInt()
                    canvas.drawLine(s.x, s.y, s2.x, s2.y, linePaint)
                }
            }
        }

        // الشبكة الأرضية
        linePaint.strokeWidth = 0.8f
        linePaint.color = accentColor
        val GS = 35f; val GR = 9; val GY = -70f
        for (i in -GR..GR) {
            val a = 0.2f + abs(i).toFloat()/GR * 0.15f + touchForce * 0.15f
            drawLine3D(canvas, i*GS, GY, -GR*GS, i*GS, GY, GR*GS, a)
            drawLine3D(canvas, -GR*GS, GY, i*GS, GR*GS, GY, i*GS, a)
        }

        // المحاور (X=أحمر، Y=أخضر، Z=أزرق)
        linePaint.strokeWidth = 1.5f
        val pO = project3D(0f,0f,0f)
        val pH = 90f
        linePaint.color = Color.rgb(255,80,80)
        linePaint.alpha = 140
        val pX = project3D(pH,0f,0f)
        canvas.drawLine(pO[0],pO[1],pX[0],pX[1],linePaint)
        linePaint.color = Color.rgb(80,255,140)
        val pY = project3D(0f,pH,0f)
        canvas.drawLine(pO[0],pO[1],pY[0],pY[1],linePaint)
        linePaint.color = Color.rgb(80,140,255)
        val pZ = project3D(0f,0f,pH)
        canvas.drawLine(pO[0],pO[1],pZ[0],pZ[1],linePaint)

        // المكعبات
        linePaint.strokeWidth = 1f
        linePaint.color = accentColor
        cubes.forEach { c ->
            val a = wfAngle * c[4]
            val ca = cos(a); val sa = sin(a)
            val hs = c[3] * (1f + touchForce * 0.5f)
            val rawVerts = arrayOf(
                floatArrayOf(-hs,-hs,-hs), floatArrayOf(hs,-hs,-hs),
                floatArrayOf(hs,hs,-hs),   floatArrayOf(-hs,hs,-hs),
                floatArrayOf(-hs,-hs,hs),  floatArrayOf(hs,-hs,hs),
                floatArrayOf(hs,hs,hs),    floatArrayOf(-hs,hs,hs)
            )
            val verts = rawVerts.map { v ->
                val rx = v[0]*ca - v[2]*sa; val rz = v[0]*sa + v[2]*ca
                project3D(c[0]+rx, c[1]+v[1], c[2]+rz)
            }
            val edges = arrayOf(0,1,1,2,2,3,3,0, 4,5,5,6,6,7,7,4, 0,4,1,5,2,6,3,7)
            for (e in edges.indices step 2) {
                val v1 = verts[edges[e]]; val v2 = verts[edges[e+1]]
                linePaint.alpha = ((0.5f*(v1[2]+v2[2]) + touchForce*0.5f) * 255f).toInt().coerceIn(40, 200)
                canvas.drawLine(v1[0],v1[1],v2[0],v2[1],linePaint)
            }

            // نقاط vertex
            dotPaint.color = accentColor
            verts.forEach { v ->
                dotPaint.alpha = 120
                canvas.drawCircle(v[0], v[1], 1.5f, dotPaint)
            }
        }

        // تعتيم مركزي لإبراز اللوجو
        val vignette = RadialGradient(W/2, H*0.38f, 160f,
            intArrayOf(Color.argb(190,0,0,0), Color.TRANSPARENT),
            floatArrayOf(0f, 1f), Shader.TileMode.CLAMP)
        bgPaint.shader = vignette
        canvas.drawRect(0f, 0f, W, H, bgPaint)
    }

    fun onTouch(x: Float, y: Float) {
        touchForce = 1.5f
        targetAngleY = (x / width - 0.5f) * 1.8f
        targetAngleX = (y / height - 0.5f) * 1.2f
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
            onTouch(event.x, event.y)
        }
        return true
    }
}

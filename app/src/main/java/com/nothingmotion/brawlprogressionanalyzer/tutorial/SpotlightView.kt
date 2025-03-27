package com.nothingmotion.brawlprogressionanalyzer.tutorial

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.RadialGradient
import android.graphics.Shader
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

/**
 * A custom view that creates a spotlight effect by punching a hole in a semi-transparent background
 * with an attractive glow effect around the highlighted area
 */
class SpotlightView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val backgroundPaint = Paint().apply {
        color = 0xE6000000.toInt() // Semi-transparent black
        style = Paint.Style.FILL
    }

    private val holePaint = Paint().apply {
        color = 0x00000000 // Transparent
        style = Paint.Style.FILL
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) // This creates the "hole" effect
    }

    private val borderPaint = Paint().apply {
        color = 0xFFFFFFFF.toInt() // White
        style = Paint.Style.STROKE
        strokeWidth = 4f
        isAntiAlias = true
    }

    // Main glow paint for outer ring
    private val glowPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 15f
        isAntiAlias = true
        alpha = 180
    }
    
    // Inner glow paint (brighter)
    private val innerGlowPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 8f
        isAntiAlias = true
        alpha = 200
    }
    
    // Outer glow paint (subtle)
    private val outerGlowPaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 20f
        isAntiAlias = true
        alpha = 100
    }
    
    // Pulse animation values
    private var pulseScale = 0f
    private var pulseAnimator: ValueAnimator? = null
    private val pulsePaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
        isAntiAlias = true
    }

    private var targetRect = RectF(0f, 0f, 0f, 0f)
    private var currentRect = RectF(0f, 0f, 0f, 0f)
    private var padding = 0f
    private var animator: ValueAnimator? = null

    init {
        // Start the pulse animation
        startPulseAnimation()
    }
    
    private fun startPulseAnimation() {
        pulseAnimator?.cancel()
        
        pulseAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 1500
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            interpolator = AccelerateDecelerateInterpolator()
            
            addUpdateListener { valueAnimator ->
                pulseScale = valueAnimator.animatedValue as Float
                invalidate()
            }
            
            start()
        }
    }

    /**
     * Set the target rectangle to spotlight with animation
     * @param rect The rectangle to spotlight
     * @param padding Additional padding around the rectangle
     */
    fun setTarget(rect: RectF, padding: Float = 0f) {
        this.padding = padding
        
        // Calculate the final target rectangle
        val newTargetRect = RectF(
            rect.left - padding,
            rect.top - padding,
            rect.right + padding,
            rect.bottom + padding
        )
        
        // If this is the first target, set it immediately without animation
        if (targetRect.isEmpty) {
            targetRect = newTargetRect
            currentRect = RectF(targetRect)
            updateGradients()
            // Store current rect as tag for access by TutorialManager
            tag = RectF(currentRect)
            invalidate()
            return
        }
        
        // Stop any existing animation
        animator?.cancel()
        
        // Set final target
        targetRect = newTargetRect
        
        // Animate to new target
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 300
            interpolator = AccelerateDecelerateInterpolator()
            
            addUpdateListener { valueAnimator ->
                val fraction = valueAnimator.animatedValue as Float
                
                // Interpolate between current and target
                currentRect.left = lerp(currentRect.left, targetRect.left, fraction)
                currentRect.top = lerp(currentRect.top, targetRect.top, fraction)
                currentRect.right = lerp(currentRect.right, targetRect.right, fraction)
                currentRect.bottom = lerp(currentRect.bottom, targetRect.bottom, fraction)
                
                // Store current rect as tag for access by TutorialManager
                tag = RectF(currentRect)
                
                updateGradients()
                invalidate()
            }
            
            start()
        }
    }
    
    private fun updateGradients() {
        if (currentRect.isEmpty) return
        
        // Calculate center and radius for the gradient
        val centerX = currentRect.centerX()
        val centerY = currentRect.centerY()
        val radius = maxOf(currentRect.width(), currentRect.height()) / 2f + 40f
        
        // Create radial gradients for the glow effects
        glowPaint.shader = RadialGradient(
            centerX, centerY, radius,
            Color.WHITE, Color.TRANSPARENT,
            Shader.TileMode.CLAMP
        )
        
        innerGlowPaint.shader = RadialGradient(
            centerX, centerY, radius - 10f,
            Color.WHITE, Color.TRANSPARENT,
            Shader.TileMode.CLAMP
        )
        
        outerGlowPaint.shader = RadialGradient(
            centerX, centerY, radius + 20f,
            Color.WHITE, Color.TRANSPARENT,
            Shader.TileMode.CLAMP
        )
        
        pulsePaint.shader = RadialGradient(
            centerX, centerY, radius + (50f * pulseScale),
            Color.argb((150 * (1 - pulseScale)).toInt(), 255, 255, 255),
            Color.TRANSPARENT,
            Shader.TileMode.CLAMP
        )
    }
    
    private fun lerp(start: Float, end: Float, fraction: Float): Float {
        return start + (end - start) * fraction
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Skip drawing if the target rect is empty
        if (currentRect.isEmpty) return

        // This is needed to create the hole effect
        val saveCount = canvas.saveLayer(0f, 0f, width.toFloat(), height.toFloat(), null)

        // Draw the semi-transparent background
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

        // Draw the hole
        canvas.drawOval(currentRect, holePaint)

        // Restore the canvas
        canvas.restoreToCount(saveCount)

        // Draw the glow effects (outside the saveLayer to avoid being affected by the xfermode)
        val pulseRect = RectF(
            currentRect.left - (40f * pulseScale),
            currentRect.top - (40f * pulseScale),
            currentRect.right + (40f * pulseScale),
            currentRect.bottom + (40f * pulseScale)
        )
        canvas.drawOval(pulseRect, pulsePaint)
        
        // Draw outer glow
        val outerGlowRect = RectF(
            currentRect.left - 15f,
            currentRect.top - 15f,
            currentRect.right + 15f,
            currentRect.bottom + 15f
        )
        canvas.drawOval(outerGlowRect, outerGlowPaint)
        
        // Draw main glow
        val glowRect = RectF(
            currentRect.left - 10f,
            currentRect.top - 10f,
            currentRect.right + 10f,
            currentRect.bottom + 10f
        )
        canvas.drawOval(glowRect, glowPaint)
        
        // Draw inner glow
        val innerGlowRect = RectF(
            currentRect.left - 5f,
            currentRect.top - 5f,
            currentRect.right + 5f,
            currentRect.bottom + 5f
        )
        canvas.drawOval(innerGlowRect, innerGlowPaint)

        // Draw the main border
        canvas.drawOval(currentRect, borderPaint)
    }
    
    override fun onDetachedFromWindow() {
        // Clean up animators when view is detached
        animator?.cancel()
        pulseAnimator?.cancel()
        super.onDetachedFromWindow()
    }
} 
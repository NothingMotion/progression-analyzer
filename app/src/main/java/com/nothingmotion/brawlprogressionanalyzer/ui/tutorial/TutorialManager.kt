package com.nothingmotion.brawlprogressionanalyzer.ui.tutorial

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.graphics.RectF
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.OvershootInterpolator
import androidx.core.animation.doOnEnd
import com.nothingmotion.brawlprogressionanalyzer.databinding.TutorialOverlayBinding

class TutorialManager(private val activity: Activity) {

    private lateinit var binding: TutorialOverlayBinding
    private var currentStep = 0
    private var tutorialSteps: List<TutorialStep> = emptyList()
    private var onTutorialComplete: () -> Unit = {}

    data class TutorialStep(
        val targetViewId: Int,
        val message: String,
        val spotlightPadding: Int = 16
    )

    fun initTutorial(steps: List<TutorialStep>, onComplete: () -> Unit) {
        tutorialSteps = steps
        onTutorialComplete = onComplete
        setupTutorialOverlay()
    }

    private fun setupTutorialOverlay() {
        // Keep screen on during tutorial
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        // Inflate tutorial overlay
        binding = TutorialOverlayBinding.inflate(LayoutInflater.from(activity))
        
        // Add overlay to window at highest level
        val parent = activity.window.decorView as ViewGroup
        val params = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        parent.addView(binding.root, params)
        
        // Ensure overlay is on top
        binding.root.bringToFront()
        binding.root.requestLayout()
        
        // Set up buttons
        binding.btnNext.setOnClickListener { showNextStep() }
        binding.btnSkip.setOnClickListener { endTutorial() }
        
        // Hide message card initially
        binding.tutorialMessageCard.alpha = 0f
        
        // Block touch events on the entire overlay to prevent interaction with underlying views
        binding.root.setOnTouchListener { _, event ->
            // Only allow touch events on the tutorial buttons
            val touchX = event.x
            val touchY = event.y
            
            // Check if touch is on the tutorial buttons
            val isTouchOnButton = isTouchOnTutorialControls(touchX, touchY)
            
            // Consume all touch events except those on the buttons
            !isTouchOnButton
        }
        
        // Set the root view to intercept all touch events
        binding.root.isClickable = true
        binding.root.isFocusable = true
        binding.root.setOnClickListener { /* Consume all clicks on the overlay */ }
        
        // Make sure the spotlight view also prevents touches from reaching through
        binding.spotlight.isClickable = true
        binding.spotlight.isFocusable = true
        binding.spotlight.setOnTouchListener { _, _ -> true }  // Consume all touch events

        // Add interceptors to all child views
        addInterceptTouchToAllChildren(binding.root)
        
        // Start first step after layout
        binding.root.post {
            showStep(0)
        }
    }
    
    /**
     * Recursively adds touch interceptors to all child views to prevent touch events
     * from reaching through to underlying views
     */
    private fun addInterceptTouchToAllChildren(viewGroup: View) {
        if (viewGroup is ViewGroup) {
            // Set touch interceptor for the ViewGroup itself
            viewGroup.isClickable = true
            viewGroup.isFocusable = true
            
            // For each child in the ViewGroup
            for (i in 0 until viewGroup.childCount) {
                val child = viewGroup.getChildAt(i)
                
                // Skip buttons and message views to ensure they remain interactive
                if (child == binding.btnNext || child == binding.btnSkip || 
                    child == binding.tutorialMessageCard) {
                    continue
                }
                
                // Set touch interceptor for this child
                child.isClickable = true
                child.isFocusable = true
                
                // If the child is another ViewGroup, recursively add to its children
                if (child is ViewGroup) {
                    addInterceptTouchToAllChildren(child)
                } else {
                    child.setOnTouchListener { _, _ -> true } // Consume all touch events
                }
            }
        }
    }
    
    private fun isTouchOnTutorialControls(x: Float, y: Float): Boolean {
        // Check if touch is on the next button
        val nextButtonRect = Rect()
        binding.btnNext.getGlobalVisibleRect(nextButtonRect)
        
        // Check if touch is on the skip button
        val skipButtonRect = Rect()
        binding.btnSkip.getGlobalVisibleRect(skipButtonRect)
        
        // Check if touch is on the message card
        val messageCardRect = Rect()
        binding.tutorialMessageCard.getGlobalVisibleRect(messageCardRect)
        
        // Convert x, y to global coordinates
        val location = IntArray(2)
        binding.root.getLocationOnScreen(location)
        val globalX = x + location[0]
        val globalY = y + location[1]
        
        return nextButtonRect.contains(globalX.toInt(), globalY.toInt()) ||
               skipButtonRect.contains(globalX.toInt(), globalY.toInt()) ||
               messageCardRect.contains(globalX.toInt(), globalY.toInt())
    }
    
    private fun showStep(stepIndex: Int) {
        if (stepIndex >= tutorialSteps.size) {
            endTutorial()
            return
        }
        
        currentStep = stepIndex
        val step = tutorialSteps[stepIndex]
        
        // Find target view
        val targetView = activity.findViewById<View>(step.targetViewId)
        if (targetView == null) {
            showNextStep()
            return
        }
        
        // Update button text
        binding.btnNext.text = if (stepIndex == tutorialSteps.size - 1) "Finish" else "Next"
        
        // Update message
        binding.tutorialMessage.text = step.message
        
        // Position and animate spotlight and message
        positionSpotlightAndMessage(targetView, step.spotlightPadding)
    }
    
    private fun positionSpotlightAndMessage(targetView: View, padding: Int) {
        // Make sure target view is properly measured
        if (targetView.width == 0 || targetView.height == 0) {
            targetView.post {
                positionSpotlightAndMessage(targetView, padding)
            }
            return
        }
        
        // Get target view coordinates accurately
        val location = IntArray(2)
        targetView.getLocationOnScreen(location)
        
        val viewRect = RectF(
            location[0].toFloat(),
            location[1].toFloat(), 
            (location[0] + targetView.width).toFloat(),
            (location[1] + targetView.height).toFloat()
        )
        
        // Adjust for status bar if we're not in immersive mode
        val statusBarHeight = getStatusBarHeight()
        viewRect.top -= statusBarHeight
        viewRect.bottom -= statusBarHeight
        
        // Set the target in the SpotlightView
        binding.spotlight.setTarget(viewRect, padding.toFloat())
        
        // Position message card
        val screenHeight = activity.window.decorView.height
        val screenWidth = activity.window.decorView.width
        val messageCard = binding.tutorialMessageCard
        
        messageCard.post {
            // Calculate padding between spotlight and message box
            val messagePadding = 12f
            
            // Calculate center of spotlight
            val spotlightCenterX = viewRect.centerX()
            val spotlightCenterY = viewRect.centerY()
            
            // Determine message position based on where there's more space
            val spaceAbove = viewRect.top
            val spaceBelow = screenHeight - viewRect.bottom
            val spaceLeft = viewRect.left
            val spaceRight = screenWidth - viewRect.right
            
            // Find maximum space direction to position message
            val maxSpace = maxOf(spaceAbove, spaceBelow, spaceLeft, spaceRight)
            
            when {
                // Position above spotlight
                maxSpace == spaceAbove && spaceAbove > messageCard.height -> {
                    messageCard.y = viewRect.top - messageCard.height - messagePadding
                    messageCard.x = spotlightCenterX - messageCard.width / 2
                }
                
                // Position below spotlight
                maxSpace == spaceBelow && spaceBelow > messageCard.height -> {
                    messageCard.y = viewRect.bottom + messagePadding
                    messageCard.x = spotlightCenterX - messageCard.width / 2
                }
                
                // Position to the left of spotlight
                maxSpace == spaceLeft && spaceLeft > messageCard.width -> {
                    messageCard.x = viewRect.left - messageCard.width - messagePadding
                    messageCard.y = spotlightCenterY - messageCard.height / 2
                }
                
                // Position to the right of spotlight
                maxSpace == spaceRight && spaceRight > messageCard.width -> {
                    messageCard.x = viewRect.right + messagePadding
                    messageCard.y = spotlightCenterY - messageCard.height / 2
                }
                
                // Default: position it where it fits best
                else -> {
                    // Try to position it below or above with close proximity
                    if (spaceBelow >= messageCard.height / 2) {
                        messageCard.y = viewRect.bottom + messagePadding
                    } else {
                        messageCard.y = viewRect.top - messageCard.height - messagePadding
                    }
                    
                    // Center horizontally relative to spotlight
                    messageCard.x = spotlightCenterX - messageCard.width / 2
                }
            }
            
            // Make sure the message is fully visible on screen
            // Adjust horizontal position if needed
            if (messageCard.x < 12) {
                messageCard.x = 12f
            } else if (messageCard.x + messageCard.width > screenWidth - 12) {
                messageCard.x = (screenWidth - messageCard.width - 12).toFloat()
            }
            
            // Adjust vertical position if needed
            if (messageCard.y < statusBarHeight + 12) {
                messageCard.y = statusBarHeight + 12f
            } else if (messageCard.y + messageCard.height > screenHeight - 12) {
                messageCard.y = (screenHeight - messageCard.height - 12).toFloat()
            }
            
            // Start animations
            animateMessage()
        }
    }
    
    private fun getStatusBarHeight(): Int {
        val resourceId = activity.resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) activity.resources.getDimensionPixelSize(resourceId) else 0
    }
    
    private fun animateMessage() {
        // Reset animations
        binding.tutorialMessageCard.clearAnimation()
        
        // Get the spotlight view and its current rectangle
        val spotlightView = binding.spotlight
        val spotlightRect = spotlightView.tag as? RectF
        
        if (spotlightRect == null || spotlightRect.isEmpty) {
            // Fallback to simple animation if we can't get the spotlight rect
            val messageFadeIn = ObjectAnimator.ofFloat(binding.tutorialMessageCard, "alpha", 0f, 1f)
            messageFadeIn.duration = 300
            messageFadeIn.start()
            return
        }
        
        // Calculate animation starting point
        val spotlightCenterX = spotlightRect.centerX()
        val spotlightCenterY = spotlightRect.centerY()
        val messageCardCenterX = binding.tutorialMessageCard.x + binding.tutorialMessageCard.width / 2
        val messageCardCenterY = binding.tutorialMessageCard.y + binding.tutorialMessageCard.height / 2
        
        // Calculate direction vector from spotlight to message
        val directionX = messageCardCenterX - spotlightCenterX
        val directionY = messageCardCenterY - spotlightCenterY
        val distance = Math.sqrt(directionX * directionX + directionY * directionY.toDouble()).toFloat()
        
        // Normalize direction vector and scale it for animation start position
        val animDistance = 30f
        val startTranslationX = if (distance > 0) -directionX / distance * animDistance else 0f
        val startTranslationY = if (distance > 0) -directionY / distance * animDistance else 0f
        
        // Prepare animations for fade in and translation
        val messageFadeIn = ObjectAnimator.ofFloat(binding.tutorialMessageCard, "alpha", 0f, 1f)
        val messageTranslateX = ObjectAnimator.ofFloat(binding.tutorialMessageCard, "translationX", startTranslationX, 0f)
        val messageTranslateY = ObjectAnimator.ofFloat(binding.tutorialMessageCard, "translationY", startTranslationY, 0f)
        
        // Play animations
        AnimatorSet().apply {
            playTogether(messageFadeIn, messageTranslateX, messageTranslateY)
            duration = 350
            interpolator = OvershootInterpolator(0.7f)
            start()
        }
    }
    
    private fun showNextStep() {
        // Fade out current step
        ObjectAnimator.ofFloat(binding.tutorialMessageCard, "alpha", 1f, 0f).apply {
            duration = 200
            doOnEnd {
                // Show next step after fade out
                showStep(currentStep + 1)
            }
            start()
        }
    }
    
    private fun endTutorial() {
        // Fade out everything
        AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(binding.spotlight, "alpha", 1f, 0f),
                ObjectAnimator.ofFloat(binding.tutorialMessageCard, "alpha", 1f, 0f)
            )
            duration = 300
            doOnEnd {
                // Remove overlay and call completion callback
                val parent = binding.root.parent as? ViewGroup
                parent?.removeView(binding.root)
                
                // Remove screen on flag
                activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                
                // Call completion callback
                onTutorialComplete()
            }
            start()
        }
    }

    companion object {
        private const val PREFS_NAME = "tutorial_prefs"
        private const val KEY_TUTORIAL_SHOWN = "tutorial_shown"

        fun shouldShowTutorial(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return !prefs.getBoolean(KEY_TUTORIAL_SHOWN, false)
        }

        fun markTutorialAsShown(context: Context) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_TUTORIAL_SHOWN, true).apply()
        }
    }
} 
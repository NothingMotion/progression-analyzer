package com.nothingmotion.brawlprogressionanalyzer.ui.components

import android.animation.ValueAnimator
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.nothingmotion.brawlprogressionanalyzer.R

/**
 * A custom accordion view component that can expand and collapse its content.
 * This component is used in the account detail and future progresses fragments.
 * Supports customization of card appearance through XML attributes.
 */
class AccordionView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CardView(context, attrs, defStyleAttr) {

    private var headerTitle: String = ""
    private var isExpanded: Boolean = false
    private var animationDuration: Long = 300
    
    // Default values for customizable properties
    private var defaultCardElevation = 2f
    private var defaultCardCornerRadius = 8f
    private var defaultMarginHorizontal = 8
    private var defaultMarginVertical = 4
    private var defaultPadding = 16

    private lateinit var headerLayout: ConstraintLayout
    private lateinit var titleTextView: TextView
    private lateinit var arrowImageView: ImageView
    private lateinit var contentLayout: LinearLayout
    private lateinit var contentContainer: LinearLayout
    
    // Root view for the accordion content
    private lateinit var rootView: LinearLayout
    
    // Store child views that are added in XML
    private val pendingChildViews = ArrayList<View>()
    
    init {
        try {
            // Create a CardView manually
            this.cardElevation = context.resources.displayMetrics.density * defaultCardElevation
            this.radius = context.resources.displayMetrics.density * defaultCardCornerRadius

            // Inflate the content into the CardView
            val inflater = LayoutInflater.from(context)
            val contentView = inflater.inflate(R.layout.view_accordion, this, false)

            // Add the content view to this CardView
            addView(contentView)

            // Get references to views from the inflated layout
            rootView = getChildAt(0) as LinearLayout
            headerLayout = rootView.findViewById(R.id.accordion_header)
            titleTextView = rootView.findViewById(R.id.accordion_title)
            arrowImageView = rootView.findViewById(R.id.accordion_arrow)
            contentLayout = rootView.findViewById(R.id.accordion_content)
            contentContainer = rootView.findViewById(R.id.accordion_content_container)

            // Process attributes
            setupAttributes(attrs)
        } catch (e: Exception) {
            Log.e("AccordionView", "Error initializing AccordionView", e)
        }
    }
    
    private fun setupAttributes(attrs: AttributeSet?) {
        if (attrs != null) {
            val typedArray = context.obtainStyledAttributes(attrs, R.styleable.AccordionView)
            try {
                // Basic accordion properties
                headerTitle = typedArray.getString(R.styleable.AccordionView_title) ?: ""
                isExpanded = typedArray.getBoolean(R.styleable.AccordionView_expanded, false)
                animationDuration = typedArray.getInteger(R.styleable.AccordionView_animationDuration, 300).toLong()
                
                // Card appearance properties
                val cardElevation = typedArray.getDimension(
                    R.styleable.AccordionView_accordionCardElevation, 
                    context.resources.displayMetrics.density * defaultCardElevation
                )
                val cardCornerRadius = typedArray.getDimension(
                    R.styleable.AccordionView_accordionCardCornerRadius,
                    context.resources.displayMetrics.density * defaultCardCornerRadius
                )
                val marginHorizontal = typedArray.getDimensionPixelSize(
                    R.styleable.AccordionView_accordionMarginHorizontal,
                    (context.resources.displayMetrics.density * defaultMarginHorizontal).toInt()
                )
                val marginVertical = typedArray.getDimensionPixelSize(
                    R.styleable.AccordionView_accordionMarginVertical,
                    (context.resources.displayMetrics.density * defaultMarginVertical).toInt()
                )
                val padding = typedArray.getDimensionPixelSize(
                    R.styleable.AccordionView_accordionPadding,
                    (context.resources.displayMetrics.density * defaultPadding).toInt()
                )
                
                // Background colors
                val headerBackground = typedArray.getResourceId(
                    R.styleable.AccordionView_headerBackground,
                    0
                )
                val contentBackground = typedArray.getResourceId(
                    R.styleable.AccordionView_contentBackground,
                    0
                )
                // Text colors
                val headerTextColor = typedArray.getColor(
                    R.styleable.AccordionView_titleTextColor,
                    0
                )

                val headerTextAppearance = typedArray.getResourceId(
                    R.styleable.AccordionView_titleTextAppearance,
                    0
                )

                // Apply card properties
                this.cardElevation = cardElevation
                this.radius = cardCornerRadius

                // Apply header padding
                headerLayout.setPadding(padding,padding,padding,padding)


                // Apply layout properties - only if we have layout params already
                if (this.layoutParams is ViewGroup.MarginLayoutParams) {
                    val layoutParams = this.layoutParams as ViewGroup.MarginLayoutParams
                    layoutParams.setMargins(marginHorizontal, marginVertical, marginHorizontal, marginVertical)

                    this.layoutParams = layoutParams
                } else {
                    // Save these values for when layout params are set
                    post {
                        if (this.layoutParams is ViewGroup.MarginLayoutParams) {
                            val layoutParams = this.layoutParams as ViewGroup.MarginLayoutParams
                            layoutParams.setMargins(marginHorizontal, marginVertical, marginHorizontal, marginVertical)
                            this.layoutParams = layoutParams
                        }
                    }
                }
                
                // Apply colors
                if (headerBackground != 0) {
                    headerLayout.setBackgroundResource(headerBackground)

                }
                if (headerTextAppearance != 0) {
                    setTitleTextAppearance(headerTextAppearance)
                }

                if(headerTextColor != 0) {
                    titleTextView.setTextColor(headerTextColor)
                }
                if (contentBackground != 0) {
                    contentContainer.setBackgroundResource(contentBackground)
                }

                // Set title
                titleTextView.text = headerTitle
            } finally {
                typedArray.recycle()
            }
        }
        
        // Set initial state
        updateArrowRotation(isExpanded)
        contentLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE
        
        // Set click listener for accordion header
        headerLayout.setOnClickListener {
            toggleExpand()
        }
        
        // Add any pending child views to the content container
        if (pendingChildViews.isNotEmpty()) {
            for (child in pendingChildViews) {
                contentContainer.addView(child)
            }
            pendingChildViews.clear()
        }
    }
    
    /**
     * Set header padding programmatically in dp
     */
    fun setHeaderPadding(paddingDp: Int) {
        val density = context.resources.displayMetrics.density
        val paddingPx = (paddingDp * density).toInt()
        headerLayout.setPadding(paddingPx, paddingPx, paddingPx, paddingPx)
    }
    
    /**
     * Set header padding with different values programmatically in dp
     */
    fun setHeaderPadding(leftDp: Int, topDp: Int, rightDp: Int, bottomDp: Int) {
        val density = context.resources.displayMetrics.density
        val leftPx = (leftDp * density).toInt()
        val topPx = (topDp * density).toInt()
        val rightPx = (rightDp * density).toInt()
        val bottomPx = (bottomDp * density).toInt()
        headerLayout.setPadding(leftPx, topPx, rightPx, bottomPx)
    }
    
    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        // Only redirect views to the content container if it's been initialized
        // and if the view is not our own content view
        if (::contentContainer.isInitialized && child != rootView) {
            // If the accordion is already set up, add the view to the content container
            contentContainer.addView(child, params)
        } else if (!::rootView.isInitialized && child != null) {
            // If the accordion is not set up yet (during XML inflation), store the view for later
            // But don't store our own root view
            if (child !is LinearLayout || 
                child.findViewById<ConstraintLayout>(R.id.accordion_header) == null) {
                pendingChildViews.add(child)
            } else {
                super.addView(child, index, params)
            }
        } else {
            // Let CardView handle adding its own views
            super.addView(child, index, params)
        }
    }
    
    /**
     * Set card elevation programmatically
     */
    fun setAccordionElevation(elevationDp: Float) {
        val density = context.resources.displayMetrics.density
        cardElevation = elevationDp * density
    }
    
    /**
     * Set card corner radius programmatically
     */
    fun setAccordionCornerRadius(radiusDp: Float) {
        val density = context.resources.displayMetrics.density
        radius = radiusDp * density
    }
    
    /**
     * Set margins programmatically
     */
    fun setAccordionMargins(horizontalDp: Int, verticalDp: Int) {
        val density = context.resources.displayMetrics.density
        if (this.layoutParams is ViewGroup.MarginLayoutParams) {
            val layoutParams = this.layoutParams as ViewGroup.MarginLayoutParams
            val horizontalPx = (horizontalDp * density).toInt()
            val verticalPx = (verticalDp * density).toInt()
            layoutParams.setMargins(horizontalPx, verticalPx, horizontalPx, verticalPx)
            this.layoutParams = layoutParams
        } else {
            // Wait for layout params to be set
            post {
                if (this.layoutParams is ViewGroup.MarginLayoutParams) {
                    val layoutParams = this.layoutParams as ViewGroup.MarginLayoutParams
                    val horizontalPx = (horizontalDp * density).toInt()
                    val verticalPx = (verticalDp * density).toInt()
                    layoutParams.setMargins(horizontalPx, verticalPx, horizontalPx, verticalPx)
                    this.layoutParams = layoutParams
                }
            }
        }
    }
    
    /**
     * Toggle the expanded state of the accordion.
     */
    fun toggleExpand() {
        isExpanded = !isExpanded
        
        // Animate the content
        if (isExpanded) {
            expandContent()
        } else {
            collapseContent()
        }
        
        // Rotate the arrow
        updateArrowRotation(isExpanded)
    }
    
    /**
     * Expand the accordion content with animation.
     */
    private fun expandContent() {
        // Set visibility to VISIBLE before measuring
        contentLayout.visibility = View.VISIBLE
        
        // Set height to 0 initially
        contentLayout.measure(
            MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        )
        val targetHeight = contentLayout.measuredHeight
        contentLayout.layoutParams.height = 0
        
        val animator = ValueAnimator.ofInt(0, targetHeight)
        animator.duration = animationDuration
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Int
            contentLayout.layoutParams.height = value
            contentLayout.requestLayout()
            
            // When animation completes, set height to WRAP_CONTENT
            if (value == targetHeight) {
                contentLayout.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            }
        }
        animator.start()
    }
    
    /**
     * Collapse the accordion content with animation.
     */
    private fun collapseContent() {
        val initialHeight = contentLayout.height
        
        // If height is WRAP_CONTENT, measure it first
        if (initialHeight == ViewGroup.LayoutParams.WRAP_CONTENT) {
            contentLayout.measure(
                MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
            )
            contentLayout.layoutParams.height = contentLayout.measuredHeight
        }
        
        val animator = ValueAnimator.ofInt(contentLayout.height, 0)
        animator.duration = animationDuration
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Int
            contentLayout.layoutParams.height = value
            contentLayout.requestLayout()
            
            // When animation is complete, set visibility to GONE
            if (value == 0) {
                contentLayout.visibility = View.GONE
            }
        }
        animator.start()
    }
    
    /**
     * Update the rotation of the arrow based on the expanded state.
     */
    private fun updateArrowRotation(expanded: Boolean) {
        val rotation = if (expanded) 180f else 0f
        arrowImageView.animate()
            .rotation(rotation)
            .setDuration(animationDuration)
            .start()
    }
    
    /**
     * Set the title of the accordion.
     */
    fun setTitle(title: String) {
        headerTitle = title
        titleTextView.text = title
    }
    
    /**
     * Set the text appearance of the title.
     * @param resId The resource ID of the text appearance style
     */
    fun setTitleTextAppearance(resId: Int) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            titleTextView.setTextAppearance(resId)
        }
        titleTextView.setTextAppearance(context, resId)
    }
    
    /**
     * Set the text color of the title.
     * @param color The color to set
     */
    fun setTitleTextColor(color: Int) {
        titleTextView.setTextColor(color)
    }
    
    /**
     * Add a view to the content of the accordion.
     */
    fun addContent(view: View) {
        contentContainer.addView(view)
    }
    
    /**
     * Remove all views from the content of the accordion.
     */
    fun clearContent() {
        contentContainer.removeAllViews()
    }
    
    /**
     * Set the expanded state of the accordion.
     */
    fun setExpanded(expanded: Boolean) {
        if (isExpanded != expanded) {
            toggleExpand()
        }
    }
    
    /**
     * Check if the accordion is expanded.
     */
    fun isExpanded(): Boolean {
        return isExpanded
    }
    
    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val savedState = Bundle()
        savedState.putParcelable("superState", superState)
        savedState.putBoolean("isExpanded", isExpanded)
        return savedState
    }
    
    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            isExpanded = state.getBoolean("isExpanded")
            updateArrowRotation(isExpanded)
            contentLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE
            super.onRestoreInstanceState(state.getParcelable("superState"))
        } else {
            super.onRestoreInstanceState(state)
        }
    }
} 
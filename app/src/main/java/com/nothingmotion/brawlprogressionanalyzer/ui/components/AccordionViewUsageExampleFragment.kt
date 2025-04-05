package com.nothingmotion.brawlprogressionanalyzer.ui.components

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.nothingmotion.brawlprogressionanalyzer.R
import com.nothingmotion.brawlprogressionanalyzer.databinding.FragmentExampleBinding
import timber.log.Timber

/**
 * This is an example fragment to demonstrate how to use the AccordionView.
 * You can add this to your navigation graph for testing, then remove it.
 */
class AccordionViewUsageExampleFragment : Fragment() {

    private var _binding: FragmentExampleBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        try {
            _binding = FragmentExampleBinding.inflate(inflater, container, false)
            return binding.root
        } catch (e: Exception) {
            Log.e("AccordionViewExample", "Error inflating example fragment", e)
            Timber.e(e, "Error inflating example fragment")
            // Fallback to a simple view if binding fails
            val view = TextView(requireContext())
            view.text = "Error inflating AccordionView example"
            return view
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            // Only run if binding was successful
            if (_binding != null) {
                // The default, elevated, and flat accordions are configured in XML
                
                // Example of programmatically setting up and customizing an accordion
                setupProgrammaticAccordion()
            }
        } catch (e: Exception) {
            Timber.e(e, "Error in onViewCreated")
        }
    }
    
    private fun setupProgrammaticAccordion() {
        try {
            // Create a TextView to add as content
            val contentTextView = TextView(requireContext()).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                text = "This accordion has custom title text appearance set programmatically."
                setPadding(16, 16, 16, 16)
            }
            
            // Set up the programmatic accordion with custom properties
            binding.programmaticAccordion.apply {
                // Set basic properties
                setTitle("Custom Text Style")
                addContent(contentTextView)
                
                // Customize appearance programmatically
                setAccordionElevation(6f) // 6dp elevation
                setAccordionCornerRadius(10f) // 10dp corner radius
                setAccordionMargins(10, 5) // 10dp horizontal, 5dp vertical margins
                setHeaderPadding(20) // 20dp header padding
                
                // Set title text appearance and color
                setTitleTextAppearance(com.google.android.material.R.style.TextAppearance_MaterialComponents_Headline6)
                setTitleTextColor(ContextCompat.getColor(context, com.google.android.material.R.color.design_default_color_primary))
                
                // You can also use your own custom style
                // setTitleTextAppearance(R.style.YourCustomTextStyle)
            }
        } catch (e: Exception) {
            Log.e("AccordionExample", "Error setting up programmatic accordion", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 
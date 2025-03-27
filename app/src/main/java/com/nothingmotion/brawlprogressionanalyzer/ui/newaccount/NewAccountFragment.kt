package com.nothingmotion.brawlprogressionanalyzer.ui.newaccount

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.nothingmotion.brawlprogressionanalyzer.R
import com.nothingmotion.brawlprogressionanalyzer.databinding.FragmentNewAccountBinding

class NewAccountFragment : Fragment() {
    private var _binding: FragmentNewAccountBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNewAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.addAccountButton.setOnClickListener {
            // TODO: Implement account creation logic
            try {
                // Use popBackStack for the most reliable back navigation
                findNavController().popBackStack()
                Log.d("NewAccountFragment", "Navigated back to accounts using popBackStack")
            } catch (e: Exception) {
                Toast.makeText(context, "Navigation error: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("NewAccountFragment", "Navigation error", e)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = NewAccountFragment()
    }
} 
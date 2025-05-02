package com.nothingmotion.brawlprogressionanalyzer.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.nothingmotion.brawlprogressionanalyzer.R
import com.nothingmotion.brawlprogressionanalyzer.databinding.FragmentWikiBinding

class WikiFragment : Fragment() {
    private var _binding : FragmentWikiBinding?=null
    private val binding get() = _binding!!


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.floatingActionButton.setOnClickListener{backButton()}

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWikiBinding.inflate(inflater,container,false)
        return binding.root
    }

    private fun backButton() {
        findNavController().popBackStack()
    }
}
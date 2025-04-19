package com.nothingmotion.brawlprogressionanalyzer.ui

import androidx.lifecycle.ViewModel
import com.nothingmotion.brawlprogressionanalyzer.domain.repository.BrawlerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BrawlerViewModel @Inject constructor(private val repository: BrawlerRepository) : ViewModel() {



}
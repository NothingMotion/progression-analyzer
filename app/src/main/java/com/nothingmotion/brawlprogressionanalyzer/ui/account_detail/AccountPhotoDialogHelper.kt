package com.nothingmotion.brawlprogressionanalyzer.ui.account_detail

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.HttpException
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nothingmotion.brawlprogressionanalyzer.R
import com.nothingmotion.brawlprogressionanalyzer.domain.model.DataError
import timber.log.Timber

object AccountPhotoDialogHelper {
    fun photoAccountDialog(context:Context,accountTag: String) {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.dialog_account_photo, null)

        val imageView = view.findViewById<ImageView>(R.id.photo_preview)
        val chipGroup = view.findViewById<ChipGroup>(R.id.photo_type_chip_group)
        val errorStateGroup = view.findViewById<View>(R.id.error_state_group)
        val loadingStateGroup = view.findViewById<View>(R.id.loading_photo_group)
        val retryButton = view.findViewById<Button>(R.id.retry_button)
        val backButton = view.findViewById<Button>(R.id.back_button)
        val backLoadingButton = view.findViewById<Button>(R.id.back_loading_button)
        val loadingText = view.findViewById<TextView>(R.id.loading_text)

        var imageType: String = "brawlers"

        // Configure loading text
        loadingText.text = context.getString(R.string.loading_account_photo)

        // Reset initial visibility states
        errorStateGroup.visibility = View.GONE
        loadingStateGroup.visibility = View.GONE

        // Create a fixed animation handler instead of mixing direct visibility changes with animations
        val showLoading = {
            // Always cancel any ongoing animations first
            errorStateGroup.animate().cancel()
            loadingStateGroup.animate().cancel()

            if (errorStateGroup.isVisible) {
                // First hide error, then show loading
                errorStateGroup.apply {
                    animate()
                        .alpha(0f)
                        .translationY(50f)
                        .setDuration(300)
                        .withEndAction {
                            visibility = View.GONE

                            // Now show loading with animation
                            loadingStateGroup.apply {
                                alpha = 0f
                                translationY = -50f
                                visibility = View.VISIBLE
                                animate()
                                    .alpha(1f)
                                    .translationY(0f)
                                    .setDuration(300)
                                    .start()
                            }
                        }
                        .start()
                }
            } else {
                // Just show loading
                loadingStateGroup.apply {
                    alpha = 0f
                    translationY = -50f
                    visibility = View.VISIBLE
                    animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(300)
                        .start()
                }
            }
        }

        val showError = {
            // Always cancel any ongoing animations first
            errorStateGroup.animate().cancel()
            loadingStateGroup.animate().cancel()

            if (loadingStateGroup.isVisible) {
                // First hide loading, then show error
                loadingStateGroup.apply {
                    animate()
                        .alpha(0f)
                        .translationY(50f)
                        .setDuration(300)
                        .withEndAction {
                            visibility = View.GONE

                            // Now show error with animation
                            errorStateGroup.apply {
                                alpha = 0f
                                translationY = -50f
                                visibility = View.VISIBLE
                                animate()
                                    .alpha(1f)
                                    .translationY(0f)
                                    .setDuration(300)
                                    .start()
                            }
                        }
                        .start()
                }
            } else {
                // Just show error
                errorStateGroup.apply {
                    alpha = 0f
                    translationY = -50f
                    visibility = View.VISIBLE
                    animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(300)
                        .start()
                }
            }
        }

        val hideStates = {
            // Always cancel any ongoing animations first
            errorStateGroup.animate().cancel()
            loadingStateGroup.animate().cancel()

            // Hide error if visible
            if (errorStateGroup.isVisible) {
                errorStateGroup.apply {
                    animate()
                        .alpha(0f)
                        .translationY(50f)
                        .setDuration(300)
                        .withEndAction {
                            visibility = View.GONE
                        }
                        .start()
                }
            }

            // Hide loading if visible
            if (loadingStateGroup.isVisible) {
                loadingStateGroup.apply {
                    animate()
                        .alpha(0f)
                        .translationY(50f)
                        .setDuration(300)
                        .withEndAction {
                            visibility = View.GONE
                        }
                        .start()
                }
            }
        }

        val fetchImage = {
            // Show loading state
            showLoading()

            val accountTag = accountTag.replace("#", "").lowercase()
            val imageUrl = GlideUrl(
                "https://img.sltbot.com/player/${accountTag}/${imageType}?o=v",
                LazyHeaders.Builder()
                    .addHeader("accept", "*/*")
                    .addHeader("accept-language", "en-US,en;q=0.9")
                    .addHeader("dnt", "1")
                    .addHeader("origin", "https://sltbot.com")
                    .addHeader("priority", "u=1, i")
                    .addHeader("referer", "https://sltbot.com/")
                    .addHeader("sec-ch-ua", "\"Chromium\";v=\"135\", \"Not-A.Brand\";v=\"8\"")
                    .addHeader("sec-ch-ua-mobile", "?0")
                    .addHeader("sec-ch-ua-platform", "\"Windows\"")
                    .addHeader("sec-fetch-dest", "empty")
                    .addHeader("sec-fetch-mode", "cors")
                    .addHeader("sec-fetch-site", "same-site")
                    .addHeader(
                        "user-agent",
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0.0.0 Safari/537.36"
                    )
                    .build()
            )

            Glide.with(imageView)
                .load(imageUrl)
                .apply(RequestOptions())
                .timeout(10000)
                .listener(object: RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>,
                        isFirstResource: Boolean
                    ): Boolean {
                        // Determine error message
                        var errorText: String? = null
                        val rootCause = e?.rootCauses?.firstOrNull()
                        if (rootCause is HttpException) {
                            Timber.tag("AccountsAdapter").e("Error loading image: ${rootCause.statusCode}")
                            val networkError = when(rootCause.statusCode) {
                                400 -> DataError.NetworkError.BAD_REQUEST
                                401 -> DataError.NetworkError.UNAUTHORIZED
                                429 -> DataError.NetworkError.TOO_MANY_REQUESTS
                                403 -> DataError.NetworkError.FORBIDDEN
                                404 -> DataError.NetworkError.NOT_FOUND
                                500 -> DataError.NetworkError.NETWORK_ERROR
                                else -> DataError.NetworkError.NETWORK_ERROR
                            }
                            errorText = networkError.name
                        }

                        // Update error message text
                        val errorTextView = errorStateGroup.findViewById<TextView>(R.id.error_message_text)
                        errorTextView.text = errorText ?: e?.localizedMessage ?: "Failed to load account data"

                        // Show error state
                        showError()

                        return true
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        // Hide both states
                        hideStates()

                        return false
                    }
                })
                .into(imageView)
        }

        // Setup chip group listener
        chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val checked = checkedIds[0]
                when (checked) {
                    R.id.chip_brawlers -> {
                        imageType = "brawlers"
                        fetchImage()
                    }
                    R.id.chip_trophies -> {
                        imageType = "trophies"
                        fetchImage()
                    }
                    R.id.chip_ranks -> {
                        imageType = "ranks"
                        fetchImage()
                    }
                    R.id.chip_masteries -> {
                        imageType = "mastery_points"
                        fetchImage()
                    }
                }
            }
        }

        // Setup retry button
        retryButton.setOnClickListener {
            fetchImage()
        }

        // Setup back buttons (both in error and loading states)
        backButton.setOnClickListener {
            errorStateGroup.apply {
                animate()
                    .alpha(0f)
                    .translationY(50f)
                    .setDuration(300)
                    .withEndAction {
                        visibility = View.GONE
                    }
                    .start()
            }
        }

        backLoadingButton.setOnClickListener {
            loadingStateGroup.apply {
                animate()
                    .alpha(0f)
                    .translationY(50f)
                    .setDuration(300)
                    .withEndAction {
                        visibility = View.GONE
                    }
                    .start()
            }
        }

        // Initial fetch
        fetchImage()

        // Show dialog
        MaterialAlertDialogBuilder(context)
            .setTitle("Take a photo")
            .setView(view)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    
}
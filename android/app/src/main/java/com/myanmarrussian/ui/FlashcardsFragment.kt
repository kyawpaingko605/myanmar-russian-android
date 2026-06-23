package com.myanmarrussian.ui

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.myanmarrussian.databinding.FragmentFlashcardsBinding
import com.myanmarrussian.models.Card
import com.myanmarrussian.models.CardData
import java.util.Locale

/**
 * FlashcardsFragment - Equivalent to iOS FlashcardsView
 * Shows flip cards with Myanmar/Russian vocabulary and TTS support
 */
class FlashcardsFragment : Fragment(), TextToSpeech.OnInitListener {

    private var _binding: FragmentFlashcardsBinding? = null
    private val binding get() = _binding!!

    private val cards: List<Card> = CardData.defaultCards
    private var currentIndex = 0
    private var isFlipped = false
    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFlashcardsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize TTS
        tts = TextToSpeech(requireContext(), this)

        // Set up flip card tap
        binding.flipCardContainer.setOnClickListener {
            flipCard()
        }

        // Previous button
        binding.btnPrevious.setOnClickListener {
            if (currentIndex > 0) {
                currentIndex--
                isFlipped = false
                updateCard()
            }
        }

        // Next button
        binding.btnNext.setOnClickListener {
            if (currentIndex < cards.size - 1) {
                currentIndex++
                isFlipped = false
                updateCard()
            }
        }

        // Listen button
        binding.btnListen.setOnClickListener {
            speakRussian()
        }

        updateCard()
    }

    private fun updateCard() {
        val card = cards[currentIndex]

        // Update counter
        binding.tvCardCounter.text = "${currentIndex + 1}/${cards.size}"

        // Show front (Myanmar) side
        binding.cardFront.visibility = View.VISIBLE
        binding.cardBack.visibility = View.GONE
        isFlipped = false

        // Update content
        binding.tvMyanmarWord.text = card.myanmar
        binding.tvRussianWord.text = card.russian
        binding.tvPronunciation.text = card.pronunciation

        // Update button states
        binding.btnPrevious.alpha = if (currentIndex == 0) 0.3f else 1.0f
        binding.btnPrevious.isEnabled = currentIndex > 0
        binding.btnNext.alpha = if (currentIndex == cards.size - 1) 0.3f else 1.0f
        binding.btnNext.isEnabled = currentIndex < cards.size - 1
    }

    private fun flipCard() {
        isFlipped = !isFlipped

        if (isFlipped) {
            // Show back (Russian) side with animation
            binding.flipCardContainer.animate()
                .rotationY(90f)
                .setDuration(150)
                .withEndAction {
                    binding.cardFront.visibility = View.GONE
                    binding.cardBack.visibility = View.VISIBLE
                    binding.flipCardContainer.rotationY = -90f
                    binding.flipCardContainer.animate()
                        .rotationY(0f)
                        .setDuration(150)
                        .start()
                }
                .start()
        } else {
            // Show front (Myanmar) side with animation
            binding.flipCardContainer.animate()
                .rotationY(90f)
                .setDuration(150)
                .withEndAction {
                    binding.cardBack.visibility = View.GONE
                    binding.cardFront.visibility = View.VISIBLE
                    binding.flipCardContainer.rotationY = -90f
                    binding.flipCardContainer.animate()
                        .rotationY(0f)
                        .setDuration(150)
                        .start()
                }
                .start()
        }
    }

    private fun speakRussian() {
        if (!isTtsReady) {
            Toast.makeText(requireContext(), "TTS မပြင်ဆင်ရသေးပါ", Toast.LENGTH_SHORT).show()
            return
        }

        val card = cards[currentIndex]
        tts?.language = Locale("ru", "RU")
        tts?.setSpeechRate(0.8f)
        tts?.speak(card.russian, TextToSpeech.QUEUE_FLUSH, null, "flashcard_tts")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        tts?.stop()
        tts?.shutdown()
        _binding = null
    }
}

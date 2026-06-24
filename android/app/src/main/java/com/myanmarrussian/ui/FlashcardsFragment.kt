package com.myanmarrussian.ui

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.myanmarrussian.AppState
import com.myanmarrussian.databinding.FragmentFlashcardsBinding
import com.myanmarrussian.api.TutorApiService
import com.myanmarrussian.api.VocabularyItem
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * FlashcardsFragment - AI-powered dynamic flashcards by learning levels
 * Shows flip cards with Myanmar/Russian vocabulary fetched from Gemini AI
 */
class FlashcardsFragment : Fragment(), TextToSpeech.OnInitListener {

    private var _binding: FragmentFlashcardsBinding? = null
    private val binding get() = _binding!!

    // AI ဆီမှ ရလာမည့် ကတ်ပြားများကို သိမ်းဆည်းရန် Array
    private val cards = mutableListOf<VocabularyItem>()
    private var currentIndex = 0
    private var isFlipped = false
    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var currentLevel = "A1"

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
            if (cards.isNotEmpty()) flipCard()
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
            if (cards.isNotEmpty()) speakRussian()
        }

        // Level Selection Buttons - ခလုတ်များ နှိပ်ပါက AI ဆီမှ အသစ်တောင်းမည်
        binding.btnLevelA1?.setOnClickListener { loadAiVocabulary("A1") }
        binding.btnLevelA2?.setOnClickListener { loadAiVocabulary("A2") }
        binding.btnLevelB1?.setOnClickListener { loadAiVocabulary("B1") }

        // စဖွင့်ချင်း Default အနေဖြင့် A1 ဒေတာကို AI ဆီမှ တောင်းယူမည်
        loadAiVocabulary("A1")
    }

    private fun loadAiVocabulary(level: String) {
        currentLevel = level
        // Loading ပြပေးခြင်း
        binding.progressBar?.visibility = View.VISIBLE
        binding.flipCardContainer.visibility = View.INVISIBLE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val api = TutorApiService.create(AppState.backendUrl)

                // 💡 ဖုန်းထဲတွင် သိမ်းဆည်းထားသော ကျောင်းသား၏ ကိုယ်ပိုင် Gemini API Key ကို လှမ်းဖတ်ခြင်း
                val sharedPref = requireContext().getSharedPreferences("AppState", Context.MODE_PRIVATE)
                val userApiKey = sharedPref.getString("USER_GEMINI_KEY", null)

                // 💡 Named arguments သုံး၍ level ရော apiKey ကိုပါ သေချာပေါက် ပူးတွဲပေးပို့ခြင်း
                val response = api.getVocabulary(level = level, apiKey = userApiKey)

                if (response.isSuccessful && response.body()?.success == true) {
                    val newCards = response.body()?.vocabulary ?: emptyList()
                    cards.clear()
                    cards.addAll(newCards)
                    currentIndex = 0
                    isFlipped = false
                    updateCard()
                    
                    binding.flipCardContainer.visibility = View.VISIBLE
                } else {
                    Toast.makeText(requireContext(), "❌ ဒေတာဆွဲယူ၍မရပါ Level: $level", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "ချိတ်ဆက်မှု အဆင်မပြေပါ: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                // Loading ပိတ်ပေးခြင်း
                binding.progressBar?.visibility = View.GONE
            }
        }
    }

    private fun updateCard() {
        if (cards.isEmpty()) {
            binding.tvCardCounter.text = "0/0"
            binding.tvMyanmarWord.text = "စာလုံးမရှိပါ"
            return
        }

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

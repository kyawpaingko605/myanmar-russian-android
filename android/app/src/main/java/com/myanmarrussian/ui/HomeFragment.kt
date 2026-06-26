package com.myanmarrussian.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.myanmarrussian.R
import com.myanmarrussian.databinding.FragmentHomeBinding

/**
 * HomeFragment - Equivalent to iOS HomeView
 * Shows stats cards and feature navigation cards
 */
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Navigate to Flashcards when card is tapped
        binding.cardFlashcards.setOnClickListener {
            findNavController().navigate(R.id.nav_flashcards)
        }

        // Navigate to Quiz when card is tapped
        binding.cardQuiz.setOnClickListener {
            findNavController().navigate(R.id.nav_quiz)
        }

        // Navigate to Pro Tutor when card is tapped
        binding.cardTutor.setOnClickListener {
            findNavController().navigate(R.id.nav_tutor)
        }

        // 💬 နှိပ်လိုက်ပါက လူအချင်းချင်း စကားပြောမည့် Group Chat သို့ သွားရန် ချိတ်ဆက်ခြင်း
        // ⚠️ မှတ်ချက် - မိမိ fragment_home.xml ထဲရှိ Group Chat ခလုတ်၏ ID အတိုင်း မှန်ကန်စွာ စစ်ဆေးပါ
        binding.cardGroupChat.setOnClickListener {
            findNavController().navigate(R.id.navigation_group_chat)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.myanmarrussian.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.myanmarrussian.R
import com.myanmarrussian.adapters.GroupChatActivity
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

        // 💬 ⚡ Crash လုံးဝမဖြစ်စေရန် တိုက်ရိုက် Intent စနစ်ဖြင့် GroupChatActivity ကို ခေါ်ယူခြင်း
        binding.cardGroupChat.setOnClickListener {
            val intent = Intent(requireContext(), GroupChatActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

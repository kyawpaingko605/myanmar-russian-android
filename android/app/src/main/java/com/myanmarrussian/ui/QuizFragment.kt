package com.myanmarrussian.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.myanmarrussian.R
import com.myanmarrussian.databinding.FragmentQuizBinding
import com.myanmarrussian.models.QuizData
import com.myanmarrussian.models.QuizQuestion

/**
 * QuizFragment - Equivalent to iOS QuizView
 * Multiple choice quiz with answer feedback
 */
class QuizFragment : Fragment() {

    private var _binding: FragmentQuizBinding? = null
    private val binding get() = _binding!!

    private val questions: List<QuizQuestion> = QuizData.defaultQuestions
    private var currentIndex = 0
    private var score = 0
    private var selectedAnswer: Int? = null
    private var showResult = false

    private lateinit var optionButtons: List<Button>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        optionButtons = listOf(
            binding.btnOption0,
            binding.btnOption1,
            binding.btnOption2,
            binding.btnOption3
        )

        // Set up option button click listeners
        optionButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                if (!showResult) {
                    selectAnswer(index)
                }
            }
        }

        // Next button
        binding.btnNext.setOnClickListener {
            nextQuestion()
        }

        updateQuestion()
    }

    private fun updateQuestion() {
        val question = questions[currentIndex]

        // Update counter
        binding.tvQuestionCounter.text = "${currentIndex + 1}/${questions.size}"

        // Update progress bar
        val progress = (currentIndex.toFloat() / questions.size * 100).toInt()
        binding.progressBar.progress = progress

        // Update question text
        binding.tvQuestion.text = question.question

        // Update option buttons
        optionButtons.forEachIndexed { index, button ->
            if (index < question.options.size) {
                button.text = question.options[index]
                button.visibility = View.VISIBLE
                button.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_option_normal)
                button.isEnabled = true
            } else {
                button.visibility = View.GONE
            }
        }

        // Reset state
        selectedAnswer = null
        showResult = false
        binding.btnNext.visibility = View.GONE
        binding.scoreCard.visibility = View.GONE
    }

    private fun selectAnswer(index: Int) {
        val question = questions[currentIndex]
        selectedAnswer = index
        showResult = true

        // Update button backgrounds based on answer
        optionButtons.forEachIndexed { i, button ->
            when {
                i == question.correctAnswer -> {
                    button.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_option_correct)
                }
                i == index && index != question.correctAnswer -> {
                    button.background = ContextCompat.getDrawable(requireContext(), R.drawable.bg_option_wrong)
                }
            }
            button.isEnabled = false
        }

        // Update score
        if (index == question.correctAnswer) {
            score++
        }

        // Show next button
        val isLastQuestion = currentIndex == questions.size - 1
        binding.btnNext.text = if (isLastQuestion) getString(R.string.finish_quiz) else getString(R.string.next_question)
        binding.btnNext.visibility = View.VISIBLE

        // Show score card on last question
        if (isLastQuestion) {
            binding.tvScore.text = "ရမှတ်: $score/${questions.size}"
            binding.scoreCard.visibility = View.VISIBLE
        }
    }

    private fun nextQuestion() {
        if (currentIndex < questions.size - 1) {
            currentIndex++
            updateQuestion()
        } else {
            // Reset quiz
            currentIndex = 0
            score = 0
            updateQuestion()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

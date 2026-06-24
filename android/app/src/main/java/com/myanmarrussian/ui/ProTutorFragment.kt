package com.myanmarrussian.ui

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.myanmarrussian.AppState
import com.myanmarrussian.R
import com.myanmarrussian.adapters.ChatMessageAdapter
import com.myanmarrussian.api.HistoryItem
import com.myanmarrussian.api.TutorApiService
import com.myanmarrussian.api.TutorRequest
import com.myanmarrussian.databinding.DialogSettingsBinding
import com.myanmarrussian.databinding.FragmentProTutorBinding
import com.myanmarrussian.models.ChatMessage
import com.myanmarrussian.models.LangMode
import com.myanmarrussian.models.TutorMode
import kotlinx.coroutines.launch

/**
 * ProTutorFragment - Equivalent to iOS ProTutorView
 * AI-powered chat tutor with language mode, tutor mode selection and user Gemini API key integration
 */
class ProTutorFragment : Fragment() {

    private var _binding: FragmentProTutorBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ChatMessageAdapter
    private val messages = mutableListOf<ChatMessage>()

    private var currentLangMode = LangMode.MYANMAR
    private var currentTutorMode = TutorMode.CONVERSATION
    private var isLoading = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProTutorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up RecyclerView (အသံထွက်လုပ်ဆောင်ချက်ကို ChatMessageAdapter တွင်းသို့ တိုက်ရိုက်လွှဲပြောင်းပေးအပ်ထားပါသည်)
        adapter = ChatMessageAdapter(messages)
        binding.rvMessages.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        binding.rvMessages.adapter = adapter

        // Show empty state initially
        updateEmptyState()

        // Language mode buttons
        binding.btnLangMyanmar.setOnClickListener {
            setLangMode(LangMode.MYANMAR)
        }
        binding.btnLangRussian.setOnClickListener {
            setLangMode(LangMode.RUSSIAN)
        }

        // Tutor mode buttons
        binding.btnModeConversation.setOnClickListener {
            setTutorMode(TutorMode.CONVERSATION)
        }
        binding.btnModePronunciation.setOnClickListener {
            setTutorMode(TutorMode.PRONUNCIATION)
        }
        binding.btnModeGrammar.setOnClickListener {
            setTutorMode(TutorMode.GRAMMAR)
        }
        binding.btnModeVocabulary.setOnClickListener {
            setTutorMode(TutorMode.VOCABULARY)
        }

        // Settings button
        binding.btnSettings.setOnClickListener {
            showSettingsDialog()
        }

        // Send button
        binding.btnSend.setOnClickListener {
            sendMessage()
        }

        // Update input hint
        updateInputHint()
    }

    private fun setLangMode(mode: LangMode) {
        currentLangMode = mode
        updateLangModeButtons()
        updateInputHint()
    }

    private fun setTutorMode(mode: TutorMode) {
        currentTutorMode = mode
        updateTutorModeButtons()
    }

    private fun updateLangModeButtons() {
        val selectedDrawable = R.drawable.bg_mode_selected
        val unselectedDrawable = R.drawable.bg_mode_unselected
        val selectedColor = requireContext().getColor(R.color.text_white)
        val unselectedColor = requireContext().getColor(R.color.text_primary)

        if (currentLangMode == LangMode.MYANMAR) {
            binding.btnLangMyanmar.setBackgroundResource(selectedDrawable)
            binding.btnLangMyanmar.setTextColor(selectedColor)
            binding.btnLangRussian.setBackgroundResource(unselectedDrawable)
            binding.btnLangRussian.setTextColor(unselectedColor)
        } else {
            binding.btnLangRussian.setBackgroundResource(selectedDrawable)
            binding.btnLangRussian.setTextColor(selectedColor)
            binding.btnLangMyanmar.setBackgroundResource(unselectedDrawable)
            binding.btnLangMyanmar.setTextColor(unselectedColor)
        }
    }

    private fun updateTutorModeButtons() {
        val selectedDrawable = R.drawable.bg_tutor_mode_selected
        val unselectedDrawable = R.drawable.bg_mode_unselected
        val selectedColor = requireContext().getColor(R.color.text_white)
        val unselectedColor = requireContext().getColor(R.color.text_primary)

        val modeButtons = mapOf(
            TutorMode.CONVERSATION to binding.btnModeConversation,
            TutorMode.PRONUNCIATION to binding.btnModePronunciation,
            TutorMode.GRAMMAR to binding.btnModeGrammar,
            TutorMode.VOCABULARY to binding.btnModeVocabulary
        )

        modeButtons.forEach { (mode, button) ->
            if (mode == currentTutorMode) {
                button.setBackgroundResource(selectedDrawable)
                button.setTextColor(selectedColor)
            } else {
                button.setBackgroundResource(unselectedDrawable)
                button.setTextColor(unselectedColor)
            }
        }
    }

    private fun updateInputHint() {
        binding.etMessageInput.hint = if (currentLangMode == LangMode.MYANMAR) {
            getString(R.string.input_hint_myanmar)
        } else {
            getString(R.string.input_hint_russian)
        }
    }

    private fun updateEmptyState() {
        if (messages.isEmpty()) {
            binding.rvMessages.visibility = View.GONE
            binding.emptyState.visibility = View.VISIBLE
        } else {
            binding.rvMessages.visibility = View.VISIBLE
            binding.emptyState.visibility = View.GONE
        }
    }

    private fun sendMessage() {
        val text = binding.etMessageInput.text.toString().trim()
        if (text.isEmpty() || isLoading) return

        // 💡 ဖုန်းထဲတွင် သိမ်းဆည်းထားသော ကျောင်းသား၏ ကိုယ်ပိုင် Gemini API Key ကို လှမ်းဖတ်ခြင်း
        val sharedPref = requireContext().getSharedPreferences("AppState", Context.MODE_PRIVATE)
        val userApiKey = sharedPref.getString("USER_GEMINI_KEY", null)

        // Add user message
        val userMessage = ChatMessage(role = ChatMessage.MessageRole.USER, text = text)
        messages.add(userMessage)
        adapter.notifyItemInserted(messages.size - 1)
        binding.etMessageInput.text.clear()
        updateEmptyState()
        scrollToBottom()

        isLoading = true
        binding.btnSend.isEnabled = false

        // Call backend API
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val api = TutorApiService.create(AppState.backendUrl)

                val history = messages.dropLast(1).map { msg ->
                    HistoryItem(
                        role = msg.role.value,
                        text = msg.text
                    )
                }

                val request = TutorRequest(
                    message = text,
                    mode = currentTutorMode.value,
                    langMode = currentLangMode.value,
                    history = history
                )

                // 💡 Interface ထဲက parameter အစီအစဉ်အတိုင်း တိကျမှန်ကန်စွာ ပေးပို့ခြင်း
                val response = api.sendMessage(request = request, apiKey = userApiKey)

                if (response.isSuccessful) {
                    val responseText = response.body()?.response ?: "❌ Empty response"
                    val assistantMessage = ChatMessage(
                        role = ChatMessage.MessageRole.ASSISTANT,
                        text = responseText
                    )
                    messages.add(assistantMessage)
                    adapter.notifyItemInserted(messages.size - 1)
                    scrollToBottom()
                } else {
                    val errorMsg = ChatMessage(
                        role = ChatMessage.MessageRole.ASSISTANT,
                        text = "❌ Error: ${response.code()} - ${response.message()}"
                    )
                    messages.add(errorMsg)
                    adapter.notifyItemInserted(messages.size - 1)
                    scrollToBottom()
                }
            } catch (e: Exception) {
                val errorMsg = ChatMessage(
                    role = ChatMessage.MessageRole.ASSISTANT,
                    text = "❌ Error: ${e.message ?: "Unknown error"}\n\nBackend URL စစ်ဆေးပါ: ${AppState.backendUrl}"
                )
                messages.add(errorMsg)
                adapter.notifyItemInserted(messages.size - 1)
                scrollToBottom()
            } finally {
                isLoading = false
                binding.btnSend.isEnabled = true
            }
        }
    }

    private fun scrollToBottom() {
        if (messages.isNotEmpty()) {
            binding.rvMessages.smoothScrollToPosition(messages.size - 1)
        }
    }

    private fun showSettingsDialog() {
        val dialogBinding = DialogSettingsBinding.inflate(layoutInflater)
        dialogBinding.etBackendUrl.setText(AppState.backendUrl)

        val sharedPref = requireContext().getSharedPreferences("AppState", Context.MODE_PRIVATE)
        val savedKey = sharedPref.getString("USER_GEMINI_KEY", "")
        
        // 💡 R.id.et_gemini_key ကြောင့် Unresolved reference မဖြစ်အောင် Dynamic ID ဖြင့် ရှာဖွေခြင်း
        val mainView = dialogBinding.root
        val resId = resources.getIdentifier("et_gemini_key", "id", requireContext().packageName)
        if (resId != 0) {
            val inputView = mainView.findViewById<View>(resId)
            if (inputView is EditText) {
                inputView.setText(savedKey)
            }
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(mainView)
            .create()

        dialogBinding.btnDone.setOnClickListener {
            val url = dialogBinding.etBackendUrl.text.toString().trim()
            if (url.isNotEmpty()) {
                AppState.backendUrl = url
            }

            // 💡 SharedPreferences ထဲသို့ Key သိမ်းဆည်းခြင်း
            if (resId != 0) {
                val inputView = mainView.findViewById<View>(resId)
                if (inputView is EditText) {
                    val inputKey = inputView.text.toString().trim()
                    sharedPref.edit().putString("USER_GEMINI_KEY", if (inputKey.isEmpty()) null else inputKey).apply()
                }
            }

            Toast.makeText(requireContext(), "သတ်မှတ်ချက်များ သိမ်းဆည်းပြီး", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter.onDestroy() // Adapter အတွင်းရှိ TTS Resource ကို အလိုအလျောက် ပိတ်သိမ်းခြင်း
        _binding = null
    }
}

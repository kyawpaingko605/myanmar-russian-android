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
import com.myanmarrussian.database.AppDatabase
import com.myanmarrussian.database.ChatMessageEntity
import com.myanmarrussian.databinding.DialogSettingsBinding
import com.myanmarrussian.databinding.FragmentProTutorBinding
import com.myanmarrussian.models.ChatMessage
import com.myanmarrussian.models.LangMode
import com.myanmarrussian.models.TutorMode
import kotlinx.coroutines.launch

/**
 * ProTutorFragment - Equivalent to iOS ProTutorView
 * AI-powered chat tutor with language mode, tutor mode selection, user Gemini API key integration
 * and local persistent storage via Room Database.
 */
class ProTutorFragment : Fragment() {

    private var _binding: FragmentProTutorBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: ChatMessageAdapter
    private val messages = mutableListOf<ChatMessage>()

    private var currentLangMode = LangMode.MYANMAR
    private var currentTutorMode = TutorMode.CONVERSATION
    private var isLoading = false
    
    // 💡 Database Object အား ကြိုတင်ကြေညာထားခြင်း
    private lateinit var database: AppDatabase

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

        // Initialize Database
        database = AppDatabase.getDatabase(requireContext())

        // Set up RecyclerView
        adapter = ChatMessageAdapter(messages)
        binding.rvMessages.layoutManager = LinearLayoutManager(requireContext()).apply {
            stackFromEnd = true
        }
        binding.rvMessages.adapter = adapter

        // 💡 မျက်နှာပြင်စဖွင့်လျှင် Room Database ထဲမှ စကားပြောမှတ်တမ်းဟောင်းများကို ဆွဲထုတ်ပြသခြင်း
        loadChatHistoryFromDatabase()

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

    private fun loadChatHistoryFromDatabase() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val savedList = database.chatDao().getAllMessages()
                messages.clear()
                savedList.forEach { entity ->
                    val role = if (entity.role == "user") ChatMessage.MessageRole.USER else ChatMessage.MessageRole.ASSISTANT
                    messages.add(ChatMessage(role = role, text = entity.text))
                }
                adapter.notifyDataSetChanged()
                updateEmptyState()
                scrollToBottom()
            } catch (e: Exception) {
                Log.e("Database", "Error loading messages: ${e.message}")
            }
        }
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

        // 💡 SharedPreferences ထဲမှ ကျောင်းသား၏ API Key လှမ်းဖတ်ခြင်း
        val sharedPref = requireContext().getSharedPreferences("AppState", Context.MODE_PRIVATE)
        val userApiKey = sharedPref.getString("USER_GEMINI_KEY", null)

        // Add user message to UI List
        val userMessage = ChatMessage(role = ChatMessage.MessageRole.USER, text = text)
        messages.add(userMessage)
        adapter.notifyItemInserted(messages.size - 1)
        binding.etMessageInput.text.clear()
        updateEmptyState()
        scrollToBottom()

        isLoading = true
        binding.btnSend.isEnabled = false

        // Call backend API & Handle Database Room Storage
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 💡 User ပို့လိုက်သောစာအား Room Database ထဲသို့ ထည့်သွင်းသိမ်းဆည်းခြင်း
                database.chatDao().insertMessage(ChatMessageEntity(role = "user", text = text))

                val api = TutorApiService.create(AppState.backendUrl)
                val history = messages.dropLast(1).map { msg ->
                    HistoryItem(role = msg.role.value, text = msg.text)
                }

                val request = TutorRequest(
                    message = text,
                    mode = currentTutorMode.value,
                    langMode = currentLangMode.value,
                    history = history
                )

                val response = api.sendMessage(request = request, apiKey = userApiKey)

                if (response.isSuccessful) {
                    val responseText = response.body()?.response ?: "❌ Empty response"
                    
                    // 💡 AI ထံမှ ပြန်ရလာသောစာအား Room Database ထဲသို့ ထည့်သွင်းသိမ်းဆည်းခြင်း
                    database.chatDao().insertMessage(ChatMessageEntity(role = "assistant", text = responseText))

                    val assistantMessage = ChatMessage(
                        role = ChatMessage.MessageRole.ASSISTANT,
                        text = responseText
                    )
                    messages.add(assistantMessage)
                    adapter.notifyItemInserted(messages.size - 1)
                    scrollToBottom()
                } else {
                    val errorText = "❌ Error: ${response.code()} - ${response.message()}"
                    val errorMsg = ChatMessage(role = ChatMessage.MessageRole.ASSISTANT, text = errorText)
                    messages.add(errorMsg)
                    adapter.notifyItemInserted(messages.size - 1)
                    scrollToBottom()
                }
            } catch (e: Exception) {
                val errorText = "❌ Error: ${e.message ?: "Unknown error"}\n\nBackend URL စစ်ဆေးပါ: ${AppState.backendUrl}"
                val errorMsg = ChatMessage(role = ChatMessage.MessageRole.ASSISTANT, text = errorText)
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
        adapter.onDestroy()
        _binding = null
    }
}

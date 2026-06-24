import express from 'express';
import cors from 'cors';
import dotenv from 'dotenv';
import { GoogleGenerativeAI } from '@google/generative-ai';

dotenv.config();

const app = express();
const PORT = process.env.PORT || 3000;
const GEMINI_API_KEY = process.env.GEMINI_API_KEY;

if (!GEMINI_API_KEY) {
  console.error('ERROR: GEMINI_API_KEY environment variable not set');
  process.exit(1);
}

const genAI = new GoogleGenerativeAI(GEMINI_API_KEY);

app.use(cors());
app.use(express.json());

// Health check
app.get('/api/health', (req, res) => {
  res.json({ status: 'ok', timestamp: new Date().toISOString() });
});

// AI Tutor endpoint
app.post('/api/tutor', async (req, res) => {
  try {
    const { message, mode = 'conversation', langMode = 'myanmar', history = [] } = req.body;

    if (!message) {
      return res.status(400).json({ error: 'Message is required' });
    }

    const systemPrompt = getSystemPrompt(mode, langMode);

    const model = genAI.getGenerativeModel({ 
      model: 'gemini-2.5-flash',
      systemInstruction: systemPrompt
    });

    const formattedHistory = history.map(msg => ({
      role: msg.role === 'user' ? 'user' : 'model',
      parts: [{ text: msg.text }],
    }));

    const chat = model.startChat({
      history: formattedHistory,
      generationConfig: {
        temperature: 0.8,
        maxOutputTokens: 1024,
      },
    });

    const result = await chat.sendMessage(message);
    const responseText = result.response.text();

    res.json({
      success: true,
      response: responseText,
      timestamp: new Date().toISOString(),
    });
  } catch (error) {
    console.error('Tutor API error:', error);
    res.status(500).json({
      success: false,
      error: error.message || 'Internal server error',
    });
  }
});

// Level အလိုက် Dynamic Flashcards ထုတ်ပေးမည့် Vocabulary Endpoint သစ်
app.get('/api/vocabulary', async (req, res) => {
  try {
    // Android ဘက်မှ ?level=A1 သို့မဟုတ် A2, B1 ဟု လှမ်းပို့သည်ကို ဖတ်ခြင်း
    const { level = 'A1' } = req.query;

    const model = genAI.getGenerativeModel({ 
      model: 'gemini-2.5-flash',
      systemInstruction: `You are Sayar (ဆရာ), a professional Russian language teacher from Myanmar. Generate 8 high-quality vocabulary flashcards tailored for Myanmar speakers at ${level} level.`
    });

    const prompt = `Generate 8 useful Russian words or phrases for ${level} level learners. 
    Return ONLY a valid JSON array of objects. Do not include markdown formatting or backticks.
    Each object MUST have exactly these keys:
    {
      "id": "unique_string_id",
      "category": "greetings/grammar/daily/travel/etc",
      "myanmar": "Natural Myanmar translation",
      "russian": "Correct Russian word in Cyrillic characters",
      "pronunciation": "Pronunciation hint in Latin characters"
    }`;

    const result = await model.generateContent(prompt);
    let responseText = result.response.text().trim();

    // Markdown Backticks (```json ... ```) ပါလာပါက သန့်စင်ဖယ်ထုတ်ခြင်း
    if (responseText.startsWith('```')) {
      responseText = responseText.replace(/^```json/, '').replace(/^```/, '').trim();
    }

    const vocabulary = JSON.parse(responseText);

    res.json({
      success: true,
      level: level,
      vocabulary: vocabulary,
      count: vocabulary.length
    });

  } catch (error) {
    console.error('Vocabulary AI error:', error);
    res.status(500).json({
      success: false,
      error: 'Failed to generate vocabulary from AI'
    });
  }
});

// Error handling middleware
app.use((err, req, res, next) => {
  console.error('Error:', err);
  res.status(500).json({
    success: false,
    error: 'Internal server error',
  });
});

// မြန်မာဆရာစရိုက် အပြည့်အဝသွင်းထားသော System Prompt Builder
function getSystemPrompt(mode, langMode) {
  const baseInstruction = `You are Sayar (ဆရာ), a native Myanmar citizen who is an absolute expert in the Russian language. 
You speak flawless, natural, and modern Myanmar language (ဗမာစကား). 
Your role is to be a friendly, encouraging, and highly professional Russian language tutor for Myanmar students.

CRITICAL ROLEPLAY RULES:
1. Persona: Act like a real Myanmar person teaching Russian. Use a warm, polite, and helpful tone (e.g., ဗျာ၊ ပါ၊ ခင်ဗျာ၊ နော်).
2. Language Polish: Your Myanmar explanations must be perfectly natural, grammatically correct, and easy for a Myanmar local to understand. No robotic or direct translations.
3. Russian Native Fluency: When you provide Russian words, sentences, or examples, they must be 100% accurate, authentic, and naturally spoken in Russia.
4. Pronunciation Guide: Always provide accurate Latin pronunciation hints for Russian words.`;

  const modeContext = {
    conversation: 'Focus on teaching everyday conversational Russian that is useful in real life.',
    pronunciation: 'Focus heavily on correct Russian phonetics, accent, and how to pronounce difficult letters properly.',
    grammar: 'Explain complex Russian grammar rules simply, using clear comparisons with Myanmar grammar structure.',
    vocabulary: 'Teach vocabulary with practical example sentences and contexts that Myanmar speakers can relate to.',
  };

  const specificInstruction = modeContext[mode] || modeContext.conversation;

  return `${baseInstruction}
${specificInstruction}

CRITICAL FORMATTING RULES FOR RESPONSES:
- Always reply by clearly separating the Myanmar explanation and the Russian teaching.
- Format: [Myanmar Explanation & Encouragement] → [Russian text] (Pronunciation Guide)
- Correct the student's mistakes gently, explaining why it is wrong using perfect Myanmar language.
- Keep sentences appropriate for the student's selected learning level.`;
}

app.listen(PORT, () => {
  console.log(`🚀 Backend server running on http://localhost:${PORT}`);
  console.log(`📚 Myanmar-Russian Language Learning API`);
});

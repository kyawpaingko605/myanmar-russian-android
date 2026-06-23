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

// 💡 Android App ရဲ့ @GET("api/health") နှင့် ကွက်တိ ကိုက်ညီအောင် ပြင်ဆင်ထားပါသည်
app.get('/api/health', (req, res) => {
  res.json({ status: 'ok', timestamp: new Date().toISOString() });
});

// 💡 Android App ရဲ့ @POST("api/tutor") နှင့် ကွက်တိ ကိုက်ညီအောင် ပြင်ဆင်ထားပါသည်
app.post('/api/tutor', async (req, res) => {
  try {
    const { message, mode = 'conversation', langMode = 'myanmar', history } = req.body;

    if (!message) {
      return res.status(400).json({ error: 'Message is required' });
    }

    const systemPrompt = getSystemPrompt(mode, langMode);

    const model = genAI.getGenerativeModel({ 
      model: 'gemini-1.5-flash',
      systemInstruction: systemPrompt
    });

    // History ပုံစံကို Gemini SDK Version အသစ်အတိုင်း စနစ်တကျ ပြောင်းလဲခြင်း
    let formattedHistory = [];
    if (history && Array.isArray(history)) {
      formattedHistory = history
        .filter(msg => msg && (msg.text || msg.content))
        .map(msg => ({
          role: msg.role === 'user' ? 'user' : 'model',
          parts: [{ text: msg.text || msg.content }],
        }));
    }

    const chat = model.startChat({
      history: formattedHistory,
      generationConfig: {
        temperature: 0.8,
        maxOutputTokens: 1024,
      },
    });

    const result = await chat.sendMessage(message);
    const responseText = result.response.text();

    // Android App ရဲ့ TutorResponse Data Class ပုံစံအတိုင်း ကွက်တိ ပြန်ပေးခြင်း
    res.json({
      success: true,
      response: responseText,
      timestamp: new Date().toISOString(),
      error: null
    });
  } catch (error) {
    console.error('Tutor API error:', error);
    res.status(500).json({
      success: false,
      response: null,
      timestamp: new Date().toISOString(),
      error: error.message || 'Internal server error'
    });
  }
});

// Vocabulary endpoint
app.get('/api/vocabulary', (req, res) => {
  const vocabulary = [
    { id: 'g1', category: 'greetings', myanmar: 'မင်္ဂလာပါ', russian: 'Привет', pronunciation: 'Privet' },
    { id: 'g2', category: 'greetings', myanmar: 'ကောင်းပါတယ်', russian: 'Спасибо', pronunciation: 'Spasibo' },
    { id: 'g3', category: 'greetings', myanmar: 'ကျေးဇူးတင်ပါတယ်', russian: 'Пожалуйста', pronunciation: 'Pozhaluysta' },
  ];
  res.json({ success: true, vocabulary, count: vocabulary.length });
});

// Error handling middleware
app.use((err, req, res, next) => {
  console.error('Error:', err);
  res.status(500).json({ success: false, error: 'Internal server error' });
});

function getSystemPrompt(mode, langMode) {
  const baseContext = 'You are an expert Russian language tutor helping Myanmar speakers learn conversational Russian.';
  return `${baseContext}\n\nCRITICAL RULES:\n1. Always respond in BOTH Myanmar and Russian\n2. Include pronunciation guides in Latin characters.`;
}

app.listen(PORT, () => {
  console.log(`🚀 Backend server running on http://localhost:${PORT}`);
});

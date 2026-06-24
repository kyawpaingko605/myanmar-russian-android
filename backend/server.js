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

    // ခေတ်မီပြီး error ကင်းစင်တဲ့ gemini-2.5-flash မော်ဒယ်ကို ပြောင်းလဲအသုံးပြုထားပါတယ်
    const model = genAI.getGenerativeModel({ 
      model: 'gemini-2.5-flash',
      systemInstruction: systemPrompt
    });

    // History ထဲတွင် user နှင့် model မှလွဲ၍ အခြား role များ လုံးဝမပါရပါ
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

// Vocabulary endpoint
app.get('/api/vocabulary', (req, res) => {
  const vocabulary = [
    { id: 'g1', category: 'greetings', myanmar: 'မင်္ဂလာပါ', russian: 'Привет', pronunciation: 'Privet' },
    { id: 'g2', category: 'greetings', myanmar: 'ကောင်းပါတယ်', russian: 'Спасибо', pronunciation: 'Spasibo' },
    { id: 'g3', category: 'greetings', myanmar: 'ကျေးဇူးတင်ပါတယ်', russian: 'Пожалуйста', pronunciation: 'Pozhaluysta' },
    { id: 'n1', category: 'numbers', myanmar: 'တစ်', russian: 'Один', pronunciation: 'Odin' },
    { id: 'n2', category: 'numbers', myanmar: 'နှစ်', russian: 'Два', pronunciation: 'Dva' },
    { id: 'n3', category: 'numbers', myanmar: 'သုံး', russian: 'Три', pronunciation: 'Tri' },
    { id: 'c1', category: 'common', myanmar: 'ကျွန်တော် ရုရှားဘာသာ သင်ချင်ပါတယ်', russian: 'Я хочу учить русский язык', pronunciation: 'Ya khochu uchit russkiy yazyk' },
    { id: 'c2', category: 'common', myanmar: 'ဒါကို ရုရှားလို ဘယ်လိုပြောလဲ?', russian: 'Как это сказать по-русски?', pronunciation: 'Kak eto skazat po-russki?' },
  ];

  res.json({
    success: true,
    vocabulary,
    count: vocabulary.length,
  });
});

// Error handling middleware
app.use((err, req, res, next) => {
  console.error('Error:', err);
  res.status(500).json({
    success: false,
    error: 'Internal server error',
  });
});

function getSystemPrompt(mode, langMode) {
  const baseContext = {
    conversation: 'You are an expert Russian language tutor helping Myanmar speakers learn conversational Russian.',
    pronunciation: 'You are a Russian pronunciation expert. Help the student practice correct Russian pronunciation and phonetics.',
    grammar: 'You are a Russian grammar specialist. Explain grammar rules clearly with Myanmar examples.',
    vocabulary: 'You are a Russian vocabulary teacher. Teach useful words and phrases with context and examples.',
  };

  const baseInstruction = baseContext[mode] || baseContext.conversation;

  if (langMode === 'myanmar') {
    return `${baseInstruction}
CRITICAL RULES:
1. Always respond in BOTH Myanmar and Russian
2. Format: [Myanmar explanation] → [Russian example with pronunciation]
3. Correct mistakes gently with explanations in Myanmar
4. Use simple Russian sentences (A1-A2 level)
5. Include pronunciation hints in Latin characters
6. Encourage frequently with positive feedback`;
  }

  return `${baseInstruction}
Respond in both Myanmar and Russian languages.
Use simple, clear explanations suitable for beginners (A1-A2 level).
Include pronunciation guides in Latin characters.
Be encouraging and supportive.`;
}

app.listen(PORT, () => {
  console.log(`🚀 Backend server running on http://localhost:${PORT}`);
  console.log(`📚 Myanmar-Russian Language Learning API`);
});

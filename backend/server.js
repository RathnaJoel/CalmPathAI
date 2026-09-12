/**
 * CalmPath AI Assistant - Secure AI Proxy Backend (CO8)
 * 
 * Architecture:
 * Android App -> Backend (/api/chat) -> AI Provider (Gemini / OpenAI / Rule-Engine) -> Android App
 * 
 * Security:
 * The AI API key is kept strictly on the backend via environment variables (GEMINI_API_KEY).
 * NO API keys are exposed to the Android mobile client.
 */

const http = require('http');
const fs = require('fs');
const path = require('path');

// Load environment variables from .env if present
const envPath = path.join(__dirname, '.env');
if (fs.existsSync(envPath)) {
  const envContent = fs.readFileSync(envPath, 'utf8');
  envContent.split('\n').forEach(line => {
    const trimmed = line.trim();
    if (trimmed && !trimmed.startsWith('#')) {
      const [key, ...vals] = trimmed.split('=');
      if (key && vals.length > 0) {
        process.env[key.trim()] = vals.join('=').trim().replace(/^["']|["']$/g, '');
      }
    }
  });
}

const PORT = process.env.PORT || 5000;
const AI_PROVIDER = process.env.AI_PROVIDER || 'gemini';
const GEMINI_API_KEY = process.env.GEMINI_API_KEY || '';
const OPENAI_API_KEY = process.env.OPENAI_API_KEY || '';

const SYSTEM_INSTRUCTION = `You are CalmPath AI Assistant, a wellness and environment-aware assistant.
Your purpose is to help users find peaceful places, understand environmental conditions, and use CalmPath.
Use only the provided CalmPath data for factual location, AQI, weather, distance, noise, and Peace Score information.
Do not invent locations or environmental values.
If the provided data is insufficient, say that the information is unavailable.
Keep responses concise, friendly, practical, and wellness-focused.
Do not diagnose medical or psychological conditions.
For place recommendations, prioritize the user's preferences, mood, distance, Peace Score, AQI, noise level, and available environmental conditions.
CalmPath currently supports India only.

Whenever you recommend a place from the provided candidates, return your response strictly formatted as a JSON object with this structure:
{
  "message": "<your friendly natural language response>",
  "recommendedPlaceId": "<place ID from the provided candidate places, or null>",
  "action": "<SHOW_PLACE | SHOW_ON_MAP | NAVIGATE | CHECK_AQI | CHECK_WEATHER | NONE>"
}`;

/**
 * Smart CalmPath rule-based fallback engine that uses actual contextual data.
 */
function generateCalmPathResponse(userMessage, context = {}) {
  const query = (userMessage || '').toLowerCase();
  const places = context.places || [];
  const currentAqi = context.aqi ?? 38;
  const weather = context.weather || '27°C, Clear';
  const mood = context.mood || 'Relax';
  const locality = context.locality || 'Mumbai, Maharashtra';

  // 1. Check Location bounds (India only)
  if (context.isOutsideIndia) {
    return {
      message: 'CalmPath is currently available only in India. I can only provide recommendations for Indian sanctuaries.',
      recommendedPlaceId: null,
      action: 'NONE'
    };
  }

  // 2. AQI Questions
  if (query.includes('aqi') || query.includes('air quality') || query.includes('pollution')) {
    const aqiDesc = currentAqi <= 50 ? 'good and clean' : (currentAqi <= 100 ? 'moderate' : 'elevated');
    const bestCleanPlace = [...places].sort((a, b) => a.aqi - b.aqi)[0];
    
    let msg = `The current AQI near your location in ${locality} is ${currentAqi}. That is generally considered a ${aqiDesc} air quality level.`;
    if (bestCleanPlace) {
      msg += ` If you want the cleanest air right now, I recommend ${bestCleanPlace.name} with an AQI of only ${bestCleanPlace.aqi}.`;
      return {
        message: msg,
        recommendedPlaceId: bestCleanPlace.id,
        action: 'CHECK_AQI'
      };
    }
    return {
      message: msg,
      recommendedPlaceId: null,
      action: 'CHECK_AQI'
    };
  }

  // 3. Weather Questions
  if (query.includes('weather') || query.includes('temperature') || query.includes('good day') || query.includes('visit a park')) {
    const isSuitable = currentAqi <= 100;
    const bestPark = places.find(p => p.category?.toLowerCase() === 'parks') || places[0];
    let msg = `Current conditions look ${isSuitable ? 'suitable' : 'challenging'} for an outdoor sanctuary visit. The weather is ${weather} and the current AQI is ${currentAqi}.`;
    if (bestPark) {
      msg += ` ${bestPark.name} is a wonderful outdoor option with a Peace Score of ${bestPark.peaceScore}/100.`;
      return {
        message: msg,
        recommendedPlaceId: bestPark.id,
        action: 'CHECK_WEATHER'
      };
    }
    return {
      message: msg,
      recommendedPlaceId: null,
      action: 'CHECK_WEATHER'
    };
  }

  // 4. Highest Peace Score
  if (query.includes('highest peace score') || query.includes('highest score') || query.includes('peace score')) {
    const topPlace = [...places].sort((a, b) => (b.peaceScore || 0) - (a.peaceScore || 0))[0];
    if (topPlace) {
      return {
        message: `${topPlace.name} currently has the highest Peace Score nearby at ${topPlace.peaceScore}/100. It features an AQI of ${topPlace.aqi} and whisper-quiet noise level of ${topPlace.noiseDb} dB, only ${topPlace.distanceKm} km away.`,
        recommendedPlaceId: topPlace.id,
        action: 'SHOW_PLACE'
      };
    }
  }

  // 5. Meditation / Quiet / Noise Questions
  if (query.includes('meditat') || query.includes('quiet') || query.includes('silence') || query.includes('whisper')) {
    const meditationPlace = places.find(p => p.category?.toLowerCase() === 'meditation') ||
      [...places].sort((a, b) => a.noiseDb - b.noiseDb)[0];

    if (meditationPlace) {
      return {
        message: `For meditation and stillness, I recommend ${meditationPlace.name}. It maintains a tranquil ambient acoustic level of only ${meditationPlace.noiseDb} dB and an AQI of ${meditationPlace.aqi} (${meditationPlace.distanceKm} km away).`,
        recommendedPlaceId: meditationPlace.id,
        action: 'SHOW_PLACE'
      };
    }
  }

  // 6. Stress / Mood / Relax Intent
  if (query.includes('stress') || query.includes('anxious') || query.includes('tired') || query.includes('sad') || query.includes('relax')) {
    // Prioritize highest peace score + low noise
    const recommended = [...places].sort((a, b) => {
      const scoreDiff = (b.peaceScore || 0) - (a.peaceScore || 0);
      return scoreDiff !== 0 ? scoreDiff : (a.distanceKm || 0) - (b.distanceKm || 0);
    })[0];

    if (recommended) {
      return {
        message: `I understand you're feeling ${query.includes('stress') ? 'stressed' : 'like unwinding'}. Taking a peaceful break in nature can significantly help decompress. Based on your current conditions in ${locality}, I recommend ${recommended.name}. It has a Peace Score of ${recommended.peaceScore}/100, noise level of ${recommended.noiseDb} dB, and is ${recommended.distanceKm} km away.`,
        recommendedPlaceId: recommended.id,
        action: 'SHOW_PLACE'
      };
    }
  }

  // 7. Distance-based query (e.g. within 5 km, nearby)
  if (query.includes('5 km') || query.includes('nearby') || query.includes('near me') || query.includes('close')) {
    const nearby = [...places].sort((a, b) => (a.distanceKm || 0) - (b.distanceKm || 0))[0];
    if (nearby) {
      return {
        message: `${nearby.name} is the closest sanctuary to your current location at ${nearby.distanceKm} km away. It has a Peace Score of ${nearby.peaceScore}/100 and an AQI of ${nearby.aqi}.`,
        recommendedPlaceId: nearby.id,
        action: 'SHOW_PLACE'
      };
    }
  }

  // 8. General sanctuary recommendation
  const fallbackRecommendation = places[0];
  if (fallbackRecommendation) {
    return {
      message: `Based on your current location and CalmPath environmental telemetry, I recommend ${fallbackRecommendation.name}. It offers a tranquil Peace Score of ${fallbackRecommendation.peaceScore}/100 with an AQI of ${fallbackRecommendation.aqi} and ambient acoustics of ${fallbackRecommendation.noiseDb} dB.`,
      recommendedPlaceId: fallbackRecommendation.id,
      action: 'SHOW_PLACE'
    };
  }

  return {
    message: `Hello! I'm your CalmPath AI Assistant. I can help you find tranquil sanctuaries, monitor real-time AQI and acoustic noise, and match spots to your mood across India. How can I help you today?`,
    recommendedPlaceId: null,
    action: 'NONE'
  };
}

/**
 * Calls Gemini REST API when GEMINI_API_KEY is configured.
 */
async function callGeminiApi(userMessage, context) {
  const url = `https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=${GEMINI_API_KEY}`;
  
  const prompt = `User query: "${userMessage}"

Available CalmPath Context:
- Current Mood: ${context.mood || 'Relax'}
- Current Location: ${context.locality || 'Mumbai, Maharashtra, India'} (${context.latitude || 19.076}, ${context.longitude || 72.877})
- Live AQI: ${context.aqi ?? 'Unavailable'}
- Live Weather: ${context.weather || '27°C, Clear'}
- Ambient Noise: ${context.noiseDb ? context.noiseDb + ' dB' : 'Unavailable'}
- Candidate Nearby Sanctuaries:
${(context.places || []).map(p => `  * ID: "${p.id}", Name: "${p.name}", Category: "${p.category}", Distance: ${p.distanceKm} km, Peace Score: ${p.peaceScore}, AQI: ${p.aqi}, Noise: ${p.noiseDb} dB, Address: "${p.address}"`).join('\n')}

Respond with strict JSON adhering to the system instruction.`;

  const body = {
    contents: [
      {
        parts: [
          { text: SYSTEM_INSTRUCTION + "\n\n" + prompt }
        ]
      }
    ],
    generationConfig: {
      temperature: 0.2,
      responseMimeType: "application/json"
    }
  };

  const response = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });

  if (!response.ok) {
    const errorText = await response.text();
    throw new Error(`Gemini API error ${response.status}: ${errorText}`);
  }

  const json = await response.json();
  const rawText = json?.candidates?.[0]?.content?.parts?.[0]?.text || '';
  
  try {
    return JSON.parse(rawText);
  } catch (err) {
    // If not clean JSON, extract JSON substring
    const match = rawText.match(/\{[\s\S]*\}/);
    if (match) {
      return JSON.parse(match[0]);
    }
    return {
      message: rawText,
      recommendedPlaceId: null,
      action: 'NONE'
    };
  }
}

// Create HTTP Server
const server = http.createServer(async (req, res) => {
  // CORS Headers
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

  if (req.method === 'OPTIONS') {
    res.writeHead(204);
    res.end();
    return;
  }

  const parsedUrl = new URL(req.url, `http://${req.headers.host || 'localhost'}`);

  // Health check endpoint
  if (req.method === 'GET' && parsedUrl.pathname === '/health') {
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({
      status: 'ok',
      service: 'CalmPath AI Proxy Backend',
      provider: AI_PROVIDER,
      hasGeminiKey: Boolean(GEMINI_API_KEY && GEMINI_API_KEY !== 'your_gemini_api_key_here'),
      timestamp: new Date().toISOString()
    }));
    return;
  }

  // Chat endpoint
  if (req.method === 'POST' && parsedUrl.pathname === '/api/chat') {
    let bodyData = '';
    req.on('data', chunk => { bodyData += chunk; });
    req.on('end', async () => {
      try {
        const payload = JSON.parse(bodyData || '{}');
        const userMessage = payload.message || '';
        const context = payload.context || {};

        let aiResult;

        if (AI_PROVIDER === 'gemini' && GEMINI_API_KEY && GEMINI_API_KEY !== 'your_gemini_api_key_here') {
          try {
            aiResult = await callGeminiApi(userMessage, context);
          } catch (err) {
            console.warn(`[AI Proxy] Gemini request failed: ${err.message}. Falling back to CalmPath reasoning engine.`);
            aiResult = generateCalmPathResponse(userMessage, context);
          }
        } else {
          aiResult = generateCalmPathResponse(userMessage, context);
        }

        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({
          message: aiResult.message || "I'm here to help you discover calm and peaceful sanctuaries.",
          recommendedPlaceId: aiResult.recommendedPlaceId || null,
          action: aiResult.action || 'NONE'
        }));
      } catch (err) {
        console.error('[AI Proxy] Error processing chat request:', err);
        res.writeHead(500, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({
          error: 'Internal Server Error',
          message: 'I encountered an issue processing your request. Please try again.'
        }));
      }
    });
    return;
  }

  // 404 for unknown endpoints
  res.writeHead(404, { 'Content-Type': 'application/json' });
  res.end(JSON.stringify({ error: 'Endpoint not found' }));
});

server.listen(PORT, '0.0.0.0', () => {
  console.log(`=======================================================`);
  console.log(`🌱 CalmPath AI Assistant Backend running on port ${PORT}`);
  console.log(`📡 URL: http://localhost:${PORT}/api/chat`);
  console.log(`🔒 Security: AI API keys configured server-side`);
  console.log(`🤖 Active Provider: ${AI_PROVIDER} (Gemini Key: ${GEMINI_API_KEY ? 'Present' : 'Rule-Based Fallback'})`);
  console.log(`=======================================================`);
});

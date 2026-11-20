import express from "express";
import cors from "cors";
import helmet from "helmet";
import { GoogleGenerativeAI } from "@google/generative-ai";

const app = express();
app.use(helmet());
app.use(cors());
app.use(express.json({ limit: "1mb" }));

const PORT = process.env.PORT || 3000;
const API_KEY = process.env.GEMINI_API_KEY;

app.get("/health", (_req, res) => {
  res.json({ status: "ok", service: "SANTALI JARVIS AI backend" });
});

app.post("/api/chat", async (req, res) => {
  try {
    const message = typeof req.body?.message === "string" ? req.body.message.trim() : "";
    if (!message) return res.status(400).json({ error: "Message is required." });
    if (message.length > 12000) return res.status(413).json({ error: "Message is too long." });
    if (!API_KEY) return res.status(503).json({ error: "Backend owner must configure GEMINI_API_KEY." });

    const genAI = new GoogleGenerativeAI(API_KEY);
    const model = genAI.getGenerativeModel({
      model: "gemini-2.0-flash",
      systemInstruction: `You are SANTALI JARVIS AI, a helpful, honest, multilingual personal AI assistant.
Reply naturally in the language used by the user. Support Bengali, Hindi, English, and Santali when capable.
Do not claim to perform phone actions unless a real authorized tool has completed them.
Protect private information and ask for confirmation before consequential actions.`
    });

    const result = await model.generateContent(message);
    const reply = result.response.text();
    return res.json({ reply });
  } catch (error) {
    console.error("Chat request failed:", error?.message || error);
    return res.status(502).json({ error: "AI request failed. Check the backend logs and model/API configuration." });
  }
});

app.listen(PORT, () => {
  console.log(`SANTALI JARVIS backend listening on ${PORT}`);
});

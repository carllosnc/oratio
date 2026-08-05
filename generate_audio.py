import json
import os
import asyncio
import edge_tts

VOICE_MAP = {
    "pt": "pt-BR-AntonioNeural",
    "en": "en-US-ChristopherNeural",
    "es": "es-ES-AlvaroNeural",
    "la": "it-IT-DiegoNeural"
}

OUTPUT_DIR = os.path.join("app", "src", "main", "res", "raw")

async def generate_audio():
    os.makedirs(OUTPUT_DIR, exist_ok=True)
    seed_path = os.path.join("app", "src", "main", "assets", "prayers_seed.json")
    
    with open(seed_path, "r", encoding="utf-8") as f:
        data = json.load(f)

    prayers = data.get("prayers", [])
    
    for prayer in prayers:
        prayer_id = prayer["id"]
        for tr in prayer.get("translations", []):
            lang = tr["languageCode"]
            voice = VOICE_MAP.get(lang, "en-US-ChristopherNeural")
            text = tr["content"]
            
            clean_text = f"{tr['title']}. {text}"
            file_name = f"{prayer_id}_{lang}.mp3".lower()
            output_file = os.path.join(OUTPUT_DIR, file_name)
            
            print(f"Generating {file_name} with voice {voice}...")
            communicate = edge_tts.Communicate(clean_text, voice, pitch="-15Hz", rate="-10%")
            await communicate.save(output_file)
            print(f"Saved {file_name}")

if __name__ == "__main__":
    asyncio.run(generate_audio())

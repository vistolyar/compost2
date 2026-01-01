from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from openai import OpenAI
import os
import json
import base64
import re
from fastapi.responses import Response, JSONResponse
from pydantic import BaseModel
import boto3
import uuid

# --- НАСТРОЙКИ ---
app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

s3_client = boto3.client(
    's3',
    aws_access_key_id=os.environ.get('AWS_ACCESS_KEY_ID'),
    aws_secret_access_key=os.environ.get('AWS_SECRET_ACCESS_KEY'),
    region_name='eu-north-1'
)
BUCKET_NAME = os.environ.get('AWS_BUCKET_NAME')

# --- МОДЕЛИ ДАННЫХ ---
class AudioRequest(BaseModel):
    prompt: str
    openai_key: str
    # Теперь эти поля опциональны: можно прислать ИЛИ ключ, ИЛИ base64
    file_key: str | None = None
    audio_base64: str | None = None

# --- ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ ---
def clean_json_string(text):
    text = text.strip()
    pattern = r"^```(?:json)?\s*(\{.*?\})\s*```$"
    match = re.search(pattern, text, re.DOTALL)
    if match:
        return match.group(1)
    return text

@app.get("/api/ping")
async def ping():
    return {"message": "pong", "status": "ok"}

# --- ШАГ 1: ПОЛУЧЕНИЕ ССЫЛКИ НА ЗАГРУЗКУ ---
@app.get("/api/get_upload_url")
async def get_upload_url():
    try:
        file_id = str(uuid.uuid4())
        file_key = f"raw_audio/{file_id}.m4a"

        presigned_url = s3_client.generate_presigned_url(
            'put_object',
            Params={
                'Bucket': BUCKET_NAME,
                'Key': file_key,
                'ContentType': 'audio/mp4'
            },
            ExpiresIn=3600
        )

        return JSONResponse(content={
            "upload_url": presigned_url,
            "file_key": file_key
        })
    except Exception as e:
        return JSONResponse(content={"error": str(e)}, status_code=500)

# --- ШАГ 2: ОБРАБОТКА (ТЕПЕРЬ УМЕЕТ ЧИТАТЬ ИЗ S3) ---
@app.post("/api/process-audio")
async def process_audio(request: AudioRequest):
    temp_file_path = None
    try:
        print("1. Request received")
        if not request.openai_key:
            return JSONResponse(content={"error": "OpenAI Key missing"}, status_code=400)
            
        client = OpenAI(api_key=request.openai_key)
        
        # Создаем временный файл
        temp_filename = f"audio_{os.urandom(4).hex()}.m4a"
        temp_file_path = os.path.join("/tmp", temp_filename)

        # ЛОГИКА ВЫБОРА ИСТОЧНИКА
        if request.file_key:
            # ВАРИАНТ А: Файл в S3 (Новый способ)
            print(f"2. Downloading from S3: {request.file_key}")
            s3_client.download_file(BUCKET_NAME, request.file_key, temp_file_path)
            
        elif request.audio_base64:
            # ВАРИАНТ Б: Base64 (Старый способ - для совместимости)
            print("2. Decoding Base64...")
            clean_base64 = request.audio_base64.replace("\n", "").replace("\r", "")
            audio_data = base64.b64decode(clean_base64)
            with open(temp_file_path, "wb") as f:
                f.write(audio_data)
        else:
            return JSONResponse(content={"error": "No file_key or audio_base64 provided"}, status_code=400)

        # ДАЛЬШЕ ВСЁ КАК ОБЫЧНО (Whisper -> GPT)
        print("3. Sending to Whisper...")
        with open(temp_file_path, "rb") as audio_file:
            transcription = client.audio.transcriptions.create(
                model="whisper-1",
                file=audio_file
            )
        
        raw_text = transcription.text
        print(f"   Whisper result: {raw_text[:50]}...")

        print("4. Sending to GPT...")
        completion = client.chat.completions.create(
            model="gpt-4o-mini",
            messages=[
                {"role": "system", "content": f"You are a professional editor. {request.prompt}. Return the result strictly as a valid JSON object with two keys: 'title' (string) and 'content' (string containing HTML). Do not add any markdown formatting."},
                {"role": "user", "content": f"Here is the raw transcript: {raw_text}"}
            ],
            response_format={"type": "json_object"}
        )

        clean_response = clean_json_string(completion.choices[0].message.content)
        return Response(content=clean_response, media_type="application/json")

    except Exception as e:
        print(f"Error: {str(e)}")
        return JSONResponse(content={"error": str(e)}, status_code=500)
        
    finally:
        if temp_file_path and os.path.exists(temp_file_path):
            try:
                os.remove(temp_file_path)
            except:
                pass

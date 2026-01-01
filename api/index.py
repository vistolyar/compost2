# Trigger build
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from openai import OpenAI
import os
import json
import base64
import re
from fastapi.responses import Response, JSONResponse
from pydantic import BaseModel

# --- НОВЫЕ ИМПОРТЫ ДЛЯ AWS S3 ---
import boto3
from botocore.exceptions import ClientError
import uuid
# -------------------------------

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# --- НАСТРОЙКА AWS S3 ---
# Мы инициализируем клиента один раз при старте
s3_client = boto3.client(
    's3',
    aws_access_key_id=os.environ.get('AWS_ACCESS_KEY_ID'),
    aws_secret_access_key=os.environ.get('AWS_SECRET_ACCESS_KEY'),
    region_name='eu-north-1' # Твой регион (Стокгольм)
)
BUCKET_NAME = os.environ.get('AWS_BUCKET_NAME')
# ------------------------

class AudioRequest(BaseModel):
    audio_base64: str
    prompt: str
    openai_key: str

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

# --- НОВЫЙ ЭНДПОИНТ (Получение ссылки на загрузку) ---
@app.get("/api/get_upload_url")
async def get_upload_url():
    try:
        # 1. Генерируем уникальное имя файла
        file_id = str(uuid.uuid4())
        file_key = f"raw_audio/{file_id}.m4a"

        # 2. Генерируем временную ссылку (Presigned URL)
        presigned_url = s3_client.generate_presigned_url(
            'put_object',
            Params={
                'Bucket': BUCKET_NAME,
                'Key': file_key,
                'ContentType': 'audio/mp4'
            },
            ExpiresIn=3600  # Ссылка живет 1 час
        )

        # 3. Возвращаем результат
        return JSONResponse(content={
            "upload_url": presigned_url,
            "file_key": file_key
        })

    except Exception as e:
        return JSONResponse(
            content={"error": str(e)}, 
            status_code=500
        )
# -----------------------------------------------------

@app.post("/api/process-audio")
async def process_audio(request: AudioRequest):
    temp_file_path = None
    try:
        print("1. Request received")
        
        if not request.openai_key:
            return Response(content=json.dumps({"error": "OpenAI Key is missing"}), status_code=400, media_type="application/json")
            
        client = OpenAI(api_key=request.openai_key)

        # Декодируем Base64
        try:
            print("2. Decoding Base64...")
            # Удаляем возможные переносы строк, которые могут прийти с Android
            clean_base64 = request.audio_base64.replace("\n", "").replace("\r", "")
            audio_data = base64.b64decode(clean_base64)
            print(f"    Decoded {len(audio_data)} bytes")
        except Exception as e:
             return Response(content=json.dumps({"error": f"Base64 Decode Error: {str(e)}"}), status_code=400, media_type="application/json")

        # Сохраняем во временный файл в /tmp (Это важно для Vercel!)
        temp_filename = f"audio_{os.urandom(4).hex()}.m4a"
        temp_file_path = os.path.join("/tmp", temp_filename)
        
        print(f"3. Saving to {temp_file_path}")
        with open(temp_file_path, "wb") as f:
            f.write(audio_data)

        try:
            # 1. Whisper
            print("4. Sending to Whisper...")
            with open(temp_file_path, "rb") as audio_file:
                transcription = client.audio.transcriptions.create(
                    model="whisper-1",
                    file=audio_file
                )
            
            raw_text = transcription.text
            print(f"    Whisper result: {raw_text[:50]}...")

            # 2. GPT-4o
            print("5. Sending to GPT...")
            completion = client.chat.completions.create(
                model="gpt-4o-mini",
                messages=[
                    {"role": "system", "content": f"You are a professional editor. {request.prompt}. Return the result strictly as a valid JSON object with two keys: 'title' (string) and 'content' (string containing HTML). Do not add any markdown formatting."},
                    {"role": "user", "content": f"Here is the raw transcript: {raw_text}"}
                ],
                response_format={"type": "json_object"}
            )

            raw_response = completion.choices[0].message.content
            clean_response = clean_json_string(raw_response)

            print("6. Success!")
            return Response(content=clean_response, media_type="application/json")

        except Exception as openai_error:
            print(f"OpenAI Error: {str(openai_error)}")
            return Response(
                content=json.dumps({"error": f"OpenAI Error: {str(openai_error)}"}), 
                status_code=400, # Возвращаем 400, чтобы Android не падал с 500, а показал текст
                media_type="application/json"
            )

    except Exception as e:
        print(f"Critical Error: {str(e)}")
        # Ловим любую другую ошибку и возвращаем её текстом
        return Response(
            content=json.dumps({"error": f"Server Error: {str(e)}"}), 
            status_code=500, 
            media_type="application/json"
        )
    finally:
        # Чистим мусор
        if temp_file_path and os.path.exists(temp_file_path):
            try:
                os.remove(temp_file_path)
            except:
                pass

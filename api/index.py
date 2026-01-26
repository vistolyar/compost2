from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from openai import OpenAI
import os
import json
import re
from fastapi.responses import JSONResponse
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

# --- МОДЕЛИ ДАННЫХ (Request Models) ---

class TranscribeRequest(BaseModel):
    file_key: str
    openai_key: str

class ProcessTextRequest(BaseModel):
    raw_text: str
    prompt: str
    openai_key: str

# --- ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ ---
def clean_json_string(text):
    text = text.strip()
    # Пытаемся вытащить JSON из markdown блоков ```json ... ```
    pattern = r"^```(?:json)?\s*(\{.*?\})\s*```$"
    match = re.search(pattern, text, re.DOTALL)
    if match:
        return match.group(1)
    return text

@app.get("/api/ping")
async def ping():
    return {"message": "pong", "status": "ok"}

# --- ЭНДПОИНТ 1: ПОЛУЧЕНИЕ ССЫЛКИ (БЕЗ ИЗМЕНЕНИЙ) ---
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


# --- ЭНДПОИНТ 2: ТРАНСКРИБАЦИЯ (STAGE 1) ---
@app.post("/api/transcribe")
async def transcribe_audio(request: TranscribeRequest):
    temp_file_path = None
    try:
        print(f"1. Transcribe request for: {request.file_key}")
        
        if not request.openai_key:
            return JSONResponse(content={"error": "OpenAI Key missing"}, status_code=400)
            
        client = OpenAI(api_key=request.openai_key)
        
        # Скачиваем файл из S3 во временную папку
        temp_filename = f"audio_{os.urandom(4).hex()}.m4a"
        temp_file_path = os.path.join("/tmp", temp_filename)

        print(f"2. Downloading from S3 to {temp_file_path}")
        s3_client.download_file(BUCKET_NAME, request.file_key, temp_file_path)

        # Отправляем в Whisper
        print("3. Sending to Whisper...")
        with open(temp_file_path, "rb") as audio_file:
            transcription = client.audio.transcriptions.create(
                model="whisper-1",
                file=audio_file
            )
        
        raw_text = transcription.text
        print(f"4. Success. Text length: {len(raw_text)}")
        
        return JSONResponse(content={"raw_text": raw_text})

    except Exception as e:
        print(f"Error: {str(e)}")
        return JSONResponse(content={"error": str(e)}, status_code=500)
        
    finally:
        # Всегда удаляем временный файл
        if temp_file_path and os.path.exists(temp_file_path):
            try:
                os.remove(temp_file_path)
            except:
                pass


# --- ЭНДПОИНТ 3: ПРОЦЕССИНГ ТЕКСТА (STAGE 2) ---
@app.post("/api/process-text")
async def process_text(request: ProcessTextRequest):
    try:
        print("1. Process text request received")
        
        if not request.openai_key:
            return JSONResponse(content={"error": "OpenAI Key missing"}, status_code=400)

        client = OpenAI(api_key=request.openai_key)

        print("2. Sending to GPT-4o-mini...")
        completion = client.chat.completions.create(
            model="gpt-4o-mini",
            messages=[
                {"role": "system", "content": f"You are a professional editor. {request.prompt}. Return the result strictly as a valid JSON object with two keys: 'title' (string) and 'content' (string containing HTML). Do not add any markdown formatting."},
                {"role": "user", "content": f"Here is the text to process: {request.raw_text}"}
            ],
            response_format={"type": "json_object"}
        )

        raw_response = completion.choices[0].message.content
        clean_response = clean_json_string(raw_response)

        print("3. Success!")
        # Возвращаем JSON напрямую, так как GPT уже вернул структуру
        return JSONResponse(content=json.loads(clean_response))

    except Exception as e:
        print(f"Error: {str(e)}")
        return JSONResponse(content={"error": str(e)}, status_code=500)

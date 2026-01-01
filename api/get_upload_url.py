from http.server import BaseHTTPRequestHandler
import os
import json
import boto3
from botocore.exceptions import ClientError
import uuid

# Настройка подключения к AWS (берем ключи из переменных Vercel)
s3_client = boto3.client(
    's3',
    aws_access_key_id=os.environ.get('AWS_ACCESS_KEY_ID'),
    aws_secret_access_key=os.environ.get('AWS_SECRET_ACCESS_KEY'),
    region_name='eu-north-1' # Твой регион (Стокгольм)
)

BUCKET_NAME = os.environ.get('AWS_BUCKET_NAME')

class handler(BaseHTTPRequestHandler):
    def do_GET(self):
        # 1. Генерируем уникальное имя файла, чтобы они не перезатерлись
        # file_id будет, например, "a1b2c3d4-..."
        file_id = str(uuid.uuid4())
        # Итоговое имя в бакете: "raw_audio/a1b2c3d4-....m4a"
        file_key = f"raw_audio/{file_id}.m4a"

        try:
            # 2. Генерируем ссылку (Presigned URL)
            # Эта ссылка позволит загрузить файл, даже не имея пароля от аккаунта
            presigned_url = s3_client.generate_presigned_url(
                'put_object',
                Params={
                    'Bucket': BUCKET_NAME,
                    'Key': file_key,
                    'ContentType': 'audio/mp4' # Тип файла (m4a)
                },
                ExpiresIn=3600  # Ссылка протухнет через 1 час (безопасность)
            )

            # 3. Формируем ответ для приложения
            response_data = {
                "upload_url": presigned_url,
                "file_key": file_key
            }

            # Отправляем ответ (JSON)
            self.send_response(200)
            self.send_header('Content-type', 'application/json')
            self.end_headers()
            self.wfile.write(json.dumps(response_data).encode('utf-8'))

        except Exception as e:
            # Если что-то сломалось — сообщаем ошибку
            self.send_response(500)
            self.send_header('Content-type', 'application/json')
            self.end_headers()
            self.wfile.write(json.dumps({"error": str(e)}).encode('utf-8'))

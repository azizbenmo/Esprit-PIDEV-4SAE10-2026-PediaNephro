FROM python:3.10-slim

EXPOSE 8000

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

ADD main.py main.py

ENTRYPOINT ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
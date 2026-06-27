# Engineering Second Brain

A RAG-powered personal knowledge assistant. Write engineering notes, search them semantically, and chat with your knowledge base — answers grounded in your own notes, no hallucination.

## Architecture

```
Angular 19 (4200)
      │
      │  REST + SSE streaming
      ▼
Spring Boot 3 (8080)          PostgreSQL 16
  Notes CRUD  ──────────────► notes, note_chunks
  Search API  
  Chat API (SSE)
      │
      │  HTTP
      ▼
Python FastAPI (8000)         Qdrant (6333)
  /embed  ──────────────────► vector store
  /search ◄──────────────────
  /chat   → OpenAI gpt-4o-mini (streaming)
  /summarize → OpenAI gpt-4o-mini
```

## Features

- **Notes** — create, edit, delete markdown notes with tags
- **Auto-summary** — notes over 500 chars get a 2-3 sentence AI summary on save
- **Semantic search** — search by meaning, not just keywords (sentence-transformers embeddings → Qdrant)
- **RAG chat** — ask questions, get answers synthesized from relevant note excerpts with source cards
- **Streaming** — AI responses stream token-by-token via SSE

## Quick Start

### Prerequisites

- Docker + Docker Compose
- Node.js 20+, Java 21, Python 3.11+
- OpenAI API key

### 1. Start infrastructure

```bash
docker compose up -d
```

Starts PostgreSQL (5432) and Qdrant (6333).

### 2. AI service

```bash
cd ai-service
cp .env.example .env          # add your OPENAI_API_KEY
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
uvicorn main:app --reload --port 8000
```

### 3. Backend

```bash
cd backend
./mvnw spring-boot:run
```

Runs on port 8080. Hibernate auto-creates tables on first start.

### 4. Frontend

```bash
cd frontend
npm install
npm start
```

Open [http://localhost:4200](http://localhost:4200).

## Project Structure

```
second-brain/
├── docker-compose.yml       # PostgreSQL + Qdrant
├── frontend/                # Angular 19 SPA
├── backend/                 # Spring Boot 3 REST API + SSE
└── ai-service/              # Python FastAPI (embeddings + LLM)
```

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Frontend | Angular 19, TypeScript, SSE via fetch API |
| Backend | Spring Boot 3.5, Java 21, JPA/Hibernate, SseEmitter |
| AI Service | Python FastAPI, sentence-transformers, OpenAI SDK |
| Vector DB | Qdrant (cosine similarity, user-scoped filtering) |
| Relational DB | PostgreSQL 16 |
| LLM | OpenAI gpt-4o-mini |
| Embeddings | all-MiniLM-L6-v2 (384d, runs locally) |

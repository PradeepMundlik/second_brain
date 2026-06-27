# Second Brain — AI Service

Python FastAPI service. Handles all ML/AI work: embedding notes into vectors, semantic search via Qdrant, streaming chat via OpenAI, and note summarization.

## Stack

- **FastAPI** + **Uvicorn**
- **sentence-transformers** — `all-MiniLM-L6-v2` (384d, runs fully locally, no API cost)
- **qdrant-client** — vector upsert, search with user-scoped filtering and score threshold
- **OpenAI SDK** — `gpt-4o-mini` for streaming chat + summarization
- **Pydantic** — request/response schemas

## Structure

```
ai-service/
├── main.py                  # FastAPI app, mounts routers
├── schemas.py               # Pydantic models for all request/response types
├── requirements.txt
├── .env                     # OPENAI_API_KEY (not committed)
├── .env.example
├── routers/
│   ├── embed.py             # POST /embed
│   ├── search.py            # POST /search
│   ├── chat.py              # POST /chat → StreamingResponse (SSE)
│   └── summarize.py         # POST /summarize
└── services/
    ├── embedder.py          # loads sentence-transformers model, encodes text
    ├── chunking.py          # splits note content into overlapping chunks
    ├── vector_store.py      # Qdrant client wrapper (upsert, search, delete)
    └── llm.py               # async OpenAI client, stream_chat() generator
```

## Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/embed` | Chunk note → embed → upsert into Qdrant |
| POST | `/search` | Embed query → Qdrant similarity search (score ≥ 0.2) |
| POST | `/chat` | RAG generation → `StreamingResponse` (SSE) |
| POST | `/summarize` | 2-3 sentence summary via gpt-4o-mini |

## Setup

```bash
cp .env.example .env
# set OPENAI_API_KEY in .env

python -m venv .venv
source .venv/bin/activate        # Windows: .venv\Scripts\activate
pip install -r requirements.txt
uvicorn main:app --reload --port 8000
```

Requires Qdrant on `localhost:6333` (see root `docker-compose.yml`).

## Key Design Decisions

### Embeddings model — local, not OpenAI

`all-MiniLM-L6-v2` runs on CPU, produces 384-dimensional vectors, and is fast enough for a personal knowledge base. This avoids per-embed API costs when ingesting many notes. The trade-off is lower quality than `text-embedding-3-small` (1536d) — acceptable for personal notes.

### Score threshold

`query_points` is called with `score_threshold=0.2` (cosine similarity). Results below this are irrelevant noise; the LLM receives an empty context and responds "I don't see that in your notes" rather than hallucinating.

### RAG prompt

```
SYSTEM:
You are a personal knowledge assistant.
Answer the user's question based ONLY on the notes provided below.
If the answer is not in the notes, say "I don't see that in your notes."
Be concise.

CONTEXT:
Excerpt 1:
<chunk text>

CONVERSATION HISTORY:
<previous turns>

USER:
<query>
```

Context contains raw chunk text only — no note titles. Titles are fetched by Spring Boot from PostgreSQL solely for rendering source cards in the UI.

### Stateless chat

The backend persists nothing about conversations. Angular sends the full `history: [{role, content}]` array on every request. Python injects it into the LLM messages list. Refreshing the page clears history — acceptable trade-off for a portfolio project.

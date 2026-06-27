# Second Brain — Backend

Spring Boot 3.5 REST API. Handles notes CRUD, delegates embedding and LLM work to the Python AI service, and streams AI chat responses to the Angular frontend via SSE.

## Stack

- **Spring Boot 3.5** — Java 21
- **Spring Data JPA / Hibernate** — PostgreSQL, auto DDL on startup
- **SseEmitter** — streams AI response chunks to the Angular client
- **RestTemplate** — HTTP calls to Python AI service (two instances: short-timeout for most calls, 60s for streaming)

## Structure

```
src/main/java/com/secondbrain/backend/
├── controller/
│   ├── NoteController.java        # CRUD /api/notes
│   ├── SearchController.java      # POST /api/search
│   └── ChatController.java        # POST /api/chat → SseEmitter
├── service/
│   ├── NoteService.java           # business logic, calls embedding + summary
│   ├── EmbeddingService.java      # chunks note, calls /embed, saves NoteChunk
│   ├── SearchService.java         # calls /search, fetches titles from DB
│   ├── ChatService.java           # RAG: search → format context → stream from Python
│   └── SummaryService.java        # calls /summarize, saves summary to note
├── client/
│   └── AiServiceClient.java       # HTTP client for Python AI service
├── entity/
│   ├── Note.java                  # id, userId, title, content, tags, summary
│   └── NoteChunk.java             # noteId, chunkIndex, qdrantPointId
├── dto/                           # request/response objects
├── repository/
│   ├── NoteRepository.java
│   └── NoteChunkRepository.java
└── exception/
    ├── NoteNotFoundException.java
    └── GlobalExceptionHandler.java
```

## API

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/notes` | List all notes for user |
| POST | `/api/notes` | Create note (triggers embed + summarize) |
| PUT | `/api/notes/{id}` | Update note (re-embeds, re-summarizes) |
| DELETE | `/api/notes/{id}` | Delete note + vectors |
| POST | `/api/search` | Semantic search |
| POST | `/api/chat` | RAG chat → SSE stream |

## Running

```bash
./mvnw spring-boot:run
```

Requires PostgreSQL on `localhost:5432` (see root `docker-compose.yml`) and Python AI service on `localhost:8000`.

Config lives in `src/main/resources/application.properties`.

## RAG Chat Flow

1. `ChatController` returns an `SseEmitter` immediately (non-blocking to HTTP thread)
2. `CompletableFuture` runs the blocking work async:
   - Calls Python `/search` → top-5 relevant chunks from Qdrant
   - Fetches note titles from PostgreSQL (for source cards only)
   - Emits `event: sources` to Angular
   - Calls Python `/chat` with query + formatted context + history
   - Forwards each SSE chunk from Python → Angular via `SseEmitter`
3. Note titles are fetched from PostgreSQL only for the UI source cards — the LLM prompt receives only the raw chunk text (no titles), keeping context focused and tokens low.

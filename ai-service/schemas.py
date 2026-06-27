from pydantic import BaseModel


class EmbedRequest(BaseModel):
    note_id: int
    user_id: int
    title: str
    content: str


class ChunkResult(BaseModel):
    chunk_index: int
    chunk_text: str
    qdrant_point_id: str


class EmbedResponse(BaseModel):
    note_id: int
    chunks: list[ChunkResult]


class SearchRequest(BaseModel):
    query: str
    user_id: int
    top_k: int = 5


class SearchHit(BaseModel):
    note_id: int
    excerpt: str
    score: float


class SearchResponse(BaseModel):
    results: list[SearchHit]


class ChatMessage(BaseModel):
    role: str  # "user" or "assistant"
    content: str


class ChatRequest(BaseModel):
    query: str
    context: str        # pre-formatted excerpts from Spring Boot (chunk text only)
    history: list[ChatMessage] = []


class SummarizeRequest(BaseModel):
    note_id: int
    title: str
    content: str


class SummarizeResponse(BaseModel):
    note_id: int
    summary: str

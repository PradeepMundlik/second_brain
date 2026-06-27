from fastapi import APIRouter, HTTPException
from schemas import EmbedRequest, EmbedResponse, ChunkResult
from services import chunking, embedder, vector_store

router = APIRouter()


@router.post("/embed", response_model=EmbedResponse)
def embed_note(req: EmbedRequest):

    print(f"Received embed request: {req}")
    chunks = chunking.chunk(req.title, req.content)
    embeddings = embedder.embed(chunks)
    point_ids = vector_store.upsert(req.note_id, req.user_id, chunks, embeddings)

    return EmbedResponse(
        note_id=req.note_id,
        chunks=[
            ChunkResult(chunk_index=i, chunk_text=chunks[i], qdrant_point_id=point_ids[i])
            for i in range(len(chunks))
        ],
    )


@router.delete("/notes/{note_id}/embeddings", status_code=204)
def delete_embeddings(note_id: int):
    vector_store.delete_by_note(note_id)

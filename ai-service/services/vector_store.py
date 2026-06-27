import os
import uuid
from qdrant_client import QdrantClient
from qdrant_client.models import (
    Distance,
    VectorParams,
    PointStruct,
    Filter,
    FieldCondition,
    MatchValue,
    FilterSelector,
)

COLLECTION = "notes"
VECTOR_SIZE = 384  # all-MiniLM-L6-v2 output dimensions (OpenAI text-embedding-3-small = 1536)

_client = QdrantClient(
    host=os.getenv("QDRANT_HOST", "localhost"),
    port=int(os.getenv("QDRANT_PORT", 6333)),
)


def ensure_collection() -> None:
    """Creates the Qdrant collection if it doesn't already exist."""
    if not _client.collection_exists(COLLECTION):
        _client.create_collection(
            collection_name=COLLECTION,
            vectors_config=VectorParams(size=VECTOR_SIZE, distance=Distance.COSINE),
        )


def upsert(note_id: int, user_id: int, chunks: list[str], embeddings: list[list[float]]) -> list[str]:
    """
    Stores each (chunk, embedding) pair as a Qdrant point.
    Returns the list of generated point IDs (UUIDs) in chunk order.

    The payload { note_id, user_id, chunk_index, chunk_text } is stored alongside
    the vector so we can filter by user and reconstruct results without a DB lookup.
    """
    point_ids = [str(uuid.uuid4()) for _ in chunks]

    points = [
        PointStruct(
            id=point_ids[i],
            vector=embeddings[i],
            payload={
                "note_id": note_id,
                "user_id": user_id,
                "chunk_index": i,
                "chunk_text": chunks[i],
            },
        )
        for i in range(len(chunks))
    ]

    _client.upsert(collection_name=COLLECTION, points=points)
    return point_ids


def search(query_vector: list[float], user_id: int, top_k: int = 5):
    """
    Finds the top-k most similar chunks for this user.

    The user_id filter is critical: it ensures users only see their own notes.
    Without it, all users' notes would be mixed in results.

    Uses query_points — the unified search API in qdrant-client >= 1.9
    (the older .search() method was removed in that version).
    """
    result = _client.query_points(
        collection_name=COLLECTION,
        query=query_vector,
        query_filter=Filter(
            must=[FieldCondition(key="user_id", match=MatchValue(value=user_id))]
        ),
        limit=top_k,
        score_threshold=0.2,
        with_payload=True,
    )
    print(f"Search result: {result}")
    return result.points


def delete_by_note(note_id: int) -> None:
    """Removes all vectors belonging to a note when the note is deleted."""
    _client.delete(
        collection_name=COLLECTION,
        points_selector=FilterSelector(
            filter=Filter(
                must=[FieldCondition(key="note_id", match=MatchValue(value=note_id))]
            )
        ),
    )

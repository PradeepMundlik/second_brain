from fastapi import APIRouter
from schemas import SearchRequest, SearchResponse, SearchHit
from services import embedder, vector_store

router = APIRouter()


@router.post("/search", response_model=SearchResponse)
def search(req: SearchRequest):
    query_vector = embedder.embed([req.query])[0]
    hits = vector_store.search(query_vector, req.user_id, req.top_k)

    return SearchResponse(
        results=[
            SearchHit(
                note_id=hit.payload["note_id"],
                excerpt=hit.payload["chunk_text"],
                score=hit.score,
            )
            for hit in hits
        ]
    )

from fastapi import APIRouter
from fastapi.responses import StreamingResponse
from schemas import ChatRequest
from services import llm

router = APIRouter()


@router.post("/chat")
async def chat(req: ChatRequest) -> StreamingResponse:
    return StreamingResponse(
        llm.stream_chat(req.query, req.context, req.history),
        media_type="text/event-stream",
        headers={"Cache-Control": "no-cache", "X-Accel-Buffering": "no"},
    )

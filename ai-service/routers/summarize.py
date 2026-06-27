from fastapi import APIRouter
from schemas import SummarizeRequest, SummarizeResponse
from services import llm

router = APIRouter()


@router.post("/summarize", response_model=SummarizeResponse)
async def summarize(req: SummarizeRequest) -> SummarizeResponse:
    summary = await llm.get_summary(req.title, req.content)
    return SummarizeResponse(note_id=req.note_id, summary=summary)

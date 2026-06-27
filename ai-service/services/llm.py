import os
import json
from groq import AsyncGroq
from schemas import ChatMessage

_SYSTEM_PROMPT = (
    "You are a personal knowledge assistant. "
    "Answer the user's question based ONLY on the notes provided below. "
    "If the answer is not in the notes, say \"I don't see that in your notes.\" "
    "Be concise. When drawing from a specific excerpt, you may reference it."
)

_MODEL = "llama-3.3-70b-versatile"

_client = AsyncGroq(api_key=os.getenv("GROQ_API_KEY"))


async def stream_chat(query: str, context: str, history: list[ChatMessage]):
    """Async generator that yields SSE-formatted data lines for each token."""
    messages = [
        {"role": "system", "content": _SYSTEM_PROMPT},
        *[{"role": m.role, "content": m.content} for m in history],
        {"role": "user", "content": f"{query}\n\nContext — Relevant excerpts from your notes:\n{context}"},
    ]

    stream = await _client.chat.completions.create(
        model=_MODEL,
        messages=messages,
        stream=True,
    )

    async for chunk in stream:
        content = chunk.choices[0].delta.content or ""
        if content:
            yield f"data: {json.dumps({'content': content})}\n\n"

    yield "data: [DONE]\n\n"


async def get_summary(title: str, content: str) -> str:
    """Returns a 1-2 sentence(s) summary of the note."""
    response = await _client.chat.completions.create(
        model=_MODEL,
        messages=[
            {
                "role": "system",
                "content": "Summarize this note in 1-2 concise sentence(s). Be technical and precise.",
            },
            {"role": "user", "content": f"Title: {title}\n\n{content}"},
        ],
    )
    return response.choices[0].message.content.strip()

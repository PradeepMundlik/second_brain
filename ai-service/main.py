from dotenv import load_dotenv
load_dotenv()  # must happen before any module imports os.getenv()

from fastapi import FastAPI, Request
from routers import embed, search, chat, summarize
from services import vector_store

app = FastAPI(title="Second Brain AI Service")

app.include_router(embed.router)
app.include_router(search.router)
app.include_router(chat.router)
app.include_router(summarize.router)


@app.on_event("startup")
def startup():
    vector_store.ensure_collection()


# Debug middleware removed — service is stable. Re-add if troubleshooting is needed.


@app.get("/health")
def health():
    return {"status": "ok"}

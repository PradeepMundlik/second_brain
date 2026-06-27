from sentence_transformers import SentenceTransformer

# Downloads ~22MB on first run, then cached at ~/.cache/huggingface/
# 384-dimensional vectors, fast on CPU, no API key needed
_model = SentenceTransformer("all-MiniLM-L6-v2")


def embed(texts: list[str]) -> list[list[float]]:
    """Batch-embeds a list of texts. Returns a list of 384-dim float vectors."""
    return _model.encode(texts, convert_to_numpy=True).tolist()

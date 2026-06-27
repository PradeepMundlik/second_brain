def chunk(title: str, content: str, size: int = 500, overlap: int = 50) -> list[str]:
    """
    Sliding window chunker. Prepends the note title to the first chunk so that
    every retrieved chunk carries context about what note it came from.

    Why overlap? The boundary between two chunks might split a sentence mid-thought.
    Repeating the last `overlap` characters in the next chunk means no idea is cut off.
    """
    text = f"Title: {title}\n\n{content}"

    if len(text) <= size:
        return [text]

    chunks: list[str] = []
    start = 0
    while start < len(text):
        end = min(start + size, len(text))
        chunks.append(text[start:end])
        if end == len(text):
            break
        start += size - overlap

    return chunks

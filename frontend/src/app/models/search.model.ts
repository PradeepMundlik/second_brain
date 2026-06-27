export interface SearchResult {
  noteId: number;
  title: string;
  excerpt: string;  // the specific chunk that matched — shown as snippet under the title
  score: number;    // cosine similarity 0–1
  tags: string[];
}

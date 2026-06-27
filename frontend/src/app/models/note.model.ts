export interface Note {
  id: number;
  title: string;
  content: string;
  summary?: string;
  tags: string[];
  createdAt: string;
  updatedAt: string;
}

export interface NoteRequest {
  title: string;
  content: string;
  tags: string[];
}

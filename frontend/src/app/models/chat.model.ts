export interface ChatMessage {
  role: 'user' | 'assistant';
  content: string;
}

export interface SourceCard {
  noteId: number;
  title: string;
  excerpt: string;
  score: number;
  tags: string[];
}

export interface ChatEvent {
  type: 'sources' | 'chunk';
  data: SourceCard[] | { content: string };
}

export interface DisplayMessage {
  role: 'user' | 'assistant';
  content: string;
  sources?: SourceCard[];
}

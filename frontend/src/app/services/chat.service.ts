import { Injectable, NgZone } from '@angular/core';
import { Observable } from 'rxjs';
import { ChatEvent, ChatMessage } from '../models/chat.model';

export interface ChatRequest {
  query: string;
  history: ChatMessage[];
}

@Injectable({ providedIn: 'root' })
export class ChatService {
  constructor(private zone: NgZone) {}

  chat(req: ChatRequest): Observable<ChatEvent> {
    return new Observable(observer => {
      fetch('/api/chat', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(req),
      })
        .then(async res => {
          const reader = res.body!.getReader();
          const decoder = new TextDecoder();
          let buffer = '';
          let lastEventName = '';
          while (true) {
            const { done, value } = await reader.read();
            if (done) {
              this.zone.run(() => observer.complete());
              break;
            }
            buffer += decoder.decode(value, { stream: true });
            const lines = buffer.split('\n');
            buffer = lines.pop()!;

            for (const line of lines) {
              console.log('SSE line:', line);
              if (line.startsWith('event:')) {
                lastEventName = line.slice(6).trim();
              } else if (line.startsWith('data:')) {
                const raw = line.slice(5).trim();
                if (raw === '[DONE]') {
                  this.zone.run(() => observer.complete());
                  return;
                }
                try {
                  const parsed = JSON.parse(raw);
                  this.zone.run(() => {
                    observer.next({
                      type: (lastEventName || 'chunk') as 'sources' | 'chunk',
                      data: parsed,
                    });
                  });
                } catch (e) {
                  console.error('SSE parse error, raw:', raw, e);
                }
                lastEventName = '';
              }
            }
          }
        })
        .catch(err => this.zone.run(() => observer.error(err)));
    });
  }
}

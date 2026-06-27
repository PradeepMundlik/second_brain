import { Component, signal, ElementRef, ViewChild, AfterViewChecked } from '@angular/core';
import { ChatService, ChatRequest } from '../../services/chat.service';
import { ChatMessage, ChatEvent, DisplayMessage, SourceCard } from '../../models/chat.model';

@Component({
  selector: 'app-chat-page',
  standalone: true,
  imports: [],
  templateUrl: './chat-page.component.html',
  styleUrl: './chat-page.component.css',
})
export class ChatPageComponent implements AfterViewChecked {
  @ViewChild('messagesEnd') private messagesEnd!: ElementRef;

  messages = signal<DisplayMessage[]>([]);
  isStreaming = signal(false);
  activePopupKey = signal<string | null>(null);
  inputValue = '';

  constructor(private chatService: ChatService) {}

  togglePopup(event: MouseEvent, key: string): void {
    event.stopPropagation();
    this.activePopupKey.update(cur => cur === key ? null : key);
  }

  ngAfterViewChecked() {
    this.messagesEnd?.nativeElement?.scrollIntoView({ behavior: 'smooth' });
  }

  send() {
    const query = this.inputValue.trim();
    if (!query || this.isStreaming()) return;

    // Capture history of completed turns before this one
    const history: ChatMessage[] = this.messages()
      .filter(m => m.content)
      .map(m => ({ role: m.role, content: m.content }));

    this.inputValue = '';
    this.isStreaming.set(true);

    this.messages.update(msgs => [
      ...msgs,
      { role: 'user', content: query },
      { role: 'assistant', content: '' },
    ]);

    const req: ChatRequest = { query, history };

    this.chatService.chat(req).subscribe({
      next: (event: ChatEvent) => {
        if (event.type === 'sources') {
          this.messages.update(msgs => {
            const updated = [...msgs];
            updated[updated.length - 1] = {
              ...updated[updated.length - 1],
              sources: event.data as SourceCard[],
            };
            return updated;
          });
        } else {
          const chunk = (event.data as { content: string }).content;
          this.messages.update(msgs => {
            const updated = [...msgs];
            updated[updated.length - 1] = {
              ...updated[updated.length - 1],
              content: updated[updated.length - 1].content + chunk,
            };
            return updated;
          });
        }
      },
      error: err => {
        console.error('Chat error', err);
        this.isStreaming.set(false);
      },
      complete: () => this.isStreaming.set(false),
    });
  }

  onKeydown(event: KeyboardEvent) {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      this.send();
    }
  }

  clearChat() {
    this.messages.set([]);
  }
}

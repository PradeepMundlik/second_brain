import { Component, EventEmitter, Input, Output, signal } from '@angular/core';
import { Note, NoteRequest } from '../../models/note.model';
import { NoteFormComponent } from '../note-form/note-form.component';

@Component({
  selector: 'app-note-card',
  standalone: true,
  imports: [NoteFormComponent],
  templateUrl: './note-card.component.html',
})
export class NoteCardComponent {
  @Input({ required: true }) note!: Note;
  @Output() update = new EventEmitter<NoteRequest>();
  @Output() delete = new EventEmitter<void>();

  editing = signal(false);

  get preview(): string {
    return this.note.content.length > 200
      ? this.note.content.slice(0, 200) + '...'
      : this.note.content;
  }

  get formattedDate(): string {
    return new Date(this.note.createdAt).toLocaleDateString();
  }

  onUpdate(req: NoteRequest) {
    this.update.emit(req);
    this.editing.set(false);
  }
}

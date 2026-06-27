import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NoteRequest } from '../../models/note.model';

@Component({
  selector: 'app-note-form',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './note-form.component.html',
})
export class NoteFormComponent implements OnInit {
  @Input() initial?: NoteRequest;
  @Input() submitLabel = 'Save';
  @Output() formSubmit = new EventEmitter<NoteRequest>();
  @Output() cancel = new EventEmitter<void>();

  title = '';
  content = '';
  tagInput = '';

  ngOnInit() {
    if (this.initial) {
      this.title = this.initial.title;
      this.content = this.initial.content;
      this.tagInput = this.initial.tags.join(', ');
    }
  }

  onSubmit() {
    const tags = this.tagInput.split(',').map(t => t.trim()).filter(Boolean);
    this.formSubmit.emit({ title: this.title, content: this.content, tags });
  }
}

import { Component, OnInit, signal } from '@angular/core';
import { Note, NoteRequest } from '../../models/note.model';
import { SearchResult } from '../../models/search.model';
import { NoteService } from '../../services/note.service';
import { SearchService } from '../../services/search.service';
import { NoteCardComponent } from '../note-card/note-card.component';
import { NoteFormComponent } from '../note-form/note-form.component';
import { SearchBarComponent } from '../search-bar/search-bar.component';
import { SearchResultCardComponent } from '../search-result-card/search-result-card.component';

@Component({
  selector: 'app-notes-page',
  standalone: true,
  imports: [NoteCardComponent, NoteFormComponent, SearchBarComponent, SearchResultCardComponent],
  templateUrl: './notes-page.component.html',
})
export class NotesPageComponent implements OnInit {
  notes = signal<Note[]>([]);
  loading = signal(true);
  error = signal<string | null>(null);
  showForm = signal(false);

  searchResults = signal<SearchResult[]>([]);
  isSearching = signal(false);
  searchActive = signal(false);

  constructor(
    private noteService: NoteService,
    private searchService: SearchService,
  ) {}

  ngOnInit() {
    this.loadNotes();
  }

  loadNotes() {
    this.loading.set(true);
    this.noteService.getAll().subscribe({
      next: (notes) => { this.notes.set(notes); this.loading.set(false); },
      error: () => {
        this.error.set('Failed to load notes. Is the backend running?');
        this.loading.set(false);
      },
    });
  }

  onCreate(req: NoteRequest) {
    this.noteService.create(req).subscribe((created) => {
      this.notes.update(notes => [created, ...notes]);
      this.showForm.set(false);
    });
  }

  onUpdate(id: number, req: NoteRequest) {
    this.noteService.update(id, req).subscribe((updated) => {
      this.notes.update(notes => notes.map(n => n.id === id ? updated : n));
    });
  }

  onDelete(id: number) {
    if (!confirm('Delete this note?')) return;
    this.noteService.delete(id).subscribe(() => {
      this.notes.update(notes => notes.filter(n => n.id !== id));
    });
  }

  toggleForm() {
    this.showForm.update(v => !v);
  }

  onSearch(query: string) {
    this.isSearching.set(true);
    this.searchActive.set(true);
    this.searchService.search(query).subscribe({
      next: (results) => { this.searchResults.set(results); this.isSearching.set(false); },
      error: () => { this.isSearching.set(false); },
    });
  }

  onClearSearch() {
    this.searchActive.set(false);
    this.searchResults.set([]);
  }
}

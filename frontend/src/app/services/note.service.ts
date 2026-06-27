import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Note, NoteRequest } from '../models/note.model';

@Injectable({ providedIn: 'root' })
export class NoteService {
  private readonly BASE = '/api/notes';

  constructor(private http: HttpClient) {}

  getAll(): Observable<Note[]> {
    return this.http.get<Note[]>(this.BASE);
  }

  create(req: NoteRequest): Observable<Note> {
    return this.http.post<Note>(this.BASE, req);
  }

  update(id: number, req: NoteRequest): Observable<Note> {
    return this.http.put<Note>(`${this.BASE}/${id}`, req);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.BASE}/${id}`);
  }
}

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SearchResult } from '../models/search.model';

@Injectable({ providedIn: 'root' })
export class SearchService {
  private readonly BASE = '/api/search';

  constructor(private http: HttpClient) {}

  search(query: string): Observable<SearchResult[]> {
    return this.http.post<SearchResult[]>(this.BASE, { query });
  }
}

import { Component, Input } from '@angular/core';
import { SearchResult } from '../../models/search.model';

@Component({
  selector: 'app-search-result-card',
  standalone: true,
  templateUrl: './search-result-card.component.html',
})
export class SearchResultCardComponent {
  @Input({ required: true }) result!: SearchResult;

  get scorePercent(): string {
    return (this.result.score * 100).toFixed(0) + '%';
  }
}

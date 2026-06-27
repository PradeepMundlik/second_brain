import { Component, EventEmitter, Output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged, filter } from 'rxjs/operators';

@Component({
  selector: 'app-search-bar',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './search-bar.component.html',
})
export class SearchBarComponent {
  @Output() search = new EventEmitter<string>();
  @Output() clear = new EventEmitter<void>();

  query = '';
  private input$ = new Subject<string>();

  constructor() {
    // Debounce: only emit after the user stops typing for 400ms
    // distinctUntilChanged: don't re-search if the query didn't change
    this.input$.pipe(
      debounceTime(400),
      distinctUntilChanged(),
      filter(q => q.trim().length >= 2),
    ).subscribe(q => this.search.emit(q.trim()));
  }

  onInput(value: string) {
    this.query = value;
    if (value.trim().length === 0) {
      this.clear.emit();
    } else {
      this.input$.next(value);
    }
  }

  onClear() {
    this.query = '';
    this.clear.emit();
  }
}

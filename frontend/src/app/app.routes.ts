import { Routes } from '@angular/router';
import { NotesPageComponent } from './components/notes-page/notes-page.component';
import { ChatPageComponent } from './components/chat-page/chat-page.component';

export const routes: Routes = [
  { path: '', component: NotesPageComponent },
  { path: 'chat', component: ChatPageComponent },
];

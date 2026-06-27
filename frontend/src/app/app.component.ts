import { Component } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <nav class="main-nav">
      <a routerLink="/" routerLinkActive="nav-active" [routerLinkActiveOptions]="{exact: true}">Notes</a>
      <a routerLink="/chat" routerLinkActive="nav-active">Chat</a>
    </nav>
    <router-outlet />
  `,
})
export class AppComponent {}

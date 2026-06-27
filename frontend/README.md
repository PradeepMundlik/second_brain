# Second Brain — Frontend

Angular 19 SPA for the Engineering Second Brain. Communicates with the Spring Boot backend via REST and SSE streaming.

## Stack

- **Angular 19** — standalone components, signals, `@for` control flow
- **Zone.js** — change detection (`eventCoalescing: false` to prevent CD batching during SSE streams)
- **fetch API** — SSE streaming for chat (EventSource doesn't support POST)
- **Angular Router** — `/` (notes), `/chat`

## Structure

```
src/app/
├── app.config.ts                  # Zone CD config, router, HttpClient
├── app.routes.ts
├── app.component.ts/html/css      # Shell with nav bar
├── components/
│   ├── notes-page/                # Notes list + create/edit/delete
│   │   ├── note-card/             # Individual note card with AI summary
│   │   └── note-form/             # Create/edit form
│   └── chat-page/                 # RAG chat UI
│       ├── chat-page.component.ts
│       ├── chat-page.component.html
│       └── chat-page.component.css
├── services/
│   ├── note.service.ts            # Notes CRUD via HttpClient
│   └── chat.service.ts            # fetch-based SSE → Observable<ChatEvent>
└── models/
    ├── note.model.ts
    └── chat.model.ts
```

## Development

```bash
npm install
npm start          # serves on http://localhost:4200
npm run build      # production build → dist/
npm test           # karma unit tests
```

The dev server proxies `/api/*` to `http://localhost:8080` via [proxy.conf.json](proxy.conf.json).

## Key Implementation Notes

### SSE Streaming

`ChatService` uses the native `fetch` API with `response.body.getReader()` because `EventSource` only supports GET requests. Each SSE message is parsed as it arrives and emitted through an `Observable<ChatEvent>`.

Two event types arrive from the backend:
- `event: sources` — array of source cards (note title, excerpt, relevance score)
- `event: chunk` — one token of the AI response

### Change Detection

`eventCoalescing: false` in `app.config.ts` is required for streaming. With `eventCoalescing: true`, Angular batches consecutive change detection runs triggered from microtasks — meaning all chunks from a streaming response would be coalesced into a single CD cycle and only rendered when the stream ends.

### Source Card Popups

Each source card stores a composite key `"msgIdx-srcIdx"` in the `activePopupKey` signal. Clicking a card toggles its popup; the popup is positioned `bottom: calc(100% + 6px)` (above the card) to avoid overflow clipping from the scrollable messages area.

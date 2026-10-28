import { ChangeDetectionStrategy, Component, input } from '@angular/core';

export type NombreIcono = 'grid' | 'book' | 'arrow' | 'check' | 'clock' | 'search' | 'users' | 'lock' | 'info';

@Component({
  selector: 'app-icono',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { style: 'display:contents' },
  template: `
    <svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.65"
         stroke-linecap="round" stroke-linejoin="round">
      @switch (nombre()) {
        @case ('grid') {
          <svg:rect x="3" y="3" width="7" height="7" rx="1"/><svg:rect x="14" y="3" width="7" height="7" rx="1"/>
          <svg:rect x="3" y="14" width="7" height="7" rx="1"/><svg:rect x="14" y="14" width="7" height="7" rx="1"/>
        }
        @case ('book') { <svg:path d="M12 5v16M3 3h5a4 4 0 0 1 4 2 4 4 0 0 1 4-2h5v16h-5a4 4 0 0 0-4 2 4 4 0 0 0-4-2H3Z"/> }
        @case ('arrow') { <svg:path d="M5 12h14m-6-6 6 6-6 6"/> }
        @case ('check') { <svg:path d="m5 12 4 4L19 6"/> }
        @case ('clock') { <svg:circle cx="12" cy="12" r="9"/><svg:path d="M12 7v5l3 2"/> }
        @case ('search') { <svg:circle cx="10.5" cy="10.5" r="6.5"/><svg:path d="m16 16 5 5"/> }
        @case ('users') {
          <svg:circle cx="9" cy="7" r="3"/><svg:path d="M3 21v-3a6 6 0 0 1 12 0v3M17 4a3 3 0 0 1 0 6m2 11v-3a6 6 0 0 0-2-4"/>
        }
        @case ('lock') { <svg:rect x="5" y="10" width="14" height="11" rx="2"/><svg:path d="M8 10V7a4 4 0 0 1 8 0v3m-4 4v3"/> }
        @default { <svg:circle cx="12" cy="12" r="9"/><svg:path d="M12 11v6m0-10v1"/> }
      }
    </svg>`,
})
export class Icono {
  readonly nombre = input.required<NombreIcono>();
}

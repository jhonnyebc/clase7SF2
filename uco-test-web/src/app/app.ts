import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Aviso } from './compartido/aviso';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <a class="skip-link" href="#contenido">Ir al contenido</a>
    <router-outlet />
    @if (aviso.mensaje()) {
      <div id="toast" role="status" aria-live="polite">{{ aviso.mensaje() }}</div>
    }
  `,
})
export class App {
  protected readonly aviso = inject(Aviso);
}

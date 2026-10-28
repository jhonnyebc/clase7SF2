import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { takeUntilDestroyed, toSignal } from '@angular/core/rxjs-interop';
import { Router } from '@angular/router';
import { AuthService } from '@auth0/auth0-angular';
import { filter, map, take } from 'rxjs';
import { Icono } from '../../compartido/icono';

@Component({
  selector: 'app-login',
  imports: [Icono],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './login.html',
})
export class Login {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly redirigiendo = signal(false);
  protected readonly error = toSignal(
    this.auth.error$.pipe(map(error => `No se pudo iniciar sesión (${error.message}). Inténtalo de nuevo.`)),
    { initialValue: '' });

  constructor() {
    // Si ya hay sesion de Auth0 no tiene sentido mostrar el login.
    this.auth.isAuthenticated$.pipe(filter(Boolean), take(1), takeUntilDestroyed())
      .subscribe(() => this.router.navigateByUrl('/'));
  }

  protected ingresar() {
    this.redirigiendo.set(true);
    this.auth.loginWithRedirect({ appState: { target: '/' } });
  }
}

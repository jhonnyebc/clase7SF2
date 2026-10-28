import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '@auth0/auth0-angular';
import { map, take } from 'rxjs';

/** Sin sesion de Auth0 se va a la pantalla de login de la aplicacion. */
export const sesionGuard: CanActivateFn = () => {
  const router = inject(Router);
  return inject(AuthService).isAuthenticated$.pipe(
    take(1),
    map(autenticado => autenticado || router.createUrlTree(['/login'])),
  );
};

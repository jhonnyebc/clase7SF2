import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { authHttpInterceptorFn, provideAuth0 } from '@auth0/auth0-angular';
import { routes } from './app.routes';
import { configuracion } from './nucleo/configuracion';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes, withComponentInputBinding()),
    provideAuth0({
      domain: configuracion.auth0.domain,
      clientId: configuracion.auth0.clientId,
      authorizationParams: {
        // Debe coincidir con "Allowed Callback URLs" de la aplicacion en Auth0.
        redirect_uri: window.location.origin,
        // Pide un token para la API (no solo para el login).
        audience: configuracion.auth0.audience,
      },
      // Conserva la sesion al recargar la pagina.
      cacheLocation: 'localstorage',
      useRefreshTokens: true,
      useRefreshTokensFallback: true,
      // Agrega "Authorization: Bearer <token>" solo a las peticiones dirigidas a la API.
      httpInterceptor: {
        allowedList: [`${configuracion.apiUrl}/*`],
      },
    }),
    provideHttpClient(withInterceptors([authHttpInterceptorFn])),
  ],
};

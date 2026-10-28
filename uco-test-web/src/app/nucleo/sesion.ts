import { Injectable, inject, signal } from '@angular/core';
import { AuthService } from '@auth0/auth0-angular';
import { Api, mensajeDeError } from './api';
import { Docente, Grupo } from './modelos';

/** Docente autenticado y sus grupos, compartidos por todas las pantallas. */
@Injectable({ providedIn: 'root' })
export class Sesion {
  private readonly api = inject(Api);
  private readonly auth = inject(AuthService);

  readonly docente = signal<Docente | null>(null);
  readonly grupos = signal<Grupo[]>([]);
  readonly cargando = signal(false);
  readonly error = signal('');

  async cargar() {
    this.cargando.set(true);
    this.error.set('');
    try {
      const [docente, grupos] = await Promise.all([this.api.yo(), this.api.misGrupos()]);
      this.docente.set(docente);
      this.grupos.set(grupos);
    } catch (error) {
      this.docente.set(null);
      this.error.set(mensajeDeError(error));
    } finally {
      this.cargando.set(false);
    }
  }

  grupo(id: string) {
    return this.grupos().find(grupo => grupo.id === id) ?? null;
  }

  cerrarSesion() {
    this.docente.set(null);
    this.grupos.set([]);
    // Debe coincidir con "Allowed Logout URLs" de la aplicacion en Auth0.
    this.auth.logout({ logoutParams: { returnTo: window.location.origin } });
  }
}

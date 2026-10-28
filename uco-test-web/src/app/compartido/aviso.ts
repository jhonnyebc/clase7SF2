import { Injectable, signal } from '@angular/core';

/** Mensaje flotante de confirmacion ("Nota guardada", etc.). */
@Injectable({ providedIn: 'root' })
export class Aviso {
  readonly mensaje = signal('');
  private temporizador?: ReturnType<typeof setTimeout>;

  mostrar(mensaje: string) {
    clearTimeout(this.temporizador);
    this.mensaje.set(mensaje);
    this.temporizador = setTimeout(() => this.mensaje.set(''), 6000);
  }
}

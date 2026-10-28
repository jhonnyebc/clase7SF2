import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { firstValueFrom } from 'rxjs';
import { configuracion } from './configuracion';
import {
  Docente, EstudianteNota, Evaluacion, Grupo, Ponderacion, PromediosGrupo,
} from './modelos';

/**
 * Llamadas a la API. El token de Auth0 lo agrega automaticamente el interceptor
 * configurado en app.config.ts, por eso aqui no aparece.
 */
@Injectable({ providedIn: 'root' })
export class Api {
  private readonly http = inject(HttpClient);
  private readonly base = configuracion.apiUrl;

  yo() {
    return firstValueFrom(this.http.get<Docente>(`${this.base}/auth/me`));
  }

  misGrupos() {
    return firstValueFrom(this.http.get<Grupo[]>(`${this.base}/mis-grupos`));
  }

  evaluaciones(idGrupo: string) {
    return firstValueFrom(this.http.get<Evaluacion[]>(`${this.base}/grupos/${idGrupo}/notas`));
  }

  ponderacion(idGrupo: string) {
    return firstValueFrom(this.http.get<Ponderacion>(`${this.base}/grupos/${idGrupo}/ponderacion`));
  }

  promedios(idGrupo: string) {
    return firstValueFrom(this.http.get<PromediosGrupo>(`${this.base}/grupos/${idGrupo}/promedios`));
  }

  estudiantes(idNota: string) {
    return firstValueFrom(this.http.get<EstudianteNota[]>(`${this.base}/notas/${idNota}/estudiantes`));
  }

  registrarNota(idEstudiante: string, idNota: string, valor: number) {
    return firstValueFrom(this.http.post<{ id: string; mensaje: string }>(
      `${this.base}/notas-estudiantes`, { idEstudiante, idNota, valor }));
  }
}

/** Convierte cualquier error de la API en un mensaje para mostrar al docente. */
export function mensajeDeError(error: unknown): string {
  if (error instanceof HttpErrorResponse) {
    if (error.status === 0) return 'No pudimos conectar con el servidor. Revisa que la API esté encendida.';
    if (error.status === 401) return 'Tu sesión terminó. Inicia sesión de nuevo.';
    if (error.status >= 500) return 'No pudimos completar la operación. Inténtalo de nuevo.';
    return error.error?.error || error.error?.mensaje || 'No pudimos completar la operación.';
  }
  return 'Ocurrió un error inesperado. Inténtalo de nuevo.';
}

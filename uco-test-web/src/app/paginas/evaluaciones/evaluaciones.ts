import { ChangeDetectionStrategy, Component, computed, effect, inject, input, signal, untracked } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { RouterLink } from '@angular/router';
import { Icono } from '../../compartido/icono';
import { fecha, numero } from '../../compartido/formato';
import { Api, mensajeDeError } from '../../nucleo/api';
import { Evaluacion, Ponderacion } from '../../nucleo/modelos';
import { Sesion } from '../../nucleo/sesion';

@Component({
  selector: 'app-evaluaciones',
  imports: [RouterLink, Icono],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './evaluaciones.html',
})
export class Evaluaciones {
  private readonly api = inject(Api);
  private readonly sesion = inject(Sesion);
  private readonly titulo = inject(Title);

  /** Viene de la ruta grupo/:idGrupo */
  readonly idGrupo = input.required<string>();

  protected readonly grupo = computed(() => this.sesion.grupo(this.idGrupo()));
  protected readonly evaluaciones = signal<Evaluacion[]>([]);
  protected readonly ponderacion = signal<Ponderacion | null>(null);
  protected readonly cargando = signal(true);
  protected readonly error = signal('');
  protected readonly abiertas = computed(() => this.evaluaciones().filter(e => e.vigente).length);
  protected readonly fecha = fecha;
  protected readonly numero = numero;
  private version = 0;

  constructor() {
    effect(() => {
      const id = this.idGrupo();
      untracked(() => this.cargar(id));
    });
  }

  protected async cargar(idGrupo = this.idGrupo()) {
    const version = ++this.version;
    this.cargando.set(true);
    this.error.set('');
    try {
      const grupo = this.sesion.grupo(idGrupo);
      if (!grupo) throw new Error('Este grupo no está disponible en tu cuenta.');
      this.titulo.setTitle(`${grupo.nombre} · Aula`);
      const [evaluaciones, ponderacion] = await Promise.all([this.api.evaluaciones(idGrupo), this.api.ponderacion(idGrupo)]);
      if (version !== this.version) return;
      this.evaluaciones.set(evaluaciones);
      this.ponderacion.set(ponderacion);
    } catch (error) {
      if (version !== this.version) return;
      this.error.set(error instanceof Error && !('status' in error) ? error.message : mensajeDeError(error));
    } finally {
      if (version === this.version) this.cargando.set(false);
    }
  }
}

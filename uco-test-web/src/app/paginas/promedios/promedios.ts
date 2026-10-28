import { ChangeDetectionStrategy, Component, computed, effect, inject, input, signal, untracked } from '@angular/core';
import { Title } from '@angular/platform-browser';
import { RouterLink } from '@angular/router';
import { Icono } from '../../compartido/icono';
import { aporte, iniciales, normalizar, notaDosDecimales, numero } from '../../compartido/formato';
import { Api, mensajeDeError } from '../../nucleo/api';
import { EstadoPromedio, PromediosGrupo } from '../../nucleo/modelos';
import { Sesion } from '../../nucleo/sesion';

@Component({
  selector: 'app-promedios',
  imports: [RouterLink, Icono],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './promedios.html',
})
export class Promedios {
  private readonly api = inject(Api);
  private readonly sesion = inject(Sesion);
  private readonly titulo = inject(Title);

  /** Viene de la ruta grupo/:idGrupo/promedios */
  readonly idGrupo = input.required<string>();

  protected readonly grupo = computed(() => this.sesion.grupo(this.idGrupo()));
  protected readonly datos = signal<PromediosGrupo | null>(null);
  protected readonly cargando = signal(true);
  protected readonly error = signal('');
  protected readonly busqueda = signal('');
  protected readonly filtro = signal<'todos' | EstadoPromedio>('todos');

  protected readonly estudiantes = computed(() => this.datos()?.estudiantes ?? []);
  protected readonly definitivas = computed(() => this.estudiantes().filter(e => e.estado === 'DEFINITIVA').length);
  protected readonly parciales = computed(() => this.estudiantes().filter(e => e.estado === 'PARCIAL').length);
  protected readonly filtrados = computed(() => {
    const texto = normalizar(this.busqueda().trim());
    const filtro = this.filtro();
    return this.estudiantes().filter(e =>
      normalizar(`${e.nombres} ${e.apellidos} ${e.identificacion}`).includes(texto)
      && (filtro === 'todos' || e.estado === filtro));
  });

  protected readonly etiquetas: Record<EstadoPromedio, string> = {
    SIN_NOTAS: 'Sin notas', PARCIAL: 'Parcial', DEFINITIVA: 'Definitiva', CONFIGURACION_INVALIDA: 'Sin cálculo',
  };
  protected readonly numero = numero;
  protected readonly aporte = aporte;
  protected readonly iniciales = iniciales;
  protected readonly notaDosDecimales = notaDosDecimales;
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
      this.titulo.setTitle(`Promedios · ${grupo.nombre} · Aula`);
      const datos = await this.api.promedios(idGrupo);
      if (version !== this.version) return;
      this.datos.set(datos);
    } catch (error) {
      if (version !== this.version) return;
      this.error.set(error instanceof Error ? error.message : mensajeDeError(error));
    } finally {
      if (version === this.version) this.cargando.set(false);
    }
  }
}

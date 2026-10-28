import {
  ChangeDetectionStrategy, Component, ElementRef, computed, effect, inject, input, signal, untracked, viewChild,
} from '@angular/core';
import { Title } from '@angular/platform-browser';
import { RouterLink } from '@angular/router';
import { Aviso } from '../../compartido/aviso';
import { Icono } from '../../compartido/icono';
import { fecha, iniciales, leerNota, normalizar, notaUnDecimal, numero } from '../../compartido/formato';
import { Api, mensajeDeError } from '../../nucleo/api';
import { EstudianteNota, Evaluacion } from '../../nucleo/modelos';
import { Sesion } from '../../nucleo/sesion';

type Filtro = 'todos' | 'pendientes' | 'registradas' | 'inactivos';

@Component({
  selector: 'app-estudiantes',
  imports: [RouterLink, Icono],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './estudiantes.html',
})
export class Estudiantes {
  private readonly api = inject(Api);
  private readonly sesion = inject(Sesion);
  private readonly aviso = inject(Aviso);
  private readonly titulo = inject(Title);

  /** Vienen de la ruta grupo/:idGrupo/evaluacion/:idNota */
  readonly idGrupo = input.required<string>();
  readonly idNota = input.required<string>();

  protected readonly grupo = computed(() => this.sesion.grupo(this.idGrupo()));
  protected readonly evaluacion = signal<Evaluacion | null>(null);
  protected readonly estudiantes = signal<EstudianteNota[]>([]);
  protected readonly cargando = signal(true);
  protected readonly error = signal('');
  protected readonly busqueda = signal('');
  protected readonly filtro = signal<Filtro>('todos');

  protected readonly registradas = computed(() => this.estudiantes().filter(e => e.registrada).length);
  protected readonly pendientes = computed(() => this.estudiantes().filter(e => e.activo && !e.registrada).length);
  protected readonly filtrados = computed(() => {
    const texto = normalizar(this.busqueda().trim());
    const filtro = this.filtro();
    return this.estudiantes().filter(e =>
      normalizar(`${e.nombres} ${e.apellidos} ${e.identificacion}`).includes(texto)
      && (filtro === 'todos'
        || (filtro === 'pendientes' && e.activo && !e.registrada)
        || (filtro === 'registradas' && e.registrada)
        || (filtro === 'inactivos' && !e.activo)));
  });

  // Dialogo de registro
  private readonly dialogo = viewChild<ElementRef<HTMLDialogElement>>('dialogo');
  private readonly campoNota = viewChild<ElementRef<HTMLInputElement>>('campoNota');
  protected readonly seleccionado = signal<EstudianteNota | null>(null);
  protected readonly valorTexto = signal('');
  protected readonly guardando = signal(false);
  protected readonly errorDialogo = signal('');

  protected readonly fecha = fecha;
  protected readonly numero = numero;
  protected readonly iniciales = iniciales;
  protected readonly notaUnDecimal = notaUnDecimal;
  private version = 0;

  constructor() {
    effect(() => {
      const idGrupo = this.idGrupo();
      const idNota = this.idNota();
      untracked(() => this.cargar(idGrupo, idNota));
    });
  }

  protected async cargar(idGrupo = this.idGrupo(), idNota = this.idNota()) {
    const version = ++this.version;
    this.cargando.set(true);
    this.error.set('');
    try {
      const grupo = this.sesion.grupo(idGrupo);
      if (!grupo) throw new Error('Este grupo no está disponible en tu cuenta.');
      const [evaluaciones, estudiantes] = await Promise.all([this.api.evaluaciones(idGrupo), this.api.estudiantes(idNota)]);
      if (version !== this.version) return;
      const evaluacion = evaluaciones.find(e => e.id === idNota);
      if (!evaluacion) throw new Error('Esta evaluación no está disponible.');
      this.titulo.setTitle(`${evaluacion.nombre} · ${grupo.nombre} · Aula`);
      this.evaluacion.set(evaluacion);
      this.estudiantes.set(estudiantes);
    } catch (error) {
      if (version !== this.version) return;
      this.error.set(error instanceof Error && !('status' in error) ? error.message : mensajeDeError(error));
    } finally {
      if (version === this.version) this.cargando.set(false);
    }
  }

  protected puedeRegistrar(estudiante: EstudianteNota) {
    return !estudiante.registrada && estudiante.activo && !!this.evaluacion()?.vigente;
  }

  protected abrirRegistro(estudiante: EstudianteNota) {
    if (!this.puedeRegistrar(estudiante)) return;
    this.seleccionado.set(estudiante);
    this.valorTexto.set('');
    this.errorDialogo.set('');
    this.dialogo()?.nativeElement.showModal();
    setTimeout(() => this.campoNota()?.nativeElement.focus());
  }

  protected cerrarRegistro() {
    if (!this.guardando()) this.dialogo()?.nativeElement.close();
  }

  protected evitarCierreMientrasGuarda(evento: Event) {
    if (this.guardando()) evento.preventDefault();
  }

  protected async guardar(evento: Event) {
    evento.preventDefault();
    const estudiante = this.seleccionado();
    const evaluacion = this.evaluacion();
    if (this.guardando() || !estudiante || !evaluacion) return;
    const valor = leerNota(this.valorTexto());
    if (valor == null) {
      this.errorDialogo.set('Escribe una nota entre 0 y 5, con máximo un decimal.');
      return;
    }
    this.guardando.set(true);
    this.errorDialogo.set('');
    try {
      await this.api.registrarNota(estudiante.idEstudiante, evaluacion.id, valor);
      this.estudiantes.update(lista => lista.map(e =>
        e.idEstudiante === estudiante.idEstudiante ? { ...e, valor, registrada: true } : e));
      this.guardando.set(false);
      this.dialogo()?.nativeElement.close();
      this.aviso.mostrar(`Nota de ${estudiante.nombres} guardada correctamente.`);
    } catch (error) {
      this.errorDialogo.set(mensajeDeError(error));
    } finally {
      this.guardando.set(false);
    }
  }
}

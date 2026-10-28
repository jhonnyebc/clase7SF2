import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Icono } from '../../compartido/icono';
import { Sesion } from '../../nucleo/sesion';

@Component({
  selector: 'app-grupos',
  imports: [RouterLink, Icono],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './grupos.html',
})
export class Grupos {
  protected readonly sesion = inject(Sesion);
  protected readonly primerNombre = computed(() => this.sesion.docente()?.nombres.split(' ')[0] ?? '');
}

import { ChangeDetectionStrategy, Component, OnInit, inject } from '@angular/core';
import { RouterLink, RouterOutlet } from '@angular/router';
import { Icono } from '../../compartido/icono';
import { iniciales } from '../../compartido/formato';
import { Sesion } from '../../nucleo/sesion';

/** Marco comun de las pantallas internas: barra lateral, barra superior y contenido. */
@Component({
  selector: 'app-shell',
  imports: [RouterOutlet, RouterLink, Icono],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './shell.html',
})
export class Shell implements OnInit {
  protected readonly sesion = inject(Sesion);
  protected readonly iniciales = iniciales;

  ngOnInit() {
    if (!this.sesion.docente()) this.sesion.cargar();
  }
}

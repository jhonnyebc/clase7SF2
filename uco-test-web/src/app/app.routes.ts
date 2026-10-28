import { Routes } from '@angular/router';
import { sesionGuard } from './nucleo/sesion-guard';
import { Login } from './paginas/login/login';
import { Shell } from './paginas/shell/shell';
import { Grupos } from './paginas/grupos/grupos';
import { Evaluaciones } from './paginas/evaluaciones/evaluaciones';
import { Estudiantes } from './paginas/estudiantes/estudiantes';
import { Promedios } from './paginas/promedios/promedios';

export const routes: Routes = [
  { path: 'login', component: Login, title: 'Iniciar sesión · Aula' },
  {
    path: '',
    component: Shell,
    canActivate: [sesionGuard],
    children: [
      { path: '', component: Grupos, title: 'Mis grupos · Aula' },
      { path: 'grupo/:idGrupo', component: Evaluaciones, title: 'Evaluaciones · Aula' },
      { path: 'grupo/:idGrupo/promedios', component: Promedios, title: 'Promedios · Aula' },
      { path: 'grupo/:idGrupo/evaluacion/:idNota', component: Estudiantes, title: 'Registro de notas · Aula' },
    ],
  },
  { path: '**', redirectTo: '' },
];

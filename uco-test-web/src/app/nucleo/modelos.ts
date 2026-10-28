export interface Docente {
  idDocente: string;
  nombres: string;
  apellidos: string;
  correo: string;
}

export interface Grupo {
  id: string;
  nombre: string;
}

export interface Evaluacion {
  id: string;
  nombre: string;
  fechaRegistroDesde: string;
  fechaRegistroHasta: string;
  vigente: boolean;
  porcentaje: number | null;
}

export interface Ponderacion {
  total: number;
  sinPorcentaje: number;
  valida: boolean;
  mensaje: string;
}

export interface EstudianteNota {
  idEstudiante: string;
  identificacion: string;
  nombres: string;
  apellidos: string;
  activo: boolean;
  valor: number | null;
  registrada: boolean;
}

export type EstadoPromedio = 'SIN_NOTAS' | 'PARCIAL' | 'DEFINITIVA' | 'CONFIGURACION_INVALIDA';

export interface DetallePromedio {
  idNota: string;
  nombre: string;
  porcentaje: number | null;
  valor: number | null;
  aporte: number | null;
  vigente: boolean;
}

export interface EstudiantePromedio {
  idEstudiante: string;
  identificacion: string;
  nombres: string;
  apellidos: string;
  activo: boolean;
  estado: EstadoPromedio;
  acumulado: number | null;
  definitiva: number | null;
  porcentajeEvaluado: number | null;
  registradas: number;
  pendientes: number;
  evaluaciones: DetallePromedio[];
}

export interface PromediosGrupo {
  ponderacion: Ponderacion;
  estudiantes: EstudiantePromedio[];
}

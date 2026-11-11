export interface Operario {
  id: number;
  nombre: string;
  especialidad: string;
  activo: boolean;
}

export interface FranjaCupo {
  id: number;
  operarioId: number;
  operarioNombre: string;
  inicio: string;
  fin: string;
  cupoMaximo: number;
  ocupados: number;
  disponibles: number;
  llena: boolean;
}

export type TipoCita = 'ENTREGA' | 'RECOGIDA';
export type EstadoCita = 'AGENDADA' | 'CANCELADA' | 'CUMPLIDA';

export interface Cita {
  id: number;
  ordenId: number;
  operarioId: number;
  operarioNombre: string;
  franjaCupoId: number;
  franjaHorario: string;
  tipo: TipoCita;
  estado: EstadoCita;
  motivoCancelacion?: string;
  fechaCreacion: string;
  fechaCancelacion?: string;
}

export interface AgendarCitaRequest {
  ordenId: number;
  operarioId: number;
  franjaCupoId: number;
  tipo: TipoCita;
}

export interface CrearFranjaRequest {
  operarioId: number;
  inicio: string;
  fin: string;
  cupoMaximo?: number;
}

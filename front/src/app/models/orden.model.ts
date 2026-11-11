export type EstadoOrden = 'RECIBIDA' | 'EN_DIAGNOSTICO' | 'EN_REPARACION' | 'REPARADA' | 'CERRADA';
export type OrigenOrden = 'OPERARIO' | 'ASEGURADORA';

export interface Orden {
  id: number;
  clienteId: number;
  clienteNombre: string;
  clienteTelefono: string;
  vehiculoId: number;
  vehiculoPlaca: string;
  vehiculoModelo: string;
  trabajoSolicitado: string;
  aseguradora?: string;
  origen: OrigenOrden;
  idExterno?: string;
  estado: EstadoOrden;
  operarioId?: string;
  fechaCreacion: string;
  fechaActualizacion: string;
}

export interface CrearOrdenRequest {
  clienteId: number;
  vehiculoId: number;
  trabajoSolicitado: string;
  aseguradora?: string;
  origen: OrigenOrden;
  idExterno?: string;
}

export interface TransicionRequest {
  estadoNuevo: EstadoOrden;
  razon?: string;
}

export interface AuditoriaOrden {
  id: number;
  ordenId: number;
  operarioId: string;
  estadoAnterior?: EstadoOrden;
  estadoNuevo: EstadoOrden;
  razon?: string;
  momento: string;
}

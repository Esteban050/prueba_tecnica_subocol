export interface Cliente {
  id?: number;
  nombre: string;
  documento: string;
  telefono: string;
  email?: string;
  direccion?: string;
  fechaCreacion?: string;
}

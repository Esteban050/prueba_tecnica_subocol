export interface Vehiculo {
  id?: number;
  placa: string;
  marca: string;
  modelo: string;
  anio?: number;
  color?: string;
  clienteId: number;
  fechaCreacion?: string;
}

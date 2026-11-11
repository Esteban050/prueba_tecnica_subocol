export interface Notificacion {
  id: number;
  eventId: string;
  ordenId?: number;
  citaId?: number;
  canal: string;
  destinatarioTelefono: string;
  destinatarioNombre?: string;
  contenido: string;
  tipoEvento: string;
  motivo?: string;
  enviada: boolean;
  razonOmision?: string;
  fechaRegistro: string;
}

import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Notificacion } from '../models/notificacion.model';

@Injectable({
  providedIn: 'root'
})
export class NotificacionesService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = 'http://localhost:8083/api';

  listarNotificaciones(): Observable<Notificacion[]> {
    return this.http.get<Notificacion[]>(`${this.baseUrl}/notificaciones`);
  }

  listarPorOrden(ordenId: number): Observable<Notificacion[]> {
    return this.http.get<Notificacion[]>(`${this.baseUrl}/notificaciones/orden/${ordenId}`);
  }

  listarPorTelefono(telefono: string): Observable<Notificacion[]> {
    return this.http.get<Notificacion[]>(`${this.baseUrl}/notificaciones/telefono/${encodeURIComponent(telefono)}`);
  }
}

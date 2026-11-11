import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Operario, FranjaCupo, Cita, AgendarCitaRequest, CrearFranjaRequest } from '../models/agenda.model';
import { StateService } from './state.service';

@Injectable({
  providedIn: 'root'
})
export class AgendaService {
  private readonly http = inject(HttpClient);
  private readonly state = inject(StateService);
  private readonly baseUrl = 'http://localhost:8082/api';

  private getHeaders(): HttpHeaders {
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'X-User-Id': this.state.activeUserId()
    });
  }

  // Operarios
  listarOperarios(): Observable<Operario[]> {
    return this.http.get<Operario[]>(`${this.baseUrl}/operarios`);
  }

  registrarOperario(operario: Partial<Operario>): Observable<Operario> {
    return this.http.post<Operario>(`${this.baseUrl}/operarios`, operario, { headers: this.getHeaders() });
  }

  // Franjas y cupos
  listarFranjas(): Observable<FranjaCupo[]> {
    return this.http.get<FranjaCupo[]>(`${this.baseUrl}/franjas`);
  }

  listarFranjasDisponibles(): Observable<FranjaCupo[]> {
    return this.http.get<FranjaCupo[]>(`${this.baseUrl}/franjas/disponibles`);
  }

  crearFranja(request: CrearFranjaRequest): Observable<FranjaCupo> {
    return this.http.post<FranjaCupo>(`${this.baseUrl}/franjas`, request, { headers: this.getHeaders() });
  }

  // Citas
  agendarCita(request: AgendarCitaRequest): Observable<Cita> {
    return this.http.post<Cita>(`${this.baseUrl}/citas`, request, { headers: this.getHeaders() });
  }

  cancelarCita(id: number, motivo: string): Observable<Cita> {
    return this.http.delete<Cita>(`${this.baseUrl}/citas/${id}?motivo=${encodeURIComponent(motivo)}`, { headers: this.getHeaders() });
  }

  listarCitas(ordenId?: number, operarioId?: number, franjaId?: number): Observable<Cita[]> {
    let url = `${this.baseUrl}/citas`;
    const params: string[] = [];
    if (ordenId) params.push(`ordenId=${ordenId}`);
    if (operarioId) params.push(`operarioId=${operarioId}`);
    if (franjaId) params.push(`franjaId=${franjaId}`);
    if (params.length > 0) {
      url += `?${params.join('&')}`;
    }
    return this.http.get<Cita[]>(url);
  }
}

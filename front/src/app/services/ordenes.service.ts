import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Cliente } from '../models/cliente.model';
import { Vehiculo } from '../models/vehiculo.model';
import { Orden, CrearOrdenRequest, TransicionRequest, AuditoriaOrden } from '../models/orden.model';
import { StateService } from './state.service';

@Injectable({
  providedIn: 'root'
})
export class OrdenesService {
  private readonly http = inject(HttpClient);
  private readonly state = inject(StateService);
  private readonly baseUrl = 'http://localhost:8081/api';

  private getHeaders(): HttpHeaders {
    return new HttpHeaders({
      'Content-Type': 'application/json',
      'X-User-Id': this.state.activeUserId()
    });
  }

  // Clientes
  registrarCliente(cliente: Cliente): Observable<Cliente> {
    return this.http.post<Cliente>(`${this.baseUrl}/clientes`, cliente, { headers: this.getHeaders() });
  }

  listarClientes(): Observable<Cliente> {
    return this.http.get<Cliente>(`${this.baseUrl}/clientes`);
  }

  // Vehiculos
  registrarVehiculo(vehiculo: Vehiculo): Observable<Vehiculo> {
    return this.http.post<Vehiculo>(`${this.baseUrl}/vehiculos`, vehiculo, { headers: this.getHeaders() });
  }

  listarVehiculos(): Observable<Vehiculo[]> {
    return this.http.get<Vehiculo[]>(`${this.baseUrl}/vehiculos`);
  }

  listarVehiculosPorCliente(clienteId: number): Observable<Vehiculo[]> {
    return this.http.get<Vehiculo[]>(`${this.baseUrl}/vehiculos/cliente/${clienteId}`);
  }

  // Ordenes
  crearOrden(request: CrearOrdenRequest): Observable<Orden> {
    return this.http.post<Orden>(`${this.baseUrl}/ordenes`, request, { headers: this.getHeaders() });
  }

  listarOrdenes(): Observable<Orden[]> {
    return this.http.get<Orden[]>(`${this.baseUrl}/ordenes`);
  }

  buscarOrdenPorId(id: number): Observable<Orden> {
    return this.http.get<Orden>(`${this.baseUrl}/ordenes/${id}`);
  }

  listarOrdenesPorCliente(clienteId: number): Observable<Orden[]> {
    return this.http.get<Orden[]>(`${this.baseUrl}/clientes/${clienteId}/ordenes`);
  }

  cambiarEstado(id: number, request: TransicionRequest): Observable<Orden> {
    return this.http.post<Orden>(`${this.baseUrl}/ordenes/${id}/transiciones`, request, { headers: this.getHeaders() });
  }

  obtenerAuditoria(id: number): Observable<AuditoriaOrden[]> {
    return this.http.get<AuditoriaOrden[]>(`${this.baseUrl}/ordenes/${id}/auditoria`);
  }
}

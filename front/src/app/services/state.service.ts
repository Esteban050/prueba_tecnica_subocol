import { Injectable, signal } from '@angular/core';

export interface ToastMessage {
  id: number;
  tipo: 'success' | 'error' | 'info' | 'warning';
  titulo: string;
  detalle: string;
}

@Injectable({
  providedIn: 'root'
})
export class StateService {
  // Operario seleccionado para enviar en encabezado X-User-Id
  readonly operariosDisponibles = [
    { id: 'OP-001', nombre: 'Carlos Mecánico', cargo: 'Operario Mecánica' },
    { id: 'OP-002', nombre: 'Ana Gómez', cargo: 'Especialista Frenos y Suspensión' },
    { id: 'OP-003', nombre: 'Pedro Supervisor', cargo: 'Supervisor de Taller' }
  ];

  readonly activeUserId = signal<string>('OP-001');
  readonly activeTab = signal<'pipeline' | 'clientes' | 'agenda' | 'whatsapp' | 'arquitectura'>('pipeline');
  readonly toasts = signal<ToastMessage[]>([]);

  private toastCounter = 0;

  setOperario(id: string) {
    this.activeUserId.set(id);
    this.showToast('info', 'Operario Cambiado', `Ahora actuando como ${id} en encabezado X-User-Id`);
  }

  getOperarioActual() {
    return this.operariosDisponibles.find(o => o.id === this.activeUserId()) || this.operariosDisponibles[0];
  }

  showToast(tipo: 'success' | 'error' | 'info' | 'warning', titulo: string, detalle: string) {
    const id = ++this.toastCounter;
    const toast: ToastMessage = { id, tipo, titulo, detalle };
    this.toasts.update(list => [...list, toast]);

    setTimeout(() => {
      this.dismissToast(id);
    }, 6000);
  }

  dismissToast(id: number) {
    this.toasts.update(list => list.filter(t => t.id !== id));
  }
}

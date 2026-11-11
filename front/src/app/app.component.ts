import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { StateService } from './services/state.service';
import { OrdenesService } from './services/ordenes.service';
import { AgendaService } from './services/agenda.service';
import { NotificacionesService } from './services/notificaciones.service';
import { Cliente } from './models/cliente.model';
import { Vehiculo } from './models/vehiculo.model';
import { Orden, EstadoOrden, OrigenOrden, AuditoriaOrden } from './models/orden.model';
import { Operario, FranjaCupo, Cita, TipoCita } from './models/agenda.model';
import { Notificacion } from './models/notificacion.model';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html'
})
export class AppComponent implements OnInit {
  public state = inject(StateService);
  private ordenesService = inject(OrdenesService);
  private agendaService = inject(AgendaService);
  private notificacionesService = inject(NotificacionesService);

  // Datos
  clientes = signal<Cliente[]>([]);
  vehiculos = signal<Vehiculo[]>([]);
  ordenes = signal<Orden[]>([]);
  operarios = signal<Operario[]>([]);
  franjas = signal<FranjaCupo[]>([]);
  citas = signal<Cita[]>([]);
  notificaciones = signal<Notificacion[]>([]);
  auditoriaOrden = signal<AuditoriaOrden[]>([]);

  // Estados de UI y modales
  loading = signal<boolean>(false);
  modalClienteAbierto = signal<boolean>(false);
  modalVehiculoAbierto = signal<boolean>(false);
  modalOrdenAbierto = signal<boolean>(false);
  modalReaperturaAbierto = signal<boolean>(false);
  modalAuditoriaAbierto = signal<boolean>(false);
  modalCitaAbierto = signal<boolean>(false);
  modalCancelarCitaAbierto = signal<boolean>(false);

  // Orden seleccionada para auditoría o reapertura
  ordenSeleccionada = signal<Orden | null>(null);
  citaSeleccionada = signal<Cita | null>(null);

  // Formularios
  formCliente: Cliente = { nombre: '', documento: '', telefono: '', email: '', direccion: '' };
  formVehiculo: Vehiculo = { placa: '', marca: '', modelo: '', anio: 2023, color: '', clienteId: 0 };
  formOrden = { clienteId: 0, vehiculoId: 0, trabajoSolicitado: '', aseguradora: '', origen: 'OPERARIO' as OrigenOrden, idExterno: '' };
  formReapertura = { razon: '' };
  formCita = { ordenId: 0, operarioId: 0, franjaCupoId: 0, tipo: 'ENTREGA' as TipoCita };
  formCancelarCita = { motivo: 'CANCELADA_POR_CLIENTE' };

  ngOnInit() {
    this.cargarTodo();
    // Polling ligero para refrescar notificaciones y citas cada 5 segundos
    setInterval(() => {
      this.cargarNotificaciones();
      this.cargarCitas();
      this.cargarFranjas();
    }, 5000);
  }

  cargarTodo() {
    this.loading.set(true);
    this.cargarClientes();
    this.cargarVehiculos();
    this.cargarOrdenes();
    this.cargarOperarios();
    this.cargarFranjas();
    this.cargarCitas();
    this.cargarNotificaciones();
    setTimeout(() => this.loading.set(false), 400);
  }

  cargarClientes() {
    this.ordenesService.listarClientes().subscribe({
      next: (data: any) => this.clientes.set(data),
      error: () => {}
    });
  }

  cargarVehiculos() {
    this.ordenesService.listarVehiculos().subscribe({
      next: (data) => this.vehiculos.set(data),
      error: () => {}
    });
  }

  cargarOrdenes() {
    this.ordenesService.listarOrdenes().subscribe({
      next: (data) => this.ordenes.set(data),
      error: () => {}
    });
  }

  cargarOperarios() {
    this.agendaService.listarOperarios().subscribe({
      next: (data) => {
        this.operarios.set(data);
        if (data.length === 0) {
          this.inicializarDatosDemo();
        }
      },
      error: () => {}
    });
  }

  cargarFranjas() {
    this.agendaService.listarFranjas().subscribe({
      next: (data) => this.franjas.set(data),
      error: () => {}
    });
  }

  cargarCitas() {
    this.agendaService.listarCitas().subscribe({
      next: (data) => this.citas.set(data),
      error: () => {}
    });
  }

  cargarNotificaciones() {
    this.notificacionesService.listarNotificaciones().subscribe({
      next: (data) => this.notificaciones.set(data),
      error: () => {}
    });
  }

  // Filtrado de órdenes por columna Kanban
  ordenesPorEstado(estado: EstadoOrden): Orden[] {
    return this.ordenes().filter(o => o.estado === estado);
  }

  // Transiciones de estado
  avanzarEstado(orden: Orden, estadoDestino: EstadoOrden) {
    if (estadoDestino === 'CERRADA') {
      if (!confirm(`¿Confirma el cierre de la orden #${orden.id}?\n\nAl cerrar la orden, cualquier cita pendiente será cancelada automáticamente con motivo 'CIERRE_ORDEN' y se liberará su cupo, sin molestar al cliente por WhatsApp.`)) {
        return;
      }
    }

    this.ordenesService.cambiarEstado(orden.id, { estadoNuevo: estadoDestino, razon: 'Avance normal en taller' }).subscribe({
      next: (actualizada) => {
        this.state.showToast('success', 'Estado Actualizado', `Orden #${actualizada.id} pasó a ${actualizada.estado}`);
        this.cargarOrdenes();
        this.cargarFranjas();
        this.cargarCitas();
        this.cargarNotificaciones();
      },
      error: (err) => {
        const msg = err.error?.message || 'Error al cambiar estado';
        this.state.showToast('error', 'Transición Rechazada', msg);
      }
    });
  }

  abrirModalReapertura(orden: Orden) {
    this.ordenSeleccionada.set(orden);
    this.formReapertura.razon = '';
    this.modalReaperturaAbierto.set(true);
  }

  confirmarReapertura() {
    const orden = this.ordenSeleccionada();
    if (!orden) return;

    if (!this.formReapertura.razon || !this.formReapertura.razon.trim()) {
      this.state.showToast('warning', 'Razón Obligatoria (RF-07)', 'Debe indicar una razón válida para reabrir la orden');
      return;
    }

    this.ordenesService.cambiarEstado(orden.id, {
      estadoNuevo: 'EN_REPARACION',
      razon: this.formReapertura.razon
    }).subscribe({
      next: (actualizada) => {
        this.state.showToast('success', 'Orden Reabierta', `Orden #${actualizada.id} reabierta en reparación con auditoría registrada`);
        this.modalReaperturaAbierto.set(false);
        this.cargarOrdenes();
        this.cargarFranjas();
        this.cargarCitas();
      },
      error: (err) => {
        const msg = err.error?.message || 'Error en reapertura';
        this.state.showToast('error', 'Reapertura Fallida', msg);
      }
    });
  }

  verAuditoria(orden: Orden) {
    this.ordenSeleccionada.set(orden);
    this.ordenesService.obtenerAuditoria(orden.id).subscribe({
      next: (historial) => {
        this.auditoriaOrden.set(historial);
        this.modalAuditoriaAbierto.set(true);
      },
      error: (err) => {
        this.state.showToast('error', 'Error', 'No se pudo cargar la auditoría');
      }
    });
  }

  // Agendamiento de Citas
  prepararCitaParaOrden(orden: Orden) {
    this.formCita.ordenId = orden.id;
    this.state.activeTab.set('agenda');
    this.modalCitaAbierto.set(true);
  }

  guardarCita() {
    if (!this.formCita.ordenId || !this.formCita.operarioId || !this.formCita.franjaCupoId) {
      this.state.showToast('warning', 'Campos Incompletos', 'Seleccione orden, operario y franja horaria');
      return;
    }

    this.agendaService.agendarCita({
      ordenId: Number(this.formCita.ordenId),
      operarioId: Number(this.formCita.operarioId),
      franjaCupoId: Number(this.formCita.franjaCupoId),
      tipo: this.formCita.tipo
    }).subscribe({
      next: (cita) => {
        this.state.showToast('success', 'Cita Agendada (RF-14)', `Cita #${cita.id} confirmada. Se emitió evento WhatsApp.`);
        this.modalCitaAbierto.set(false);
        this.cargarFranjas();
        this.cargarCitas();
        this.cargarNotificaciones();
      },
      error: (err) => {
        const msg = err.error?.message || 'No se pudo agendar la cita';
        this.state.showToast('error', 'Agendamiento Rechazado (RF-15 / RF-17)', msg);
      }
    });
  }

  abrirCancelarCita(cita: Cita) {
    this.citaSeleccionada.set(cita);
    this.formCancelarCita.motivo = 'CANCELADA_POR_CLIENTE';
    this.modalCancelarCitaAbierto.set(true);
  }

  confirmarCancelarCita() {
    const cita = this.citaSeleccionada();
    if (!cita) return;

    this.agendaService.cancelarCita(cita.id, this.formCancelarCita.motivo).subscribe({
      next: () => {
        this.state.showToast('info', 'Cita Cancelada (RF-16)', `Cita #${cita.id} cancelada. Cupo liberado.`);
        this.modalCancelarCitaAbierto.set(false);
        this.cargarFranjas();
        this.cargarCitas();
        this.cargarNotificaciones();
      },
      error: (err) => {
        this.state.showToast('error', 'Error', err.error?.message || 'No se pudo cancelar la cita');
      }
    });
  }

  // Guardar Cliente
  guardarCliente() {
    if (!this.formCliente.nombre || !this.formCliente.documento || !this.formCliente.telefono) {
      this.state.showToast('warning', 'Campos Incompletos', 'Nombre, cédula y teléfono WhatsApp son obligatorios');
      return;
    }

    this.ordenesService.registrarCliente(this.formCliente).subscribe({
      next: (nuevo) => {
        this.state.showToast('success', 'Cliente Registrado (RF-01)', `Cliente ${nuevo.nombre} guardado exitosamente`);
        this.modalClienteAbierto.set(false);
        this.formCliente = { nombre: '', documento: '', telefono: '', email: '', direccion: '' };
        this.cargarClientes();
      },
      error: (err) => {
        this.state.showToast('error', 'Error', err.error?.message || 'Error al guardar cliente');
      }
    });
  }

  // Guardar Vehículo
  guardarVehiculo() {
    if (!this.formVehiculo.placa || !this.formVehiculo.marca || !this.formVehiculo.clienteId) {
      this.state.showToast('warning', 'Campos Incompletos', 'Placa, marca y cliente son obligatorios');
      return;
    }

    this.ordenesService.registrarVehiculo(this.formVehiculo).subscribe({
      next: (nuevo) => {
        this.state.showToast('success', 'Vehículo Registrado (RF-02)', `Vehículo ${nuevo.placa} asociado al cliente`);
        this.modalVehiculoAbierto.set(false);
        this.formVehiculo = { placa: '', marca: '', modelo: '', anio: 2023, color: '', clienteId: 0 };
        this.cargarVehiculos();
      },
      error: (err) => {
        this.state.showToast('error', 'Error', err.error?.message || 'Error al guardar vehículo');
      }
    });
  }

  // Guardar Orden
  guardarOrden() {
    if (!this.formOrden.clienteId || !this.formOrden.vehiculoId || !this.formOrden.trabajoSolicitado) {
      this.state.showToast('warning', 'Campos Incompletos', 'Seleccione cliente, vehículo y trabajo a realizar');
      return;
    }

    this.ordenesService.crearOrden({
      clienteId: Number(this.formOrden.clienteId),
      vehiculoId: Number(this.formOrden.vehiculoId),
      trabajoSolicitado: this.formOrden.trabajoSolicitado,
      aseguradora: this.formOrden.aseguradora,
      origen: this.formOrden.origen,
      idExterno: this.formOrden.idExterno
    }).subscribe({
      next: (nueva) => {
        this.state.showToast('success', 'Orden Creada (RF-03/04)', `Orden #${nueva.id} creada en estado RECIBIDA`);
        this.modalOrdenAbierto.set(false);
        this.formOrden = { clienteId: 0, vehiculoId: 0, trabajoSolicitado: '', aseguradora: '', origen: 'OPERARIO', idExterno: '' };
        this.cargarOrdenes();
        this.state.activeTab.set('pipeline');
      },
      error: (err) => {
        this.state.showToast('error', 'Error', err.error?.message || 'Error al crear orden');
      }
    });
  }

  // Inicialización de datos de demo
  inicializarDatosDemo() {
    // Registrar operarios en Agenda si no hay
    const op1 = { nombre: 'Carlos Mecánico', especialidad: 'Mecánica General', activo: true };
    const op2 = { nombre: 'Ana Gómez', especialidad: 'Frenos y Suspensión', activo: true };

    this.agendaService.registrarOperario(op1).subscribe({
      next: (creado1) => {
        const ahora = new Date();
        const f1Inicio = new Date(ahora.getTime() + 2 * 3600 * 1000).toISOString().slice(0, 16);
        const f1Fin = new Date(ahora.getTime() + 4 * 3600 * 1000).toISOString().slice(0, 16);

        this.agendaService.crearFranja({
          operarioId: creado1.id,
          inicio: f1Inicio,
          fin: f1Fin,
          cupoMaximo: 2
        }).subscribe(() => this.cargarFranjas());
      }
    });

    this.agendaService.registrarOperario(op2).subscribe({
      next: (creado2) => {
        const ahora = new Date();
        const f2Inicio = new Date(ahora.getTime() + 5 * 3600 * 1000).toISOString().slice(0, 16);
        const f2Fin = new Date(ahora.getTime() + 7 * 3600 * 1000).toISOString().slice(0, 16);

        this.agendaService.crearFranja({
          operarioId: creado2.id,
          inicio: f2Inicio,
          fin: f2Fin,
          cupoMaximo: 2
        }).subscribe(() => this.cargarFranjas());
      }
    });
  }
}

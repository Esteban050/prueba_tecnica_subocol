# REQUISITOS

Documento de contexto y requisitos para implementar el sistema de un taller automotriz. Está pensado para que lo lea un agente de IA (y una persona) antes de escribir código. Complementa a `DECISIONES.md`, que explica el porqué de cada frontera. Si hay una contradicción entre este documento y `DECISIONES.md`, avisa en lugar de elegir en silencio.

---

## 1. Contexto del problema

Un taller automotriz recibe vehículos de clientes particulares y de aseguradoras. Hoy todo se maneja en Excel y WhatsApp (los avisos a clientes se hacen a mano). Necesitan un sistema donde:

- El operario registra la orden de servicio: cliente, vehículo, aseguradora si aplica y qué hay que hacer.
- La orden avanza por estados hasta repararse y cerrarse. A veces hay que reabrirla, y siempre con una razón.
- El cliente tiene citas para traer o recoger el vehículo. Cada operario atiende un número limitado de citas por franja horaria.
- El cliente recibe un WhatsApp cuando se le agenda o cancela una cita.
- Al cerrar una orden, sus citas pendientes se cancelan solas, y en ese caso **no** se avisa al cliente, porque ya recogió su vehículo.
- Auditoría necesita saber quién cambió cada estado y cuándo, sin que nadie pueda alterar ese registro.
- Las órdenes de las aseguradoras grandes llegarán más adelante por integración; no las crea el operario.

Es una prueba técnica de un día. Se evalúa el criterio de diseño (fronteras, dueños de datos, comunicación) más que la cantidad de código. Se puede usar IA libremente.

## 2. Restricciones técnicas

- Front end: **Angular**.
- Backend: **Java** con **microservicios** (se sugiere Spring Boot).
- Todo debe levantar con `docker compose up`.
- WhatsApp se simula con un **log**.
- Cada microservicio tiene su **propia base de datos** (se sugiere PostgreSQL, una base o esquema por servicio).
- Comunicación entre servicios por **eventos** a través de un broker (se sugiere RabbitMQ). Entre el front y los servicios, REST.

`PENDIENTE DE CONFIRMAR`: broker exacto y si se implementa outbox transaccional (que el cambio y el evento se guarden en la misma transacción y un publicador los envíe después). Si hay poco tiempo, se puede publicar el evento tras confirmar la transacción y documentar la limitación como deuda conocida.

## 3. Arquitectura

Tres servicios, más el front y un broker.

```
Angular ──REST──> Órdenes
Angular ──REST──> Agenda
Aseguradoras (futuro, adaptador) ┄┄> Órdenes

Órdenes ──publica──> Broker ──> Agenda
Agenda  ──publica──> Broker ──> Notificaciones ──> log de WhatsApp
```

### 3.1 Principio rector

Cada dato tiene un solo dueño y solo el dueño lo modifica. Los demás servicios piden el cambio al dueño o guardan una copia de solo lectura, actualizada por eventos. **Ningún servicio escribe en la base de datos de otro ni usa llaves foráneas hacia ella.**

### 3.2 Servicio Órdenes

- **Es dueño de:** clientes, vehículos, órdenes, estado, transiciones y el registro de auditoría.
- **Única que cambia el estado de una orden.** Valida las transiciones, guarda el cambio y su registro de auditoría en la misma transacción y publica el evento.
- El teléfono del cliente se guarda aquí (se necesita para el aviso por WhatsApp).

### 3.3 Servicio Agenda

- **Es dueño de:** operarios, franjas, cupos y citas.
- Guarda una **copia de solo lectura** del estado de la orden (`ordenId` sin llave foránea), actualizada por eventos.
- **No escribe el estado de la orden.** Si hiciera falta cerrar una orden desde aquí, lo pediría a Órdenes.
- El cupo por operario y franja exige consistencia inmediata; por eso vive dentro de este servicio.

### 3.4 Servicio Notificaciones

- **Es dueño de:** plantillas, política de a quién avisar y registro de mensajes enviados.
- Solo consume eventos; no es llamado directamente. Si cae, el resto del sistema sigue funcionando.
- La política de avisos vive aquí: Agenda no sabe nada del canal de mensajes.

## 4. Eventos

| Evento | Emisor | Consumidores | Efecto |
|---|---|---|---|
| `EstadoOrdenCambiado` (ordenId, estadoAnterior, estadoNuevo, razón, operarioId, momento) | Órdenes | Agenda | Actualiza su copia del estado |
| `OrdenCerrada` (ordenId, operarioId, momento) | Órdenes | Agenda | Cancela las citas pendientes con motivo `CIERRE_ORDEN` |
| `CitaAgendada` (citaId, ordenId, clienteTelefono, franja) | Agenda | Notificaciones | Avisa al cliente |
| `CitaCancelada` (citaId, ordenId, clienteTelefono, franja, motivo) | Agenda | Notificaciones | Avisa, salvo si el motivo es `CIERRE_ORDEN` |

Reglas para todos los eventos:

- Cada evento lleva un `eventId` único.
- La entrega es "al menos una vez", así que **todos los consumidores deben ser idempotentes** (procesar dos veces el mismo evento no duplica efectos).
- `PENDIENTE DE CONFIRMAR`: cómo obtiene Agenda el teléfono del cliente para enviarlo en el evento (por ejemplo, incluirlo en `OrdenCreada` o consultarlo a Órdenes). Decide una opción y documéntala en `DECISIONES.md`.

## 5. Modelo de estados (propuesta, confirmar)

**Orden:** `RECIBIDA` → `EN_DIAGNOSTICO` → `EN_REPARACION` → `REPARADA` → `CERRADA`.
Reabrir: `CERRADA` → `EN_REPARACION`, con razón obligatoria.
Cualquier otra transición se rechaza.

**Cita:** `AGENDADA`, `CANCELADA`, `CUMPLIDA`. "Cita pendiente" significa `AGENDADA`.

Una orden reabierta **no** revive las citas canceladas por el cierre; si hace falta, se agendan nuevas.

## 6. Requisitos funcionales

Prioridad: **O** = obligatorio, **D** = deseable.

### Órdenes

| ID | Requisito | P |
|---|---|---|
| RF-01 | El operario puede registrar un cliente (datos de contacto, incluido el teléfono para WhatsApp). | O |
| RF-02 | El operario puede registrar un vehículo asociado a un cliente. | O |
| RF-03 | El operario puede crear una orden de servicio con cliente, vehículo, trabajo a realizar y aseguradora si aplica. | O |
| RF-04 | La orden guarda su `origen` (`OPERARIO` o `ASEGURADORA`) e `id_externo` opcional, sin exigir operario creador. | O |
| RF-05 | La orden avanza por la secuencia de estados definida; una transición no permitida se rechaza. | O |
| RF-06 | El operario puede cerrar una orden. | O |
| RF-07 | El operario puede reabrir una orden cerrada; la razón es obligatoria y, sin ella, la operación falla. | O |
| RF-08 | El sistema permite consultar una orden y el historial de órdenes de un cliente. | O |
| RF-09 | Cada cambio de estado registra automáticamente quién lo hizo, cuándo, estado anterior, estado nuevo y razón (si aplica), en la misma transacción que el cambio. | O |
| RF-10 | El registro de auditoría no puede modificarse ni borrarse (solo inserción). | O |
| RF-11 | El sistema permite consultar el historial de auditoría de una orden. | O |
| RF-12 | Cada cambio de estado y el cierre publican un evento. | O |

### Agenda

| ID | Requisito | P |
|---|---|---|
| RF-13 | El sistema gestiona operarios y franjas horarias con un cupo máximo de citas por operario y franja. | O |
| RF-14 | El operario puede agendar una cita de entrega o recogida de un vehículo, asociada a una orden. | O |
| RF-15 | El sistema rechaza agendar si la franja del operario ya alcanzó su cupo, incluso con peticiones simultáneas. | O |
| RF-16 | El operario puede cancelar una cita indicando el motivo, y el cupo se libera. | O |
| RF-17 | El sistema no permite agendar citas sobre órdenes cerradas, según su copia del estado. | D |
| RF-18 | Al recibir el evento de cierre de una orden, el sistema cancela automáticamente sus citas pendientes con motivo `CIERRE_ORDEN`. | O |
| RF-19 | El sistema mantiene una copia de solo lectura del estado de la orden, actualizada por eventos. | O |
| RF-20 | Cada cita agendada o cancelada publica un evento (la cancelación incluye el motivo). | O |
| RF-21 | El operario puede consultar las citas por franja, operario y orden. | D |

### Notificaciones

| ID | Requisito | P |
|---|---|---|
| RF-22 | Al agendarse una cita, el sistema envía un aviso al cliente por WhatsApp (simulado con un log). | O |
| RF-23 | Al cancelarse una cita por un motivo distinto de `CIERRE_ORDEN`, el sistema envía un aviso al cliente. | O |
| RF-24 | Cuando la cancelación es por cierre de orden, el sistema no envía aviso al cliente. | O |
| RF-25 | El sistema registra cada mensaje enviado (destinatario, contenido, momento). | D |
| RF-26 | Procesar el mismo evento dos veces no genera dos avisos. | D |

### Transversales

| ID | Requisito | P |
|---|---|---|
| RF-27 | Cada petición que cambia datos identifica al operario que la realiza (por ejemplo, con un encabezado `X-User-Id`). | O |
| RF-28 | Si Notificaciones no está disponible, agendar, cancelar y cerrar siguen funcionando. | O |
| RF-29 | Todo el sistema levanta con un solo `docker compose up`. | O |

## 7. Criterios de aceptación de las reglas delicadas

### Cupo por franja (RF-15)

- **Dado** una franja con cupo máximo 2 y 2 citas agendadas, **cuando** se intenta agendar una tercera, **entonces** se rechaza y el cupo ocupado sigue siendo 2.
- **Dado** una franja con 1 cupo libre, **cuando** llegan dos peticiones simultáneas, **entonces** solo una se agenda y la otra se rechaza.
- **Dado** una cita agendada, **cuando** se cancela, **entonces** el cupo ocupado baja en 1 en la misma transacción.

Implementación sugerida: una operación atómica en la base de datos.

```sql
UPDATE franja_cupo SET ocupados = ocupados + 1
WHERE operario_id = :o AND inicio = :f AND ocupados < cupo_max;
-- 0 filas afectadas = franja llena
```

### Cierre de orden sin aviso (RF-18, RF-24)

- **Dado** una orden con dos citas `AGENDADA`, **cuando** se cierra la orden, **entonces** ambas pasan a `CANCELADA` con motivo `CIERRE_ORDEN` y se libera su cupo.
- **Dado** que se emitió `CitaCancelada` con motivo `CIERRE_ORDEN`, **cuando** Notificaciones lo procesa, **entonces** no se genera ningún aviso al cliente.
- **Dado** que se emitió `CitaCancelada` con motivo `CANCELADA_POR_CLIENTE`, **cuando** Notificaciones lo procesa, **entonces** se genera un aviso.
- **Dado** que el evento `OrdenCerrada` llega dos veces, **cuando** Agenda lo procesa, **entonces** el segundo procesamiento no tiene efecto.

### Reapertura con razón (RF-07)

- **Dado** una orden `CERRADA`, **cuando** se reabre sin razón (vacía o ausente), **entonces** se rechaza y el estado no cambia.
- **Dado** una orden `CERRADA`, **cuando** se reabre con razón, **entonces** pasa a `EN_REPARACION`, la razón queda en la auditoría y se publica `EstadoOrdenCambiado`.
- **Dado** una orden reabierta, **entonces** las citas canceladas por el cierre permanecen canceladas.

### Auditoría inmutable (RF-09, RF-10)

- **Dado** cualquier cambio de estado, **entonces** existe un registro con operario, momento, estado anterior, estado nuevo y razón.
- **Dado** que el cambio de estado falla y se revierte, **entonces** no queda registro de auditoría de ese cambio.
- **Dado** un registro de auditoría existente, **cuando** la aplicación intenta actualizarlo o borrarlo, **entonces** la base de datos lo rechaza.

Implementación sugerida: usuario de base de datos de la aplicación con permiso solo de `INSERT` sobre esa tabla, y un trigger que bloquee `UPDATE` y `DELETE`.

### Aislamiento de fallos (RF-28)

- **Dado** que Notificaciones está detenido, **cuando** se agenda una cita o se cierra una orden, **entonces** la operación termina con éxito, y cuando Notificaciones vuelve procesa los eventos pendientes.

## 8. Fuera de alcance

- Vistas diferenciadas por rol (hay un solo perfil de operario).
- Autenticación y autorización reales (se simula la identidad con `X-User-Id`).
- Envío real de WhatsApp (se simula con un log).
- Conector de aseguradoras (solo se deja la costura en RF-04).
- Diseño visual pulido del front.

## 9. Interfaz mínima del front (Angular)

Sin vistas por rol. Un único flujo para el operario:

1. Crear cliente y vehículo.
2. Crear una orden.
3. Ver una orden y cambiar su estado (cerrar y reabrir con razón).
4. Agendar y cancelar citas, mostrando cuando una franja está llena.
5. Ver el historial de auditoría de una orden.

## 10. API sugerida (propuesta, ajustable)

**Órdenes**
- `POST /clientes`, `POST /vehiculos`
- `POST /ordenes`, `GET /ordenes/{id}`, `GET /clientes/{id}/ordenes`
- `POST /ordenes/{id}/transiciones` (cuerpo: estado destino y razón opcional)
- `GET /ordenes/{id}/auditoria`

**Agenda**
- `POST /operarios`, `POST /franjas`
- `POST /citas`, `DELETE /citas/{id}` (con motivo), `GET /citas?ordenId=&operarioId=&franja=`

Cuando el cambio de estado lo hace el front, llama directamente a Órdenes. `PENDIENTE DE CONFIRMAR` si se decide otra ruta.

## 11. Reglas para el agente de IA

1. **Respeta las fronteras.** No leas ni escribas la base de datos de otro servicio ni crees llaves foráneas entre bases.
2. **Solo Órdenes cambia el estado de una orden.**
3. **La auditoría se guarda en la misma transacción que el cambio de estado.**
4. **Todos los consumidores de eventos son idempotentes.**
5. **No llames a Notificaciones directamente;** solo por eventos.
6. **No añadas servicios, funcionalidades ni dependencias** que no estén en este documento sin preguntar.
7. **Si algo es ambiguo o está marcado `PENDIENTE DE CONFIRMAR`,** pregunta o propón una opción y regístrala en `DECISIONES.md` en lugar de decidir en silencio.
8. **Cuando propongas una decisión de diseño distinta a la de este documento,** explícala y espera confirmación. Las propuestas que la persona rechace se registran en `DECISIONES.md`, sección 7.
9. **Escribe pruebas** para las reglas de la sección 7, especialmente el cupo concurrente, el cierre sin aviso y la inmutabilidad de la auditoría.
10. **Verifica `docker compose up --build` desde cero** antes de dar algo por terminado.

## 12. Orden de implementación sugerido

1. `docker-compose.yml` con bases de datos y broker.
2. Órdenes: clientes, vehículos, órdenes, máquina de estados, auditoría y publicación de eventos.
3. Agenda: franjas, cupo atómico, citas, consumo de `OrdenCerrada` y `EstadoOrdenCambiado`.
4. Notificaciones: consumo de eventos, política por motivo y log simulado.
5. Pruebas de las reglas delicadas.
6. Front Angular mínimo.
7. Completar `DECISIONES.md` con lo realmente implementado.

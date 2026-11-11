package com.subocol.taller.notificaciones.repository;

import com.subocol.taller.notificaciones.domain.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    List<Notificacion> findAllByOrderByFechaRegistroDesc();
    List<Notificacion> findByOrdenIdOrderByFechaRegistroDesc(Long ordenId);
    List<Notificacion> findByDestinatarioTelefonoOrderByFechaRegistroDesc(String destinatarioTelefono);
}

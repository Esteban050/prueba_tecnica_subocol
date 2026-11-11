package com.subocol.taller.agenda.repository;

import com.subocol.taller.agenda.domain.OrdenReplica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdenReplicaRepository extends JpaRepository<OrdenReplica, Long> {
}

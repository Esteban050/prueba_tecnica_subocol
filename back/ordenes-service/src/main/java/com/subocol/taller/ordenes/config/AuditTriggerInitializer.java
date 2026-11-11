package com.subocol.taller.ordenes.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class AuditTriggerInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AuditTriggerInitializer.class);
    private final JdbcTemplate jdbcTemplate;

    public AuditTriggerInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        try {
            // Verificar si es PostgreSQL antes de aplicar el trigger PL/pgSQL
            String databaseProductName = jdbcTemplate.getDataSource().getConnection().getMetaData().getDatabaseProductName();
            if ("PostgreSQL".equalsIgnoreCase(databaseProductName)) {
                log.info("Configurando trigger de inmutabilidad para tabla auditoria_orden en PostgreSQL...");

                jdbcTemplate.execute("""
                    CREATE OR REPLACE FUNCTION fn_prevenir_modificacion_auditoria()
                    RETURNS TRIGGER AS $$
                    BEGIN
                        RAISE EXCEPTION 'Auditoría inmutable: No está permitido modificar ni eliminar registros de auditoría (RF-10)';
                    END;
                    $$ LANGUAGE plpgsql;
                """);

                jdbcTemplate.execute("""
                    DROP TRIGGER IF EXISTS trg_auditoria_inmutable ON auditoria_orden;
                """);

                jdbcTemplate.execute("""
                    CREATE TRIGGER trg_auditoria_inmutable
                    BEFORE UPDATE OR DELETE ON auditoria_orden
                    FOR EACH ROW
                    EXECUTE FUNCTION fn_prevenir_modificacion_auditoria();
                """);

                log.info("Trigger de inmutabilidad configurado exitosamente en PostgreSQL.");
            }
        } catch (Exception e) {
            log.warn("No se pudo aplicar el trigger de inmutabilidad (normal si la tabla aún no se crea o en H2): {}", e.getMessage());
        }
    }
}

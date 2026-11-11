#!/bin/bash
set -e

echo "Inicializando bases de datos independientes para microservicios..."

# 1. Base de datos para servicio Órdenes
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE USER ordenes_user WITH PASSWORD 'ordenes_pass';
    CREATE DATABASE ordenes_db OWNER ordenes_user;
    GRANT ALL PRIVILEGES ON DATABASE ordenes_db TO ordenes_user;
EOSQL

# Conectar a ordenes_db y conceder permisos sobre el esquema public
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "ordenes_db" <<-EOSQL
    GRANT ALL ON SCHEMA public TO ordenes_user;
EOSQL

# 2. Base de datos para servicio Agenda
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE USER agenda_user WITH PASSWORD 'agenda_pass';
    CREATE DATABASE agenda_db OWNER agenda_user;
    GRANT ALL PRIVILEGES ON DATABASE agenda_db TO agenda_user;
EOSQL

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "agenda_db" <<-EOSQL
    GRANT ALL ON SCHEMA public TO agenda_user;
EOSQL

# 3. Base de datos para servicio Notificaciones
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE USER notificaciones_user WITH PASSWORD 'notificaciones_pass';
    CREATE DATABASE notificaciones_db OWNER notificaciones_user;
    GRANT ALL PRIVILEGES ON DATABASE notificaciones_db TO notificaciones_user;
EOSQL

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "notificaciones_db" <<-EOSQL
    GRANT ALL ON SCHEMA public TO notificaciones_user;
EOSQL

echo "Bases de datos para microservicios creadas exitosamente."

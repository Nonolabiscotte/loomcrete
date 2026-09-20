-- Initialize PostgreSQL for loomcrete services
-- Create separate databases (or schemas) for each service

CREATE DATABASE loomcrete_tenant;
CREATE DATABASE loomcrete_inventory;
CREATE DATABASE loomcrete_reservation;

-- Alternatively, if you prefer schemas in a single DB, use:
-- CREATE SCHEMA loomcrete_tenant;
-- CREATE SCHEMA loomcrete_inventory;
-- CREATE SCHEMA loomcrete_reservation;

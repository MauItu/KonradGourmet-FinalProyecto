-- Los nombres de las sucursales, para el correo de stock bajo (HU-16, 4; DEC-102). Es la misma carga inicial
-- de ms-identidad y ms-analitica (S10): cada servicio guarda su copia.
CREATE TABLE sucursal (
    id     BIGINT PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL
);
INSERT INTO sucursal (id, nombre) VALUES (1, 'Sucursal Centro'), (2, 'Sucursal Norte'), (3, 'Sucursal Chapinero');

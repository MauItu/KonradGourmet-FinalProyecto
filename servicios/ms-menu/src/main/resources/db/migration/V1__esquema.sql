-- Base de ms-menu (ms_menu). Ningún otro servicio la lee: ms-pedidos y ms-inventario reciben los platos
-- por menu.actualizado.
CREATE TABLE categoria (
    id     BIGINT PRIMARY KEY,
    nombre VARCHAR(60) NOT NULL UNIQUE
);

CREATE TABLE producto (
    id           BIGINT PRIMARY KEY,
    nombre       VARCHAR(80) NOT NULL UNIQUE,
    categoria_id BIGINT NOT NULL REFERENCES categoria (id)
);

CREATE TABLE plato (
    id           BIGSERIAL PRIMARY KEY,
    sucursal_id  BIGINT NOT NULL,
    creado_por   BIGINT NOT NULL,
    nombre       VARCHAR(120) NOT NULL,
    categoria    VARCHAR(40) NOT NULL,
    descripcion  VARCHAR(200) NOT NULL DEFAULT '',
    precio_venta NUMERIC(12, 2) NOT NULL CHECK (precio_venta > 0),
    activo       BOOLEAN NOT NULL,
    imagen_url   VARCHAR(300)
);
-- El nombre es único en la sucursal (existeNombre), sin importar mayúsculas.
CREATE UNIQUE INDEX plato_nombre_por_sucursal ON plato (sucursal_id, lower(nombre));

CREATE TABLE ingrediente (
    plato_id    BIGINT NOT NULL REFERENCES plato (id) ON DELETE CASCADE,
    orden       INT NOT NULL,
    producto_id BIGINT NOT NULL REFERENCES producto (id),
    cantidad    NUMERIC(12, 4) NOT NULL CHECK (cantidad > 0),
    unidad      VARCHAR(10) NOT NULL,
    PRIMARY KEY (plato_id, orden)
);

-- Los productos son los insumos del inventario (mismos ids en ms-inventario).
INSERT INTO categoria (id, nombre) VALUES (1, 'Proteína'), (2, 'Cereal'), (3, 'Tubérculo');
INSERT INTO producto (id, nombre, categoria_id) VALUES (1, 'Res', 1), (2, 'Arroz', 2), (3, 'Papa', 3), (4, 'Pollo', 1);

-- Base de ms-pedidos (ms_pedidos). Ningún otro servicio la lee: ms-inventario recibe los pedidos por
-- pedido.registrado.
CREATE TABLE mesa (
    sucursal_id BIGINT NOT NULL,
    numero      INT NOT NULL,
    capacidad   INT NOT NULL,
    activa      BOOLEAN NOT NULL,
    PRIMARY KEY (sucursal_id, numero)
);

-- Los números siguen a los de la demo: el primero es PED-0251.
CREATE SEQUENCE pedido_numero START WITH 251;

CREATE TABLE pedido (
    numero      VARCHAR(20) PRIMARY KEY,
    sucursal_id BIGINT NOT NULL,
    mesa_numero INT NOT NULL,
    mesero_id   BIGINT NOT NULL,
    fecha       TIMESTAMP NOT NULL,
    estado      VARCHAR(20) NOT NULL,
    FOREIGN KEY (sucursal_id, mesa_numero) REFERENCES mesa (sucursal_id, numero)
);
CREATE INDEX pedido_mesero_fecha ON pedido (mesero_id, fecha);

CREATE TABLE detalle_pedido (
    pedido_numero   VARCHAR(20) NOT NULL REFERENCES pedido (numero) ON DELETE CASCADE,
    orden           INT NOT NULL,
    plato_id        BIGINT NOT NULL,
    cantidad        INT NOT NULL CHECK (cantidad > 0),
    nota            VARCHAR(200),
    precio_unitario NUMERIC(12, 2) NOT NULL,
    PRIMARY KEY (pedido_numero, orden)
);

-- Copia local del menú de cada sucursal (nota del diagrama: cada réplica lee el catálogo de su base).
-- La llena menu.actualizado; un plato desactivado queda con activo = false.
CREATE TABLE plato_catalogo (
    plato_id    BIGINT PRIMARY KEY,
    sucursal_id BIGINT NOT NULL,
    nombre      VARCHAR(120) NOT NULL,
    categoria   VARCHAR(40),
    precio      NUMERIC(12, 2) NOT NULL,
    activo      BOOLEAN NOT NULL
);

-- Las mesas de la Sucursal Centro que muestran V02 y V03 en la demo.
INSERT INTO mesa (sucursal_id, numero, capacidad, activa) VALUES
    (1, 2, 4, TRUE), (1, 3, 4, TRUE), (1, 5, 4, TRUE), (1, 6, 4, TRUE), (1, 8, 4, TRUE);

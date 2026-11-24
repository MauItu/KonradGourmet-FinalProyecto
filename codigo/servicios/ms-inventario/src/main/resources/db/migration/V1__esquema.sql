-- Base de ms-inventario (ms_inventario). Ningún otro servicio la lee.
CREATE TABLE insumo (
    sucursal_id      BIGINT NOT NULL,
    producto_id      BIGINT NOT NULL,
    nombre           VARCHAR(80) NOT NULL,
    stock_actual     NUMERIC(12, 3) NOT NULL CHECK (stock_actual >= 0),
    stock_referencia NUMERIC(12, 3) NOT NULL CHECK (stock_referencia > 0),
    unidad           VARCHAR(10) NOT NULL,
    alerta_atendida  BOOLEAN NOT NULL DEFAULT FALSE,
    PRIMARY KEY (sucursal_id, producto_id)
);

-- Copia local de cada receta (kg de cada producto por plato), que llega por menu.actualizado.
CREATE TABLE receta (
    plato_id    BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    cantidad    NUMERIC(12, 4) NOT NULL,
    PRIMARY KEY (plato_id, producto_id)
);

-- Lo que consumieron los pedidos del día y todavía no se descontó (lo descuenta ConsumoDiarioJob).
CREATE TABLE consumo_pendiente (
    sucursal_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    cantidad    NUMERIC(12, 4) NOT NULL,
    PRIMARY KEY (sucursal_id, producto_id)
);

-- ShedLock: con varias réplicas, solo una ejecuta el cierre de las 23:59 (DEC-36).
CREATE TABLE shedlock (
    name       VARCHAR(64) PRIMARY KEY,
    lock_until TIMESTAMP NOT NULL,
    locked_at  TIMESTAMP NOT NULL,
    locked_by  VARCHAR(255) NOT NULL
);

-- El inventario de la Sucursal Centro de la demo: la res empieza justo en el 25 %.
INSERT INTO insumo (sucursal_id, producto_id, nombre, stock_actual, stock_referencia, unidad) VALUES
    (1, 1, 'Res', 12.5, 50, 'kg'),
    (1, 2, 'Arroz', 40, 60, 'kg'),
    (1, 3, 'Papa', 30, 40, 'kg'),
    (1, 4, 'Pollo', 25, 40, 'kg');

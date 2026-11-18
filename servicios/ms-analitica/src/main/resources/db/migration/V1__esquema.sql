-- ms-analitica: su propio almacén de hechos, armado con los eventos de negocio (no lee las bases de otros servicios).

-- Los hechos que suman o promedian los KPI (HechoNegocio): DEMANDA_PRODUCTO, DEMANDA_SUCURSAL y PRECIO_PROVEEDOR,
-- y los de los otros tópicos (STOCK_BAJO, SOLICITUD_SUCURSAL y ORDEN_PROVEEDOR). correlacion_id es la del evento que
-- lo dejó (DEC-64).
CREATE TABLE hecho (
    id             BIGSERIAL PRIMARY KEY,
    tipo           VARCHAR(40)    NOT NULL,
    sucursal_id    BIGINT,
    clave          VARCHAR(120)   NOT NULL,
    valor          NUMERIC(14, 4) NOT NULL,
    fecha          DATE           NOT NULL,
    correlacion_id UUID
);
CREATE INDEX hecho_fecha ON hecho (fecha, tipo);

-- Cada consumidor guarda los id ya procesados y descarta los repetidos (nota de EventoDominio).
CREATE TABLE evento_procesado (
    id           UUID PRIMARY KEY,
    topico       VARCHAR(60) NOT NULL,
    procesado_en TIMESTAMP   NOT NULL DEFAULT now()
);

-- La copia local de los nombres (DEC-90): los platos, de menu.actualizado; las sucursales, de la carga inicial (S10).
CREATE TABLE plato_nombre (
    plato_id BIGINT PRIMARY KEY,
    nombre   VARCHAR(120) NOT NULL
);
CREATE TABLE sucursal (
    id     BIGINT PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL
);
INSERT INTO sucursal (id, nombre) VALUES (1, 'Sucursal Centro'), (2, 'Sucursal Norte'), (3, 'Sucursal Chapinero');

-- Las promociones sugeridas (SugerenciaPromocion, HU-19): una por producto y día de inicio de su vigencia.
CREATE TABLE sugerencia (
    id             BIGSERIAL PRIMARY KEY,
    producto       VARCHAR(120) NOT NULL,
    motivo         VARCHAR(200) NOT NULL,
    vigencia_desde DATE         NOT NULL,
    vigencia_hasta DATE         NOT NULL,
    estado         VARCHAR(20)  NOT NULL,
    creada_en      TIMESTAMP    NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX sugerencia_unica ON sugerencia (lower(producto), vigencia_desde);

-- ShedLock: con varias réplicas, solo una ejecuta TendenciasJob (DEC-36).
CREATE TABLE shedlock (
    name       VARCHAR(64) PRIMARY KEY,
    lock_until TIMESTAMP NOT NULL,
    locked_at  TIMESTAMP NOT NULL,
    locked_by  VARCHAR(255) NOT NULL
);

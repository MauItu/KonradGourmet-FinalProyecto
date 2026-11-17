-- Base de ms-solicitudes (ms_solicitudes). Ningún otro servicio la lee: ms-inventario (y en los cortes
-- siguientes ms-notificaciones y ms-analitica) reciben cada solicitud por solicitud.registrada.

-- Los números siguen a los de la demo: la primera solicitud es SOL-031.
CREATE SEQUENCE solicitud_numero START WITH 31;

CREATE TABLE solicitud (
    numero               VARCHAR(20) PRIMARY KEY,
    sucursal_id          BIGINT NOT NULL,
    solicitante_id       BIGINT NOT NULL,
    correo_solicitante   VARCHAR(160) NOT NULL,
    fecha                TIMESTAMP NOT NULL,
    estado               VARCHAR(20) NOT NULL,
    valor_sugerido_total NUMERIC(14, 2) NOT NULL
);
CREATE INDEX solicitud_solicitante ON solicitud (solicitante_id, fecha);
CREATE INDEX solicitud_estado_fecha ON solicitud (estado, fecha);

CREATE TABLE item_solicitud (
    solicitud_numero VARCHAR(20) NOT NULL REFERENCES solicitud (numero) ON DELETE CASCADE,
    orden            INT NOT NULL,
    producto_id      BIGINT NOT NULL,
    cantidad         NUMERIC(12, 3) NOT NULL CHECK (cantidad > 0),
    marca            VARCHAR(80),
    unidad           VARCHAR(20) NOT NULL,
    cotizado         BOOLEAN NOT NULL,
    precio_sugerido  NUMERIC(12, 2),
    PRIMARY KEY (solicitud_numero, orden)
);
CREATE INDEX item_solicitud_producto ON item_solicitud (producto_id);

-- Las cotizaciones que ya se aplicaron a la solicitud (idempotencia de cotizacion.enviada, corte 4b).
CREATE TABLE cotizacion_aplicada (
    solicitud_numero  VARCHAR(20) NOT NULL REFERENCES solicitud (numero) ON DELETE CASCADE,
    cotizacion_numero VARCHAR(20) NOT NULL,
    PRIMARY KEY (solicitud_numero, cotizacion_numero)
);

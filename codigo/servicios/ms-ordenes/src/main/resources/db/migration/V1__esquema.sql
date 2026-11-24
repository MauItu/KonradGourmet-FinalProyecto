-- Base de ms-ordenes (ms_ordenes). Ningún otro servicio la lee: ms-cotizaciones, ms-solicitudes,
-- ms-notificaciones y ms-analitica reciben cada orden por orden.generada.

-- Los números siguen a los de la demo: la primera orden es OC-0012 (como COT-018 en ms-cotizaciones).
CREATE SEQUENCE orden_numero START WITH 12;

-- cotizacion_numero {único}: no puede haber dos órdenes sobre la misma cotización (HU-14), ni con un doble clic.
CREATE TABLE orden_compra (
    numero            VARCHAR(20) PRIMARY KEY,
    fecha             DATE NOT NULL,
    cotizacion_numero VARCHAR(20) NOT NULL UNIQUE,
    proveedor         VARCHAR(120) NOT NULL,
    correo_proveedor  VARCHAR(160) NOT NULL,
    pdf_url           VARCHAR(255),
    estado            VARCHAR(20) NOT NULL
);
CREATE INDEX orden_compra_estado ON orden_compra (estado, numero);

-- Las solicitudes de la cotización (una o varias, DEC-05): con orden.generada pasan a COMPLETADA.
CREATE TABLE orden_solicitud (
    orden_numero     VARCHAR(20) NOT NULL REFERENCES orden_compra (numero) ON DELETE CASCADE,
    posicion         INT NOT NULL,
    solicitud_numero VARCHAR(20) NOT NULL,
    PRIMARY KEY (orden_numero, posicion)
);

-- Los productos heredan de la cotización la cantidad, la unidad y el precio por unidad del proveedor (HU-14).
CREATE TABLE item_orden (
    orden_numero VARCHAR(20) NOT NULL REFERENCES orden_compra (numero) ON DELETE CASCADE,
    posicion     INT NOT NULL,
    producto_id  BIGINT NOT NULL,
    cantidad     NUMERIC(12, 3) NOT NULL CHECK (cantidad > 0),
    unidad       VARCHAR(20) NOT NULL,
    precio       NUMERIC(12, 2) NOT NULL CHECK (precio >= 0),
    PRIMARY KEY (orden_numero, posicion)
);

-- Copia del nombre de cada producto, tal como venía en la cotización (DEC-74): el PDF y V23 lo muestran.
CREATE TABLE producto_nombre (
    producto_id BIGINT PRIMARY KEY,
    nombre      VARCHAR(80) NOT NULL
);

-- Outbox transaccional (nota n_outbox de la 4a, DEC-104): KafkaEventPublisher guarda aquí cada evento en la
-- misma transacción que el cambio y ReleOutbox lo publica en Kafka y lo borra.
CREATE TABLE outbox (
    secuencia BIGSERIAL    PRIMARY KEY,
    topico    VARCHAR(120) NOT NULL,
    clave     VARCHAR(200),
    valor     TEXT         NOT NULL,
    clase     VARCHAR(200),
    creado_en TIMESTAMP    NOT NULL DEFAULT now()
);

-- Los eventos que cada consumidor ya procesó: un evento repetido se descarta (DEC-104).
CREATE TABLE evento_procesado (
    id           UUID        NOT NULL,
    consumidor   VARCHAR(80) NOT NULL,
    topico       VARCHAR(60) NOT NULL,
    procesado_en TIMESTAMP   NOT NULL DEFAULT now(),
    PRIMARY KEY (id, consumidor)
);

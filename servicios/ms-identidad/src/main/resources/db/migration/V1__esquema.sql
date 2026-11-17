-- Base de ms-identidad (ms_identidad). Ningún otro servicio la lee.
CREATE TABLE sucursal (
    id     BIGINT PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL UNIQUE
);

CREATE TABLE usuario (
    id            BIGSERIAL PRIMARY KEY,
    nombre        VARCHAR(120) NOT NULL,
    correo        VARCHAR(160) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    rol           VARCHAR(30)  NOT NULL CHECK (rol IN ('MESERO', 'JEFE_COCINA', 'AUXILIAR_COMPRAS',
                                                       'DIRECTOR_COMPRAS', 'DIRECTOR_COMERCIAL', 'ADMINISTRADOR')),
    sucursal_id   BIGINT NOT NULL REFERENCES sucursal (id)
);

-- Llave RSA con la que se firman los JWT (DEC-62).
CREATE TABLE llave_firma (
    kid     VARCHAR(40) PRIMARY KEY,
    publica TEXT NOT NULL,
    privada TEXT NOT NULL,
    creada  TIMESTAMP NOT NULL DEFAULT now()
);

INSERT INTO sucursal (id, nombre) VALUES
    (1, 'Sucursal Centro'),
    (2, 'Sucursal Norte'),
    (3, 'Sucursal Chapinero');

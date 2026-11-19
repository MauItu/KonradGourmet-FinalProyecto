-- Histórico de la demostración: lo que el almacén habría acumulado antes de la plataforma, para que el tablero BAM
-- tenga meses de datos y las tres sucursales (los pedidos reales llegan después por pedido.registrado). Es el mismo
-- papel que el histórico cargado a mano de la versión en memoria (DEC-40). Las fechas son relativas al día en que se
-- crea la base; los hechos no tienen correlación porque no salieron de un evento.

-- Demanda diaria de los últimos 400 días por sucursal y plato (los de la carta inicial de ms-menu): la Bandeja paisa
-- manda en Centro, el Ajiaco en Norte y la Pechuga en Chapinero; entre 60 y 150 días atrás, el Ajiaco sube en todas
-- (temporada de lluvias).
WITH base (sucursal_id, plato, unidades) AS (VALUES
    (1, 'Bandeja paisa', 16), (1, 'Ajiaco', 6), (1, 'Punta de anca', 4), (1, 'Pechuga a la plancha', 5),
    (1, 'Ensalada césar', 3), (1, 'Arepa con queso', 4), (1, 'Limonada natural', 5), (1, 'Jugo natural', 3),
    (2, 'Bandeja paisa', 4), (2, 'Ajiaco', 9), (2, 'Punta de anca', 3), (2, 'Pechuga a la plancha', 4),
    (2, 'Ensalada césar', 2), (2, 'Arepa con queso', 3), (2, 'Limonada natural', 4), (2, 'Jugo natural', 3),
    (3, 'Bandeja paisa', 3), (3, 'Ajiaco', 3), (3, 'Punta de anca', 4), (3, 'Pechuga a la plancha', 8),
    (3, 'Ensalada césar', 5), (3, 'Arepa con queso', 2), (3, 'Limonada natural', 3), (3, 'Jugo natural', 4)
)
INSERT INTO hecho (tipo, sucursal_id, clave, valor, fecha)
SELECT 'DEMANDA_PRODUCTO', b.sucursal_id, b.plato,
       b.unidades + ((d + b.sucursal_id + length(b.plato)) % 2)
           + CASE WHEN b.plato = 'Ajiaco' AND d BETWEEN 60 AND 150 THEN 8 ELSE 0 END,
       CURRENT_DATE - d
FROM base b CROSS JOIN generate_series(1, 400) AS d;

INSERT INTO hecho (tipo, sucursal_id, clave, valor, fecha)
SELECT 'DEMANDA_SUCURSAL', h.sucursal_id, s.nombre, sum(h.valor), h.fecha
FROM hecho h JOIN sucursal s ON s.id = h.sucursal_id
WHERE h.tipo = 'DEMANDA_PRODUCTO'
GROUP BY h.sucursal_id, s.nombre, h.fecha;

-- Diferencia contra el precio estándar de las cotizaciones OPCIONADAS del último año (y una de hace más de un año).
INSERT INTO hecho (tipo, sucursal_id, clave, valor, fecha) VALUES
    ('PRECIO_PROVEEDOR', NULL, 'Proveedor ABC', 3.10, CURRENT_DATE - 340),
    ('PRECIO_PROVEEDOR', NULL, 'Proveedor B', -1.80, CURRENT_DATE - 320),
    ('PRECIO_PROVEEDOR', NULL, 'Proveedor C', 6.50, CURRENT_DATE - 300),
    ('PRECIO_PROVEEDOR', NULL, 'Proveedor ABC', 5.20, CURRENT_DATE - 270),
    ('PRECIO_PROVEEDOR', NULL, 'Proveedor B', -2.40, CURRENT_DATE - 240),
    ('PRECIO_PROVEEDOR', NULL, 'Proveedor D', 1.50, CURRENT_DATE - 210),
    ('PRECIO_PROVEEDOR', NULL, 'Proveedor ABC', 4.00, CURRENT_DATE - 180),
    ('PRECIO_PROVEEDOR', NULL, 'Proveedor B', -3.00, CURRENT_DATE - 150),
    ('PRECIO_PROVEEDOR', NULL, 'Proveedor C', 9.00, CURRENT_DATE - 120),
    ('PRECIO_PROVEEDOR', NULL, 'Proveedor D', -0.50, CURRENT_DATE - 90),
    ('PRECIO_PROVEEDOR', NULL, 'Proveedor B', -1.20, CURRENT_DATE - 60),
    ('PRECIO_PROVEEDOR', NULL, 'Proveedor ABC', 2.90, CURRENT_DATE - 45),
    ('PRECIO_PROVEEDOR', NULL, 'Proveedor D', -8.00, CURRENT_DATE - 380);

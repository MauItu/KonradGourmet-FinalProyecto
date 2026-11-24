-- evento_procesado pasa a ser la de compartido: la clave es el evento y el consumidor (DEC-104).
ALTER TABLE evento_procesado ADD COLUMN consumidor VARCHAR(80) NOT NULL DEFAULT 'NegocioEventsListener';
ALTER TABLE evento_procesado ALTER COLUMN consumidor DROP DEFAULT;
ALTER TABLE evento_procesado DROP CONSTRAINT evento_procesado_pkey;
ALTER TABLE evento_procesado ADD PRIMARY KEY (id, consumidor);

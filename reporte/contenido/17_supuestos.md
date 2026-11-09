<!-- §17. Correcciones: R-57 (S5), R-61 (S9), R-64 (S10), R-65 (S11) y R-66 (S12). -->
# 17. Supuestos

El enunciado pide declarar y sustentar los supuestos. La tabla 15 reúne los que tomó el diseño.

!tabla 15 | Supuestos del diseño | 1,7,6.2
| # | Supuesto | Por qué |
|---|---|---|
| S1 | Las 1.000 TPS de «compras» corresponden sobre todo a pedidos de los meseros y a operaciones de cotización y orden | Son los flujos transaccionales del enunciado; por eso esos servicios tienen más réplicas |
| S2 | El proveedor responde la cotización por correo, fuera del sistema, y el director digita los valores | Lo dice el enunciado y lo muestra la ventana 18 |
| S3 | El precio estándar del Ministerio cambia como mucho una vez al día | Permite la caché del Proxy sin afectar la validación |
| S4 | El descuento de inventario se consolida una vez al día, aunque el consumo se acumula en línea con pedido.registrado | El enunciado pide una verificación diaria; acumular en línea evita recalcular todo al cierre |
| S5 | El director comercial y el administrador no tienen mockup; sus servicios y rutas quedan diseñados, incluida el alta de usuarios por el administrador (POST /api/usuarios). Tampoco tienen mockup el registro de proveedores (HU-15, /api/proveedores) ni el histórico de promociones sugeridas (HU-19, /api/bam/promociones), ni la edición y desactivación de platos, el reenvío de una cotización y los destinatarios de las alertas, que también aparecen en el recuadro «Sin mockup» del mapa. Los seis roles son fijos y el administrador asigna uno al registrar cada usuario | Sus funciones están en el enunciado pero no en el prototipo entregado |
| S6 | El despliegue es en nube con Kubernetes administrado; el proveedor de nube se decide después | Decidir la tecnología lo más tarde posible, como pidió el profesor |
| S7 | La certificación y el sello de tiempo los presta un servicio externo de correo certificado | El sistema agrega los encabezados y el sello con el Decorator, pero la certificación legal la da un tercero |
| S8 | Las réplicas mínimas y máximas del despliegue son valores iniciales | Deben validarse con pruebas de carga dentro del plan de pruebas |
| S9 | El mesero que registra un pedido es el usuario del token JWT; la SPA no lo envía | Así nadie registra un pedido a nombre de otro mesero, y el pedido queda asociado al usuario que lo tomó, como pide HU-01 |
| S10 | Las listas maestras (categorías, productos, unidades, marcas y mesas) y el primer administrador se cargan con la migración inicial del servicio dueño de cada una | Ninguna fuente dice quién las mantiene; la parametrización del administrador cubre parámetros, tema, logos y destinatarios |
| S11 | El análisis de promociones corre cada semana sobre los 7 días anteriores, con las tendencias de una red social y los 3 productos más pedidos; cada sugerencia vale 7 días | HU-19 no fija la red, la ventana ni la cantidad; el adaptador de redes aísla la elección, y los valores son constantes de ms-analitica |
| S12 | Los sistemas externos que consumen el SOAP se registran como clientes en ms-identidad y obtienen su token con OAuth 2.0 client credentials | Ninguna fuente dice cómo se autentican; así el gateway usa un solo mecanismo (JWT) y la auditoría tiene un usuario (client_id) |

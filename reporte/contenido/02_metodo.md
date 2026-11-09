<!-- §2. Correcciones: R-62 (tabla de niveles del C4, DEC-48). -->
# 2. Método de análisis

## 2.1 Fuentes

Se analizaron tres fuentes. La primera es el enunciado del proyecto (López, 2026a), con sus requerimientos funcionales y diecinueve requerimientos no funcionales agrupados en seguridad, desempeño, interfaz gráfica, almacenamiento, mantenimiento, tecnología e integración. La segunda son las 23 ventanas del prototipo (quince de inicio de sesión, mesero, jefe de cocina y auxiliar de compras, y ocho del director de compras), junto con el flujo web que las conecta botón por botón. La tercera son las sesiones de clase, en las que los grupos expusieron los estilos de arquitectura y el profesor presentó el modelo C4 y los patrones de diseño (López, 2026b).

## 2.2 Criterios tomados de las clases

El profesor dejó criterios explícitos que guiaron cada decisión. Un diseño se juzga frente a sus restricciones, así que cada estilo elegido debe responder a un requerimiento no funcional concreto (Bass et al., 2021). Los estilos se combinan: al hacer zoom sobre una caja se puede usar otro estilo por dentro. La tecnología se decide lo más tarde posible para mantener flexibilidad, y las flechas deben llevar su protocolo.

El modelo C4 es obligatorio en sus cuatro niveles, contexto, contenedores, componentes y código (Brown, 2018), y en el nivel de código, con diagrama de clases UML (Object Management Group, 2017), debe verse dónde va cada patrón; los diagramas dinámico y de despliegue son complementarios. La tabla 2 indica dónde está cada uno en este documento. Cada estilo se documenta con la plantilla que usaron los grupos: esquema, lineamientos, políticas y reglas, aplicabilidad, ventajas y desventajas, y un ejemplo.

!tabla 2 | Dónde está cada nivel del C4 | 2.6,2.4,2.4,2.2
| Nivel | Página del .drawio | Figura | Sección |
|---|---|---|---|
| 1. Contexto | 2 | 11 | 14 |
| 2. Contenedores | 3a | 12 | 14 |
| 3. Componentes | 3b | 13 | 14 |
| 4. Código | 4a a 4e | 8 a 10 y A1 a A5 | 12 y anexo A |
| Dinámico | 6 | 7 | 10 |
| Despliegue | 5 | 14 | 14 |

## 2.3 Cómo se leyeron las ventanas

De cada ventana se registró el rol, la acción principal (el botón que cambia datos), la información que crea o consulta y si esa acción cambia lo que ve otro rol. Esta última pregunta fue decisiva. Cuando un clic se resuelve dentro del mismo rol y el usuario espera la respuesta, como registrar un pedido en la ventana 03 y ver PED-0251 en la 04, la comunicación es síncrona. Cuando el clic de un rol modifica la pantalla de otro, como cuando el auxiliar envía una cotización en la 13 y la solicitud del jefe pasa a EN TRÁMITE en la 10, esa costura se modeló como evento. Así aparecieron las fronteras entre servicios y los diez tópicos del broker.

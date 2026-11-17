#!/usr/bin/env python3
# Revisa en PostgreSQL y en Kafka lo que dejó recorrido_corte3.py (corre contra los contenedores con
# docker exec, sin instalar nada): el pedido a nombre del mesero, el plato nuevo en las tres bases, los
# eventos con el usuario del JWT y la correlación del recorrido (DEC-64) y la alerta de stock bajo.
# Uso: python verificar_corte3.py   (COMPOSE_PROJECT_NAME elige otra instancia; por omisión, konrad-gourmet)
import json, os, subprocess, sys

PROYECTO = os.environ.get('COMPOSE_PROJECT_NAME', 'konrad-gourmet')
fallas = []

def ok(cond, texto):
    print(('  ✓ ' if cond else '  ✗ ') + texto)
    if not cond:
        fallas.append(texto)

def sql(base, consulta):
    r = subprocess.run(['docker', 'exec', f'{PROYECTO}-postgres-1', 'psql', '-U', 'postgres', '-d', base, '-tAF', '|', '-c', consulta],
                       capture_output=True, text=True, check=True)
    return [l.split('|') for l in r.stdout.strip().splitlines() if l]

def topico(nombre):
    r = subprocess.run(['docker', 'exec', f'{PROYECTO}-kafka-1', '/opt/kafka/bin/kafka-console-consumer.sh', '--bootstrap-server',
                        'localhost:9092', '--topic', nombre, '--from-beginning', '--timeout-ms', '5000'], capture_output=True, text=True)
    return [json.loads(l) for l in r.stdout.splitlines() if l.startswith('{')]

print('== ms-pedidos')
numero, mesero, total = sql('ms_pedidos', "SELECT p.numero, p.mesero_id, sum(d.cantidad * d.precio_unitario) FROM pedido p "
                            "JOIN detalle_pedido d ON d.pedido_numero = p.numero GROUP BY p.numero ORDER BY p.numero DESC LIMIT 1")[0]
ok(mesero == '1', f'{numero} quedó a nombre del mesero que inició sesión (id {mesero})')
ok(float(total) == 82000, f'{numero} suma $82.000 ({total})')

auditoria = topico('auditoria.evento')
registrado = [e for e in topico('pedido.registrado') if e['numero'] == numero]
ok(len(registrado) == 1, f'pedido.registrado de {numero} está en Kafka')
if registrado:
    e = registrado[0]
    ok(e['usuario'] == 'isabella.torres@konradgourmet.com', f'pedido.registrado lleva el correo del mesero ({e["usuario"]})')
    a = [x for x in auditoria if x['accion'].startswith(f'Pedido {numero} ')]
    ok(bool(a) and a[0]['correlacionId'] == e['correlacionId'], 'su auditoría comparte el correlacionId del recorrido')

print('== ms-menu → ms-pedidos y ms-inventario (menu.actualizado)')
filas = sql('ms_menu', "SELECT id, nombre, activo, imagen_url FROM plato WHERE nombre LIKE 'Sancocho %' ORDER BY id DESC LIMIT 1")
ok(bool(filas), 'el plato del recorrido está en ms_menu')
if filas:
    pid, nombre, activo, imagen = filas[0]
    ok(activo == 'f', f'«{nombre}» quedó inactivo en ms_menu')
    ok(imagen == f's3://imagenes-platos/platos/{pid}.png', f'su imagen está en el almacén S3 ({imagen})')
    cat = sql('ms_pedidos', f'SELECT categoria, precio, activo FROM plato_catalogo WHERE plato_id = {pid}')
    ok(cat == [['Sopas', '29500.00', 'f']], f'ms_pedidos tiene la copia del catálogo: Sopas, $29.500, inactivo ({cat})')
    receta = sql('ms_inventario', f'SELECT producto_id, cantidad FROM receta WHERE plato_id = {pid}')
    ok(receta == [['4', '0.2500']], f'ms_inventario tiene la receta: 250 g de pollo = 0,25 kg ({receta})')
    eventos = [e for e in topico('menu.actualizado') if e['platoId'] == int(pid)]
    ok([e['activo'] for e in eventos] == [True, True, False], 'menu.actualizado: crear, editar y desactivar (3 eventos)')
    ok(all(e['usuario'] == 'carlos.mendoza@konradgourmet.com' for e in eventos), 'los tres a nombre del jefe de cocina')
    for e in eventos:
        a = [x for x in auditoria if x['correlacionId'] == e['correlacionId'] and nombre in x['accion']]
        ok(bool(a), f'«{a[0]["accion"] if a else "?"}» comparte la correlación de su menu.actualizado')
    ok(any(x['accion'] == f'Imagen del plato {nombre} actualizada' and x['usuario'] == 'carlos.mendoza@konradgourmet.com' for x in auditoria),
       'la imagen subida queda en la auditoría a nombre del jefe')

print('== ms-inventario (cierre del día)')
res = sql('ms_inventario', 'SELECT stock_actual, stock_referencia FROM insumo WHERE sucursal_id = 1 AND producto_id = 1')[0]
ok(float(res[0]) / float(res[1]) < 0.25, f'la res quedó bajo el 25 % ({res[0]} de {res[1]} kg)')
# el recorrido a 390 px registra un pedido después del cierre: solo ese queda pendiente (2 bandejas y 1 ajiaco)
pendiente = sql('ms_inventario', 'SELECT producto_id, cantidad FROM consumo_pendiente ORDER BY producto_id')
ok(pendiente == [['1', '1.0000'], ['2', '0.2000'], ['3', '0.3000'], ['4', '0.2000']],
   f'el cierre descontó todo lo anterior: solo queda el consumo del último pedido ({pendiente})')
bajos = [e for e in topico('inventario.stock-bajo') if e['productoId'] == 1]
ok(bool(bajos), 'inventario.stock-bajo de la res está en Kafka')
if bajos:
    e = bajos[-1]
    ok(e['usuario'] == 'sistema', 'el cierre es un job: el evento dice «sistema»')
    ok(any(x['correlacionId'] == e['correlacionId'] and x['accion'].startswith('Consumo diario') for x in auditoria),
       'y comparte correlacionId con la auditoría del mismo cierre')
ok(sql('ms_inventario', "SELECT count(*) FROM shedlock WHERE name = 'ConsumoDiarioJob'") == [['1']], 'ShedLock registró el bloqueo del cierre')

print('\nFALLAS:' if fallas else '\nTodo bien.', *fallas, sep='\n  ')
sys.exit(1 if fallas else 0)

-- =====================================================================
-- V2 - Datos de ejemplo: productos, clientes y ventas.
-- Los ids de categoria, marca, comuna, medio de pago y tipo de entrega
-- son los catalogos cargados en V1.
-- =====================================================================

-- categoria_id: 1 Guitarras, 2 Bajos, 3 Baterías, 4 Teclados, 5 Amplificadores, 6 Micrófonos, 7 Pedales, 8 Accesorios
-- marca_id:     1 Fender, 2 Pearl, 3 Yamaha, 4 Marshall, 5 Shure, 6 Boss, 7 Dunlop
INSERT INTO producto (nombre, descripcion, precio, stock, categoria_id, marca_id) VALUES
  ('Guitarra Electrica Stratocaster', 'Guitarra electrica cuerpo solido, 3 pastillas single coil, ideal para rock y blues.', 349990, 5, 1, 1),
  ('Bajo Electrico Jazz Bass', 'Bajo de 4 cuerdas, cuerpo de tilo, sonido calido y versatil.', 289990, 3, 2, 1),
  ('Bateria Acustica 5 Piezas', 'Set completo de bateria con platillos, ideal para estudio y ensayo.', 549990, 2, 3, 2),
  ('Teclado Digital 61 Teclas', 'Teclado digital con multiples sonidos y ritmos incorporados, sensible al toque.', 199990, 6, 4, 3),
  ('Amplificador de Guitarra 20W', 'Amplificador combo de 20 watts, canal limpio y canal con distorsion.', 89990, 8, 5, 4),
  ('Microfono Vocal Dinamico', 'Microfono dinamico para voz, ideal para presentaciones en vivo.', 45990, 10, 6, 5),
  ('Pedal de Efectos Overdrive', 'Pedal de distorsion suave para guitarra electrica, tono clasico.', 39990, 7, 7, 6),
  ('Set de Puas y Correa', 'Pack de puas variadas mas correa ajustable para guitarra o bajo.', 14990, 25, 8, 7);

-- comuna_id: 1 Viña del Mar, 2 Valparaíso, 3 Concón, 4 Quilpué, ...
INSERT INTO cliente (run, nombre, apellidos, correo, telefono, direccion, comuna_id) VALUES
  ('20123456K', 'Camila', 'Rojas Pérez', 'cliente@gmail.com', '987654321', 'Calle Álvarez 455', 1),
  ('19011022K', 'Ignacio', 'Hidalgo', 'ignacio@duoc.cl', '912345678', 'Av. Brasil 2100', 2);

-- medio_pago_id: 1 Transferencia, 2 Débito, 3 Crédito, 4 Efectivo | tipo_entrega_id: 1 Retiro, 2 Despacho
INSERT INTO venta (fecha, cliente_id, producto_id, cantidad, precio_unitario, medio_pago_id, tipo_entrega_id) VALUES
  ('2026-11-06', 1, 1, 1, 349990, 2, 1),
  ('2026-11-06', 2, 6, 2, 45990, 3, 2);

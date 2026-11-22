-- =====================================================================
-- Sonido Vivo - Script completo para crear y poblar la base de datos MySQL.
-- Es la union de las migraciones Flyway V1 a V6
-- (backend/src/main/resources/db/migration), en el mismo orden.
--
-- IMPORTANTE: para EJECUTAR el proyecto NO hace falta importar este script.
-- Al arrancar el backend, Flyway crea la base sonido_vivo y ejecuta las migraciones solo.
-- Si se importa este script a mano, Flyway encontrara tablas sin su historial y el backend
-- no arrancara: en ese caso borrar la base (DROP DATABASE sonido_vivo;) y arrancar el backend.
-- Este archivo se entrega como respaldo y para revisar el modelo de datos.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS sonido_vivo CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE sonido_vivo;

-- =====================================================================
-- V1 - Tablas normalizadas (3FN) y datos de los catalogos.
-- Flyway ejecuta este archivo UNA sola vez y lo registra en la tabla
-- flyway_schema_history. NO se edita: cualquier cambio va en una V nueva.
-- =====================================================================

-- ---------------------------------------------------------------------
-- Tablas de tipos / catalogos (cada valor se guarda una sola vez)
-- ---------------------------------------------------------------------
CREATE TABLE categoria (
  id     BIGINT      NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(50) NOT NULL,
  CONSTRAINT pk_categoria PRIMARY KEY (id),
  CONSTRAINT uq_categoria_nombre UNIQUE (nombre)
);

CREATE TABLE marca (
  id     BIGINT      NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(50) NOT NULL,
  CONSTRAINT pk_marca PRIMARY KEY (id),
  CONSTRAINT uq_marca_nombre UNIQUE (nombre)
);

CREATE TABLE comuna (
  id     BIGINT      NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(50) NOT NULL,
  CONSTRAINT pk_comuna PRIMARY KEY (id),
  CONSTRAINT uq_comuna_nombre UNIQUE (nombre)
);

CREATE TABLE medio_pago (
  id     BIGINT      NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(40) NOT NULL,
  CONSTRAINT pk_medio_pago PRIMARY KEY (id),
  CONSTRAINT uq_medio_pago_nombre UNIQUE (nombre)
);

CREATE TABLE tipo_entrega (
  id     BIGINT      NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(40) NOT NULL,
  CONSTRAINT pk_tipo_entrega PRIMARY KEY (id),
  CONSTRAINT uq_tipo_entrega_nombre UNIQUE (nombre)
);

-- ---------------------------------------------------------------------
-- Tablas principales (las 3 entidades del backend)
-- ---------------------------------------------------------------------
CREATE TABLE producto (
  id           BIGINT       NOT NULL AUTO_INCREMENT,
  nombre       VARCHAR(100) NOT NULL,
  descripcion  VARCHAR(500),
  precio       INT          NOT NULL,             -- pesos chilenos
  stock        INT          NOT NULL DEFAULT 0,
  categoria_id BIGINT       NOT NULL,
  marca_id     BIGINT       NOT NULL,
  CONSTRAINT pk_producto PRIMARY KEY (id),
  CONSTRAINT ck_producto_precio CHECK (precio >= 0),
  CONSTRAINT ck_producto_stock  CHECK (stock >= 0),
  CONSTRAINT fk_producto_categoria FOREIGN KEY (categoria_id) REFERENCES categoria (id),
  CONSTRAINT fk_producto_marca     FOREIGN KEY (marca_id)     REFERENCES marca (id)
);

CREATE TABLE cliente (
  id        BIGINT       NOT NULL AUTO_INCREMENT,
  run       VARCHAR(9)   NOT NULL,                -- sin puntos ni guion: 20123456K
  nombre    VARCHAR(50)  NOT NULL,
  apellidos VARCHAR(100) NOT NULL,
  correo    VARCHAR(100) NOT NULL,
  telefono  VARCHAR(15),
  direccion VARCHAR(300),                         -- calle y numero
  comuna_id BIGINT       NOT NULL,
  CONSTRAINT pk_cliente PRIMARY KEY (id),
  CONSTRAINT uq_cliente_run    UNIQUE (run),
  CONSTRAINT uq_cliente_correo UNIQUE (correo),
  CONSTRAINT fk_cliente_comuna FOREIGN KEY (comuna_id) REFERENCES comuna (id)
);

-- El total no se guarda porque es un dato calculado (cantidad * precio_unitario).
-- precio_unitario es el precio al momento de la venta (el del producto puede cambiar).
CREATE TABLE venta (
  id              BIGINT NOT NULL AUTO_INCREMENT,
  fecha           DATE   NOT NULL,
  cliente_id      BIGINT NOT NULL,
  producto_id     BIGINT NOT NULL,
  cantidad        INT    NOT NULL,
  precio_unitario INT    NOT NULL,
  medio_pago_id   BIGINT NOT NULL,
  tipo_entrega_id BIGINT NOT NULL,
  CONSTRAINT pk_venta PRIMARY KEY (id),
  CONSTRAINT ck_venta_cantidad CHECK (cantidad > 0),
  CONSTRAINT ck_venta_precio   CHECK (precio_unitario >= 0),
  CONSTRAINT fk_venta_cliente      FOREIGN KEY (cliente_id)      REFERENCES cliente (id),
  CONSTRAINT fk_venta_producto     FOREIGN KEY (producto_id)     REFERENCES producto (id),
  CONSTRAINT fk_venta_medio_pago   FOREIGN KEY (medio_pago_id)   REFERENCES medio_pago (id),
  CONSTRAINT fk_venta_tipo_entrega FOREIGN KEY (tipo_entrega_id) REFERENCES tipo_entrega (id)
);

-- ---------------------------------------------------------------------
-- Datos de los catalogos (los mismos que usa el frontend)
-- ---------------------------------------------------------------------
INSERT INTO categoria (id, nombre) VALUES
  (1, 'Guitarras'), (2, 'Bajos'), (3, 'Baterías'), (4, 'Teclados'),
  (5, 'Amplificadores'), (6, 'Micrófonos'), (7, 'Pedales'), (8, 'Accesorios');

INSERT INTO marca (id, nombre) VALUES
  (1, 'Fender'), (2, 'Pearl'), (3, 'Yamaha'), (4, 'Marshall'),
  (5, 'Shure'), (6, 'Boss'), (7, 'Dunlop');

INSERT INTO comuna (id, nombre) VALUES
  (1, 'Viña del Mar'), (2, 'Valparaíso'), (3, 'Concón'), (4, 'Quilpué'),
  (5, 'Villa Alemana'), (6, 'Quillota'), (7, 'San Antonio'), (8, 'Los Andes');

INSERT INTO medio_pago (id, nombre) VALUES
  (1, 'Transferencia bancaria'), (2, 'Tarjeta de débito'),
  (3, 'Tarjeta de crédito'), (4, 'Efectivo al retirar');

INSERT INTO tipo_entrega (id, nombre) VALUES
  (1, 'Retiro en tienda'), (2, 'Despacho a domicilio');

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

-- =====================================================================
-- V3 - Usuarios que pueden iniciar sesion (autenticacion JWT).
-- La contrasena NUNCA se guarda en texto plano: se guarda su hash BCrypt.
-- =====================================================================

CREATE TABLE usuario (
  id         BIGINT       NOT NULL AUTO_INCREMENT,
  correo     VARCHAR(100) NOT NULL,
  contrasena VARCHAR(100) NOT NULL,               -- hash BCrypt (60 caracteres)
  CONSTRAINT pk_usuario PRIMARY KEY (id),
  CONSTRAINT uq_usuario_correo UNIQUE (correo)
);

-- Usuario de prueba (el mismo del frontend): admin@duoc.cl / admin123
INSERT INTO usuario (correo, contrasena) VALUES
  ('admin@duoc.cl', '$2a$10$0rDhDGqsssbRagfWyfjU6e/ALL1QXmGU.lFaBtzlerHiP8nQWtvfC');

-- =====================================================================
-- V4 - Roles de usuario: ADMIN (administrador) y USER (cliente).
-- Igual que categoria, marca o comuna, el rol es una tabla de tipos (3FN):
-- el nombre del rol se guarda una sola vez y usuario solo guarda rol_id.
-- No se edita V3 (ya aplicada): el cambio va en esta migracion nueva.
-- =====================================================================

CREATE TABLE rol (
  id     BIGINT      NOT NULL AUTO_INCREMENT,
  nombre VARCHAR(20) NOT NULL,
  CONSTRAINT pk_rol PRIMARY KEY (id),
  CONSTRAINT uq_rol_nombre UNIQUE (nombre)
);

-- rol_id: 1 ADMIN, 2 USER
INSERT INTO rol (id, nombre) VALUES (1, 'ADMIN'), (2, 'USER');

-- Todo usuario parte como USER (rol_id = 2)
ALTER TABLE usuario ADD COLUMN rol_id BIGINT NOT NULL DEFAULT 2;
ALTER TABLE usuario ADD CONSTRAINT fk_usuario_rol FOREIGN KEY (rol_id) REFERENCES rol (id);

-- El usuario creado en V3 es el administrador
UPDATE usuario SET rol_id = 1 WHERE correo = 'admin@duoc.cl';

-- Cliente de prueba (el mismo del frontend): cliente@gmail.com / cliente123 (hash BCrypt)
INSERT INTO usuario (correo, contrasena, rol_id) VALUES
  ('cliente@gmail.com', '$2a$10$ObIvGklM/fg9KY/ML/2eFezvVREkBrgKy3d.jzOPO5bUmLxnF63Dy', 2);

-- =====================================================================
-- V5 - Primera forma normal (1FN) en cliente: "apellidos" guardaba dos
-- datos en una sola columna (paterno y materno). Se separa en
-- apellido_paterno (obligatorio) y apellido_materno (opcional, hay
-- personas con un solo apellido).
-- No se edita V1 (ya aplicada): el cambio va en esta migracion nueva.
-- =====================================================================

ALTER TABLE cliente ADD COLUMN apellido_paterno VARCHAR(50) NOT NULL DEFAULT '' AFTER nombre;
ALTER TABLE cliente ADD COLUMN apellido_materno VARCHAR(50) NULL AFTER apellido_paterno;

-- Reparte los datos existentes: lo que esta antes del primer espacio es el paterno, el resto el materno
UPDATE cliente SET
  apellido_paterno = CASE WHEN LOCATE(' ', TRIM(apellidos)) > 0
                          THEN SUBSTRING(TRIM(apellidos), 1, LOCATE(' ', TRIM(apellidos)) - 1)
                          ELSE TRIM(apellidos) END,
  apellido_materno = CASE WHEN LOCATE(' ', TRIM(apellidos)) > 0
                          THEN TRIM(SUBSTRING(TRIM(apellidos), LOCATE(' ', TRIM(apellidos)) + 1))
                          ELSE NULL END;

-- El valor por defecto solo servia para agregar la columna a filas existentes
ALTER TABLE cliente ALTER COLUMN apellido_paterno DROP DEFAULT;

ALTER TABLE cliente DROP COLUMN apellidos;


-- =====================================================================
-- V6 - Relacion entre la cuenta (usuario) y la ficha del cliente.
-- Antes usuario y rol quedaban como un grupo suelto: usuario y cliente
-- solo compartian el correo, sin llave foranea. Ahora usuario guarda
-- cliente_id, asi todas las tablas quedan conectadas:
--   rol -> usuario -> cliente -> venta
-- cliente_id puede ser NULL: el administrador no es cliente (no compra).
-- UNIQUE: una ficha de cliente pertenece a una sola cuenta (relacion 1 a 1).
-- No se edita V3 ni V4 (ya aplicadas): el cambio va en esta migracion nueva.
-- =====================================================================

ALTER TABLE usuario ADD COLUMN cliente_id BIGINT NULL;
ALTER TABLE usuario ADD CONSTRAINT uq_usuario_cliente UNIQUE (cliente_id);
ALTER TABLE usuario ADD CONSTRAINT fk_usuario_cliente FOREIGN KEY (cliente_id) REFERENCES cliente (id);

-- Enlaza las cuentas que ya existen con su ficha (mismo correo).
-- El administrador no tiene ficha de cliente y queda con cliente_id NULL.
UPDATE usuario SET cliente_id = (SELECT c.id FROM cliente c WHERE c.correo = usuario.correo);

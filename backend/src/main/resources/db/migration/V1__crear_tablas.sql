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

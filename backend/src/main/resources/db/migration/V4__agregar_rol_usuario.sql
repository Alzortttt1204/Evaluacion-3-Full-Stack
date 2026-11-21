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

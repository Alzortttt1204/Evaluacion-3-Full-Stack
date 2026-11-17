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

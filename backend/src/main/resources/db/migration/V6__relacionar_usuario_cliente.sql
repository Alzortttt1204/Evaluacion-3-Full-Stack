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

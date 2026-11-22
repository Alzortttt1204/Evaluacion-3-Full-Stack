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

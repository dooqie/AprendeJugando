-- Datos semilla de la aplicación Aprende Jugando
-- Coincide con los INSERT ejecutados en DBHelper.onCreate()

-- Usuario administrador / padre (rol_id = 1)
-- Contraseña: admin123
INSERT INTO usuarios (nombre, password_hash, rol_id, avatar, fecha_alta, padre_id)
VALUES ('admin','240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9',1, 'default', datetime('now'), NULL);

-- Usuario niño de prueba (rol_id = 2), hijo de admin
-- Contraseña: user123
INSERT INTO usuarios (nombre, password_hash, rol_id, avatar, fecha_alta, padre_id)
VALUES ('user','e606e38b0d8c19b24cf0ee3808183162ea7cd63ff7912dbb22b5e803286b4446',2, 'default', datetime('now'),(SELECT id FROM usuarios WHERE nombre = 'admin' LIMIT 1));

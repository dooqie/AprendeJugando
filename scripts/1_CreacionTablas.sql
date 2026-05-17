-- Esquema SQLite de la aplicación Aprende Jugando
-- Refleja el esquema creado por DBHelper.java

-- Roles implícitos:
--   rol_id = 1 → Administrador / Padre
--   rol_id = 2 → Niño (cuenta hija)

CREATE TABLE IF NOT EXISTS usuarios (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre        TEXT    UNIQUE NOT NULL,
    password_hash TEXT    NOT NULL,
    rol_id        INTEGER DEFAULT 2,
    avatar        TEXT,
    fecha_alta    TEXT,
    padre_id      INTEGER,
    FOREIGN KEY (padre_id) REFERENCES usuarios(id)
);

-- Cada fila representa una partida completa jugada por un usuario.
-- tipo_ejercicio es una clave textual: 'sumar', 'restar', 'multiplicar',
-- 'dividir', 'logica', 'figuras', 'animales', 'memoria', 'mixto'.
CREATE TABLE IF NOT EXISTS resultados (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    usuario_id     INTEGER NOT NULL,
    tipo_ejercicio TEXT    NOT NULL,
    aciertos       INTEGER,
    fallos         INTEGER,
    tiempo         INTEGER,   -- segundos efectivos jugados
    fecha          TEXT,      -- formato 'yyyy-MM-dd HH:mm:ss'
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

-- Controla qué ejercicios puede ver/acceder cada cuenta de niño.
-- El padre activa o desactiva ejercicios (activo = 1 / 0).
CREATE TABLE IF NOT EXISTS permisos (
    usuario_id     INTEGER NOT NULL,
    tipo_ejercicio TEXT    NOT NULL,
    activo         INTEGER NOT NULL DEFAULT 1,
    PRIMARY KEY (usuario_id, tipo_ejercicio),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

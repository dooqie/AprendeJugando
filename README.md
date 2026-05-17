# Aprende Jugando

**Autor:** José López Mohedano
**Versión:** 1.0
**Plataforma:** Android (mínimo API 24 – Android 7.0 Nougat)

---

## Descripción

Aprende Jugando es una aplicación Android educativa orientada a niños en edad escolar. Su objetivo es reforzar el aprendizaje de matemáticas y habilidades cognitivas a través de ejercicios interactivos presentados en formato de juego, con temporizador, feedback sonoro y sistema de niveles progresivos.

La app incluye un sistema de cuentas con dos roles: **padre/administrador** y **niño**. El padre puede registrar cuentas infantiles y controlar qué ejercicios tiene disponibles cada hijo.

---

## Funcionalidades principales

### Ejercicios disponibles
| Ejercicio | Descripción |
|-----------|-------------|
| Sumar | Sumas con operandos aleatorios adaptados a la dificultad |
| Restar | Restas con resultado siempre positivo |
| Multiplicar | Tablas de multiplicar con dificultad progresiva |
| Dividir | Divisiones exactas generadas dinámicamente |
| Lógica | Patrones numéricos y secuencias lógicas |
| Figuras | Identificación de formas geométricas |
| Animales | Reconocimiento de animales por imagen y sonido |
| Memoria | Juego de pares de cartas |
| Mixto | Combinación aleatoria de operaciones matemáticas |

### Sistema de juego
- Cuatro opciones de respuesta por pregunta (tipo test)
- Temporizador por partida según dificultad
- Tres niveles de dificultad: **Fácil**, **Medio** y **Difícil**
- Subida de nivel automática al encadenar respuestas correctas consecutivas
- Fin de partida por tiempo agotado o tres fallos consecutivos
- Pantalla de informe al finalizar (aciertos, fallos, nivel alcanzado)
- Pantalla de nivel superado con animación al avanzar de nivel

### Sistema de usuarios
- Registro e inicio de sesión con contraseña (almacenada como hash SHA-256)
- Rol **Administrador** (padre): gestiona cuentas de hijos y permisos
- Rol **Niño**: accede solo a los ejercicios habilitados por su padre
- Panel de administración para activar/desactivar ejercicios por hijo

### Seguimiento del progreso
- Historial de partidas jugadas por usuario
- Pantalla de progreso con estadísticas por tipo de ejercicio
- Gráficas de rendimiento (MPAndroidChart)

---

## Estructura del proyecto

```
app/src/main/java/com/aprende/jugando/
├── database/          # DBHelper (SQLite) y PermisosRepository
├── model/             # Clases de datos: Usuario, Resultado, Rol
├── ui/
│   ├── auth/          # Login, Registro, Permisos de hijo
│   ├── admin/         # Panel de administración
│   ├── ejercicios/    # Una Activity por tipo de ejercicio + utilidades
│   ├── main/          # MainActivity (menú principal)
│   └── onboarding/    # Pantallas de bienvenida (primera ejecución)
└── utils/             # SessionManager, MusicPlayer, ButtonAnimUtils, PasswordUtils
```

---

## Base de datos

SQLite local gestionada por `DBHelper`. Tres tablas:

- **usuarios** — nombre, hash de contraseña, rol, avatar, fecha de alta y referencia al padre
- **resultados** — partidas jugadas: tipo de ejercicio, aciertos, fallos, tiempo y fecha
- **permisos** — mapa usuario↔ejercicio que controla el acceso de cada niño

---

## Tecnologías utilizadas

- **Java** — lenguaje principal
- **Android SDK** — API 24 mínimo, compilado con API 35
- **SQLite** — base de datos local
- **MPAndroidChart** — gráficas de progreso
- **Lottie** — animaciones vectoriales
- **ViewPager2** — pantallas de onboarding
- **Material Design** — componentes de interfaz

---

## Cómo importar el proyecto

1. Abrir **Android Studio**
2. `File → Open` y seleccionar la carpeta `AprendeJugando`
3. Esperar a que Gradle sincronice las dependencias
4. Ejecutar en un emulador o dispositivo con Android 7.0 o superior

### Usuarios de prueba
| Usuario | Contraseña | Rol |
|---------|-----------|-----|
| admin | admin123 | Administrador / Padre |
| user | user123 | Niño |

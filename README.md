# Lou Araya Macramé — App móvil 

Aplicación Android nativa (Kotlin + Jetpack Compose) para el emprendimiento de macramé artesanal **Lou Araya**.
Permite a clientes explorar el catálogo, guardar favoritos, escanear códigos QR de las piezas, ver ferias en un mapa
y solicitar cotizaciones; y a la administradora gestionar productos, campañas y solicitudes.

## Integrantes
martin ignacio lopez bustos todo

## Funcionalidades
- **Dos perfiles** (Cliente / Administrador) con navegación inferior distinta y datos separados por usuario.
- **Registro e inicio de sesión** con validación por campo en vivo, íconos de estado y mensajes de error.
- **Catálogo** con búsqueda, categorías, detalle, favoritos y solicitud de cotización.
- **Solicitudes** con estados (Pendiente → Confirmado → Entregado / Rechazada) y **notificaciones** internas.
- **Administración**: CRUD de productos (con foto desde la cámara), campañas con fases y publicación.
- **Recursos nativos**: 📷 cámara (escáner QR con CameraX + ML Kit y foto de producto) y 📍 ubicación (mapa OpenStreetMap con osmdroid). Permisos en tiempo de ejecución con explicación y acceso a ajustes.
- **Animaciones**: bienvenida escalonada, chips, corazón de favorito, check de éxito, tarjetas expandibles, contadores del panel, línea de escaneo, transiciones de navegación.

## Arquitectura
```
com.louaraya.macrame
├── data/      Room (entidades, DAOs), repositorio, sesión, datos semilla
├── domain/    Modelos y Validators (reglas puras, sin Android)
├── ui/
│   ├── theme/       Paleta Verde Salvia y Ceniza, tipografía, formas
│   ├── components/  Campos de formulario, tarjetas, barra flotante, permisos
│   ├── viewmodel/   Estado de cada pantalla (StateFlow) + validación
│   ├── screens/     Pantallas Compose
│   └── navigation/  Rutas y NavHost
└── util/      Hash de contraseña, formateadores
```
MVVM + repositorio, inyección manual (`AppContainer`) y persistencia local con Room (SQLite).
Las contraseñas se guardan con SHA-256 + sal.

## Cómo ejecutar
1. Abrir la carpeta con **Android Studio** (Koala o superior, JDK 17).
2. Esperar la sincronización de Gradle (se regenera el wrapper si falta).
3. Ejecutar en un emulador o dispositivo con API 26+. El mapa necesita internet; el escáner funciona mejor en un dispositivo real (en emulador se puede escribir el código a mano, ej. `LAM-0001`).
4. Pruebas: `./gradlew test`.

## Credenciales de demostración
| Perfil | Correo | Contraseña |
|---|---|---|
| Administrador | admin@louaraya.cl | Admin123! |
| Cliente | valentina@demo.cl | Demo1234 |

## Cobertura de la rúbrica
| Criterio | Dónde verlo |
|---|---|
| Interfaz y navegación | `ui/navigation/AppRoot.kt`, `ui/screens/` |
| Formularios y validación visual | `ui/components/FormComponents.kt`, pantallas de registro, solicitud y admin |
| Validación desacoplada | `domain/Validators.kt` + `ValidatorsTest.kt` |
| Animaciones | `LayoutComponents.kt`, `ScannerScreen.kt`, `RequestScreens.kt` |
| Arquitectura y persistencia | `data/`, `ui/viewmodel/` |
| Recursos nativos | `ScannerScreen.kt`, `MapScreen.kt`, `AdminScreens.kt` (foto) |
| GitHub y Trello | `docs/COLABORACION.md` |

> Los datos y productos son ficticios. Backend remoto (Retrofit/Spring Boot) queda para la evaluación final; la capa de repositorio ya está preparada.

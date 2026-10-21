# Plan de colaboración (GitHub + Trello)

## Tablero Trello (columnas)
`Backlog` → `En progreso` → `En revisión` → `Hecho`. Una tarjeta por funcionalidad, con responsable y checklist.

| Tarjeta | Responsable |
|---|---|
| Configuración del proyecto, Gradle y tema | Integrante 1 |
| Room: entidades, DAOs, repositorio, semilla | Integrante 1 |
| Validators + pruebas unitarias | Integrante 2 |
| Autenticación (bienvenida, login, registro) | Integrante 2 |
| Catálogo, detalle y favoritos | Integrante 1 |
| Solicitudes y notificaciones | Integrante 2 |
| Escáner QR (cámara) | Integrante 1 |
| Mapa (ubicación) | Integrante 2 |
| Panel de administración | Alternar |
| README y documentación | Ambos |

## Flujo Git
- `main` estable; ramas `feature/<tema>`; Pull Request revisado por la otra persona.
- Commits pequeños, en infinitivo y técnicos. Ejemplos:
  - `feat(data): agregar entidades y DAOs de Room`
  - `feat(domain): centralizar validaciones en Validators`
  - `feat(ui): campo de texto con ícono de error y validación en vivo`
  - `feat(camera): escáner QR con CameraX y ML Kit`
  - `feat(map): mostrar ferias en osmdroid con permiso de ubicación`
  - `test(domain): cubrir validaciones de correo y contraseña`
  - `docs: README con pasos de ejecución`
- Repartir los commits entre ambos integrantes: debe verse trabajo real de cada uno.

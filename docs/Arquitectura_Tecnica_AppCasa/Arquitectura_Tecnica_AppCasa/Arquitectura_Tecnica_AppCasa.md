


**AppCasa**

Documento de Arquitectura Técnica

Versión 1.0 · MVP

Mayo 2025



# **1. Visión y objetivos**
AppCasa es una aplicación móvil diseñada como un sistema operativo del hogar. Su arquitectura es modular y enchufable: cada funcionalidad es un módulo independiente que puede activarse, desactivarse o ampliarse sin afectar al núcleo.

## **1.1 Objetivos técnicos**
- Arquitectura modular y extensible desde el primer día.
- API REST versionada, documentada con OpenAPI / Swagger.
- Frontend Ionic + Angular con lazy loading por módulo.
- Backend Spring Boot 3 con Java 21.
- Base de datos PostgreSQL con Flyway para migraciones.
- Autenticación JWT stateless, multiusuario y multhogar.
- Notificaciones locales en el dispositivo (Capacitor).
- Sincronización entre dispositivos a través del backend.


# **2. Stack tecnológico**

|**Capa**|**Tecnología**|
| :- | :- |
|Frontend móvil|Ionic Framework 7 + Angular 17 + Capacitor 5|
|Backend|Spring Boot 3.3 · Java 21 · Maven|
|Base de datos|PostgreSQL 16|
|Migraciones BD|Flyway|
|Autenticación|JWT (jjwt 0.12) + Spring Security 6|
|Notificaciones|Capacitor Local Notifications|
|Documentación API|Springdoc OpenAPI 2 (Swagger UI)|
|Control de versiones|Git + GitHub|
|CI/CD|GitHub Actions|
|Contenedor|Docker + Docker Compose|


# **3. Arquitectura general del sistema**
La aplicación sigue una arquitectura cliente-servidor clásica con separación clara de responsabilidades:

┌─────────────────────────────────┐

│  Dispositivo móvil              │

│  Ionic + Angular + Capacitor    │

│  ┌──────────────────────────┐   │

│  │  Módulos (lazy loaded)   │   │

│  │  Dashboard / Tareas /    │   │

│  │  Mascotas / Familia /    │   │

│  │  Calendario / Listas...  │   │

│  └──────────────────────────┘   │

│  CoreModule: Auth, Services,    │

│  Interceptors, Guards, Models   │

└──────────┬──────────────────────┘

`           `│ HTTPS + JWT

`           `▼

┌─────────────────────────────────┐

│  Spring Boot API REST           │

│  /api/v1/...                    │

│  ┌──────────────────────────┐   │

│  │  Controllers (API)       │   │

│  │  Services (Application)  │   │

│  │  Repositories (Domain)   │   │

│  └──────────────────────────┘   │

│  Spring Security + JWT Filter   │

└──────────┬──────────────────────┘

`           `│ JPA / Hibernate

`           `▼

┌─────────────────────────────────┐

│  PostgreSQL 16                  │

│  Flyway migrations              │

└─────────────────────────────────┘


# **4. Diseño del Backend**
## **4.1 Estructura de paquetes**
com.appcasa

├── AppCasaApplication.java

├── domain/

│   ├── common/          ← BaseEntity, auditoría

│   ├── hogar/           ← Hogar, HogarRepository

│   ├── usuario/         ← Usuario, UsuarioRepository

│   ├── miembro/         ← MiembroHogar

│   ├── tarea/           ← Tarea, TareaAsignacion

│   ├── recordatorio/    ← Recordatorio

│   ├── evento/          ← Evento

│   └── lista/           ← Lista, ListaItem

├── application/         ← Servicios (casos de uso)

│   ├── tarea/           ← TareaService, TareaRequest

│   ├── recordatorio/

│   ├── miembro/

│   └── auth/            ← AuthService, JwtService

└── infrastructure/

`    `├── api/             ← Controllers REST

`    `├── security/        ← SecurityConfig, JwtFilter

`    `└── exception/       ← GlobalExceptionHandler

## **4.2 Convenciones de API REST**

|**Verbo**|**Uso**|
| :- | :- |
|GET|Lectura de recursos|
|POST|Creación de recursos|
|PUT|Actualización completa|
|PATCH|Actualización parcial (ej: completar tarea)|
|DELETE|Borrado lógico (cambio de estado)|

Base URL: /api/v1/{recurso}

Formato de respuesta: JSON. Errores: RFC 9457 ProblemDetail.

## **4.3 Seguridad JWT**
- Login: POST /api/v1/auth/login → devuelve token (24h) y refreshToken (7d).
- Cada petición incluye: Authorization: Bearer <token>
- JwtAuthFilter valida el token antes de llegar al controlador.
- Los roles por hogar (ADMIN, COLABORADOR, SOLO\_LECTURA) se guardan en TB\_HOGAR\_USUARIO.


# **5. Diseño del Frontend**
## **5.1 Estructura de módulos Angular**
src/app/

├── app.module.ts

├── app-routing.module.ts

├── core/                 ← singleton, cargado una vez

│   ├── core.module.ts

│   ├── models/           ← interfaces de dominio

│   ├── services/         ← AuthService, TareaService...

│   ├── interceptors/     ← AuthInterceptor

│   └── guards/           ← AuthGuard

├── shared/               ← componentes reutilizables

│   ├── shared.module.ts

│   └── components/       ← tarjeta-tarea, avatar-mascota...

└── features/             ← módulos lazy loaded

`    `├── auth/

`    `├── dashboard/

`    `├── tareas/

`    `├── recordatorios/

`    `├── calendario/

`    `├── mascotas/

`    `├── familia/

`    `├── listas/

`    `└── calculadoras/

## **5.2 Navegación principal (Tab Bar)**

|**Pestaña**|**Ruta**|
| :- | :- |
|🏠 Inicio|/dashboard|
|✅ Tareas|/tareas|
|📅 Calendario|/calendario|
|🐾 Mascotas|/mascotas|
|⚙️ Ajustes|/ajustes|

## **5.3 Gestión de estado**
MVP: servicios Angular con BehaviorSubject y RxJS. Sin NgRx en la primera versión para mantener la complejidad baja. Si la app crece (más de 5 módulos activos simultáneos), se evaluará migrar a NgRx Signal Store.


# **6. Base de datos**
## **6.1 Tablas principales del MVP**

|**Tabla**|**Descripción**|
| :- | :- |
|TB\_HOGAR|Hogares|
|TB\_USUARIO|Cuentas de usuario|
|TB\_HOGAR\_USUARIO|Relación hogar ↔ usuario + rol|
|TB\_MIEMBRO\_HOGAR|Personas y mascotas del hogar|
|TB\_TAREA|Tareas del hogar|
|TB\_TAREA\_ASIGNACION|Asignación de tareas a miembros|
|TB\_RECORDATORIO|Recordatorios y notificaciones|
|TB\_EVENTO|Eventos (cumpleaños, vacunas...)|
|TB\_LISTA|Listas de compra y otras|
|TB\_LISTA\_ITEM|Elementos de una lista|
|TB\_HERRAMIENTA|Módulos disponibles en la app|
|TB\_CONFIGURACION|Preferencias por hogar/usuario|

## **6.2 Tablas maestras (catálogos)**
- TM\_TIPO\_MIEMBRO: PERSONA, PERRO, GATO, TORTUGA, AVE, OTRO
- TM\_ESTADO\_GENERAL: ACTIVO, INACTIVO, ELIMINADO
- TM\_PRIORIDAD: BAJA, MEDIA, ALTA, URGENTE
- TM\_TIPO\_RECORDATORIO: PUNTUAL, DIARIO, SEMANAL, MENSUAL, ANUAL, CUSTOM
- TM\_ROL\_HOGAR: ADMIN, COLABORADOR, SOLO\_LECTURA
- TM\_TIPO\_EVENTO: CUMPLEANOS, VACUNA, VETERINARIO, CITA\_MEDICA, ANIVERSARIO, OTRO

## **6.3 Estrategia de migraciones**
Se usa Flyway. Todos los cambios de esquema se hacen mediante scripts versionados en:

src/main/resources/db/migration/

`  `V1\_\_init\_schema.sql

`  `V2\_\_datos\_maestros.sql

`  `V3\_\_...sql

Nunca se modifica un script ya ejecutado en producción. Los cambios retrocompatibles van en nuevas versiones.


# **7. Notificaciones**
En el MVP se usan notificaciones locales mediante el plugin @capacitor/local-notifications, sin necesidad de servidor push.

- Al crear o actualizar un recordatorio, el frontend programa la notificación local.
- Al iniciar la app, se sincronizan los recordatorios con el backend y se reprograman.
- Para notificaciones push (fase 2) se usará Firebase Cloud Messaging (FCM).


# **8. MVP técnico — orden de desarrollo**

|**Fase**|**Contenido**|
| :- | :- |
|Fase 1 · Cimientos|BD + Flyway + Spring Boot base + Auth JWT + Ionic base|
|Fase 2 · Hogar y usuarios|Registro, login, crear hogar, invitar al segundo usuario|
|Fase 3 · Miembros|CRUD de personas y mascotas del hogar|
|Fase 4 · Tareas|CRUD tareas, asignación, completar, prioridades|
|Fase 5 · Recordatorios|CRUD recordatorios + notificaciones locales|
|Fase 6 · Calendario|Vista mensual con tareas, recordatorios y cumpleaños|
|Fase 7 · Listas|Listas de compra y checklists|
|Fase 8 · Dashboard|Resumen integrado de todas las fuentes|
|Fase 9 · Calculadoras|Herramientas de cálculo (gastos, dosis mascota, IMC...)|


# **9. CI/CD y despliegue**
## **9.1 GitHub Actions**
- Rama main: build + tests + deploy a producción.
- Ramas feature/\*: build + tests en cada push.

## **9.2 Docker Compose (desarrollo local)**
services:

`  `db:

`    `image: postgres:16

`    `environment:

`      `POSTGRES\_DB: appcasa\_dev

`      `POSTGRES\_USER: appcasa

`      `POSTGRES\_PASSWORD: appcasa

`    `ports:

`      `- "5432:5432"

`  `backend:

`    `build: ./backend

`    `depends\_on: [db]

`    `environment:

`      `SPRING\_PROFILES\_ACTIVE: dev

`    `ports:

`      `- "8080:8080"


# **10. Roadmap técnico**

|**Versión**|**Funcionalidades**|
| :- | :- |
|v1 MVP|Auth, Hogar, Miembros, Tareas, Recordatorios, Calendario, Listas, Dashboard|
|v1.1|Calculadoras, Gestión de mascotas avanzada, Historial médico|
|v1.2|Notificaciones push (FCM), Tema oscuro, Widgets de inicio|
|v2.0|Finanzas familiares, Inventario del hogar, Gestión de vehículos|
|v2.x|Asistente IA, Recetas, Huerto, Turnos laborales avanzados|


# **11. Conclusión**
Este documento establece la arquitectura técnica de AppCasa sobre una base sólida, modular y preparada para crecer. El stack Ionic + Angular + Spring Boot + PostgreSQL aprovecha la experiencia existente en Java y TypeScript/Angular, minimizando la curva de aprendizaje y maximizando la productividad desde el primer commit.

Cada capa está diseñada para ser independiente: añadir un nuevo módulo es tan sencillo como crear una carpeta en features/ en el frontend y un nuevo paquete en el backend, sin tocar el núcleo de la aplicación.

**AppCasa está lista para convertirse en el sistema operativo del hogar.**

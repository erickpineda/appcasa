**Modelo de Base de Datos Completo y Profesional\
AppCasa**

Diseño de base de datos para una aplicación modular de gestión del hogar, familia, mascotas, tareas, recordatorios y herramientas.
# **1. Principios de Diseño**
- Arquitectura modular y extensible.
- Separación entre usuarios, hogares y miembros.
- Soporte para personas y mascotas como miembros del hogar.
- Borrado lógico mediante estados.
- Auditoría y trazabilidad.
- Preparado para sincronización en la nube.
# **2. Entidades Principales**
- TB\_HOGAR
- TB\_USUARIO
- TB\_HOGAR\_USUARIO
- TB\_MIEMBRO\_HOGAR
- TB\_TAREA
- TB\_TAREA\_ASIGNACION
- TB\_RECORDATORIO
- TB\_EVENTO
- TB\_TURNO
- TB\_LISTA
- TB\_LISTA\_ITEM
- TB\_HERRAMIENTA
- TB\_CONFIGURACION
# **3. Tablas Maestras**
- TM\_TIPO\_MIEMBRO
- TM\_ESTADO\_GENERAL
- TM\_PRIORIDAD
- TM\_TIPO\_RECORDATORIO
- TM\_ROL\_HOGAR
- TM\_TIPO\_EVENTO
# **4. Relaciones Principales**
- Un hogar tiene múltiples usuarios.
- Un hogar tiene múltiples miembros.
- Un usuario puede pertenecer a varios hogares.
- Una tarea puede asignarse a varios miembros.
- Un recordatorio puede asociarse a tareas, eventos o miembros.
- Una lista contiene múltiples elementos.
# **5. Tabla TB\_HOGAR**
- Identificador del hogar.
- Nombre.
- Descripción.
- Estado.
- Fechas de auditoría.
# **6. Tabla TB\_USUARIO**
- Credenciales y datos personales.
- Configuraciones y preferencias.
# **7. Tabla TB\_HOGAR\_USUARIO**
- Relaciona usuarios con hogares y roles.
# **8. Tabla TB\_MIEMBRO\_HOGAR**
- Representa personas y mascotas.
- Tipo de miembro.
- Estado.
- Fecha de nacimiento.
- Datos adicionales.
# **9. Tabla TB\_TAREA**
- Título y descripción.
- Prioridad.
- Fechas.
- Periodicidad.
- Estado.
# **10. Tabla TB\_TAREA\_ASIGNACION**
- Asignación de tareas a miembros.
# **11. Tabla TB\_RECORDATORIO**
- Fecha y hora.
- Reglas de repetición.
- Configuración de notificaciones.
# **12. Tabla TB\_EVENTO**
- Cumpleaños, citas, vacunas y otros eventos.
# **13. Tabla TB\_TURNO**
- Importación y sincronización de turnos laborales.
# **14. Tabla TB\_LISTA y TB\_LISTA\_ITEM**
- Listas de compra y otros checklists.
# **15. Tabla TB\_HERRAMIENTA**
- Registro de módulos y utilidades activables.
# **16. Tabla TB\_CONFIGURACION**
- Preferencias del hogar y de usuarios.
# **17. Auditoría**
- created\_at
- updated\_at
- created\_by
- updated\_by
- version
# **18. Estrategia de Extensión**
- Añadir nuevas herramientas sin alterar el núcleo.
# **19. MVP de Base de Datos**
- Hogar, Usuario, Miembro, Tarea, Recordatorio, Evento.
# **20. Conclusión**
- Modelo robusto, escalable y preparado para convertirse en el sistema operativo del hogar.

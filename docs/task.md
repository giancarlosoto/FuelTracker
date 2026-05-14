# FuelTracker Android Auto - Task List

## Fase 1: Estructura del Proyecto Android
- [x] Crear estructura de directorios del proyecto Gradle
- [x] Crear archivos de configuración Gradle (settings, build)
- [x] Crear `gradle.properties`
- [x] Crear Gradle Wrapper properties

## Fase 2: Módulo App (Base Android)
- [x] Crear `AndroidManifest.xml` con permisos de Car Hardware
- [x] Crear la base de datos Room (`FuelDatabase`, `FuelEntry`, `FuelDao`)
- [x] Crear `FuelRepository` para lógica de acceso a datos
- [x] Crear `MainViewModel` con la lógica de negocio
- [x] Crear `MainActivity` con UI Jetpack Compose (pantalla del celular)
- [x] Crear `ui/theme/` con colores, tipografía y tema

## Fase 3: Módulo Android Auto (Proyección)
- [x] Crear `FuelTrackerCarAppService` (punto de entrada para Android Auto)
- [x] Crear `FuelTrackerSession` (sesión de la app en el auto)
- [x] Crear `DashboardScreen` (pantalla principal en el auto)
- [x] Crear `RecordFuelScreen` (pantalla de registro de carga)
- [x] Crear `VehicleDataManager` (lectura nativa de telemetría)
- [x] Crear archivo XML de descripción del auto (`automotive_app_desc.xml`)

## Fase 4: Integración Google Assistant (Voz)
- [x] Crear `shortcuts.xml` para App Actions
- [x] Crear `VoiceActionActivity` para procesar intenciones de voz

## Fase 5: Recursos y Configuración Final
- [x] Crear strings, colores y temas XML
- [x] Crear íconos placeholder (adaptive icons)
- [x] Crear archivo `proguard-rules.pro`
- [x] Verificar que la estructura esté completa y coherente

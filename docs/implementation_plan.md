# Desarrollo de App de Consumo de Gasolina para Android Auto

El objetivo es desarrollar una aplicación Android que se proyecte en la pantalla del Toyota Yaris Cross Híbrido mediante Android Auto. Permitirá registrar cargas de combustible, calcular el rendimiento y extraer estadísticas, priorizando la lectura directa de la computadora del auto.

**Valor Añadido frente al Sistema Nativo:**
Aunque la pantalla táctil de 10.1" del Yaris Cross ya cuenta con un "Historial de Eficiencia" detallado (consumo por viaje/minuto), **esta aplicación complementará esa información** enfocándose en la eficiencia *financiera* e histórica (tanque a tanque). El auto sabe cuánta gasolina quema, pero nuestra app sabrá cuánto te costó cada recarga, el precio del mercado en ese momento y el rendimiento económico real a largo plazo.

## User Review Required

El plan ha sido actualizado con tus preferencias. Por favor revisa la estrategia de extracción de datos y el flujo de captura por voz. Si estás de acuerdo, podemos dar por cerrado el diseño inicial y pasar a la ejecución (creación del proyecto Android y su estructura base).

## Proposed Changes

### Estrategia de Captura de Datos

Se implementará un enfoque de dos fases para la captura de datos al momento de recargar gasolina (estando estacionado/en modo *Parking*):

1. **Intento Principal (Lectura Nativa):** Al abrir la app en el grifo, la aplicación intentará conectarse a la computadora del Yaris Cross usando `CarHardwareManager`. Se intentará capturar automáticamente:
   - Nivel de combustible (`EnergyLevel`).
   - Kilometraje actual (`Mileage`).
   
2. **Respaldo (Ingreso por Voz o Manual en Pantalla):** Si la radio de Toyota bloquea la lectura de esos datos hacia Android Auto, la app te permitirá registrar la información sin distracciones. Aprovechando que el auto estará en Parking, usaremos **Google Assistant** (comandos de voz) o una pantalla simplificada para que ingreses los siguientes datos clave:
   - Monto total pagado.
   - Costo unitario de la gasolina.
   - Kilometraje actual (si no se pudo leer automáticamente).
   - *Métrica calculada automática:* Galones o Litros ingresados (Monto Total / Costo Unitario).
   - *Metadatos adicionales automáticos:* Fecha, hora y ubicación aproximada.

*(Nota: Queda en el radar la futura integración de un OBD2 Bluetooth si prefieres automatizarlo completamente más adelante, pero por ahora nos concentraremos en estos métodos).*

### Arquitectura General de la App

La aplicación será un proyecto estándar de Android moderno (Kotlin + Jetpack Compose) con dos experiencias:
1. **App Móvil (Celular):** Gráficos detallados, historial de recargas, costo por kilómetro y edición manual.
2. **App en el Auto (Android for Cars App Library):** Interfaz para Android Auto diseñada específicamente para un uso rápido al estar estacionado.

---

### Módulo de Android Auto (Proyección)

#### [NEW] `CarAppService`
El punto de entrada para Android Auto. Se declarará bajo la categoría "Internet of Things" (IoT) o "Navigation/POI".

#### [NEW] `DashboardScreen`
Pantalla principal en el auto (`PaneTemplate`) que mostrará:
- Rendimiento actual calculado (ej. km/galón).
- Botón grande de "Registrar Carga" (validando que el auto se encuentre detenido o en Parking).

#### [NEW] `VoiceCaptureService`
Integración con **App Actions / Google Assistant** para procesar intenciones de voz como: *"Hey Google, registrar recarga en [Nombre de la App]"*.

---

### Módulo de Telemetría (Extracción de Datos)

#### [NEW] `VehicleDataManager`
Clase encargada de usar la API `CarHardwareManager` para escuchar los datos nativos:
- `addMileageListener` (Odómetro).
- `addEnergyLevelListener` (Nivel de tanque de gasolina).

---

### Módulo de Lógica de Negocio y Base de Datos

#### [NEW] `FuelDatabase` (Room)
Almacenamiento local con los siguientes campos principales:
- `timestamp`: Fecha y hora de la recarga.
- `odometer`: Kilometraje del auto al momento de llenar.
- `total_cost`: Monto total pagado (ej. en Soles o Dólares).
- `unit_price`: Costo unitario del galón/litro.
- `gallons_added`: Cantidad de combustible (calculada).
- `location`: (Opcional) Ubicación GPS del grifo.

## Verification Plan

### 1. Pruebas de Desarrollo (Simulador DHU)
Simularemos la pantalla del auto usando el **Desktop Head Unit (DHU)** de Google, verificando que la interfaz funcione y los botones de registro operen correctamente al simular que el auto está en modo "Park".

### 2. Pruebas de Telemetría Simulada y Voz
- Inyectaremos datos falsos al simulador para verificar que la lectura nativa de `CarHardwareManager` procesa la información de gasolina y kilometraje.
- Probaremos los comandos de Google Assistant en el emulador para garantizar que entienda el flujo de ingreso de datos (monto y costo).

### 3. Verificación Manual en el Yaris Cross
Instalarás el `.apk` en tu celular y lo conectaremos a tu vehículo para la prueba final. Aquí verificaremos si la computadora del Yaris Cross autoriza a Android Auto a leer los sensores de kilometraje nativamente o si recurrimos inmediatamente al flujo por voz.

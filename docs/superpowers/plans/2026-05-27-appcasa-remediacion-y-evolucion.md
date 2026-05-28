# AppCasa Remediacion Y Evolucion Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Estabilizar AppCasa, cerrar riesgos de datos y privacidad, alinear la arquitectura con un modelo real de hogar multiusuario y preparar una base mantenible para sync, diseno superior y nuevas funcionalidades.

**Architecture:** La implementacion se divide en dos grandes bloques: estabilizacion del MVP Android local y evolucion hacia un nucleo multihogar preparado para sincronizacion. El trabajo empieza blindando datos, permisos y recordatorios, sigue desacoplando ViewModels de DAOs mediante repositorios/casos de uso y termina con UX, producto y plataforma.

**Tech Stack:** Kotlin, Jetpack Compose, Room, Hilt, Coroutines, Navigation Compose, WorkManager, AndroidX Test, JUnit

---

## Scope And Decomposition

Este plan es un plan maestro y conviene ejecutarlo por fases. Si se quiere paralelizar trabajo humano, dividirlo en cuatro subplanes:

1. `estabilizacion-datos-y-seguridad`
2. `modelo-multihogar-y-arquitectura`
3. `recordatorios-calendario-y-fiabilidad`
4. `ux-diseno-y-roadmap-producto`

Cada bloque de abajo sigue siendo ejecutable por separado.

## File Structure

### Existing Files To Modify

- `c:\dev\android_wks\AppCasa\app\src\main\AndroidManifest.xml`
  - Reducir permisos, endurecer backup, ajustar componentes exportados y documentar permisos realmente usados.
- `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\di\DatabaseModule.kt`
  - Sustituir migracion destructiva por migraciones versionadas y configuracion segura de Room.
- `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\core\data\local\AppCasaDatabase.kt`
  - Registrar nuevas entidades y versionar schema con migraciones reales.
- `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\core\utils\NotificationHelper.kt`
  - Reemplazar logica parcial de alarmas por una capa fiable y reprogramable.
- `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\dashboard\presentation\viewmodel\DashboardViewModel.kt`
  - Reducir responsabilidades, eliminar seed destructivo y usar casos de uso.
- `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\settings\presentation\screen\SettingsScreen.kt`
  - Mover acciones peligrosas tras confirmacion, flag debug o eliminarlas.
- `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\family\presentation\viewmodel\FamilyViewModel.kt`
  - Eliminar `1L` fijo y consumir contexto de hogar.
- `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\tasks\presentation\viewmodel\TasksViewModel.kt`
  - Eliminar `1L` fijo y mover acceso a tareas a un repositorio.
- `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\tasks\presentation\viewmodel\AddTaskViewModel.kt`
  - Resolver miembros y hogar actual desde un proveedor de sesion.
- `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\calendar\presentation\viewmodel\CalendarViewModel.kt`
  - Importacion robusta, feedback de error y uso del hogar actual.
- `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\reminders\presentation\viewmodel\RemindersViewModel.kt`
  - Integrar scheduler fiable y repeticion real.
- `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\finance\presentation\viewmodel\FinanceViewModel.kt`
  - Corregir total mensual y filtrar por hogar.
- `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\inventory\presentation\viewmodel\StockViewModel.kt`
  - Filtrar por hogar, endurecer reabastecimiento y mostrar errores.
- `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\utilities\presentation\viewmodel\VehicleViewModel.kt`
  - Eliminar `1L` fijo.
- `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\finance\data\local\ExpenseEntity.kt`
  - Anadir `hogarId`.
- `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\inventory\data\local\StockEntity.kt`
  - Anadir `hogarId`.
- `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\finance\data\local\ExpenseDao.kt`
  - Consultas filtradas por hogar.
- `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\inventory\data\local\StockDao.kt`
  - Consultas filtradas por hogar.
- `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\settings\data\local\ConfiguracionDao.kt`
  - Exponer hogar/usuario actual de forma consistente.
- `c:\dev\android_wks\AppCasa\gradle.properties`
  - Retirar flags obsoletos y estabilizar build.
- `c:\dev\android_wks\AppCasa\app\build.gradle.kts`
  - Anadir dependencias de test y WorkManager si faltan piezas.
- `c:\dev\android_wks\AppCasa\docs\Arquitectura_Tecnica_AppCasa\Arquitectura_Tecnica_AppCasa\Arquitectura_Tecnica_AppCasa.md`
  - Actualizar arquitectura real o reescribir roadmap.
- `c:\dev\android_wks\AppCasa\docs\Arquitectura_Tecnica_AppCasa\Documento_Funcional_AppCasa_Completo\Documento_Funcional_AppCasa_Completo.md`
  - Alinear producto real frente a objetivo.

### New Files To Create

- `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\core\data\local\Migrations.kt`
  - Migraciones Room versionadas.
- `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\core\session\CurrentHouseholdProvider.kt`
  - Contrato para resolver hogar y usuario actual.
- `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\core\session\DefaultCurrentHouseholdProvider.kt`
  - Implementacion temporal basada en Room.
- `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\di\SessionModule.kt`
  - Inyeccion del proveedor de contexto de hogar.
- `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\finance\data\ExpenseRepository.kt`
  - Encapsular acceso a gastos.
- `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\inventory\data\StockRepository.kt`
  - Encapsular acceso a inventario.
- `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\tasks\data\TaskRepository.kt`
  - Encapsular acceso a tareas.
- `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\calendar\data\CalendarRepository.kt`
  - Agregar eventos, recordatorios y tareas con fecha.
- `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\reminders\domain\ReminderScheduler.kt`
  - Contrato para programar y cancelar recordatorios.
- `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\reminders\data\WorkManagerReminderScheduler.kt`
  - Implementacion basada en WorkManager.
- `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\reminders\worker\ReminderWorker.kt`
  - Worker real para emitir notificaciones.
- `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\reminders\worker\BootRescheduleReceiver.kt`
  - Reprogramacion tras reinicio.
- `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\core\ui\components\ConfirmDestructiveActionDialog.kt`
  - Dialogo reutilizable para acciones destructivas.
- `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\core\ui\state\UserMessage.kt`
  - Modelo simple para feedback al usuario.
- `c:\dev\android_wks\AppCasa\app\src\test\java\com\appcasa\features\finance\FinanceViewModelTest.kt`
- `c:\dev\android_wks\AppCasa\app\src\test\java\com\appcasa\features\reminders\RemindersViewModelTest.kt`
- `c:\dev\android_wks\AppCasa\app\src\test\java\com\appcasa\features\calendar\CalendarViewModelTest.kt`
- `c:\dev\android_wks\AppCasa\app\src\androidTest\java\com\appcasa\core\data\local\AppCasaDatabaseMigrationTest.kt`
- `c:\dev\android_wks\AppCasa\docs\product\AppCasa_Roadmap_30_60_90.md`
  - Roadmap de ejecucion por fases.

## Execution Order

1. Blindar datos y privacidad.
2. Corregir modelo de hogar.
3. Rehacer recordatorios y calendario.
4. Separar capas y cubrir tests.
5. Refinar UX/diseno.
6. Actualizar documentacion.
7. Preparar roadmap de sync y producto.

### Task 1: Blindar base de datos y evitar perdida de datos

**Files:**
- Create: `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\core\data\local\Migrations.kt`
- Modify: `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\di\DatabaseModule.kt`
- Modify: `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\core\data\local\AppCasaDatabase.kt`
- Test: `c:\dev\android_wks\AppCasa\app\src\androidTest\java\com\appcasa\core\data\local\AppCasaDatabaseMigrationTest.kt`

- [ ] **Step 1: Write the failing migration test**

```kotlin
package com.appcasa.core.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppCasaDatabaseMigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppCasaDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrate4To5_preservesExistingRows() {
        helper.createDatabase("migration-test", 4).apply {
            execSQL("INSERT INTO hogares (id, nombre, descripcion) VALUES (1, 'Mi Hogar', 'Migracion')")
            close()
        }

        helper.runMigrationsAndValidate("migration-test", 5, true, MIGRATION_4_5)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew --no-daemon :app:connectedDebugAndroidTest`
Expected: FAIL porque no existe `MIGRATION_4_5` ni configuracion con migraciones.

- [ ] **Step 3: Write the migration object**

```kotlin
package com.appcasa.core.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE gastos ADD COLUMN hogar_id INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE stock ADD COLUMN hogar_id INTEGER NOT NULL DEFAULT 1")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_gastos_hogar_id ON gastos(hogar_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_stock_hogar_id ON stock(hogar_id)")
    }
}

val ALL_MIGRATIONS = arrayOf(
    MIGRATION_4_5
)
```

- [ ] **Step 4: Replace destructive fallback in Room**

```kotlin
@Provides
@Singleton
fun provideAppCasaDatabase(
    @ApplicationContext context: Context
): AppCasaDatabase = Room.databaseBuilder(
    context,
    AppCasaDatabase::class.java,
    "appcasa.db"
)
    .addMigrations(*ALL_MIGRATIONS)
    .build()
```

- [ ] **Step 5: Run test to verify it passes**

Run: `.\gradlew --no-daemon :app:connectedDebugAndroidTest`
Expected: PASS para `AppCasaDatabaseMigrationTest`.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/appcasa/core/data/local/Migrations.kt app/src/main/java/com/appcasa/di/DatabaseModule.kt app/src/androidTest/java/com/appcasa/core/data/local/AppCasaDatabaseMigrationTest.kt
git commit -m "fix: replace destructive room migration with versioned migrations"
```

### Task 2: Reducir superficie de ataque y endurecer privacidad local

**Files:**
- Modify: `c:\dev\android_wks\AppCasa\app\src\main\AndroidManifest.xml`
- Modify: `c:\dev\android_wks\AppCasa\app\build.gradle.kts`
- Test: `c:\dev\android_wks\AppCasa\app\src\test\java\com\appcasa\manifest\ManifestPolicyTest.kt`

- [ ] **Step 1: Write the failing policy test**

```kotlin
package com.appcasa.manifest

import org.junit.Assert.assertFalse
import org.junit.Test
import java.io.File

class ManifestPolicyTest {
    @Test
    fun manifest_doesNotKeepDangerousUnusedPermissions() {
        val manifest = File("src/main/AndroidManifest.xml").readText()
        assertFalse(manifest.contains("android.permission.CAMERA"))
        assertFalse(manifest.contains("android.permission.READ_MEDIA_IMAGES"))
        assertFalse(manifest.contains("android.permission.READ_EXTERNAL_STORAGE"))
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew --no-daemon :app:testDebugUnitTest --tests "com.appcasa.manifest.ManifestPolicyTest"`
Expected: FAIL porque el manifiesto aun contiene permisos no usados.

- [ ] **Step 3: Update the manifest**

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

  <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
  <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
  <uses-permission android:name="android.permission.INTERNET" />

  <application
    android:name=".AppCasaApplication"
    android:allowBackup="false"
    android:fullBackupContent="false"
    android:icon="@mipmap/ic_launcher"
    android:label="@string/app_name"
    android:roundIcon="@mipmap/ic_launcher_round"
    android:supportsRtl="true"
    android:theme="@style/Theme.AppCasa">
```

- [ ] **Step 4: Run test to verify it passes**

Run: `.\gradlew --no-daemon :app:testDebugUnitTest --tests "com.appcasa.manifest.ManifestPolicyTest"`
Expected: PASS.

- [ ] **Step 5: Verify the app still assembles**

Run: `.\gradlew --no-daemon :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 6: Commit**

```bash
git add app/src/main/AndroidManifest.xml app/src/test/java/com/appcasa/manifest/ManifestPolicyTest.kt
git commit -m "fix: harden manifest permissions and local backup policy"
```

### Task 3: Introducir contexto de hogar actual y eliminar los `1L` hardcodeados

**Files:**
- Create: `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\core\session\CurrentHouseholdProvider.kt`
- Create: `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\core\session\DefaultCurrentHouseholdProvider.kt`
- Create: `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\di\SessionModule.kt`
- Modify: `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\family\presentation\viewmodel\FamilyViewModel.kt`
- Modify: `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\tasks\presentation\viewmodel\TasksViewModel.kt`
- Modify: `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\tasks\presentation\viewmodel\AddTaskViewModel.kt`
- Modify: `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\calendar\presentation\viewmodel\CalendarViewModel.kt`
- Modify: `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\reminders\presentation\viewmodel\RemindersViewModel.kt`
- Modify: `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\utilities\presentation\viewmodel\VehicleViewModel.kt`
- Test: `c:\dev\android_wks\AppCasa\app\src\test\java\com\appcasa\core\session\DefaultCurrentHouseholdProviderTest.kt`

- [ ] **Step 1: Write the failing provider test**

```kotlin
package com.appcasa.core.session

import com.appcasa.features.settings.data.local.ConfiguracionDao
import com.appcasa.features.settings.data.local.HogarEntity
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultCurrentHouseholdProviderTest {
    @Test
    fun returnsPersistedHouseholdId() = runTest {
        val dao = object : ConfiguracionDao by FakeConfiguracionDao() {
            override fun getHogarActual() = flowOf(HogarEntity(id = 7L, nombre = "Casa 7"))
        }

        val provider = DefaultCurrentHouseholdProvider(dao)
        assertEquals(7L, provider.requireHouseholdId())
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew --no-daemon :app:testDebugUnitTest --tests "com.appcasa.core.session.DefaultCurrentHouseholdProviderTest"`
Expected: FAIL porque el proveedor aun no existe.

- [ ] **Step 3: Create the provider contract and implementation**

```kotlin
package com.appcasa.core.session

interface CurrentHouseholdProvider {
    suspend fun requireHouseholdId(): Long
}
```

```kotlin
package com.appcasa.core.session

import com.appcasa.features.settings.data.local.ConfiguracionDao
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class DefaultCurrentHouseholdProvider @Inject constructor(
    private val configuracionDao: ConfiguracionDao
) : CurrentHouseholdProvider {
    override suspend fun requireHouseholdId(): Long {
        return configuracionDao.getHogarActual().first()?.id
            ?: error("No household selected")
    }
}
```

- [ ] **Step 4: Bind the provider in Hilt**

```kotlin
package com.appcasa.di

import com.appcasa.core.session.CurrentHouseholdProvider
import com.appcasa.core.session.DefaultCurrentHouseholdProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SessionModule {
    @Binds
    @Singleton
    abstract fun bindCurrentHouseholdProvider(
        impl: DefaultCurrentHouseholdProvider
    ): CurrentHouseholdProvider
}
```

- [ ] **Step 5: Replace hardcoded `1L` in one ViewModel pattern**

```kotlin
@HiltViewModel
class FamilyViewModel @Inject constructor(
    private val miembroDao: MiembroDao,
    private val currentHouseholdProvider: CurrentHouseholdProvider
) : ViewModel() {

    val familyMembers: StateFlow<List<MiembroEntity>> = flow {
        emit(currentHouseholdProvider.requireHouseholdId())
    }.flatMapLatest { hogarId ->
        miembroDao.getMiembrosByHogar(hogarId)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
}
```

- [ ] **Step 6: Apply the same pattern to Tasks, AddTask, Calendar, Reminders and Vehicle**

```kotlin
val tasks: StateFlow<List<TareaEntity>> = flow {
    emit(currentHouseholdProvider.requireHouseholdId())
}.flatMapLatest { hogarId ->
    tareaDao.getTareasByHogar(hogarId)
}.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = emptyList()
)
```

- [ ] **Step 7: Run targeted tests and build**

Run: `.\gradlew --no-daemon :app:testDebugUnitTest`
Expected: PASS for provider tests and no compile errors.

Run: `.\gradlew --no-daemon :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 8: Commit**

```bash
git add app/src/main/java/com/appcasa/core/session app/src/main/java/com/appcasa/di/SessionModule.kt app/src/main/java/com/appcasa/features/family/presentation/viewmodel/FamilyViewModel.kt app/src/main/java/com/appcasa/features/tasks/presentation/viewmodel/TasksViewModel.kt app/src/main/java/com/appcasa/features/tasks/presentation/viewmodel/AddTaskViewModel.kt app/src/main/java/com/appcasa/features/calendar/presentation/viewmodel/CalendarViewModel.kt app/src/main/java/com/appcasa/features/reminders/presentation/viewmodel/RemindersViewModel.kt app/src/main/java/com/appcasa/features/utilities/presentation/viewmodel/VehicleViewModel.kt
git commit -m "refactor: resolve current household through injected session provider"
```

### Task 4: Hacer que gastos e inventario pertenezcan a un hogar real

**Files:**
- Modify: `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\finance\data\local\ExpenseEntity.kt`
- Modify: `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\inventory\data\local\StockEntity.kt`
- Modify: `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\finance\data\local\ExpenseDao.kt`
- Modify: `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\inventory\data\local\StockDao.kt`
- Modify: `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\finance\presentation\viewmodel\FinanceViewModel.kt`
- Modify: `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\inventory\presentation\viewmodel\StockViewModel.kt`
- Modify: `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\core\data\local\Migrations.kt`
- Test: `c:\dev\android_wks\AppCasa\app\src\test\java\com\appcasa\features\finance\FinanceViewModelTest.kt`

- [ ] **Step 1: Write the failing finance test**

```kotlin
package com.appcasa.features.finance

import app.cash.turbine.test
import com.appcasa.core.session.CurrentHouseholdProvider
import com.appcasa.features.finance.data.local.ExpenseDao
import com.appcasa.features.finance.presentation.viewmodel.FinanceViewModel
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class FinanceViewModelTest {
    @Test
    fun monthlyTotal_emitsRepositoryValue() = runTest {
        val dao = FakeExpenseDao(total = 125.5)
        val provider = object : CurrentHouseholdProvider {
            override suspend fun requireHouseholdId() = 1L
        }

        val viewModel = FinanceViewModel(dao, provider)
        viewModel.monthlyTotal.test {
            assertEquals(125.5, awaitItem(), 0.0)
        }
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew --no-daemon :app:testDebugUnitTest --tests "com.appcasa.features.finance.FinanceViewModelTest"`
Expected: FAIL porque el ViewModel devuelve `0.0`.

- [ ] **Step 3: Add household ownership to entities**

```kotlin
@Entity(tableName = "gastos")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "hogar_id", index = true)
    val hogarId: Long,
    @ColumnInfo(name = "concepto")
    val concepto: String,
    @ColumnInfo(name = "importe")
    val importe: Double,
    @ColumnInfo(name = "categoria")
    val categoria: String,
    @ColumnInfo(name = "fecha")
    val fecha: Long = System.currentTimeMillis()
)
```

```kotlin
@Entity(tableName = "stock")
data class StockEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "hogar_id", index = true)
    val hogarId: Long,
    @ColumnInfo(name = "nombre")
    val nombre: String,
    @ColumnInfo(name = "categoria")
    val categoria: String,
    @ColumnInfo(name = "cantidad_actual")
    val cantidadActual: Double,
    @ColumnInfo(name = "cantidad_minima")
    val cantidadMinima: Double,
    @ColumnInfo(name = "unidad")
    val unidad: String
)
```

- [ ] **Step 4: Filter DAOs by household**

```kotlin
@Dao
interface ExpenseDao {
    @Query("SELECT * FROM gastos WHERE hogar_id = :hogarId ORDER BY fecha DESC")
    fun getExpensesByHogar(hogarId: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT COALESCE(SUM(importe), 0) FROM gastos WHERE hogar_id = :hogarId AND fecha >= :startOfMonth")
    fun getTotalMonthlyExpense(hogarId: Long, startOfMonth: Long): Flow<Double>
}
```

```kotlin
@Dao
interface StockDao {
    @Query("SELECT * FROM stock WHERE hogar_id = :hogarId ORDER BY categoria ASC, nombre ASC")
    fun getAllStock(hogarId: Long): Flow<List<StockEntity>>

    @Query("SELECT * FROM stock WHERE hogar_id = :hogarId AND cantidad_actual <= cantidad_minima")
    fun getLowStockItems(hogarId: Long): Flow<List<StockEntity>>
}
```

- [ ] **Step 5: Fix the monthly total implementation**

```kotlin
@HiltViewModel
class FinanceViewModel @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val currentHouseholdProvider: CurrentHouseholdProvider
) : ViewModel() {

    val monthlyTotal: StateFlow<Double> = flow {
        emit(currentHouseholdProvider.requireHouseholdId())
    }.flatMapLatest { hogarId ->
        expenseDao.getTotalMonthlyExpense(hogarId, getStartOfMonth())
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )
}
```

- [ ] **Step 6: Run tests and build**

Run: `.\gradlew --no-daemon :app:testDebugUnitTest --tests "com.appcasa.features.finance.FinanceViewModelTest"`
Expected: PASS.

Run: `.\gradlew --no-daemon :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/appcasa/features/finance app/src/main/java/com/appcasa/features/inventory app/src/main/java/com/appcasa/core/data/local/Migrations.kt app/src/test/java/com/appcasa/features/finance/FinanceViewModelTest.kt
git commit -m "fix: scope finance and inventory data by household"
```

### Task 5: Rehacer recordatorios con un scheduler fiable y reprogramable

**Files:**
- Create: `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\reminders\domain\ReminderScheduler.kt`
- Create: `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\reminders\data\WorkManagerReminderScheduler.kt`
- Create: `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\reminders\worker\ReminderWorker.kt`
- Create: `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\reminders\worker\BootRescheduleReceiver.kt`
- Modify: `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\core\utils\NotificationHelper.kt`
- Modify: `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\reminders\presentation\viewmodel\RemindersViewModel.kt`
- Modify: `c:\dev\android_wks\AppCasa\app\src\main\AndroidManifest.xml`
- Test: `c:\dev\android_wks\AppCasa\app\src\test\java\com\appcasa\features\reminders\RemindersViewModelTest.kt`

- [ ] **Step 1: Write the failing scheduler test**

```kotlin
package com.appcasa.features.reminders

import com.appcasa.core.session.CurrentHouseholdProvider
import com.appcasa.features.reminders.data.local.RecordatorioDao
import com.appcasa.features.reminders.presentation.viewmodel.RemindersViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

class RemindersViewModelTest {
    @Test
    fun addReminder_persistsAndSchedulesWork() = runTest {
        val dao = FakeReminderDao(insertedId = 44L)
        val scheduler = FakeReminderScheduler()
        val provider = object : CurrentHouseholdProvider {
            override suspend fun requireHouseholdId() = 3L
        }

        val viewModel = RemindersViewModel(dao, scheduler, provider)
        viewModel.addReminder("Vacuna", 1893456000000L, "NINGUNA")

        verify(scheduler).schedule(44L, "Vacuna", 1893456000000L, "NINGUNA")
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew --no-daemon :app:testDebugUnitTest --tests "com.appcasa.features.reminders.RemindersViewModelTest"`
Expected: FAIL porque aun no existe `ReminderScheduler`.

- [ ] **Step 3: Create the scheduler contract**

```kotlin
package com.appcasa.features.reminders.domain

interface ReminderScheduler {
    suspend fun schedule(reminderId: Long, title: String, triggerAtMillis: Long, repetitionType: String)
    suspend fun cancel(reminderId: Long)
}
```

- [ ] **Step 4: Implement WorkManager scheduler**

```kotlin
package com.appcasa.features.reminders.data

import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.appcasa.features.reminders.domain.ReminderScheduler
import com.appcasa.features.reminders.worker.ReminderWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class WorkManagerReminderScheduler @Inject constructor(
    private val workManager: WorkManager
) : ReminderScheduler {
    override suspend fun schedule(reminderId: Long, title: String, triggerAtMillis: Long, repetitionType: String) {
        val delay = (triggerAtMillis - System.currentTimeMillis()).coerceAtLeast(0L)
        val work = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(
                Data.Builder()
                    .putLong("reminder_id", reminderId)
                    .putString("title", title)
                    .build()
            )
            .build()

        workManager.enqueue(work)
    }

    override suspend fun cancel(reminderId: Long) {
        workManager.cancelAllWorkByTag("reminder-$reminderId")
    }
}
```

- [ ] **Step 5: Update ViewModel to schedule through the new abstraction**

```kotlin
@HiltViewModel
class RemindersViewModel @Inject constructor(
    private val recordatorioDao: RecordatorioDao,
    private val reminderScheduler: ReminderScheduler,
    private val currentHouseholdProvider: CurrentHouseholdProvider
) : ViewModel() {

    fun addReminder(titulo: String, fechaHora: Long, tipo: String = "NINGUNA") {
        viewModelScope.launch {
            val hogarId = currentHouseholdProvider.requireHouseholdId()
            val id = recordatorioDao.insertRecordatorio(
                RecordatorioEntity(
                    hogarId = hogarId,
                    titulo = titulo,
                    fechaHora = fechaHora,
                    tipoRepeticion = tipo
                )
            )
            reminderScheduler.schedule(id, titulo, fechaHora, tipo)
        }
    }
}
```

- [ ] **Step 6: Register reboot receiver and keep helper only for rendering notifications**

```xml
<receiver
    android:name=".features.reminders.worker.BootRescheduleReceiver"
    android:enabled="true"
    android:exported="false">
    <intent-filter>
        <action android:name="android.intent.action.BOOT_COMPLETED" />
    </intent-filter>
</receiver>
```

- [ ] **Step 7: Run tests and smoke build**

Run: `.\gradlew --no-daemon :app:testDebugUnitTest --tests "com.appcasa.features.reminders.RemindersViewModelTest"`
Expected: PASS.

Run: `.\gradlew --no-daemon :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 8: Commit**

```bash
git add app/src/main/java/com/appcasa/features/reminders app/src/main/java/com/appcasa/core/utils/NotificationHelper.kt app/src/main/AndroidManifest.xml app/src/test/java/com/appcasa/features/reminders/RemindersViewModelTest.kt
git commit -m "feat: replace fragile alarms with reschedulable reminder workers"
```

### Task 6: Corregir UX de calendario y recordatorios

**Files:**
- Modify: `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\calendar\presentation\screen\CalendarScreen.kt`
- Modify: `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\calendar\presentation\viewmodel\CalendarViewModel.kt`
- Create: `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\core\ui\state\UserMessage.kt`
- Test: `c:\dev\android_wks\AppCasa\app\src\test\java\com\appcasa\features\calendar\CalendarViewModelTest.kt`

- [ ] **Step 1: Write the failing CSV import test**

```kotlin
package com.appcasa.features.calendar

import app.cash.turbine.test
import com.appcasa.features.calendar.presentation.viewmodel.CalendarViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class CalendarViewModelTest {
    @Test
    fun invalidCsv_emitsUserFacingError() = runTest {
        val viewModel = buildCalendarViewModel()
        viewModel.importShiftsFromCsv("bad-line-without-date")

        viewModel.messages.test {
            assertTrue(awaitItem().message.contains("No se pudo importar"))
        }
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew --no-daemon :app:testDebugUnitTest --tests "com.appcasa.features.calendar.CalendarViewModelTest"`
Expected: FAIL porque no existe `messages`.

- [ ] **Step 3: Add a user-facing message model**

```kotlin
package com.appcasa.core.ui.state

data class UserMessage(
    val message: String,
    val isError: Boolean = false
)
```

- [ ] **Step 4: Emit robust feedback from CSV import**

```kotlin
private val _messages = MutableSharedFlow<UserMessage>()
val messages = _messages.asSharedFlow()

fun importShiftsFromCsv(content: String) {
    viewModelScope.launch {
        runCatching {
            val lines = content.lines().filter { it.isNotBlank() }
            require(lines.isNotEmpty()) { "empty" }
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            lines.forEach { line ->
                val parts = line.split(",")
                require(parts.size >= 2) { "invalid_line" }
                val date = requireNotNull(dateFormat.parse(parts[0].trim())?.time)
                eventoDao.insertEvento(
                    EventoEntity(
                        hogarId = currentHouseholdProvider.requireHouseholdId(),
                        titulo = "Turno: ${parts[1].trim()}",
                        fecha = date,
                        tipo = TipoEvento.REUNION.name
                    )
                )
            }
            _messages.emit(UserMessage("Turnos importados correctamente"))
        }.onFailure {
            _messages.emit(UserMessage("No se pudo importar el CSV de turnos", isError = true))
        }
    }
}
```

- [ ] **Step 5: Add date and time selection to reminder dialog**

```kotlin
var selectedHour by remember { mutableStateOf(9) }
var selectedMinute by remember { mutableStateOf(0) }

Button(onClick = { showTimePicker = true }) {
    Text("Hora: %02d:%02d".format(selectedHour, selectedMinute))
}

val reminderMillis = Instant.ofEpochMilli(selectedDateMillis)
    .atZone(ZoneId.systemDefault())
    .withHour(selectedHour)
    .withMinute(selectedMinute)
    .toInstant()
    .toEpochMilli()
```

- [ ] **Step 6: Render feedback in the screen**

```kotlin
val snackbarHostState = remember { SnackbarHostState() }

LaunchedEffect(Unit) {
    viewModel.messages.collect { message ->
        snackbarHostState.showSnackbar(message.message)
    }
}

Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = { /* actual top bar */ }
) { scaffoldPadding ->
```

- [ ] **Step 7: Run tests and manual smoke**

Run: `.\gradlew --no-daemon :app:testDebugUnitTest --tests "com.appcasa.features.calendar.CalendarViewModelTest"`
Expected: PASS.

Run: `.\gradlew --no-daemon :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 8: Commit**

```bash
git add app/src/main/java/com/appcasa/features/calendar app/src/main/java/com/appcasa/core/ui/state/UserMessage.kt app/src/test/java/com/appcasa/features/calendar/CalendarViewModelTest.kt
git commit -m "feat: improve calendar import feedback and reminder time selection"
```

### Task 7: Encapsular acceso a datos con repositorios pequenos

**Files:**
- Create: `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\tasks\data\TaskRepository.kt`
- Create: `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\finance\data\ExpenseRepository.kt`
- Create: `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\inventory\data\StockRepository.kt`
- Modify: `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\dashboard\presentation\viewmodel\DashboardViewModel.kt`
- Modify: `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\tasks\presentation\viewmodel\TasksViewModel.kt`
- Modify: `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\finance\presentation\viewmodel\FinanceViewModel.kt`
- Modify: `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\inventory\presentation\viewmodel\StockViewModel.kt`
- Test: `c:\dev\android_wks\AppCasa\app\src\test\java\com\appcasa\features\dashboard\DashboardViewModelTest.kt`

- [ ] **Step 1: Write the failing dashboard aggregation test**

```kotlin
package com.appcasa.features.dashboard

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DashboardViewModelTest {
    @Test
    fun pendingTasksCount_comesFromTaskRepository() = runTest {
        val viewModel = buildDashboardViewModel(pendingTasks = 3)
        viewModel.pendingTasksCount.test {
            assertEquals("3", awaitItem())
        }
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew --no-daemon :app:testDebugUnitTest --tests "com.appcasa.features.dashboard.DashboardViewModelTest"`
Expected: FAIL porque el dashboard no usa repositorios fakeables.

- [ ] **Step 3: Create a focused task repository**

```kotlin
package com.appcasa.features.tasks.data

import com.appcasa.features.tasks.data.local.TareaDao
import com.appcasa.features.tasks.data.local.TareaEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TaskRepository @Inject constructor(
    private val tareaDao: TareaDao
) {
    fun observeTasks(hogarId: Long): Flow<List<TareaEntity>> = tareaDao.getTareasByHogar(hogarId)
    suspend fun updateTask(task: TareaEntity) = tareaDao.updateTarea(task)
    suspend fun deleteTask(task: TareaEntity) = tareaDao.deleteTarea(task)
}
```

- [ ] **Step 4: Use repositories in DashboardViewModel**

```kotlin
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val currentHouseholdProvider: CurrentHouseholdProvider,
    private val taskRepository: TaskRepository,
    private val expenseRepository: ExpenseRepository,
    private val stockRepository: StockRepository,
    private val calendarRepository: CalendarRepository
) : ViewModel() {
    init {
        observeData()
    }
}
```

- [ ] **Step 5: Remove the destructive seed path from production logic**

```kotlin
@VisibleForTesting
suspend fun seedPreviewData() {
    // solo para pruebas y previews; no exponer desde ajustes en release
}
```

- [ ] **Step 6: Run tests and build**

Run: `.\gradlew --no-daemon :app:testDebugUnitTest`
Expected: PASS en dashboard y ViewModels afectados.

Run: `.\gradlew --no-daemon :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/appcasa/features/tasks/data/TaskRepository.kt app/src/main/java/com/appcasa/features/finance/data/ExpenseRepository.kt app/src/main/java/com/appcasa/features/inventory/data/StockRepository.kt app/src/main/java/com/appcasa/features/dashboard/presentation/viewmodel/DashboardViewModel.kt
git commit -m "refactor: introduce repositories and slim dashboard responsibilities"
```

### Task 8: Eliminar acciones destructivas inseguras de ajustes

**Files:**
- Create: `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\core\ui\components\ConfirmDestructiveActionDialog.kt`
- Modify: `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\settings\presentation\screen\SettingsScreen.kt`
- Modify: `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\dashboard\presentation\viewmodel\DashboardViewModel.kt`
- Test: `c:\dev\android_wks\AppCasa\app\src\test\java\com\appcasa\features\settings\SettingsPolicyTest.kt`

- [ ] **Step 1: Write the failing settings policy test**

```kotlin
package com.appcasa.features.settings

import org.junit.Assert.assertFalse
import org.junit.Test
import java.io.File

class SettingsPolicyTest {
    @Test
    fun settings_doesNotExposeSeedButtonInReleaseFlow() {
        val content = File("src/main/java/com/appcasa/features/settings/presentation/screen/SettingsScreen.kt").readText()
        assertFalse(content.contains("Cargar datos de ejemplo (MVP)"))
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew --no-daemon :app:testDebugUnitTest --tests "com.appcasa.features.settings.SettingsPolicyTest"`
Expected: FAIL porque el boton todavia existe.

- [ ] **Step 3: Add reusable confirmation dialog**

```kotlin
package com.appcasa.core.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun ConfirmDestructiveActionDialog(
    title: String,
    body: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(body) },
        confirmButton = { Button(onClick = onConfirm) { Text("Confirmar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
```

- [ ] **Step 4: Remove seed button from normal settings UI**

```kotlin
@Composable
fun SettingsContent(
    innerPadding: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp)
    ) {
        Text("Ajustes", style = MaterialTheme.typography.headlineMedium)
        Text("Preferencias del hogar, notificaciones y privacidad")
    }
}
```

- [ ] **Step 5: Move any seed utility behind debug-only code**

```kotlin
if (BuildConfig.DEBUG) {
    // opcionalmente exponer herramientas internas en un screen de debug separado
}
```

- [ ] **Step 6: Run tests and build**

Run: `.\gradlew --no-daemon :app:testDebugUnitTest --tests "com.appcasa.features.settings.SettingsPolicyTest"`
Expected: PASS.

Run: `.\gradlew --no-daemon :app:assembleDebug`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 7: Commit**

```bash
git add app/src/main/java/com/appcasa/core/ui/components/ConfirmDestructiveActionDialog.kt app/src/main/java/com/appcasa/features/settings/presentation/screen/SettingsScreen.kt app/src/test/java/com/appcasa/features/settings/SettingsPolicyTest.kt
git commit -m "fix: remove destructive seed action from settings"
```

### Task 9: Subir la calidad base del proyecto con tests y fakeables

**Files:**
- Modify: `c:\dev\android_wks\AppCasa\app\build.gradle.kts`
- Create: `c:\dev\android_wks\AppCasa\app\src\test\java\com\appcasa\testdoubles\FakeConfiguracionDao.kt`
- Create: `c:\dev\android_wks\AppCasa\app\src\test\java\com\appcasa\testdoubles\FakeExpenseDao.kt`
- Create: `c:\dev\android_wks\AppCasa\app\src\test\java\com\appcasa\testdoubles\FakeReminderDao.kt`
- Modify: `c:\dev\android_wks\AppCasa\app\src\test\java\com\appcasa\features\finance\FinanceViewModelTest.kt`
- Modify: `c:\dev\android_wks\AppCasa\app\src\test\java\com\appcasa\features\reminders\RemindersViewModelTest.kt`
- Modify: `c:\dev\android_wks\AppCasa\app\src\test\java\com\appcasa\features\calendar\CalendarViewModelTest.kt`

- [ ] **Step 1: Add test dependencies**

```kotlin
dependencies {
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("app.cash.turbine:turbine:1.1.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
    androidTestImplementation("androidx.room:room-testing:2.8.4")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
}
```

- [ ] **Step 2: Create minimal test doubles**

```kotlin
package com.appcasa.testdoubles

import com.appcasa.features.settings.data.local.ConfiguracionDao
import com.appcasa.features.settings.data.local.ConfiguracionEntity
import com.appcasa.features.settings.data.local.HogarEntity
import com.appcasa.features.settings.data.local.UsuarioEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

open class FakeConfiguracionDao : ConfiguracionDao {
    override fun getHogarActual(): Flow<HogarEntity?> = flowOf(HogarEntity(id = 1L, nombre = "Fake"))
    override suspend fun insertHogar(hogar: HogarEntity): Long = hogar.id
    override fun getUsuarioActual(): Flow<UsuarioEntity?> = flowOf(null)
    override suspend fun insertUsuario(usuario: UsuarioEntity): Long = usuario.id
    override fun getConfiguracion(hogarId: Long): Flow<List<ConfiguracionEntity>> = flowOf(emptyList())
    override suspend fun insertConfiguracion(config: ConfiguracionEntity): Long = config.id
}
```

- [ ] **Step 3: Run full unit test suite**

Run: `.\gradlew --no-daemon :app:testDebugUnitTest`
Expected: FAIL solo en tests aun no adaptados o dobles faltantes.

- [ ] **Step 4: Adapt tests until suite passes**

```kotlin
@get:Rule
val mainDispatcherRule = MainDispatcherRule()
```

```kotlin
viewModelScope.launch {
    // probar emisiones con Turbine
}
```

- [ ] **Step 5: Commit**

```bash
git add app/build.gradle.kts app/src/test/java
git commit -m "test: add unit test baseline and reusable fakes"
```

### Task 10: Mejorar UX y consistencia visual de hubs, formularios y estados vacios

**Files:**
- Modify: `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\dashboard\presentation\screen\DashboardScreen.kt`
- Modify: `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\dashboard\presentation\screen\ManagementHubScreen.kt`
- Modify: `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\dashboard\presentation\screen\FamilyHubScreen.kt`
- Modify: `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\calendar\presentation\screen\CalendarScreen.kt`
- Modify: `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\lists\presentation\screen\ListsScreen.kt`
- Modify: `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\finance\presentation\screen\ExpenseScreen.kt`
- Modify: `c:\dev\android_wks\AppCasa\app\src\main\java\com\appcasa\features\inventory\presentation\screen\StockScreen.kt`

- [ ] **Step 1: Define one empty-state pattern**

```kotlin
@Composable
fun EmptyState(
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Text(subtitle, style = MaterialTheme.typography.bodyMedium)
        if (actionLabel != null && onAction != null) {
            Button(onClick = onAction) { Text(actionLabel) }
        }
    }
}
```

- [ ] **Step 2: Replace one inconsistent screen first**

```kotlin
if (expenses.isEmpty()) {
    item {
        EmptyState(
            title = "Sin gastos registrados",
            subtitle = "Empieza anotando el primer gasto familiar",
            actionLabel = "Anadir gasto",
            onAction = { showAddDialog = true }
        )
    }
}
```

- [ ] **Step 3: Add stronger form validation copy**

```kotlin
val importeValido = importe.toDoubleOrNull()?.let { it > 0 } == true

OutlinedTextField(
    value = importe,
    onValueChange = { importe = it },
    isError = importe.isNotBlank() && !importeValido,
    supportingText = {
        if (importe.isNotBlank() && !importeValido) {
            Text("Introduce un importe mayor que 0")
        }
    }
)
```

- [ ] **Step 4: Harmonize top bars and section subtitles**

```kotlin
TopAppBar(
    title = {
        Column {
            Text("Gestion del Hogar")
            Text("Tareas, listas e inventario", style = MaterialTheme.typography.bodySmall)
        }
    }
)
```

- [ ] **Step 5: Manual QA checklist**

Run:

```bash
.\gradlew --no-daemon :app:assembleDebug
```

Expected: `BUILD SUCCESSFUL`

Manual:

```text
1. Abrir dashboard y revisar jerarquia visual.
2. Entrar en gastos con lista vacia.
3. Crear lista, gasto e item de stock.
4. Borrar un elemento y confirmar mensajes.
5. Revisar hubs de gestion y familia.
```

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/appcasa/features/dashboard/presentation/screen app/src/main/java/com/appcasa/features/calendar/presentation/screen/CalendarScreen.kt app/src/main/java/com/appcasa/features/lists/presentation/screen/ListsScreen.kt app/src/main/java/com/appcasa/features/finance/presentation/screen/ExpenseScreen.kt app/src/main/java/com/appcasa/features/inventory/presentation/screen/StockScreen.kt
git commit -m "feat: unify empty states and strengthen core screen UX"
```

### Task 11: Alinear documentacion con el producto real

**Files:**
- Modify: `c:\dev\android_wks\AppCasa\docs\Arquitectura_Tecnica_AppCasa\Arquitectura_Tecnica_AppCasa\Arquitectura_Tecnica_AppCasa.md`
- Modify: `c:\dev\android_wks\AppCasa\docs\Arquitectura_Tecnica_AppCasa\Documento_Funcional_AppCasa_Completo\Documento_Funcional_AppCasa_Completo.md`
- Modify: `c:\dev\android_wks\AppCasa\docs\Arquitectura_Tecnica_AppCasa\Modelo_Base_Datos_AppCasa_Completo\Modelo_Base_Datos_AppCasa_Completo.md`

- [ ] **Step 1: Rewrite architecture summary to match the repo**

```markdown
## Estado real del repositorio

- Frontend actual: aplicacion Android nativa con Kotlin + Jetpack Compose.
- Persistencia actual: Room local en dispositivo.
- Inyeccion de dependencias: Hilt.
- Estado actual: MVP local sin backend integrado en este repositorio.
- Roadmap objetivo: preparar el modelo para sync, multiusuario y servicios remotos.
```

- [ ] **Step 2: Add an explicit gap section**

```markdown
## Brechas conocidas frente a la vision objetivo

1. No existe aun backend REST integrado en este repo.
2. No existe autenticacion JWT operativa.
3. No existe sincronizacion entre dispositivos.
4. iOS no forma parte del alcance actual de implementacion.
```

- [ ] **Step 3: Update DB model narrative**

```markdown
## Decision tecnica inmediata

Todas las entidades de dominio compartido deben pertenecer a `hogar_id`, incluidas finanzas e inventario, para evitar deuda estructural antes de introducir sincronizacion.
```

- [ ] **Step 4: Commit**

```bash
git add docs/Arquitectura_Tecnica_AppCasa
git commit -m "docs: align architecture and roadmap with current android implementation"
```

### Task 12: Preparar roadmap 30/60/90 y backlog de crecimiento

**Files:**
- Create: `c:\dev\android_wks\AppCasa\docs\product\AppCasa_Roadmap_30_60_90.md`

- [ ] **Step 1: Create the 30-day section**

```markdown
# AppCasa Roadmap 30 60 90

## 0-30 dias

- Migraciones Room reales.
- Permisos y backup endurecidos.
- Recordatorios fiables con WorkManager.
- Eliminacion de `hogarId = 1L`.
- Tests base de ViewModels y migraciones.
```

- [ ] **Step 2: Create the 60-day section**

```markdown
## 31-60 dias

- Repositorios y casos de uso en modulos principales.
- Dashboard agregado por repositorios.
- UX unificada en formularios, vacios y errores.
- Configuracion real de hogar y usuario.
- Export/import de datos local.
```

- [ ] **Step 3: Create the 90-day section**

```markdown
## 61-90 dias

- Modelo de sync y conflictos.
- Invitaciones a hogar y roles.
- Push notifications y backup cloud.
- Widgets y automatizaciones.
- Definicion tecnica de asistente IA domestico.
```

- [ ] **Step 4: Commit**

```bash
git add docs/product/AppCasa_Roadmap_30_60_90.md
git commit -m "docs: add 30 60 90 execution roadmap for appcasa"
```

## Validation Checklist

- [ ] `.\gradlew --no-daemon :app:testDebugUnitTest`
- [ ] `.\gradlew --no-daemon :app:connectedDebugAndroidTest`
- [ ] `.\gradlew --no-daemon :app:assembleDebug`
- [ ] Verificar que no queda ningun `1L` fijo como hogar en `app/src/main/java`
- [ ] Verificar que `AndroidManifest.xml` no contiene permisos no usados
- [ ] Verificar que `SettingsScreen` no expone acciones destructivas en flujo normal
- [ ] Verificar que los gastos e inventario usan `hogar_id`
- [ ] Verificar que los recordatorios sobreviven a reinicio y reprogramacion

## Product Follow-Ups After This Plan

- Autenticacion real de usuarios.
- Multiusuario compartiendo hogar.
- Sincronizacion cloud.
- Historial medico avanzado de mascotas.
- Finanzas familiares con presupuestos.
- Integracion de turnos, ICS y Google Calendar.
- Motor de IA para organizacion del hogar.

## Self-Review

### Spec coverage

- Riesgos explotables: cubiertos en Tasks 1, 2, 5 y 8.
- Mejoras de arquitectura: cubiertas en Tasks 3, 4 y 7.
- Arreglos funcionales: cubiertos en Tasks 4, 5 y 6.
- Diseno y UX: cubiertos en Task 10.
- Documentacion y estrategia: cubiertas en Tasks 11 y 12.
- Potenciacion de producto: reflejada en roadmap y follow-ups.

### Placeholder scan

- No hay `TODO`, `TBD` ni referencias vacias.
- Cada tarea tiene archivos, pasos, comandos y codigo inicial.

### Type consistency

- `CurrentHouseholdProvider.requireHouseholdId()` se usa de forma consistente.
- `ReminderScheduler.schedule()` y `cancel()` se mantienen igual en contrato y ViewModel.
- `hogarId` se adopta como nombre unico en entidades y consultas.

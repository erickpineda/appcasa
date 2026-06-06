# Implementation Plan - Architecture Improvement & Modularization

This plan addresses the business logic leakage in ViewModels and the "God module" issue of `feature:dashboard`, as identified in the technical report.

## Proposed Changes

### 1. Refactor ViewModels (Logic Extraction)

#### [FinanceViewModel.kt](file:///C:/dev/android_wks/AppCasa/feature/finance/src/main/java/com/appcasa/features/presentation/viewmodel/FinanceViewModel.kt)
- Move ML Kit text processing and Regex logic from `processTicket` to a new `ProcessTicketUseCase`.
- ViewModel will only handle the UI state of the OCR result.

#### [DashboardViewModel.kt](file:///C:/dev/android_wks/AppCasa/feature/dashboard/src/main/java/com/appcasa/features/presentation/viewmodel/DashboardViewModel.kt)
- Move `updateMemberMood` logic (setting the emoji and the `updatedAt` timestamp) to a new `UpdateMemberMoodUseCase`.
- Extract `petData` summary logic to a dedicated helper or UseCase.

#### [StockViewModel.kt](file:///C:/dev/android_wks/AppCasa/feature/inventory/src/main/java/com/appcasa/features/presentation/viewmodel/StockViewModel.kt)
- Move `updateQuantity` logic (clamping and triggering auto-restock) to a new `UpdateStockQuantityUseCase`.

---

### 2. Modularization of `feature:dashboard`

We will split the current `feature:dashboard` module to increase cohesion.

#### [NEW] `:feature:family` module
- Move everything in `com.appcasa.features.family` to this new module.
- Move `FamilyHubScreen.kt` and `FamilyScreen.kt` to this module.
- Define separate navigation for family features.

#### [NEW] `:feature:lists` module
- Move everything in `com.appcasa.features.lists` to this new module.
- Move `ListsScreen.kt` and `ListDetailScreen.kt` to this module.

#### [Dashboard module](file:///C:/dev/android_wks/AppCasa/feature/dashboard/build.gradle)
- Update dependencies to point to the new modules where necessary.

---

### 3. Documentation Update

#### [README.md](file:///C:/dev/android_wks/AppCasa/README.md)
- Replace outdated Ionic/Angular/Spring info with current Kotlin/Compose/Hilt/Room stack.
- Document the new modular structure.

## Verification Plan

### Automated Tests
- Run existing unit tests (if any) using: `./gradlew test`
- Add new unit tests for the newly created UseCases (`ProcessTicketUseCase`, `UpdateMemberMoodUseCase`, `UpdateStockQuantityUseCase`).
- Command: `./gradlew :core:domain:test` (or specific module test task).

### Manual Verification
- **OCR:** Verify that scanning a ticket still populates the price and store fields in the Finance feature.
- **Mood:** Verify that updating a family member's mood correctly updates the emoji and timestamp in the dashboard.
- **Inventory:** Verify that adding/removing items correctly triggers auto-restock logic.
- **Navigation:** Verify that navigating to Family and Lists still works after modularization.
- **Build:** Verify the project builds successfully: `./gradlew assembleDebug`

package com.appcasa.features.inventory.domain.usecase

import com.appcasa.core.domain.model.StockItem
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class UpdateStockQuantityUseCaseTest {

    private val updateStockItemUseCase: UpdateStockItemUseCase = mockk()
    private val autoRestockStockItemUseCase: AutoRestockStockItemUseCase = mockk()
    private val useCase = UpdateStockQuantityUseCase(updateStockItemUseCase, autoRestockStockItemUseCase)

    @Test
    fun `should increment quantity and trigger auto restock`() = runTest {
        // Given
        val item = StockItem(id = 1L, hogarId = 1L, nombre = "Leche", categoria = "Lacteos", cantidadActual = 2.0, cantidadMinima = 1.0, unidad = "L")
        val delta = 1.0
        
        coEvery { updateStockItemUseCase(any()) } returns Unit
        coEvery { autoRestockStockItemUseCase(any()) } returns Unit

        // When
        useCase(item, delta)

        // Then
        coVerify { 
            updateStockItemUseCase(withArg { 
                assert(it.cantidadActual == 3.0)
            })
            autoRestockStockItemUseCase(withArg {
                assert(it.cantidadActual == 3.0)
            })
        }
    }

    @Test
    fun `should not allow negative quantity`() = runTest {
        // Given
        val item = StockItem(id = 1L, hogarId = 1L, nombre = "Leche", categoria = "Lacteos", cantidadActual = 1.0, cantidadMinima = 1.0, unidad = "L")
        val delta = -2.0
        
        coEvery { updateStockItemUseCase(any()) } returns Unit
        coEvery { autoRestockStockItemUseCase(any()) } returns Unit

        // When
        useCase(item, delta)

        // Then
        coVerify { 
            updateStockItemUseCase(withArg { 
                assert(it.cantidadActual == 0.0)
            })
        }
    }
}

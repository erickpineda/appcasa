package com.appcasa.features.finance.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class ProcessTicketUseCaseTest {

    private val useCase = ProcessTicketUseCase()

    @Test
    fun `should extract max price and store name from ticket text`() {
        // Given
        val text = """
            MERCADONA
            C/ Mayor 123
            12/05/2026
            LECHE    1,50
            PAN      0,80
            TOTAL    2,30
        """.trimIndent()

        // When
        val result = useCase.interpretText(text)

        // Then
        assertEquals(2.30, result.total!!, 0.001)
        assertEquals("MERCADONA", result.store)
    }

    @Test
    fun `should handle prices with dot as decimal separator`() {
        // Given
        val text = "CARREFOUR\nTOTAL 15.99"

        // When
        val result = useCase.interpretText(text)

        // Then
        assertEquals(15.99, result.total!!, 0.001)
    }
}

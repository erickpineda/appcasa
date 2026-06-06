package com.appcasa.core.domain.usecase

import com.appcasa.core.domain.model.FamilyMember
import com.appcasa.core.domain.model.TipoMiembro
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class UpdateMemberMoodUseCaseTest {

    private val getMemberByIdUseCase: GetMemberByIdUseCase = mockk()
    private val updateMemberUseCase: UpdateMemberUseCase = mockk()
    private val useCase = UpdateMemberMoodUseCase(getMemberByIdUseCase, updateMemberUseCase)

    @Test
    fun `when emoji is not null, should update member with emoji and current timestamp`() = runTest {
        // Given
        val memberId = 1L
        val emoji = "😊"
        val member = FamilyMember(id = memberId, hogarId = 1L, nombre = "Juan", tipo = TipoMiembro.PERSONA)
        
        coEvery { getMemberByIdUseCase(memberId) } returns member
        coEvery { updateMemberUseCase(any()) } returns Unit

        // When
        useCase(memberId, emoji)

        // Then
        coVerify { 
            updateMemberUseCase(withArg { 
                assertNotNull(it.estadoAnimoUpdatedAt)
                assert(it.estadoAnimo == emoji)
            }) 
        }
    }

    @Test
    fun `when emoji is null, should update member with null emoji and null timestamp`() = runTest {
        // Given
        val memberId = 1L
        val emoji = null
        val member = FamilyMember(id = memberId, hogarId = 1L, nombre = "Juan", tipo = TipoMiembro.PERSONA, estadoAnimo = "😊", estadoAnimoUpdatedAt = 12345L)
        
        coEvery { getMemberByIdUseCase(memberId) } returns member
        coEvery { updateMemberUseCase(any()) } returns Unit

        // When
        useCase(memberId, emoji)

        // Then
        coVerify { 
            updateMemberUseCase(withArg { 
                assertNull(it.estadoAnimoUpdatedAt)
                assertNull(it.estadoAnimo)
            }) 
        }
    }
}

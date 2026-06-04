package com.ubusmobilidade.ubus

import kotlin.test.Test
import kotlin.test.assertEquals

class FleetLogicTest {

    @Test
    fun testCalculateCapacity() {
        // Simulação da lógica que está no CadastroVeiculoMultiStepScreen
        fun calculate(lastSeat: Int, hasBathroom: Boolean, emptySeatsInFirstRow: Int): Int {
            var base = lastSeat
            if (hasBathroom) base -= 1
            base -= emptySeatsInFirstRow
            return base
        }

        // Caso 1: 48 poltronas, com banheiro, primeira fileira completa (4 poltronas)
        assertEquals(47, calculate(48, true, 0))

        // Caso 2: 48 poltronas, sem banheiro, primeira fileira com 1 faltando
        assertEquals(47, calculate(48, false, 1))

        // Caso 3: 48 poltronas, com banheiro, primeira fileira com 2 faltando
        assertEquals(45, calculate(48, true, 2))
    }
}

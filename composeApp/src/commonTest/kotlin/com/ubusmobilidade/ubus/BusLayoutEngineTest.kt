package com.ubusmobilidade.ubus

import com.ubusmobilidade.ubus.data.model.*
import com.ubusmobilidade.ubus.ui.util.BusLayoutEngine
import kotlin.test.*

class BusLayoutEngineTest {

    @Test
    fun testValidation() {
        val baseAnswers = BusWizardAnswers(
            plate = "ABC-1234",
            identificationNumber = "1050",
            p1 = SeatNumberingMode.PHYSICAL,
            p2 = FrontRowLayout.FOUR,
            p3 = RearLayout.NORMAL,
            p4capacity = 44,
            p5 = AccessibilityFeature.NONE,
            p6 = NumerationSide.LEFT
        )

        val errBoxNone = baseAnswers.copy(p3 = RearLayout.BOX, p5 = AccessibilityFeature.NONE)
        assertNotNull(BusLayoutEngine.validate(errBoxNone))

        val errNotBoxDpm = baseAnswers.copy(p3 = RearLayout.NORMAL, p5 = AccessibilityFeature.BOX)
        assertNotNull(BusLayoutEngine.validate(errNotBoxDpm))

        val errThreeBathroom = baseAnswers.copy(p2 = FrontRowLayout.THREE, p3 = RearLayout.BATHROOM)
        assertNotNull(BusLayoutEngine.validate(errThreeBathroom))

        val errFourFive = baseAnswers.copy(p2 = FrontRowLayout.FOUR, p3 = RearLayout.FIVE)
        assertNotNull(BusLayoutEngine.validate(errFourFive))

        val errCapacityInvalid = baseAnswers.copy(p4capacity = 45)
        assertNotNull(BusLayoutEngine.validate(errCapacityInvalid))

        val ok = BusLayoutEngine.validate(baseAnswers)
        assertNull(ok)
    }

    @Test
    fun testBuildLayoutRowsAndSeats() {
        val answersP2Four = BusWizardAnswers(
            plate = "ABC-1234",
            identificationNumber = "1050",
            p1 = SeatNumberingMode.PHYSICAL,
            p2 = FrontRowLayout.FOUR,
            p3 = RearLayout.NORMAL,
            p4capacity = 44,
            p5 = AccessibilityFeature.NONE,
            p6 = NumerationSide.LEFT
        )
        val layoutFour = BusLayoutEngine.buildLayout(answersP2Four)
        assertEquals(CellType.SEAT, layoutFour.rows.first().cells[0].type)
        assertEquals(CellType.SEAT, layoutFour.rows.first().cells[1].type)
        assertEquals(CellType.AISLE, layoutFour.rows.first().cells[2].type)
        assertEquals(CellType.SEAT, layoutFour.rows.first().cells[3].type)
        assertEquals(CellType.SEAT, layoutFour.rows.first().cells[4].type)

        val answersP2Three = answersP2Four.copy(p2 = FrontRowLayout.THREE)
        val layoutThree = BusLayoutEngine.buildLayout(answersP2Three)
        assertEquals(CellType.EMPTY, layoutThree.rows.first().cells[3].type)

        val answersP2Two = answersP2Four.copy(p2 = FrontRowLayout.TWO)
        val layoutTwo = BusLayoutEngine.buildLayout(answersP2Two)
        assertEquals(CellType.EMPTY, layoutTwo.rows.first().cells[1].type)
        assertEquals(CellType.EMPTY, layoutTwo.rows.first().cells[3].type)

        val answersP3Bathroom = answersP2Four.copy(p3 = RearLayout.BATHROOM, p4capacity = 42)
        val layoutBathroom = BusLayoutEngine.buildLayout(answersP3Bathroom)
        assertEquals(CellType.BATHROOM, layoutBathroom.rows.last().cells[3].type)
        assertEquals(CellType.BATHROOM, layoutBathroom.rows.last().cells[4].type)

        val answersP3Box = answersP2Four.copy(p3 = RearLayout.BOX, p5 = AccessibilityFeature.BOX, p4capacity = 40)
        val layoutBox = BusLayoutEngine.buildLayout(answersP3Box)
        assertTrue(layoutBox.rows.last().cells.all { it.type == CellType.BOX })

        val answersP3Five = answersP2Four.copy(p3 = RearLayout.FIVE, p2 = FrontRowLayout.TWO, p4capacity = 47)
        val layoutFive = BusLayoutEngine.buildLayout(answersP3Five)
        assertTrue(layoutFive.rows.last().cells.all { it.type == CellType.SEAT })
    }

    @Test
    fun testSeatPosition() {
        val answers = BusWizardAnswers(
            plate = "ABC-1234",
            identificationNumber = "1050",
            p1 = SeatNumberingMode.PHYSICAL,
            p2 = FrontRowLayout.FOUR,
            p3 = RearLayout.NORMAL,
            p4capacity = 44,
            p5 = AccessibilityFeature.NONE,
            p6 = NumerationSide.LEFT
        )
        val layout = BusLayoutEngine.buildLayout(answers)
        val row = layout.rows[1]
        assertEquals(SeatPosition.WINDOW_LEFT, row.cells[0].position)
        assertEquals(SeatPosition.AISLE_LEFT, row.cells[1].position)
        assertNull(row.cells[2].position)
        assertEquals(SeatPosition.AISLE_RIGHT, row.cells[3].position)
        assertEquals(SeatPosition.WINDOW_RIGHT, row.cells[4].position)
    }

    @Test
    fun testNumerationAndDpm() {
        val answersLeft = BusWizardAnswers(
            plate = "ABC-1234",
            identificationNumber = "1050",
            p1 = SeatNumberingMode.PHYSICAL,
            p2 = FrontRowLayout.FOUR,
            p3 = RearLayout.NORMAL,
            p4capacity = 44,
            p5 = AccessibilityFeature.DPM,
            p6 = NumerationSide.LEFT
        )
        val layoutLeft = BusLayoutEngine.buildLayout(answersLeft)
        assertEquals(1, layoutLeft.rows[0].cells[0].virtualNumber)
        assertEquals(2, layoutLeft.rows[0].cells[1].virtualNumber)
        assertEquals(3, layoutLeft.rows[0].cells[3].virtualNumber)
        assertEquals(4, layoutLeft.rows[0].cells[4].virtualNumber)

        val dpmLeftVal = layoutLeft.dpmSeatVirtualNumber
        assertEquals(3, dpmLeftVal)
        assertTrue(layoutLeft.rows[0].cells[3].isDpm)

        val answersRight = answersLeft.copy(p6 = NumerationSide.RIGHT)
        val layoutRight = BusLayoutEngine.buildLayout(answersRight)
        assertEquals(4, layoutRight.rows[0].cells[0].virtualNumber)
        assertEquals(3, layoutRight.rows[0].cells[1].virtualNumber)
        assertEquals(2, layoutRight.rows[0].cells[3].virtualNumber)
        assertEquals(1, layoutRight.rows[0].cells[4].virtualNumber)

        val dpmRightVal = layoutRight.dpmSeatVirtualNumber
        assertEquals(3, dpmRightVal)
        assertTrue(layoutRight.rows[0].cells[1].isDpm)

        val answersTwoDpmLeft = answersLeft.copy(p2 = FrontRowLayout.TWO)
        val layoutTwoDpmLeft = BusLayoutEngine.buildLayout(answersTwoDpmLeft)
        val dpmFallbackLeftVal = layoutTwoDpmLeft.dpmSeatVirtualNumber
        assertEquals(2, dpmFallbackLeftVal)
        assertTrue(layoutTwoDpmLeft.rows[0].cells[4].isDpm)

        val answersTwoDpmRight = answersRight.copy(p2 = FrontRowLayout.TWO)
        val layoutTwoDpmRight = BusLayoutEngine.buildLayout(answersTwoDpmRight)
        val dpmFallbackRightVal = layoutTwoDpmRight.dpmSeatVirtualNumber
        assertEquals(2, dpmFallbackRightVal)
        assertTrue(layoutTwoDpmRight.rows[0].cells[0].isDpm)
    }

    @Test
    fun testApplyPhysicalNumbers() {
        val answers = BusWizardAnswers(
            plate = "ABC-1234",
            identificationNumber = "1050",
            p1 = SeatNumberingMode.MIXED,
            p2 = FrontRowLayout.FOUR,
            p3 = RearLayout.NORMAL,
            p4capacity = 44,
            p5 = AccessibilityFeature.NONE,
            p6 = NumerationSide.LEFT
        )
        val layout = BusLayoutEngine.buildLayout(answers)
        val map = mapOf(1 to 101, 2 to 102)
        val updated = BusLayoutEngine.applyPhysicalNumbers(layout, map)

        assertEquals(101, updated.rows[0].cells[0].physicalNumber)
        assertEquals(102, updated.rows[0].cells[1].physicalNumber)
        assertNull(updated.rows[0].cells[3].physicalNumber)
    }
}

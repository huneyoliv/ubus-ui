package com.ubusmobilidade.ubus.ui.util

import com.ubusmobilidade.ubus.data.model.*

object BusLayoutEngine {

    fun validate(answers: BusWizardAnswers): String? {
        val p2 = answers.p2
        val p3 = answers.p3
        val p4capacity = answers.p4capacity
        val p5 = answers.p5

        if (p3 == RearLayout.BOX && p5 == AccessibilityFeature.NONE) {
            return "Se há box no fundo, deve existir um recurso de acessibilidade. Revise P5."
        }
        if (p3 != RearLayout.BOX && p5 == AccessibilityFeature.BOX) {
            return "Box de cadeira de rodas só é possível quando o fundo foi declarado como espaço de cadeirante (P3=D). Revise P3 ou P5."
        }

        if (p2 == FrontRowLayout.THREE && p3 == RearLayout.BATHROOM) {
            return "Combinação inválida de primeira fileira e fundo do ônibus."
        }
        if (p2 == FrontRowLayout.FOUR && p3 == RearLayout.FIVE) {
            return "Combinação inválida de primeira fileira e fundo do ônibus."
        }

        val frontSeats = when (p2) {
            FrontRowLayout.FOUR -> 4
            FrontRowLayout.THREE -> 3
            FrontRowLayout.TWO -> 2
        }

        val rearSeats = when (p3) {
            RearLayout.BATHROOM -> 2
            RearLayout.NORMAL -> 4
            RearLayout.FIVE -> 5
            RearLayout.BOX -> 0
        }

        val remaining = p4capacity - frontSeats - rearSeats
        if (remaining < 0 || remaining % 4 != 0) {
            return "O número total de lugares não fecha com o layout informado. Revise P2, P3 ou P4."
        }

        return null
    }

    fun buildLayout(answers: BusWizardAnswers): BusLayout {
        val p2 = answers.p2
        val p3 = answers.p3
        val p4capacity = answers.p4capacity
        val p6 = answers.p6
        val p6b = answers.p6b

        val frontRowCells = buildFrontRowCells(p2)

        val frontSeats = when (p2) {
            FrontRowLayout.FOUR -> 4
            FrontRowLayout.THREE -> 3
            FrontRowLayout.TWO -> 2
        }
        val rearSeats = when (p3) {
            RearLayout.BATHROOM -> 2
            RearLayout.NORMAL -> 4
            RearLayout.FIVE -> 5
            RearLayout.BOX -> 0
        }
        val middleRowsCount = (p4capacity - frontSeats - rearSeats) / 4

        val allRowsCells = mutableListOf<MutableList<BusCell>>()
        allRowsCells.add(frontRowCells)
        repeat(middleRowsCount) {
            allRowsCells.add(buildMiddleRowCells())
        }
        allRowsCells.add(buildRearRowCells(p3))

        val isRearFive = p3 == RearLayout.FIVE

        when (p6b) {
            NumberingPattern.SEQUENTIAL -> assignSequential(allRowsCells, p6, isRearFive, answers)
            NumberingPattern.ODD_WINDOW -> assignOddEvenWindow(allRowsCells, p6, isRearFive, answers, windowGetsOdd = true)
            NumberingPattern.EVEN_WINDOW -> assignOddEvenWindow(allRowsCells, p6, isRearFive, answers, windowGetsOdd = false)
        }

        val tempRows = allRowsCells.map { BusLayoutRow(it.toList()) }
        val tempLayout = BusLayout(
            busId = "",
            numberingMode = answers.p1,
            numerationSide = p6,
            rows = tempRows,
            dpmSeatVirtualNumber = null
        )

        val dpmVirtualNum = computeDpmVirtualNumber(answers, tempLayout)

        if (dpmVirtualNum != null) {
            for (rowCells in allRowsCells) {
                for (j in rowCells.indices) {
                    val cell = rowCells[j]
                    if (cell.virtualNumber == dpmVirtualNum) {
                        rowCells[j] = cell.copy(isDpm = true)
                    }
                }
            }
        }

        val finalRows = allRowsCells.map { BusLayoutRow(it.toList()) }
        return BusLayout(
            busId = "",
            numberingMode = answers.p1,
            numerationSide = p6,
            rows = finalRows,
            dpmSeatVirtualNumber = dpmVirtualNum
        )
    }

    private fun assignSequential(
        allRowsCells: MutableList<MutableList<BusCell>>,
        p6: NumerationSide,
        isRearFive: Boolean,
        answers: BusWizardAnswers
    ) {
        var virtualCounter = 1
        for (i in allRowsCells.indices) {
            val rowCells = allRowsCells[i]
            val isLastRow = i == allRowsCells.lastIndex
            val colIndices = if (isLastRow && isRearFive) {
                (0..4).toList()
            } else if (p6 == NumerationSide.LEFT) {
                (0..4).toList()
            } else {
                (4 downTo 0).toList()
            }

            for (colIdx in colIndices) {
                val cell = rowCells[colIdx]
                if (cell.type == CellType.SEAT) {
                    val virtualNum = virtualCounter++
                    rowCells[colIdx] = cell.copy(
                        virtualNumber = virtualNum,
                        physicalNumber = physicalFor(virtualNum, answers)
                    )
                }
            }
        }
    }

    /**
     * Ímpar/par nas janelas: dois contadores paralelos percorrem os assentos
     * linha a linha (frente→fundo, lado definido por p6).
     * Pass 1 → janelas (WINDOW_LEFT, WINDOW_RIGHT): recebem 1, 3, 5… (windowGetsOdd=true) ou 2, 4, 6…
     * Pass 2 → corredor (AISLE_LEFT, AISLE_RIGHT): recebem 2, 4, 6… ou 1, 3, 5…
     * Fileira de 5 (rear five) e fileiras especiais (BATHROOM/BOX) seguem sequencial.
     */
    private fun assignOddEvenWindow(
        allRowsCells: MutableList<MutableList<BusCell>>,
        p6: NumerationSide,
        isRearFive: Boolean,
        answers: BusWizardAnswers,
        windowGetsOdd: Boolean
    ) {
        val windowPositions = setOf(SeatPosition.WINDOW_LEFT, SeatPosition.WINDOW_RIGHT)
        val aislePositions = setOf(SeatPosition.AISLE_LEFT, SeatPosition.AISLE_RIGHT)

        var oddCounter = if (windowGetsOdd) 1 else 2
        var evenCounter = if (windowGetsOdd) 2 else 1

        val normalIndices = if (p6 == NumerationSide.LEFT) (0..4).toList() else (4 downTo 0).toList()

        for (i in allRowsCells.indices) {
            val rowCells = allRowsCells[i]
            val isLastRow = i == allRowsCells.lastIndex
            val colIndices = if (isLastRow && isRearFive) (0..4).toList() else normalIndices

            if (isLastRow && isRearFive) {
                var seqCounter = (oddCounter.coerceAtLeast(evenCounter) - 1).let {
                    val lastAssigned = allRowsCells.take(i)
                        .flatMap { it }
                        .mapNotNull { it.virtualNumber }
                        .maxOrNull() ?: 0
                    lastAssigned + 1
                }
                for (colIdx in colIndices) {
                    val cell = rowCells[colIdx]
                    if (cell.type == CellType.SEAT) {
                        val virtualNum = seqCounter++
                        rowCells[colIdx] = cell.copy(
                            virtualNumber = virtualNum,
                            physicalNumber = physicalFor(virtualNum, answers)
                        )
                    }
                }
                continue
            }

            for (colIdx in colIndices) {
                val cell = rowCells[colIdx]
                if (cell.type != CellType.SEAT) continue
                val pos = cell.position

                val virtualNum = when {
                    pos in windowPositions -> {
                        val n = oddCounter
                        oddCounter += 2
                        n
                    }
                    pos in aislePositions -> {
                        val n = evenCounter
                        evenCounter += 2
                        n
                    }
                    else -> {
                        val n = oddCounter.coerceAtLeast(evenCounter)
                        oddCounter = n + 2
                        n
                    }
                }
                rowCells[colIdx] = cell.copy(
                    virtualNumber = virtualNum,
                    physicalNumber = physicalFor(virtualNum, answers)
                )
            }
        }
    }

    private fun physicalFor(virtualNum: Int, answers: BusWizardAnswers): Int? {
        return if (answers.p1 == SeatNumberingMode.PHYSICAL) virtualNum
        else answers.p7physicalNumbers[virtualNum]
    }

    fun computeDpmVirtualNumber(answers: BusWizardAnswers, layout: BusLayout): Int? {
        if (answers.p5 != AccessibilityFeature.DPM) return null
        if (layout.rows.isEmpty()) return null

        val firstRow = layout.rows.first()
        return if (answers.p6 == NumerationSide.LEFT) {
            val col4Cell = firstRow.cells[3]
            if (col4Cell.type == CellType.SEAT) {
                col4Cell.virtualNumber
            } else {
                val col5Cell = firstRow.cells[4]
                if (col5Cell.type == CellType.SEAT) col5Cell.virtualNumber else null
            }
        } else {
            val col2Cell = firstRow.cells[1]
            if (col2Cell.type == CellType.SEAT) {
                col2Cell.virtualNumber
            } else {
                val col1Cell = firstRow.cells[0]
                if (col1Cell.type == CellType.SEAT) col1Cell.virtualNumber else null
            }
        }
    }

    fun applyPhysicalNumbers(layout: BusLayout, p7map: Map<Int, Int>): BusLayout {
        val updatedRows = layout.rows.map { row ->
            BusLayoutRow(
                row.cells.map { cell ->
                    if (cell.type == CellType.SEAT) {
                        val mapped = p7map[cell.virtualNumber] ?: cell.physicalNumber
                        cell.copy(physicalNumber = mapped)
                    } else {
                        cell
                    }
                }
            )
        }
        return layout.copy(rows = updatedRows)
    }

    private fun buildFrontRowCells(p2: FrontRowLayout): MutableList<BusCell> {
        return when (p2) {
            FrontRowLayout.FOUR -> mutableListOf(
                BusCell(1, CellType.SEAT, position = SeatPosition.WINDOW_LEFT),
                BusCell(2, CellType.SEAT, position = SeatPosition.AISLE_LEFT),
                BusCell(3, CellType.AISLE),
                BusCell(4, CellType.SEAT, position = SeatPosition.AISLE_RIGHT),
                BusCell(5, CellType.SEAT, position = SeatPosition.WINDOW_RIGHT)
            )
            FrontRowLayout.THREE -> mutableListOf(
                BusCell(1, CellType.SEAT, position = SeatPosition.WINDOW_LEFT),
                BusCell(2, CellType.SEAT, position = SeatPosition.AISLE_LEFT),
                BusCell(3, CellType.AISLE),
                BusCell(4, CellType.EMPTY),
                BusCell(5, CellType.SEAT, position = SeatPosition.WINDOW_RIGHT)
            )
            FrontRowLayout.TWO -> mutableListOf(
                BusCell(1, CellType.SEAT, position = SeatPosition.WINDOW_LEFT),
                BusCell(2, CellType.EMPTY),
                BusCell(3, CellType.AISLE),
                BusCell(4, CellType.EMPTY),
                BusCell(5, CellType.SEAT, position = SeatPosition.WINDOW_RIGHT)
            )
        }
    }

    private fun buildMiddleRowCells(): MutableList<BusCell> {
        return mutableListOf(
            BusCell(1, CellType.SEAT, position = SeatPosition.WINDOW_LEFT),
            BusCell(2, CellType.SEAT, position = SeatPosition.AISLE_LEFT),
            BusCell(3, CellType.AISLE),
            BusCell(4, CellType.SEAT, position = SeatPosition.AISLE_RIGHT),
            BusCell(5, CellType.SEAT, position = SeatPosition.WINDOW_RIGHT)
        )
    }

    private fun buildRearRowCells(p3: RearLayout): MutableList<BusCell> {
        return when (p3) {
            RearLayout.BATHROOM -> mutableListOf(
                BusCell(1, CellType.SEAT, position = SeatPosition.WINDOW_LEFT),
                BusCell(2, CellType.SEAT, position = SeatPosition.AISLE_LEFT),
                BusCell(3, CellType.AISLE),
                BusCell(4, CellType.BATHROOM),
                BusCell(5, CellType.BATHROOM)
            )
            RearLayout.NORMAL -> mutableListOf(
                BusCell(1, CellType.SEAT, position = SeatPosition.WINDOW_LEFT),
                BusCell(2, CellType.SEAT, position = SeatPosition.AISLE_LEFT),
                BusCell(3, CellType.AISLE),
                BusCell(4, CellType.SEAT, position = SeatPosition.AISLE_RIGHT),
                BusCell(5, CellType.SEAT, position = SeatPosition.WINDOW_RIGHT)
            )
            RearLayout.FIVE -> mutableListOf(
                BusCell(1, CellType.SEAT, position = SeatPosition.WINDOW_LEFT),
                BusCell(2, CellType.SEAT, position = SeatPosition.AISLE_LEFT),
                BusCell(3, CellType.SEAT, position = SeatPosition.CENTER),
                BusCell(4, CellType.SEAT, position = SeatPosition.AISLE_RIGHT),
                BusCell(5, CellType.SEAT, position = SeatPosition.WINDOW_RIGHT)
            )
            RearLayout.BOX -> mutableListOf(
                BusCell(1, CellType.BOX),
                BusCell(2, CellType.BOX),
                BusCell(3, CellType.BOX),
                BusCell(4, CellType.BOX),
                BusCell(5, CellType.BOX)
            )
        }
    }
}

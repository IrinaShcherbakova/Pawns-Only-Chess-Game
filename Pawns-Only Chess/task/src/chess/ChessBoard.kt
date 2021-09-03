package chess

import kotlin.math.*

data class Pawn(var row: Int, var column: Char, val color: Char)

object ChessBoard {

    private val horizontalLine = "+---+---+---+---+---+---+---+---+"
    private val verticalLine = "|   |   |   |   |   |   |   |   |"
    private val lineLetters = "  a   b   c   d   e   f   g   h "
    private val pawns = initPawns()
    private var isPassant = false
    var winner = ""

    private fun initPawns() : MutableList<Pawn>{
        val whiteRow = 2; val blackRow = 7
        val whiteColor = 'W'; val blackColor ='B'
        val pawns = mutableListOf<Pawn>()
        for(column in 'a'..'h'){
            val white = Pawn(whiteRow, column, whiteColor)
            val black = Pawn(blackRow, column, blackColor)
            pawns.add(white)
            pawns.add(black)
        }
        return pawns
    }

    private fun getPawnsAtRow(row: Int) : Set<Pawn> {
        return pawns.filter { it.row == row }.toSet()
    }

    private fun getPawnAtCoordinate(row: Int, column: Char) : Pawn? {
        return pawns.firstOrNull(){ it.row == row && it.column == column }
    }

    fun printChessBoard() {
        println("  $horizontalLine")
        for(row in 8 downTo 1){
            printRow(row)
            print("\n")
            println("  $horizontalLine")
        }
        println("  $lineLetters")
    }

    private fun printRow(row: Int){
        val pawnsAtRow = getPawnsAtRow(row)

        if(pawnsAtRow.isEmpty()){
            print("$row $verticalLine")
            return
        }

        print("$row |")
        for(col in 'a'..'h'){
            val pawn = pawnsAtRow.firstOrNull() { it.column == col }
            if(pawn != null){
                print(" ${pawn.color} |")
            } else{
                print("   |")
            }
        }
    }

    fun isStalemate(color: Char) : Boolean {
        val playersPawns = pawns.filter { it.color == color }
        val offset = if(color == 'W') 1 else -1
        val startRow = if(color == 'W') 2 else 7

        //check if any move forward is possible
        playersPawns.forEach() {
            if(!isMoveBlocked(it.row+offset, it.column)) return false
            else if(it.row == startRow && !isMoveBlocked(it.row+(offset*2), it.column)) return false
        }

        //check if any move diagonally is possible
        playersPawns.forEach() {
            if(canAttackDiagonally(it)) return false
        }

        return true
    }

    fun makeMove(move: String, color: Char) : Boolean {
        val startColumn = move[0]; val startRow = move[1].code - '0'.code
        val targetColumn = move[2]; val targetRow = move[3].code - '0'.code

        if(!isValidMove(startRow, startColumn, targetRow, targetColumn, color))   return false

        val movingPawn = pawns.first { it.row == startRow && it.column == startColumn }
        val isMoved = if(movingPawn.column == targetColumn) {
            movePawnForward(movingPawn, targetRow, targetColumn)
        } else{
            movePawnDiagonally(movingPawn, targetRow, targetColumn)
        }

        if(!isMoved) printInvalidMoveHelper()
        return true
    }

    private fun printInvalidMoveHelper() : Boolean {
        println("Invalid Input")
        return false
    }

    private fun isValidMove(startRow: Int, startColumn: Char, targetRow: Int,
                    targetColumn: Char, color: Char) : Boolean {
        var isInvalidMove = false
        val colorStartRow = if(color == 'W') 2 else 7

        if(isMoveOutOfRange(startRow, startColumn) || isMoveOutOfRange(targetRow, targetColumn)) isInvalidMove = true
        if(startRow == colorStartRow && abs(startRow - targetRow) > 2) isInvalidMove = true
        if(startRow != colorStartRow && abs(startRow - targetRow) > 1) isInvalidMove = true
        if(color == 'W' && targetRow < startRow) isInvalidMove = true
        if(color == 'B' && targetRow > startRow) isInvalidMove = true

        if(isInvalidMove) printInvalidMoveHelper()

        val pawnAtCell = pawns.firstOrNull() { it.row == startRow && it.column == startColumn }
        if(pawnAtCell == null || pawnAtCell.color != color){
            val pawnColor = if(color == 'W') "white" else "black"
            println("No $pawnColor pawn at $startColumn$startRow")
            return false
        }

        if(startRow == targetRow)  isInvalidMove = true
        if(isInvalidMove) printInvalidMoveHelper()
        return true
    }

    private fun isMoveBlocked(row: Int, column: Char) =
        pawns.firstOrNull(){it.row == row && it.column == column} != null

    private fun isMoveOutOfRange(row: Int, column: Char) = column !in 'a'..'h' || row !in 1..8

    //move forward when target column is the same as the pawn's column
    private fun movePawnForward(pawn: Pawn, targetRow: Int, targetColumn: Char) : Boolean {
        if (isMoveBlocked(targetRow, targetColumn)) return false

        if(abs(targetRow - pawn.row) == 2){
            val skippedRow = if(pawn.color == 'B') targetRow + 1 else targetRow - 1
            if(isMoveBlocked(skippedRow, targetColumn)) return false
        }

        isPassant = abs(targetRow - pawn.row) == 2
        pawn.row = targetRow
        return true
    }

    //move diagonally when target column is different from the pawn's column
    private fun movePawnDiagonally(pawn: Pawn, targetRow: Int, targetColumn: Char) : Boolean{
        if(abs(targetColumn - pawn.column) != 1) return false

        if(captureAtPassant(pawn, targetRow, targetColumn))   return true
        val pawnAtTarget = getPawnAtCoordinate(targetRow, targetColumn) ?: return false
        if(pawnAtTarget.color == pawn.color)    return false

        pawns.remove(pawnAtTarget)
        pawn.row = targetRow
        pawn.column = targetColumn
        isPassant = false
        return true
    }

    private fun canAttackDiagonally(pawn: Pawn) : Boolean {
        if(canCaptureAtPassant(pawn))   return true

        val offset = if(pawn.color == 'W') 1 else -1
        val pawnAtLeftTarget = getPawnAtCoordinate(pawn.row+offset, pawn.column-1)
        if(pawnAtLeftTarget != null && pawnAtLeftTarget.color != pawn.color)    return true
        val pawnAtRightTarget = getPawnAtCoordinate(pawn.row+offset, pawn.column+1)
        if(pawnAtRightTarget != null && pawnAtRightTarget.color != pawn.color)    return true

        return false
    }

    private fun canCaptureAtPassant(pawn: Pawn) : Boolean {
        val targetRow = if(pawn.color == 'W') pawn.row + 1 else pawn.row - 1
        val rowAtPassant = if(pawn.color == 'W') targetRow - 1 else targetRow + 1
        val pawnAtLeftPassant = getPawnAtCoordinate(rowAtPassant, pawn.column - 1)
        val pawnAtRightPassant = getPawnAtCoordinate(rowAtPassant, pawn.column + 1)
        val attackRowAtPassant = if(pawn.color == 'W') 5 else 4

        if(pawnAtLeftPassant != null && isPassant && attackRowAtPassant == pawn.row
                && pawnAtLeftPassant.color != pawn.color) return true

        if(pawnAtRightPassant != null && isPassant && attackRowAtPassant == pawn.row
                && pawnAtRightPassant.color != pawn.color)  return true

        return false
    }

    private fun captureAtPassant(pawn: Pawn, targetRow: Int, targetColumn: Char) : Boolean{
        if(!canCaptureAtPassant(pawn)) return false

        val rowAtPassant = if(pawn.color == 'W') targetRow - 1 else targetRow + 1
        val pawnAtPassant = getPawnAtCoordinate(rowAtPassant, targetColumn)

        pawns.remove(pawnAtPassant)
        pawn.row = targetRow
        pawn.column = targetColumn
        isPassant = false
        return true
    }

    fun hasWinner() : Boolean {
        if(pawns.any { it.color == 'B' && it.row == 1 }){
            winner = "Black"
            return true
        }
        if(pawns.any { it.color == 'W' && it.row == 8 }){
            winner = "White"
            return true
        }
        if(pawns.count { it.color == 'B' } == 0){
            winner = "White"
            return true
        }
        if(pawns.count { it.color == 'W' } == 0){
            winner = "Black"
            return true
        }

        return false
    }

}
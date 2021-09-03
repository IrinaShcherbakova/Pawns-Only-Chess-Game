package chess

fun main() {
    println("Pawns-Only Chess")
    println("First Player's name:")
    val player1 = readLine()!!
    println("Second Player's name:")
    val player2 = readLine()!!
    ChessBoard.printChessBoard()
    var turn = player1; var color = 'W'

    while(true){

        if(ChessBoard.isStalemate(color)){
            println("Stalemate!")
            println("Bye!")
            return
        }

        println("$turn\'s turn:")
        val move = readLine()!!
        if(move == "exit"){
            println("Bye!")
            return
        }
        if(ChessBoard.makeMove(move, color)){
            ChessBoard.printChessBoard()
            turn = if (turn == player1) player2 else player1
            color = if(color == 'W') 'B' else 'W'
            if(ChessBoard.hasWinner()){
                println("${ChessBoard.winner} Wins!")
                println("Bye!")
                return
            }
        }
    }
}




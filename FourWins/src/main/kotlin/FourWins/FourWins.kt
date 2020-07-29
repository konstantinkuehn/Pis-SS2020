package FourWins

import java.util.*
import kotlin.Int.Companion.MIN_VALUE

class FourWins {

    constructor() {
        boardData = (IntArray(42))
        currentPlayer = 1
    }

    constructor(oldData: FourWins) {
        prevBoard = oldData;
        // println(prevBoard.toString())
        this.boardData = oldData.boardData.clone();
    }

    val rowsize = 6
    val columnSize = 7

    var boardData = (IntArray(42))
    var currentPlayer = 1
    var prevBoard: FourWins? = null

    var lastindex = 0;

    fun IsValidMove(move: Int): Boolean {
        return boardData[move] == 0
    }

    fun AlphaBeta(depth: Int, alpha: Int, beta: Int): IntArray {

        var vAlpha = alpha
        //Check for draw game
        if (depth == 0)
            return intArrayOf(Evaludate(), lastindex)
        var principalVariationKnot = false

        var bestMove = intArrayOf(MIN_VALUE, -1)

        var possibleMoves = GetPossibleMoves()
        var value = 0
        for (move in possibleMoves) {
            var nextBoard = Move(move)
            if (principalVariationKnot) {
                value = -nextBoard.AlphaBeta(depth - 1, -vAlpha - 1, -vAlpha)
                if (value > vAlpha && vAlpha < beta)
                    value = -nextBoard.AlphaBeta(depth - 1, -beta, -value)
            } else {
                value = -nextBoard.AlphaBeta(depth - 1, -beta, -vAlpha)
            }
            if (value > bestMove[0]) {
                if (value >= beta)
                    return intArrayOf(value, move)
                bestMove = intArrayOf(value, move)
                if (value > vAlpha) {
                    vAlpha = value
                    principalVariationKnot = true
                }
            }
        }
        return bestMove
    }

    //TODO: Rework instead of calculating it on each call save on each call how many space in each column is reaming
    fun GetPossibleMoves(): IntArray {
        // Primitive implementation to get forward and get a first complete version done
        val list = mutableListOf<Int>()
        for (index in 0..columnSize - 1) {
            if (boardData[index] == 0)
                list.add(index)
        }
        return list.toIntArray()
    }

    fun Move(pos: Int): FourWins {
        // var data = Fourw

        var newboard = FourWins(this)

        //TODO: Check if requested move is possible

        if (IsValidMove(pos)) {
            for (row in 5 downTo 0) {
                var index = row * columnSize + pos
                if (boardData[index] == 0) {
                    newboard.boardData[index] = currentPlayer;
                    var matchstate = newboard.CheckForWinCondition()
                    newboard.lastindex = index;
                    if (matchstate == 1)
                        println("Jesus we have a win condition")
                    newboard.currentPlayer = -currentPlayer;
                    break
                    //println(String.format("set something to int %d",index))
                    // break
                }
            }
        } else {
            //TODO: Maybe print out that no valid move is possible
        }
        return newboard
    }

    fun Undo(): FourWins? {
        return prevBoard
    }

    private fun Evaludate(): Int {


        return 0
    }

    private fun GenericStreakCheck(lastindex: Int, steps: Int, streak: Int): Int {
        var matches = 0
        var newIndex = 0
        for (directionSteps in -3..3) {
            newIndex = lastindex + (directionSteps * steps)
            if (newIndex in 0..41) {
                if (boardData[lastindex] == boardData[newIndex]) {
                    if (matches + 1 == streak)
                        return 1
                    else
                        matches++;
                } else
                    matches = 0
            }
        }
        return 0
    }

    fun CheckForWinCondition(): Int {

        /* var check = 0
         check=GenericCheck(lastindex,columnSize)
         check=GenericCheck(lastindex,rowsize)
         check=GenericCheck(lastindex,columnSize+1)
         check=GenericCheck(lastindex,columnSize-1)*/
        val rows = arrayOf<Int>(columnSize, rowsize, columnSize + 1, (1))
        return Arrays.stream(rows).parallel().anyMatch() { row: Int ->
            GenericStreakCheck(lastindex, row, 4) == 1
        } as Int
    }

    fun GetMoveCount(): Int {
        var moves = 0
        if (prevBoard != null){
           moves += prevBoard?.GetMoveCount() as Int
        }
        else moves = 1
        return moves
    }


    override fun toString(): String {
        var toReturn = ""
        for (row in 0..5) {
            toReturn += "<tr>\n"
            for (column in 0..6) {
                var index = column + (row * 7)
                when (boardData[index]) {
                    0 -> toReturn += String.format("  <td bgcolor=\"%s\">%d</td>\n", "white", index)
                    1 -> toReturn += String.format("  <td bgcolor=\"%s\">%d</td>\n", "red", index)
                    -1 -> toReturn += String.format("  <td bgcolor=\"%s\">%d</td>\n", "blue", index)
                }
            }
            toReturn += "</tr>\n"
        }

        return toReturn
    }

}
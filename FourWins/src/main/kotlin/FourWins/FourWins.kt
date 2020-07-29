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

    fun AlphaBeta( alpha: Int, beta: Int): IntArray {

        var vAlpha = alpha
        var vBeta = beta
        //Check for draw game
        if (GetMoveCount() == 42)
            return intArrayOf(0, lastindex)

        for (position in 0..5) {
            if (IsValidMove(position)) {
                var move = Move(position)
                if (move.CheckForWinCondition() == 1) {
                    return intArrayOf(Evaluate(), move.lastindex)
                }
            }
        }
        var max = (columnSize * rowsize - 1 - GetMoveCount()) / 2

        if (vBeta > max) {
            vBeta = max
            if (vAlpha >= vBeta) return intArrayOf(vBeta, lastindex)
        }
        for (position in 0..6) {
            if (IsValidMove(position)) {
                var move = Move(position)
                var score = move.AlphaBeta( -vAlpha, -vBeta)
                score[0] = -score[0]
                if (score[0] >= vBeta) return score
                if (score[0] > vAlpha) vAlpha = score[0]
            }
        }
        return intArrayOf(vAlpha, lastindex)
        //   if (depth == 0)
        //     return intArrayOf(Evaluate(), lastindex)
        /*  var principalVariationKnot = false

          var bestMove = intArrayOf(MIN_VALUE, -1)

          var possibleMoves = GetPossibleMoves()
          var value = IntArray(2)
          for (move in possibleMoves) {
              var nextBoard = Move(move)
              if (principalVariationKnot) {
                  value = nextBoard.AlphaBeta(depth - 1, -vAlpha - 1, -vAlpha)
                  value[0] * -1
                  if (value[0] > vAlpha && vAlpha < beta) {
                      value[0] = -(value[0])
                      value = nextBoard.AlphaBeta(depth - 1, -beta, value[0])
                      value[0] * -1
                  }
              } else {
                  value = nextBoard.AlphaBeta(depth - 1, -beta, -vAlpha)
                  value[0] * -1
              }
              if (value[0] > bestMove[0]) {
                  if (value[0] >= beta)
                      return intArrayOf(value[0], move)
                  bestMove = intArrayOf(value[0], move)
                  if (value[0] > vAlpha) {
                      vAlpha = value[0]
                      principalVariationKnot = true
                  }
              }
          }*/
        //  return bestMove
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
                // println("try to go for index : " +  row + "|" + pos + "|"+ index)
                if (boardData[index] == 0) {
                    newboard.boardData[index] = currentPlayer;
                    newboard.lastindex = index;
                    var matchstate = newboard.CheckForWinCondition()
                    /* if (matchstate == 1)
                         println("Jesus we have a win condition")*/
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

    private fun Evaluate(): Int {
        var value = 0
        if (CheckForWinCondition() == 1)
            value = 1000
        return  (lastindex - GetMoveCount()) / 2
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
        var result = Arrays.stream(rows).parallel().anyMatch() { row: Int ->
            (GenericStreakCheck(lastindex, row, 4) == 1)
        }
        return if (result)
            1
        else
            0
    }

    fun GetMoveCount(): Int {
        var moves = 0
        if (prevBoard != null) {
            moves += prevBoard?.GetMoveCount() as Int
        } else moves = 1
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
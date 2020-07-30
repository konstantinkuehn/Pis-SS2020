package FourWins

import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.StringBuilder
import java.nio.file.Files
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.max

class FourWins {
    companion object {
        var entries: HashMap<Int, IntArray> = HashMap<Int, IntArray>()

    }

    constructor() {
        boardData = (IntArray(42))
        currentPlayer = 1
        LoadDB()
    }

    constructor(oldData: FourWins) {
        prevBoard = oldData;
        // println(prevBoard.toString())
        this.boardData = oldData.boardData.clone();
    }

    var winner = 0


    val rowsize = 6
    val columnSize = 7

    var boardData = (IntArray(42))
    var currentPlayer = 1
    var prevBoard: FourWins? = null

    var lastindex = 0;
    var starTime: Long = 0
    var timoutTheshhold: Int = 0

    val fileName = "db"

    fun SaveDB() {
        var stringbuilder = StringBuilder()
        for (item in entries) {
            stringbuilder.append(item.key.toString() + " " + item.value[0] + " " + item.value[1] + System.lineSeparator())
        }
        File(fileName).writeBytes(stringbuilder.toString().toByteArray(Charsets.UTF_8))
    }

    fun LoadDB() {
        var file = File(fileName)

        if (!file.exists())
            return
        var data = File(fileName).forEachLine { line ->

            val seperated = line.split(' ')

            if (!entries.containsKey(seperated[0].toInt())) {
                entries.put(seperated[0].toInt(), intArrayOf(seperated[1].toInt(), seperated[2].toInt()))
            }
        }

        println("Loaded DB with " + entries.count() + " entries. ")

    }

    fun IsValidMove(move: Int): Boolean {
        return boardData[move] == 0
    }

    fun SetStartTime(time: Long, threshold: Int) {
        starTime = time
        timoutTheshhold = threshold
    }

    fun GetHashForMove(): Int {
        return boardData.hashCode()
    }

    fun newAlphaBeta(depth: Int, alpha: Int, beta: Int, preventWin: Boolean = false, remainingChecks: Int = 0): IntArray {
        var vAlpha = alpha
        var vBeta = beta
        var relatedHash = GetHashForMove()
        //Depth is 0 or time is out
        var totalTime = (System.nanoTime() - starTime)
        val elapsedTimeInSecond = totalTime.toDouble() / 1000000000
        var timeout = timoutTheshhold <= elapsedTimeInSecond
        if (depth == 0 || timeout) {

            if (depth == 0)
                println("Timeout by depth")
            else if (timeout)
                println("Timeout by time")
            else
                println("calculates completly")
            if (FourWins.entries.containsKey(relatedHash)) {
                println("Saved some time calculated this already total calculation" + entries.count())
                return FourWins.entries.getValue(relatedHash) as IntArray
            }

            var array = intArrayOf(EvaluateTow(), lastindex)
            FourWins.entries.put(relatedHash, array)
            return array

        }
        //Check for draw game
        if (GetMoveCount() == 42) {
            println(" DRAW " + intArrayOf(0, lastindex))
            return intArrayOf(0, lastindex)
        }
        if (FourWins.entries.containsKey(relatedHash)) {
            println("Saved some time calculated this already total calculation" + entries.count())
            return FourWins.entries.getValue(relatedHash) as IntArray
        }
        for (position in 0..5) {
            if (IsValidMove(position)) {
                var move = Move(position)
                if (move.CheckForWinCondition() == 1) {
                    var array = intArrayOf(move.EvaluateTow(), move.lastindex)
                    //println(" Found a win condition score"+ array[0] + " for field" +  array[1])
                    // println(move.toString())
                    FourWins.entries.put(relatedHash, array)
                    return array
                } else if (preventWin) { // If the next enemy move would be a win
                    for (nextMove in 0..5) {
                        if (move.IsValidMove(nextMove)) {
                            var enemymove = move.Move(nextMove)
                            if (enemymove.CheckForWinCondition() == 1) {
                                var array = intArrayOf(-500, nextMove)

                                FourWins.entries.put(relatedHash, array)
                                return array
                            }
                        }
                    }
                }
            }
        }
        return intArrayOf(0, lastindex)
    }

    /*

            if (depth == 0) {
                return intArrayOf(Evaluate(), lastindex)
            }
            var possibleMoves = GetPossibleMoves()

            if (preventWin) {
                var score = Int.MIN_VALUE
                for (move in possibleMoves) {
                    vAlpha = max(score,vAlpha)
                    if(vAlpha>= vBeta){
                        break
                    }
                    return  intArrayOf(score,move)
                }
            } else {

            }
     */
    fun AlphaBeta(depth: Int, alpha: Int, beta: Int, preventWin: Boolean = false, remainingChecks: Int = 0): IntArray {

        var vAlpha = alpha
        var vBeta = beta
        var relatedHash = GetHashForMove()
        var totalTime = (System.nanoTime() - starTime)
        val elapsedTimeInSecond = totalTime.toDouble() / 1000000000
        var timeout = timoutTheshhold <= elapsedTimeInSecond

        if (depth == 0 || timeout) {
            var array = intArrayOf(Evaluate(), lastindex)

            if (FourWins.entries.containsKey(relatedHash)) {
                println("Saved some time calculated this already total calculation" + entries.count())
                return FourWins.entries.getValue(relatedHash) as IntArray
            }
            return intArrayOf(Evaluate(), lastindex)
        }

        if (FourWins.entries.containsKey(relatedHash)) {
            //println("Saved some time calculated this already total calculation" + entries.count())
            return FourWins.entries.getValue(relatedHash) as IntArray
        }

        var principalVariationKnot = false

        var bestMove = intArrayOf(Int.MIN_VALUE, 0)

        var possibleMoves = GetPossibleMoves()

        var value: IntArray
        for (move in possibleMoves) {

            var nextBoard = Move(move)
            if (principalVariationKnot) {
                value = nextBoard.AlphaBeta(depth - 1, -vAlpha - 1, -vAlpha)
                value[0] *= -1
                if (value[0] > vAlpha && value[0] < beta) {
                    value = nextBoard.AlphaBeta(depth - 1, -vBeta, -value[0])
                    value[0] *= -1
                }
            } else {
                value = nextBoard.AlphaBeta(depth - 1, -vBeta, -vAlpha)
                value[0] *= -1
            }
            if (value[0] > bestMove[0]) {
                if (value[0] >= vBeta) {
                    var array = intArrayOf(value[0], move)
                    FourWins.entries.put(relatedHash, array)
                    return array
                }
                bestMove = intArrayOf(value[0], move)
                if (value[0] > vAlpha) {
                    vAlpha = value[0]
                    principalVariationKnot = true
                }
            }
        }
        FourWins.entries.put(relatedHash, bestMove)

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
        newboard.SetStartTime(starTime, timoutTheshhold)
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

    private fun EvaluateTow():Int{
        return  (lastindex - GetMoveCount()) /2
    }
    private fun Evaluate(): Int {
        var score = 0
        var positive = 0
        var negative = 0
        if (CheckForWinCondition() == 1) {
            if (currentPlayer == 1) {
                positive += 90
            } else {
                negative += 90
            }
        }
        positive += (GetCountForPlayer(1, 3) * 10) + (GetCountForPlayer(1, 2) * 4)
        negative += (GetCountForPlayer(-1, 3) * 5) + (GetCountForPlayer(-1, 2))

        println("Rate Match: " + positive + " " + negative +  " end score" + (negative-positive))

        return negative - positive
        /*  var value = 0
          if (CheckForWinCondition() == 1)
              value = 1000
          return (lastindex - GetMoveCount()) / 2*/
    }

    private fun GetCountForPlayer(player: Int, streak: Int): Int {
        var totalSum: Int = 0
        val rows = arrayOf<Int>(columnSize, rowsize, columnSize + 1, (1))
        for (row in rows)
            totalSum += GenericStreakCount(lastindex, streak, 1)
        return totalSum
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

    private fun GenericStreakCount(steps: Int, streak: Int, relatedplayer: Int): Int {
        var matches = 0
        var newIndex = 0
        var times = 0
        for (lastindex in 0..41)
            if (boardData[lastindex] == relatedplayer)
                for (directionSteps in -3..3) {
                    newIndex = lastindex + (directionSteps * steps)
                    if (newIndex in 0..41) {
                        if (boardData[lastindex] == boardData[newIndex]) {
                            if (matches + 1 == streak) {
                                times++
                                break
                            } else
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
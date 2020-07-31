package FourWins

import java.io.File
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.HashMap

class FourWins : GameEngine {

    // Used to save all moves static
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


    private val rowSize = 6
    private val columnSize = 7

    private var boardData = (IntArray(42))
    private var timeoutThreshold: Int = 0

    private val fileName = "db"

    var currentPlayer = 1
    var prevBoard: FourWins? = null

    var lastindex = 0;
    var starTime: Long = 0

    var colorPlayerOne = "red"
    var colorPlayerTwo = "blue"

    override fun SaveDB() {
        var stringBuilder = StringBuilder()
        for (item in entries) {
            stringBuilder.append(item.key.toString() + " " + item.value[0] + " " + item.value[1] + System.lineSeparator())
        }
        File(fileName).writeBytes(stringBuilder.toString().toByteArray(Charsets.UTF_8))
    }

    override fun LoadDB() {
        var file = File(fileName)

        if (!file.exists())
            return
        var data = File(fileName).forEachLine { line ->

            val separated = line.split(' ')

            if (!entries.containsKey(separated[0].toInt())) {
                entries.put(separated[0].toInt(), intArrayOf(separated[1].toInt(), separated[2].toInt()))
            }
        }
        println("Loaded DB with " + entries.count() + " entries. ")

    }

    fun IsValidMove(move: Int): Boolean {
        return boardData[move] == 0
    }

    override fun SetStartTime(time: Long, threshold: Int) {
        starTime = time
        timeoutThreshold = threshold
    }

    private fun GetHashForMove(): Int {
        return boardData.hashCode()
    }

    override fun CalculateBestMove(depth: Int, alpha: Int, beta: Int, preventWin: Boolean): IntArray {
        assert(alpha < beta)
        var vAlpha = alpha
        var vBeta = beta
        var relatedHash = GetHashForMove()
        //Depth is 0 or time is out

        var totalTime = (System.nanoTime() - starTime)
        val elapsedTimeInSecond = totalTime.toDouble() / 1000000000
        var timeout = timeoutThreshold <= elapsedTimeInSecond
        if (depth == 0 || timeout) {
            if (FourWins.entries.containsKey(relatedHash)) {
                //println("Saved some time calculated this already total calculation" + entries.count())
                return FourWins.entries.getValue(relatedHash) as IntArray
            }

            var array = intArrayOf(Evaluate(), lastindex)
            FourWins.entries.put(relatedHash, array)
            return array

        }

        //Check for draw game
        if (GetMoveCount() == 42) {
            // println(" DRAW " + intArrayOf(0, lastindex))
            return intArrayOf(0, lastindex)
        }

        for (move in 0..5) {
            if (IsValidMove(move)) {
                var nextBoard = Move(move)
                if (nextBoard.CheckForWinCondition() == 1) {
                    var score = intArrayOf(Evaluate(), move)
                    FourWins.entries.put(relatedHash, score)
                    return score

                } else if (preventWin ) { // If the next enemy move would be a win
                    for (nextMove in 0..5) {
                        if (nextBoard.IsValidMove(nextMove)) {
                            var CheckForNext = nextBoard.Move(nextMove)
                            if (CheckForNext.CheckForWinCondition() == 1) {
                                var array = intArrayOf(Evaluate(), nextMove)
                                // println("I can prevent a win")
                                FourWins.entries.put(relatedHash, array)
                                return array
                            }
                        }
                    }
                }
            }
        }


        //Play something if we know what to do here
        if (FourWins.entries.containsKey(relatedHash)) {
            //println("Saved some time calculated this already total calculation" + entries.count())
            return FourWins.entries.getValue(relatedHash) as IntArray
        }

        var max = (lastindex - 1 - GetMoveCount()) / 2

        if (vBeta > max) {
            vBeta = max
            if (vAlpha >= vBeta) {
                return intArrayOf(vBeta, lastindex)
            }
        }
        var alphascore: IntArray = intArrayOf(0, 0)

        for (position in 0..5) {
            if (IsValidMove(position)) {
                var move = Move(position)

                var score = move.CalculateBestMove(depth - 1, -vBeta, -vAlpha)
                score[0] *= -1
                if (score[0] >= vBeta) {
                    FourWins.entries.put(relatedHash, score)
                    return score
                }
                if (score[0] > vAlpha) alphascore = score
            }
        }
        FourWins.entries.put(relatedHash, alphascore)

        return alphascore

    }

    override fun MatchWon(): Boolean {
        return CheckForWinCondition() != 0
    }


    override fun Move(pos: Int): FourWins {

        var newBoard = FourWins(this)
        newBoard.SetStartTime(starTime, timeoutThreshold)

        if (IsValidMove(pos)) {
            for (row in 5 downTo 0) {
                var index = row * columnSize + pos
                if (boardData[index] == 0) {
                    newBoard.boardData[index] = currentPlayer;
                    newBoard.lastindex = index;
                    newBoard.currentPlayer = -currentPlayer;
                    break
                }
            }
        }
        //TODO: Maybe print out that no valid move is possible

        return newBoard
    }


    // Give back the last board
    override fun Undo(): FourWins? {
        return prevBoard
    }

    override fun FlipColor() {
        var tmpColor = colorPlayerOne
        colorPlayerOne = colorPlayerTwo
        colorPlayerTwo = tmpColor
    }

    // Gives back a score
    private fun Evaluate(): Int {
        return (((lastindex + 1) - GetMoveCount()) / 2)
    }

    // Methode that get used to check for a specific stone if a streak is possible ( could open possibility to also do a five connect)
    private fun GenericStreakCheck(index: Int, steps: Int, streak: Int): Int {
        var matches = 0
        var newIndex: Int
        for (directionSteps in (-streak + 1) until streak) {
            newIndex = index + (directionSteps * steps)
            if (newIndex in 0..41) {
                if (boardData[index] == boardData[newIndex]) {
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


    private fun CheckForWinCondition(): Int {
        val rows = arrayOf<Int>(columnSize, rowSize, columnSize + 1, (1))
        var result = Arrays.stream(rows).parallel().anyMatch() { row: Int ->
            (GenericStreakCheck(lastindex, row, 4) == 1)
        }
        return if (result)
            1
        else
            0
    }

    private fun GetMoveCount(): Int {
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
                    1 -> toReturn += String.format("  <td bgcolor=\"%s\">%d</td>\n", colorPlayerTwo, index)
                    -1 -> toReturn += String.format("  <td bgcolor=\"%s\">%d</td>\n", colorPlayerOne, index)
                }
            }
            toReturn += "</tr>\n"
        }
        return toReturn
    }

}
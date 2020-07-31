package FourWins

interface GameEngine {

    //will save the current board
    fun SaveDB()
    //will load the current board
    fun LoadDB()

    fun CalculateBestMove(depth: Int, alpha: Int, beta: Int, preventWin: Boolean = true): IntArray

    fun MatchWon():Boolean
    fun SetStartTime(time: Long, threshold: Int)

    fun Move(pos: Int) : GameEngine

    fun Undo() : GameEngine?
    fun FlipColor()
}
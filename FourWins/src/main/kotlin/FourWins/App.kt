/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package FourWins

import io.javalin.Javalin
import io.javalin.http.Context


class App {

    init {
        var game = FourWins()
        val app = Javalin.create { config ->
            config.addStaticFiles("/public")
        }.start(7070)



        //game.toString()
        app.get("/move") { ctx: Context ->
            val input = ctx.queryParam("pos")!!.toInt()
            // if (game.IsValidMove(input))
            game = game.Move(input)
            ctx.result(game.toString())
        }
        app.get("/newgame") { ctx: Context ->
            game = FourWins()
            ctx.result(game.toString())
        }
        app.get("/undo") { ctx: Context ->

            var prev = game.Undo()
            if (prev != null) {
                //println("Sucessfull undo move")
                game = prev as FourWins
            } else {
                //println("Null no prev board found")
                //ctx.result("Null no prev board found")
            }
            ctx.result(game.toString())
        }
        app.get("/rows") { ctx: Context ->
            ctx.result(game.toString())
        }
        app.post("/") { ctx ->
            // some code
            game.toString()
        }
    }
    var mode = 0


}

fun main(args: Array<String>) {
    App()


}

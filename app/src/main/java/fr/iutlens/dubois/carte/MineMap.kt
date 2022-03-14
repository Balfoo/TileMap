package fr.iutlens.dubois.carte

import android.util.Log
import android.view.View
import fr.iutlens.dubois.carte.sprite.TileMap

class MineMap( override val sizeX: Int, override val sizeY: Int) : TileMap {
    var demineurGrid = Array(sizeY) { IntArray(sizeX) }
    var displayGrid = Array(sizeY) { IntArray(sizeX) }
    var tile_value = -1
    var reds = 3
    var greens = reds
    var blues = reds
    var placed_mines = -1
    var flagged_mines = 0
    var flags = Array(reds + greens + blues) { IntArray(4) }
    fun validTile(x: Int,y: Int): Boolean {
        return x >= 0 && x < sizeX && y >= 0 && y < sizeY
    }
    fun showSpace(x: Int, y: Int) {
        if (demineurGrid[y][x] != 0) return //garantis pas de mine voisine
        if (displayGrid[y][x] !=0 ) return //case pas encore visible
        displayGrid[y][x] = 1
        for (i in x-1..x+1) {
            for (j in y-1..y+1) {
                if (validTile(i,j))
                    showSpace(i,j)
            }
        }
        for (i in x-1..x+1) {
            for (j in y-1..y+1){
                if (validTile(i,j)&&displayGrid[j][i] ==0 && demineurGrid[j][i] != 0)
                    displayGrid[j][i] = demineurGrid[j][i]
            }
        }
    }
    fun addRed(color: Int) : Int {
        return when(color) {
            6 -> 8
            7 -> 9
            10 -> 11
            else -> color
        }
    }
    fun addGreen(color: Int) : Int {
        return when(color) {
            5 -> 8
            7 -> 10
            9 -> 11
            else -> color
        }
    }
    fun addBlue(color: Int) : Int {
        return when(color) {
            5 -> 9
            6 -> 10
            8 -> 11
            else -> color
        }
    }
    fun generateMines() {
        while ((reds + greens + blues > 0)) {
            val tile_x = (0..sizeX - 1).random()
            val tile_y = (0..sizeY - 1).random()
            tile_value = demineurGrid[tile_y][tile_x]
            if (tile_value == 0) {
                placed_mines++
                if (reds > 0) {
                    tile_value = 2
                    reds--
                    flags[placed_mines][0] = 2
                }
                else if (greens > 0) {
                    tile_value = 3
                    greens--
                    flags[placed_mines][0] = 3
                }
                else {
                    tile_value = 4
                    blues--
                    flags[placed_mines][0] = 4
                }
                flags[placed_mines][2] = tile_x
                flags[placed_mines][1] = tile_y
                //if (tile_value != 0) {
                    demineurGrid[tile_y][tile_x] = tile_value
                    //displayGrid[tile_y][tile_x] = tile_value
                    val neighbours = arrayOf(-1,0,1)
                    for (i in neighbours) {
                        for (j in neighbours) {
                            if (validTile(tile_x+j, tile_y+i)) {
                                if (i != 0 || j != 0) {
                                    var neighbour_color = 0
                                    when (demineurGrid[tile_y][tile_x]) {
                                        5 -> neighbour_color = addRed(demineurGrid[tile_y + i][tile_x + j])
                                        6 -> neighbour_color = addGreen(demineurGrid[tile_y + i][tile_x + j])
                                        7 -> neighbour_color = addBlue(demineurGrid[tile_y + i][tile_x + j])
                                        else -> neighbour_color = tile_value + 3
                                    }
                                    demineurGrid[tile_y + i][tile_x + j] = neighbour_color
                                }
                            }
                        }
                    }
                //}
            }
        }
    }
    init {
        generateMines()
    }
    override fun get(x: Int, y: Int): Int {
        return displayGrid[y][x]
    }
    fun show(x: Float, y: Float): Int {
        var tile_value = demineurGrid[y.toInt()][x.toInt()]
        var loseMod = 0
        if (tile_value == 0) {
            tile_value = 1
            showSpace(x.toInt(),y.toInt())
        }
        if (tile_value == 2 || tile_value == 3 || tile_value == 4){
            loseMod = 1
            placed_mines--
        }
        displayGrid[y.toInt()][x.toInt()] = tile_value
        return loseMod
    }
    fun flag(x: Float, y: Float): Int {
        var display_value = displayGrid[y.toInt()][x.toInt()]
        when (display_value) {
            0 -> display_value = 17
            17, 18 -> display_value++
            19 -> display_value = 0
        }
        for (flag in flags) {
            if (x.toInt() == flag[2] && y.toInt() == flag[1]) {
                if (flag[0] == flag[3]) {
                    flagged_mines--
                }
                flag[3] = display_value
                if (flag[0].toInt() == flag[3].toInt()-15) {
                    if (flagged_mines == placed_mines) { //placed_mines starts counting at 0
                        return 1
                    }
                    flagged_mines++
                }
            }
        }
        displayGrid[y.toInt()][x.toInt()] = display_value
        if (demineurGrid[y.toInt()][x.toInt()] == display_value-15) {
            Log.d("Frick", flagged_mines.toString())
        }
        return 0
    }
}

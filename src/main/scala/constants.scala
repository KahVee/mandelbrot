package object main {

    val WindowHeight = 400
    val WindowWidth = 400

    val MinZoom = 0.005
    val MaxZoom = 30 //floating point inaccuracy gets in the way with bigger numbers

    //Iteration cuts when z^2 > EscapeRadius. By default this is 4, but higher values make nicer looking coloring
    val EscapeRadius = 100
}
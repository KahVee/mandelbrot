package main

import main._

object Mandelbrot {
    def iteratePoint(cr: Double, ci: Double) = {
        var r = z0r
        var i = z0i

        var nr = r
        var ni = i

        var n = 0
        while( n < maxIterations && r*r+i*i < 4) {
            nr = r*r - i*i + cr
            ni = 2*r*i + ci

            r = nr
            i = ni
            n += 1
        }

        (/*r*r + i*i < 4, */n)
    }
}
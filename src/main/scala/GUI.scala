package main
import main._

import scala.concurrent._
import scala.math._
import ExecutionContext.Implicits.global

import scalafx.application.{JFXApp, Platform}
import scalafx.Includes._
import scalafx.scene.Scene
import scalafx.scene.layout.{VBox,HBox}
import scalafx.scene.image.{WritableImage, ImageView}
import scalafx.scene.paint.Color

import javafx.util.converter.FormatStringConverter
import java.text.NumberFormat
import scalafx.scene.control.Button
import javafx.scene.image.{PixelFormat, PixelWriter}


object GUI extends JFXApp {
  var zoomLevel = 1.0
  //Center of the screen on the complex plane 
  var R = -0.75
  var I = 0.0
  //Used for measuring performance when iterating and coloring the scene
  private var startTime = System.nanoTime()
  //Runoff value, x-coord of the pixel, y-coord of the pixel in screen space
  private var pixelNList = Array.fill[(Int, Int, Int)](WindowHeight*WindowWidth)((0, 0, 0)).par
  //Color of each pixel stored in the bits of Int, ARGB
  private var pixelColors = Array[Int]()

  private val wimg = new WritableImage(WindowWidth, WindowHeight)
  private val pixelWriter = wimg.getPixelWriter()

  stage = new JFXApp.PrimaryStage {
    title.value = "Mandelbrot Explorer"
    resizable = false
    onCloseRequest = e => scalafx.application.Platform.exit()
    scene = new Scene(WindowWidth, WindowHeight) {
      content = new VBox(
        new ImageView(wimg)
      )
    }
  }

  stage.sizeToScene()
  ControlPanel.stage.show()
  println("Initiation took " + (System.nanoTime()-startTime)/1000000000.0)

  //Generates the runoff values with the current zoom level and chosen imaginary coordinates as center point
  def generate() = {
    startTime = System.nanoTime()
    println("Generating scene...")
    val zoom = new Zoom(zoomLevel, R, I)
    println(zoom.maxIterations + " max iterations")
    //Calculates the runoff values of each pixel into pixelNlist
    (0 until WindowWidth).par.foreach(i => 
    (0 until WindowHeight).par.foreach(j => {
      val n = Mandelbrot.iteratePoint(
        zoom.scaleToMandelbrotSpaceR(i),
        zoom.scaleToMandelbrotSpaceI(j),
        zoom.maxIterations
      )
      pixelNList(j*WindowWidth+i) = (n,i,j)
    }))

    pixelColors = loadPixelColors(zoom.maxIterations)
    writePixels()
    
    //Non-parallel version
    /*
    for (i <- 0 until WindowWidth) {
      for (j <- 0 until WindowHeight) {
        val point = Mandelbrot.iteratePoint(
          scaleToMandelbrotSpaceR(i),
          scaleToMandelbrotSpaceI(j)
        )
        pixelNList(i*WindowWidth+j) = point
      }
    }*/

    println("took " + (System.nanoTime()-startTime)/1000000000.0)
  }
  
  def updateColors() = {
    startTime = System.nanoTime()
    println("Updating scene colors...")
    val zoom = new Zoom(zoomLevel, R, I)
    pixelColors = loadPixelColors(zoom.maxIterations)
    writePixels()
    println("took " + (System.nanoTime()-startTime)/1000000000.0)
  }

  private def writePixels() = pixelWriter.setPixels(0, 0, WindowWidth-1, WindowHeight-1, PixelFormat.getIntArgbInstance(), pixelColors, 0, WindowWidth)

  //Writes a color for each pixel using the "runaway" value (n) and a constant from user sliders.
  //Non-repeating color patterns are achieved via cosine function and the user-set sliders for different color channels.
  private def loadPixelColors(maxN: Int): Array[Int] = {
    val arr = Array.ofDim[Int](WindowHeight*WindowWidth)
    (0 until arr.length).par.foreach(i => {
      val n = pixelNList(i)._1
      arr(i) = if (n< maxN) { 0xff000000  |
        ((1 + cos(cr.value/100.0 * n)) * 127).asInstanceOf[Int] << 16 |
        ((1 + cos(cg.value/100.0 * n)) * 127).asInstanceOf[Int] << 8 |
        ((1 + cos(cb.value/100.0 * n)) * 127).asInstanceOf[Int] 
      } else 0xff000000 //background color
    })
    arr
  }
}

sealed abstract class ColorChanger(var value: Double)
case object cr extends ColorChanger(5)
case object cg extends ColorChanger(5)
case object cb extends ColorChanger(5)
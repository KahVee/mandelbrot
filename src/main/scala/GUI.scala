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
import javafx.scene.image.PixelFormat
import javafx.scene.image.PixelWriter


object GUI extends JFXApp {
  var zoomLevel = 1d
  var startTime = System.nanoTime()
  //(n, x-coord of the pixel, y-coord of the pixel)
  var pixelNList = Array.fill[(Int, Int, Int)](windowHeight*windowWidth)((0, 0, 0)).par
  var pixelColors = Array[Int]()
  val wimg = new WritableImage(windowWidth, windowHeight)
  val pixelWriter = wimg.getPixelWriter()

  stage = new JFXApp.PrimaryStage {
    width = windowWidth
    height = windowHeight
    onCloseRequest = e => scalafx.application.Platform.exit()
    scene = new Scene(windowWidth, windowHeight) {
      content = new VBox(
        new ImageView(wimg)
      )
    }
  }
  ControlPanel.stage.show()
  println("Initiation took " + (System.nanoTime()-startTime)/1000000000.0)

  def generate() = {
  startTime = System.nanoTime()
  println("Generating scene...")
  val zoom = new Zoom(zoomLevel)
  println(zoom.maxIterations)
  //Calculates the runoff values of each pixel into pixelNlist
  (0 until windowWidth).par.foreach(i => 
  (0 until windowHeight).par.foreach(j => {
    val n = Mandelbrot.iteratePoint(
      zoom.scaleToMandelbrotSpaceR(i),
      zoom.scaleToMandelbrotSpaceI(j),
      zoom.maxIterations
    )
    pixelNList(j*windowWidth+i) = (n,i,j)
  }))

  pixelColors = loadPixelColors(zoom.maxIterations)
  pixelWriter.setPixels(0,0,windowWidth-1,windowHeight-1,PixelFormat.getIntArgbInstance(),pixelColors,windowHeight,windowWidth)
  
  //Non-parallel version
  /*
  for (i <- 0 until windowWidth) {
    for (j <- 0 until windowHeight) {
      val point = Mandelbrot.iteratePoint(
        scaleToMandelbrotSpaceR(i),
        scaleToMandelbrotSpaceI(j)
      )
      pixelNList(i*windowWidth+j) = point
    }
  }*/

  println("took " + (System.nanoTime()-startTime)/1000000000.0)
  }

  def updateColors() = {
    startTime = System.nanoTime()
    println("Updating scene")
    val zoom = new Zoom(zoomLevel)
    loadPixelColors(zoom.maxIterations)
    println("took " + (System.nanoTime()-startTime)/1000000000.0)
  }
  
  //Writes a color for each pixel using the "runaway" value (n) and a constant from user sliders.
  //Non-repeating color patterns are achieved via cosine function and the user-set sliders for different color channels.
  def loadPixelColors(maxN: Int): Array[Int] = {
    val arr = Array.ofDim[Int](windowHeight*windowWidth)
    (0 until arr.length).par.foreach(i => {
      val n = pixelNList(i)._1
      arr(i) = if (n< maxN) { 0xff000000  |
        ((1 + cos(cr.value * n)) * 127).asInstanceOf[Int] << 16 |
        ((1 + cos(cg.value * n)) * 127).asInstanceOf[Int] << 8 |
        ((1 + cos(cb.value * n)) * 127).asInstanceOf[Int] 
      } else 0xff000000 //background color
    })
    arr
  }

  //ScalaFX doesn't like when multiple threads try to access its methods
  /*
  def updateScene(): Unit = {    

    (0 until windowWidth).par.foreach(i =>
    (0 until windowHeight).par.foreach{j => 
      pixelWriter.setColor(i, j, {
        val n = pixelNList(i*windowWidth + j)
        if (n >= maxIterations)
          backgroundColor
        else {
          Color(
            (1 + cos(cr.value * n)) / 2,
            (1 + cos(cg.value * n)) / 2,
            (1 + cos(cb.value * n)) / 2,
            1.0
          )
        }
      })
      if(Thread.interrupted()) return
    })*/
  }

sealed abstract class ColorChanger(var value: Double)
case object cr extends ColorChanger(0.05)
case object cg extends ColorChanger(0.05)
case object cb extends ColorChanger(0.05)
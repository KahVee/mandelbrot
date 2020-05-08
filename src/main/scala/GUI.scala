package main

import scala.concurrent._
import ExecutionContext.Implicits.global

import scalafx.application.{JFXApp, Platform}
import scalafx.scene._
import scalafx.Includes._
import main._
import scalafx.scene.image._
import scalafx.scene.paint._
import scala.math._
import scalafx.scene.layout.VBox
import scalafx.scene.control.Slider
import scalafx.scene.layout.HBox
import scalafx.beans.property.StringProperty
import scalafx.beans.property.DoubleProperty
import scalafx.scene.control.TextFormatter
import javafx.util.converter.FormatStringConverter
import java.text.NumberFormat
import scalafx.scene.control.Label
import scalafx.scene.control.TextField
import scala.collection.mutable


object GUI extends JFXApp {
  var time = System.nanoTime()
  //(n, x, y)
  var pixelNList = Array.fill[(Int, Int, Int)](windowHeight*windowWidth)((0, 0, 0)).par
  var blackList = mutable.Buffer[(Int, Int)]().par
  val wimg = new WritableImage(windowWidth, windowHeight)
  val pixelWriter = wimg.getPixelWriter()

  //Calculates the runoff values of each pixel into pixelNlist and black pixels to their own list
  (0 until windowWidth).par.foreach(i => 
  (0 until windowHeight).par.foreach(j => {
    val n = Mandelbrot.iteratePoint(scaleToMandelbrotSpaceR(i),scaleToMandelbrotSpaceI(j))
    pixelNList(i*windowWidth+j) = (n,i,j)
  }))

  if(setColored)
    blackList.foreach(v => pixelWriter.setColor(v._1, v._2, backgroundColor))

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


  println((System.nanoTime()-time)/1000000000.0)

  stage = new JFXApp.PrimaryStage {
    width = windowWidth
    height = windowHeight
    onCloseRequest = e => scalafx.application.Platform.exit()
    scene = new Scene(windowWidth, windowHeight) {
      content = new VBox(new HBox(sliderConstructor(cr), sliderConstructor(cg), sliderConstructor(cb)), new ImageView(wimg))
    }
  }

  updateScene()
  def updateScene(): Unit = {
    time = System.nanoTime()
    println("Updating scene")
    pixelNList.foreach{v => pixelWriter.setColor(v._2, v._3, {
          Color(
            (1 + cos(cr.value * v._1)) / 2,
            (1 + cos(cg.value * v._1)) / 2,
            (1 + cos(cb.value * v._1)) / 2,
            1.0
          )
        })
      }
    println("took " + (System.nanoTime()-time)/1000000000.0)
      

        /*
    (0 until windowWidth).par.foreach(i =>
    (0 until windowHeight).par.foreach{j => 
      pixelWriter.setColor(i, j, {
        val n = pixelNList(i*windowWidth + j)
        if (n >= maxIterations)
          Color(0.0, 0.0, 0.0, 1.0)
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

  /*
  for (i <- 0 until windowHeight;
       j <- 0 until windowWidth) {
    val n = pixelNList(i*windowWidth + j)
      pixelWriter.setColor(
        i,
        j,
        if (n >= maxIterations)
          Color(0.0, 0.0, 0.0, 1.0)
        else
          Color(
            (1 + cos(0.05 * n)) / 2,
            (1 + cos(0.04 * n)) / 2,
            (1 + cos(0.03 * n)) / 2,
            1.0
          )
      )
  }
  */
  }

  def scaleToMandelbrotSpaceR(r: Int) = {
    (rMax - rMin) * (r.toDouble / windowWidth.toDouble) + rMin
  }

  def scaleToMandelbrotSpaceI(i: Int) = {
    (iMax - iMin) * (i.toDouble / windowHeight.toDouble) + iMin
  }

  def sliderConstructor(c: ColorChanger): HBox = {
    def changeColor(c: ColorChanger, v: Double) = {
      c.value = v
      updateScene()
    }
    //from 0.001 to 1, default to 0.005
    val s = new Slider(0.001, 1, 0.005){
      onMouseClicked = e => changeColor(c, value.toDouble)
      //onMouseDragged = e => changeColor(c, value.toDouble) //laggy af
    }
    val l = new TextField {
      //binds value of slider to its label
      val nf = NumberFormat.getInstance()
      nf.setMaximumFractionDigits(3)
      nf.setMinimumFractionDigits(3)
      textFormatter = new TextFormatter(new FormatStringConverter[Number](nf)) {
        value <==> s.value
      }
      
      onAction = e => changeColor(c, s.value.toDouble)
    }
    new HBox(s,l)
  }  
}

//Not used atm
class SceneUpdater extends Runnable {
  def run(): Unit = {
    try {
      GUI.updateScene()
    } catch {
        // We've been interrupted: no more messages.        
        case e: InterruptedException => println("Interrupted")
    }
  }
}

sealed abstract class ColorChanger(var value: Double)
case object cr extends ColorChanger(0.05)
case object cg extends ColorChanger(0.05)
case object cb extends ColorChanger(0.05)
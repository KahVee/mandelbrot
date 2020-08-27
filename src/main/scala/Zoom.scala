package main

import main._

class Zoom(zoomLevel: Double, centerR: Double, centerI: Double) {

  val maxIterations = math.floor(zoomLevel*zoomLevel*zoomLevel+100).asInstanceOf[Int]

  //This function gives a nice "linear-feeling" zoom
  private val constantR = 1.25/(math.exp(GUI.zoomLevel))
  private val constantI = (WindowHeight.toDouble/WindowWidth.toDouble)*constantR

  private val localRMin = centerR - constantR
  private val localRMax = centerR + constantR

  //screen space y is "reversed", it increases downwards
  private val localIMin = centerI + constantI
  private val localIMax = centerI - constantI

  //These scale the screen space coordinates to complex plane coordinates 
  def scaleToMandelbrotSpaceR(pixelR: Int) = {
    (localRMax - localRMin) * (pixelR.toDouble / WindowWidth.toDouble) + localRMin
  }
  def scaleToMandelbrotSpaceI(pixelI: Int) = {
    (localIMax - localIMin) * (pixelI.toDouble / WindowHeight.toDouble) + localIMin
  }
}
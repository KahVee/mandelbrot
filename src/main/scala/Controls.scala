package main

import main._
import scalafx.Includes._
import scalafx.stage.Stage
import scalafx.scene.Scene
import scalafx.scene.layout.{VBox,HBox, Region}
import scalafx.scene.control.{Slider, TextField, TextFormatter, Button}
import scalafx.scene.text.{Text, Font}
import scalafx.beans.property.{StringProperty, DoubleProperty}

import javafx.util.converter.FormatStringConverter
import java.text.NumberFormat
import java.util.ResourceBundle.Control
import scalafx.geometry.Pos
import scalafx.geometry.Orientation
import scalafx.geometry.Insets
import javafx.event.EventHandler
import javafx.event.ActionEvent
import javafx.beans.value.ChangeListener
import java.beans.PropertyChangeEvent
import scalafx.scene.control.CheckBox

object ControlPanel {
  private var continuousUpdating = false

	val stage = new Stage {
    title.value = "Control Panel"
    x = (GUI.stage.x.value + GUI.stage.width.value)
    resizable = false
    onCloseRequest = e => scalafx.application.Platform.exit()
		scene = new Scene {
			content = new HBox (
        zoomSliderConstructor(200, 60),
        hReg,
        //These sliders control the multiplier of each channel of the coloring function
				new VBox (
					sliderConstructor("R", changeColorFunction(cr), 1, 50, 5, 5, 100, textPadding=true),
					sliderConstructor("G", changeColorFunction(cg), 1, 50, 5, 5, 100, textPadding=true),
          sliderConstructor("B", changeColorFunction(cb), 1, 50, 5, 5, 100, textPadding=true),
          coordinates,
          vReg,
					new HBox(
						new Button("Generate") {
							onAction = e => GUI.generate()
            },
            hReg,
            new CheckBox("Continuous") {
              padding = Insets(5)
              onAction = e => continuousUpdating = selected.value
            }
					)
				)
			) {padding = Insets(5)}          
		}
  }

  private def numberBox(boxWidth: Double, defaultValue: Double, decimals: Int, connectedSlider: Option[Slider] = None) = {
    new TextField {      
      prefWidth = boxWidth
      //Format is US to get decimal points for converting from String to Double
      val numberFormat = NumberFormat.getInstance(java.util.Locale.US)
      numberFormat.setMaximumFractionDigits(decimals)
      numberFormat.setMinimumFractionDigits(decimals)
      textFormatter = new TextFormatter(new FormatStringConverter[Number](numberFormat)) {
        if(connectedSlider.nonEmpty) {
          value <==> connectedSlider.get.value
        } else
          value = defaultValue
      }
    }
  }
  
  private def coordinates = {
    def changeR: (Double => Unit) = {
      v => GUI.R = v
      if(continuousUpdating) GUI.generate()
    }
    def changeI: (Double => Unit) = {
      v => GUI.I = v
      if(continuousUpdating) GUI.generate()
    }
    new VBox(sliderConstructor("Real", changeR, -2, 0.5, -0.75, 16, 150, true), sliderConstructor("Imag", changeI, -1.2, 1.2, 0, 16, 150, true))
  }
		
	private def changeColorFunction(c: ColorChanger): Double => Unit = {
    v => c.value = v
    if(continuousUpdating) GUI.updateColors()
	}

	private def sliderConstructor(label: String, function: Double => Unit, min: Double, max: Double, default: Double, decimals: Int, sliderWidth: Int, sliderBelowTextField: Boolean = false, textPadding: Boolean = false) = {

		val slider = new Slider(min, max, default){
			prefHeight = 30
			prefWidth = sliderWidth
			onMouseClicked = e => function(value.toDouble)
			onMouseDragged = e => function(value.toDouble)
		}

    val textField = numberBox(70, default, decimals, Some(slider))	
    textField.onKeyTyped = e => function(textField.text.value.toDouble)

		val text = new VBox(
				new Text { 
				text = label
				font = Font.font(20)
			}
		) {
      if(textPadding) prefWidth = label.length*20
			alignment = Pos.CENTER_LEFT
		}

    if(sliderBelowTextField)
      new VBox(new HBox(text, slider), textField)
    else
		  new HBox(text, slider, textField)
		}  

	private def zoomSliderConstructor(boxHeight: Int, textWidth: Int) = {

		def changeZoomLevel(v: Double) = {
      GUI.zoomLevel = v
      if(continuousUpdating) GUI.generate()
		}
		
		val slider = new Slider(MinZoom, MaxZoom, 1) {
      orientation = Orientation.VERTICAL
			prefHeight = boxHeight
			onMouseDragged = e => changeZoomLevel(value.toDouble)
			onMouseClicked = e => changeZoomLevel(value.toDouble)
    }
	
	  val textField = numberBox(textWidth, 1, 3, Some(slider))
		textField.onKeyTyped = e => changeZoomLevel(textField.text.value.toDouble)

    val plus = new Button("+") {
      prefWidth = textWidth
      prefHeight = (boxHeight - textField.height.toDouble) / 2
      onAction = e => {
        val newZoom = math.min(GUI.zoomLevel+1, MaxZoom)
        changeZoomLevel(newZoom)
        slider.value = newZoom
      }
    }

    val minus = new Button("-") {
      prefWidth = textWidth
      prefHeight = (boxHeight - textField.height.toDouble) / 2
      onAction = e => {
        val newZoom = math.max(GUI.zoomLevel-1, MinZoom)
        changeZoomLevel(newZoom)
        slider.value = newZoom
      }
    }
    
    val controls = new VBox (
      plus,
      vReg,
      textField,
      vReg,
      minus
    )

		new HBox(slider, hReg, controls)
  }
  
  //Spacers between elements
  private def vReg = new Region {prefHeight = 5}
  private def hReg = new Region {prefWidth = 5}
}
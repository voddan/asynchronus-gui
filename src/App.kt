import javafx.application.*
import javafx.beans.binding.*
import javafx.beans.property.*
import javafx.collections.*
import javafx.concurrent.*
import tornadofx.*
import java.util.concurrent.*

/**
 * Created by voddan on 06/11/16.
 */

fun main(args: Array<String>) {
    Application.launch(MApp::class.java)
}


class MApp : App(MView::class)


class MControler : Controller() {
    val list1 = FXCollections.observableList((1..150).toMutableList())
    val list2 = FXCollections.observableList((151..300).toMutableList())

    val direction1to2 = SimpleBooleanProperty(true)

    val from = Bindings.createObjectBinding(Callable {if(direction1to2.value) list1 else list2} , direction1to2)
    val to = Bindings.createObjectBinding(Callable {if(direction1to2.value) list2 else list1} , direction1to2)

    fun move() {
        println("move: ${Thread.currentThread().id}")

        Thread.sleep(10)
        Platform.runLater {
            if(from.value.isNotEmpty())
                to.value.add(0, from.value.removeAt(0))
        }
    }

    fun moveAll() {

        println("moveAll: ${Thread.currentThread().id}")

        repeat(from.value.size) {
            move()
        }

    }

    init {
        println("MControler: ${Thread.currentThread().id}")
    }

    val service = object : Service<Unit>() {
        override fun createTask(): Task<Unit> = object : Task<Unit>() {
            override fun call() {
                moveAll()
            }
        }
    }
}


class MView : View() {
    val controller: MControler by inject()


    override val root = hbox {
        listview(controller.list1) {  }
        listview(controller.list2) {  }

        vbox {

            togglegroup {
                val default = togglebutton("1 -> 2") { }
                togglebutton("1 <- 2") { }

                selectToggle(default)
                val is1to2 = selectedToggleProperty().booleanBinding {t -> t === default}

                controller.direction1to2.bind(is1to2)
            }
            button("Move").setOnAction {
                Thread({
                    controller.move()
                }).start()
            }
            button("MoveAll").setOnAction {
                controller.service.restart()
            }
            button("Stop").setOnAction {
                controller.service.cancel()
            }
        }
    }

    init {
        println("MView: ${Thread.currentThread().id}")
    }
}
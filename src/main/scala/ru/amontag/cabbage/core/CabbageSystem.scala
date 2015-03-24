package ru.amontag.cabbage.core

import akka.actor.ActorSystem
import ru.amontag.cabbage.core.description.{ApiDescription, ApiDescriptor}

import scala.collection.mutable

/**
 * Created by montag on 23.03.15.
 */
abstract class CabbageSystem {
    val modules: List[ModuleOperations]
    implicit val actorContext = ActorSystem("ru-amontag-cabbage")
    val api: mutable.Map[Class[_], ApiDescriptor] = mutable.Map()

    import ru.amontag.cabbage.core.ModuleOperations._

    def init(): Unit = {
        modules
          .map(module => {module.context.set(actorContext); module})
          .map(module => module -> ModuleContainer.create(module)).foreach(t => {
            val (module, props) = t
            api += moduleOperations2Module(module).getClass -> ApiDescriptor(actorContext.actorOf(props, module.name), ApiDescription(module))
        })
    }
}
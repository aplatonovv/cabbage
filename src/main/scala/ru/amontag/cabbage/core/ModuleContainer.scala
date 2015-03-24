package ru.amontag.cabbage.core

import akka.actor.{Props, Actor}

/**
 * Created by montag on 23.03.15.
 */
case class ModuleContainer(module: Module) extends Actor {
    @throws[Exception](classOf[Exception])
    override def preStart(): Unit = module.preStart()

    override def receive: Receive = {
        case InitMsg => ()
        case ShutdownMsg => module.shutdown()
        case Call(method, params) => sender ! Result(module.call(method, params))
    }
}

object ModuleContainer {
    def create(module: Module): Props = Props.create(classOf[ModuleContainer], module)
}

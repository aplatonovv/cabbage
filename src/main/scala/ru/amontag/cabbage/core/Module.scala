package ru.amontag.cabbage.core

import java.util.concurrent.atomic.AtomicReference

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, duration}

/**
 * Created by montag on 23.03.15.
 */
object ModuleOperations {
    implicit def module2ModuleOperations(module: Module): ModuleOperations = ModuleOperations(module)

    implicit def moduleOperations2Module(module: ModuleOperations): Module = module.module
}

case class ModuleOperations(module: Module) {
    def +(other: ModuleOperations): List[ModuleOperations] = module :: other :: Nil

    def +(others: List[ModuleOperations]): List[ModuleOperations] = module :: others
}

abstract class Module {
    var context: AtomicReference[ActorSystem] = new AtomicReference[ActorSystem]()
    val name: String

    val methods = getClass.getMethods
      .filter(_.getAnnotation(classOf[annotation.Handler]) != null)
      .map(method => {
        val methodName = method.getName
        val parameters = method.getParameters.map(Parameter.apply).map(p => p.name -> p).toMap
        methodName -> Method(methodName, parameters)(method)
    }).toMap

    private implicit val awaitingTime = Duration(10, duration.MINUTES)
    private implicit val timeout: Timeout = Timeout(awaitingTime)


    protected def init(): Unit = ()

    def shutdown(): Unit = ()

    def preStart(): Unit = init()

    def call(name: String, constants: Map[String, AnyRef]): AnyRef = {
        methods.get(name) match {
            case Some(method) =>
                val parameters = method.parameters.values.map(param => evaluateParameter(param, method, constants)).toList
                method.ref.invoke(this, parameters: _*)
            case None => throw new NoSuchMethodException("Cannot find method " + name + " in module " + this.name)
        }
    }

    private def evaluateParameter(description: Parameter, method: Method, constants: Map[String, AnyRef]): AnyRef = {
        description.isQuery match {
            case false =>
                constants.get(description.name) match {
                    case Some(value) => value
                    case None => throw new IllegalArgumentException(
                        String.format("Cannot find value for parameter %s in method %s in module %s",
                            description.name, method.name, this.name)
                    )
                }

            //TODO: Добавить препроцессинг параметров запроса и построцессинг
            case true =>
                if (description.target.get == this.name) {
                    call(description.method.get, constants)
                } else {
                    if (context.get != null)
                        Await.result[Result]((context.get.actorSelection("/user/" + description.target.get) ask Call(description.method.get, constants)).mapTo[Result],
                            Duration(10, duration.MINUTES)).value
                    else {
                        throw new IllegalStateException("Module was not added to Cabbage System")
                    }
                }
        }
    }
}

package ru.amontag.cabbage.core

import akka.actor._
import akka.pattern.ask
import akka.util.Timeout

import scala.collection.mutable
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, duration}

/**
 * Created by montag on 23.03.15.
 */
object Module {
    val systemPath = "ru.amontag.cabbage"

    //TODO: добавление мапки со списком модулей
    def create(modules: List[Class[Module]]): (ActorSystem, Map[String, ActorRef]) = {
        val as = ActorSystem(systemPath)
        modules.map(clazz => Props(clazz))
        as -> Map()
    }
}

abstract class Module extends Actor {
    protected val name: String

    private val methods = mutable.Map[String, Method]()
    private implicit val awaitingTime = Duration(10, duration.MINUTES)
    private implicit val timeout: Timeout = Timeout(awaitingTime)

    protected def init(): Unit = ()

    protected def shutdown(): Unit = ()

    @throws[Exception](classOf[Exception])
    override def preStart(): Unit = {
        getClass.getMethods
          .filter(_.getAnnotation(classOf[annotation.Handler]) != null)
          .foreach(method => {
            val methodName = method.getName
            val parameters = method.getParameters.map(Parameter.apply).map(p => p.name -> p).toMap
            methods.put(methodName, Method(methodName, parameters)(method))
        })
    }

    override def receive: Receive = {
        case InitMsg => init()
        case ShutdownMsg => shutdown()
        case Call(method, params) => sender ! Result(call(method, params))
    }

    private def call(name: String, constants: Map[String, AnyRef]): AnyRef = {
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
            case true => {
                if (description.target.get == this.name) {
                    call(description.method.get, constants)
                } else {
                    Await.result[Result]((context.actorSelection("/user/" + description.target.get) ask Call(description.method.get, constants)).mapTo[Result],
                        Duration(10, duration.MINUTES)).value
                }
            }
        }
    }
}

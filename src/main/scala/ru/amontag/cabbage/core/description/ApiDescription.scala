package ru.amontag.cabbage.core.description

import akka.actor.ActorRef
import ru.amontag.cabbage.core.Module

/**
 * Created by montag on 24.03.15.
 */
abstract class ApiDescription(module: Module) {
    val name: String
    val get: String
}

object ApiDescription {
    def apply(module: Module): Map[String, ApiDescription] = {
        val description: JsonDescription = new JsonDescription(module)
        Map(description.name -> description)
    }
}

case class ApiDescriptor(ref: ActorRef, descriptions: Map[String, ApiDescription])
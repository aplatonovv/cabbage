package ru.amontag.cabbage.core.description

import ru.amontag.cabbage.core.Module

/**
 * Created by montag on 24.03.15.
 */
class JsonDescription(module: Module) extends ApiDescription(module) {
    override val name: String = "json"

    override val get: String = "{\n\tmethods: [" + module.methods.map({
        case (name, description) => "\n\t\t{name: '" + name + "', parameters: [" +
          description.parameters.map({
              case (paramName, param) => "\n\t\t\t{name: '" + paramName + "', type: '" + param.ofType + "', description: '" + param.description + "'}"
          }).mkString(",") + "]}"
    }).mkString(",") + "]}"
}

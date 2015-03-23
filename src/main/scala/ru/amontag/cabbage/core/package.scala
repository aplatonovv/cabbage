package ru.amontag.cabbage

import ru.amontag.cabbage.core.annotation.Query

/**
 * Created by montag on 23.03.15.
 */
package object core {

    case class InitMsg()

    case class ShutdownMsg()

    case class Call(methodName: String, parameters: Map[String, AnyRef])

    case class Result(value: AnyRef)

    case class Method(name: String, parameters: Map[String, Parameter])(val ref: java.lang.reflect.Method)

    case class Parameter(name: String,
                         isQuery: Boolean,
                         value: Option[AnyRef] = None,
                         target: Option[String] = None,
                         method: Option[String] = None,
                         preprocessScript: Option[String] = None,
                         postprocessScript: Option[String] = None)

    object Parameter {
        def apply(defenition: java.lang.reflect.Parameter): Parameter = {
            Option(defenition.getAnnotation(classOf[Query])) match {
                case None => Parameter(defenition.getAnnotation(classOf[annotation.Parameter]).value(), false)
                case Some(query) =>
                    val preprocessStep = query.preparationScript() match {
                        case "" => None
                        case value => Some(value)
                    }
                    val postprocessStep = query.postprocessingScript() match {
                        case "" => None
                        case value => Some(value)
                    }
                    Parameter(name = query.value(),
                        isQuery = true,
                        target = Option(query.target()),
                        method = Option(query.method()),
                        preprocessScript = preprocessStep,
                        postprocessScript = postprocessStep)
            }
        }
    }

}

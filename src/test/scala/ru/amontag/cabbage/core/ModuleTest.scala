package ru.amontag.cabbage.core

import akka.actor.{Props, ActorSystem}
import akka.util.Timeout
import org.scalatest.FunSuite
import ru.amontag.cabbage.core.annotation.{Create, Query, AccessType, Handler}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, duration}
import akka.pattern.ask

/**
 * Created by montag on 23.03.15.
 */
class TestModule extends Module {
    override protected val name: String = "one"

    @Handler(name = "sum", accessType = AccessType.Public)
    def sum(@Query(value = "a", target = "two", method = "one") a: Int,
            @Query(value = "b", target = "two", method = "two") b: Int): Int = {
        a + b
    }
}

class TestModuleTwo extends Module {
    override protected val name: String = "two"

    @Handler(name = "one", accessType = AccessType.Public)
    def one(): Int = 1

    @Handler(name = "two", accessType = AccessType.Public)
    def two(): Int = 2
}

class ModuleTest extends FunSuite {

    test("simple summator") {
        implicit val timeout = Timeout(Duration(10, duration.MINUTES))
        val actors = ActorSystem("cabbage")
        val actor = actors.actorOf(Props(classOf[TestModule]), "test")
        val actor2 = actors.actorOf(Props(classOf[TestModuleTwo]), "two")
        val r = Await.result[Result]((actor ask Call("sum", Map())).mapTo[Result], Duration(10, duration.MINUTES))
        println(r)
    }
}
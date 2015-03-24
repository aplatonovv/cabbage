package ru.amontag.cabbage.core

import akka.util.Timeout
import org.scalatest.FunSuite
import ru.amontag.cabbage.core.annotation.{AccessType, Handler, Query}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, duration}

/**
 * Created by montag on 23.03.15.
 */
class TestModule extends Module {
    override val name: String = "one"

    @Handler(name = "sum", accessType = AccessType.Public)
    def sum(@Query(value = "a", target = "two", method = "one") a: Int,
            @Query(value = "b", target = "two", method = "two") b: Int): Int = {
        a + b
    }
}

class TestModuleTwo extends Module {
    override val name: String = "two"

    @Handler(name = "one", accessType = AccessType.Public)
    def one(): Int = 1

    @Handler(name = "two", accessType = AccessType.Public)
    def two(): Int = 2
}

object TestConfig extends CabbageSystem {

    import ru.amontag.cabbage.core.ModuleOperations._

    override val modules: List[ModuleOperations] = new TestModule() + new TestModuleTwo()
}

class ModuleTest extends FunSuite {
    test("simple summator") {
        import akka.pattern.ask
        implicit val timeout = Timeout(Duration.apply(10, duration.MINUTES))
        TestConfig.init()
        val r = Await.result[Result]((TestConfig.api(classOf[TestModule]).ref ask Call("sum", Map())).mapTo[Result],
            Duration.apply(10, duration.MINUTES))
        println(r)
        Thread.sleep(100000)
    }
}
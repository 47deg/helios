package helios.core

import arrow.test.UnitSpec
import arrow.test.laws.MonoidLaws
import arrow.test.laws.SemigroupLaws
import helios.instances.jsstring.eq.eq
import helios.instances.jsstring.monoid.monoid
import helios.instances.jsstring.semigroup.semigroup
import io.kotlintest.properties.Gen

class MonoidTest : UnitSpec() {

    init {
        testLaws(
            SemigroupLaws.laws(
                JsString.semigroup(),
                JsString("a"),
                JsString("b"),
                JsString("c"),
                JsString.eq()
            )
        )

        testLaws(
            MonoidLaws.laws(
                JsString.monoid(),
                Gen.string().map { JsString(it) },
                JsString.eq()
            )
        )
    }
}

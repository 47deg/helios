package helios.optics

import arrow.typeclasses.Eq
import helios.arrow.UnitSpec
import helios.arrow.generators.functionAToB
import helios.arrow.laws.OptionalLaws
import helios.arrow.laws.TraversalLaws
import helios.core.JsArray
import helios.core.JsObject
import helios.optics.jsarray.each.each
import helios.optics.jsarray.index.index
import helios.optics.jsobject.each.each
import helios.optics.jsobject.index.index
import helios.test.generators.alphaStr
import helios.test.generators.jsArray
import helios.test.generators.jsObject
import helios.test.generators.json
import io.kotest.properties.Gen

class InstancesTest : UnitSpec() {

    init {

        testLaws(
            OptionalLaws.laws(
                optionalGen = Gen.alphaStr().map { JsObject.index().index(it) },
                aGen = Gen.jsObject(),
                bGen = Gen.json(),
                funcGen = Gen.functionAToB(Gen.json()),
                EQA = Eq.any(),
                EQOptionB = Eq.any()
            )
        )

        testLaws(
            OptionalLaws.laws(
                optional = JsArray.index().index(1),
                aGen = Gen.jsArray(),
                bGen = Gen.json(),
                funcGen = Gen.functionAToB(Gen.json()),
                EQA = Eq.any(),
                EQOptionB = Eq.any()
            )
        )

        testLaws(
            TraversalLaws.laws(
                traversal = JsObject.each().each(),
                aGen = Gen.jsObject(),
                bGen = Gen.json(),
                funcGen = Gen.functionAToB(Gen.json()),
                EQA = Eq.any(),
                EQOptionB = Eq.any(),
                EQListB = Eq.any()
            )
        )

        testLaws(
            TraversalLaws.laws(
                traversal = JsArray.each().each(),
                aGen = Gen.jsArray(),
                bGen = Gen.json(),
                funcGen = Gen.functionAToB(Gen.json()),
                EQA = Eq.any(),
                EQOptionB = Eq.any(),
                EQListB = Eq.any()
            )
        )
    }
}
package helios.instances

import arrow.extension
import arrow.typeclasses.Monoid
import helios.core.JsArray
import helios.core.JsDouble
import helios.core.JsFloat
import helios.core.JsInt
import helios.core.JsLong
import helios.core.JsObject
import helios.core.JsString

@extension
interface JsIntMonoid : Monoid<JsInt>, JsIntSemigroup {
    override fun empty(): JsInt = JsInt(0)
}

@extension
interface JsLongMonoid : Monoid<JsLong>, JsLongSemigroup {
    override fun empty(): JsLong = JsLong(0L)
}

@extension
interface JsFloatMonoid : Monoid<JsFloat>, JsFloatSemigroup {
    override fun empty(): JsFloat = JsFloat(0f)
}

@extension
interface JsDoubleMonoid : Monoid<JsDouble>, JsDoubleSemigroup {
    override fun empty(): JsDouble = JsDouble(0.0)
}

@extension
interface JsStringMonoid : Monoid<JsString>, JsStringSemigroup {
    override fun empty(): JsString = JsString("")
}

@extension
interface JsArrayMonoid : Monoid<JsArray>, JsArraySemigroup {
    override fun empty(): JsArray = JsArray(emptyList())
}

@extension
interface JsObjectMonoid : Monoid<JsObject>, JsObjectSemigroup {
    override fun empty(): JsObject = JsObject(emptyMap())
}

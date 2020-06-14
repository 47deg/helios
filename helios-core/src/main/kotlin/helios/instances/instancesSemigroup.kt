package helios.instances

import arrow.core.ListK
import arrow.core.extensions.listk.semigroup.semigroup
import arrow.core.extensions.semigroup
import arrow.core.k
import arrow.extension
import arrow.typeclasses.Semigroup
import helios.core.JsArray
import helios.core.JsDouble
import helios.core.JsFloat
import helios.core.JsInt
import helios.core.JsLong
import helios.core.JsObject
import helios.core.JsString
import helios.core.Json

@extension
interface JsIntSemigroup : Semigroup<JsInt> {
    override fun JsInt.combine(b: JsInt): JsInt = JsInt(Int.semigroup().run {
        value.combine(b.value)
    })
}

@extension
interface JsLongSemigroup : Semigroup<JsLong> {
    override fun JsLong.combine(b: JsLong): JsLong = JsLong(Long.semigroup().run {
        value.combine(b.value)
    })
}

@extension
interface JsFloatSemigroup : Semigroup<JsFloat> {
    override fun JsFloat.combine(b: JsFloat): JsFloat = JsFloat(Float.semigroup().run {
        value.combine(b.value)
    })
}

@extension
interface JsDoubleSemigroup : Semigroup<JsDouble> {
    override fun JsDouble.combine(b: JsDouble): JsDouble = JsDouble(Double.semigroup().run {
        value.combine(b.value)
    })
}

@extension
interface JsStringSemigroup : Semigroup<JsString> {
    override fun JsString.combine(b: JsString): JsString = JsString(String.semigroup().run {
        value.toString().combine(b.value.toString())
    })
}

@extension
interface JsArraySemigroup : Semigroup<JsArray> {
    override fun JsArray.combine(b: JsArray): JsArray = JsArray(ListK.semigroup<Json>().run {
        value.k().combine(b.value.k())
    })
}

@extension
interface JsObjectSemigroup : Semigroup<JsObject> {
    override fun JsObject.combine(b: JsObject): JsObject = JsObject(value + b.value)
}

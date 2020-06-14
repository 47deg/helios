package helios.instances

import arrow.core.extensions.eq
import arrow.core.extensions.list.foldable.forAll
import arrow.extension
import arrow.typeclasses.Eq
import helios.core.*
import helios.instances.jsarray.eq.eq
import helios.instances.jsnumber.eq.eq
import helios.instances.jsobject.eq.eq
import helios.instances.json.eq.eq

@extension
interface JsStringEq : Eq<JsString> {
  override fun JsString.eqv(b: JsString): Boolean = with(String.eq()) {
    value.toString().eqv(b.value.toString())
  }
}

@extension
interface JsObjectEq : Eq<JsObject> {
  override fun JsObject.eqv(b: JsObject): Boolean = with(Json.eq()) {
    value.entries.zip(b.value.entries) { aa, bb ->
      aa.key == bb.key && aa.value.eqv(bb.value)
    }.forAll { it }
  }
}

@extension
interface JsArrayEq : Eq<JsArray> {
  override fun JsArray.eqv(b: JsArray): Boolean = with(Json.eq()) {
    value.zip(b.value) { a, b -> a.eqv(b) }
      .forAll { it }
  }
}

@extension
interface JsonEq : Eq<Json> {
  override fun Json.eqv(b: Json): Boolean = when {
    this is JsObject && b is JsObject   -> JsObject.eq().run { this@eqv.eqv(b) }
    this is JsString && b is JsString   -> String.eq().run {
      this@eqv.value.toString().eqv(b.value.toString())
    }
    this is JsNumber && b is JsNumber   -> JsNumber.eq().run { this@eqv.eqv(b) }
    this is JsBoolean && b is JsBoolean -> Boolean.eq().run { this@eqv.value.eqv(b.value) }
    this is JsArray && b is JsArray     -> JsArray.eq().run { this@eqv.eqv(b) }
    else                                -> isNull && b.isNull
  }

}

@extension
interface JsNumberEq : Eq<JsNumber> {
  override fun JsNumber.eqv(b: JsNumber): Boolean = when (this) {
    is JsDecimal -> when (b) {
      is JsDecimal -> String.eq().run { this@eqv.value.eqv(b.value) }
      is JsLong    -> String.eq().run { this@eqv.value.eqv(b.value.toString()) }
      is JsDouble  -> String.eq().run { this@eqv.value.eqv(b.value.toString()) }
      is JsFloat   -> String.eq().run { this@eqv.value.eqv(b.value.toString()) }
      is JsInt     -> String.eq().run { this@eqv.value.eqv(b.value.toString()) }
    }
    is JsLong    -> when (b) {
      is JsDecimal -> String.eq().run { this@eqv.value.toString().eqv(b.value) }
      is JsLong    -> Long.eq().run { this@eqv.value.eqv(b.value) }
      is JsDouble  -> Double.eq().run { this@eqv.value.toDouble().eqv(b.value) }
      is JsFloat   -> Float.eq().run { this@eqv.value.toFloat().eqv(b.value) }
      is JsInt     -> Long.eq().run { this@eqv.value.eqv(b.value.toLong()) }
    }
    is JsDouble  -> when (b) {
      is JsDecimal -> String.eq().run { this@eqv.value.toString().eqv(b.value) }
      is JsLong    -> Double.eq().run { this@eqv.value.eqv(b.value.toDouble()) }
      is JsDouble  -> Double.eq().run { this@eqv.value.eqv(b.value) }
      is JsFloat   -> Double.eq().run { this@eqv.value.eqv(b.value.toDouble()) }
      is JsInt     -> Double.eq().run { this@eqv.value.eqv(b.value.toDouble()) }
    }
    is JsFloat   -> when (b) {
      is JsDecimal -> String.eq().run { this@eqv.value.toString().eqv(b.value) }
      is JsLong    -> Float.eq().run { this@eqv.value.eqv(b.value.toFloat()) }
      is JsDouble  -> Double.eq().run { this@eqv.value.toDouble().eqv(b.value) }
      is JsFloat   -> Float.eq().run { this@eqv.value.eqv(b.value) }
      is JsInt     -> Float.eq().run { this@eqv.value.eqv(b.value.toFloat()) }
    }
    is JsInt     -> when (b) {
      is JsDecimal -> String.eq().run { this@eqv.value.toString().eqv(b.value) }
      is JsLong    -> Long.eq().run { this@eqv.value.toLong().eqv(b.value) }
      is JsDouble  -> Double.eq().run { this@eqv.value.toDouble().eqv(b.value) }
      is JsFloat   -> Float.eq().run { this@eqv.value.toFloat().eqv(b.value) }
      is JsInt     -> Int.eq().run { this@eqv.value.eqv(b.value) }
    }
  }
}
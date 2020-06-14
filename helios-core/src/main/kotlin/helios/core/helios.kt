package helios.core

import arrow.core.*
import arrow.core.extensions.monoid
import arrow.core.extensions.option.applicative.applicative
import arrow.typeclasses.Monoid
import helios.instances.HeliosFacade
import helios.instances.jsarray.eq.eq
import helios.instances.jsnumber.eq.eq
import helios.instances.jsobject.eq.eq
import helios.instances.json.eq.eq
import helios.parser.Parser
import helios.typeclasses.Decoder
import java.io.File
import java.math.BigDecimal
import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.channels.ReadableByteChannel

private const val MaxLongString = "9223372036854775807"
private const val MinLongString = "-9223372036854775808"

private fun String.withPrefixPerLine(prefix: String) =
  lines().joinToString(separator = "\n") { "$prefix$it" }

@Suppress("unused")
sealed class Json {

  inline val isNull inline get() = this === JsNull

  companion object {
    fun fromValues(i: Iterable<Json>): JsArray = JsArray(i.toList())

    fun parseFromByteBuffer(buf: ByteBuffer): Either<Throwable, Json> =
      Parser.parseFromByteBuffer(buf, HeliosFacade).toEither()

    fun parseFromChannel(channel: ReadableByteChannel): Either<Throwable, Json> =
      Parser.parseFromChannel(channel, HeliosFacade).toEither()

    fun parseFromCharSequence(cs: CharSequence): Either<Throwable, Json> =
      Parser.parseFromCharSequence(cs, HeliosFacade).toEither()

    fun parseFromFile(file: File): Either<Throwable, Json> =
      Parser.parseFromFile(file, HeliosFacade).toEither()

    fun parseFromPath(path: String): Either<Throwable, Json> =
      Parser.parseFromPath(path, HeliosFacade).toEither()

    fun parseFromString(s: String): Either<Throwable, Json> =
      Parser.parseFromString(s, HeliosFacade).toEither()

    fun parseUnsafe(s: String): Json =
      Parser.parseFromString(s, HeliosFacade).fold({ throw it }, { it })
  }

  operator fun get(key: String): Option<Json> = when (this) {
    is JsObject -> Option.fromNullable(this.value[key])
    else        -> None
  }

  fun <A> decode(decoder: Decoder<A>): Either<DecodingError, A> =
    decoder.decode(this)

  fun <B> fold(
    ifJsString: (JsString) -> B,
    ifJsNumber: (JsNumber) -> B,
    ifJsArray: (JsArray) -> B,
    ifJsObject: (JsObject) -> B,
    ifJsBoolean: (JsBoolean) -> B,
    ifJsNull: () -> B
  ): B =
    when (this) {
      is JsString -> ifJsString(this)
      is JsNumber -> ifJsNumber(this)
      is JsArray -> ifJsArray(this)
      is JsObject -> ifJsObject(this)
      is JsNull -> ifJsNull()
      is JsBoolean -> ifJsBoolean(this)
    }

  fun asJsString(): Option<JsString> =
    (this as? JsString)?.some() ?: none()

  fun asJsBoolean(): Option<JsBoolean> =
    (this as? JsBoolean)?.some() ?: none()

  fun asJsNumber(): Option<JsNumber> =
    (this as? JsNumber)?.some() ?: none()

  fun asJsArray(): Option<JsArray> =
    (this as? JsArray)?.some() ?: none()

  fun asJsObject(): Option<JsObject> =
    (this as? JsObject)?.some() ?: none()

  fun asJsNull(): Option<JsNull> =
    (this as? JsNull)?.some() ?: none()

  fun merge(that: Json, M: Monoid<Json>): Json = M.combineAll(listOf(this, that))

  abstract fun noSpaces(): String

  open fun spaces2(): String = noSpaces()

  open fun spaces4(): String = noSpaces()

  override fun equals(other: Any?): Boolean = Json.eq().run {
    (other as? Json)?.let { eqv(it) } ?: false
  }

  override fun hashCode(): Int = javaClass.hashCode()

}

data class JsBoolean(val value: Boolean) : Json() {

  override fun noSpaces(): String = "$value"

  companion object
}

data class JsString(val value: CharSequence) : Json() {
  override fun noSpaces(): String = """"$value""""

  companion object
}

sealed class JsNumber : Json() {

  abstract fun toBigDecimal(): BigDecimal

  abstract fun toBigInteger(): BigInteger

  abstract fun toDouble(): Double

  abstract fun toFloat(): Float

  abstract fun toLong(): Long

  open fun toInt(): Option<Int> = toLong().let {
    val asInt: Int = it.toInt()
    if (asInt.compareTo(it) == 0) Some(asInt) else None
  }

  open fun toShort(): Option<Short> = toInt().flatMap {
    val asShort: Short = it.toShort()
    if (asShort.compareTo(it) == 0) Some(asShort) else None
  }

  open fun toByte(): Option<Byte> = toInt().flatMap {
    val asByte: Byte = it.toByte()
    if (asByte.compareTo(it) == 0) Some(asByte) else None
  }

  override fun equals(other: Any?): Boolean = JsNumber.eq().run {
    (other as? JsNumber)?.let { eqv(it) } ?: false
  }

  abstract override fun hashCode(): Int

  companion object {

    operator fun invoke(value: BigDecimal): JsDecimal = JsDecimal(value.toString())

    operator fun invoke(value: BigInteger): JsDecimal = JsDecimal(value.toString())

    operator fun invoke(value: Double): JsDouble = JsDouble(value)

    operator fun invoke(value: Float): JsFloat = JsFloat(value)

    operator fun invoke(value: Long): JsLong = JsLong(value)

    operator fun invoke(value: Int): JsInt = JsInt(value)

    operator fun invoke(value: Short): JsInt = JsInt(value.toInt())

    operator fun invoke(value: Byte): JsInt = JsInt(value.toInt())

    fun fromDecimalStringUnsafe(value: String): JsDecimal = JsDecimal(value)

    fun fromIntegralStringUnsafe(value: String): JsNumber {
      val bound = if (value[0] == '-') MinLongString else MaxLongString
      val isJsDecimal =
        !(value.length < bound.length
            || (value.length == bound.length && value <= bound))
      return if (isJsDecimal) JsDecimal(value) else {
        val longValue = java.lang.Long.parseLong(value)

        if (value[0] == '-' && longValue == 0L) JsDecimal(value) else JsLong(longValue)
      }
    }
  }
}

data class JsDecimal(val value: String) : JsNumber() {

  override fun toBigDecimal(): BigDecimal = value.toBigDecimal()

  override fun toBigInteger(): BigInteger = value.toBigInteger()

  override fun toDouble(): Double = value.toDouble()

  override fun toFloat(): Float = value.toFloat()

  override fun toLong(): Long = value.toLong()

  override fun noSpaces(): String = value

  override fun equals(other: Any?): Boolean = JsNumber.eq().run {
    (other as? JsNumber)?.let { eqv(it) } ?: false
  }

  override fun hashCode(): Int = value.hashCode()

  companion object
}

data class JsLong(val value: Long) : JsNumber() {
  override fun toBigDecimal(): BigDecimal = value.toBigDecimal()

  override fun toBigInteger(): BigInteger = value.toBigInteger()

  override fun toDouble(): Double = value.toDouble()

  override fun toFloat(): Float = value.toFloat()

  override fun toLong(): Long = value

  override fun noSpaces(): String = "$value"

  override fun equals(other: Any?): Boolean = JsNumber.eq().run {
    (other as? JsNumber)?.let { eqv(it) } ?: false
  }

  override fun hashCode(): Int = value.hashCode()

  companion object
}

data class JsDouble(val value: Double) : JsNumber() {

  override fun toBigDecimal(): BigDecimal = value.toBigDecimal()

  override fun toBigInteger(): BigInteger = value.toBigDecimal().toBigInteger()

  override fun toDouble(): Double = value

  override fun toFloat(): Float = value.toFloat()

  override fun toLong(): Long = value.toLong()

  override fun noSpaces(): String = "$value"

  override fun equals(other: Any?): Boolean = JsNumber.eq().run {
    (other as? JsNumber)?.let { eqv(it) } ?: false
  }

  override fun hashCode(): Int = value.hashCode()

  companion object
}

data class JsFloat(val value: Float) : JsNumber() {

  override fun toBigDecimal(): BigDecimal = value.toBigDecimal()

  override fun toBigInteger(): BigInteger = value.toBigDecimal().toBigInteger()

  override fun toDouble(): Double = value.toDouble()

  override fun toFloat(): Float = value

  override fun toLong(): Long = value.toLong()

  override fun noSpaces(): String = "$value"

  override fun equals(other: Any?): Boolean = JsNumber.eq().run {
    (other as? JsNumber)?.let { eqv(it) } ?: false
  }

  override fun hashCode(): Int = value.hashCode()

  companion object
}

data class JsInt(val value: Int) : JsNumber() {
  override fun toBigDecimal(): BigDecimal = value.toBigDecimal()

  override fun toBigInteger(): BigInteger = value.toBigInteger()

  override fun toDouble(): Double = value.toDouble()

  override fun toFloat(): Float = value.toFloat()

  override fun toLong(): Long = value.toLong()

  override fun toInt(): Option<Int> = value.some()

  override fun toShort(): Option<Short> = value.toShort().some()

  override fun toByte(): Option<Byte> = value.toByte().some()

  override fun noSpaces(): String = "$value"

  override fun equals(other: Any?): Boolean = JsNumber.eq().run {
    (other as? JsNumber)?.let { eqv(it) } ?: false
  }

  override fun hashCode(): Int = value.hashCode()

  companion object
}

data class JsArray(val value: List<Json>) : Json() {

  operator fun get(index: Int): Option<Json> = Option.fromNullable(value.getOrNull(index))

  override fun noSpaces(): String =
    value.joinToString(prefix = "[", separator = ",", postfix = "]", transform = Json::noSpaces)

  override fun spaces2(): String =
    value.joinToString(
      prefix = "[\n",
      separator = ",\n",
      postfix = "\n]",
      transform = { it.spaces2().withPrefixPerLine("  ") })

  override fun spaces4(): String =
    value.joinToString(
      prefix = "[\n",
      separator = ",\n",
      postfix = "\n]",
      transform = { it.spaces4().withPrefixPerLine("    ") })

  override fun equals(other: Any?): Boolean = JsArray.eq().run {
    (other as? JsArray)?.let { eqv(it) } ?: false
  }

  override fun hashCode(): Int = value.hashCode()

  companion object
}

data class JsObject(val value: Map<String, Json>) : Json() {

  companion object {
    operator fun invoke(vararg keyValues: Pair<String, Json>): JsObject =
      JsObject(keyValues.toMap())

    operator fun invoke(vararg keyValues: Tuple2<String, Json>): JsObject =
      JsObject(keyValues.map { it.a to it.b }.toMap())
  }

  fun add(key: String, value: Json): Json = JsObject(this.value + (key to value))

  fun toList(): List<Tuple2<String, Json>> = value.toList().map { it.toTuple2() }

  override fun noSpaces(): String =
    value.map { (k, v) -> """"$k":${v.noSpaces()}""" }.joinToString(
      prefix = "{",
      separator = ",",
      postfix = "}"
    )

  override fun spaces2(): String =
    value.map { (k, v) -> """"$k": ${v.spaces2()}""" }.joinToString(
      prefix = "{\n",
      separator = ",\n",
      postfix = "\n}"
    ) { it.withPrefixPerLine("  ") }

  override fun spaces4(): String =
    value.map { (k, v) -> """"$k": ${v.spaces4()}""" }.joinToString(
      prefix = "{\n",
      separator = ",\n",
      postfix = "\n}"
    ) { it.withPrefixPerLine("    ") }

  override fun equals(other: Any?): Boolean = JsObject.eq().run {
    (other as? JsObject)?.let { eqv(it) } ?: false
  }

  override fun hashCode(): Int = value.hashCode()

}

object JsNull : Json() {
  override fun noSpaces(): String = "null"
}

val JsTrue = JsBoolean(true)
val JsFalse = JsBoolean(false)


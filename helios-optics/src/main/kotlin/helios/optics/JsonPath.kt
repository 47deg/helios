package helios.optics

import arrow.core.*
import arrow.free.free
import arrow.optics.*
import arrow.optics.typeclasses.*
import helios.core.*
import helios.instances.StringDecoderInstance
import helios.instances.StringEncoderInstance
import helios.typeclasses.*

/**
 * [JsonPath] is a Json DSL based on Arrow-Optics (http://arrow-kt.io/docs/optics/iso/).
 *
 * With [JsonPath] you can represent paths/relations within your [Json] and allows for working with [Json] in an elegant way.
 */
data class JsonPath(override val json: Optional<Json, Json>): JsonPathFunctions<POptional.ForOptional> {

    companion object {
        /**
         * [JsonPath] [root] which is the start of any path.
         */
        val root: JsonPathFunctions<POptional.ForOptional> = JsonPath(Optional.id())

        /**
         * Overload constructor to point to [root].
         */
        operator fun invoke() = root
    }

    /**
     * Extract value as [Boolean] from path.
     */
    override val boolean: Optional<Json, Boolean> = json compose jsonJsBoolean() compose jsBooleanIso()

    /**
     * Extract value as [CharSequence] from path.
     */
    override val charseq: Optional<Json, CharSequence> = json compose jsonJsString() compose  jsStringIso()

    /**
     * Extract value as [String] from path.
     */
    override val string: Optional<Json, String> = extract(StringEncoderInstance, StringDecoderInstance)

    /**
     * Extract value as [JsNumber] from path.
     */
    override val number: Optional<Json, JsNumber> = json compose jsonJsNumber()

    /**
     * Extract value as [JsDecimal] from path.
     */
    override val decimal: Optional<Json, String> = number compose jsNumberJsDecimal() compose jsDecimalIso()

    /**
     * Extract value as [Long] from path.
     */
    override val long: Optional<Json, Long> = number compose jsNumberJsLong() compose jsLongIso()

    /**
     * Extract value as [Float] from path.
     */
    override val float: Optional<Json, Float> = number compose jsNumberJsFloat() compose jsFloatIso()

    /**
     * Extract value as [Int] from path.
     */
    override val int: Optional<Json, Int> = number compose jsNumberJsInt() compose jsIntIso()

    /**
     * Extract [JsArray] as `List<Json>` from path.
     */
    override val array: Optional<Json, List<Json>> = json compose jsonJsArray() compose jsArrayIso()

    /**
     * Extract [JsObject] as `Map<String, Json>` from path.
     */
    override val `object`: Optional<Json, Map<String, Json>> = json compose jsonJsObject() compose jsObjectIso()

    /**
     * Extract [JsNull] from path.
     */
    override val `null`: Optional<Json, JsNull> = json compose jsonJsNull()

    /**
     * Select field with [name] in [JsObject] from path.
     */
    override fun select(name: String) = JsonPath(json compose jsonJsObject() compose index<JsObject, String, Json>().index(name).asHPOptional())

    /**
     * Extract field with [name] from [JsObject] from path.
     */
    override fun at(field: String): Optional<Json, Option<Json>> = json compose jsonJsObject() compose At.at(field)

    /**
     *  Get element at index [i] from [JsArray].
     */
    override operator fun get(i: Int) = JsonPath(json compose jsonJsArray() compose index<JsArray, Int, Json>().index(i).asHPOptional())

    /**
     * Extract [A] from path.
     */
    override fun <A> extract(DE: Decoder<A>, EN: Encoder<A>): Optional<Json, A> =
            json compose parse(DE, EN)

    /**
     * Select field with [name] in [JsObject] and extract as [A] from path.
     */
    override fun <A> selectExtract(DE: Decoder<A>, EN: Encoder<A>, name: String): Optional<Json, A> =
            select(name).extract(DE, EN)

    /**
     * Select every entry in [JsObject] or [JsArray].
     */
    override fun every() = JsonTraversalPath(json compose jsonTraversal)

    /**
     * Filter [JsArray] by indices that satisfy the predicate [p].
     */
    override fun filterIndex(p: Predicate<Int>) = JsonTraversalPath(array compose filterIndex<List<Json>, Int, Json>().filter(p).asHPTraversal())

    /**
     * Filter [JsObject] by keys that satisfy the predicate [p].
     */
    override fun filterKeys(p: Predicate<String>) = JsonTraversalPath(`object` compose filterIndex<Map<String, Json>, String, Json>().filter(p).asHPTraversal())

}

/**
 * Extract [A] from path.
 */
inline fun <reified A> JsonPath.extract(EN: Encoder<A> = encoder(), DE: Decoder<A> = decoder()): Optional<Json, A> = extract(DE, EN)

/**
 * Select field with [name] in [JsObject] and extract as [A] from path.
 */
inline fun <reified A> JsonPath.selectExtract(name: String, DE: Decoder<A> = decoder(), EN: Encoder<A> = encoder()): Optional<Json, A> =
        selectExtract(DE, EN, name)

/**
 * Unsafe optic: needs some investigation because it is required to extract reasonable typed values from Json.
 * https://github.com/circe/circe/blob/master/modules/optics/src/main/scala/io/circe/optics/JsonPath.scala#L152
 */
@PublishedApi
internal fun <A> parse(DE: Decoder<A>, EN: Encoder<A>): Prism<Json, A> = Prism(
        getOrModify = { json -> DE.decode(json).mapLeft { _ -> json } },
        reverseGet = EN::encode
)

/**
 * Unsafe optic: needs some investigation because it is required to extract reasonable typed values from Json.
 * https://github.com/circe/circe/blob/master/modules/optics/src/main/scala/io/circe/optics/JsonPath.scala#L152
 */
@PublishedApi
internal inline fun <reified A> parse(EN: Encoder<A> = encoder(), DE: Decoder<A> = decoder()): Prism<Json, A> = parse(DE, EN)
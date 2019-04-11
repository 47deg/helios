package helios.optics

import helios.meta.json
import io.kotlintest.properties.Gen

@json
data class City(val streets: List<Street>) {
  companion object
}

@json
data class Street(val name: String) {
  companion object
}

fun genStreet(): Gen<Street> = Gen.string().map { str -> Street(str) }

fun genCity(): Gen<City> = Gen.list(genStreet()).map { streets -> City(streets) }

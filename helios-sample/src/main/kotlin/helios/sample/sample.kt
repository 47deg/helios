package helios.sample

import arrow.core.Either
import helios.core.Json
import helios.optics.JsonPath.Companion.root
import helios.optics.JsonPathFunctions
import helios.optics.Traversal
import helios.optics.fix
import helios.optics.modify
import helios.typeclasses.Decoder
import helios.typeclasses.DecodingError
import helios.typeclasses.decoder

const val companyJsonString = """
{
  "name": "Arrow",
  "address": {
    "city": "Functional Town",
    "street": {
      "number": 1337,
      "name": "Functional street"
    }
  },
  "employees": [
    {
      "name": "John",
      "lastName": "doe"
    },
    {
      "name": "Jane",
      "lastName": "doe"
    }
  ]
}"""

fun main(args: Array<String>) {

    val companyDecoder: Decoder<Company> = decoder()
    val companyJson: Json = Json.parseUnsafe(companyJsonString)
    val errorOrCompany: Either<DecodingError, Company> = companyDecoder.decode(companyJson)

    errorOrCompany.fold({
        println("Something went wrong during decoding: $it")
    }, {
        println("Successfully decode the json: $it")
    })

    root.name.string.fix().modify(companyJson, String::toUpperCase).let(::println)

    root.address.street.name.string.fix().getOption(companyJson).let(::println)

    val employeeLastNames: Traversal<Json, String> = root.employees.every().lastName.string.fix()

    employeeLastNames.modify(companyJson, String::capitalize).let {
        employeeLastNames.getAll(it)
    }.let(::println)

    root.employees.filterIndex { it == 0 }.select("name").string.getAll(companyJson).let(::println)

    root.employees.every().filterKeys { it == "name" }.string.getAll(companyJson).let(::println)

}

/**
 * Generated code for Company
 */
val <F> JsonPathFunctions<F>.name: JsonPathFunctions<F>
    get() = select("name")
val <F> JsonPathFunctions<F>.address: JsonPathFunctions<F>
    get() = select("address")
val <F> JsonPathFunctions<F>.employees: JsonPathFunctions<F>
    get() = select("employees")

/**
 * Generated code for Address
 */
val <F> JsonPathFunctions<F>.city: JsonPathFunctions<F>
    get() = select("city")
val <F> JsonPathFunctions<F>.street: JsonPathFunctions<F>
    get() = select("street")

/**
 * Generated code for Street
 */
val <F> JsonPathFunctions<F>.number: JsonPathFunctions<F>
    get() = select("number")
//fun <F> JsonPathFunctions<F>.name(): JsonPathFunctions<F> = select("name") <-- conflict with Company.name && Employee.name


/**
 * Generated code for Employee
 */
//fun <F> JsonPathFunctions<F>.name(): JsonPathFunctions<F> = select("name") <-- conflict with Company.name && Street.name
val <F> JsonPathFunctions<F>.lastName: JsonPathFunctions<F>
    get() = select("lastName")
package com.github.mjakubowski84.parquet4s

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class StringLensSpec extends AnyFlatSpec with Matchers {

  case class Street(name: String, more: Option[String])
  case class Address(street: Street, city: String)
  case class Person(name: String, age: Int, address: Address)

  val person: Person = Person(
    name = "Joe",
    age = 18,
    address = Address(
      street = Street(name = "Broad St", more = Some("123")),
      city = "Somewhere"
    )
  )

  "StringLens" should "extract String field from a long path" in {
      StringLens[Person](person, "address.street.name") should be("Broad St")
  }

  it should "extract String field from a short path" in {
      StringLens[Person](person, "name") should be("Joe")
  }

  it should "fail to extract Option field" in {
    val path = "address.street.more"
    val e = intercept[IllegalArgumentException](StringLens[Person](person, path))
    e.getMessage should be(s"Invalid element at path '$path'. Only String field can be used for partitioning.")
  }

  it should "fail to extract non-String field" in {
    val path = "age"
    val e = intercept[IllegalArgumentException](StringLens[Person](person, path))
    e.getMessage should be(s"Invalid element at path '$path'. Only String field can be used for partitioning.")
  }

  it should "fail to extract non-existing field" in {
    val e = intercept[IllegalArgumentException](StringLens[Person](person, "address.state"))
    e.getMessage should be(s"Invalid element at path 'address'. Field 'state' does not exist.")
  }

  it should "fail to read a child from a String field" in {
    val e = intercept[IllegalArgumentException](StringLens[Person](person, "name.[0]"))
    e.getMessage should be(s"Invalid element at path 'name'. Attempted to access field '[0]' from String.")
  }

  it should "fail to extract Product" in {
    val e = intercept[IllegalArgumentException](StringLens[Person](person, "address.street"))
    e.getMessage should be(s"Invalid element at path 'address.street'. Cannot partition by a Product class.")
  }

  it should "fail to extract a field using empty path" in {
    val e = intercept[IllegalArgumentException](StringLens[Person](person, ""))
    e.getMessage should be(s"Invalid element at path ''. Field '' does not exist.")
  }

}

package com.vxksoftware.model.dto

import zio.json.*

case class FilterMealDTO(
  strMeal: String,
  strMealThumb: String,
  idMeal: String
)

object FilterMealDTO {
  given JsonDecoder[FilterMealDTO] = DeriveJsonDecoder.gen[FilterMealDTO]
}

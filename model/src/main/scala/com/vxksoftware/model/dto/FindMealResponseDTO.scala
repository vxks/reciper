package com.vxksoftware.model.dto

import zio.Chunk
import zio.json.*

case class FindMealResponseDTO(
  meals: Chunk[MealDTO]
)

object FindMealResponseDTO {
  given JsonDecoder[FindMealResponseDTO] = DeriveJsonDecoder.gen[FindMealResponseDTO]
}

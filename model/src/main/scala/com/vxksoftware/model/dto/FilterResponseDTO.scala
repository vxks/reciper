package com.vxksoftware.model.dto

import zio.Chunk
import zio.json.*

case class FilterResponseDTO(
  meals: Chunk[FilterMealDTO]
)

object FilterResponseDTO {
  given JsonDecoder[FilterResponseDTO] = DeriveJsonDecoder.gen[FilterResponseDTO]
}

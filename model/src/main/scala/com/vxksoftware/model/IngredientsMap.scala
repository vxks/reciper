package com.vxksoftware.model

import zio.json.*

type Kind = String

case class IngredientsMap(
  map: List[IngredientsMapEntry]
)// derives JsonDecoder

object IngredientsMap {
  given JsonCodec[IngredientsMap] = DeriveJsonCodec.gen[IngredientsMap]
}

case class IngredientsMapEntry(
  name: Kind,
  ingredients: List[IngredientsMapIngredient]
) //derives JsonDecoder

object IngredientsMapEntry {
  given JsonCodec[IngredientsMapEntry] = DeriveJsonCodec.gen[IngredientsMapEntry]

}

case class IngredientsMapIngredient(
  name: String,
  id: String
) //derives JsonDecoder

object IngredientsMapIngredient {
  given JsonCodec[IngredientsMapIngredient] = DeriveJsonCodec.gen[IngredientsMapIngredient]

}

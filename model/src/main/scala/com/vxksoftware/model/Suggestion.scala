package com.vxksoftware.model

import com.vxksoftware.model.Recipe

case class Suggestion(
  recipe: Recipe,
  missingIngredientKinds: Set[IngredientKind]
)

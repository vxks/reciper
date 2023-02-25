package com.vxksoftware.model.dto

import zio.json.JsonEncoder

case class SuggestionsResponseDTO(
  exact: Set[RecipeResponseDTO],
  suggestions: Set[SuggestionDTO]
) derives JsonEncoder

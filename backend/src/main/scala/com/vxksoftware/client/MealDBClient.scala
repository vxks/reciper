package com.vxksoftware.client

import com.vxksoftware.model.dto.{FilterMealDTO, FilterResponseDTO, FindMealResponseDTO, MealDTO}
import zio.*
import zio.stream.*
import zio.json.*
import com.vxksoftware.model.Recipe
import com.vxksoftware.utils.*

import java.net.URL
import java.util.concurrent.TimeUnit
import scala.io.Source
import scala.util.Try

trait MealDBClient {
  def findByIngredient(ingredient: String): RIO[Scope, Set[Long]]

  def findByIngredients(ingredients: Set[String]): RIO[Scope, Set[MealDTO]]

  def find(id: Long): RIO[Scope, Option[MealDTO]]
}

object MealDBClient {
  lazy val live: ZLayer[Any, Nothing, MealDBClientLive] = ZLayer.fromFunction(MealDBClientLive.apply _)

  case class MealDBClientLive() extends MealDBClient {
    private val apiKey                                = 1
    private val baseUrl: String                       = s"https://www.themealdb.com/api/json/v1/$apiKey"
    private val ingredientFilterUrl: String => String = ingredient => baseUrl + s"/filter.php?i=$ingredient"
    private val mealUrl: Long => String               = id => baseUrl + s"/lookup.php?i=$id"

    def find(id: Long): RIO[Scope, Option[MealDTO]] = {
      val url = mealUrl(id)

      source(new URL(url)).flatMap { source =>
        val lines = source.getLines()
        val response =
          ZIO.fromEither(lines.mkString.fromJson[FindMealResponseDTO]).mapError(s => new RuntimeException(s))

        response.flatMap {
          case FindMealResponseDTO(Chunk(recipe)) => ZIO.succeed(Some(recipe))
          case FindMealResponseDTO(meals) =>
            Console.printLineError(s"Could not find recipe by id: $id. Got " + meals) *>
              ZIO.succeed(None)
        }
      }
    }

    def findByIngredient(ingredient: String): RIO[Scope, Set[Long]] = {
      val url = ingredientFilterUrl(ingredient)
      source(new URL(url)).flatMap { source =>
        val lines = source.getLines()
        val response =
          ZIO.fromEither(lines.mkString.fromJson[FilterResponseDTO]).mapError(s => new RuntimeException(s))

        response.map(_.meals.map(_.idMeal.toLong).toSet)
      }
    }

    def findByIngredients(availableIngredients: Set[String]): RIO[Scope, Set[MealDTO]] = {
      val urls   = availableIngredients map ingredientFilterUrl
      val stream = ZStream.from(urls)

      stream
        // meal IDS containing the ingredient
        .mapZIOPar(20) { url =>
          val src = Source.fromURL(new URL(url))

          timedPrint("Get meal IDs") {
            for {
              lines <- ZIO.fromTry(Try(src.getLines().mkString)) <* ZIO.attempt(src.close())
              response <-
                ZIO.fromEither(lines.mkString.fromJson[FilterResponseDTO]).mapError(new RuntimeException(_))
              mealIds = response.meals.map(_.idMeal.toLong).toSet
            } yield mealIds
          }
        }
        .flattenIterables
        // MEALS containing the ingredient
        .mapZIOPar(20) { mealId =>
          timedPrint("Find meal") {
            find(mealId)
          }
        }
        // meals that can be made with available ingredients
        .collect {
          case Some(meal) if meal.ingredients.subsetOf(availableIngredients) => meal
        }
        .runCollect
        .map(_.toSet)
    }
  }
}

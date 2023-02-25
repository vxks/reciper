import com.vxksoftware.client.MealDBClient
import com.vxksoftware.model.CommonIngredients.*
import com.vxksoftware.model.IngredientKind
import com.vxksoftware.model.dto.{RecipeResponseDTO, SuggestionsResponseDTO}
import com.vxksoftware.service.RecipeFinder
import zio.*
import zio.json.*
import zio.http.*
import zio.http.Middleware.*
import zio.http.middleware.Cors.CorsConfig
import zio.http.model.*

object BackendApp extends ZIOAppDefault:

  val corsMW = cors(
    CorsConfig(
      anyOrigin = false,
      allowedOrigins = _.startsWith("http://localhost:3000")
    )
  )
  val httpApp = Http.collectZIO[Request] {
    case request @ Method.GET -> !! / "find" => {
      for {
        recipeFinder <- ZIO.service[RecipeFinder]
        ingredientsStr <- ZIO
                            .fromOption(request.url.queryParams.get("i"))
                            .mapError(_ => new RuntimeException("Could not extract `i` query param from find request"))
        ingredients = ingredientsStr.toSet.flatMap(IngredientKind.fromIngredient)
        recipes    <- recipeFinder.findMatchingRecipes(ingredients)
        response    = recipes.map(RecipeResponseDTO.fromRecipe)
      } yield Response.json(response.toJson)
    }
    case request @ Method.GET -> !! / "find_suggestions" => {
      for {
        recipeFinder <- ZIO.service[RecipeFinder]
        ingredientsStr <- ZIO
                            .fromOption(request.url.queryParams.get("i"))
                            .mapError(_ => new RuntimeException("Could not extract `i` query param from find request"))
        margin <- ZIO
                    .fromOption(request.url.queryParams.get("margin").flatMap(_.headOption).map(_.toInt))
                    .mapError(_ => new RuntimeException("Could not extract `i` query param from find request"))
        ingredients             = ingredientsStr.toSet.flatMap(IngredientKind.fromIngredient)
        recipesWithSuggestions <- recipeFinder.findSuggestions(ingredients, margin)
        (exact, suggestions)    = recipesWithSuggestions
        response                = SuggestionsResponseDTO(exact.map(RecipeResponseDTO.fromRecipe), suggestions)
      } yield Response.json(response.toJson)
    }
  } @@ corsMW

  val program = for {
    serverFiber <-
      Server
        .serve(
          httpApp
            .tapErrorZIO(t => Console.printLineError(t.getMessage))
            .mapError(t => Response(Status.InternalServerError, body = Body.fromString(t.getMessage)))
        )
        .fork
    _ <- Console.readLine
    _ <- serverFiber.interrupt
  } yield ExitCode.success

  val run = program.provide(
    Server.default,
    MealDBClient.live,
    RecipeFinder.mealDBRecipeFinder
  )

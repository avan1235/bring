package `in`.procyk.bring.db

import `in`.procyk.bring.RecipeIngredient
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.DoubleColumnType
import org.jetbrains.exposed.sql.TextColumnType
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import java.util.*

/**
 * Stores cooking recipes in a single PostgreSQL table without auxiliary tables for
 * ingredients or steps. PostgreSQL native arrays are leveraged to keep ingredient
 * components (name, measure, unit) and recipe steps inline with the recipe row.
 *
 * Ingredients are kept as three parallel arrays of equal length, each index `i`
 * forming one [RecipeIngredient] (name[i], measure[i], unit[i]).
 */
internal object CookingRecipesTable : UUIDTable(name = "cooking_recipes") {
    val name = text("name", eagerLoading = true)
    val byUserId = uuid("by_user_id").index()
    val createdAt = timestamp("created_at")
    val steps = array("steps", TextColumnType())
    val ingredientNames = array("ingredient_names", TextColumnType())
    val ingredientMeasures = array("ingredient_measures", DoubleColumnType())
    val ingredientUnits = array("ingredient_units", TextColumnType())
}

internal class CookingRecipeEntity(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object : UUIDEntityClass<CookingRecipeEntity>(CookingRecipesTable)

    var name by CookingRecipesTable.name
    var byUserId by CookingRecipesTable.byUserId
    var createdAt by CookingRecipesTable.createdAt
    var steps by CookingRecipesTable.steps
    var ingredientNames by CookingRecipesTable.ingredientNames
    var ingredientMeasures by CookingRecipesTable.ingredientMeasures
    var ingredientUnits by CookingRecipesTable.ingredientUnits

    var ingredients: List<RecipeIngredient>
        get() {
            val names = ingredientNames
            val measures = ingredientMeasures
            val units = ingredientUnits
            require(names.size == measures.size && names.size == units.size) {
                "Inconsistent ingredient arrays for recipe $id"
            }
            return List(names.size) { i -> RecipeIngredient(names[i], measures[i], units[i]) }
        }
        set(value) {
            ingredientNames = value.map { it.name }
            ingredientMeasures = value.map { it.measure }
            ingredientUnits = value.map { it.unit }
        }
}

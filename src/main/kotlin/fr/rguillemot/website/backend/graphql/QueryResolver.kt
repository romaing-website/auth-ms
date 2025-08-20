package fr.rguillemot.website.backend.graphql

import fr.rguillemot.website.backend.util.ExpressionEvaluator
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class QueryResolver(
    private val evaluator: ExpressionEvaluator
) {
    @QueryMapping
    fun hello(@Argument name: String?): String = "Hello, ${name ?: "world"}!"

    @QueryMapping
    fun calculate(@Argument expr: String): Double {
        // Trim + normalisation simple
        val cleaned = expr.replace(',', '.').trim()
        return evaluator.evaluate(cleaned)
    }
}

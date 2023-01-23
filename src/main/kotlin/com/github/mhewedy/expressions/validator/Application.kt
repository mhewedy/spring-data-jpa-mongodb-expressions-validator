package com.github.mhewedy.expressions.validator

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.mhewedy.expressions.Expressions
import com.github.mhewedy.expressions.ExpressionsRepository
import com.github.mhewedy.expressions.ExpressionsRepositoryImpl
import jakarta.persistence.Entity
import jakarta.persistence.Id
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.badRequest
import org.springframework.http.ResponseEntity.ok
import org.springframework.stereotype.Repository
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
@EnableJpaRepositories(repositoryBaseClass = ExpressionsRepositoryImpl::class)
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}

private const val INVALID_OPERATOR_PREFIX = "No enum constant com.github.mhewedy.expressions.Operator."
private const val INVALID_ATTR_PREFIX = "Unable to locate attribute with the given name"

@RestController
class Controller(
    val objectMapper: ObjectMapper,
    val testRepository: TestRepository
) {

    @PostMapping("/api/v1/validate")
    fun validate(@RequestBody expressionsString: String): ResponseEntity<ErrorResponse> {

        val expressions: Expressions

        try {
            expressions = objectMapper.readValue(expressionsString, Expressions::class.java)
        } catch (ex: Exception) {
            return badRequest().body(
                ErrorResponse("Invalid Request: " + ex.message)
            )
        }

        try {
            testRepository.findAll(expressions)
        } catch (ex: Exception) {
            if (ex.message?.contains(INVALID_ATTR_PREFIX) == true) {
                println("parsing success!")
                return ok().build()
            } else if (ex.message?.contains(INVALID_OPERATOR_PREFIX) == true) {
                return badRequest().body(
                    ErrorResponse("Invalid operator: " + ex.message?.removePrefix(INVALID_OPERATOR_PREFIX))
                )
            }
        }
        println("parsing success!")
        return ok().build()
    }
}

data class ErrorResponse(val error: String?)


@Entity
data class TestEntity(@Id val id: Int? = null)

@Repository
interface TestRepository : ExpressionsRepository<TestEntity, Long>
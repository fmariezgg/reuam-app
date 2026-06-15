package ni.uam.edu.models.validation

import io.ktor.http.HttpStatusCode
import ni.uam.edu.models.buildBackendError

typealias ValidationError = String
typealias ValidationFunction<T> = ValidationObject<T>.() -> ValidationError?
typealias FormValidationFunction = () -> Pair<String, ValidationError>?

class ValidationObject<T>(val name: String, val value: T) {
    private val validations: MutableList<ValidationFunction<T>> = mutableListOf()

    fun useValidation(validation: ValidationFunction<T>): ValidationObject<T> {
        validations.add(validation)
        return this
    }

    fun validate(): List<ValidationError> = validations.mapNotNull { this.it() }
}

class ValidationService(
    private val objects: List<ValidationObject<Any?>>,
    private val formValidations: List<FormValidationFunction>,
    private val message: String,
) {
    fun validate() {
        val errors = mutableMapOf<String, MutableList<ValidationError>>()

        objects.forEach { validationObject ->
            val fieldErrors = validationObject.validate()
            if (fieldErrors.isNotEmpty()) errors[validationObject.name] = fieldErrors.toMutableList()
        }

        formValidations.forEach { validation ->
            val fieldError = validation()
            if (fieldError != null) errors.getOrPut(fieldError.first) { mutableListOf() }.add(fieldError.second)
        }

        if (errors.isEmpty()) return

        throw buildBackendError(
            status = HttpStatusCode.BadRequest,
            code = "validation_error",
            detail = message,
        ) {
            errors.forEach { (field, messages) -> error(field, messages.joinToString()) }
        }
    }
}

class ValidationServiceBuilder {
    private val objects: MutableList<ValidationObject<Any?>> = mutableListOf()
    private val formValidations: MutableList<FormValidationFunction> = mutableListOf()

    @Suppress("UNCHECKED_CAST")
    fun <T> field(name: String, value: T?, validation: ValidationObject<T?>.() -> Unit) {
        val validationObject = ValidationObject(name, value)
        validationObject.validation()
        objects.add(validationObject as ValidationObject<Any?>)
    }

    fun refine(validation: FormValidationFunction) {
        formValidations.add(validation)
    }

    fun build(message: String) = ValidationService(objects, formValidations, message)
}

fun buildValidationService(
    message: String = "There was an error validating your request",
    validation: ValidationServiceBuilder.() -> Unit,
): ValidationService {
    val builder = ValidationServiceBuilder()
    builder.validation()
    return builder.build(message)
}

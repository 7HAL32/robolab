package mqtt

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File

/**
 * @author leon
 */
object Login {
    val login by lazy {
        jacksonObjectMapper().readValue<Login>(File("src/main/resources/login.json"))
    }

    data class Login(
            val username: String,
            val password: String
    )
}
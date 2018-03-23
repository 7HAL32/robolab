package communication

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File
import java.io.FileNotFoundException

/**
 * @author leon
 */
object Login {
    var login = loginFromFile()

    fun loginFromFile() =
            try {
                jacksonObjectMapper().readValue<Login>(File("src/main/resources/mqtt_login.json"))
            } catch (e: FileNotFoundException) {
                Login("", "")
            }


    data class Login(
            val username: String,
            val password: String
    )
}
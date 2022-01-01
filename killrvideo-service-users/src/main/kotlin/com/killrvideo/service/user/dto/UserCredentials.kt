package com.killrvideo.service.user.dto

import com.datastax.oss.driver.api.mapper.annotations.CqlName
import com.datastax.oss.driver.api.mapper.annotations.Entity
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey
import java.util.*
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

/**
 * Pojo representing DTO for table 'user_credentials'
 *
 * @author DataStax Developer Advocates team.
 */
@Entity
@CqlName("user_credentials")
data class UserCredentials(
    @PartitionKey
    var email: String? = null,
    var password: @Size(min = 1, message = "password must not be empty") String? = null,
    var userid: @NotNull UUID? = null
) {
    companion object {
        fun from(user: User, hashedPassword: String?) =
            UserCredentials(
                user.email,
                hashedPassword,
                user.userid
            )
    }
}

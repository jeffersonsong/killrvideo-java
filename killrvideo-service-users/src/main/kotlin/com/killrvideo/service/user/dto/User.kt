package com.killrvideo.service.user.dto

import com.datastax.oss.driver.api.mapper.annotations.CqlName
import com.datastax.oss.driver.api.mapper.annotations.Entity
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey
import java.time.Instant
import java.util.*
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

/**
 * Pojo representing DTO for table 'users'.
 *
 * @author DataStax Developer Advocates team.
 */
@Entity
@CqlName("users")
data class User(
    @PartitionKey
    var userid: UUID? = null,
    var firstname: @Size(min = 1, message = "firstName must not be empty") String? = null,
    var lastname: @Size(min = 1, message = "lastname must not be empty") String? = null,
    var email: @Size(min = 1, message = "email must not be empty") String? = null,
    var createdDate: @NotNull Instant? = null
)

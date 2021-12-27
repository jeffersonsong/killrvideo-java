package com.killrvideo.service.user.dto;

import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import lombok.*;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

/**
 * Pojo representing DTO for table 'users'.
 *
 * @author DataStax Developer Advocates team.
 */
@Entity
@CqlName("users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class User {
    @PartitionKey
    private UUID userid;

    @Size(min = 1, message = "firstName must not be empty")
    private String firstname;

    @Size(min = 1, message = "lastname must not be empty")
    private String lastname;

    @Size(min = 1, message = "email must not be empty")
    private String email;

    @NotNull
    private Instant createdDate;
}

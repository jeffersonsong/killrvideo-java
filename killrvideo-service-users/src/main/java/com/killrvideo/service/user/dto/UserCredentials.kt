package com.killrvideo.service.user.dto;

import com.datastax.oss.driver.api.mapper.annotations.CqlName;
import com.datastax.oss.driver.api.mapper.annotations.Entity;
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.UUID;

/**
 * Pojo representing DTO for table 'user_credentials'
 *
 * @author DataStax Developer Advocates team.
 */
@Entity
@CqlName("user_credentials")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UserCredentials implements Serializable {
    private static final long serialVersionUID = 2013590265131367178L;

    @PartitionKey
    private String email;

    @Size(min = 1, message = "password must not be empty")
    private String password;

    @NotNull
    private UUID userid;

    public static UserCredentials from(User user, String hashedPassword) {
        return new UserCredentials(
                user.getEmail(),
                hashedPassword,
                user.getUserid()
        );
    }
}

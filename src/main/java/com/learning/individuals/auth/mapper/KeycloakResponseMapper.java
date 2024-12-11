package com.learning.individuals.auth.mapper;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.learning.individuals.auth.dto.UserInfoResponse;
import com.learning.individuals.auth.dto.UserRepresentation;

@Mapper(componentModel = "spring", imports = {ZonedDateTime.class, Instant.class, ZoneId.class})
public interface KeycloakResponseMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "roles", source = "groups")
    @Mapping(target = "createdAt", expression = "java(user.getCreatedTimestamp() != null ? " +
            "ZonedDateTime.ofInstant(Instant.ofEpochMilli(user.getCreatedTimestamp()), ZoneId.systemDefault()) : null)")
    UserInfoResponse toUserInfoResponse(UserRepresentation user);
}
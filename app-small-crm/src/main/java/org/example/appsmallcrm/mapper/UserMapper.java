package org.example.appsmallcrm.mapper;

import org.example.appsmallcrm.dto.UserCreateDTO;
import org.example.appsmallcrm.dto.UserDTO;
import org.example.appsmallcrm.dto.UserUpdateDTO;
import org.example.appsmallcrm.entity.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    UserDTO toDto(User user);

    List<UserDTO> toDto(List<User> users);

    User fromCreateDTO(UserCreateDTO createDTO);

    // For updates, ignore null values from DTO
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromDto(UserUpdateDTO updateDTO, @MappingTarget User user);
}
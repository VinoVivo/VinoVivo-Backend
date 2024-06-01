package com.msusers.models.DTO;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private String id;

    private String userName;

    private String email;

    private String firstName;

    private String lastName;
}

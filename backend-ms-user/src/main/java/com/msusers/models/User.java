package com.msusers.models;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private String id;
    private String userName;
    private String email;
    private String firstName;
    private String lastName;
}

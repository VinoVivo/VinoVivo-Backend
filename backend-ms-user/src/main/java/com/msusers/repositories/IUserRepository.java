package com.msusers.repositories;

import com.msusers.models.User;

import java.util.Optional;

public interface IUserRepository {

    Optional<User> findById(String id);

}

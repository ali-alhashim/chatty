package com.chatty.repository;

import com.chatty.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByName(String name);

    boolean existsByEmail(String email);

    boolean existsByName(String name);


    @Query("{ '$or': [ " +
            "  { 'name': { $regex: ?0, $options: 'i' } }, " +

            "] }")
    List<User> findByKeyword(String keyword);
}

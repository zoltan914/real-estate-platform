package com.devtiro.realestate.repositories;

import com.devtiro.realestate.domain.entities.Role;
import com.devtiro.realestate.domain.entities.User;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends ElasticsearchRepository<User, String> {
    @Override
    List<User> findAll();

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    List<User> findAllByRole(Role role);
}


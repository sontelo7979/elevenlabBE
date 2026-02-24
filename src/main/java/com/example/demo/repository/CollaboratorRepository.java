package com.example.demo.repository;

import com.example.demo.model.Collaborator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CollaboratorRepository extends JpaRepository<Collaborator, Long> {
    Optional<Collaborator> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
}
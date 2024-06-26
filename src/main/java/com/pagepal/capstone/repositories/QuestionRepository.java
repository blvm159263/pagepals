package com.pagepal.capstone.repositories;

import com.pagepal.capstone.entities.postgre.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface QuestionRepository extends JpaRepository<Question, UUID>{

}

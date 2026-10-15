package com.companybot.repository;

import com.companybot.entity.Person;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonRepository extends JpaRepository<Person, Long> {

    boolean existsByDesemail(String desemail);
}
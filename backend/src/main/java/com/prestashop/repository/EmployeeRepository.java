package com.prestashop.repository;

import com.prestashop.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmail(String email);

    Optional<Employee> findByEmailAndActiveTrue(String email);

    Optional<Employee> findByResetPasswordToken(String token);

    boolean existsByEmail(String email);
}

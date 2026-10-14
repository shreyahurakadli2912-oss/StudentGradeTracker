package com.codealpha.gradetracker.repository;

import com.codealpha.gradetracker.model.StudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<StudentEntity, Long>, JpaSpecificationExecutor<StudentEntity> {

    Optional<StudentEntity> findByStudentIdIgnoreCase(String studentId);

    boolean existsByStudentIdIgnoreCase(String studentId);

    void deleteByStudentIdIgnoreCase(String studentId);
}

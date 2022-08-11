package com.example.dao;

import com.example.domain.Student;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 功能注释
 *
 * @author zhangdj
 * @version 1.0.0
 * @createdAt 2022/8/3 16:22
 * @updatedAt 2022/8/3 16:22
 */
@Repository
public class StudentDao {

    public Optional<Student> findById(Integer id) {
        return Optional.empty();
    }

    public List<Student> findAll() {
        return null;
    }
}
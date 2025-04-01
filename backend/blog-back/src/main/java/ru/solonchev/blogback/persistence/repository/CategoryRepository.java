package ru.solonchev.blogback.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.solonchev.blogback.persistence.model.Category;

import java.util.List;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

    @Query("""
            select c
            from Category c
            left join fetch c.posts
            """)
    List<Category> findAllWithPostCount();

    boolean existsByNameIgnoreCase(String name);
}

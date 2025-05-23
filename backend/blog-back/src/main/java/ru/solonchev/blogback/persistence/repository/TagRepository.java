package ru.solonchev.blogback.persistence.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.solonchev.blogback.persistence.model.Tag;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface TagRepository extends JpaRepository<Tag, UUID> {

    @Query("""
            select t
            from Tag t
            left join fetch t.posts
            """)
    Page<Tag> findAllWithPostCount(Pageable pageable);

    List<Tag> findByNameIn(Set<String> names);
}

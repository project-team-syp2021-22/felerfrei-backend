package at.htlstp.felerfrei.persistence;

import at.htlstp.felerfrei.domain.Product;
import at.htlstp.felerfrei.domain.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Integer> {

    Page<Project> findAllByPublished(boolean published, Pageable pageable);

}

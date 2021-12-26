package at.htlstp.felerfrei.persistence;

import at.htlstp.felerfrei.domain.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Integer> {
}

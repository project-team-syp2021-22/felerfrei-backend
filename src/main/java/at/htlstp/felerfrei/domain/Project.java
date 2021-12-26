package at.htlstp.felerfrei.domain;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "project")
public class Project {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "description", length = 1024)
    private String description;

    @Column(name = "title", length = 256)
    private String title;

    @Column(name = "published")
    private Boolean published;

    @ManyToMany
    @JoinTable(
            name="project_image",
            joinColumns = @JoinColumn(name="project_id"),
            inverseJoinColumns = @JoinColumn(name="image_id")
    )
    private List<Image> images;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return Objects.equals(id, project.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
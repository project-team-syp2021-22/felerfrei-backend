package at.htlstp.felerfrei.persistence;

import at.htlstp.felerfrei.domain.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<Image, Integer> {


    @Query("""
            select case when count(p) > 0
             then true
             else
             (select case when count (proj) > 0 then true else false end from Project proj where ?1 member of proj.images) 
             end
            from Product p where ?1 member of p.images
            """)
    Boolean imageStillInUse(Image image);

}

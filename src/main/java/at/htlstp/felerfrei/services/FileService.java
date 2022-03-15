package at.htlstp.felerfrei.services;

import at.htlstp.felerfrei.domain.Image;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface FileService {

    int save(MultipartFile file, String directory);

    void delete(int id);

    Optional<ByteArrayResource> get(int id);

    Optional<Image> getImage(int id);

}

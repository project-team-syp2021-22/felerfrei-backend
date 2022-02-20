package at.htlstp.felerfrei.services;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface ImageService {

    /**
     * Saves a given image to the filesystem.
     * @param file
     * @return id of the image
     */
    boolean saveImage(MultipartFile file, String directory);

    Optional<ByteArrayResource> getImage(String directory);

    void delete(String directory);

}

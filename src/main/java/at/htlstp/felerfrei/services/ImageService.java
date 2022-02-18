package at.htlstp.felerfrei.services;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ImageService {

    /**
     * Saves a given image to the filesystem.
     * @param file
     * @return id of the image
     */
    long saveImage(MultipartFile file);

    List<Long> saveImages(List<MultipartFile> files);

    ByteArrayResource getImage(long id) throws IOException;


}

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
    int saveImage(MultipartFile file, int id, String directory);

    List<Integer> saveImages(List<MultipartFile> files);

    ByteArrayResource getImage(int id, String directory) throws IOException;

    void delete(int id, String directory);

}

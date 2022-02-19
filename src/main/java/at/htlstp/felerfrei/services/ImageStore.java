package at.htlstp.felerfrei.services;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Repository("imageStore")
public class ImageStore implements ImageService{

    @Override
    public int saveImage(MultipartFile file, int id, String directory) {
        return 0;
    }

    @Override
    public List<Integer> saveImages(List<MultipartFile> files) {
        return null;
    }

    @Override
    public ByteArrayResource getImage(int id, String directory) throws IOException {
        return null;
    }

    @Override
    public void delete(int id, String directory) {

    }
}

package at.htlstp.felerfrei.services;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service("imageStore")
public class ImageStore implements ImageService{
    @Override
    public long saveImage(MultipartFile file) {
        return 0;
    }

    @Override
    public List<Long> saveImages(List<MultipartFile> files) {
        return null;
    }

    @Override
    public ByteArrayResource getImage(long id) throws IOException {
        return null;
    }
}

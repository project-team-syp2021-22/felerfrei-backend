package at.htlstp.felerfrei.services;

import at.htlstp.felerfrei.domain.Image;
import at.htlstp.felerfrei.persistence.ImageRepository;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service("imageLocationService")
public class ImageLocationService implements FileService {

    private static final String RESOURCE_DIR = ImageLocationService.class.getResource("/").getPath();

    private final ImageRepository repository;

    private final ImageService imageService;

    public ImageLocationService(ImageRepository repository, ImageService imageService) {
        this.repository = repository;
        this.imageService = imageService;
    }

    @Override
    public int save(MultipartFile file, String directory) {
        var saved = repository.save(new Image(null, RESOURCE_DIR + directory));
        saved.setPath(saved.getPath() + "/" + saved.getId() + ".png");
        repository.save(saved);

        var wasSavedToFilesystem = imageService.saveImage(file, saved.getPath());
        if (!wasSavedToFilesystem) {
            delete(saved.getId());
            throw new IllegalStateException("Could not save image to filesystem");
        }
        return saved.getId();
    }

    @Override
    public void delete(int id) throws NoSuchElementException {
        var found = repository.findById(id);
        repository.delete(found.orElseThrow(() -> new NoSuchElementException("Image not found")));
        imageService.delete(found.get().getPath());
    }

    @Override
    public Optional<ByteArrayResource> get(int id) {
        var found = repository.findById(id);
        if(found.isEmpty()) {
            return Optional.empty();
        }
        return imageService.getImage(found.get().getPath());
    }
}

package at.htlstp.felerfrei.services;

import at.htlstp.felerfrei.domain.Image;
import at.htlstp.felerfrei.persistence.ImageRepository;
import lombok.SneakyThrows;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service("imageLocationService")
public class ImageLocationService implements FileService {

    private static final String RESOURCE_DIR = "images";

    private final ImageRepository repository;

    private final ImageService imageService;

    public ImageLocationService(ImageRepository repository, ImageService imageService) {
        this.repository = repository;
        this.imageService = imageService;
    }

    @SneakyThrows
    @Override
    public int save(MultipartFile file, String directory) {
        var saved = repository.save(new Image(null, RESOURCE_DIR + directory));
        saved.setPath(saved.getPath() + "/" + saved.getId() + ".png");
        repository.save(saved);

        var path = saved.getPath();

        var wasSavedToFilesystem = imageService.saveImage(file, path);
        if (!wasSavedToFilesystem) {
            delete(saved.getId());
            throw new IllegalStateException("Could not save image to filesystem");
        }
        return saved.getId();
    }

    @Override
    public void delete(int id) throws NoSuchElementException {
        var found = repository.findById(id);
        if(found.isEmpty()) {
            throw new NoSuchElementException("Image not found");
        }
        repository.delete(found.get());
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

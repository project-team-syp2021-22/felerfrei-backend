package at.htlstp.felerfrei.services;

import lombok.SneakyThrows;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

@Repository("imageService")
public class ImageStore implements ImageService {

    @SneakyThrows
    @Override
    public boolean saveImage(MultipartFile file, String path) {
        var savedFile = new File(path).getAbsolutePath();
        try (var output = new DataOutputStream(new FileOutputStream(savedFile))) {
            output.write(file.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @SneakyThrows
    @Override
    public Optional<ByteArrayResource> getImage(String path) {
        var file = new File(path).getAbsoluteFile();
        if (Files.exists(file.toPath())) {
            return Optional.of(new ByteArrayResource(Files.readAllBytes(file.toPath())));
        }
        return Optional.empty();
    }

    @SneakyThrows
    @Override
    public void delete(String path) {
        var file = new File(path);
        if (Files.exists(file.toPath())) {
            Files.delete(file.toPath());
        }
    }
}

package at.htlstp.felerfrei.services;

import org.springframework.core.io.FileSystemResource;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface FileService {

    int save(MultipartFile file, String directory);

    void delete(int id);

    Optional<FileSystemResource> get(int id);

}

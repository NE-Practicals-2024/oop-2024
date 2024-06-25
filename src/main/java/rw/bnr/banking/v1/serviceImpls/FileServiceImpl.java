package rw.bnr.banking.v1.serviceImpls;

import rw.bnr.banking.v1.enums.EFileSizeType;
import rw.bnr.banking.v1.enums.EFileStatus;
import rw.bnr.banking.v1.exceptions.AppException;
import rw.bnr.banking.v1.exceptions.BadRequestException;
import rw.bnr.banking.v1.exceptions.ResourceNotFoundException;
import rw.bnr.banking.v1.models.File;
import rw.bnr.banking.v1.repositories.IFileRepository;
import rw.bnr.banking.v1.services.IFileService;
import rw.bnr.banking.v1.standalone.FileStorageService;
import rw.bnr.banking.v1.utils.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;
import java.util.UUID;

@Service
public class FileServiceImpl implements IFileService {
    private final IFileRepository fileRepository;
    private final FileStorageService fileStorageService;

    @Value("${uploads.extensions}")
    private String extensions;

    @Autowired
    public FileServiceImpl(IFileRepository fileRepository, FileStorageService fileStorageService) {
        this.fileRepository = fileRepository;
        this.fileStorageService = fileStorageService;
    }

    @Override
    public File getById(UUID id) {
        return this.fileRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("File", "id", id.toString()));
    }

    @Override
    public File create(MultipartFile document, String directory) {
        File file = new File();
        file.setStatus(EFileStatus.PENDING);
        String fileName = FileUtil.generateUUID(Objects.requireNonNull(document.getOriginalFilename()));
        String updatedFileName = this.handleFileName(fileName, UUID.randomUUID());
        EFileSizeType sizeType = FileUtil.getFileSizeTypeFromFileSize(file.getSize());
        int size = FileUtil.getFormattedFileSizeFromFileSize(document.getSize(), sizeType);

        file.setName(updatedFileName);
        file.setPath(fileStorageService.save(document, directory, updatedFileName));
        file.setStatus(EFileStatus.SAVED);
        file.setType(document.getContentType());
        file.setSize(size);
        file.setSizeType(sizeType);

        return this.fileRepository.save(file);
    }

    @Override
    public boolean delete(UUID id) {
        boolean exists = this.fileRepository.existsById(id);
        if (!exists)
            throw new ResourceNotFoundException("File", "id", id.toString());
        this.fileStorageService.removeFileOnDisk(this.getById(id).getPath());
        this.fileRepository.deleteById(id);
        return true;
    }

    @Override
    public String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex < 0) {
            return null;
        }
        return fileName.substring(dotIndex + 1);
    }

    @Override
    public String handleFileName(String fileName, UUID id) {

        String cleanFileName = fileName.replaceAll("[^A-Za-z0-9.()]", "");
        String extension = getFileExtension(cleanFileName);

        if (!isValidExtension(cleanFileName)) {
            throw new BadRequestException("Invalid File Extension");
        }

        String base = "image-" + id;

        cleanFileName = base + "." + extension;

        return cleanFileName;
    }

    @Override
    public boolean isValidExtension(String fileName) {
        String fileExtension = getFileExtension(fileName);

        if (fileExtension == null) {
            throw new AppException("No File Extension");
        }

        fileExtension = fileExtension.toLowerCase();

        for (String validExtension : extensions.split(",")) {
            if (fileExtension.equals(validExtension)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public File getByName(String filename) {
        return this.fileRepository.getFileByName(filename);
    }

}

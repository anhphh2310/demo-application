package com.fpt.application.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileHandlerService {

  private Path foundFile;

  public String saveFile(String fileName, MultipartFile file) throws IOException {
    Path uploadPath = Paths.get("Files-Upload");

    if (!Files.exists(uploadPath)) {
      Files.createDirectories(uploadPath);
    }

    try (InputStream inputStream = file.getInputStream()) {
      Path filePath = uploadPath.resolve(fileName);
      Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException ioe) {
      throw new IOException("Could not save file: " + fileName, ioe);
    }

    return fileName;
  }

  public Resource getFileAsResource(String fileName) throws IOException {
    Path dirPath = Paths.get("Files-Upload");

    Files.list(dirPath).forEach(file -> {
      if (file.getFileName().toString().startsWith(fileName)) {
        foundFile = file;
        return;
      }
    });

    if (foundFile != null) {
      return new UrlResource(foundFile.toUri());
    }

    return null;
  }
}

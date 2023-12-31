package com.fpt.application.controller;

import com.fpt.application.dto.FileUploadResponse;
import com.fpt.application.service.FileHandlerService;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
public class MainController {

  @Autowired
  private FileHandlerService service;

  @GetMapping(value = "/version")
  public String getVersion() {
    return "0.0.2";
  }

  @GetMapping(value = "/download-file/key/{key}/name/{file_name}")
  public ResponseEntity<?> download(@PathVariable(value = "key") String key, @PathVariable(value = "file_name") String fileName) {

    Resource resource = null;
    try {
      resource = service.getFileAsResource(key + "/" + fileName);
    } catch (IOException e) {
      return ResponseEntity.internalServerError().build();
    }

    if (resource == null) {
      return new ResponseEntity<>("File not found", HttpStatus.NOT_FOUND);
    }

    String contentType = "application/octet-stream";
    String headerValue = "attachment; filename=\"" + resource.getFilename() + "\"";

    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(contentType))
        .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
        .body(resource);
  }

  @PostMapping(value = "/upload-file")
  public ResponseEntity<FileUploadResponse> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
    String fileName = StringUtils.cleanPath(file.getOriginalFilename());
    long size = file.getSize();

    String fileCode = service.saveFile(fileName, file);

    FileUploadResponse response = new FileUploadResponse();
    response.setFileName(fileName);
    response.setSize(size);
    response.setKey(fileCode.split("/")[0]);

    return new ResponseEntity<>(response, HttpStatus.OK);
  }
}

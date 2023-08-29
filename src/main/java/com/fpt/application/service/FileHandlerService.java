package com.fpt.application.service;

import com.fpt.application.util.DataBucketUtil;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileHandlerService {

  private static final Logger log = LoggerFactory.getLogger(FileHandlerService.class);
  @Autowired
  private DataBucketUtil service;

  public String saveFile(String fileName, MultipartFile file) throws IOException {
    log.info("File: " + fileName + ", " + file.getContentType());
    return service.uploadFile(file, fileName, file.getContentType());
  }

  public Resource getFileAsResource(String fileName) throws IOException {

    return service.downloadFile(fileName);
  }
}

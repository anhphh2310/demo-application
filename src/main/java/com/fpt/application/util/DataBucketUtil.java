package com.fpt.application.util;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class DataBucketUtil {

  private static final Logger log = LoggerFactory.getLogger(DataBucketUtil.class);

  @Value("${gcp.config.file}")
  private String configFile;

  @Value("${gcp.config.project-id}")
  private String projectId;

  @Value("${gcp.config.bucket-id}")
  private String bucketId;

  public String uploadFile(MultipartFile file, String name, String contentType) {

    File tmp = null;
    try {
      tmp = convertFile(file);
      byte[] data = FileUtils.readFileToByteArray(convertFile(file));

      InputStream credentialStream = new ClassPathResource(configFile).getInputStream();
      GoogleCredentials credentials = ServiceAccountCredentials.fromStream(credentialStream);
      StorageOptions storageOptions = StorageOptions
          .newBuilder()
          .setCredentials(credentials)
          .build();

      Storage storage = storageOptions.getService();
      Bucket bucket = storage.get(bucketId, Storage.BucketGetOption.fields());
      String random = RandomUtils.nextInt(0,10000) + "";

      Blob blob = bucket.create(random + "/" + name, data, contentType);
      if (blob == null) {
        throw new RuntimeException("Error while upload gcp");
      }
      return random + "/" + name;
    } catch (Exception e) {
      log.error(e.getMessage());
      throw new RuntimeException("Error while upload gcp");
    } finally {
      tmp.delete();
    }

  }

  public Resource downloadFile(String key) throws IOException {
    InputStream credentialStream = new ClassPathResource(configFile).getInputStream();
    GoogleCredentials credentials = ServiceAccountCredentials.fromStream(credentialStream);
    StorageOptions storageOptions = StorageOptions
        .newBuilder()
        .setCredentials(credentials)
        .build();

    Storage storage = storageOptions.getService();
    Blob blob = storage.get(bucketId, key);
    ByteArrayResource resource = new ByteArrayResource(
        blob.getContent());
    return resource;
  }


  private File convertFile(MultipartFile file) {
    try {
      File convertFile = new File(file.getOriginalFilename());
      FileOutputStream outputStream = new FileOutputStream(convertFile);
      outputStream.write(file.getBytes());
      outputStream.close();
      return convertFile;
    } catch (IOException e) {
      log.error("Error while write file, " + e.getMessage());
      throw new RuntimeException("Error while read file");
    }
  }
}

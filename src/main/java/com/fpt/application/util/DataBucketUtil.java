package com.fpt.application.util;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.io.IOException;
import java.io.InputStream;
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

    try {

      InputStream credentialStream = new ClassPathResource(configFile).getInputStream();
      GoogleCredentials credentials = ServiceAccountCredentials.fromStream(credentialStream);
      StorageOptions storageOptions = StorageOptions
          .newBuilder()
          .setCredentials(credentials)
          .build();

      Storage storage = storageOptions.getService();
      Bucket bucket = storage.get(bucketId, Storage.BucketGetOption.fields());
      String random = RandomUtils.nextInt(0,10000) + "";

      Blob blob = bucket.create(random + "/" + name, file.getBytes(), contentType);
      if (blob == null) {
        throw new RuntimeException("Error while upload gcp");
      }
      return random + "/" + name;
    } catch (Exception e) {
      log.error(e.getMessage());
      throw new RuntimeException("Error while upload gcp");
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
}

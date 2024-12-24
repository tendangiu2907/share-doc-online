package com.share_doc.sharing_document.client.documentstorage;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobStorageException;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.common.sas.*;
import com.share_doc.sharing_document.dto.UploadDocumentResult;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AzureDocumentStorageClient implements DocumentStorageClient {
  private final BlobServiceClient blobServiceClient;

  public AzureDocumentStorageClient(BlobServiceClient blobServiceClient) {
    this.blobServiceClient = blobServiceClient;
  }

  @Override
  public UploadDocumentResult uploadDocument(
      String originalImageName, InputStream data, long length) {
    try {
      // Get BlobContainerClient to interact with the container
      BlobContainerClient blobContainerClient =
          blobServiceClient.getBlobContainerClient("documents-container");

      // Rename the file to a unique name
      String newDocumentName =
          UUID.randomUUID().toString()
              + originalImageName.substring(originalImageName.lastIndexOf("."));

      // Get the BlobClient to interact with the specified blob
      BlobClient blobClient = blobContainerClient.getBlobClient(newDocumentName);

      // Upload the file to the blob
      blobClient.upload(data,true);
      return new UploadDocumentResult(blobClient.getBlobUrl(), newDocumentName);
    } catch (BlobStorageException e) {
      throw new RuntimeException("Failed to upload document to storage blob", e);
    }
  }

  public String generateToken(String blobName) {
    // Get BlobContainerClient to interact with the container
    BlobContainerClient blobContainerClient =
            blobServiceClient.getBlobContainerClient("documents-container");

    // Get the BlobClient to interact with the specified blob
    BlobClient blobClient = blobContainerClient.getBlobClient(blobName);

    OffsetDateTime expiryTime = OffsetDateTime.now().plusMinutes(5);
    BlobSasPermission sasPermission = new BlobSasPermission()
            .setReadPermission(true)
            .setTagsPermission(true);

    BlobServiceSasSignatureValues sasSignatureValues = new BlobServiceSasSignatureValues(expiryTime, sasPermission)
//            .setStartTime(OffsetDateTime.now().minusMinutes(5))
            ;

    String sasToken = blobClient.generateSas(sasSignatureValues);
    return blobClient.getBlobUrl() + "?" + sasToken;
  }
}

package com.share_doc.sharing_document.client.documentstorage;

import com.share_doc.sharing_document.dto.UploadDocumentResult;
import java.io.InputStream;

public interface DocumentStorageClient {
  UploadDocumentResult uploadDocument(String originalImageName, InputStream data, long length)
      throws Exception;
  String generateToken(String blobName);
}

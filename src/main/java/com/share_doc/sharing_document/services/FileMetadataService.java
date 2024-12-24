package com.share_doc.sharing_document.services;

import com.share_doc.sharing_document.entity.FileMetadata;
import com.share_doc.sharing_document.entity.User;
import com.share_doc.sharing_document.repository.FileMetadataRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FileMetadataService {
  private final FileMetadataRepository fileMetadataRepository;

  @Autowired
  public FileMetadataService(FileMetadataRepository fileMetadataRepository) {
    this.fileMetadataRepository = fileMetadataRepository;
  }

  public List<FileMetadata> getDocumentsByNameAndCategory(
      String documentName, String categoryName) {
    return fileMetadataRepository
        .findFileMetadataByOriginalFileNameContainingAndDocumentCategory_DocumentCategoryNameContaining(
            documentName, categoryName);
  }

  public FileMetadata addNew(FileMetadata fileMetadata) {
    return fileMetadataRepository.save(fileMetadata);
  }

  public List<FileMetadata> getDocumentsByUser(User user) {
    return fileMetadataRepository.findFileMetadataByUploader(user);
  }

  public FileMetadata getDocumentById(Integer id) {
    return fileMetadataRepository
        .findById(id)
        .orElseThrow(() -> new RuntimeException("Not found document for this Id"));
  }
}

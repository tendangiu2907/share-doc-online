package com.share_doc.sharing_document.repository;

import com.share_doc.sharing_document.entity.FileMetadata;
import com.share_doc.sharing_document.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Integer> {
  List<FileMetadata>
      findFileMetadataByOriginalFileNameContainingAndDocumentCategory_DocumentCategoryNameContaining(
          String originalFileName, String documentCategory_documentCategoryName);

  List<FileMetadata> findFileMetadataByUploader(User user);

  Optional<FileMetadata> findById(Integer id);
}

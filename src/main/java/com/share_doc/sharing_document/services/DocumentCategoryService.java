package com.share_doc.sharing_document.services;

import com.share_doc.sharing_document.entity.DocumentCategory;
import com.share_doc.sharing_document.repository.DocumentCategoryRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.webjars.NotFoundException;

@Service
public class DocumentCategoryService {
  private final DocumentCategoryRepository documentCategoryRepository;

  public DocumentCategoryService(DocumentCategoryRepository documentCategoryRepository) {
    this.documentCategoryRepository = documentCategoryRepository;
  }

  public List<DocumentCategory> getAll() {
    return documentCategoryRepository.findAll();
  }

  public DocumentCategory addNew(DocumentCategory documentCategory) {
    return documentCategoryRepository.save(documentCategory);
  }

  public DocumentCategory getById(Integer categoryId) {
    return documentCategoryRepository
        .findById(categoryId)
        .orElseThrow(() -> new NotFoundException("Not found document category"));
  }
}

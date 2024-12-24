package com.share_doc.sharing_document.repository;

import com.share_doc.sharing_document.entity.DownloadHistory;
import com.share_doc.sharing_document.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DownloadHistoryRepository extends JpaRepository<DownloadHistory, Integer> {}

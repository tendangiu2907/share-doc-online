package com.share_doc.sharing_document.repository;

import com.share_doc.sharing_document.entity.DownloadHistory;
import com.share_doc.sharing_document.entity.FileMetadata;
import com.share_doc.sharing_document.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DownloadHistoryRepository extends JpaRepository<DownloadHistory, Integer> {
  List<DownloadHistory> getDownloadHistoriesByOwnerFile(User ownerFile);

  List<DownloadHistory> getDownloadHistoriesByDownloader(User downloader);

  Optional<DownloadHistory> findByDownloadedFileAndOwnerFileAndDownloader(
      FileMetadata downloadedFile, User ownerFile, User downloader);
}

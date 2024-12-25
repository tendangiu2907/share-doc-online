package com.share_doc.sharing_document.services;

import com.share_doc.sharing_document.entity.DownloadHistory;
import com.share_doc.sharing_document.entity.FileMetadata;
import com.share_doc.sharing_document.entity.User;
import com.share_doc.sharing_document.repository.DownloadHistoryRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class DownloadHistoryService {
  private final DownloadHistoryRepository downloadHistoryRepository;

  public DownloadHistoryService(DownloadHistoryRepository downloadHistoryRepository) {
    this.downloadHistoryRepository = downloadHistoryRepository;
  }

  public DownloadHistory addNew(DownloadHistory downloadHistory) {
    return downloadHistoryRepository.save(downloadHistory);
  }

  public List<DownloadHistory> getByOwnerFile(User ownerFile) {
    return downloadHistoryRepository.getDownloadHistoriesByOwnerFile(ownerFile);
  }

  public List<DownloadHistory> getByDownloader(User downloader) {
    return downloadHistoryRepository.getDownloadHistoriesByDownloader(downloader);
  }

  public Optional<DownloadHistory> findByDocumentAndOwnerFileAndDownloader(
      FileMetadata downloadedFile, User ownerFile, User downloader) {
    return downloadHistoryRepository.findByDownloadedFileAndOwnerFileAndDownloader(
        downloadedFile, ownerFile, downloader);
  }
}

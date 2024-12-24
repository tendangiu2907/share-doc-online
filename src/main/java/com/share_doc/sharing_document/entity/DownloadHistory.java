package com.share_doc.sharing_document.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "download_history")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DownloadHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_file_id")
    public User ownerFile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "downloader_id")
    public User downloader;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "downloaded_file_id")
    public FileMetadata downloadedFile;

    public LocalDateTime downloadDate;
}

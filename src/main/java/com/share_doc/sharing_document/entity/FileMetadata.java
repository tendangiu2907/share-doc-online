package com.share_doc.sharing_document.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Entity
@Table(name = "file_metadata")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileMetadata {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Integer id;

  public String originalFileName;

  public String blobName;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "uploaded_by")
  public User uploader;

  public LocalDateTime uploadDate;

  public Float size;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "document_category_id")
  public DocumentCategory documentCategory;

  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "downloadedFile")
  private List<DownloadHistory> downloadHistories = new ArrayList<>();
}

package com.share_doc.sharing_document.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer id;

    public String username;

    @NotEmpty
    public String password;

    @Column(unique = true)
    public String email;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "uploader")
    private List<FileMetadata> fileMetadataList = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "ownerFile")
    private List<DownloadHistory> downloadDetailFiles = new ArrayList<>(); // Details of who has downloaded your files

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "downloader")
    private List<DownloadHistory> yourDownloads = new ArrayList<>(); // Files you have downloaded
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}

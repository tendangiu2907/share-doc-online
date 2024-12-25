package com.share_doc.sharing_document.controller;

import com.share_doc.sharing_document.client.documentstorage.DocumentStorageClient;
import com.share_doc.sharing_document.dto.Result;
import com.share_doc.sharing_document.dto.UploadDocumentResult;
import com.share_doc.sharing_document.dto.WhoDownloadedYourDoc;
import com.share_doc.sharing_document.entity.DocumentCategory;
import com.share_doc.sharing_document.entity.DownloadHistory;
import com.share_doc.sharing_document.entity.FileMetadata;
import com.share_doc.sharing_document.entity.User;
import com.share_doc.sharing_document.services.DocumentCategoryService;
import com.share_doc.sharing_document.services.DownloadHistoryService;
import com.share_doc.sharing_document.services.FileMetadataService;
import com.share_doc.sharing_document.services.UserService;
import com.share_doc.sharing_document.util.CustomUserDetails;
import com.share_doc.sharing_document.util.PdfRender;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class DocumentActivityController {
  private final FileMetadataService fileMetadataService;
  private final DocumentCategoryService documentCategoryService;
  private final UserService userService;
  private final DocumentStorageClient documentStorageClient;
  private final DownloadHistoryService downloadHistoryService;

  @Autowired
  public DocumentActivityController(
      FileMetadataService fileMetadataService,
      DocumentCategoryService documentCategoryService,
      UserService userService,
      DocumentStorageClient documentStorageClient,
      DownloadHistoryService downloadHistoryService) {
    this.fileMetadataService = fileMetadataService;
    this.documentCategoryService = documentCategoryService;
    this.userService = userService;
    this.documentStorageClient = documentStorageClient;
    this.downloadHistoryService = downloadHistoryService;
  }

  @GetMapping("/dashboard/")
  public String dashboard(Model model) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (!(authentication instanceof AnonymousAuthenticationToken)) {
      String currentUsername = authentication.getName();
      model.addAttribute("username", currentUsername);
    }
    return "dashboard";
  }

  @GetMapping("/upload-document")
  public String uploadDocument(Model model) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (!(authentication instanceof AnonymousAuthenticationToken)) {
      String currentUsername = authentication.getName();
      model.addAttribute("username", currentUsername);
    }
    List<DocumentCategory> documentCategories = documentCategoryService.getAll();
    model.addAttribute("documentCategories", documentCategories);
    return "upload-document";
  }

  @PostMapping("/add-new-document")
  public String addNewDocument(
      @RequestParam(name = "documentName") String documentName,
      @RequestParam(name = "categoryId", required = false) Integer categoryId,
      @RequestParam(name = "newDocumentCategoryName", required = false)
          String newDocumentCategoryName,
      @RequestParam(name = "documentUrl") String url,
      @RequestParam(name = "fileSize") Float fileSize,
      Model model) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User user = null;
    if (!(authentication instanceof AnonymousAuthenticationToken)) {
      String currentUsername = authentication.getName();
      user = ((CustomUserDetails) authentication.getPrincipal()).getUser();
      model.addAttribute("username", currentUsername);
    }
    DocumentCategory documentCategory;
    if (newDocumentCategoryName != null && !newDocumentCategoryName.isEmpty()) {
      DocumentCategory newDocumentCategory = new DocumentCategory();
      newDocumentCategory.setDocumentCategoryName(newDocumentCategoryName);
      documentCategory = documentCategoryService.addNew(newDocumentCategory);
    } else {
      documentCategory = documentCategoryService.getById(categoryId);
    }

    FileMetadata document = new FileMetadata();
    document.setDocumentCategory(documentCategory);
    document.setOriginalFileName(documentName);
    document.setBlobName(url);
    document.setSize(fileSize);
    document.setUploader(user);
    document.setUploadDate(LocalDateTime.now());

    fileMetadataService.addNew(document);

    return "redirect:/upload-document";
  }

  @PostMapping("/upload-to-storage-account")
  public ResponseEntity<Result> uploadToStorageAccount( // update convert to word later, use
      @RequestParam(name = "documentFile") MultipartFile documentFile) throws Exception {
    try {
      // Kiểm tra định dạng file
      String fileName = documentFile.getOriginalFilename();
      if (fileName == null || fileName.isEmpty()) {
        throw new IllegalArgumentException("Filename is invalid.");
      }
      String fileExtension = PdfRender.getFileExtension(fileName).toLowerCase();

      InputStream inputStream;

      if ("docx".equals(fileExtension)) {
        // Chuyển đổi .docx sang PDF
        inputStream = PdfRender.convertDocxToPdf(documentFile);
        fileName = fileName.replaceAll("\\.docx$", ".pdf"); // Thay đổi đuôi file thành .pdf
      } else if ("doc".equals(fileExtension)) {
        // Chuyển đổi .doc sang PDF
        inputStream = PdfRender.convertDocToPdf(documentFile);
        fileName = fileName.replaceAll("\\.doc$", ".pdf"); // Thay đổi đuôi file thành .pdf
      } else {
        inputStream = documentFile.getInputStream();
      }

      UploadDocumentResult uploadDocumentResult =
          documentStorageClient.uploadDocument(fileName, inputStream, documentFile.getSize());

      return new ResponseEntity<>(
          new Result(true, "Upload to storage account succeeded", uploadDocumentResult),
          HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(
          new Result(
              false, e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : null),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @GetMapping("/search")
  public String searchDocument(
      @RequestParam(name = "document", required = false) String documentName,
      @RequestParam(name = "category", required = false) String categoryName,
      Model model) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (!(authentication instanceof AnonymousAuthenticationToken)) {
      String currentUsername = authentication.getName();
      model.addAttribute("username", currentUsername);
    }
    List<FileMetadata> documents =
        fileMetadataService.getDocumentsByNameAndCategory(documentName, categoryName);

    model.addAttribute("document", documents);
    model.addAttribute("documentName", documentName);
    model.addAttribute("categoryName", categoryName);

    return "dashboard";
  }

  @GetMapping("/view-your-documents")
  public String viewDocumentByUser(Model model) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User user = null;
    if (!(authentication instanceof AnonymousAuthenticationToken)) {
      String currentUsername = authentication.getName();
      user = ((CustomUserDetails) authentication.getPrincipal()).getUser();
      model.addAttribute("username", currentUsername);
    }

    List<FileMetadata> documents = fileMetadataService.getDocumentsByUser(user);
    model.addAttribute("document", documents);
    return "your-document";
  }

  @GetMapping("/document-detail/{id}")
  public String documentDetail(@PathVariable(name = "id") Integer id, Model model) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User user = null;
    if (!(authentication instanceof AnonymousAuthenticationToken)) {
      String currentUsername = authentication.getName();
      user = ((CustomUserDetails) authentication.getPrincipal()).getUser();
      model.addAttribute("username", currentUsername);
    }

    FileMetadata document = fileMetadataService.getDocumentById(id);
    String isOwner = document.getUploader().getEmail().equals(user.getEmail()) ? "Yes" : "No";
    model.addAttribute("document", document);
    model.addAttribute("isOwner", isOwner);

    return "document-detail";
  }

  @GetMapping("/pdfjs-download/{id}")
  public ResponseEntity<Result> downloadFile1(@PathVariable Integer id) {
    try {
      FileMetadata document = fileMetadataService.getDocumentById(id);
      String blobUrl = document.getBlobName();
      String blobName = blobUrl.substring(blobUrl.lastIndexOf('/') + 1);
      String sasUrl = documentStorageClient.generateToken(blobName);
      UploadDocumentResult uploadDocumentResult = new UploadDocumentResult(null, sasUrl);
      return new ResponseEntity<>(
          new Result(true, "Generate sas token successes", uploadDocumentResult), HttpStatus.OK);
    } catch (Exception e) {
      throw new RuntimeException("Không thể tải file", e);
    }
  }

  @GetMapping("/download/{id}")
  public ResponseEntity<Result> getUrlDownloadForClient(@PathVariable Integer id) {
    try {
      FileMetadata document = fileMetadataService.getDocumentById(id);
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      User user = null;
      if (!(authentication instanceof AnonymousAuthenticationToken)) {
        String currentUsername = authentication.getName();
        user = ((CustomUserDetails) authentication.getPrincipal()).getUser();
      }
      if (user != null) {
        List<FileMetadata> documentsByUser = fileMetadataService.getDocumentsByUser(user);
        Integer countDocumentsOfUser = documentsByUser.size();
        if (countDocumentsOfUser < 2) {
          return new ResponseEntity<>(
              new Result(false, "You need to upload at least 2 document to download", null),
              HttpStatus.NOT_ACCEPTABLE);
        }

        FileMetadata newestDocument =
            documentsByUser.stream()
                .max(Comparator.comparing(FileMetadata::getUploadDate))
                .orElseThrow(
                    () -> new RuntimeException("Not found file metadata for this user in list"));

        if (newestDocument.getUploadDate().isBefore(LocalDateTime.now().minusDays(15))) {
          return new ResponseEntity<>(
              new Result(false, "You need to upload new one document", null),
              HttpStatus.NOT_ACCEPTABLE);
        }
      }

      // add to download history
      User ownerFile = document.getUploader();
      User downloader = user;

      if (downloadHistoryService
          .findByDocumentAndOwnerFileAndDownloader(document, ownerFile, downloader)
          .isEmpty()) {
        DownloadHistory downloadHistory =
            DownloadHistory.builder()
                .downloader(downloader)
                .downloadedFile(document)
                .ownerFile(ownerFile)
                .downloadDate(LocalDateTime.now())
                .build();
        downloadHistoryService.addNew(downloadHistory);
      }

      String blobUrl = document.getBlobName();
      String blobName = blobUrl.substring(blobUrl.lastIndexOf('/') + 1);
      String sasUrl = documentStorageClient.generateToken(blobName);
      UploadDocumentResult uploadDocumentResult = new UploadDocumentResult(sasUrl, null);
      return new ResponseEntity<>(
          new Result(true, "Get url download successes", uploadDocumentResult), HttpStatus.OK);
    } catch (Exception e) {
      throw new RuntimeException("Can not download", e);
    }
  }

  @GetMapping("/your-downloaded-documents")
  public String yourDownloadedDocuments(Model model) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User user = null;
    if (!(authentication instanceof AnonymousAuthenticationToken)) {
      String currentUsername = authentication.getName();
      user = ((CustomUserDetails) authentication.getPrincipal()).getUser();
      model.addAttribute("username", currentUsername);
    }

    List<DownloadHistory> downloadHistories = downloadHistoryService.getByDownloader(user);

    model.addAttribute("downloadHistories", downloadHistories);
    return "your-downloaded-documents";
  }

  @GetMapping("/who-downloaded-your-documents")
  public String whoDownloadedYourDocuments(Model model) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User user = null;
    if (!(authentication instanceof AnonymousAuthenticationToken)) {
      String currentUsername = authentication.getName();
      user = ((CustomUserDetails) authentication.getPrincipal()).getUser();
      model.addAttribute("username", currentUsername);
    }

    // Lấy danh sách lịch sử tải và group theo file
    List<DownloadHistory> downloadHistories = downloadHistoryService.getByOwnerFile(user);
    Map<Integer, List<DownloadHistory>> groupedByFile =
        downloadHistories.stream()
            .collect(Collectors.groupingBy(h -> h.getDownloadedFile().getId()));

    // Tạo danh sách WhoDownloadedYourDoc
    List<WhoDownloadedYourDoc> whoDownloadedYourDocs =
        groupedByFile.entrySet().stream()
            .map(
                entry -> {
                  Integer fileId = entry.getKey();
                  List<DownloadHistory> histories = entry.getValue();
                  FileMetadata file = histories.get(0).getDownloadedFile();
                  List<User> downloaders =
                      histories.stream()
                          .map(DownloadHistory::getDownloader)
                          .collect(Collectors.toList());
                  User owner = histories.get(0).getOwnerFile();
                  return new WhoDownloadedYourDoc(
                      fileId, file.getOriginalFileName(), downloaders, owner);
                })
            .collect(Collectors.toList());

    model.addAttribute("whoDownloadedYourDocs", whoDownloadedYourDocs);
    return "who-downloaded-your-documents";
  }
}

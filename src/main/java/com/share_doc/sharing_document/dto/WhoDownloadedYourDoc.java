package com.share_doc.sharing_document.dto;

import com.share_doc.sharing_document.entity.User;
import java.util.List;

public record WhoDownloadedYourDoc(
    Integer fileId, String fileName, List<User> downloaders, User ownerFile) {}

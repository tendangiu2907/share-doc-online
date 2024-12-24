const fileInput = document.querySelector(".file-input"),
    uploadedArea = document.querySelector(".uploaded-area"),
    uploadProgressContainer = document.querySelector("#uploadProgressContainer"),
    progressBar = document.querySelector("#uploadProgress"),
    uploadStatus = document.querySelector("#uploadStatus"),
    warpper = document.querySelector(".wrapper");

// Trigger file selection
document.querySelector(".custom-form").addEventListener("click", () => {
    fileInput.click();
});

// File selection handler
fileInput.onchange = async ({ target }) => {
    let file = target.files[0];
    if (file) {
        const fileSizeMB = file.size / (1024 * 1024);
        const allowedTypes = ['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'];

        if (fileSizeMB < 1) {
            alert('File size must be at least 1MB.');
            fileInput.value = '';
            return;
        } else if (!allowedTypes.includes(file.type)) {
            alert('Only allow .doc, .docx, hoặc .pdf.');
            fileInput.value = '';
            return;
        }
        let fileName = file.name;
        await uploadFile(file, fileName);
    }
};

// Upload file with progress
async function uploadFile(file, name) {
    uploadProgressContainer.style.display = 'block';
    uploadStatus.textContent = 'Đang tải lên...';
    progressBar.value = 0;

    const url = "/upload-to-storage-account"; // API endpoint
    const formData = new FormData();
    formData.append("documentFile", file);

    try {
        // Giả lập tiến trình upload
        const simulateProgress = setInterval(() => {
            if (progressBar.value < 99) {
                progressBar.value += 1; // Tăng 1% mỗi lần
                uploadStatus.textContent = `Đang tải lên: ${progressBar.value}%`;
            }
        }, 100); // Mỗi 30ms tăng 1%

        const response = await fetch(url, {
            method: "POST",
            body: formData,
        });

        clearInterval(simulateProgress); // Dừng tiến trình giả lập

        if (!response.ok) {
            throw new Error("Upload failed");
        }

        const result = await response.json(); // Nhận kết quả JSON từ server
        console.log(result)
        document.querySelector("#documentUrl").value = result.data.url; // Cập nhật URL trả về vào input hidden

        let fileSize = (file.size / (1024 * 1024)).toFixed(2) + " MB";
        document.querySelector("#fileSize").value = (file.size / (1024 * 1024)).toFixed(2)
        let uploadedHTML = 
        `<li class="row">
            <div class="d-flex align-items-center justify-content-between w-100 upload-item">
                <div class="d-flex align-items-center">
                    <i class="fas fa-file-alt me-2"></i>
                    <div>
                        <span class="d-block">${name} • Uploaded</span>
                        <small class="text-muted">${fileSize}</small>
                    </div>
                </div>
                <i class="fas fa-trash-alt btn-delete text-danger ms-3 btn-delete-uploaded-doc" onclick="resetUpload()" title="Xóa"></i>
            </div>
        </li>`;


        uploadStatus.textContent = 'Tải lên thành công!';
        progressBar.value = 100;
        uploadedArea.insertAdjacentHTML("afterbegin", uploadedHTML);
    } catch (error) {
        console.error(error);
        uploadStatus.textContent = 'Lỗi khi tải lên. Vui lòng thử lại!';
    } finally {
        warpper.style.display = 'None';
        setTimeout(() => {
            uploadProgressContainer.style.display = 'none';
        }, 3000);
    }
}

function resetUpload() {
    uploadedArea.innerHTML = ''; // Xóa danh sách các file đã upload
    warpper.style.display = 'block'; // Hiển thị lại form upload
    fileInput.value = ''; // Xóa giá trị của input file
}

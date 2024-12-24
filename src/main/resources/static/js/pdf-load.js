const zoomButton = document.getElementById('zoom');
const input = document.getElementById('inputFile');
const openFile = document.getElementById('openPDF');
const currentPage = document.getElementById('current_page');
const viewer = document.querySelector('.pdf-viewer');
const isOwner = document.querySelector('.isOwner');
let currentPDF = {}

function resetCurrentPDF() {
	currentPDF = {
		file: null,
		countOfPages: 0,
		currentPage: 1,
		zoom: 1
	}
}

document.addEventListener("DOMContentLoaded", function () {
	const currentUrl = window.location.pathname;
	const segments = currentUrl.split("/");
	const documentId = segments[segments.length - 1] || 1;
	showLoading();
	loadPDF(`/pdfjs-download/${documentId}`);
	zoomButton.disabled = false;
});

zoomButton.addEventListener('input', () => {
	if (currentPDF.file) {
		document.getElementById('zoomValue').innerHTML = zoomButton.value + "%";
		currentPDF.zoom = parseInt(zoomButton.value) / 100;
		renderCurrentPage();
	}
});

document.getElementById('next').addEventListener('click', () => {
	let isValidPage = false;
	const isOwnerValue = isOwner.textContent.trim();
	if (isOwnerValue == "Yes") {
		isValidPage = true;
	} else {
		isValidPage = currentPDF.currentPage < currentPDF.countOfPages * 0.1;
	}

	if (isValidPage) {
		currentPDF.currentPage += 1;
		renderCurrentPage();
	} else {
		alert("You need to download this document to view all")
	}
});

document.getElementById('previous').addEventListener('click', () => {
	const isValidPage = currentPDF.currentPage - 1 > 0;
	if (isValidPage) {
		currentPDF.currentPage -= 1;
		renderCurrentPage();
	}
});

async function loadPDF(data) {
	// URL của API bạn muốn gọi
	const url = "https://api.example.com/resource";

	try {
		// Gửi GET request để lấy dữ liệu
		const response = await fetch(data, {
			method: "GET",
		});

		// Kiểm tra trạng thái phản hồi
		if (!response.ok) {
			throw new Error(`HTTP error! Status: ${response.status}`);
		}

		// Chuyển đổi phản hồi sang JSON
		const result = await response.json();
		console.log("Dữ liệu nhận được:", result);

		// Kiểm tra xem `blobName` có tồn tại hay không
		if (!result.data || !result.data.blobName) {
			throw new Error("Không tìm thấy blobName trong phản hồi");
		}

		// Tải tệp PDF từ blobName
		const pdfFile = await pdfjsLib.getDocument(result.data.blobName).promise;

		// Reset trạng thái hiện tại của PDF
		resetCurrentPDF();

		// Cập nhật thông tin PDF
		currentPDF.file = pdfFile;
		currentPDF.countOfPages = pdfFile.numPages;

		// Hiển thị viewer và render trang
		viewer.classList.remove('hidden');
		hideLoading();
		renderCurrentPage();

	} catch (error) {
		console.error("Đã xảy ra lỗi:", error);
	}
}


function renderCurrentPage() {
	currentPDF.file.getPage(currentPDF.currentPage).then((page) => {
		var context = viewer.getContext('2d');
		var viewport = page.getViewport({ scale: currentPDF.zoom, });
		viewer.height = viewport.height;
		viewer.width = viewport.width;

		var renderContext = {
			canvasContext: context,
			viewport: viewport
		};
		page.render(renderContext);
	});
	currentPage.innerHTML = currentPDF.currentPage + ' of ' + currentPDF.countOfPages;
}

function showLoading() {
	console.log("Show loading");
	const loadingOverlay = document.getElementById('loadingOverlay');
	loadingOverlay.classList.remove('hidden');
}

function hideLoading() {
	console.log("Hide loading");
	const loadingOverlay = document.getElementById('loadingOverlay');
	loadingOverlay.classList.add('hidden');
}


/**
 * download from azure
 */
document.getElementById('downloadButton').addEventListener('click', function () {
	const documentId = this.getAttribute('data-document-id');

	fetch(`/download/${documentId}`)
		.then(response => {
			if (!response.ok) {
				return response.json().then(error => {
					alert(error.message); // Hiển thị lỗi nếu có
					throw new Error(error.message);
				});
			}
			return response.json(); // Lấy link từ backend
		})
		.then(data => {
			const downloadUrl = data.data.url;
			window.open(downloadUrl, '_blank'); // Mở link tải trực tiếp từ Azure
		})
		.catch(error => console.error('Error:', error));
});


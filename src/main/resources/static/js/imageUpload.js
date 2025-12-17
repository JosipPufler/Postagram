import { loadImage } from './imageEditor.js'

let cropper;

function openPostModal(img) {
    const postId = img.getAttribute("data-id");
    let src = `/rest/public/post/${postId}/image`;
    document.getElementById("postModalImage").src = src;
    loadImage(src)

    document.getElementById("postModalTitle").textContent = "Post by " + img.dataset.author
    document.getElementById("postModalDescription").textContent = img.dataset.description
    const date = new Date(img.dataset.date);
    const month = date.toLocaleString('default', { month: 'long' });
    document.getElementById("postModalDate").textContent = date.getHours() + ":" + date.getMinutes() + ", " + String(date.getDate() + 1).padStart(2, "0") + " " + month + " " + date.getFullYear()
    document.getElementById("postModalHashtags").textContent = img.dataset.hashtags

    $('#postModal').modal('show')
}

function initSelect2() {
    $('#format').select2({
        width: '100%',
        dropdownParent: $('#createPostModal'),
        minimumResultsForSearch: Infinity
    });

    $('#hashtags').select2({
        width: '100%',
        dropdownParent: $('#createPostModal'),
        tags: true,
        createTag: function (params) {
            let text = params.term.trim();
            if (!text.startsWith("#")) text = "#" + text;
            return { id: text, text: text, newTag: true };
        }
    });
}

$(document).ready(function() {
    initSelect2()
});

document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll(".post-tile").forEach(tile => {
        tile.addEventListener("click", () => openPostModal(tile));
        console.log(tile)
    });
});

document.getElementById("imageInput").addEventListener("change", function (e) {
    const file = e.target.files[0];
    if (!file) return;

    const img = document.getElementById("editorImage");
    img.src = URL.createObjectURL(file);
    img.style.display = "block";

    img.onload = function () {
        setTimeout(() => {
            $('#format').select2('destroy');
            $('#hashtags').select2('destroy');
            initSelect2();

            const modalEl = document.getElementById('createPostModal');
            const modal = bootstrap.Modal.getInstance(modalEl);
            if (modal) {
                modal.handleUpdate();
            }
        }, 100);
    };

    if (cropper) cropper.destroy();
    cropper = new Cropper(img, {
        aspectRatio: NaN,
        viewMode: 1,
        autoCropArea: 1,
        zoomable: false,
        outlined: true,
    });

    document.getElementById("formatGroup").style.display = "block"

    const type = file.type;

    let normalized = null;
    console.log(type)
    if (type === "image/jpeg") {
        normalized = "JPEG";
    } else if (type === "image/png") {
        normalized = "PNG";
    } else if (type === "image/bmp") {
        normalized = "BMP";
    } else {
        alert("Unsupported image format");
        this.value = "";
        return;
    }

    $('#format').val(normalized).trigger('change');


});

document.querySelector("form").addEventListener("submit", function (e) {
    if (!cropper) return;

    e.preventDefault();

    cropper.getCroppedCanvas().toBlob(blob => {
        const fileInput = document.getElementById("imageInput");

        const file = new File([blob], "image."+$("#format").select2('val'), { type: blob.type });
        const dataTransfer = new DataTransfer();
        dataTransfer.items.add(file);
        fileInput.files = dataTransfer.files;

        e.target.submit();
    }, "image/"+$("#format").select2('val'), 0.9);
});
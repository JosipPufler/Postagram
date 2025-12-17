let cropper;

function openEditor(img) {
    const postId = img.getAttribute("data-id");
    let src = `/rest/public/post/${postId}/image`;
    let contentType
    fetch(src)
        .then(res => {
            contentType = res.headers.get("Content-Type");
            console.log(contentType);
            return res.blob();
        })
        .then(blob => {
            const img = document.getElementById("editorImage");
            img.src = URL.createObjectURL(blob);
            img.style.display = "block";

            handleImageUpload(img, contentType)
        });

    document.getElementById("postId").value = img.dataset.id;
    document.getElementById("description").textContent = img.dataset.description;

    const raw = img.getAttribute("data-hashtags");
    const hashtags = raw
        .replace(/[\[\]]/g, "")
        .split(",")
        .map(s => s.trim());
    console.log(hashtags)

    $("#hashtags")
        .val(hashtags)
        .trigger("change");

    $('#createPostModal').modal('show')
}

function handleImageUpload(img, type){
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

            if (cropper) cropper.destroy();
            cropper = new Cropper(img, {
                aspectRatio: NaN,
                viewMode: 1,
                autoCropArea: 1,
                zoomable: false,
                outlined: true,
            });

            document.getElementById("formatGroup").style.display = "block"

            let normalized = null;
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
        }, 100);
    };
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
        tile.addEventListener("click", () => openEditor(tile));
    });
});

document.getElementById("imageInput").addEventListener("change", function (e) {
    const file = e.target.files[0];
    if (!file) return;

    const img = document.getElementById("editorImage");
    img.src = URL.createObjectURL(file);
    img.style.display = "block";

    handleImageUpload(img, file.type)
});

document.querySelector("#postForm").addEventListener("submit", function (e) {
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
import { loadImage } from './imageEditor.js'
import {publishPostClosedEvent, publishPostOpenEvent, getCookie} from './listener.js'

const createPostModalSelector = '#createPostModal'
const postModalSelector = '#postModal'
const formatSelector = '#format'
const hashtagsSelector = '#hashtags'
const commentInput = document.getElementById("commentInput");
const commentsList = document.getElementById("commentsList");
const submitCommentBtn = document.getElementById("submitCommentBtn");
let cropper;
let openedAt
let postId

$(postModalSelector)
    .on('shown.bs.modal', function () {
        openedAt = Date.now();
    })
    .on('hidden.bs.modal', function () {
        const durationMs = Date.now() - openedAt;
        publishPostClosedEvent(postId, durationMs/1000)
    });

function openPostModal(img) {
    postId = img.getAttribute("data-id");
    let src = `/rest/public/post/${postId}/image`;
    document.getElementById("postModalImage").src = src;
    loadImage(src)

    document.getElementById("postModalTitle").textContent = "Post by " + img.dataset.author
    document.getElementById("postModalDescription").textContent = img.dataset.description
    const date = new Date(img.dataset.date);
    const month = date.toLocaleString('default', { month: 'long' });
    document.getElementById("postModalDate").textContent = date.getHours() + ":" +  String(date.getMinutes()).padStart(2, "0") + ", " + String(date.getDate() + 1).padStart(2, "0") + " " + month + " " + date.getFullYear()
    document.getElementById("postModalHashtags").textContent = img.dataset.hashtags
    loadComments(postId)

    $(postModalSelector).modal('show')
    publishPostOpenEvent(postId)
}

function initSelect2() {
    $('#format').select2({
        width: '100%',
        dropdownParent: $(createPostModalSelector),
        minimumResultsForSearch: Infinity
    });

    $(hashtagsSelector).select2({
        width: '100%',
        dropdownParent: $(createPostModalSelector),
        tags: true,
        createTag: function (params) {
            let text = params.term.trim();
            if (!text.startsWith("#")) text = "#" + text;
            return { id: text, text: text, newTag: true };
        }
    });
}

function loadComments(postId) {
    fetch(`/rest/public/comment/${postId}`, {
        headers: {
            "Authorization": "Bearer " + getCookie(),
            "Content-Type": "application/json"
        },
    })
        .then(res => res.json())
        .then(comments => {
            // latest first
            console.log(comments);
            comments.sort((a, b) =>
                new Date(b.postedAt) - new Date(a.postedAt)
            );

            renderComments(comments);
        });
}

function renderComments(comments) {
    commentsList.innerHTML = "";

    if (!comments.length) {
        commentsList.innerHTML = "<p class='text-muted'>No comments yet.</p>";
        return;
    }

    comments.forEach(c => {
        const date = new Date(c.postedAt);

        const el = document.createElement("div");
        el.className = "border rounded p-2 mb-2 text-start";

        el.innerHTML = `
            <div class="d-flex justify-content-between">
                <strong>${c.username}</strong>
                <small class="text-muted">
                    ${date.toLocaleString()}
                </small>
            </div>
            <div>${c.content}</div>
        `;

        commentsList.appendChild(el);
    });
}

function postComment(postId) {
    const content = commentInput.value.trim();
    if (!content) return;

    fetch(`/rest/public/comment/${postId}`, {
        method: "POST",
        headers: {
            "Authorization": "Bearer " + getCookie(),
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ content })
    })
        .then(res => {
            if (!res.ok) throw new Error("Failed to post comment");
            commentInput.value = "";
            loadComments(postId);
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

submitCommentBtn.addEventListener("click", () => {
    postComment(postId);
});

document.getElementById("imageInput").addEventListener("change", function (e) {
    const file = e.target.files[0];
    if (!file) return;

    const img = document.getElementById("editorImage");
    img.src = URL.createObjectURL(file);
    img.style.display = "block";

    img.onload = function () {
        setTimeout(() => {
            $(formatSelector).select2('destroy');
            $(hashtagsSelector).select2('destroy');
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

    $(formatSelector).val(normalized).trigger('change');


});

document.querySelector("form").addEventListener("submit", function (e) {
    if (!cropper) return;

    e.preventDefault();

    cropper.getCroppedCanvas().toBlob(blob => {
        const fileInput = document.getElementById("imageInput");

        const file = new File([blob], "image."+$(formatSelector).select2('val'), { type: blob.type });
        const dataTransfer = new DataTransfer();
        dataTransfer.items.add(file);
        fileInput.files = dataTransfer.files;

        e.target.submit();
    }, "image/"+$(formatSelector).select2('val'), 0.9);
});
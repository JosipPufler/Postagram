const canvas = document.getElementById("canvas");
const ctx = canvas.getContext("2d");
let originalImage = null;

const controls = [
    { toggle: "sepiaToggle", range: "sepiaRange", css: v => `sepia(${v}%)` },
    { toggle: "blurToggle", range: "blurRange", css: v => `blur(${v}px)` },
    { toggle: "grayscaleToggle", range: "grayscaleRange", css: v => `grayscale(${v}%)` },
    { toggle: "contrastToggle", range: "contrastRange", css: v => `contrast(${v}%)` }
];

controls.forEach(c => {
    document.getElementById(c.toggle).addEventListener("change", updateFilters);
    document.getElementById(c.range).addEventListener("input", updateFilters);
});

document.getElementById("downloadBtn").addEventListener("click", () => {
    const url = canvas.toDataURL("image/jpeg", 0.95);
    const a = document.createElement("a");
    a.href = url;
    a.download = "processed.jpg";
    a.click();
});

document.getElementById("clearBtn").addEventListener("click", () => {
    controls
        .filter(c => document.getElementById(c.toggle).checked)
        .forEach(c => {
            document.getElementById(c.toggle).checked = false;
        });
});

export function loadImage(src) {
    originalImage = new Image()
    originalImage.src = src

    canvas.width = originalImage.width;
    canvas.height = originalImage.height;
    updateFilters()
}

function setFilters(filters){
    ctx.filter = filters || "none";
    ctx.drawImage(originalImage, 0, 0);
}

function updateFilters() {
    let filters = controls
        .filter(c => document.getElementById(c.toggle).checked)
        .map(c => {
            const value = document.getElementById(c.range).value;
            return c.css(value);
        })
        .join(" ");

    ctx.clearRect(0, 0, canvas.width, canvas.height);
    setFilters(filters)
}
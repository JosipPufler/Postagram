document.addEventListener('DOMContentLoaded', function () {
    const icon     = document.getElementById('userIcon');
    const dropdown = document.getElementById('userDropdown');
    if (!icon) return;            // if anonymous, no toggler present
    icon.addEventListener('click', function (e) {
        e.preventDefault();
        dropdown.classList.toggle('active');
    });
    document.addEventListener('click', function (e) {
        if (!icon.contains(e.target) && !dropdown.contains(e.target)) {
            dropdown.classList.remove('active');
        }
    });
});
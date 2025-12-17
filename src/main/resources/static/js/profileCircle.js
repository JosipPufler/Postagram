(function () {
    function initRings() {
        document.querySelectorAll('.progress-ring').forEach(svg => {
            const ring = svg.querySelector('.ring');
            const label = svg.querySelector('.ring-label');

            const r = ring.r.baseVal.value;
            const circumference = 2 * Math.PI * r;

            ring.style.strokeDasharray = `${circumference} ${circumference}`;

            const current = Number(ring.getAttribute('data-current') || 0);
            console.log(current)

            ring.style.strokeDashoffset = circumference - (current / 100) * circumference;

            label.textContent = `${current}%`;
            svg.setAttribute('aria-valuenow', String(current));
        });
    }

    window.updateProgressRing = function (selectorOrElement, current, max) {
        const ring = (typeof selectorOrElement === 'string') ? document.querySelector(selectorOrElement) : selectorOrElement;
        if (!ring) return;
        const circle = ring.querySelector('.ring');
        const label = ring.querySelector('.ring-label');
        const r = circle.r.baseVal.value;
        const circ = 2 * Math.PI * r;
        circle.style.strokeDasharray = `${circ} ${circ}`;
        const percent = Math.max(0, Math.min(100, Math.round((current / (max || 1)) * 100)));
        circle.style.strokeDashoffset = circ - (percent / 100) * circ;
        label.textContent = `${percent}%`;
        ring.setAttribute('title', `${current} of ${max} (${percent}%)`);
    };

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initRings);
    } else {
        initRings();
    }
})();
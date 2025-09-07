document.addEventListener('DOMContentLoaded', () => {

    // --- Hero Animation on Page Load ---
    const heroElements = document.querySelectorAll('.reveal-hero');
    heroElements.forEach((el, index) => {
        setTimeout(() => {
            el.classList.add('visible');
        }, (index + 1) * 250); // Staggered delay for hero elements
    });

    // --- Header Scroll & Active Nav Link ---
    const header = document.getElementById('main-header');
    const navLinks = document.querySelectorAll('.nav-link');
    const sections = document.querySelectorAll('#hero, .content-section, .main-footer');

    const changeHeaderOnScroll = () => {
        if (window.scrollY > 50) {
            header.classList.add('scrolled');
        } else {
            header.classList.remove('scrolled');
        }
    };

    const observerOptions = { root: null, rootMargin: '0px', threshold: 0.3 };
    const sectionObserver = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                navLinks.forEach(link => {
                    link.classList.remove('active');
                    if (link.getAttribute('href').substring(1) === entry.target.id) {
                        link.classList.add('active');
                    }
                });
            }
        });
    }, observerOptions);

    sections.forEach(section => sectionObserver.observe(section));
    window.addEventListener('scroll', changeHeaderOnScroll);

    // --- Scroll Reveal Animation (including staggered) ---
    const revealObserver = new IntersectionObserver((entries, observer) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                if (entry.target.dataset.staggerChildren) {
                    const children = entry.target.querySelectorAll('.reveal');
                    children.forEach((child, index) => {
                        child.style.transitionDelay = `${index * 120}ms`;
                    });
                }
                entry.target.classList.add('visible');
                observer.unobserve(entry.target);
            }
        });
    }, { threshold: 0.1 });

    document.querySelectorAll('.reveal').forEach(el => revealObserver.observe(el));

    // --- Notifications Modal Logic ---
    const notificationsBtn = document.getElementById('notifications-btn');
    const modal = document.getElementById('notifications-modal');
    const closeModalBtn = document.getElementById('close-modal-btn');

    if (notificationsBtn && modal && closeModalBtn) {
        notificationsBtn.addEventListener('click', () => modal.style.display = 'block');
        closeModalBtn.addEventListener('click', () => modal.style.display = 'none');
        window.addEventListener('click', (e) => {
            if (e.target === modal) {
                modal.style.display = 'none';
            }
        });
    }

    // --- Toast Notification Function ---
    function showToast(message) {
        const toast = document.getElementById('toast');
        if (!toast) return;
        toast.textContent = message;
        toast.classList.add('show');
        setTimeout(() => { toast.classList.remove('show'); }, 3000);
    }

    // --- File Upload & Other Logic (Assumed to be here) ---
    const dropZone = document.getElementById('drop-zone');
    const fileInput = document.getElementById('file-input');
    const previewsContainer = document.getElementById('file-previews');

    if (dropZone) {
        dropZone.addEventListener('click', () => fileInput.click());
        dropZone.addEventListener('dragover', (e) => { e.preventDefault(); dropZone.classList.add('active'); });
        dropZone.addEventListener('dragleave', () => dropZone.classList.remove('active'));
        dropZone.addEventListener('drop', (e) => { e.preventDefault(); dropZone.classList.remove('active'); handleFiles(e.dataTransfer.files); });
        fileInput.addEventListener('change', () => handleFiles(fileInput.files));
    }

    function handleFiles(files) {
        if(!previewsContainer) return;
        for (const file of files) {
            const preview = document.createElement('div');
            preview.className = 'file-preview';
            preview.innerHTML = `<i class="fas fa-file-alt"></i><span class="file-name">${file.name}</span><div class="progress-bar"><div class="progress"></div></div><span class="status">Uploading...</span>`;
            previewsContainer.appendChild(preview);
            let progress = 0;
            const interval = setInterval(() => {
                progress += 10;
                preview.querySelector('.progress').style.width = `${progress}%`;
                if (progress >= 100) { clearInterval(interval); preview.querySelector('.status').textContent = 'Complete'; preview.querySelector('.status').style.color = 'var(--secure-green)'; }
            }, 200);
        }
    }
});
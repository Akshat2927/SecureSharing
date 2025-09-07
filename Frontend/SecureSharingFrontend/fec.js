window.addEventListener("DOMContentLoaded", () => {
    loadProfile();
    initUpload();
});

// ----------------- Profile -----------------
function formatDate(dateStr, onlyDate = false) {
    if (!dateStr) return "-";
    const fixed = dateStr.replace(/(\.\d{1,3})(?=Z|$)/, match => (match + "00").slice(0, 4));
    const d = new Date(fixed);
    if (isNaN(d)) return "-";
    return onlyDate ? d.toLocaleDateString() : d.toLocaleString();
}

async function loadProfile() {
    const token = localStorage.getItem("accessToken");
    console.log("JWT token from localStorage:", token);

    const profileName = document.getElementById("profile-name");
    const profileEmail = document.getElementById("profile-email");
    const nameEl = document.getElementById("name");
    const emailEl = document.getElementById("email");
    const memberSinceEl = document.getElementById("memberSince");
    const lastLoginEl = document.getElementById("lastLogin");
    const accountStatusEl = document.getElementById("accountStatus");

    // ✅ Dashboard header element
    const welcomeUserEl = document.getElementById("welcome-user");

    if (!token) {
        if (welcomeUserEl) welcomeUserEl.textContent = "Welcome, Guest";
        if (profileName) profileName.textContent = "Guest";
        if (profileEmail) profileEmail.textContent = "Not logged in";
        if (nameEl) nameEl.textContent = "Guest";
        if (emailEl) emailEl.textContent = "Not logged in";
        if (memberSinceEl) memberSinceEl.textContent = "-";
        if (lastLoginEl) lastLoginEl.textContent = "-";
        if (accountStatusEl) accountStatusEl.textContent = "Inactive";
        return;
    }

    try {
        const response = await fetch("http://localhost:8080/profile", {
            method: "GET",
            headers: {
                "Authorization": `Bearer ${token}`,
                "Content-Type": "application/json"
            }
        });

        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);

        const profile = await response.json();
        console.log("Profile data received:", profile);

        // ✅ Update header username
        if (welcomeUserEl) welcomeUserEl.textContent = `Welcome, ${profile.name}`;

        if (profileName) profileName.textContent = profile.name;
        if (profileEmail) profileEmail.textContent = profile.email;
        if (nameEl) nameEl.textContent = profile.name;
        if (emailEl) emailEl.textContent = profile.email;
        if (memberSinceEl) memberSinceEl.textContent = formatDate(profile.memberSince, true);
        if (lastLoginEl) lastLoginEl.textContent = formatDate(profile.lastLogin);
        if (accountStatusEl) accountStatusEl.textContent = profile.accountStatus;

    } catch (error) {
        console.error("Error fetching profile:", error);

        if (welcomeUserEl) welcomeUserEl.textContent = "Welcome, Guest";
        if (profileName) profileName.textContent = "Guest";
        if (profileEmail) profileEmail.textContent = "Not logged in";
        if (nameEl) nameEl.textContent = "Guest";
        if (emailEl) emailEl.textContent = "Not logged in";
        if (memberSinceEl) memberSinceEl.textContent = "-";
        if (lastLoginEl) lastLoginEl.textContent = "-";
        if (accountStatusEl) accountStatusEl.textContent = "Inactive";
    }
}

// ----------------- Upload -----------------
function initUpload() {
    const dropZone = document.getElementById("drop-zone");
    const fileInput = document.getElementById("file-input");
    const uploadBtn = document.getElementById("upload-btn");
    const filePreviews = document.getElementById("file-previews");
    let selectedFiles = [];

    if (!dropZone || !fileInput || !uploadBtn || !filePreviews) return;

    dropZone.addEventListener("click", () => fileInput.click());

    dropZone.addEventListener("dragover", (e) => {
        e.preventDefault();
        dropZone.classList.add("dragover");
    });

    dropZone.addEventListener("dragleave", (e) => {
        e.preventDefault();
        dropZone.classList.remove("dragover");
    });

    dropZone.addEventListener("drop", (e) => {
        e.preventDefault();
        dropZone.classList.remove("dragover");
        handleFiles(e.dataTransfer.files);
    });

    fileInput.addEventListener("change", (e) => handleFiles(e.target.files));

    function handleFiles(files) {
        for (let file of files) {
            selectedFiles.push(file);
            const p = document.createElement("p");
            p.textContent = file.name;
            filePreviews.appendChild(p);
        }
    }

    uploadBtn.addEventListener("click", async () => {
        if (selectedFiles.length === 0) {
            alert("Please select at least one file.");
            return;
        }

        const sensitivity = document.querySelector('input[name="sensitivity"]:checked')?.nextElementSibling.textContent.trim() || "Public";
        const encrypted = document.querySelector('.toggle-switch input')?.checked || false;
        const token = localStorage.getItem("accessToken");
        if (!token) {
            alert("You must be logged in to upload files.");
            return;
        }

        for (let file of selectedFiles) {
            const formData = new FormData();
            formData.append("file", file);
            formData.append("sensitivity", sensitivity);
            formData.append("encrypted", encrypted);

            try {
                const response = await fetch("http://localhost:8080/upload", {
                    method: "POST",
                    headers: {
                        "Authorization": `Bearer ${token}`
                    },
                    body: formData
                });

                if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);

                const result = await response.text();
                console.log(result);
                alert(`File "${file.name}" uploaded successfully!`);
            } catch (err) {
                console.error("Upload error:", err);
                alert(`Failed to upload "${file.name}": ${err.message}`);
            }
        }

        selectedFiles = [];
        filePreviews.innerHTML = "";
    });
}

const API_BASE_URL = "http://localhost:8080";

// Elements
const shareBtn = document.getElementById("share-secure-btn");
const shareModal = document.getElementById("share-modal");
const closeShareModal = document.getElementById("close-share-modal");
const uploadedFilesList = document.getElementById("uploaded-files-list");
const formTemplate = document.getElementById("share-form-template");

// -------- Open modal
if (shareBtn) {
  shareBtn.addEventListener("click", async () => {
    const token = localStorage.getItem("accessToken");
    if (!token) {
      alert("⚠️ No JWT token found. Please login.");
      return;
    }
    shareModal.style.display = "flex";
    await fetchUserUploads(token);
  });
}

// -------- Close modal
if (closeShareModal) {
  closeShareModal.addEventListener("click", () => closeModal());
}
window.addEventListener("click", (e) => {
  if (e.target === shareModal) closeModal();
});

function closeModal() {
  shareModal.style.display = "none";
  uploadedFilesList.innerHTML = "";
}

// -------- Fetch uploaded files
async function fetchUserUploads(token) {
  try {
    const res = await fetch(`${API_BASE_URL}/getAll`, {
      headers: {
        "Authorization": `Bearer ${token}`,
        "Content-Type": "application/json"
      }
    });

    if (!res.ok) {
      const text = await res.text();
      throw new Error(`Failed to fetch uploads. Status: ${res.status}, ${text}`);
    }

    const files = await res.json();
    uploadedFilesList.innerHTML = "";

    if (files.length === 0) {
      uploadedFilesList.innerHTML = "<li>No files uploaded yet.</li>";
      return;
    }

    files.forEach(file => {
      const li = document.createElement("li");
      li.classList.add("file-item");

      li.innerHTML = `
        <div class="file-row">
          <span class="file-name"><i class="fas fa-file"></i> ${file.fileName}</span>
          <button class="btn-share btn btn-primary" data-fileid="${file.id}">Share</button>
        </div>
      `;

      // Clone form template
      const formClone = formTemplate.content.cloneNode(true);
      li.appendChild(formClone);

      uploadedFilesList.appendChild(li);
    });

  } catch (err) {
    console.error(err);
    alert("⚠️ Could not fetch uploads: " + err.message);
  }
}

// -------- Event delegation for dynamically added buttons
uploadedFilesList.addEventListener("click", (e) => {
  const target = e.target;

  if (target.classList.contains("btn-share")) {
    const parentLi = target.closest("li");
    const form = parentLi.querySelector(".share-form");
    form.style.display = form.style.display === "none" ? "block" : "none";

    const expirationSelect = form.querySelector(".share-expiration");
    const customExpDiv = form.querySelector(".custom-expiration");
    expirationSelect.addEventListener("change", () => {
      customExpDiv.style.display = expirationSelect.value === "custom" ? "flex" : "none";
    });
  }

  if (target.classList.contains("btn-generate")) {
    const parentLi = target.closest("li");
    generateSecureLink(parentLi);
  }

  if (target.classList.contains("btn-copy")) {
    const parentLi = target.closest("li");
    const linkInput = parentLi.querySelector(".generated-link");
    linkInput.select();
    document.execCommand("copy");
    alert("✅ Link copied to clipboard!");
  }
});

// -------- Generate link via backend
async function generateSecureLink(fileLi) {
  const token = localStorage.getItem("accessToken");
  if (!token) return alert("⚠️ No JWT token found. Please login.");

  const fileId = fileLi.querySelector(".btn-share").dataset.fileid;
  const password = fileLi.querySelector(".share-password").value.trim();
  const expiration = fileLi.querySelector(".share-expiration").value.trim();
  const hours = parseInt(fileLi.querySelector(".custom-hours")?.value || 0);
  const minutes = parseInt(fileLi.querySelector(".custom-minutes")?.value || 0);
  const seconds = parseInt(fileLi.querySelector(".custom-seconds")?.value || 0);

  if (!password) return alert("⚠️ Please enter a password.");
  if (!expiration) return alert("⚠️ Please select expiration time.");

  let expiresInSeconds;
  switch (expiration) {
    case "1h":
      expiresInSeconds = 3600;
      break;
    case "24h":
      expiresInSeconds = 86400;
      break;
    case "7d":
      expiresInSeconds = 7 * 24 * 3600;
      break;
    case "custom":
      expiresInSeconds = hours * 3600 + minutes * 60 + seconds;
      break;
    default:
      expiresInSeconds = 3600;
  }

  try {
    const res = await fetch(`${API_BASE_URL}/create`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${token}`
      },
      body: JSON.stringify({ fileId, password, expiresInSeconds })
    });

    if (!res.ok) {
      const text = await res.text();
      throw new Error(`Failed to generate link: ${res.status} ${text}`);
    }

    const data = await res.json();
    const linkContainer = fileLi.querySelector(".generated-link-container");
    const linkInput = fileLi.querySelector(".generated-link");
    linkInput.value = data.shareUrl;
    linkContainer.style.display = "block";

  } catch (err) {
    console.error(err);
    alert("⚠️ Could not generate link: " + err.message);
  }
}

// -------- Password popup for shared links --------
document.addEventListener("DOMContentLoaded", () => {
  const pathParts = window.location.pathname.split("/");
  const token = pathParts[pathParts.length - 1];

  // Only if user opens /share/{token}
  if (window.location.pathname.startsWith("/share/") && token) {
    const popup = document.createElement("div");
    popup.innerHTML = `
      <div id="password-popup" style="
          position:fixed;top:0;left:0;width:100%;height:100%;
          display:flex;align-items:center;justify-content:center;
          background:#f5f5f5;z-index:9999;">
        <div style="background:#fff;padding:20px;border-radius:10px;text-align:center;min-width:300px;">
          <h2>🔒 Enter Password</h2>
          <input type="password" id="access-password" placeholder="Enter password"
                 style="padding:8px;width:90%;margin:10px 0;">
          <br>
          <button id="submit-pass" style="padding:8px 16px;cursor:pointer;">Submit</button>
          <p id="error-msg" style="color:red;display:none;margin-top:10px;"></p>
        </div>
      </div>
    `;
    document.body.appendChild(popup);

    const submitBtn = document.getElementById("submit-pass");
    const errorMsg = document.getElementById("error-msg");

    submitBtn.addEventListener("click", async () => {
      const password = document.getElementById("access-password").value.trim();
      if (!password) return;

      try {
        const res = await fetch(`${API_BASE_URL}/access/${token}`, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ password })   // ✅ send as { "password": "xyz" }
        });

        if (!res.ok) {
          const errText = await res.text();
          errorMsg.innerText = errText;
          errorMsg.style.display = "block";
          return;
        }

        // ✅ Expect JSON { fileName, contentType, base64Data }
        const data = await res.json();
        const { fileName, contentType, base64Data } = data;

        // Remove password popup
        document.getElementById("password-popup").remove();

        // Create full-screen preview
        const viewer = document.createElement("div");
        viewer.innerHTML = `
          <div style="position:fixed;top:0;left:0;width:100%;height:100%;
                      background:#000;display:flex;justify-content:center;align-items:center;z-index:99999;">
            <div style="background:#fff;padding:10px;border-radius:8px;max-width:95%;max-height:95%;overflow:auto;position:relative;">
              <button id="close-viewer" style="position:absolute;top:10px;right:10px;">❌</button>
              <h3 style="margin-top:0;text-align:center;">${fileName}</h3>
              <div style="text-align:center;">
                ${
                  contentType.startsWith("image/")
                    ? `<img src="data:${contentType};base64,${base64Data}" style="max-width:100%;max-height:80vh;">`
                    : contentType === "application/pdf"
                    ? `<embed src="data:${contentType};base64,${base64Data}" type="application/pdf" width="800" height="600">`
                    : `<iframe src="data:${contentType};base64,${base64Data}" style="width:800px;height:600px;" frameborder="0"></iframe>`
                }
              </div>
            </div>
          </div>
        `;
        document.body.appendChild(viewer);

        document.getElementById("close-viewer").addEventListener("click", () => {
          viewer.remove();
        });

      } catch (err) {
        errorMsg.innerText = "⚠️ Error accessing file";
        errorMsg.style.display = "block";
      }
    });
  }
});

import "./style.css";

const API_BASE_URL = "http://localhost:8080";

// -------- Signup --------
async function signup(name, email, password) {
    try {
        const response = await fetch(`${API_BASE_URL}/register`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ name, email, password })
        });

        const resultText = await response.text();
        if (!response.ok) throw new Error(resultText || "Signup failed");

        alert(resultText);
        window.location.href = "/login.html";
    } catch (error) {
        console.error("Signup error:", error);
        alert("Signup failed: " + error.message);
    }
}

// -------- Login --------
async function login(email, password) {
    try {
        const response = await fetch(`${API_BASE_URL}/login`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, password })
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || "Login failed");
        }

        const data = await response.json();
        localStorage.setItem("accessToken", data.accessToken);
        localStorage.setItem("refreshToken", data.refreshToken);
         alert("Login successful!");

        window.location.href = "dashboard.html";
    } catch (error) {
        console.error("Login error:", error);
        alert("Login failed: " + error.message);
    }
}

// -------- Attach Form Events --------
document.addEventListener("DOMContentLoaded", () => {
    const signupForm = document.getElementById("signupForm");
    const loginForm = document.getElementById("loginForm");

    if (signupForm) {
        signupForm.addEventListener("submit", e => {
            e.preventDefault();
            signup(
                document.getElementById("signupName").value.trim(),
                document.getElementById("signupEmail").value.trim(),
                document.getElementById("signupPassword").value.trim()
            );
        });
    }

    if (loginForm) {
        loginForm.addEventListener("submit", e => {
            e.preventDefault();
            login(
                document.getElementById("loginEmail").value.trim(),
                document.getElementById("loginPassword").value.trim()
            );
        });
    }
});

// -------- Logout Helper --------
function logout() {
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    window.location.href = "login.html";
}

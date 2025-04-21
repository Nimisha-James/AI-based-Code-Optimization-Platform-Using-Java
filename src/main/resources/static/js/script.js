// Auth functions
function toggleForm(type) {
    document.getElementById("message").innerText = "";
    if (type === 'signup') {
        document.getElementById("form-title").innerText = "Sign Up";
        document.getElementById("signup-form").style.display = "block";
        document.getElementById("signin-form").style.display = "none";
    } else {
        document.getElementById("form-title").innerText = "Sign In";
        document.getElementById("signup-form").style.display = "none";
        document.getElementById("signin-form").style.display = "block";
    }
}

function signup() {
    let name = document.getElementById("name").value.trim();
    let email = document.getElementById("email-signup").value.trim();
    let password = document.getElementById("password-signup").value.trim();

    if (!name || !email || !password) {
        document.getElementById("message").innerText = "All fields are required!";
        return;
    }

    fetch("/auth/signup", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ name, email, password })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            alert("Sign-up successful! Redirecting to dashboard...");
            localStorage.setItem("userEmail", email);
            window.location.href = "/dashboard.html";
        } else {
            document.getElementById("message").innerText = data.message;
        }
    })
    .catch(error => console.error("Error:", error));
}

function signin() {
    let email = document.getElementById("email-signin").value.trim();
    let password = document.getElementById("password-signin").value.trim();

    if (!email || !password) {
        document.getElementById("message").innerText = "All fields are required!";
        return;
    }

    fetch("/auth/signin", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email, password })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success==="true") {
            alert("Login successful! Redirecting to dashboard...");
            localStorage.setItem("userEmail", email);
            window.location.href = "/dashboard.html";
        } else {
            document.getElementById("message").innerText = data.message;
        }
    })
    .catch(error => console.error("Error:", error));
}

// Dashboard functions
document.addEventListener('DOMContentLoaded', () => {
    if (document.getElementById('compile-btn')) {
        document.getElementById('compile-btn').addEventListener('click', compileCode);
        document.getElementById('analyze-btn').addEventListener('click', analyzeCode);
        document.getElementById('optimize-btn').addEventListener('click', optimizeCode);
    }
});

async function compileCode() {
    const code = document.getElementById('code-editor').value;
    const outputDiv = document.getElementById('output');
    const email = localStorage.getItem("userEmail");

    if (!email) {
        outputDiv.innerHTML = `<span class="error">Error: Please log in first!</span>`;
        return;
    }

    outputDiv.innerHTML = "Compiling code...";
    outputDiv.className = "";

    try {
        const response = await fetch('/api/check', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ code })
        });

        const result = await response.json();

        if (result.valid) {
            outputDiv.innerHTML = `<span class="success">✓ Compilation successful!</span>`;
            outputDiv.className = "success";

            await fetch("/code/analyze", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email, code })
            });
        } else {
            let errorHtml = "<h4>Compilation errors:</h4><ul>";
            result.errors.forEach(error => {
                const errorClass = error.includes("Warning") ? "warning" : "error";
                errorHtml += `<li class="${errorClass}">${error}</li>`;
            });
            errorHtml += "</ul>";
            outputDiv.innerHTML = errorHtml;
        }
    } catch (error) {
        outputDiv.innerHTML = `<span class="error">Error: ${error.message}</span>`;
        outputDiv.className = "error";
    }
}

async function analyzeCode() {
    const code = document.getElementById('code-editor').value;
    const outputDiv = document.getElementById('output');
    const email = localStorage.getItem("userEmail");

    if (!email) {
        outputDiv.innerHTML = `<span class="error">Error: Please log in first!</span>`;
        return;
    }

    if (!code) {
        outputDiv.innerHTML = `<span class="error">Error: Please enter code to analyze!</span>`;
        return;
    }

    outputDiv.innerHTML = "Analyzing code...";

    try {
        const response = await fetch("/code/analyze", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, code })
        });

        const data = await response.json();

        if (data.success) {
            outputDiv.innerHTML = `<span class="success">Time Complexity: ${data.complexity}</span>`;
            outputDiv.className = "success";
        } else {
            outputDiv.innerHTML = `<span class="error">Error: ${data.error}</span>`;
            outputDiv.className = "error";
        }
    } catch (error) {
        outputDiv.innerHTML = `<span class="error">Error: ${error.message}</span>`;
        outputDiv.className = "error";
    }
}

async function optimizeCode() {
    const code = document.getElementById('code-editor').value;
    const outputDiv = document.getElementById('output');
    const email = localStorage.getItem("userEmail");

    if (!email) {
        outputDiv.innerHTML = `<span class="error">Error: Please log in first!</span>`;
        return;
    }

    if (!code) {
        outputDiv.innerHTML = `<span class="error">Error: Please enter code to optimize!</span>`;
        return;
    }

    outputDiv.innerHTML = "Optimizing code...";

    try {
        const response = await fetch("/code/optimize", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, code })
        });

        const data = await response.json();

        if (data.success) {
            outputDiv.innerHTML = `
                <span class="success">Optimized Code:</span>
                <pre>${escapeHTML(data.optimizedCode)}</pre>
            `;
            outputDiv.className = "success";
        } else {
            outputDiv.innerHTML = `<span class="error">Error: ${data.error}</span>`;
            outputDiv.className = "error";
        }
    } catch (error) {
        outputDiv.innerHTML = `<span class="error">Error: ${error.message}</span>`;
        outputDiv.className = "error";
    }
}

// Utility to escape code output
function escapeHTML(text) {
    return text.replace(/[&<>"']/g, match => ({
        '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;'
    }[match]));
}

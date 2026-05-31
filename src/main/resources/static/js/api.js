const API = window.location.origin + '/api/applications';
const AUTH_API = window.location.origin + '/api/auth/google';

async function apiFetch(url, options = {}) {
  const token = localStorage.getItem('auth_token');
  const headers = { 'Content-Type': 'application/json' };
  if (token) {
    headers['Authorization'] = 'Bearer ' + token;
  }

  const res = await fetch(url, {
    ...options,
    headers: { ...headers, ...options.headers }
  });

  if (res.status === 401) {
    forceLogout();
    showToast("Your session has expired. Please sign in again.", "error");
    throw new Error("Your session has timed out. Please sign in again to keep tracking your applications.");
  }

  if (!res.ok) {
    const err = await res.json().catch(() => ({ message: "We encountered a hiccup processing your request. Please try again in a moment." }));
    throw new Error(err.message || `HTTP ${res.status}`);
  }
  return res.status === 204 ? null : res.json();
}

function showToast(msg, type = 'success') {
  const t = document.createElement('div');
  t.className = `fixed bottom-10 right-10 z-[100] px-8 py-4 rounded-2xl bg-black text-white text-xs mono uppercase tracking-widest shadow-2xl animate-in slide-in-from-bottom-10 duration-500 dark:bg-white dark:text-black`;
  if (type === 'error') {
    t.className += ' text-rose-500 dark:text-rose-600';
  }
  t.textContent = msg;
  document.body.appendChild(t);
  setTimeout(() => t.remove(), 4000);
}

function showError(msg) {
  const b = document.getElementById('error-banner');
  if (b) {
    b.textContent = msg;
    b.classList.remove('hidden');
  }
}

function hideError() {
  const b = document.getElementById('error-banner');
  if (b) {
    b.classList.add('hidden');
  }
}

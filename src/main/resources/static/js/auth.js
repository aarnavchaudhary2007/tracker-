let authMode = 'signin';

function switchAuthTab(mode, el) {
  authMode = mode;
  const btnSubmit = document.getElementById('btn-auth-submit');
  const errBanner = document.getElementById('auth-error');
  
  if (errBanner) errBanner.classList.add('hidden');
  
  const nav = document.getElementById('auth-nav');
  if (nav) {
    nav.querySelectorAll('.nav-tab').forEach(t => t.classList.remove('active'));
    
    // Set active tab, falling back to data-val lookup if element is not passed programmatically
    const targetEl = el || nav.querySelector(`.nav-tab[data-val="${mode}"]`);
    if (targetEl) targetEl.classList.add('active');
    
    // Trigger cursor repositioning
    nav.dispatchEvent(new Event('mouseleave'));
  }
  
  if (btnSubmit) {
    btnSubmit.textContent = mode === 'signin' ? "Sign In" : "Sign Up";
  }
}

function showInputError(id) {
  const el = document.getElementById(id);
  if (el) el.classList.add('border-rose-500', 'bg-rose-50', 'dark:bg-rose-950/20');
}

function clearInputError(id) {
  const el = document.getElementById(id);
  if (el) el.classList.remove('border-rose-500', 'bg-rose-50', 'dark:bg-rose-950/20');
}

async function submitAuth() {
  const usernameInput = document.getElementById('auth-username');
  const passwordInput = document.getElementById('auth-password');
  const username = usernameInput ? usernameInput.value.trim() : '';
  const password = passwordInput ? passwordInput.value : '';
  const errBanner = document.getElementById('auth-error');
  const btnSubmit = document.getElementById('btn-auth-submit');

  if (errBanner) errBanner.classList.add('hidden');
  clearInputError('auth-username');
  clearInputError('auth-password');

  let hasErrors = false;
  if (!username) { showInputError('auth-username'); hasErrors = true; }
  if (!password) { showInputError('auth-password'); hasErrors = true; }
  if (hasErrors) return;

  if (btnSubmit) {
    btnSubmit.disabled = true;
    btnSubmit.textContent = "Processing...";
  }

  try {
    const url = window.location.origin + '/api/auth/' + (authMode === 'signin' ? 'signin' : 'signup');
    const res = await fetch(url, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password })
    });

    const data = await res.json().catch(() => ({}));
    if (!res.ok) {
      throw new Error(data.message || "That username or password doesn't match our records. Please double-check and try again.");
    }

    if (authMode === 'signin') {
      try {
        localStorage.setItem('auth_token', data.token);
        localStorage.setItem('auth_username', data.username);
      } catch (e) {}
      showToast('Welcome back, ' + data.username, 'success');
      
      // Animate transition out
      const overlay = document.getElementById('auth-overlay');
      if (overlay) {
        overlay.classList.add('opacity-0');
        setTimeout(() => {
          overlay.classList.replace('flex', 'hidden');
          overlay.classList.remove('opacity-0');
        }, 500);
      }

      initApp();
    } else {
      showToast('Account created! Please sign in.', 'success');
      switchAuthTab('signin');
      if (passwordInput) passwordInput.value = '';
    }
  } catch (e) {
    if (errBanner) {
      errBanner.textContent = e.message;
      errBanner.classList.remove('hidden');
    }
  } finally {
    if (btnSubmit) {
      btnSubmit.disabled = false;
      btnSubmit.textContent = authMode === 'signin' ? "Sign In" : "Sign Up";
    }
  }
}

async function submitGuestAuth() {
  const btnSubmit = document.getElementById('btn-auth-submit');
  const btnGuest = document.getElementById('btn-guest-submit');
  const errBanner = document.getElementById('auth-error');

  if (errBanner) errBanner.classList.add('hidden');

  if (btnSubmit) btnSubmit.disabled = true;
  if (btnGuest) {
    btnGuest.disabled = true;
    btnGuest.textContent = "Opening Gate...";
  }

  try {
    const url = window.location.origin + '/api/auth/guest';
    const res = await fetch(url, {
      method: 'POST'
    });

    const data = await res.json().catch(() => ({}));
    if (!res.ok) {
      throw new Error(data.message || "We had trouble establishing your guest session. Please try again.");
    }

    try {
      localStorage.setItem('auth_token', data.token);
      localStorage.setItem('auth_username', data.username);
    } catch (e) {}

    showToast("Logged in as Guest: " + data.username, "success");

    // Animate transition out
    const overlay = document.getElementById('auth-overlay');
    if (overlay) {
      overlay.classList.add('opacity-0');
      setTimeout(() => {
        overlay.classList.replace('flex', 'hidden');
        overlay.classList.remove('opacity-0');
      }, 500);
    }

    initApp();
  } catch (e) {
    if (errBanner) {
      errBanner.textContent = e.message;
      errBanner.classList.remove('hidden');
    }
  } finally {
    if (btnSubmit) btnSubmit.disabled = false;
    if (btnGuest) {
      btnGuest.disabled = false;
      btnGuest.textContent = "Continue as Guest";
    }
  }
}

async function handleSignout() {
  let token = null;
  try {
    token = localStorage.getItem('auth_token');
  } catch (e) {}
  if (token) {
    try {
      await fetch(window.location.origin + '/api/auth/signout', {
        method: 'POST',
        headers: { 'Authorization': 'Bearer ' + token }
      });
    } catch (e) {}
  }
  showToast('Signed out', 'success');
  forceLogout();
}

async function handleDeleteAccount() {
  const confirmed = confirm("Are you sure you want to permanently delete your account and clear all your job tracking applications? This action is serious and cannot be undone.");
  if (!confirmed) return;

  let token = null;
  try {
    token = localStorage.getItem('auth_token');
  } catch (e) {}
  
  if (token) {
    try {
      const res = await fetch(window.location.origin + '/api/auth/account', {
        method: 'DELETE',
        headers: { 'Authorization': 'Bearer ' + token }
      });
      const data = await res.json().catch(() => ({}));
      if (!res.ok) {
        throw new Error(data.message || "We encountered a problem deleting your account. Please try again.");
      }
      showToast('Account and tracking data permanently deleted.', 'success');
    } catch (e) {
      showToast(e.message, 'error');
    }
  }
  
  forceLogout();
}

function forceLogout() {
  try {
    localStorage.removeItem('auth_token');
    localStorage.removeItem('auth_username');
  } catch (e) {}
  
  const overlay = document.getElementById('auth-overlay');
  const userDisp = document.getElementById('user-display');
  
  if (overlay) overlay.classList.replace('hidden', 'flex');
  if (userDisp) userDisp.textContent = 'Guest';
}

function initApp() {
  let token = null;
  let username = null;
  try {
    token = localStorage.getItem('auth_token');
    username = localStorage.getItem('auth_username');
  } catch (e) {}
  const overlay = document.getElementById('auth-overlay');
  const userDisp = document.getElementById('user-display');

  if (token && username) {
    if (overlay) overlay.classList.replace('flex', 'hidden');
    if (userDisp) userDisp.textContent = username;
    loadStats();
    loadList();
  } else {
    if (overlay) overlay.classList.replace('hidden', 'flex');
  }
  initFAB();
}

function initTheme() {
  let isDark = false;
  try {
    isDark = localStorage.theme === 'dark' || (!('theme' in localStorage) && window.matchMedia('(prefers-color-scheme: dark)').matches);
  } catch (e) {
    isDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
  }
  const sunIcon = document.getElementById('sun-icon');
  const moonIcon = document.getElementById('moon-icon');
  if (isDark) {
    document.documentElement.classList.add('dark');
    if (sunIcon) sunIcon.classList.remove('hidden');
    if (moonIcon) moonIcon.classList.add('hidden');
  } else {
    document.documentElement.classList.remove('dark');
    if (sunIcon) sunIcon.classList.add('hidden');
    if (moonIcon) moonIcon.classList.remove('hidden');
  }
}

function toggleTheme() {
  const isDark = document.documentElement.classList.toggle('dark');
  const sunIcon = document.getElementById('sun-icon');
  const moonIcon = document.getElementById('moon-icon');
  try {
    if (isDark) {
      localStorage.theme = 'dark';
    } else {
      localStorage.theme = 'light';
    }
  } catch (e) {}
  
  if (isDark) {
    if (sunIcon) sunIcon.classList.remove('hidden');
    if (moonIcon) moonIcon.classList.add('hidden');
    showToast('Dark mode activated', 'success');
  } else {
    if (sunIcon) sunIcon.classList.add('hidden');
    if (moonIcon) moonIcon.classList.remove('hidden');
    showToast('Light mode activated', 'success');
  }
}

function toggleMenuPanel() {
  const panel = document.getElementById('controls-panel');
  const hamIcon = document.getElementById('hamburger-icon');
  const closeIcon = document.getElementById('close-icon');
  const fabBtn = document.getElementById('menu-fab');
  
  if (!panel) return;
  
  const isShown = panel.classList.contains('opacity-100');
  
  if (isShown) {
    panel.classList.replace('opacity-100', 'opacity-0');
    panel.classList.replace('translate-x-0', '-translate-x-2');
    panel.classList.add('pointer-events-none');
    
    if (hamIcon) hamIcon.classList.remove('hidden');
    if (closeIcon) closeIcon.classList.add('hidden');
    if (fabBtn) fabBtn.classList.remove('rotate-90');
  } else {
    panel.classList.replace('opacity-0', 'opacity-100');
    panel.classList.replace('-translate-x-2', 'translate-x-0');
    panel.classList.remove('pointer-events-none');
    
    if (hamIcon) hamIcon.classList.add('hidden');
    if (closeIcon) closeIcon.classList.remove('hidden');
    if (fabBtn) fabBtn.classList.add('rotate-90');
  }
}

// Close controls panel when clicking outside
document.addEventListener('click', (e) => {
  const panel = document.getElementById('controls-panel');
  const fab = document.getElementById('menu-fab');
  if (panel && fab && !panel.contains(e.target) && !fab.contains(e.target)) {
    const isShown = panel.classList.contains('opacity-100');
    if (isShown) {
      toggleMenuPanel();
    }
  }
});

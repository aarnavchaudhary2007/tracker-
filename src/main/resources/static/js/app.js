let activeStatusFilter = 'all';
let activeTypeFilter = 'all';
let searchTimer = null;
let editingId = null;
let statusTargetId = null;

// Sliding Cursor Logic
function setupNav(navId, cursorId) {
  const nav = document.getElementById(navId);
  const cursor = document.getElementById(cursorId);
  if (!nav || !cursor) return;
  const tabs = nav.querySelectorAll('.nav-tab');

  function updateCursor(tab) {
    if (!tab) {
      cursor.style.opacity = '0';
      return;
    }
    cursor.style.width = `${tab.offsetWidth}px`;
    cursor.style.left = `${tab.offsetLeft}px`;
    cursor.style.opacity = '1';
  }

  tabs.forEach(tab => {
    tab.addEventListener('mouseenter', () => updateCursor(tab));
  });

  nav.addEventListener('mouseleave', () => {
    const activeTab = nav.querySelector('.nav-tab.active');
    updateCursor(activeTab);
  });

  // Initial position
  setTimeout(() => updateCursor(nav.querySelector('.nav-tab.active')), 100);
}

async function loadStats() {
  try {
    const stats = await apiFetch(`${API}/stats`);
    const cntApplied = document.getElementById('cnt-applied');
    const cntInterview = document.getElementById('cnt-interview');
    const cntOffer = document.getElementById('cnt-offer');
    const cntRejected = document.getElementById('cnt-rejected');

    if (cntApplied) cntApplied.textContent = stats.applied;
    if (cntInterview) cntInterview.textContent = stats.interview;
    if (cntOffer) cntOffer.textContent = stats.offer;
    if (cntRejected) cntRejected.textContent = stats.rejected;
  } catch (e) {}
}

async function loadList() {
  const list = document.getElementById('list');
  if (list) list.innerHTML = '<div class="text-center py-20 text-slate-200 mono text-xs uppercase tracking-widest animate-pulse">Synchronizing Data...</div>';

  const searchEl = document.getElementById('search');
  const search = searchEl ? searchEl.value.trim() : '';
  let params = new URLSearchParams();
  
  if (search) params.append('search', search);
  if (activeStatusFilter !== 'all') params.append('status', activeStatusFilter);
  if (activeTypeFilter !== 'all') params.append('type', activeTypeFilter);

  const url = API + '?' + params.toString();

  try {
    const apps = await apiFetch(url);
    hideError();
    renderList(apps);
  } catch (e) {
    showError("We're having trouble connecting to the database server. Your entries are safe, but we can't display them right now. Please check your internet connection.");
    if (list) list.innerHTML = '';
  }
}

function renderList(apps) {
  const list = document.getElementById('list');
  if (!list) return;
  if (apps.length === 0) {
    list.innerHTML = `
      <div class="text-center py-32 border-2 border-dashed border-slate-100 dark:border-zinc-800 rounded-3xl">
        <p class="text-slate-300 dark:text-zinc-600 mono text-xs uppercase tracking-widest">No Entries Detected</p>
      </div>`;
    return;
  }

  list.innerHTML = apps.map(a => `
    <div class="app-card p-6 flex flex-col md:flex-row justify-between items-start md:items-center gap-6">
      <div class="flex-1">
        <div class="flex items-center gap-3 mb-1">
          <h3 class="text-lg font-bold tracking-tight dark:text-white">${esc(a.role)}</h3>
          <span class="text-[9px] font-bold px-2 py-0.5 bg-slate-100 dark:bg-zinc-800 text-slate-500 dark:text-zinc-400 rounded uppercase tracking-tighter">${a.type}</span>
        </div>
        <p class="text-slate-400 dark:text-zinc-500 font-medium text-sm mb-4">${esc(a.company)}</p>
        <div class="flex flex-wrap gap-5">
          <div class="flex items-center gap-2 text-[10px] mono text-slate-400 dark:text-zinc-500 uppercase tracking-wider">
            <span class="text-slate-200 dark:text-zinc-700 font-bold">Applied:</span> ${a.dateApplied}
          </div>
          ${a.location ? `
          <div class="flex items-center gap-2 text-[10px] mono text-slate-400 dark:text-zinc-500 uppercase tracking-wider">
            <span class="text-slate-200 dark:text-zinc-700 font-bold">Loc:</span> ${esc(a.location)}
          </div>` : ''}
          ${a.salaryRange ? `
          <div class="flex items-center gap-2 text-[10px] mono text-emerald-500 dark:text-emerald-400 uppercase tracking-wider">
            <span class="text-emerald-100 dark:text-emerald-950 font-bold">Comp:</span> ${esc(a.salaryRange)}
          </div>` : ''}
        </div>
      </div>
      <div class="flex items-center gap-5 w-full md:w-auto">
        <button onclick="openStatusPick(${a.id})" class="badge badge-${a.status.toLowerCase()} transition-all hover:scale-105">
          ${a.status}
        </button>
        <div class="flex gap-2">
          <button onclick="openEdit(${a.id})" class="p-2 text-slate-300 dark:text-zinc-600 hover:text-black dark:hover:text-white transition-colors">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
          </button>
          <button onclick="deleteApp(${a.id})" class="p-2 text-slate-300 dark:text-zinc-600 hover:text-rose-500 dark:hover:text-rose-500 transition-colors">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14H6L5 6"/><path d="M10 11v6"/><path d="M14 11v6"/><path d="M9 6V4h6v2"/></svg>
          </button>
        </div>
      </div>
    </div>`).join('');
}

function setStatusFilter(val, el) {
  activeStatusFilter = val;
  const nav = document.getElementById('status-nav');
  if (nav) {
    nav.querySelectorAll('.nav-tab').forEach(c => c.classList.remove('active'));
  }
  if (el) el.classList.add('active');
  loadList();
}

function setTypeFilter(val, el) {
  activeTypeFilter = val;
  const nav = document.getElementById('type-nav');
  if (nav) {
    nav.querySelectorAll('.nav-tab').forEach(c => c.classList.remove('active'));
  }
  if (el) el.classList.add('active');
  loadList();
}

function onSearch() {
  clearTimeout(searchTimer);
  searchTimer = setTimeout(loadList, 350);
}

function openModal() {
  editingId = null;
  const modalTitle = document.getElementById('modal-title');
  const btnSave = document.getElementById('btn-save');
  const dateInput = document.getElementById('f-date');
  const overlay = document.getElementById('overlay');

  if (modalTitle) modalTitle.textContent = 'ADD RECORD';
  if (btnSave) btnSave.textContent = 'Save Application';
  
  clearForm();
  
  if (dateInput) dateInput.value = new Date().toISOString().split('T')[0];
  if (overlay) overlay.classList.replace('hidden', 'flex');
}

async function openEdit(id) {
  try {
    const app = await apiFetch(`${API}/${id}`);
    editingId = id;
    
    const modalTitle = document.getElementById('modal-title');
    const btnSave = document.getElementById('btn-save');
    const overlay = document.getElementById('overlay');

    if (modalTitle) modalTitle.textContent = 'EDIT RECORD';
    if (btnSave) btnSave.textContent = 'Update Application';

    document.getElementById('f-role').value = app.role;
    document.getElementById('f-company').value = app.company;
    document.getElementById('f-type').value = app.type;
    document.getElementById('f-status').value = app.status;
    document.getElementById('f-date').value = app.dateApplied;
    document.getElementById('f-location').value = app.location || '';
    document.getElementById('f-salary').value = app.salaryRange || '';
    document.getElementById('f-notes').value = app.notes || '';
    
    if (overlay) overlay.classList.replace('hidden', 'flex');
  } catch (e) { showToast("We couldn't retrieve the details for this application. Please try opening it again.", "error"); }
}

function closeModal() {
  const overlay = document.getElementById('overlay');
  if (overlay) overlay.classList.replace('flex', 'hidden');
}

function overlayClick(e) {
  if (e.target === document.getElementById('overlay')) closeModal();
}

function clearForm() {
  ['f-role','f-company','f-location','f-salary','f-notes'].forEach(id => {
    const el = document.getElementById(id);
    if (el) el.value = '';
  });
  const typeEl = document.getElementById('f-type');
  const statusEl = document.getElementById('f-status');
  if (typeEl) typeEl.value = 'INTERNSHIP';
  if (statusEl) statusEl.value = 'APPLIED';
  clearAllErrors();
}

function clearAllErrors() {
  ['f-role', 'f-company', 'f-date'].forEach(clearInputError);
}

async function saveApp() {
  clearAllErrors();
  
  const payload = {
    role: document.getElementById('f-role').value.trim(),
    company: document.getElementById('f-company').value.trim(),
    type: document.getElementById('f-type').value,
    status: document.getElementById('f-status').value,
    dateApplied: document.getElementById('f-date').value,
    location: document.getElementById('f-location').value.trim() || null,
    salaryRange: document.getElementById('f-salary').value.trim() || null,
    notes: document.getElementById('f-notes').value.trim() || null
  };

  let hasErrors = false;
  if (!payload.role) { showInputError('f-role'); hasErrors = true; }
  if (!payload.company) { showInputError('f-company'); hasErrors = true; }
  if (!payload.dateApplied) { showInputError('f-date'); hasErrors = true; }

  if (hasErrors) {
    showToast("Please fill out all the mandatory fields marked with an asterisk (*) to save this application.", "error");
    return;
  }

  const btn = document.getElementById('btn-save');
  if (btn) {
    btn.disabled = true;
    btn.textContent = 'Processing...';
  }

  try {
    if (editingId) {
      await apiFetch(`${API}/${editingId}`, { method: 'PUT', body: JSON.stringify(payload) });
    } else {
      await apiFetch(API, { method: 'POST', body: JSON.stringify(payload) });
    }
    showToast(editingId ? 'Record updated' : 'Application tracked', 'success');
    closeModal();
    loadList();
    loadStats();
  } catch (e) { showToast(e.message, 'error'); }
  finally {
    if (btn) {
      btn.disabled = false;
      btn.textContent = editingId ? 'Update Application' : 'Save Application';
    }
  }
}

let deleteTargetId = null;

function deleteApp(id) {
  deleteTargetId = id;
  const card = document.querySelector(`[onclick="deleteApp(${id})"]`);
  let detail = '';
  if (card) {
    const row = card.closest('.app-card');
    if (row) {
      const role = row.querySelector('h3');
      const company = row.querySelector('p');
      if (role && company) detail = `${role.textContent.trim()} @ ${company.textContent.trim()}`;
    }
  }
  const detailEl = document.getElementById('delete-confirm-detail');
  if (detailEl) detailEl.textContent = detail || 'this application';
  
  // Reset animation state
  const lid = document.getElementById('trash-lid');
  const wrapper = document.getElementById('trash-anim-wrapper');
  const icon = wrapper ? wrapper.querySelector('.trash-icon') : null;
  const modalContent = document.querySelector('#delete-confirm > div');
  const backdrop = document.getElementById('delete-confirm');

  if (lid) lid.classList.remove('closing');
  if (icon) icon.classList.remove('trash-deleting', 'trash-shake', 'trash-fall');
  if (modalContent) modalContent.classList.remove('modal-shake', 'modal-crunch');
  if (backdrop) {
    backdrop.classList.remove('backdrop-fade-out');
    backdrop.classList.replace('hidden', 'flex');
  }
  if (wrapper) wrapper.style.opacity = '1';
}

function cancelDelete() {
  deleteTargetId = null;
  const confirmEl = document.getElementById('delete-confirm');
  if (confirmEl) confirmEl.classList.replace('flex', 'hidden');
}

function closeDeleteConfirm(e) {
  if (e.target === document.getElementById('delete-confirm')) cancelDelete();
}

async function confirmDelete() {
  if (!deleteTargetId) return;
  const btn = document.getElementById('btn-confirm-delete');
  if (btn) {
    btn.disabled = true;
    btn.textContent = 'Deleting...';
  }

  // Animate: close lid → shake → fall + crunch
  const lid = document.getElementById('trash-lid');
  const wrapper = document.getElementById('trash-anim-wrapper');
  const icon = wrapper ? wrapper.querySelector('.trash-icon') : null;
  const modalContent = document.querySelector('#delete-confirm > div');
  const backdrop = document.getElementById('delete-confirm');
  if (lid) lid.classList.add('closing');

  await new Promise(r => setTimeout(r, 350));
  if (icon) icon.classList.add('trash-shake');
  if (modalContent) modalContent.classList.add('modal-shake');

  await new Promise(r => setTimeout(r, 500));
  if (icon) icon.classList.add('trash-deleting', 'trash-fall');
  if (modalContent) modalContent.classList.add('modal-crunch');
  if (backdrop) backdrop.classList.add('backdrop-fade-out');

  await new Promise(r => setTimeout(r, 400));

  try {
    await apiFetch(`${API}/${deleteTargetId}`, { method: 'DELETE' });
    showToast('Record removed', 'success');
    loadList();
    loadStats();
  } catch (e) { showToast("We were unable to remove this record from your list. Please try again.", "error"); }
  finally {
    if (btn) {
      btn.disabled = false;
      btn.textContent = 'Delete';
    }
    cancelDelete();
  }
}

function openStatusPick(id) {
  statusTargetId = id;
  const pick = document.getElementById('status-pick');
  if (pick) pick.classList.replace('hidden', 'flex');
}

function closeStatusPick(e) {
  if (e.target === document.getElementById('status-pick')) {
    document.getElementById('status-pick').classList.replace('flex', 'hidden');
  }
}

async function pickStatus(status) {
  const pick = document.getElementById('status-pick');
  if (pick) pick.classList.replace('flex', 'hidden');
  try {
    await apiFetch(`${API}/${statusTargetId}/status`, {
      method: 'PATCH',
      body: JSON.stringify({ status })
    });
    showToast('Status updated', 'success');
    loadList();
    loadStats();
  } catch (e) { showToast("We couldn't save your status change. Please check your network and try again.", "error"); }
}

function esc(str) {
  if (!str) return '';
  return str.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');
}

function initFAB() {
  const staticBtn = document.getElementById('static-add-btn');
  const floatingBtn = document.getElementById('floating-add-btn');
  if (!staticBtn || !floatingBtn) return;

  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        floatingBtn.style.transform = 'scale(0)';
        floatingBtn.style.opacity = '0';
      } else {
        floatingBtn.style.transform = 'scale(1)';
        floatingBtn.style.opacity = '1';
      }
    });
  }, { threshold: 0 });

  observer.observe(staticBtn);
}

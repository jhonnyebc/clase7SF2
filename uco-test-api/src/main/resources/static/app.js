'use strict';
const $ = selector => document.querySelector(selector);
const app = $('#app');
$('.skip-link').addEventListener('click', event => {
  event.preventDefault();
  const content = $('#contenido');
  content.setAttribute('tabindex', '-1');
  content.focus();
});
const state = { user: null, groups: [], notes: [], students: [], group: null, note: null, averages: [], query: '', filter: 'todos', revision: 0, saving: false };
const escapeHtml = value => String(value ?? '').replace(/[&<>"']/g, character => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[character]));
const number = value => new Intl.NumberFormat('es-CO', { maximumFractionDigits: 2 }).format(value);
const date = value => new Intl.DateTimeFormat('es-CO', { day: 'numeric', month: 'short', year: 'numeric' }).format(new Date(value));
const initials = (first, last) => escapeHtml(`${first?.[0] ?? ''}${last?.[0] ?? ''}`.toUpperCase());
const normalize = value => String(value).normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase();
const symbols = {
  grid: '<rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/>',
  book: '<path d="M12 5v16M3 3h5a4 4 0 0 1 4 2 4 4 0 0 1 4-2h5v16h-5a4 4 0 0 0-4 2 4 4 0 0 0-4-2H3Z"/>',
  arrow: '<path d="M5 12h14m-6-6 6 6-6 6"/>',
  check: '<path d="m5 12 4 4L19 6"/>',
  clock: '<circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/>',
  search: '<circle cx="10.5" cy="10.5" r="6.5"/><path d="m16 16 5 5"/>',
  users: '<circle cx="9" cy="7" r="3"/><path d="M3 21v-3a6 6 0 0 1 12 0v3M17 4a3 3 0 0 1 0 6m2 11v-3a6 6 0 0 0-2-4"/>',
  lock: '<rect x="5" y="10" width="14" height="11" rx="2"/><path d="M8 10V7a4 4 0 0 1 8 0v3m-4 4v3"/>',
  info: '<circle cx="12" cy="12" r="9"/><path d="M12 11v6m0-10v1"/>'
};
const icon = name => `<svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.65" stroke-linecap="round" stroke-linejoin="round">${symbols[name] || symbols.info}</svg>`;
const brand = () => '<div class="brand"><span class="brand-mark" aria-hidden="true">a</span><div><span class="brand-word">aula</span><small>ESPACIO DOCENTE</small></div></div>';

async function api(path, options = {}) {
  let response;
  try {
    response = await fetch(`/api/v1${path}`, { credentials: 'same-origin', signal: AbortSignal.timeout(15000), ...options });
  } catch {
    throw new Error('No pudimos conectar. Revisa tu conexión e inténtalo de nuevo.');
  }
  const data = await response.json().catch(() => ({}));
  if (!response.ok) {
    if (response.status === 401 && state.user && path !== '/auth/login') {
      state.revision++;
      state.user = null;
      if ($('#grade-dialog').open) $('#grade-dialog').close();
      showLogin('Tu sesión terminó. Inicia sesión de nuevo.');
    }
    const message = response.status >= 500 ? 'No pudimos completar la operación. Inténtalo de nuevo.'
      : data.error || data.mensaje || 'No pudimos completar la operación.';
    throw Object.assign(new Error(message), { status: response.status });
  }
  return data;
}
async function post(path, body, contentType = 'application/json') {
  const csrf = await api('/auth/csrf');
  return api(path, { method: 'POST', headers: { 'Content-Type': contentType, [csrf.headerName]: csrf.token }, body });
}
let toastTimeout;
function toast(message) {
  clearTimeout(toastTimeout);
  $('#toast').textContent = message;
  $('#toast').hidden = false;
  toastTimeout = setTimeout(() => { $('#toast').hidden = true; }, 6000);
}
function showLogin(message = '') {
  window.scrollTo(0, 0);
  document.title = 'Iniciar sesión · Aula';
  app.innerHTML = `<main id="contenido" class="login">
    <section class="login-story" aria-label="Aula, espacio docente">${brand()}
      <div class="story-body"><div class="story-line"></div><span class="eyebrow">UN ESPACIO PARA ENSEÑAR</span><h1>Cada nota,<br>en su lugar.</h1><p>Tus grupos, tus evaluaciones y el progreso de tus estudiantes. Todo en un mismo espacio.</p></div>
      <div class="story-foot"><span>0—5</span><span>Registra con claridad.<br>Acompaña cada aprendizaje.</span></div><div class="orbit" aria-hidden="true"></div>
    </section>
    <section class="login-side"><div class="login-box"><span class="eyebrow">BIENVENIDO A AULA</span><h2>Tu clase empieza aquí.</h2><p class="muted">Ingresa con tu cuenta de docente.</p>
      <form id="login-form"><label for="usuario">Usuario</label><input id="usuario" name="usuario" autocomplete="username" placeholder="Tu usuario docente" required maxlength="120">
        <label for="contrasena">Contraseña</label><div class="password-wrap"><input id="contrasena" name="contrasena" type="password" autocomplete="current-password" required><button id="toggle-password" type="button" aria-label="Mostrar contraseña">Mostrar</button></div>
        <p id="login-error" class="form-error" role="alert" ${message ? '' : 'hidden'}>${escapeHtml(message)}</p>
        <button class="button primary" type="submit" id="login-button">Iniciar sesión ${icon('arrow')}</button>
      </form><div class="login-caption">${icon('lock')}<span>Acceso exclusivo a tus grupos y evaluaciones.</span></div>
    </div><p class="login-footer">Aula · Registro académico</p></section></main>`;
  $('#toggle-password').addEventListener('click', () => {
    const hidden = $('#contrasena').type === 'password';
    $('#contrasena').type = hidden ? 'text' : 'password';
    $('#toggle-password').textContent = hidden ? 'Ocultar' : 'Mostrar';
    $('#toggle-password').setAttribute('aria-label', hidden ? 'Ocultar contraseña' : 'Mostrar contraseña');
  });
  $('#login-form').addEventListener('submit', async event => {
    event.preventDefault();
    const button = $('#login-button');
    button.disabled = true;
    button.textContent = 'Iniciando sesión…';
    $('#login-error').hidden = true;
    try {
      await post('/auth/login', new URLSearchParams({ usuario: $('#usuario').value.trim(), contrasena: $('#contrasena').value }).toString(), 'application/x-www-form-urlencoded');
      $('#contrasena').value = '';
      await bootstrap();
    } catch (error) {
      if ($('#login-error')) {
        $('#login-error').textContent = error.message;
        $('#login-error').hidden = false;
        button.disabled = false;
        button.innerHTML = `Iniciar sesión ${icon('arrow')}`;
      }
    }
  });
}
function shell() {
  const user = state.user;
  app.innerHTML = `<div class="layout"><aside class="sidebar">${brand()}<nav aria-label="Navegación principal"><p class="nav-label">MI ESPACIO</p><a href="#/" class="nav-link">${icon('grid')} Mis grupos</a></nav><div class="sidebar-note"><div class="mini-line"></div>Una evaluación a la vez.<br>Un aprendizaje que avanza.</div></aside>
    <div class="main-area"><header class="topbar"><span class="topbar-label">Registro académico</span><div class="topbar-right"><div class="profile"><span class="avatar">${initials(user.nombres,user.apellidos)}</span><div><b>${escapeHtml(user.nombres)} ${escapeHtml(user.apellidos)}</b><small>Docente</small></div></div><button class="logout" id="logout">Cerrar sesión</button></div></header>
    <main id="contenido" class="content" tabindex="-1"></main></div></div>`;
  $('#logout').addEventListener('click', async () => {
    const button = $('#logout');
    button.disabled = true;
    try {
      await post('/auth/logout', '');
      state.user = null; state.revision++; state.groups = []; state.students = []; state.notes = [];
      history.replaceState(null, '', '#/');
      showLogin();
      toast('Cerraste sesión correctamente.');
    } catch (error) {
      toast(error.message);
      if ($('#logout')) $('#logout').disabled = false;
    }
  });
}
const loading = () => '<div class="loading" role="status"><span class="spinner" aria-hidden="true"></span>Cargando tu información…</div>';
const stat = (label, value, detail, symbol, accent = false) => `<div class="stat${accent ? ' accent' : ''}"><span class="stat-label">${label}${icon(symbol)}</span><span class="stat-value">${value}</span><small>${detail}</small></div>`;
function empty(title, text) { return `<div class="empty">${icon('book')}<h3>${title}</h3><p>${text}</p></div>`; }
function crumbs(note = false) {
  return `<nav class="breadcrumbs" aria-label="Ruta de navegación"><a href="#/">Mis grupos</a><span aria-hidden="true">/</span>${note ? `<a href="#/grupo/${state.group.id}">${escapeHtml(state.group.nombre)}</a><span aria-hidden="true">/</span><span aria-current="page">${escapeHtml(state.note.nombre)}</span>` : `<span aria-current="page">${escapeHtml(state.group.nombre)}</span>`}</nav>`;
}
function groupsView() {
  document.title = 'Mis grupos · Aula';
  $('#contenido').innerHTML = `<div class="page-heading"><div><span class="eyebrow">TU ESPACIO DE TRABAJO</span><h1>Hola, ${escapeHtml(state.user.nombres.split(' ')[0])}.</h1><p class="muted">Elige un grupo para consultar sus evaluaciones y registrar notas.</p></div></div>
    <div class="section-heading"><h2>Mis grupos</h2><small>${state.groups.length} ${state.groups.length === 1 ? 'grupo asignado' : 'grupos asignados'}</small></div>
    ${state.groups.length ? `<div class="group-grid">${state.groups.map((group,index) => `<article class="group-card"><div class="card-top"><span class="card-icon">${icon('book')}</span><span class="card-index">${String(index+1).padStart(2,'0')}</span></div><h3>${escapeHtml(group.nombre)}</h3><p>Evaluaciones y registro de calificaciones</p><div class="card-footer"><a href="#/grupo/${group.id}" aria-label="Abrir ${escapeHtml(group.nombre)}">Abrir grupo ${icon('arrow')}</a></div></article>`).join('')}</div>` : `<div class="panel">${empty('Aún no tienes grupos','Cuando tengas un grupo asignado, lo encontrarás aquí.')}</div>`}
    <div class="tip">${icon('info')}<p>En cada evaluación puedes consultar las notas registradas e identificar a los estudiantes que aún están pendientes.</p></div>`;
}
function notesView(ponderacion) {
  document.title = `${state.group.nombre} · Aula`;
  const notes = state.notes, open = notes.filter(n => n.vigente).length;
  $('#contenido').innerHTML = `${crumbs()}<div class="page-heading"><div><span class="eyebrow">EVALUACIONES DEL GRUPO</span><h1>${escapeHtml(state.group.nombre)}</h1><p class="muted">Selecciona una evaluación para consultar o registrar las notas.</p></div><a class="button primary" href="#/grupo/${state.group.id}/promedios">Ver promedios ${icon('arrow')}</a></div>
    <div class="summary">${stat('Evaluaciones',notes.length,'En este grupo','book')}${stat('Abiertas',open,'Disponibles para registrar','clock',true)}${stat('Ponderación',`${number(ponderacion.total)}<small> %</small>`,ponderacion.valida ? 'Distribución completa' : 'Distribución por revisar','check')}</div>
    <section class="panel" aria-label="Evaluaciones"><div class="panel-head"><h2>Plan de evaluación</h2><span class="muted help">Peso sobre la nota final</span></div>
      ${notes.length ? notes.map(note => `<article class="evaluation-row"><div><div class="evaluation-name">${escapeHtml(note.nombre)}</div><small>Hasta ${date(note.fechaRegistroHasta)}</small></div><div class="weight">${note.porcentaje == null ? '—' : number(note.porcentaje)}<span>%</span></div><div><span class="badge ${note.vigente ? 'green' : 'gray'}">${note.vigente ? 'Abierta' : 'Fuera de plazo'}</span></div><a class="button secondary table-action" href="#/grupo/${state.group.id}/evaluacion/${note.id}">Ver estudiantes</a></article>`).join('') : empty('Sin evaluaciones','Este grupo todavía no tiene evaluaciones asignadas.')}
    </section><div class="notice weight-notice ${ponderacion.valida ? '' : 'warning'}">${icon(ponderacion.valida ? 'check' : 'info')}<div><strong>${escapeHtml(ponderacion.mensaje)}.</strong> ${ponderacion.valida ? 'Cada evaluación tiene un peso definido en la calificación del grupo.' : 'No se puede obtener una nota definitiva hasta completar una distribución válida.'}</div></div>`;
}
function studentsView() {
  document.title = `${state.note.nombre} · ${state.group.nombre} · Aula`;
  const registered = state.students.filter(s => s.registrada).length;
  const pending = state.students.filter(s => s.activo && !s.registrada).length;
  $('#contenido').innerHTML = `${crumbs(true)}<div class="page-heading"><div><span class="eyebrow">REGISTRO DE CALIFICACIONES</span><h1>${escapeHtml(state.note.nombre)}</h1><p class="muted">${escapeHtml(state.group.nombre)} <span aria-hidden="true">·</span> ${state.note.porcentaje == null ? 'Porcentaje sin asignar' : `${number(state.note.porcentaje)} % de la nota final`} <span aria-hidden="true">·</span> Cierre: ${date(state.note.fechaRegistroHasta)}</p></div><div class="heading-actions"><a class="button secondary" href="#/grupo/${state.group.id}/promedios">Ver promedios</a><button class="button secondary" id="refresh">Actualizar</button></div></div>
    <div class="summary">${stat('Estudiantes',state.students.length,'Integrantes del grupo','users')}${stat('Registradas',registered,'Notas guardadas','check',true)}${stat('Pendientes',pending,'Estudiantes activos sin nota','clock')}</div>
    ${state.note.vigente ? '' : '<p class="notice warning">Esta evaluación está fuera del plazo de registro. Puedes consultar las notas, pero no guardar nuevas calificaciones.</p>'}
    <section class="panel" aria-label="Estudiantes"><div class="toolbar"><div class="search">${icon('search')}<label class="skip-link" for="search-student">Buscar estudiante</label><input id="search-student" type="search" placeholder="Buscar por nombre o identificación" value="${escapeHtml(state.query)}"></div><label class="skip-link" for="filter">Filtrar estudiantes</label><select id="filter"><option value="todos">Todos los estados</option><option value="pendientes">Pendientes</option><option value="registradas">Registradas</option><option value="inactivos">Inactivos</option></select></div>
    <div class="table-wrap"><table><thead><tr><th scope="col">Estudiante</th><th scope="col">Estado</th><th scope="col">Nota</th><th scope="col">Registro</th></tr></thead><tbody id="student-rows"></tbody></table></div>
    <div class="table-footer"><span id="result-count" role="status"></span><span>Escala de 0,0 a 5,0 · Un decimal</span></div></section>`;
  $('#filter').value = state.filter;
  $('#search-student').addEventListener('input', event => { state.query = event.target.value; studentRows(); });
  $('#filter').addEventListener('change', event => { state.filter = event.target.value; studentRows(); });
  $('#refresh').addEventListener('click', () => route(false));
  studentRows();
}
function studentRows() {
  const query = normalize(state.query.trim());
  const students = state.students.filter(s => normalize(`${s.nombres} ${s.apellidos} ${s.identificacion}`).includes(query)
    && (state.filter === 'todos' || state.filter === 'pendientes' && s.activo && !s.registrada || state.filter === 'registradas' && s.registrada || state.filter === 'inactivos' && !s.activo));
  $('#student-rows').innerHTML = students.length ? students.map(s => {
    const available = !s.registrada && s.activo && state.note.vigente;
    return `<tr class="${s.activo ? '' : 'row-disabled'}"><td><div class="student-name"><span class="avatar">${initials(s.nombres,s.apellidos)}</span><div><strong>${escapeHtml(s.nombres)} ${escapeHtml(s.apellidos)}</strong><small>ID ${escapeHtml(s.identificacion)}</small></div></div></td>
      <td><span class="badge ${!s.activo ? 'gray' : s.registrada ? 'green' : 'amber'}">${!s.activo ? 'Inactivo' : s.registrada ? 'Registrada' : 'Pendiente'}</span></td><td><span class="grade-number">${s.registrada ? Number(s.valor).toFixed(1).replace('.',',') : '—'}</span></td>
      <td>${available ? `<button class="button secondary table-action" data-grade="${s.idEstudiante}" aria-label="Registrar nota de ${escapeHtml(s.nombres)} ${escapeHtml(s.apellidos)}">Registrar nota</button>` : `<span class="help">${s.registrada ? 'Guardada' : !s.activo ? 'Estudiante inactivo' : 'Plazo cerrado'}</span>`}</td></tr>`;
  }).join('') : `<tr><td colspan="4">${empty(state.students.length ? 'No encontramos coincidencias' : 'Sin estudiantes', state.students.length ? 'Prueba con otro nombre o cambia el filtro.' : 'Este grupo todavía no tiene estudiantes.')}</td></tr>`;
  $('#result-count').textContent = `${students.length} de ${state.students.length} estudiantes`;
  document.querySelectorAll('[data-grade]').forEach(button => button.addEventListener('click', () => openGrade(button.dataset.grade)));
}
const averageLabels = { SIN_NOTAS: 'Sin notas', PARCIAL: 'Parcial', DEFINITIVA: 'Definitiva', CONFIGURACION_INVALIDA: 'Sin cálculo' };
const gradeFormat = value => value == null ? '—' : new Intl.NumberFormat('es-CO', { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(value);
function averagesView(data) {
  document.title = `Promedios · ${state.group.nombre} · Aula`;
  const students = data.estudiantes;
  $('#contenido').innerHTML = `<nav class="breadcrumbs" aria-label="Ruta de navegación"><a href="#/">Mis grupos</a><span>/</span><a href="#/grupo/${state.group.id}">${escapeHtml(state.group.nombre)}</a><span>/</span><span aria-current="page">Promedios</span></nav>
    <div class="page-heading"><div><span class="eyebrow">EL PROGRESO DEL GRUPO</span><h1>Promedios ponderados</h1><p class="muted">${escapeHtml(state.group.nombre)} · Cada evaluación aporta según su porcentaje.</p></div><button class="button secondary" id="refresh-averages">Actualizar</button></div>
    <div class="summary">${stat('Con nota definitiva',students.filter(s => s.estado === 'DEFINITIVA').length,'Todas las evaluaciones registradas','check',true)}${stat('Con acumulado parcial',students.filter(s => s.estado === 'PARCIAL').length,'Aún tienen evaluaciones pendientes','clock')}${stat('Estudiantes',students.length,'Integrantes del grupo','users')}</div>
    <p class="notice ${data.ponderacion.valida ? '' : 'warning'}">${data.ponderacion.valida ? '<strong>El acumulado parcial no es la nota definitiva.</strong> Se suman los aportes de las notas registradas, sin convertir las pendientes en cero ni redistribuir sus porcentajes.' : `<strong>Cálculo no disponible.</strong> ${escapeHtml(data.ponderacion.mensaje)}. Revisa la distribución de las evaluaciones.`}</p>
    <section class="panel" aria-label="Promedios de estudiantes"><div class="toolbar"><div class="search">${icon('search')}<label class="skip-link" for="average-search">Buscar estudiante</label><input id="average-search" type="search" placeholder="Buscar por nombre o identificación" value="${escapeHtml(state.query)}"></div><label class="skip-link" for="average-filter">Filtrar promedios</label><select id="average-filter"><option value="todos">Todos los estados</option><option value="PARCIAL">Parciales</option><option value="DEFINITIVA">Definitivas</option><option value="SIN_NOTAS">Sin notas</option><option value="CONFIGURACION_INVALIDA">Sin cálculo</option></select></div>
    <div class="table-wrap"><table><thead><tr><th scope="col">Estudiante</th><th scope="col">Evaluado</th><th scope="col">Acumulado</th><th scope="col">Definitiva</th><th scope="col">Estado</th></tr></thead><tbody id="average-rows"></tbody></table></div><div class="table-footer"><span id="average-count" role="status"></span><span>Escala de 0 a 5 · Total redondeado a dos decimales</span></div></section>
    <div class="tip">${icon('info')}<p>Abre el detalle de un estudiante para ver cada aporte. Las evaluaciones fuera de plazo siguen formando parte de la nota final.</p></div>`;
  $('#average-filter').value = ['todos','PARCIAL','DEFINITIVA','SIN_NOTAS','CONFIGURACION_INVALIDA'].includes(state.filter) ? state.filter : 'todos';
  state.filter = $('#average-filter').value;
  $('#average-search').addEventListener('input', event => { state.query = event.target.value; averageRows(); });
  $('#average-filter').addEventListener('change', event => { state.filter = event.target.value; averageRows(); });
  $('#refresh-averages').addEventListener('click', () => route(false));
  averageRows();
}
function averageRows() {
  const query = normalize(state.query.trim());
  const students = state.averages.filter(s => normalize(`${s.nombres} ${s.apellidos} ${s.identificacion}`).includes(query) && (state.filter === 'todos' || s.estado === state.filter));
  $('#average-rows').innerHTML = students.length ? students.map(s => `<tr class="average-main"><td><div class="student-name"><span class="avatar">${initials(s.nombres,s.apellidos)}</span><div><strong>${escapeHtml(s.nombres)} ${escapeHtml(s.apellidos)}</strong><small>ID ${escapeHtml(s.identificacion)}${s.activo ? '' : ' · Inactivo'}</small></div></div></td>
      <td><strong>${s.porcentajeEvaluado == null ? '—' : `${number(s.porcentajeEvaluado)} %`}</strong><div class="help">${s.registradas} de ${s.evaluaciones.length} notas</div></td><td><span class="grade-number">${gradeFormat(s.acumulado)}</span></td><td><span class="grade-number">${gradeFormat(s.definitiva)}</span></td><td><span class="badge ${s.estado === 'DEFINITIVA' ? 'green' : s.estado === 'PARCIAL' ? 'amber' : 'gray'}">${averageLabels[s.estado] || 'Sin cálculo'}</span></td></tr>
      <tr class="average-detail"><td colspan="5"><details><summary>Ver cálculo de ${escapeHtml(s.nombres)} ${escapeHtml(s.apellidos)} <span>${s.pendientes} ${s.pendientes === 1 ? 'pendiente' : 'pendientes'}</span></summary>
      ${s.evaluaciones.length ? `<table class="detail-table"><thead><tr><th>Evaluación</th><th>Peso</th><th>Nota</th><th>Aporte</th></tr></thead><tbody>${s.evaluaciones.map(e => `<tr><td><a href="#/grupo/${state.group.id}/evaluacion/${e.idNota}">${escapeHtml(e.nombre)}</a>${e.valor == null ? `<small class="detail-pending">${e.vigente ? 'Pendiente de registro' : 'Pendiente · fuera de plazo'}</small>` : ''}</td><td>${e.porcentaje == null ? '—' : `${number(e.porcentaje)} %`}</td><td>${e.valor == null ? 'Pendiente' : number(e.valor)}</td><td>${e.aporte == null ? '—' : new Intl.NumberFormat('es-CO',{maximumFractionDigits:5}).format(e.aporte)}</td></tr>`).join('')}</tbody></table><p class="detail-formula">Aporte = nota × porcentaje ÷ 100. Los aportes se suman antes de redondear el total.</p>` : '<p class="help">No hay evaluaciones en este grupo.</p>'}</details></td></tr>`).join('') : `<tr><td colspan="5">${empty('Sin resultados','No hay estudiantes que coincidan con esta búsqueda o filtro.')}</td></tr>`;
  $('#average-count').textContent = `${students.length} de ${state.averages.length} estudiantes`;
}
let selectedStudent = null;
function openGrade(id) {
  selectedStudent = state.students.find(student => student.idEstudiante === id);
  if (!selectedStudent || !selectedStudent.activo || selectedStudent.registrada || !state.note.vigente) return;
  $('#grade-student').textContent = `${selectedStudent.nombres} ${selectedStudent.apellidos}`;
  $('#grade-context').textContent = `${state.group.nombre} · ${state.note.nombre}`;
  $('#grade-value').value = '';
  $('#grade-error').hidden = true;
  $('#grade-dialog').showModal();
  $('#grade-value').focus();
}
function closeDialog() { if (!state.saving) $('#grade-dialog').close(); }
$('#close-dialog').addEventListener('click', closeDialog);
$('#cancel-grade').addEventListener('click', closeDialog);
$('#grade-dialog').addEventListener('cancel', event => { if (state.saving) event.preventDefault(); });
$('#grade-form').addEventListener('submit', async event => {
  event.preventDefault();
  if (state.saving) return;
  const raw = $('#grade-value').value.trim().replace(',', '.');
  const errorElement = $('#grade-error');
  if (!/^\d(?:\.\d)?$/.test(raw) || Number(raw) > 5) {
    errorElement.textContent = 'Escribe una nota entre 0 y 5, con máximo un decimal.';
    errorElement.hidden = false;
    return;
  }
  const student = selectedStudent, noteId = state.note.id;
  state.saving = true;
  errorElement.hidden = true;
  $('#save-grade').disabled = true;
  $('#save-grade').textContent = 'Guardando…';
  $('#grade-value').disabled = true;
  $('#cancel-grade').disabled = true;
  $('#close-dialog').disabled = true;
  try {
    await post('/notas-estudiantes', JSON.stringify({ idEstudiante: student.idEstudiante, idNota: noteId, valor: Number(raw) }));
    student.valor = Number(raw); student.registrada = true;
    $('#grade-dialog').close();
    if (state.user && state.note?.id === noteId) studentsView();
    toast(`Nota de ${student.nombres} guardada correctamente.`);
  } catch (error) {
    errorElement.textContent = error.message;
    errorElement.hidden = false;
  } finally {
    state.saving = false;
    $('#save-grade').disabled = false;
    $('#save-grade').textContent = 'Guardar nota';
    $('#grade-value').disabled = false;
    $('#cancel-grade').disabled = false;
    $('#close-dialog').disabled = false;
  }
});
async function route(resetSearch = true) {
  if (!state.user) return;
  if (resetSearch) window.scrollTo(0, 0);
  const revision = ++state.revision;
  const content = $('#contenido');
  content.innerHTML = loading();
  if ($('#grade-dialog').open && !state.saving) $('#grade-dialog').close();
  if (resetSearch) { state.query = ''; state.filter = 'todos'; }
  try {
    const parts = location.hash.replace(/^#\/?/, '').split('/').filter(Boolean);
    if (parts.length === 0) { groupsView(); return; }
    const group = state.groups.find(group => group.id === parts[1]);
    if (parts[0] !== 'grupo' || !group) throw new Error('Este grupo no está disponible en tu cuenta.');
    const [notes, weights] = await Promise.all([api(`/grupos/${group.id}/notas`), api(`/grupos/${group.id}/ponderacion`)]);
    if (revision !== state.revision || !state.user) return;
    state.group = group; state.notes = notes;
    if (parts.length === 2) { notesView(weights); return; }
    if (parts.length === 3 && parts[2] === 'promedios') {
      const averages = await api(`/grupos/${group.id}/promedios`);
      if (revision !== state.revision || !state.user) return;
      state.averages = averages.estudiantes;
      averagesView(averages);
      return;
    }
    const note = notes.find(note => note.id === parts[3]);
    if (parts[2] !== 'evaluacion' || !note || parts.length !== 4) throw new Error('Esta evaluación no está disponible.');
    const students = await api(`/notas/${note.id}/estudiantes`);
    if (revision !== state.revision || !state.user) return;
    state.note = note; state.students = students;
    studentsView();
  } catch (error) {
    if (revision === state.revision && state.user) {
      content.innerHTML = `${empty('No pudimos abrir esta vista',escapeHtml(error.message))}<div style="text-align:center"><button class="button secondary" id="retry">Reintentar</button> <a class="button primary" href="#/">Mis grupos</a></div>`;
      $('#retry').addEventListener('click', () => route(false));
    }
  }
}
async function bootstrap() {
  try {
    state.user = await api('/auth/me');
    state.groups = await api('/mis-grupos');
    shell();
    await route();
  } catch (error) {
    state.user = null;
    showLogin(error.status === 401 ? '' : error.message);
  }
}
window.addEventListener('hashchange', () => route());
bootstrap();


<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    String cbUri = request.getRequestURI();
    String cbPage = cbUri != null ? cbUri.substring(cbUri.lastIndexOf("/") + 1).toLowerCase() : "";
    String cbRole = "GUEST";

    if (session.getAttribute("admin") != null
            || cbPage.contains("adminhome")
            || cbPage.contains("stations.jsp")
            || cbPage.contains("managestation")
            || cbPage.contains("viewslots")
            || cbPage.contains("stationbookings")
            || cbPage.contains("bookinghistory")) {
        cbRole = "ADMIN";
    } else if (session.getAttribute("email") != null
            || session.getAttribute("user") != null
            || cbPage.contains("userhome")
            || cbPage.contains("search.jsp")
            || cbPage.contains("searchslot")
            || cbPage.contains("mybookings")
            || cbPage.contains("payment.jsp")) {
        cbRole = "USER";
    }

    // Extract stationId from current URL if present (e.g. ?stationId=4) so the widget can offer 1-click station context
    String pageStationId = request.getParameter("stationId");
    if (pageStationId == null || !pageStationId.matches("\\d+")) {
        pageStationId = "";
    }
%>
<!-- ======= Plugy Guided Decision-Tree Chatbot Widget ======= -->
<style>
  .plugy-cb-fab {
    position: fixed;
    bottom: 24px;
    right: 24px;
    height: 56px;
    padding: 0 20px;
    border-radius: 999px;
    background: linear-gradient(135deg, #01036f 0%, #0205a1 55%, #1acc8d 100%);
    color: #ffffff;
    border: 2px solid rgba(255, 255, 255, 0.25);
    box-shadow: 0 10px 28px rgba(1, 3, 111, 0.38);
    display: flex;
    align-items: center;
    gap: 10px;
    font-family: 'Poppins', 'Open Sans', sans-serif;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    z-index: 9999;
    transition: transform 0.2s ease, box-shadow 0.2s ease;
  }
  .plugy-cb-fab:hover {
    transform: translateY(-2px);
    box-shadow: 0 14px 32px rgba(1, 3, 111, 0.48);
  }
  .plugy-cb-fab-bolt {
    font-size: 18px;
    line-height: 1;
  }

  .plugy-cb-window {
    position: fixed;
    bottom: 92px;
    right: 24px;
    width: 420px;
    max-width: calc(100vw - 28px);
    height: 610px;
    max-height: calc(100vh - 116px);
    background: #f8fafc;
    border-radius: 20px;
    box-shadow: 0 20px 50px rgba(15, 23, 42, 0.28);
    border: 1px solid #cbd5e1;
    display: none;
    flex-direction: column;
    overflow: hidden;
    z-index: 9999;
    font-family: 'Poppins', 'Open Sans', sans-serif;
  }
  .plugy-cb-window.open {
    display: flex;
  }

  /* Header */
  .plugy-cb-header {
    background: linear-gradient(135deg, #01036f 0%, #0205a1 100%);
    color: #ffffff;
    padding: 13px 16px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    flex-shrink: 0;
  }
  .plugy-cb-brand {
    display: flex;
    align-items: center;
    gap: 10px;
  }
  .plugy-cb-brand-icon {
    width: 34px;
    height: 34px;
    border-radius: 10px;
    background: rgba(255, 255, 255, 0.14);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 18px;
  }
  .plugy-cb-brand h6 {
    margin: 0;
    font-size: 14.5px;
    font-weight: 700;
    color: #ffffff;
    line-height: 1.2;
  }
  .plugy-cb-badge {
    display: inline-block;
    font-size: 10px;
    padding: 2px 7px;
    border-radius: 999px;
    background: rgba(26, 204, 141, 0.25);
    color: #6ee7b7;
    font-weight: 600;
    letter-spacing: 0.4px;
    margin-top: 2px;
  }
  .plugy-cb-header-right {
    display: flex;
    align-items: center;
    gap: 6px;
  }
  .plugy-cb-lang-group {
    display: flex;
    background: rgba(255, 255, 255, 0.12);
    border-radius: 8px;
    padding: 2px;
  }
  .plugy-cb-lang-btn {
    background: transparent;
    border: none;
    color: rgba(255, 255, 255, 0.78);
    font-size: 11px;
    font-weight: 600;
    padding: 3px 7px;
    border-radius: 6px;
    cursor: pointer;
    transition: all 0.15s ease;
  }
  .plugy-cb-lang-btn.active {
    background: #1acc8d;
    color: #01036f;
  }
  .plugy-cb-close-btn {
    background: rgba(255, 255, 255, 0.12);
    border: none;
    color: #ffffff;
    width: 30px;
    height: 30px;
    border-radius: 8px;
    font-size: 16px;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  .plugy-cb-close-btn:hover {
    background: rgba(255, 255, 255, 0.24);
  }

  /* Breadcrumb & Navigation Bar */
  .plugy-cb-navbar {
    background: #ffffff;
    border-bottom: 1px solid #e2e8f0;
    padding: 8px 14px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 8px;
    flex-shrink: 0;
  }
  .plugy-cb-breadcrumb {
    font-size: 11.5px;
    font-weight: 600;
    color: #64748b;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    max-width: 220px;
  }
  .plugy-cb-nav-actions {
    display: flex;
    gap: 6px;
  }
  .plugy-cb-nav-btn {
    background: #f1f5f9;
    border: 1px solid #cbd5e1;
    color: #1e293b;
    border-radius: 7px;
    padding: 4px 9px;
    font-size: 11.5px;
    font-weight: 600;
    cursor: pointer;
    display: inline-flex;
    align-items: center;
    gap: 4px;
    transition: all 0.15s ease;
  }
  .plugy-cb-nav-btn:hover:not(:disabled) {
    background: #e2e8f0;
    border-color: #94a3b8;
  }
  .plugy-cb-nav-btn:disabled {
    opacity: 0.45;
    cursor: default;
  }
  .plugy-cb-nav-home {
    background: #eff6ff;
    border-color: #bfdbfe;
    color: #0205a1;
  }
  .plugy-cb-nav-home:hover {
    background: #dbeafe;
  }

  /* Scrollable Content Area */
  .plugy-cb-body {
    flex: 1;
    padding: 14px;
    overflow-y: auto;
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  /* Answer Card */
  .plugy-cb-card {
    background: #ffffff;
    border: 1px solid #e2e8f0;
    border-radius: 14px;
    padding: 14px 15px;
    box-shadow: 0 2px 8px rgba(15, 23, 42, 0.04);
  }
  .plugy-cb-card-title {
    margin: 0 0 6px 0;
    font-size: 15px;
    font-weight: 700;
    color: #0f172a;
    line-height: 1.35;
  }
  .plugy-cb-card-reply {
    margin: 0;
    font-size: 13px;
    color: #334155;
    line-height: 1.55;
    white-space: pre-wrap;
  }

  /* Spaced Bullet Items (Fixes Clustered Reply Issue) */
  .plugy-cb-bullet-list {
    list-style: none;
    padding: 0;
    margin: 10px 0 0 0;
    display: flex;
    flex-direction: column;
    gap: 8px;
  }
  .plugy-cb-bullet-item {
    background: #f8fafc;
    border: 1px solid #e2e8f0;
    border-left: 3.5px solid #0205a1;
    border-radius: 9px;
    padding: 9px 11px;
    font-size: 12.5px;
    color: #1e293b;
    line-height: 1.5;
  }

  /* Simple Everyday Norm / Translation Callout Box */
  .plugy-cb-norm-box {
    background: #ecfdf5;
    border: 1px solid #a7f3d0;
    border-radius: 12px;
    padding: 10px 13px;
    font-size: 12.5px;
    color: #065f46;
    line-height: 1.5;
  }

  /* Actionable Teleport Buttons (Deep Links into Project Pages) */
  .plugy-cb-actions-wrap {
    display: flex;
    flex-direction: column;
    gap: 7px;
  }
  .plugy-cb-action-label {
    font-size: 11px;
    font-weight: 700;
    text-transform: uppercase;
    letter-spacing: 0.5px;
    color: #64748b;
    margin-bottom: 2px;
  }
  .plugy-cb-action-btn {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px 14px;
    border-radius: 11px;
    font-size: 13px;
    font-weight: 600;
    text-decoration: none !important;
    transition: transform 0.15s ease, box-shadow 0.15s ease;
  }
  .plugy-cb-action-btn.style-primary {
    background: linear-gradient(135deg, #0205a1 0%, #1d4ed8 100%);
    color: #ffffff !important;
    box-shadow: 0 4px 12px rgba(2, 5, 161, 0.2);
  }
  .plugy-cb-action-btn.style-emerald {
    background: linear-gradient(135deg, #059669 0%, #10b981 100%);
    color: #ffffff !important;
    box-shadow: 0 4px 12px rgba(5, 150, 105, 0.2);
  }
  .plugy-cb-action-btn.style-outline {
    background: #ffffff;
    border: 1px solid #cbd5e1;
    color: #1e293b !important;
  }
  .plugy-cb-action-btn:hover {
    transform: translateY(-1px);
    opacity: 0.95;
  }

  /* Interactive Guided Question Options */
  .plugy-cb-options-wrap {
    display: flex;
    flex-direction: column;
    gap: 7px;
  }
  .plugy-cb-options-header {
    font-size: 11.5px;
    font-weight: 700;
    color: #475569;
    margin-top: 2px;
  }
  .plugy-cb-option-btn {
    width: 100%;
    text-align: left;
    background: #ffffff;
    border: 1px solid #cbd5e1;
    border-radius: 12px;
    padding: 10px 13px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 10px;
    cursor: pointer;
    transition: all 0.15s ease;
  }
  .plugy-cb-option-btn:hover {
    border-color: #0205a1;
    background: #eff6ff;
    transform: translateX(2px);
  }
  .plugy-cb-option-left {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 13px;
    font-weight: 600;
    color: #1e293b;
    line-height: 1.35;
  }
  .plugy-cb-option-icon {
    font-size: 16px;
    flex-shrink: 0;
  }
  .plugy-cb-option-right {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-shrink: 0;
  }
  .plugy-cb-option-badge {
    font-size: 10px;
    font-weight: 600;
    padding: 2px 7px;
    border-radius: 999px;
    background: #e0f2fe;
    color: #0369a1;
  }
  .plugy-cb-option-arrow {
    color: #64748b;
    font-size: 16px;
    font-weight: 700;
  }

  /* Footer Quick Area/Keyword Search */
  .plugy-cb-footer {
    padding: 9px 12px;
    background: #ffffff;
    border-top: 1px solid #e2e8f0;
    display: flex;
    align-items: center;
    gap: 8px;
    flex-shrink: 0;
  }
  .plugy-cb-footer input {
    flex: 1;
    border: 1px solid #cbd5e1;
    border-radius: 999px;
    padding: 7px 13px;
    font-size: 12.5px;
    outline: none;
    color: #1e293b;
  }
  .plugy-cb-footer input:focus {
    border-color: #0205a1;
  }
  .plugy-cb-send {
    width: 34px;
    height: 34px;
    border-radius: 50%;
    border: none;
    background: #1acc8d;
    color: #ffffff;
    font-size: 14px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    flex-shrink: 0;
  }
  .plugy-cb-send:hover {
    background: #15a874;
  }
</style>

<button type="button" class="plugy-cb-fab" id="plugyCbToggle" aria-label="Open Plugy EV Assistant">
  <span class="plugy-cb-fab-bolt">&#9889;</span>
  <span>EV Help &amp; Guide</span>
</button>

<div class="plugy-cb-window" id="plugyCbWindow" data-role="<%= cbRole %>" data-station="<%= pageStationId %>">
  <!-- Header -->
  <div class="plugy-cb-header">
    <div class="plugy-cb-brand">
      <div class="plugy-cb-brand-icon">&#9889;</div>
      <div>
        <h6>Plugy Assistant</h6>
        <span class="plugy-cb-badge" id="plugyCbBadge"><%= cbRole %> GUIDE</span>
      </div>
    </div>
    <div class="plugy-cb-header-right">
      <div class="plugy-cb-lang-group" title="Switch Simple Explanation Language">
        <button type="button" class="plugy-cb-lang-btn active" data-lang="en">EN</button>
        <button type="button" class="plugy-cb-lang-btn" data-lang="hi">हिं</button>
        <button type="button" class="plugy-cb-lang-btn" data-lang="mr">मरा</button>
      </div>
      <button type="button" class="plugy-cb-close-btn" id="plugyCbClose" title="Minimize">&#10005;</button>
    </div>
  </div>

  <!-- Breadcrumb & Navigation Bar -->
  <div class="plugy-cb-navbar">
    <div class="plugy-cb-breadcrumb" id="plugyCbBreadcrumb" title="Current Path">🏠 Main Menu</div>
    <div class="plugy-cb-nav-actions">
      <button type="button" class="plugy-cb-nav-btn" id="plugyCbBackBtn" disabled>
        <span>&#8592;</span> <span id="plugyCbBackText">Back</span>
      </button>
      <button type="button" class="plugy-cb-nav-btn plugy-cb-nav-home" id="plugyCbHomeBtn" title="Return to Main Menu">
        <span>&#8962;</span> <span id="plugyCbHomeText">Menu</span>
      </button>
    </div>
  </div>

  <!-- Structured Card Body -->
  <div class="plugy-cb-body" id="plugyCbBody"></div>

  <!-- Optional Quick Search Bar -->
  <form class="plugy-cb-footer" id="plugyCbForm" autocomplete="off">
    <input type="text" id="plugyCbInput" maxlength="120" placeholder="Or type an area (e.g. Wakad, Hadapsar, CCS2)..." />
    <button type="submit" class="plugy-cb-send" title="Search">&#10148;</button>
  </form>
</div>

<script>
(function() {
  var STORAGE_KEY = 'plugy_guided_cb_v4';
  var ENDPOINT = '<%= request.getContextPath() %>/ChatbotController';
  var win = document.getElementById('plugyCbWindow');
  var badgeEl = document.getElementById('plugyCbBadge');
  var breadcrumbEl = document.getElementById('plugyCbBreadcrumb');
  var role = win.getAttribute('data-role') || 'GUEST';
  var pageStationId = win.getAttribute('data-station') || '';
  var currentPage = window.location.pathname.substring(window.location.pathname.lastIndexOf('/') + 1) || 'index.jsp';

  var toggleBtn = document.getElementById('plugyCbToggle');
  var closeBtn = document.getElementById('plugyCbClose');
  var backBtn = document.getElementById('plugyCbBackBtn');
  var homeBtn = document.getElementById('plugyCbHomeBtn');
  var backTextEl = document.getElementById('plugyCbBackText');
  var homeTextEl = document.getElementById('plugyCbHomeText');
  var bodyEl = document.getElementById('plugyCbBody');
  var formEl = document.getElementById('plugyCbForm');
  var inputEl = document.getElementById('plugyCbInput');
  var langBtns = document.querySelectorAll('.plugy-cb-lang-btn');

  var UI_LABELS = {
    en: { back: 'Back', home: 'Menu', choose: 'Choose a question or station below:', actionHeader: 'Quick Page Shortcuts:', placeholder: 'Or type an area (e.g. Wakad, Hadapsar, CCS2)...' },
    hi: { back: 'पीछे', home: 'मेनू', choose: 'नीचे एक प्रश्न या स्टेशन चुनें:', actionHeader: 'सीधे पेज पर जाएँ:', placeholder: 'या एरिया का नाम लिखें (जैसे Wakad, Hadapsar)...' },
    mr: { back: 'मागे', home: 'मेनू', choose: 'खालीलपैकी एक प्रश्न किंवा स्टेशन निवडा:', actionHeader: 'थेट पेजवर जा:', placeholder: 'किंवा परिसराचे नाव टाका (उदा. Wakad, Hadapsar)...' }
  };

  function loadState() {
    try {
      var raw = sessionStorage.getItem(STORAGE_KEY);
      if (!raw) return null;
      var parsed = JSON.parse(raw);
      if (parsed.role !== role) return null;
      return parsed;
    } catch (e) {
      return null;
    }
  }

  function saveState() {
    try {
      sessionStorage.setItem(STORAGE_KEY, JSON.stringify(state));
    } catch (e) {}
  }

  var state = loadState() || {
    role: role,
    isOpen: false,
    lang: 'en',
    historyStack: [],
    currentData: null
  };

  function updateLangUI() {
    var l = state.lang || 'en';
    langBtns.forEach(function(btn) {
      if (btn.getAttribute('data-lang') === l) btn.classList.add('active');
      else btn.classList.remove('active');
    });
    var labels = UI_LABELS[l] || UI_LABELS.en;
    backTextEl.textContent = labels.back;
    homeTextEl.textContent = labels.home;
    inputEl.placeholder = labels.placeholder;
  }

  function renderCurrentNode() {
    if (state.isOpen) win.classList.add('open');
    else win.classList.remove('open');

    updateLangUI();

    if (badgeEl && state.role) {
      badgeEl.textContent = state.role + ' GUIDE';
    }

    var d = state.currentData;
    if (!d) return;

    // Enable Back button if historyStack has items or node is not ROOT
    backBtn.disabled = (d.nodeId === 'ROOT' && state.historyStack.length === 0);
    breadcrumbEl.textContent = d.breadcrumb || '🏠 Main Menu';
    breadcrumbEl.title = d.breadcrumb || '🏠 Main Menu';

    bodyEl.innerHTML = '';
    var labels = UI_LABELS[state.lang] || UI_LABELS.en;

    // 1. Main Answer Card (Title + Intro + Spaced Bullet Cards)
    var card = document.createElement('div');
    card.className = 'plugy-cb-card';

    if (d.title) {
      var h = document.createElement('h5');
      h.className = 'plugy-cb-card-title';
      h.textContent = d.title;
      card.appendChild(h);
    }

    if (d.reply) {
      var p = document.createElement('p');
      p.className = 'plugy-cb-card-reply';
      p.textContent = d.reply;
      card.appendChild(p);
    }

    if (d.bullets && d.bullets.length > 0) {
      var ul = document.createElement('ul');
      ul.className = 'plugy-cb-bullet-list';
      d.bullets.forEach(function(bText) {
        var li = document.createElement('li');
        li.className = 'plugy-cb-bullet-item';
        li.textContent = bText;
        ul.appendChild(li);
      });
      card.appendChild(ul);
    }
    bodyEl.appendChild(card);

    // 2. Simple Everyday Meaning / Translation Box
    var normText = d.simpleNorm;
    if (state.lang === 'hi' && d.normHi) normText = d.normHi;
    else if (state.lang === 'mr' && d.normMr) normText = d.normMr;

    if (normText) {
      var normBox = document.createElement('div');
      normBox.className = 'plugy-cb-norm-box';
      normBox.textContent = normText;
      bodyEl.appendChild(normBox);
    }

    // 3. Actionable Page Shortcut Buttons
    if (d.actions && d.actions.length > 0) {
      var actWrap = document.createElement('div');
      actWrap.className = 'plugy-cb-actions-wrap';

      var actHdr = document.createElement('div');
      actHdr.className = 'plugy-cb-action-label';
      actHdr.textContent = labels.actionHeader;
      actWrap.appendChild(actHdr);

      d.actions.forEach(function(a) {
        var link = document.createElement('a');
        var sClass = 'style-primary';
        if (a.style === 'emerald') sClass = 'style-emerald';
        else if (a.style === 'outline') sClass = 'style-outline';

        link.className = 'plugy-cb-action-btn ' + sClass;
        link.href = a.url;

        var span = document.createElement('span');
        span.textContent = a.label;
        var arrow = document.createElement('span');
        arrow.innerHTML = '&#8594;';

        link.appendChild(span);
        link.appendChild(arrow);
        actWrap.appendChild(link);
      });
      bodyEl.appendChild(actWrap);
    }

    // 4. Nested Guided Question Options
    if (d.options && d.options.length > 0) {
      var optWrap = document.createElement('div');
      optWrap.className = 'plugy-cb-options-wrap';

      var optHdr = document.createElement('div');
      optHdr.className = 'plugy-cb-options-header';
      optHdr.textContent = labels.choose;
      optWrap.appendChild(optHdr);

      d.options.forEach(function(o) {
        var btn = document.createElement('button');
        btn.type = 'button';
        btn.className = 'plugy-cb-option-btn';

        var left = document.createElement('div');
        left.className = 'plugy-cb-option-left';

        var ic = document.createElement('span');
        ic.className = 'plugy-cb-option-icon';
        ic.textContent = o.icon || '•';

        var lb = document.createElement('span');
        lb.textContent = o.label;

        left.appendChild(ic);
        left.appendChild(lb);

        var right = document.createElement('div');
        right.className = 'plugy-cb-option-right';

        if (o.badge) {
          var bg = document.createElement('span');
          bg.className = 'plugy-cb-option-badge';
          bg.textContent = o.badge;
          right.appendChild(bg);
        }

        var chev = document.createElement('span');
        chev.className = 'plugy-cb-option-arrow';
        chev.innerHTML = '&#8250;';
        right.appendChild(chev);

        btn.appendChild(left);
        btn.appendChild(right);

        btn.addEventListener('click', function() {
          navigateToNode(o.nodeId, true);
        });
        optWrap.appendChild(btn);
      });
      bodyEl.appendChild(optWrap);
    }

    bodyEl.scrollTop = 0;
  }

  function navigateToNode(targetNodeId, pushHistory) {
    if (pushHistory && state.currentData && state.currentData.nodeId) {
      state.historyStack.push(state.currentData.nodeId);
      if (state.historyStack.length > 20) state.historyStack.shift();
    }

    var params = new URLSearchParams();
    params.append('nodeId', targetNodeId);
    params.append('currentPage', currentPage);
    if (pageStationId) params.append('pageStationId', pageStationId);

    fetch(ENDPOINT, {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8' },
      body: params.toString()
    })
    .then(function(r) {
      if (!r.ok) throw new Error('HTTP ' + r.status);
      return r.json();
    })
    .then(function(data) {
      if (data.role) state.role = data.role;
      state.currentData = data;
      saveState();
      renderCurrentNode();
    })
    .catch(function(err) {
      state.currentData = {
        nodeId: 'ROOT',
        parentId: '',
        breadcrumb: '🏠 Main Menu',
        title: '⚠️ Tomcat Restart Required',
        reply: 'ChatbotController returned ' + (err.message || 'an error') + '.',
        bullets: ['In Eclipse, right-click your Tomcat Server in the Servers tab and click Restart so the updated servlet classes load into memory.'],
        simpleNorm: 'Once Tomcat restarts, click Main Menu above.',
        options: [],
        actions: []
      };
      renderCurrentNode();
    });
  }

  function submitQuickSearch(queryText) {
    var trimmed = (queryText || '').trim();
    if (!trimmed) return;

    if (state.currentData && state.currentData.nodeId) {
      state.historyStack.push(state.currentData.nodeId);
    }
    inputEl.value = '';

    var params = new URLSearchParams();
    params.append('message', trimmed);
    params.append('currentPage', currentPage);
    if (pageStationId) params.append('pageStationId', pageStationId);

    fetch(ENDPOINT, {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8' },
      body: params.toString()
    })
    .then(function(r) {
      if (!r.ok) throw new Error('HTTP ' + r.status);
      return r.json();
    })
    .then(function(data) {
      if (data.role) state.role = data.role;
      state.currentData = data;
      saveState();
      renderCurrentNode();
    })
    .catch(function() {
      navigateToNode('ROOT', false);
    });
  }

  toggleBtn.addEventListener('click', function() {
    state.isOpen = !state.isOpen;
    if (state.isOpen && !state.currentData) {
      navigateToNode('ROOT', false);
    } else {
      saveState();
      renderCurrentNode();
    }
  });

  closeBtn.addEventListener('click', function() {
    state.isOpen = false;
    saveState();
    renderCurrentNode();
  });

  backBtn.addEventListener('click', function() {
    if (state.historyStack.length > 0) {
      var prev = state.historyStack.pop();
      navigateToNode(prev, false);
    } else if (state.currentData && state.currentData.parentId) {
      navigateToNode(state.currentData.parentId, false);
    } else {
      navigateToNode('ROOT', false);
    }
  });

  homeBtn.addEventListener('click', function() {
    state.historyStack = [];
    navigateToNode('ROOT', false);
  });

  langBtns.forEach(function(btn) {
    btn.addEventListener('click', function() {
      state.lang = btn.getAttribute('data-lang') || 'en';
      saveState();
      renderCurrentNode();
    });
  });

  formEl.addEventListener('submit', function(e) {
    e.preventDefault();
    submitQuickSearch(inputEl.value);
  });

  if (state.currentData) {
    renderCurrentNode();
  } else {
    updateLangUI();
  }
})();
</script>
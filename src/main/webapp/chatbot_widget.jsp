<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    String cbUri = request.getRequestURI();
    String cbPage = cbUri != null ? cbUri.substring(cbUri.lastIndexOf("/") + 1).toLowerCase() : "";
    String cbRole = "GUEST";

    if (session.getAttribute("admin") != null
            || cbPage.contains("adminhome") || cbPage.contains("stations.jsp")
            || cbPage.contains("managestation") || cbPage.contains("viewslots")
            || cbPage.contains("stationbookings") || cbPage.contains("bookinghistory")) {
        cbRole = "ADMIN";
    } else if (session.getAttribute("email") != null || session.getAttribute("user") != null
            || cbPage.contains("userhome") || cbPage.contains("search.jsp")
            || cbPage.contains("searchslot") || cbPage.contains("mybookings")
            || cbPage.contains("payment.jsp")) {
        cbRole = "USER";
    }

    String pageStationId = request.getParameter("stationId");
    if (pageStationId == null || !pageStationId.matches("\\d+")) pageStationId = "";
%>
<!-- ======= Conversational Plugy Chatbot Widget ======= -->
<style>
/* Move Chatbot FAB to the Bottom-Left */
  .plugy-cb-fab {
    position: fixed;
    bottom: 24px;
    left: 24px; /* Changed from right: 24px */
    height: 52px;
    padding: 0 18px;
    border-radius: 999px;
    background: linear-gradient(135deg, #01036f 0%, #0205a1 100%);
    color: #ffffff;
    border: none;
    box-shadow: 0 8px 24px rgba(1, 3, 111, 0.3);
    display: flex;
    align-items: center;
    gap: 8px;
    font-family: 'Poppins', sans-serif;
    font-size: 13px;
    font-weight: 600;
    cursor: pointer;
    z-index: 9999;
    transition: transform 0.2s ease, box-shadow 0.2s ease;
  }
  .plugy-cb-fab:hover {
    transform: translateY(-2px);
    box-shadow: 0 12px 28px rgba(1, 3, 111, 0.4);
  }

  /* Anchor the Chatbot Window to the Bottom-Left when opened */
  .plugy-cb-window {
    position: fixed;
    bottom: 90px;
    left: 24px; /* Changed from right: 24px */
    width: 340px;
    height: 500px;
    min-width: 280px;
    min-height: 380px;
    max-width: 100vw;
    max-height: 100vh;
    background: #f4f6fa;
    border-radius: 16px;
    box-shadow: 0 15px 45px rgba(15, 23, 42, 0.25);
    border: 1px solid #cbd5e1;
    display: none;
    flex-direction: column;
    z-index: 9999;
    font-family: 'Open Sans', sans-serif;
    resize: both;
    overflow: hidden;
  }
  .plugy-cb-window.open {
    display: flex;
  }

  /* Draggable Header */
  .plugy-cb-header {
    background: #01036f;
    color: #ffffff;
    padding: 10px 14px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    cursor: grab; /* Indicates draggability */
    user-select: none;
    flex-shrink: 0;
  }
  .plugy-cb-header:active {
    cursor: grabbing;
  }
  .plugy-cb-brand {
    display: flex;
    align-items: center;
    gap: 8px;
  }
  .plugy-cb-brand h6 {
    margin: 0;
    font-size: 13px;
    font-weight: 700;
    font-family: 'Poppins', sans-serif;
  }
  .plugy-cb-badge {
    font-size: 9.5px;
    background: rgba(255,255,255,0.15);
    padding: 2px 6px;
    border-radius: 4px;
    letter-spacing: 0.5px;
  }
  .plugy-cb-header-controls {
    display: flex;
    align-items: center;
    gap: 12px;
  }
  .plugy-cb-header-btn {
    background: transparent;
    border: none;
    color: rgba(255, 255, 255, 0.8);
    font-size: 14px;
    cursor: pointer;
    padding: 0;
  }
  .plugy-cb-header-btn:hover { color: #ffffff; }

  /* Continuous Chat Feed */
  .plugy-cb-feed {
    flex: 1;
    padding: 14px 12px;
    overflow-y: auto;
    display: flex;
    flex-direction: column;
    gap: 12px;
    scroll-behavior: smooth;
  }

  /* Chat Bubbles */
  .cb-bubble {
    max-width: 85%;
    font-size: 12.5px;
    line-height: 1.45;
    padding: 9px 12px;
    word-wrap: break-word;
  }
  .cb-user-bubble {
    align-self: flex-end;
    background: #0205a1;
    color: #ffffff;
    border-radius: 14px 14px 2px 14px;
    box-shadow: 0 2px 5px rgba(2, 5, 161, 0.15);
  }
  .cb-bot-group {
    display: flex;
    flex-direction: column;
    gap: 6px;
    align-items: flex-start;
  }
  .cb-bot-bubble {
    background: #ffffff;
    color: #1e293b;
    border: 1px solid #e2e8f0;
    border-radius: 14px 14px 14px 2px;
    box-shadow: 0 2px 6px rgba(0,0,0,0.03);
  }
  .cb-bot-title {
    font-weight: 700;
    font-size: 13px;
    color: #0f172a;
    margin-bottom: 4px;
    display: block;
  }
  .cb-bot-bullets {
    margin: 6px 0 0 16px;
    padding: 0;
  }
  .cb-bot-bullets li {
    margin-bottom: 4px;
    font-size: 12px;
    color: #334155;
  }
  .cb-bot-bullets li:last-child { margin-bottom: 0; }

  /* Action Links (Deep Links inside bubbles) */
  .cb-actions {
    display: flex;
    flex-direction: column;
    gap: 6px;
    margin-top: 8px;
    border-top: 1px solid #f1f5f9;
    padding-top: 8px;
  }
  .cb-action-btn {
    display: inline-block;
    font-size: 11.5px;
    font-weight: 600;
    color: #0205a1;
    text-decoration: none !important;
  }
  .cb-action-btn:hover { text-decoration: underline !important; }

  /* Quick-Reply Chips (Only shown on latest bot message) */
  .cb-chips-wrapper {
    display: flex;
    flex-direction: column;
    gap: 6px;
    width: 100%;
    padding-left: 2px;
    padding-right: 12px;
  }
  .cb-chip {
    background: #ffffff;
    border: 1px solid #cbd5e1;
    color: #334155;
    border-radius: 8px;
    padding: 7px 10px;
    font-size: 11.5px;
    font-weight: 600;
    text-align: left;
    cursor: pointer;
    transition: all 0.15s ease;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  .cb-chip:hover {
    background: #f8fafc;
    border-color: #0205a1;
    color: #0205a1;
  }

  /* Input Footer */
  .plugy-cb-footer {
    padding: 8px 10px;
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
    padding: 6px 12px;
    font-size: 12px;
    outline: none;
  }
  .plugy-cb-footer input:focus {
    border-color: #0205a1;
  }
  .plugy-cb-send {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    border: none;
    background: #1acc8d;
    color: #ffffff;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }
</style>

<button type="button" class="plugy-cb-fab" id="plugyCbToggle" aria-label="Open Assistant">
  <span class="plugy-cb-fab-bolt">&#9889;</span>
  <span>Assistant</span>
</button>

<div class="plugy-cb-window" id="plugyCbWindow" data-role="<%= cbRole %>" data-station="<%= pageStationId %>">
  
  <!-- Draggable Header -->
  <div class="plugy-cb-header" id="plugyCbHeader" title="Drag to move">
    <div class="plugy-cb-brand">
      <span style="font-size: 16px;">&#9889;</span>
      <div>
        <h6>Plugy Bot</h6>
        <span class="plugy-cb-badge" id="plugyCbBadge"><%= cbRole %></span>
      </div>
    </div>
    <div class="plugy-cb-header-controls">
      <button type="button" class="plugy-cb-header-btn" id="plugyCbRestart" title="Restart Chat">&#8635;</button>
      <button type="button" class="plugy-cb-header-btn" id="plugyCbClose" title="Minimize">&#10005;</button>
    </div>
  </div>

  <!-- Scrolling Chat Feed -->
  <div class="plugy-cb-feed" id="plugyCbFeed"></div>

  <!-- Bottom Input Bar -->
  <form class="plugy-cb-footer" id="plugyCbForm" autocomplete="off">
    <input type="text" id="plugyCbInput" maxlength="120" placeholder="Type a question or area..." />
    <button type="submit" class="plugy-cb-send" title="Send">&#10148;</button>
  </form>

</div>

<script>
(function() {
  var STORAGE_KEY = 'plugy_chat_v5';
  var ENDPOINT = '<%= request.getContextPath() %>/ChatbotController';
  var win = document.getElementById('plugyCbWindow');
  var header = document.getElementById('plugyCbHeader');
  var feedEl = document.getElementById('plugyCbFeed');
  var inputEl = document.getElementById('plugyCbInput');
  var role = win.getAttribute('data-role') || 'GUEST';
  var pageStationId = win.getAttribute('data-station') || '';
  var currentPage = window.location.pathname.substring(window.location.pathname.lastIndexOf('/') + 1) || 'index.jsp';

  // --- 1. DRAG & DROP LOGIC ---
  var isDragging = false;
  var startX, startY, initialLeft, initialTop;

  header.addEventListener('mousedown', function(e) {
    if (e.target.tagName.toLowerCase() === 'button') return;
    isDragging = true;
    startX = e.clientX;
    startY = e.clientY;
    var rect = win.getBoundingClientRect();
    initialLeft = rect.left;
    initialTop = rect.top;
    win.style.bottom = 'auto';
    win.style.right = 'auto';
    win.style.left = initialLeft + 'px';
    win.style.top = initialTop + 'px';
  });

  document.addEventListener('mousemove', function(e) {
    if (!isDragging) return;
    var dx = e.clientX - startX;
    var dy = e.clientY - startY;
    win.style.left = (initialLeft + dx) + 'px';
    win.style.top = (initialTop + dy) + 'px';
  });

  document.addEventListener('mouseup', function() {
    isDragging = false;
  });


  // --- 2. STATE & RENDER LOGIC ---
  function loadState() {
    try {
      var raw = sessionStorage.getItem(STORAGE_KEY);
      if (!raw) return null;
      var parsed = JSON.parse(raw);
      if (parsed.role !== role) return null;
      return parsed;
    } catch (e) { return null; }
  }

  function saveState() {
    try { sessionStorage.setItem(STORAGE_KEY, JSON.stringify(state)); } catch (e) {}
  }

  var state = loadState() || {
    role: role,
    isOpen: false,
    chatHistory: [] // Array of { sender: 'user'|'bot', text: '...', data: { ... } }
  };

  function renderFeed() {
    if (state.isOpen) win.classList.add('open');
    else win.classList.remove('open');

    feedEl.innerHTML = '';

    state.chatHistory.forEach(function(msg, index) {
      var isLatestBotMessage = (index === state.chatHistory.length - 1 && msg.sender === 'bot');

      if (msg.sender === 'user') {
        var uWrap = document.createElement('div');
        uWrap.className = 'cb-bubble cb-user-bubble';
        uWrap.textContent = msg.text;
        feedEl.appendChild(uWrap);
      } 
      else {
        var bGroup = document.createElement('div');
        bGroup.className = 'cb-bot-group';

        var bWrap = document.createElement('div');
        bWrap.className = 'cb-bubble cb-bot-bubble';

        if (msg.data.title) {
          var t = document.createElement('span');
          t.className = 'cb-bot-title';
          t.textContent = msg.data.title;
          bWrap.appendChild(t);
        }

        if (msg.data.reply) {
          var r = document.createElement('span');
          r.textContent = msg.data.reply;
          bWrap.appendChild(r);
        }

        if (msg.data.bullets && msg.data.bullets.length > 0) {
          var ul = document.createElement('ul');
          ul.className = 'cb-bot-bullets';
          msg.data.bullets.forEach(function(bText) {
            var li = document.createElement('li');
            li.textContent = bText;
            ul.appendChild(li);
          });
          bWrap.appendChild(ul);
        }

        if (msg.data.actions && msg.data.actions.length > 0) {
          var acts = document.createElement('div');
          acts.className = 'cb-actions';
          msg.data.actions.forEach(function(a) {
            var link = document.createElement('a');
            link.className = 'cb-action-btn';
            link.href = a.url;
            link.innerHTML = a.label + ' &rarr;';
            acts.appendChild(link);
          });
          bWrap.appendChild(acts);
        }

        bGroup.appendChild(bWrap);

        // ONLY render chips for the very latest bot message to prevent UI clutter
        if (isLatestBotMessage && msg.data.options && msg.data.options.length > 0) {
          var chipsWrap = document.createElement('div');
          chipsWrap.className = 'cb-chips-wrapper';
          msg.data.options.forEach(function(o) {
            var btn = document.createElement('button');
            btn.className = 'cb-chip';
            
            var left = document.createElement('span');
            left.textContent = (o.icon ? o.icon + ' ' : '') + o.label;
            btn.appendChild(left);

            var right = document.createElement('span');
            right.style.color = '#94a3b8';
            right.innerHTML = '&rsaquo;';
            btn.appendChild(right);

            btn.onclick = function() {
              pushUserMessage(o.label);
              fetchNode(o.nodeId);
            };
            chipsWrap.appendChild(btn);
          });
          bGroup.appendChild(chipsWrap);
        }

        feedEl.appendChild(bGroup);
      }
    });

    feedEl.scrollTop = feedEl.scrollHeight;
  }

  // --- 3. COMMUNICATION LOGIC ---
  function pushUserMessage(text) {
    if (!text.trim()) return;
    state.chatHistory.push({ sender: 'user', text: text.trim() });
    saveState();
    renderFeed();
  }

  function fetchNode(nodeId, isSearch) {
    var params = new URLSearchParams();
    if (isSearch) {
      params.append('message', nodeId);
    } else {
      params.append('nodeId', nodeId);
    }
    params.append('currentPage', currentPage);
    if (pageStationId) params.append('pageStationId', pageStationId);

    fetch(ENDPOINT, {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8' },
      body: params.toString()
    })
    .then(function(r) { return r.json(); })
    .then(function(data) {
      state.chatHistory.push({ sender: 'bot', data: data });
      saveState();
      renderFeed();
    })
    .catch(function(err) {
      state.chatHistory.push({
        sender: 'bot',
        data: { title: 'Network Error', reply: 'Could not connect. Ensure Tomcat is running.', bullets: [], actions: [], options: [] }
      });
      renderFeed();
    });
  }

  // --- 4. EVENT LISTENERS ---
  document.getElementById('plugyCbToggle').onclick = function() {
    state.isOpen = !state.isOpen;
    if (state.isOpen && state.chatHistory.length === 0) {
      fetchNode('ROOT', false);
    } else {
      renderFeed();
    }
  };

  document.getElementById('plugyCbClose').onclick = function() {
    state.isOpen = false;
    saveState();
    renderFeed();
  };

  document.getElementById('plugyCbRestart').onclick = function() {
    state.chatHistory = [];
    saveState();
    fetchNode('ROOT', false);
  };

  document.getElementById('plugyCbForm').onsubmit = function(e) {
    e.preventDefault();
    var val = inputEl.value;
    inputEl.value = '';
    if (val.trim()) {
      pushUserMessage(val);
      fetchNode(val, true); // Search triggers the quick-search logic in controller
    }
  };

  // Initial render
  if (state.isOpen) renderFeed();

})();
</script>
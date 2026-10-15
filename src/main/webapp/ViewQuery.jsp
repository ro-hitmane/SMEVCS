<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, com.dao.ContactDao, com.bean.ContactQuery, com.util.HtmlUtils, java.text.SimpleDateFormat" %>
<%
    String adminEmail = (String) session.getAttribute("admin");
    if (adminEmail == null || adminEmail.trim().isEmpty()) {
        response.sendRedirect("AdminLogin.jsp");
        return;
    }

    List<ContactQuery> queryList = new ContactDao().getAllQueries();
    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a");
%>
<!DOCTYPE html>
<html lang="en">

<head>
  <meta charset="utf-8">
  <meta content="width=device-width, initial-scale=1.0" name="viewport">
  <title>Plugy - Support Logs</title>

  <link href="assets/img/apple-touch-icon.png" rel="icon">
  <link href="https://fonts.googleapis.com/css?family=Inter:300,400,500,600,700|Poppins:300,400,500,600,700" rel="stylesheet">
  <link href="assets/vendor/bootstrap/css/bootstrap.min.css" rel="stylesheet">
  <link href="assets/vendor/bootstrap-icons/bootstrap-icons.css" rel="stylesheet">
  <link href="assets/css/style.css" rel="stylesheet">

  <style>
    body {
      background-color: #f8fafc;
      font-family: 'Inter', 'Poppins', sans-serif;
    }
    .table-container-card {
      background: #ffffff;
      border-radius: 16px;
      border: 1px solid #e2e8f0;
      box-shadow: 0 4px 18px rgba(0,0,0,0.02);
      padding: 24px;
      overflow-x: auto;
    }
    .table thead th {
      background-color: #f8fafc;
      color: #64748b;
      font-weight: 600;
      text-transform: uppercase;
      font-size: 12px;
      letter-spacing: 0.5px;
      border-bottom: 2px solid #e2e8f0;
      padding: 16px;
    }
    .table tbody td {
      padding: 16px;
      vertical-align: top;
      color: #334155;
      font-size: 14px;
      border-bottom: 1px solid #f1f5f9;
    }
    
    /* Read More / Read Less Styling */
    .msg-wrapper {
      background: #f8fafc;
      border: 1px solid #e2e8f0;
      border-left: 3px solid #0205a1;
      border-radius: 8px;
      padding: 12px 14px;
      width: 100%;
      max-width: 450px;
    }
    .msg-content {
      font-family: monospace;
      font-size: 13px;
      color: #1e293b;
      white-space: pre-wrap;
      word-break: break-word;
      max-height: 4.5em; /* Limits to ~3 lines initially */
      overflow: hidden;
      transition: max-height 0.3s ease;
    }
    .msg-content.expanded {
      max-height: 2000px; /* Expands fully */
    }
    .toggle-btn {
      background: none;
      border: none;
      color: #0205a1;
      font-size: 12px;
      font-weight: 600;
      padding: 0;
      margin-top: 8px;
      cursor: pointer;
    }
    .toggle-btn:hover {
      text-decoration: underline;
    }
    
    /* Badges & Buttons */
    .subject-badge {
      background-color: #eff6ff;
      color: #1d4ed8;
      padding: 6px 10px;
      border-radius: 6px;
      font-weight: 500;
      font-size: 13px;
    }
    .btn-resolve {
      background-color: #10b981;
      color: white;
      border: none;
      border-radius: 8px;
      padding: 8px 16px;
      font-weight: 600;
      font-size: 13px;
      transition: all 0.2s;
    }
    .btn-resolve:hover {
      background-color: #059669;
      transform: translateY(-1px);
      box-shadow: 0 4px 12px rgba(16, 185, 129, 0.2);
    }
  </style>
</head>

<body>

<jsp:include page="nav_admin.jsp" />

    <!-- Hero Section -->
  <section id="hero" style="padding: 60px 0 0 0;">
    <svg class="hero-waves" xmlns="http://www.w3.org/2000/svg" viewBox="0 24 150 28" preserveAspectRatio="none">
      <defs><path id="wave-path" d="M-160 44c30 0 58-18 88-18s 58 18 88 18 58-18 88-18 58 18 88 18 v44h-352z"></defs>
      <g class="wave1"><use xlink:href="#wave-path" x="50" y="3" fill="rgba(255,255,255, .1)"></g>
      <g class="wave2"><use xlink:href="#wave-path" x="50" y="0" fill="rgba(255,255,255, .2)"></g>
      <g class="wave3"><use xlink:href="#wave-path" x="50" y="9" fill="#f8fafc"></g>
    </svg>
  </section>

  <main id="main" class="pt-2 pb-5">
    <div class="container">

      <div class="d-flex flex-wrap justify-content-between align-items-end mb-4 mt-3">
        <div>
          <div class="d-flex align-items-center gap-2 mb-1">
            <span class="text-uppercase fw-semibold" style="font-size: 11px; letter-spacing: 1.5px; color: #64748b;">Customer Support</span>
            <div style="width: 45px; height: 2px; background-color: #10b981;"></div>
          </div>
          <h2 class="fw-bold m-0" style="font-family:'Poppins', sans-serif; color: #01036f; font-size: 26px; letter-spacing: -0.5px;">ACTIVE USER QUERIES</h2>
          <p class="text-muted small mt-1 mb-0">Manage and resolve pending support tickets from the community.</p>
        </div>
      </div>

      <% if (request.getParameter("msg") != null) { %>
          <div class="alert alert-success alert-dismissible fade show" role="alert">
              <i class="bi bi-check-circle-fill me-2"></i> <%= HtmlUtils.escape(request.getParameter("msg")) %>
              <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
          </div>
      <% } %>

      <div class="table-container-card">
        <table class="table table-borderless table-hover mb-0">
          <thead>
            <tr>
              <th width="15%">Date & Time</th>
              <th width="20%">Sender Identity</th>
              <th width="15%">Subject</th>
              <th width="40%">Message Payload</th>
              <th width="10%" class="text-center">Action</th>
            </tr>
          </thead>
          <tbody>
            <% if (queryList == null || queryList.isEmpty()) { %>
                <tr>
                    <td colspan="5" class="text-center text-muted py-5">
                        <i class="bi bi-check2-all fs-1 d-block mb-3" style="color: #10b981;"></i>
                        <h5 class="fw-bold text-dark">All Caught Up!</h5>
                        <p class="small">There are no active support queries requiring your attention.</p>
                    </td>
                </tr>
            <% } else { 
                for (ContactQuery q : queryList) { 
            %>
                <tr>
                    <td>
                        <span class="badge bg-light text-secondary border">
                            <i class="bi bi-clock me-1"></i>
                            <%= (q.getCreatedAt() != null) ? sdf.format(q.getCreatedAt()) : "N/A" %>
                        </span>
                    </td>
                    <td>
                        <strong class="text-dark"><%= HtmlUtils.escape(q.getName()) %></strong><br>
                        <a href="mailto:<%= HtmlUtils.escape(q.getEmail()) %>" class="text-decoration-none small" style="color: #64748b;">
                            <i class="bi bi-envelope me-1"></i><%= HtmlUtils.escape(q.getEmail()) %>
                        </a>
                    </td>
                    <td>
                        <span class="subject-badge">
                            <%= HtmlUtils.escape(q.getSubject()) %>
                        </span>
                    </td>
                    <td>
                        <div class="msg-wrapper">
                            <div class="msg-content"><%= HtmlUtils.escape(q.getMessage()) %></div>
                            <button class="toggle-btn" onclick="toggleMsg(this)" style="display: none;">
                                Read more <i class="bi bi-chevron-down ms-1"></i>
                            </button>
                        </div>
                    </td>
                    <td class="text-center align-middle">
                        <form action="ResolvequeryController" method="post" class="m-0">
                            <input type="hidden" name="queryId" value="<%= q.getId() %>">
                            <button type="submit" class="btn-resolve">
                                <i class="bi bi-check2 me-1"></i> Resolve
                            </button>
                        </form>
                    </td>
                </tr>
            <%  } 
               } %>
          </tbody>
        </table>
      </div>

    </div>
  </main>

  <script src="assets/vendor/bootstrap/js/bootstrap.bundle.min.js"></script>
  
  <!-- Dynamic Read More / Less Logic -->
  <script>
    document.addEventListener("DOMContentLoaded", function() {
        // Evaluate every message box on page load
        document.querySelectorAll('.msg-content').forEach(function(contentDiv) {
            // If the actual scroll height of the text exceeds its clamped CSS height (~4.5em)
            if (contentDiv.scrollHeight > contentDiv.clientHeight) {
                // Show the "Read more" button
                contentDiv.nextElementSibling.style.display = 'inline-block';
            }
        });
    });

    function toggleMsg(btn) {
        const content = btn.previousElementSibling;
        if (content.classList.contains('expanded')) {
            content.classList.remove('expanded');
            btn.innerHTML = 'Read more <i class="bi bi-chevron-down ms-1"></i>';
        } else {
            content.classList.add('expanded');
            btn.innerHTML = 'Read less <i class="bi bi-chevron-up ms-1"></i>';
        }
    }
  </script>
</body>
</html>
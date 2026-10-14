<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, com.bean.Station, com.bean.Slot, com.dao.AdminDao" %>
<%
    int stationId = -1;
    try { stationId = Integer.parseInt(request.getParameter("stationId")); } catch (Exception ignored) {}
    AdminDao dao = new AdminDao();
    Station station = stationId > 0 ? dao.getStationById(stationId) : null;
    if (station == null) {
        response.sendRedirect("Stations.jsp");
        return;
    }
    List<Slot> slots = dao.getSlotsByStation(stationId);
    int availableCount = 0;
    for (Slot sl : slots) {
        if ("Available".equals(sl.getStatus())) availableCount++;
    }
    int otherCount = slots.size() - availableCount;
%>

<%!
    private static String esc(String v) {
        if (v == null) return "";
        return v.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    private static String statusClass(String st) {
        if (st == null) return "status-other";
        String v = st.trim().toLowerCase();
        if (v.equals("available")) return "status-available";
        if (v.equals("reserved")) return "status-reserved";
        if (v.equals("offline")) return "status-offline";
        if (v.contains("maint")) return "status-maintenance";
        return "status-other";
    }
%>
<!DOCTYPE html>
<html lang="en">


<head>
  <meta charset="utf-8">
  <meta content="width=device-width, initial-scale=1.0" name="viewport">

  <title>SMEVCS - Manage Station</title>
  <meta content="" name="description">
  <meta content="" name="keywords">

  <!-- Favicons -->
  <link href="assets/img/apple-touch-icon.png" rel="icon">
  <link href="assets/img/apple-touch-icon.png" rel="apple-touch-icon">

  <!-- Google Fonts -->
  <link href="https://fonts.googleapis.com/css?family=Open+Sans:300,300i,400,400i,600,600i,700,700i|Montserrat:300,300i,400,400i,500,500i,600,600i,700,700i|Poppins:300,300i,400,400i,500,500i,600,600i,700,700i" rel="stylesheet">

  <!-- Vendor CSS Files -->
  <link href="assets/vendor/aos/aos.css" rel="stylesheet">
  <link href="assets/vendor/bootstrap/css/bootstrap.min.css" rel="stylesheet">
  <link href="assets/vendor/bootstrap-icons/bootstrap-icons.css" rel="stylesheet">
  <link href="assets/vendor/boxicons/css/boxicons.min.css" rel="stylesheet">
  <link href="assets/vendor/glightbox/css/glightbox.min.css" rel="stylesheet">
  <link href="assets/vendor/remixicon/remixicon.css" rel="stylesheet">
  <link href="assets/vendor/swiper/swiper-bundle.min.css" rel="stylesheet">

  <!-- Template Main CSS File -->
  <link href="assets/css/style.css?v=6" rel="stylesheet">

  <!-- =======================================================
  * Template Name: Bootslander - v4.7.0
  * Template URL: https://bootstrapmade.com/bootslander-free-bootstrap-landing-page-template/
  * Author: BootstrapMade.com
  * License: https://bootstrapmade.com/license/
  ======================================================== -->
</head>

<body>

  <!-- ======= Header ======= -->
  <header id="header" class="fixed-top d-flex align-items-center header-transparent">
    <div class="container d-flex align-items-center justify-content-between">

      <div class="logo">
        <h1><a href="index.jsp"><span>Plugy</span></a></h1>
      </div>

      <nav id="navbar" class="navbar">
        <ul>
          <li><a class="nav-link scrollto" href="AdminHome.jsp">Home</a></li>
          <li><a class="nav-link scrollto active" href="Stations.jsp">Station</a></li>
          <li><a class="nav-link scrollto" href="LogoutController">Logout</a></li>
        </ul>
        <i class="bi bi-list mobile-nav-toggle"></i>
      </nav>
      <!-- .navbar -->
    </div>
  </header><!-- End Header -->

  <!-- ======= Hero Section ======= -->
  <section id="hero">

    <div class="container">
     
    </div>

    <svg class="hero-waves" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 24 150 28 " preserveAspectRatio="none">
      <defs>
        <path id="wave-path" d="M-160 44c30 0 58-18 88-18s 58 18 88 18 58-18 88-18 58 18 88 18 v44h-352z">
      </defs>
      <g class="wave1">
        <use xlink:href="#wave-path" x="50" y="3" fill="rgba(255,255,255, .1)">
      </g>
      <g class="wave2">
        <use xlink:href="#wave-path" x="50" y="0" fill="rgba(255,255,255, .2)">
      </g>
      <g class="wave3">
        <use xlink:href="#wave-path" x="50" y="9" fill="#fff">
      </g>
    </svg>

  </section><!-- End Hero -->

  <main id="main">
    <section id="manage-station" class="contact stations-section">
      <div class="container">

        <div class="section-title" data-aos="fade-up">
          <h2>Station</h2>
          <p>Manage Station</p>
        </div>

        <div class="mb-3">
          <a href="Stations.jsp" style="color:#01036f;font-weight:500;"><i class="bi bi-arrow-left"></i> Back to Stations</a>
        </div>

        <!-- Station info -->
        <div class="station-detail-card">
          <div class="station-detail-head">
            <div class="station-icon"><i class="bi bi-lightning-charge-fill"></i></div>
            <div>
              <h3 class="station-detail-name"><%= esc(station.getStationName()) %></h3>
              <span class="station-detail-id">Station ID #<%= station.getId() %></span>
            </div>
          </div>

          <div class="row g-3 station-detail-grid">
            <div class="col-md-6">
              <span class="detail-label"><i class="bi bi-signpost"></i> Address</span>
              <span class="detail-value"><%= esc(station.getAddress()) %></span>
            </div>
            <div class="col-md-6">
              <span class="detail-label"><i class="bi bi-geo-alt"></i> Area</span>
              <span class="detail-value"><%= esc(station.getArea()) %></span>
            </div>
            <div class="col-md-4">
              <span class="detail-label"><i class="bi bi-building"></i> City</span>
              <span class="detail-value"><%= esc(station.getCity()) %></span>
            </div>
            <div class="col-md-4">
              <span class="detail-label"><i class="bi bi-pin-map"></i> State</span>
              <span class="detail-value"><%= esc(station.getState()) %></span>
            </div>
            <div class="col-md-4">
              <span class="detail-label"><i class="bi bi-globe2"></i> Country</span>
              <span class="detail-value"><%= esc(station.getCountry()) %></span>
            </div>
          </div>
        </div>

        <!-- Slot summary -->
        <div class="row g-3 slot-summary">
          <div class="col-4">
            <div class="summary-box"><span class="summary-number"><%= slots.size() %></span><span class="summary-text">Total slots</span></div>
          </div>
          <div class="col-4">
            <div class="summary-box"><span class="summary-number"><%= availableCount %></span><span class="summary-text">Available</span></div>
          </div>
          <div class="col-4">
            <div class="summary-box"><span class="summary-number"><%= otherCount %></span><span class="summary-text">Reserved / other</span></div>
          </div>
        </div>

        <!-- Slots -->
        <div class="stations-toolbar mt-4">
          <span class="stations-count">Slots at this station</span>
          <div class="d-flex gap-2 flex-wrap">
            <a class="btn-station btn-station-outline" href="StationBookings.jsp?stationId=<%= station.getId() %>" style="padding:10px 24px;font-size:15px;">
              <i class="bi bi-calendar-check"></i> View Bookings
            </a>
            <a class="btn-create-station" href="ManageSlots.jsp?stationId=<%= station.getId() %>" style="text-decoration:none;">
              <i class="bi bi-plus-lg"></i> Add Slot
            </a>
          </div>
        </div>

        <div class="slot-table-card">
          <div class="table-responsive">
            <table class="slot-table">
              <thead>
                <tr>
                  <th>#</th>
                  <th>Slot</th>
                  <th>Vehicle Type</th>
                  <th>Charger</th>
                  <th>Status</th>
                  <th class="text-end">Actions</th>
                </tr>
              </thead>
              <tbody>
                <% if (slots.isEmpty()) { %>
                <tr>
                  <td colspan="6" class="slot-empty">
                    <i class="bi bi-plug"></i>
                    <span>No slots added yet for this station.</span>
                  </td>
                </tr>
                <% } else {
                     int n = 1;
                     for (Slot sl : slots) {
                %>
                <tr>
                  <td class="slot-num"><%= n++ %></td>
                  <td class="slot-name"><%= esc(sl.getSlotLabel()) %></td>
                  <td><span class="chip chip-vehicle"><%= esc(sl.getVehicleType()) %></span></td>
                  <td><span class="chip chip-charger"><i class="bi bi-plug"></i> <%= esc(sl.getChargerType()) %></span></td>
                  <td><span class="status-pill <%= statusClass(sl.getStatus()) %>"><i class="bi bi-circle-fill"></i> <%= esc(sl.getStatus()) %></span></td>
                  <td class="text-end slot-actions">
                    <button type="button" class="btn-slot btn-slot-edit"
                            data-bs-toggle="modal" data-bs-target="#updateSlotModal"
                            data-id="<%= sl.getId() %>"
                            data-label="<%= esc(sl.getSlotLabel()) %>"
                            data-vehicle="<%= esc(sl.getVehicleType()) %>"
                            data-charger="<%= esc(sl.getChargerType()) %>"
                            data-status="<%= esc(sl.getStatus()) %>">
                      <i class="bi bi-pencil-square"></i> Update
                    </button>
                    <button type="button" class="btn-slot btn-slot-delete"
                            data-bs-toggle="modal" data-bs-target="#deleteSlotModal"
                            data-id="<%= sl.getId() %>"
                            data-label="<%= esc(sl.getSlotLabel()) %>">
                      <i class="bi bi-trash"></i> Delete
                    </button>
                  </td>
                </tr>
                <%   }
                   } %>
              </tbody>
            </table>
          </div>
        </div>

      </div>
    </section>
  </main>
  <!-- End #main -->

<!-- ======= Footer ======= -->
  <footer id="footer">
    <div class="container">
      <div class="copyright">
        &copy; Copyright <strong><span>Plugy</span></strong>. All Rights Reserved
      </div>
    </div>
  </footer>
  <!-- End Footer -->

  <!-- ======= Update Slot Modal ======= -->
  <div class="modal fade" id="updateSlotModal" tabindex="-1" aria-labelledby="updateSlotLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content station-modal">
        <form action="UpdateSlotController" method="post">
          <input type="hidden" name="slotId" id="updSlotId">
          <input type="hidden" name="stationId" value="<%= station.getId() %>">
          <div class="modal-header">
            <h5 class="modal-title" id="updateSlotLabel">Update Slot</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <div class="mb-3">
              <label for="updSlotLabel" class="form-label">Slot label</label>
              <input type="text" class="form-control" id="updSlotLabel" name="slotLabel" maxlength="50" required>
            </div>
            <div class="mb-3">
              <label for="updVehicle" class="form-label">Vehicle type</label>
              <select class="form-select" id="updVehicle" name="vehicleType" required>
                <option>2-Wheeler</option>
                <option>4-Wheeler-Normal</option>
                <option>4-Wheeler-Fast</option>
              </select>
            </div>
            <div class="mb-3">
              <label for="updCharger" class="form-label">Charger type</label>
              <select class="form-select" id="updCharger" name="chargerType" required>
                <option>CCS2</option>
                <option>CHAdeMO</option>
                <option>GB/T</option>
              </select>
            </div>
            <div class="mb-1">
              <label for="updStatus" class="form-label">Status</label>
              <select class="form-select" id="updStatus" name="status" required>
                <option>Available</option>
                <option>Reserved</option>
                <option>Offline</option>
                <option>In maintenance</option>
              </select>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn-station btn-station-outline" data-bs-dismiss="modal">Cancel</button>
            <button type="submit" class="btn-station btn-station-solid">Save changes</button>
          </div>
        </form>
      </div>
    </div>
  </div>

  <!-- ======= Delete Slot Modal ======= -->
  <div class="modal fade" id="deleteSlotModal" tabindex="-1" aria-labelledby="deleteSlotLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content station-modal">
        <form action="DeleteSlotController" method="post">
          <input type="hidden" name="slotId" id="delSlotId">
          <input type="hidden" name="stationId" value="<%= station.getId() %>">
          <div class="modal-header">
            <h5 class="modal-title" id="deleteSlotLabel">Delete Slot</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <p class="mb-2">Are you sure you want to delete <strong id="delSlotName"></strong>? This cannot be undone.</p>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn-station btn-station-outline" data-bs-dismiss="modal">Cancel</button>
            <button type="submit" class="btn-station btn-station-danger">Delete</button>
          </div>
        </form>
      </div>
    </div>
  </div>

  <a href="#" class="back-to-top d-flex align-items-center justify-content-center"><i class="bi bi-arrow-up-short"></i></a>
  <div id="preloader"></div>

  <!-- Vendor JS Files -->
  <script src="assets/vendor/purecounter/purecounter.js"></script>
  <script src="assets/vendor/aos/aos.js"></script>
  <script src="assets/vendor/bootstrap/js/bootstrap.bundle.min.js"></script>
  <script src="assets/vendor/glightbox/js/glightbox.min.js"></script>
  <script src="assets/vendor/swiper/swiper-bundle.min.js"></script>
  <!-- self Main JS File -->
  <script src="assets/js/main.js?v=2"></script>
  <script>
    // Fill the Update modal from the clicked row
    document.getElementById('updateSlotModal').addEventListener('show.bs.modal', function (e) {
      var b = e.relatedTarget;
      if (!b) return;
      document.getElementById('updSlotId').value = b.getAttribute('data-id');
      document.getElementById('updSlotLabel').value = b.getAttribute('data-label');
      setSelect('updVehicle', b.getAttribute('data-vehicle'));
      setSelect('updCharger', b.getAttribute('data-charger'));
      setSelect('updStatus', b.getAttribute('data-status'));
    });

    // Fill the Delete modal from the clicked row
    document.getElementById('deleteSlotModal').addEventListener('show.bs.modal', function (e) {
      var b = e.relatedTarget;
      if (!b) return;
      document.getElementById('delSlotId').value = b.getAttribute('data-id');
      document.getElementById('delSlotName').textContent = b.getAttribute('data-label');
    });

    // Select a value, adding it as an option if the list does not have it yet
    function setSelect(id, value) {
      var sel = document.getElementById(id);
      for (var i = 0; i < sel.options.length; i++) {
        if (sel.options[i].value === value) { sel.selectedIndex = i; return; }
      }
      var opt = document.createElement('option');
      opt.text = value;
      opt.value = value;
      sel.add(opt);
      sel.value = value;
    }
  </script>
</body>
</html>
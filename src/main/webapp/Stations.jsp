<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, com.bean.Station, com.dao.AdminDao" %>
<%
    List<Station> stations = new AdminDao().getStations();
%>

<%!
    private static String esc(String v) {
        if (v == null) return "";
        return v.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }
%>
<!DOCTYPE html>
<html lang="en">


<head>
  <meta charset="utf-8">
  <meta content="width=device-width, initial-scale=1.0" name="viewport">

  <title>SMEVCS</title>
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
  <link href="assets/css/style.css?v=4" rel="stylesheet">

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
    <section id="stations" class="contact stations-section">
      <div class="container">

        <div class="section-title" data-aos="fade-up">
          <h2>Stations</h2>
          <p>Charging Stations</p>
        </div>

        <div class="stations-toolbar">
          <span class="stations-count"><%= stations.size() %> station<%= stations.size() == 1 ? "" : "s" %></span>
          <button type="button" class="btn-create-station" data-bs-toggle="modal" data-bs-target="#createStationModal">
            <i class="bi bi-plus-lg"></i> Create Station
          </button>
        </div>

        <% if (stations.isEmpty()) { %>
          <div class="stations-empty">
            <i class="bi bi-lightning-charge-fill"></i>
            <p>No stations yet. Click <strong>Create Station</strong> to add the first one.</p>
          </div>
        <% } else { %>
        <div class="row g-4">
          <% for (Station s : stations) { %>
          <div class="col-md-6 col-lg-4">
            <div class="station-card">
              <div class="station-icon"><i class="bi bi-lightning-charge-fill"></i></div>
              <h4 class="station-name"><%= esc(s.getStationName()) %></h4>
              <p class="station-area"><i class="bi bi-geo-alt"></i> <%= esc(s.getArea()) %>, <%= esc(s.getCity()) %></p>
              <p class="station-address"><%= esc(s.getAddress()) %><br><%= esc(s.getState()) %>, <%= esc(s.getCountry()) %></p>
              <div class="station-actions">
                <a class="btn-station btn-station-outline" href="ViewSlots.jsp?stationId=<%= s.getId() %>"><i class="bi bi-eye"></i> View Slots</a>
                <a class="btn-station btn-station-solid" href="ManageStation.jsp?stationId=<%= s.getId() %>"><i class="bi bi-gear"></i> Manage Station</a>
              </div>
            </div>
          </div>
          <% } %>
        </div>
        <% } %>

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

  <!-- ======= Create Station Modal ======= -->
  <div class="modal fade" id="createStationModal" tabindex="-1" aria-labelledby="createStationLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-dialog-scrollable">
      <div class="modal-content station-modal">
        <form action="CreateStationController" method="post">
          <div class="modal-header">
            <h5 class="modal-title" id="createStationLabel">Create Station</h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <div class="mb-3">
              <label for="stationName" class="form-label">Station name</label>
              <input type="text" class="form-control" id="stationName" name="stationName" maxlength="150" placeholder="e.g. Plugy Hub" required>
            </div>
            <div class="mb-3">
              <label for="stationAddress" class="form-label">Address</label>
              <input type="text" class="form-control" id="stationAddress" name="address" maxlength="455" placeholder="Street, landmark" required>
            </div>
            <div class="mb-3">
              <label for="stationArea" class="form-label">Area</label>
              <input type="text" class="form-control" id="stationArea" name="area" maxlength="100" placeholder="e.g. Hinjewadi" required>
            </div>
            <div class="row g-3">
              <div class="col-md-6">
                <label for="stationCity" class="form-label">City</label>
                <input type="text" class="form-control" id="stationCity" name="city" maxlength="50" placeholder="e.g. Pune" required>
              </div>
              <div class="col-md-6">
                <label for="stationState" class="form-label">State</label>
                <input type="text" class="form-control" id="stationState" name="state" maxlength="50" value="Maharashtra" required>
              </div>
              <div class="col-12">
                <label for="stationCountry" class="form-label">Country</label>
                <input type="text" class="form-control" id="stationCountry" name="country" maxlength="50" value="India">
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button type="button" class="btn-station btn-station-outline" data-bs-dismiss="modal">Cancel</button>
            <button type="submit" class="btn-station btn-station-solid">Create</button>
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
</body>
</html>
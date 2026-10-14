<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, com.bean.Station, com.dao.AdminDao" %>
<%
    List<Station> stations = new AdminDao().getStations();
    String selectedStation = request.getParameter("stationId");
%>

<!DOCTYPE html>
<html lang="en">


<head>
  <meta charset="utf-8">
  <meta content="width=device-width, initial-scale=1.0" name="viewport">

   <title>Plugy - Manage Hardware</title>
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
  <link href="assets/css/style.css" rel="stylesheet">

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

    <!-- ======= Contact Section ======= -->
    <section class="contact" class="contact">
      <div class="container">

        <div class="section-title text-center">
          <h2>Infrastructure</h2>
          <p>Add / Update Charging Bay</p>
        </div>
	 <%
    boolean hasStation = selectedStation != null && selectedStation.matches("\\d+");
    String backHref = hasStation ? "ManageStation.jsp?stationId=" + selectedStation : "Stations.jsp";
    String backText = hasStation ? "Back to Station" : "Back to Stations";
%>
<div class="mb-3"><a href="<%= backHref %>" style="color:#01036f;font-weight:500;"><i class="bi bi-arrow-left"></i> <%= backText %></a></div>
	
        <div class="row justify-content-center">
          <div class="col-lg-6">
            <div class="card shadow p-4 border-0 rounded-4">
               <form action="ManageSlotsController" method="post">
                 <div class="form-group mb-3">
                   <label class="fw-bold small text-muted">Parent Hub</label>
                   <select class="form-select" name="stationId" required>
                     <option value="">Select charging station</option>
                     <% for (Station s : stations) { %>
                       <option value="<%= s.getId() %>"><%= s.getStationName() %> (<%= s.getArea() %>)</option>
                     <% } %>
                   </select>
                 </div>

                 <div class="form-group mb-3">
                   <label class="fw-bold small text-muted">Bay Identifier</label>
                   <input type="text" class="form-control" name="slotLabel" placeholder="e.g. Bay 1" required>
                 </div>

                 <div class="form-group mb-3">
                   <label class="fw-bold small text-muted">Vehicle Class</label>
                   <select class="form-select" name="vehicleType" required>
                     <option value="4-Wheeler-Fast">4-Wheeler-Fast</option>
                     <option value="4-Wheeler-Normal">4-Wheeler-Normal</option>
                     <option value="2-Wheeler">2-Wheeler</option>
                   </select>
                 </div>

                 <div class="form-group mb-3">
                   <label class="fw-bold small text-muted">Connector Standard</label>
                   <select class="form-select" name="chargerType" required>
                     <option value="CCS2">CCS2</option>
                     <option value="CHAdeMO">CHAdeMO</option>
                     <option value="GB/T">GB/T</option>
                   </select>
                 </div>

                 <div class="form-group mb-4">
                   <label class="fw-bold small text-muted">Hardware State</label>
                   <select class="form-select border-primary" name="status" required>
                     <option value="Available" class="text-success fw-bold">Online (Available)</option>
                     <option value="Maintenance" class="text-danger fw-bold">Offline (Maintenance)</option>
                   </select>
                 </div>

                 <button type="submit" class="btn btn-primary w-100 py-2">Deploy Configuration</button>
               </form>
            </div>
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

  <a href="#" class="back-to-top d-flex align-items-center justify-content-center"><i class="bi bi-arrow-up-short"></i></a>
  <div id="preloader"></div>

  <!-- Vendor JS Files -->
  <script src="assets/vendor/purecounter/purecounter.js"></script>
  <script src="assets/vendor/aos/aos.js"></script>
  <script src="assets/vendor/bootstrap/js/bootstrap.bundle.min.js"></script>
  <script src="assets/vendor/glightbox/js/glightbox.min.js"></script>
  <script src="assets/vendor/swiper/swiper-bundle.min.js"></script>

  <!-- self Main JS File -->
  <script src="assets/js/main.js"></script>

</body>

</html>
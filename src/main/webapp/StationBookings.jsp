<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List, java.sql.Timestamp, java.text.SimpleDateFormat, com.bean.Station, com.bean.Booking, com.dao.AdminDao" %>
<%
    int stationId = -1;
    try { stationId = Integer.parseInt(request.getParameter("stationId")); } catch (Exception ignored) {}
    AdminDao dao = new AdminDao();
    Station station = stationId > 0 ? dao.getStationById(stationId) : null;
    if (station == null) {
        response.sendRedirect("Stations.jsp");
        return;
    }
    List<Booking> bookings = dao.getBookingsByStation(stationId);

    long now = System.currentTimeMillis();
    int currentCount = 0, completedCount = 0;
    double totalAmount = 0;
    for (Booking bk : bookings) {
        Timestamp st = bk.getStartTime(), en = bk.getEndTime();
        if (en != null && now > en.getTime()) completedCount++; else currentCount++;
        try { totalAmount += Double.parseDouble(bk.getAmount().replaceAll("[^0-9.]", "")); } catch (Exception ignored) {}
    }
    SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a");
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

  <title>SMEVCS - Station Bookings</title>
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
    <section id="station-bookings" class="contact stations-section">
      <div class="container">

        <div class="section-title" data-aos="fade-up">
          <h2>Bookings</h2>
          <p><%= esc(station.getStationName()) %></p>
        </div>

        <div class="mb-3">
          <a href="ManageStation.jsp?stationId=<%= station.getId() %>" style="color:#01036f;font-weight:500;"><i class="bi bi-arrow-left"></i> Back to Station</a>
        </div>

        <!-- Summary -->
        <div class="row g-3 slot-summary">
          <div class="col-6 col-md-3">
            <div class="summary-box"><span class="summary-number"><%= bookings.size() %></span><span class="summary-text">Total bookings</span></div>
          </div>
          <div class="col-6 col-md-3">
            <div class="summary-box"><span class="summary-number"><%= currentCount %></span><span class="summary-text">Active / upcoming</span></div>
          </div>
          <div class="col-6 col-md-3">
            <div class="summary-box"><span class="summary-number"><%= completedCount %></span><span class="summary-text">Completed</span></div>
          </div>
          <div class="col-6 col-md-3">
            <div class="summary-box"><span class="summary-number">&#8377;<%= String.format("%.2f", totalAmount) %></span><span class="summary-text">Total amount</span></div>
          </div>
        </div>

        <div class="stations-toolbar mt-4">
          <span class="stations-count">All bookings at this station</span>
        </div>

        <div class="slot-table-card">
          <div class="table-responsive">
            <table class="slot-table">
              <thead>
                <tr>
                  <th>Booking ID</th>
                  <th>Customer</th>
                  <th>Vehicle</th>
                  <th>Slot</th>
                  <th>Start</th>
                  <th>End</th>
                  <th>Amount</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                <% if (bookings.isEmpty()) { %>
                <tr>
                  <td colspan="8" class="slot-empty">
                    <i class="bi bi-plug"></i>
                    <span>No bookings yet for this station.</span>
                  </td>
                </tr>
                <% } else {
                     for (Booking bk : bookings) {
                       Timestamp st = bk.getStartTime(), en = bk.getEndTime();
                       String label, css;
                       if (st == null || en == null) { label = "Booked"; css = "status-other"; }
                       else if (now < st.getTime())  { label = "Upcoming"; css = "status-reserved"; }
                       else if (now <= en.getTime()) { label = "Active"; css = "status-available"; }
                       else                          { label = "Completed"; css = "status-offline"; }
                %>
                <tr>
                  <td class="slot-name">PLG-<%= bk.getId() %>849</td>
                  <td>
                    <strong><%= esc(bk.getOwnerName()) %></strong><br>
                    <span style="font-size:12px;color:#6c757d;"><%= esc(bk.getEmail()) %></span>
                  </td>
                  <td>
                    <%= esc(bk.getVehicleNumber()) %><br>
                    <span style="font-size:12px;color:#6c757d;"><%= esc(bk.getBatteryCapacity()) %> kWh</span>
                  </td>
                  <td>
                    <strong><%= esc(bk.getSlotLabel()) %></strong><br>
                    <span style="font-size:12px;color:#6c757d;"><%= esc(bk.getVehicleType()) %> &middot; <%= esc(bk.getChargerType()) %></span>
                  </td>
                  <td style="white-space:nowrap;"><%= st != null ? sdf.format(st) : "N/A" %></td>
                  <td style="white-space:nowrap;"><%= en != null ? sdf.format(en) : "N/A" %></td>
                  <td style="white-space:nowrap;">
                    <strong>&#8377;<%= esc(bk.getAmount()) %></strong><br>
                    <span class="chip chip-charger" style="margin-top:2px;"><%= esc(bk.getPayMode()) %></span>
                  </td>
                  <td><span class="status-pill <%= css %>"><i class="bi bi-circle-fill"></i> <%= label %></span></td>
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
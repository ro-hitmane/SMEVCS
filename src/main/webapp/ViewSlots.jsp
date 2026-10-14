<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.sql.ResultSet"%>
<%@ page import="java.sql.PreparedStatement"%>
<%@ page import="com.connection.DBConnection"%>
<%@ page import="java.sql.Connection"%>
<%!
    private static String esc(String v) {
        if (v == null) return "";
        return v.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;");
    }

    private static String statusClass(String st) {
        if (st == null) return "status-other";
        String v = st.trim().toLowerCase();
        if (v.equals("available")) return "status-available";
        if (v.equals("reserved") || v.equals("occupied")) return "status-reserved";
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

  <title>Plugy - View Slots</title>
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

  <!-- Main CSS File -->
  <link href="assets/css/style.css?v=6" rel="stylesheet">
</head>

<body>

<jsp:include page="nav_admin.jsp" />

  <!-- ======= Hero Section ======= -->
  <section id="hero">
    <div class="container"></div>

    <svg class="hero-waves" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 24 150 28" preserveAspectRatio="none">
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

    <!-- ======= Section ======= -->
    <section id="contact" class="contact">
      <div class="container">

        <div class="section-title" data-aos="fade-up">
          <h2>Slots</h2>
          <p>View Slots</p>
        </div>
        <div class="mb-3">
          <a href="Stations.jsp" style="color:#01036f;font-weight:500;"><i class="bi bi-arrow-left"></i> Back to Stations</a>
        </div>

        <div class="slot-table-card">
          <div class="table-responsive">
            <table class="slot-table">
              <thead>
                <tr>
                  <th>#</th>
                  <th>Station</th>
                  <th>Area</th>
                  <th>Slot</th>
                  <th>Vehicle Type</th>
                  <th>Charger</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
    <%
    Connection con = DBConnection.getConnection();
    if (con == null) {
    %>
                <tr><td colspan="7" class="slot-empty"><i class="bi bi-exclamation-triangle-fill"></i><span>Could not connect to the database.</span></td></tr>
    <%
    } else {
      try {
        String stationParam = request.getParameter("stationId");
        boolean filtered = stationParam != null && stationParam.matches("\\d+");
        String query = "SELECT sl.id, st.station_name, st.area, sl.slot_label, sl.vehicle_type, sl.charger_type, sl.status, " +
                       "(SELECT COUNT(*) FROM tbl_payment p WHERE p.slot_id = sl.id AND p.start_time <= NOW() AND p.end_time > NOW()) AS active_now " +
                       "FROM tbl_slot sl JOIN tbl_station st ON st.id = sl.station_id " +
                       (filtered ? "WHERE st.id = ? " : "") +
                       "ORDER BY st.station_name, sl.slot_label";
        PreparedStatement st = con.prepareStatement(query);
        if (filtered) st.setInt(1, Integer.parseInt(stationParam));
        ResultSet rs = st.executeQuery();
        int n = 0;
        while (rs.next()) {
          n++;
          String baseStatus = rs.getString("status");
          if (baseStatus == null || baseStatus.trim().isEmpty()) {
              baseStatus = "Available";
          }
          int activeNow = rs.getInt("active_now");
          String displayStatus;
          if ("Offline".equalsIgnoreCase(baseStatus) || baseStatus.toLowerCase().contains("maint")) {
              displayStatus = baseStatus;
          } else if (activeNow > 0) {
              displayStatus = "Reserved";
          } else {
              displayStatus = baseStatus;
          }
    %>
                <tr>
                  <td class="slot-num"><%= n %></td>
                  <td class="slot-name"><%= esc(rs.getString("station_name")) %></td>
                  <td><%= esc(rs.getString("area")) %></td>
                  <td><%= esc(rs.getString("slot_label")) %></td>
                  <td><span class="chip chip-vehicle"><%= esc(rs.getString("vehicle_type")) %></span></td>
                  <td><span class="chip chip-charger"><i class="bi bi-plug"></i> <%= esc(rs.getString("charger_type")) %></span></td>
                  <td><span class="status-pill <%= statusClass(displayStatus) %>"><i class="bi bi-circle-fill"></i> <%= esc(displayStatus) %></span></td>
                </tr>
    <%
        }
        if (n == 0) {
    %>
                <tr><td colspan="7" class="slot-empty"><i class="bi bi-plug"></i><span>No slots added yet.</span></td></tr>
    <%
        }
        rs.close();
        st.close();
      } catch (Exception e) {
        e.printStackTrace();
    %>
                <tr><td colspan="7" class="slot-empty"><i class="bi bi-exclamation-triangle-fill"></i><span>Error loading slots. Check the server log.</span></td></tr>
    <%
      } finally {
        try { con.close(); } catch (Exception ignored) {}
      }
    }
    %>
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

  <!-- Main JS File -->
  <script src="assets/js/main.js"></script>
</body>
</html>
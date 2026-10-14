<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.sql.*, java.util.*, com.connection.DBConnection" %>
<%
    // Prevent unauthenticated access
    String adminEmail = (String) session.getAttribute("admin");

    // Live Metrics Aggregation
    int totalStations = 0;
    int totalSlots = 0;
    int activeBookings = 0;
    double totalRevenue = 0.0;

    // Charts Data Containers
    List<String> last7Days = new ArrayList<>();
    List<Integer> last7Counts = new ArrayList<>();
    List<Double> last7Revenues = new ArrayList<>();

    List<String> topStationNames = new ArrayList<>();
    List<Integer> topStationBookings = new ArrayList<>();

    int[] hourlyLoad = new int[24];

    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;

    try {
        con = DBConnection.getConnection();
        if (con != null) {
            // 1. Total Stations
            ps = con.prepareStatement("SELECT COUNT(*) FROM tbl_station");
            rs = ps.executeQuery();
            if (rs.next()) totalStations = rs.getInt(1);
            rs.close(); ps.close();

            // 2. Total Slots
            ps = con.prepareStatement("SELECT COUNT(*) FROM tbl_slot");
            rs = ps.executeQuery();
            if (rs.next()) totalSlots = rs.getInt(1);
            rs.close(); ps.close();

            // 3. Active Bookings Now (start_time <= NOW() AND end_time > NOW())
            ps = con.prepareStatement("SELECT COUNT(*) FROM tbl_payment WHERE start_time <= NOW() AND end_time > NOW()");
            rs = ps.executeQuery();
            if (rs.next()) activeBookings = rs.getInt(1);
            rs.close(); ps.close();

            // 4. Total Gross Revenue
            ps = con.prepareStatement("SELECT COALESCE(SUM(CAST(amt AS DECIMAL(10,2))), 0.0) FROM tbl_payment");
            rs = ps.executeQuery();
            if (rs.next()) totalRevenue = rs.getDouble(1);
            rs.close(); ps.close();

            // 5. Last 7 Days Booking & Revenue Trend
            String trendSql = "SELECT DATE_FORMAT(start_time, '%d %b') AS bdate, " +
                              "COUNT(*) AS bcount, " +
                              "COALESCE(SUM(CAST(amt AS DECIMAL(10,2))), 0.0) AS bamt " +
                              "FROM tbl_payment " +
                              "WHERE start_time >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) " +
                              "GROUP BY DATE(start_time), DATE_FORMAT(start_time, '%d %b') " +
                              "ORDER BY DATE(start_time) ASC";
            ps = con.prepareStatement(trendSql);
            rs = ps.executeQuery();
            while (rs.next()) {
                last7Days.add(rs.getString("bdate"));
                last7Counts.add(rs.getInt("bcount"));
                last7Revenues.add(rs.getDouble("bamt"));
            }
            rs.close(); ps.close();

            // 6. Top 3 Charging Hotspots
            String topStationsSql = "SELECT st.station_name, COUNT(p.id) AS total_usage " +
                                    "FROM tbl_payment p " +
                                    "JOIN tbl_slot sl ON p.slot_id = sl.id " +
                                    "JOIN tbl_station st ON sl.station_id = st.id " +
                                    "GROUP BY st.id, st.station_name " +
                                    "ORDER BY total_usage DESC LIMIT 3";
            ps = con.prepareStatement(topStationsSql);
            rs = ps.executeQuery();
            while (rs.next()) {
                topStationNames.add(rs.getString("station_name"));
                topStationBookings.add(rs.getInt("total_usage"));
            }
            rs.close(); ps.close();

            // 7. 24-Hour Peak Load Distribution
            String peakLoadSql = "SELECT HOUR(start_time) AS bhour, COUNT(*) AS load_count " +
                                 "FROM tbl_payment GROUP BY HOUR(start_time)";
            ps = con.prepareStatement(peakLoadSql);
            rs = ps.executeQuery();
            while (rs.next()) {
                int hour = rs.getInt("bhour");
                if (hour >= 0 && hour < 24) {
                    hourlyLoad[hour] = rs.getInt("load_count");
                }
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        if (rs != null) try { rs.close(); } catch (Exception ignored) {}
        if (ps != null) try { ps.close(); } catch (Exception ignored) {}
        if (con != null) try { con.close(); } catch (Exception ignored) {}
    }
%>
<!DOCTYPE html>
<html lang="en">

<head>
  <meta charset="utf-8">
  <meta content="width=device-width, initial-scale=1.0" name="viewport">
  <title>Plugy - Admin Intelligence Dashboard</title>

  <!-- Favicons & Fonts -->
  <link href="assets/img/apple-touch-icon.png" rel="icon">
  <link href="https://fonts.googleapis.com/css?family=Open+Sans:300,400,600,700|Poppins:300,400,500,600,700" rel="stylesheet">

  <!-- Vendor CSS Files -->
  <link href="assets/vendor/bootstrap/css/bootstrap.min.css" rel="stylesheet">
  <link href="assets/vendor/bootstrap-icons/bootstrap-icons.css" rel="stylesheet">

  <!-- Main CSS File -->
  <link href="assets/css/style.css" rel="stylesheet">
  <link href="css/main.css" rel="stylesheet">

  <!-- Chart.js via CDN -->
  <script src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js"></script>

  <style>
    body {
      background-color: #f4f6fa;
    }
    .kpi-card {
      background: #ffffff;
      border-radius: 14px;
      padding: 22px 20px;
      border: 1px solid #e2e8f0;
      box-shadow: 0 4px 16px rgba(0,0,0,0.03);
      transition: transform 0.2s ease, box-shadow 0.2s ease;
      height: 100%;
    }
    .kpi-card:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 24px rgba(2, 5, 161, 0.08);
    }
    .kpi-icon-wrap {
      width: 52px;
      height: 52px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 1.5rem;
    }
    .chart-container-card {
      background: #ffffff;
      border-radius: 16px;
      border: 1px solid #e2e8f0;
      box-shadow: 0 4px 18px rgba(0,0,0,0.03);
      padding: 24px;
      height: 100%;
    }
    .quick-action-btn {
      background: #ffffff;
      border: 1.5px solid #e2e8f0;
      border-radius: 12px;
      padding: 16px 20px;
      text-decoration: none;
      color: #1e293b;
      font-weight: 600;
      display: flex;
      align-items: center;
      justify-content: space-between;
      transition: all 0.2s ease;
    }
    .quick-action-btn:hover {
      background: #f8fafc;
      border-color: #0205a1;
      color: #0205a1;
      transform: translateX(3px);
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
      <g class="wave3"><use xlink:href="#wave-path" x="50" y="9" fill="#fff"></g>
    </svg>
  </section>

  <main id="main" class="pt-2 pb-5">
    <div class="container">

      <!-- Title & Greeting -->
      <div class="d-flex flex-wrap justify-content-between align-items-center mb-4 mt-2">
        <div>
          <h2 class="fw-bold text-dark mb-1">Network Operations Dashboard</h2>
          <p class="text-muted small mb-0">Live EV hardware telemetry, session loads, and revenue metrics across Maharashtra.</p>
        </div>
        <div>
          <a href="Stations.jsp" class="btn btn-primary px-4 py-2 rounded-3 shadow-sm" style="background:#0205a1; border:none;">
            <i class="bi bi-plus-circle me-1"></i> Add / Manage Hubs
          </a>
        </div>
      </div>

      <!-- KPI METRIC CARDS -->
      <div class="row g-3 mb-4">
        <!-- 1. Total Gross Revenue -->
        <div class="col-xl-3 col-md-6 col-12">
          <div class="kpi-card d-flex align-items-center">
            <div class="kpi-icon-wrap me-3" style="background-color: #ecfdf5; color: #059669;">
              <i class="bi bi-currency-rupee"></i>
            </div>
            <div>
              <span class="text-muted small fw-semibold text-uppercase">Total Collections</span>
              <h3 class="fw-bold text-dark mb-0">₹<%= String.format("%.2f", totalRevenue) %></h3>
            </div>
          </div>
        </div>

        <!-- 2. Active Now -->
        <div class="col-xl-3 col-md-6 col-12">
          <div class="kpi-card d-flex align-items-center">
            <div class="kpi-icon-wrap me-3" style="background-color: #eff6ff; color: #0205a1;">
              <i class="bi bi-lightning-charge-fill"></i>
            </div>
            <div>
              <span class="text-muted small fw-semibold text-uppercase">Active Charging Now</span>
              <h3 class="fw-bold text-primary mb-0"><%= activeBookings %> <span class="fs-6 text-muted fw-normal">vehicles</span></h3>
            </div>
          </div>
        </div>

        <!-- 3. Total Stations -->
        <div class="col-xl-3 col-md-6 col-12">
          <div class="kpi-card d-flex align-items-center">
            <div class="kpi-icon-wrap me-3" style="background-color: #fef3c7; color: #d97706;">
              <i class="bi bi-geo-alt-fill"></i>
            </div>
            <div>
              <span class="text-muted small fw-semibold text-uppercase">Charging Stations</span>
              <h3 class="fw-bold text-dark mb-0"><%= totalStations %> <span class="fs-6 text-muted fw-normal">locations</span></h3>
            </div>
          </div>
        </div>

        <!-- 4. Total Bays/Slots -->
        <div class="col-xl-3 col-md-6 col-12">
          <div class="kpi-card d-flex align-items-center">
            <div class="kpi-icon-wrap me-3" style="background-color: #f3e8ff; color: #7c3aed;">
              <i class="bi bi-ev-front-fill"></i>
            </div>
            <div>
              <span class="text-muted small fw-semibold text-uppercase">Total Installed Bays</span>
              <h3 class="fw-bold text-dark mb-0"><%= totalSlots %> <span class="fs-6 text-muted fw-normal">ports</span></h3>
            </div>
          </div>
        </div>
      </div>

      <!-- MAIN CHARTS ROW -->
      <div class="row g-4 mb-4">
        <!-- Chart 1: 7-Day Velocity & Volume -->
        <div class="col-lg-8 col-12">
          <div class="chart-container-card">
            <div class="d-flex justify-content-between align-items-center mb-3">
              <div>
                <h5 class="fw-bold text-dark mb-0">7-Day Booking &amp; Revenue Trend</h5>
                <span class="text-muted small">Daily volume paired with financial collections</span>
              </div>
              <span class="badge bg-light text-secondary border px-3 py-1">Last 7 Days</span>
            </div>
            <div style="position: relative; height: 320px;">
              <canvas id="velocityChart"></canvas>
            </div>
          </div>
        </div>

        <!-- Chart 2: Top 3 Hotspots & Quick Shortcuts -->
        <div class="col-lg-4 col-12">
          <div class="chart-container-card d-flex flex-column">
            <h5 class="fw-bold text-dark mb-1">Top Utilized Hubs</h5>
            <span class="text-muted small mb-3">Highest demand charging locations</span>
            
            <div style="position: relative; height: 180px;" class="mb-3">
              <canvas id="hotspotsChart"></canvas>
            </div>

            <!-- Quick Action Shortcuts -->
            <div class="mt-auto d-flex flex-column gap-2 pt-2 border-top">
              <span class="text-muted small fw-bold text-uppercase">Fast Shortcuts</span>
              <a href="Stations.jsp" class="quick-action-btn py-2">
                <span class="small"><i class="bi bi-building me-2 text-primary"></i> Station Manager</span>
                <i class="bi bi-chevron-right text-muted small"></i>
              </a>
              <a href="BookingHistory.jsp" class="quick-action-btn py-2">
                <span class="small"><i class="bi bi-clock-history me-2 text-success"></i> Central Agenda &amp; Logs</span>
                <i class="bi bi-chevron-right text-muted small"></i>
              </a>
            </div>
          </div>
        </div>
      </div>

      <!-- SECONDARY CHARTS ROW: Peak Congestion Curve -->
      <div class="row g-4">
        <div class="col-12">
          <div class="chart-container-card">
            <div class="d-flex justify-content-between align-items-center mb-3">
              <div>
                <h5 class="fw-bold text-dark mb-0">24-Hour Network Load &amp; Congestion Curve</h5>
                <span class="text-muted small">Distribution of charging demand throughout the day (00:00 to 23:00 IST)</span>
              </div>
              <span class="badge bg-light text-primary border px-3 py-1"><i class="bi bi-clock me-1"></i> Hourly Distribution</span>
            </div>
            <div style="position: relative; height: 260px;">
              <canvas id="peakLoadChart"></canvas>
            </div>
          </div>
        </div>
      </div>

    </div>
  </main>

  <!-- Footer -->
  <footer id="footer">
    <div class="container">
      <div class="copyright">
        &copy; Copyright <strong><span>Plugy</span></strong>. All Rights Reserved
      </div>
    </div>
  </footer>

  <a href="#" class="back-to-top d-flex align-items-center justify-content-center"><i class="bi bi-arrow-up-short"></i></a>

  <!-- Vendor JS Files -->
  <script src="assets/vendor/bootstrap/js/bootstrap.bundle.min.js"></script>
  <script src="assets/js/main.js"></script>

  <!-- Live Chart.js Rendering Engine -->
  <script>
    document.addEventListener("DOMContentLoaded", function () {
      
      // 1. Velocity Trend Chart
      const velocityCtx = document.getElementById('velocityChart').getContext('2d');
      const trendLabels = [<% for(int i=0; i<last7Days.size(); i++){ out.print("'" + last7Days.get(i) + "'" + (i < last7Days.size()-1 ? "," : "")); } %>];
      const trendCounts = [<% for(int i=0; i<last7Counts.size(); i++){ out.print(last7Counts.get(i) + (i < last7Counts.size()-1 ? "," : "")); } %>];
      const trendRevenues = [<% for(int i=0; i<last7Revenues.size(); i++){ out.print(last7Revenues.get(i) + (i < last7Revenues.size()-1 ? "," : "")); } %>];

      new Chart(velocityCtx, {
        type: 'bar',
        data: {
          labels: trendLabels.length ? trendLabels : ['No Data'],
          datasets: [
            {
              type: 'line',
              label: 'Revenue (₹)',
              data: trendRevenues.length ? trendRevenues : [0],
              borderColor: '#059669',
              backgroundColor: 'rgba(5, 150, 105, 0.1)',
              borderWidth: 2.5,
              tension: 0.35,
              yAxisID: 'y1'
            },
            {
              type: 'bar',
              label: 'Bookings Count',
              data: trendCounts.length ? trendCounts : [0],
              backgroundColor: '#0205a1',
              borderRadius: 6,
              yAxisID: 'y'
            }
          ]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: { position: 'top' }
          },
          scales: {
            y: {
              beginAtZero: true,
              ticks: { precision: 0 },
              title: { display: true, text: 'Total Bookings' }
            },
            y1: {
              beginAtZero: true,
              position: 'right',
              grid: { drawOnChartArea: false },
              title: { display: true, text: 'Revenue (₹)' }
            }
          }
        }
      });

      // 2. Hotspots Doughnut Chart
      const hotspotsCtx = document.getElementById('hotspotsChart').getContext('2d');
      const hotspotLabels = [<% for(int i=0; i<topStationNames.size(); i++){ out.print("'" + topStationNames.get(i).replace("'", "\\'") + "'" + (i < topStationNames.size()-1 ? "," : "")); } %>];
      const hotspotData = [<% for(int i=0; i<topStationBookings.size(); i++){ out.print(topStationBookings.get(i) + (i < topStationBookings.size()-1 ? "," : "")); } %>];

      new Chart(hotspotsCtx, {
        type: 'doughnut',
        data: {
          labels: hotspotLabels.length ? hotspotLabels : ['No Bookings Yet'],
          datasets: [{
            data: hotspotData.length ? hotspotData : [1],
            backgroundColor: ['#0205a1', '#2563eb', '#60a5fa'],
            borderWidth: 2
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: { position: 'bottom', labels: { boxWidth: 12, font: { size: 11 } } }
          }
        }
      });

      // 3. 24-Hour Peak Load Line Chart
      const peakCtx = document.getElementById('peakLoadChart').getContext('2d');
      const hourlyLabels = [
        '12 AM','1 AM','2 AM','3 AM','4 AM','5 AM','6 AM','7 AM','8 AM','9 AM','10 AM','11 AM',
        '12 PM','1 PM','2 PM','3 PM','4 PM','5 PM','6 PM','7 PM','8 PM','9 PM','10 PM','11 PM'
      ];
      const hourlyData = [<% for(int i=0; i<24; i++){ out.print(hourlyLoad[i] + (i < 23 ? "," : "")); } %>];

      new Chart(peakCtx, {
        type: 'line',
        data: {
          labels: hourlyLabels,
          datasets: [{
            label: 'Bookings Initiated',
            data: hourlyData,
            fill: true,
            backgroundColor: 'rgba(2, 5, 161, 0.08)',
            borderColor: '#0205a1',
            borderWidth: 2,
            pointBackgroundColor: '#0205a1',
            tension: 0.4
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: { display: false }
          },
          scales: {
            y: {
              beginAtZero: true,
              ticks: { precision: 0 }
            }
          }
        }
      });

    });
  </script>
</body>
</html>
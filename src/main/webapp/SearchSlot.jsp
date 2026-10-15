<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.sql.*, com.connection.DBConnection" %>
<%
    // Retain or set session email
    String userEmail = request.getParameter("email");
    if (userEmail != null && !userEmail.trim().isEmpty()) {
        session.setAttribute("email", userEmail.trim());
    } else {
        userEmail = (String) session.getAttribute("email");
    }

    // Capture parameters passed from Search.jsp
    String passedAmount = request.getParameter("amount");
    if (passedAmount == null || passedAmount.trim().isEmpty()) { passedAmount = "0.00"; }
    
    String searchArea = request.getParameter("area");
    if (searchArea == null) { searchArea = ""; }
    
    String vehicleType = request.getParameter("vehicleType");
    if (vehicleType == null || vehicleType.trim().isEmpty()) { vehicleType = "%"; }
    
    String portType = request.getParameter("portType");
    if (portType == null || portType.trim().isEmpty()) { portType = "%"; }
    
    String duration = request.getParameter("duration");
    if (duration == null || duration.trim().isEmpty()) { duration = "60"; }

    // FLOW GATEKEEPER: Checks if user came from Search form with an area and calculated price
    boolean isFromSearchFlow = (searchArea != null && !searchArea.trim().isEmpty()) 
                               && (passedAmount != null && !passedAmount.trim().isEmpty() && !passedAmount.equals("0.00"));
%>
<!DOCTYPE html>
<html lang="en">

<head>
  <meta charset="utf-8">
  <meta content="width=device-width, initial-scale=1.0" name="viewport">
  <title>Plugy - Select Charging Slot</title>

  <!-- Favicons & Fonts -->
  <link href="assets/img/apple-touch-icon.png" rel="icon">
  <link href="https://fonts.googleapis.com/css?family=Open+Sans:300,400,600,700|Poppins:300,400,500,600,700" rel="stylesheet">

  <!-- Vendor CSS Files -->
  <link href="assets/vendor/bootstrap/css/bootstrap.min.css" rel="stylesheet">
  <link href="assets/vendor/bootstrap-icons/bootstrap-icons.css" rel="stylesheet">

  <!-- Main CSS File -->
  <link href="assets/css/style.css" rel="stylesheet">
  <link href="css/main.css" rel="stylesheet">

  <style>
  /* Blend the page background without altering layout heights */
  body, #main, section.contact {
    background-color: #f8fafc !important;
  }

  /* Remove only the inline SVG gap beneath the wave */
  #hero .hero-waves {
    display: block !important;
    vertical-align: bottom !important;
    margin-bottom: -1px !important;
  }
    .dashboard-card {
      background: #ffffff;
      border-radius: 14px;
      transition: transform 0.2s ease, box-shadow 0.2s ease;
    }
    .dashboard-card:hover {
      box-shadow: 0 10px 25px rgba(2, 5, 161, 0.08) !important;
    }
    .station-title {
      font-size: 1.15rem;
      line-height: 1.4;
      word-break: break-word;
      color: #1e293b;
    }
    .status-badge {
      font-size: 0.78rem;
      letter-spacing: 0.3px;
      white-space: nowrap;
    }
    /* Fixed high-contrast primary button style */
    .btn-search-custom {
      background-color: #0205a1 !important;
      background: linear-gradient(135deg, #0205a1 0%, #1e3a8a 100%) !important;
      color: #ffffff !important;
      font-weight: 600 !important;
      font-size: 0.95rem;
      border-radius: 8px !important;
      border: none !important;
      padding: 10px 24px;
      box-shadow: 0 4px 14px rgba(2, 5, 161, 0.25);
      transition: all 0.2s ease;
      text-decoration: none !important;
      display: inline-flex;
      align-items: center;
      justify-content: center;
    }
    .btn-search-custom:hover {
      background-color: #010478 !important;
      background: linear-gradient(135deg, #010478 0%, #172554 100%) !important;
      color: #ffffff !important;
      box-shadow: 0 6px 18px rgba(2, 5, 161, 0.35);
      transform: translateY(-1px);
    }
    .btn-search-custom:disabled,
    .btn-search-custom.disabled {
      background-color: #e2e8f0 !important;
      background: #e2e8f0 !important;
      border-color: #cbd5e1 !important;
      color: #94a3b8 !important;
      cursor: not-allowed;
      box-shadow: none !important;
    }
    /* Interactive Preview Button */
    .btn-preview-book {
      background-color: #ffffff !important;
      border: 2px solid #0205a1 !important;
      color: #0205a1 !important;
      font-weight: 700 !important;
      border-radius: 8px !important;
      transition: all 0.2s ease;
      display: inline-flex;
      align-items: center;
      justify-content: center;
    }
    .btn-preview-book:hover {
      background-color: #0205a1 !important;
      color: #ffffff !important;
      box-shadow: 0 4px 14px rgba(2, 5, 161, 0.25);
      transform: translateY(-1px);
    }
  </style>
</head>

<body>

  <!-- Navigation -->
  <jsp:include page="nav_user.jsp" />

  <!-- Hero Section -->
  <section id="hero" style="padding: 60px 0 0 0;">
    <svg class="hero-waves" xmlns="http://www.w3.org/2000/svg" viewBox="0 24 150 28" preserveAspectRatio="none">
      <defs><path id="wave-path" d="M-160 44c30 0 58-18 88-18s 58 18 88 18 58-18 88-18 58 18 88 18 v44h-352z"></defs>
      <g class="wave1"><use xlink:href="#wave-path" x="50" y="3" fill="rgba(255,255,255, .1)"></g>
      <g class="wave2"><use xlink:href="#wave-path" x="50" y="0" fill="rgba(255,255,255, .2)"></g>
      <g class="wave3"><use xlink:href="#wave-path" x="50" y="9" fill="#f8fafc"></g>
    </svg>
  </section>

  <main id="main">
    <section class="contact pt-2">
      <div class="container">
        
        <div class="section-title text-center mb-4">
          <h2>Book Slot</h2>
          <p><%= isFromSearchFlow ? "Available Charging Bays in " + searchArea : "All Charging Bays (Preview Mode)" %></p>
        </div>

        <!-- Informational Banner for Direct Navbar Visitors -->
        <% if (!isFromSearchFlow) { %>
          <div class="row justify-content-center mb-3">
            <div class="col-lg-10">
              <div class="alert alert-warning border-0 shadow-sm d-flex flex-wrap justify-content-between align-items-center py-3 px-4 rounded-4" style="background-color: #fffbeb; border: 1.5px solid #fde68a !important;">
                <div class="d-flex align-items-center mb-2 mb-md-0">
                  <i class="bi bi-info-circle-fill text-warning fs-3 me-3"></i>
                  <div>
                    <strong class="text-dark">Browsing Mode</strong>
                    <p class="mb-0 text-muted small">You are viewing all network bays. Click <strong>Book This Bay</strong> on any card to prefill that station and calculate pricing.</p>
                  </div>
                </div>
                <a href="Search.jsp" class="btn btn-search-custom px-4 py-2 text-nowrap">
                  <i class="bi bi-search me-1"></i> Start Search Flow
                </a>
              </div>
            </div>
          </div>
        <% } %>

        <!-- Dynamic Bays Grid -->
        <div class="row g-4 justify-content-center">
          <%
             Connection con = null;
             PreparedStatement ps = null;
             ResultSet rs = null;
             try {
                 con = DBConnection.getConnection();
                 
                 String query = "SELECT s.id AS slot_id, s.slot_label, s.status, s.vehicle_type, s.charger_type, " +
                                "st.id AS station_id, st.station_name, st.address, st.area, st.city, " +
                                "(SELECT COUNT(*) FROM tbl_payment p " +
                                " WHERE p.slot_id = s.id AND p.start_time <= NOW() AND p.end_time > NOW()) AS active_now " +
                                "FROM tbl_slot s " +
                                "JOIN tbl_station st ON s.station_id = st.id " +
                                "WHERE (st.area LIKE ? OR st.station_name LIKE ?) " +
                                "AND s.vehicle_type LIKE ? AND s.charger_type LIKE ? " +
                                "ORDER BY st.city ASC, st.station_name ASC, s.id ASC";
                                
                 ps = con.prepareStatement(query);
                 ps.setString(1, "%" + searchArea + "%");
                 ps.setString(2, "%" + searchArea + "%");
                 ps.setString(3, vehicleType);
                 ps.setString(4, portType);
                 
                 rs = ps.executeQuery();
                 
                 boolean found = false;
                 while(rs.next()) {
                     found = true;
                     String slotId = rs.getString("slot_id");
                     String slotLabel = rs.getString("slot_label");
                     String stationName = rs.getString("station_name");
                     String address = rs.getString("address");
                     String areaName = rs.getString("area");
                     String city = rs.getString("city");
                     String hardwareStatus = rs.getString("status");
                     String slotVehicle = rs.getString("vehicle_type");
                     String slotCharger = rs.getString("charger_type");
                     int activeNow = rs.getInt("active_now");
                     
                     if (hardwareStatus == null || hardwareStatus.trim().isEmpty()) { hardwareStatus = "Available"; }
                     
                     boolean isMaintenance = "Maintenance".equalsIgnoreCase(hardwareStatus);
                     boolean isOccupied = (activeNow > 0) || "Occupied".equalsIgnoreCase(hardwareStatus);
                     boolean isAvailable = !isMaintenance && !isOccupied;
                     
                     String displayBadge = isMaintenance ? "Maintenance" : (isOccupied ? "Occupied" : "Available");
                     String badgeClass = isMaintenance ? "bg-warning text-dark" : (isOccupied ? "bg-danger" : "bg-success");
                     String borderColor = isMaintenance ? "border-warning" : (isOccupied ? "border-danger" : "border-success");
          %>
                  
          <div class="col-lg-4 col-md-6 col-12">
            <div class="dashboard-card shadow-sm p-4 h-100 border-top border-4 <%= borderColor %> d-flex flex-column">
              
              <!-- Responsive Header Section -->
              <div class="mb-3">
                <div class="d-flex justify-content-between align-items-start gap-2 mb-2">
                  <h5 class="fw-bold station-title mb-0 flex-grow-1"><%= stationName %></h5>
                  <span class="badge <%= badgeClass %> px-3 py-2 rounded-pill status-badge flex-shrink-0"><%= displayBadge %></span>
                </div>
                <div class="badge bg-light text-primary border mb-2"><%= slotLabel %></div>
                <p class="text-muted small mb-0"><i class="bi bi-geo-alt-fill text-danger me-1"></i> <%= address %>, <%= city %></p>
              </div>
              
              <!-- Card Details & Actions Footer -->
              <div class="mt-auto">
                <hr class="text-muted my-3">
                <div class="d-flex justify-content-between align-items-center mb-1">
                  <span class="small text-secondary">Vehicle:</span>
                  <span class="small fw-semibold text-dark"><%= slotVehicle %></span>
                </div>
                <div class="d-flex justify-content-between align-items-center mb-4">
                  <span class="small text-secondary">Port:</span>
                  <span class="small fw-bold text-primary"><i class="bi bi-lightning-charge-fill text-warning"></i> <%= slotCharger %></span>
                </div>

                <% if (isAvailable) { %>
                  <% if (isFromSearchFlow) { %>
                    <!-- Active reservation button when in flow -->
                    <button type="button" class="btn btn-search-custom w-100 py-2" 
                            onclick="openPaymentModal('<%= slotId %>', '<%= stationName.replace("'", "\\'") %> - <%= slotLabel %>', '<%= passedAmount %>')">
                      Book &amp; Pay Now
                    </button>
                  <% } else { %>
                    <!-- Preview action: prompts user and forwards station details to Search.jsp -->
                    <button type="button" class="btn btn-preview-book w-100 py-2" 
                            onclick="promptToPrefillFlow('<%= stationName.replace("'", "\\'") %>', '<%= slotLabel %>', '<%= areaName.replace("'", "\\'") %>', '<%= slotVehicle %>', '<%= slotCharger %>')">
                      <i class="bi bi-lightning-charge me-1"></i> Book This Bay
                    </button>
                  <% } %>
                <% } else if (isMaintenance) { %>
                  <button type="button" class="btn btn-search-custom w-100 py-2" disabled>
                    Under Maintenance
                  </button>
                <% } else { %>
                  <button type="button" class="btn btn-search-custom w-100 py-2" disabled>
                    Currently Reserved
                  </button>
                <% } %>
              </div>
            </div>
          </div>
          
          <%
                 } 
                 
                 if (!found) {
          %>
            <div class="col-12 text-center py-5">
              <h4 class="text-muted">No compatible bays found.</h4>
              <p class="text-muted small">Try searching another location or altering vehicle specifications.</p>
              <a href="Search.jsp" class="btn btn-outline-primary mt-3">Search Another Station</a>
            </div>
          <%
                 } 
                 
             } catch (Exception e) { 
                 out.println("<p class='text-danger text-center'>Error connecting to database.</p>");
                 e.printStackTrace();
             } finally {
                 if (rs != null) try { rs.close(); } catch (Exception e) {}
                 if (ps != null) try { ps.close(); } catch (Exception e) {}
                 if (con != null) try { con.close(); } catch (Exception e) {}
             }
          %>
        </div>

      </div>
    </section>
  </main>

  <!-- Flow Prefill Modal (Opens when user clicks "Book This Bay" in Preview mode) -->
  <div class="modal fade" id="prefillModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content rounded-4 border-0 shadow-lg text-center p-4">
        <div class="modal-body p-2">
          <div class="mb-3">
            <span class="badge rounded-circle p-3" style="background-color: #e0e7ff;">
              <i class="bi bi-lightning-charge-fill fs-1 text-primary"></i>
            </span>
          </div>
          <h5 class="fw-bold text-dark mb-1" id="modalTargetStation">Station Name</h5>
          <div class="badge bg-light text-primary border mb-3" id="modalTargetBay">Bay 1</div>
          <p class="text-muted small px-3 mb-4">
            To generate verified pricing and confirm your reservation, we will prefill this station in the booking setup. Select your duration and proceed.
          </p>
          <div class="d-flex justify-content-center gap-2">
            <button type="button" class="btn btn-light px-4 py-2 border rounded-3" data-bs-dismiss="modal">Cancel</button>
            <a href="#" id="modalPrefillBtn" class="btn btn-search-custom px-4 py-2 rounded-3">
              <i class="bi bi-arrow-right-circle me-1"></i> Configure &amp; Book
            </a>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- Checkout & Scheduling Modal -->
  <div class="modal fade" id="paymentModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content rounded-4 border-0 shadow-lg">
        <div class="modal-header border-bottom-0 pb-0">
          <h5 class="modal-title fw-bold text-dark">Plugy Secure Checkout</h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body pt-2 pb-4 px-4">
          <p class="text-muted small mb-3">Reserving: <strong id="modalSlotName" class="text-primary"></strong></p>
          
          <form action="PayController" method="post">
            <input type="hidden" name="slot_id" id="modalSlotId">
            <input type="hidden" name="email" value="<%= userEmail != null ? userEmail : "" %>">
            <input type="hidden" name="duration" value="<%= duration %>">
            
            <div class="form-group mb-3">
              <label class="custom-label">Vehicle Owner Name <span class="text-danger">*</span></label>
              <input type="text" class="form-control custom-input" name="ownername" placeholder="Name on registration" required>
            </div>
            
            <div class="row g-3 mb-3">
              <div class="col-sm-6 col-12 form-group">
                <label class="custom-label">Vehicle Number <span class="text-danger">*</span></label>
                <input type="text" class="form-control custom-input" name="number" placeholder="MH-12-AB-1234" required>
              </div>
              <div class="col-sm-6 col-12 form-group">
                <label class="custom-label">Battery Capacity <span class="text-danger">*</span></label>
                <div class="input-group">
                  <input type="number" class="form-control custom-input border-end-0" name="bat" placeholder="40" required>
                  <span class="input-group-text bg-white">kWh</span>
                </div>
              </div>
            </div>

            <!-- Booking Schedule Toggle -->
            <div class="form-group mb-3 border p-3 rounded bg-light">
              <label class="custom-label fw-bold mb-2">Booking Schedule <span class="text-danger">*</span></label>
              <div class="d-flex gap-4 mb-2 flex-wrap">
                <div class="form-check">
                  <input class="form-check-input" type="radio" name="booking_type" id="bookNow" value="immediate" checked onchange="toggleSchedule()">
                  <label class="form-check-label text-dark" for="bookNow">Book Immediately</label>
                </div>
                <div class="form-check">
                  <input class="form-check-input" type="radio" name="booking_type" id="bookLater" value="reserve" onchange="toggleSchedule()">
                  <label class="form-check-label text-dark" for="bookLater">Reserve for Later</label>
                </div>
              </div>
              
              <!-- Date/Time Picker -->
              <div id="reserveTimeContainer" style="display: none; margin-top: 10px;">
                <label class="custom-label small text-muted">Select Start Date &amp; Time</label>
                <input type="datetime-local" class="form-control custom-input" name="start_time" id="start_time_input">
              </div>
            </div>

            <div class="form-group mb-3">
              <label class="custom-label">Payment Mode <span class="text-danger">*</span></label>
              <select class="form-select custom-input" name="pay_mode" required>
                <option value="UPI">UPI (GPay, PhonePe, Paytm)</option>
                <option value="Card">Credit / Debit Card</option>
                <option value="Cash">Cash at Station</option>
              </select>
            </div>

            <div class="p-3 bg-light rounded border mb-4">
              <div class="d-flex justify-content-between align-items-center">
                <span class="fw-bold text-dark">Total Payable:</span>
                <span class="fw-bold text-success fs-5">₹<input type="text" class="bg-transparent border-0 text-success fw-bold p-0 text-end" style="width: 90px; outline: none;" id="modalAmount" name="amt" readonly></span>
              </div>
            </div>

            <button type="submit" class="btn btn-search-custom w-100 py-3">Confirm &amp; Pay</button>
          </form>
        </div>
      </div>
    </div>
  </div>

  <!-- Footer -->
  <jsp:include page="footer_user.jsp" />

  <!-- Vendor JS Files -->
  <script src="assets/vendor/bootstrap/js/bootstrap.bundle.min.js"></script>
  <script src="assets/js/main.js"></script>

  <script>
    function promptToPrefillFlow(stationName, bayLabel, areaName, vehicleType, chargerType) {
      document.getElementById('modalTargetStation').innerText = stationName;
      document.getElementById('modalTargetBay').innerText = bayLabel;
      
      const targetUrl = 'Search.jsp?area=' + encodeURIComponent(areaName) + 
                        '&vehicleType=' + encodeURIComponent(vehicleType) + 
                        '&portType=' + encodeURIComponent(chargerType);
                        
      document.getElementById('modalPrefillBtn').setAttribute('href', targetUrl);
      
      var modal = new bootstrap.Modal(document.getElementById('prefillModal'));
      modal.show();
    }

    function openPaymentModal(slotId, slotName, amount) {
      document.getElementById('modalSlotId').value = slotId;
      document.getElementById('modalSlotName').innerText = slotName;
      document.getElementById('modalAmount').value = amount;
      
      const now = new Date();
      const localMin = new Date(now.getTime() - now.getTimezoneOffset() * 60000).toISOString().slice(0, 16);
      document.getElementById('start_time_input').min = localMin;
      
      var myModal = new bootstrap.Modal(document.getElementById('paymentModal'));
      myModal.show();
    }

    function toggleSchedule() {
      const isReserve = document.getElementById('bookLater').checked;
      const timeContainer = document.getElementById('reserveTimeContainer');
      const timeInput = document.getElementById('start_time_input');
      
      if (isReserve) {
        timeContainer.style.display = 'block';
        timeInput.required = true;
      } else {
        timeContainer.style.display = 'none';
        timeInput.required = false;
        timeInput.value = '';
      }
    }
  </script>
</body>
</html>
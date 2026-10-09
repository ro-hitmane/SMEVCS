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
    .btn-search-custom:disabled,
    .btn-search-custom.disabled {
      background-color: #e9ecef !important;
      border-color: #dee2e6 !important;
      color: #6c757d !important;
      cursor: not-allowed;
      box-shadow: none !important;
    }
  </style>
</head>

<body>

  <!-- Navigation -->
  <jsp:include page="nav_user.jsp" />

  <!-- Background Hero -->
  <section id="hero" style="padding: 60px 0 0 0;">
    <svg class="hero-waves" xmlns="http://www.w3.org/2000/svg" viewBox="0 24 150 28" preserveAspectRatio="none">
      <defs><path id="wave-path" d="M-160 44c30 0 58-18 88-18s 58 18 88 18 58-18 88-18 58 18 88 18 v44h-352z"></defs>
      <g class="wave1"><use xlink:href="#wave-path" x="50" y="3" fill="rgba(255,255,255, .1)"></g>
      <g class="wave2"><use xlink:href="#wave-path" x="50" y="0" fill="rgba(255,255,255, .2)"></g>
      <g class="wave3"><use xlink:href="#wave-path" x="50" y="9" fill="#fff"></g>
    </svg>
  </section>

  <main id="main">
    <section class="contact pt-2">
      <div class="container">
        
        <div class="section-title text-center mb-4">
          <h2>Book Slot</h2>
          <p>Available Charging Bays in <%= searchArea.isEmpty() ? "Selected Area" : searchArea %></p>
        </div>

        <!-- Dynamic Bays Grid -->
        <div class="row g-4 justify-content-center">
          <%
             Connection con = null;
             PreparedStatement ps = null;
             ResultSet rs = null;
             try {
                 con = DBConnection.getConnection();
                 
                 // Relational query joining slot and parent station
                 String query = "SELECT s.id AS slot_id, s.slot_label, s.status, s.vehicle_type, s.charger_type, " +
                                "st.station_name, st.address, st.city " +
                                "FROM tbl_slot s " +
                                "JOIN tbl_station st ON s.station_id = st.id " +
                                "WHERE st.area LIKE ? AND s.vehicle_type LIKE ? AND s.charger_type LIKE ? " +
                                "ORDER BY s.id ASC";
                                
                 ps = con.prepareStatement(query);
                 ps.setString(1, "%" + searchArea + "%");
                 ps.setString(2, vehicleType);
                 ps.setString(3, portType);
                 
                 rs = ps.executeQuery();
                 
                 boolean found = false;
                 while(rs.next()) {
                     found = true;
                     String slotId = rs.getString("slot_id");
                     String slotLabel = rs.getString("slot_label");
                     String stationName = rs.getString("station_name");
                     String address = rs.getString("address");
                     String city = rs.getString("city");
                     String status = rs.getString("status");
                     String slotVehicle = rs.getString("vehicle_type");
                     String slotCharger = rs.getString("charger_type");
                     
                     if (status == null || status.trim().isEmpty()) { status = "Available"; }
                     boolean isAvailable = "Available".equalsIgnoreCase(status);
                     
                     String borderColor = isAvailable ? "border-success" : "border-danger";
                     String badgeClass = isAvailable ? "bg-success" : "bg-danger";
          %>
                  
          <div class="col-lg-4 col-md-6">
            <div class="dashboard-card shadow-sm p-4 h-100 position-relative border-top border-4 <%= borderColor %> d-flex flex-column">
              <div>
                <span class="badge <%= badgeClass %> position-absolute top-0 end-0 m-3 px-3 py-2 rounded-pill"><%= status %></span>
                <h4 class="fw-bold text-dark mb-1"><%= stationName %></h4>
                <div class="badge bg-light text-primary border mb-2"><%= slotLabel %></div>
                <p class="text-muted small mb-0"><i class="bi bi-geo-alt-fill text-danger me-1"></i> <%= address %>, <%= city %></p>
              </div>
              
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
                  <button type="button" class="btn btn-search-custom w-100 py-2" 
                          onclick="openPaymentModal('<%= slotId %>', '<%= stationName %> - <%= slotLabel %>', '<%= passedAmount %>')">
                    Book & Pay Now
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
              <h4 class="text-muted">No compatible bays found in "<%= searchArea %>".</h4>
              <p class="text-muted small">Try selecting another area or altering your vehicle port filters.</p>
              <a href="Search.jsp" class="btn btn-outline-primary mt-3">Search Another Area</a>
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
              <div class="col-6 form-group">
                <label class="custom-label">Vehicle Number <span class="text-danger">*</span></label>
                <input type="text" class="form-control custom-input" name="number" placeholder="MH-12-AB-1234" required>
              </div>
              <div class="col-6 form-group">
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
              <div class="d-flex gap-4 mb-2">
                <div class="form-check">
                  <input class="form-check-input" type="radio" name="booking_type" id="bookNow" value="immediate" checked onchange="toggleSchedule()">
                  <label class="form-check-label text-dark" for="bookNow">Book Immediately</label>
                </div>
                <div class="form-check">
                  <input class="form-check-input" type="radio" name="booking_type" id="bookLater" value="reserve" onchange="toggleSchedule()">
                  <label class="form-check-label text-dark" for="bookLater">Reserve for Later</label>
                </div>
              </div>
              
              <!-- Hidden Date/Time Picker -->
              <div id="reserveTimeContainer" style="display: none; margin-top: 10px;">
                <label class="custom-label small text-muted">Select Start Date & Time</label>
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

            <button type="submit" class="btn btn-search-custom w-100 py-3">Confirm & Pay</button>
          </form>
        </div>
      </div>
    </div>
  </div>

  <jsp:include page="footer_user.jsp" />

  <!-- Vendor JS Files -->
  <script src="assets/vendor/bootstrap/js/bootstrap.bundle.min.js"></script>
  
  <script src="assets/js/main.js"></script>

  <script>
    function openPaymentModal(slotId, slotName, amount) {
      document.getElementById('modalSlotId').value = slotId;
      document.getElementById('modalSlotName').innerText = slotName;
      document.getElementById('modalAmount').value = amount;
      
      // Enforce minimum selectable time as right now
      const now = new Date();
      now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
      document.getElementById('start_time_input').min = now.toISOString().slice(0, 16);
      
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
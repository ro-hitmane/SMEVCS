<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.sql.*, com.connection.DBConnection" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%
    // Check session first, then request parameter as fallback
    String userEmail = (String) session.getAttribute("email");
    if (userEmail == null || userEmail.trim().isEmpty()) {
        userEmail = request.getParameter("email");
        if (userEmail != null && !userEmail.trim().isEmpty()) {
            session.setAttribute("email", userEmail.trim());
        }
    }
%>
<!DOCTYPE html>
<html lang="en">

<head>
  <meta charset="utf-8">
  <title>Plugy - My Bookings</title>
  
  <link href="assets/img/apple-touch-icon.png" rel="icon">
  <link href="assets/vendor/bootstrap/css/bootstrap.min.css" rel="stylesheet">
  <link href="assets/vendor/bootstrap-icons/bootstrap-icons.css" rel="stylesheet">
  <link href="assets/css/style.css" rel="stylesheet">
  <link href="css/main.css" rel="stylesheet">
  
  <style>
    .receipt-slip { background: #fff; border: 2px dashed #dbe2ea; border-radius: 8px; padding: 25px; }
    .receipt-header { border-bottom: 2px solid #20c997; padding-bottom: 15px; margin-bottom: 15px; }
    @media print {
      body * { visibility: hidden; }
      #slipModal .modal-content, #slipModal .modal-content * { visibility: visible; }
      #slipModal .modal-content { position: absolute; left: 0; top: 0; width: 100%; border: none; }
      .no-print { display: none !important; }
    }
  </style>
</head>

<body>

  <jsp:include page="nav_user.jsp" />

  <section id="hero" style="padding: 60px 0 0 0;">
    <svg class="hero-waves" xmlns="http://www.w3.org/2000/svg" viewBox="0 24 150 28" preserveAspectRatio="none">
      <defs><path id="wave-path" d="M-160 44c30 0 58-18 88-18s 58 18 88 18 58-18 88-18 58 18 88 18 v44h-352z"></defs>
      <g class="wave1"><use xlink:href="#wave-path" x="50" y="3" fill="rgba(255,255,255, .1)"></g>
      <g class="wave2"><use xlink:href="#wave-path" x="50" y="0" fill="rgba(255,255,255, .2)"></g>
      <g class="wave3"><use xlink:href="#wave-path" x="50" y="9" fill="#fff"></g>
    </svg>
  </section>

  <main id="main">
    <section class="contact pt-4">
      <div class="container">
        
        <div class="section-title text-center mb-4">
          <h2>Plugy</h2>
          <p>My Bookings & Receipts</p>
        </div>

        <div class="row justify-content-center">
          <div class="col-lg-10">
            <div class="dashboard-card shadow-sm p-4">
              
              <% if (userEmail == null || userEmail.isEmpty()) { %>
                <div class="text-center py-5">
                  <h4 class="text-muted">No active session found.</h4>
                  <p class="text-muted small">Please search for charging points and enter your email to view bookings.</p>
                  <a href="Search.jsp" class="btn btn-search-custom px-4 mt-2">Search Points</a>
                </div>
              <% } else { %>
              
                <ul class="nav nav-pills nav-fill mb-4" id="bookingTabs" role="tablist">
                  <li class="nav-item" role="presentation">
                    <button class="nav-link active fw-bold border" id="active-tab" data-bs-toggle="tab" data-bs-target="#active" type="button" role="tab">Current Reservations</button>
                  </li>
                  <li class="nav-item ms-2" role="presentation">
                    <button class="nav-link fw-bold border" id="history-tab" data-bs-toggle="tab" data-bs-target="#history" type="button" role="tab">Previous Bookings</button>
                  </li>
                </ul>

                <div class="tab-content" id="bookingTabsContent">
                  
                  <!-- CURRENT RESERVATIONS TAB -->
                  <div class="tab-pane fade show active" id="active" role="tabpanel">
                    <div class="table-responsive">
                      <table class="table table-hover align-middle">
                        <thead class="table-light">
                          <tr>
                            <th>Booking ID</th>
                            <th>Station & Bay</th>
                            <th>Vehicle No.</th>
                            <th>Payment</th>
                            <th>Status</th>
                            <th class="text-end">Action</th>
                          </tr>
                        </thead>
                        <tbody>
                          <%
                             Connection con = null;
                             PreparedStatement ps = null;
                             ResultSet rs = null;
                             SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a");
                             
                             try {
                                 con = DBConnection.getConnection();
                                 // Relational query pulling from payment, slot, and station tables
                                 String query = "SELECT p.id as payment_id, p.ownername, p.number, p.pay_mode, p.amt, p.start_time, p.end_time, " +
                                                "s.slot_label, s.vehicle_type, s.charger_type, " +
                                                "st.station_name, st.address, st.city " +
                                                "FROM tbl_payment p " +
                                                "JOIN tbl_slot s ON p.slot_id = s.id " +
                                                "JOIN tbl_station st ON s.station_id = st.id " +
                                                "WHERE p.email = ? ORDER BY p.id DESC";
                                                
                                 ps = con.prepareStatement(query);
                                 ps.setString(1, userEmail);
                                 rs = ps.executeQuery();
                                 
                                 boolean hasRecords = false;
                                 while(rs.next()) {
                                     hasRecords = true;
                                     String txId = "PLG-" + rs.getInt("payment_id") + "849";
                                     String owner = rs.getString("ownername");
                                     String vehicle = rs.getString("number");
                                     String mode = rs.getString("pay_mode");
                                     String amt = rs.getString("amt");
                                     
                                     String stationName = rs.getString("station_name");
                                     String slotLabel = rs.getString("slot_label");
                                     String location = rs.getString("city");
                                     
                                     Timestamp startTime = rs.getTimestamp("start_time");
                                     Timestamp endTime = rs.getTimestamp("end_time");
                                     
                                     String formattedStart = (startTime != null) ? sdf.format(startTime) : "N/A";
                                     String formattedEnd = (endTime != null) ? sdf.format(endTime) : "N/A";
                                     
                                     // Escape single quotes for JavaScript function call
                                     String fullStationStr = (stationName + ", " + location).replace("'", "\\'");
                          %>
                          <tr>
                            <td class="fw-semibold text-primary"><%= txId %></td>
                            <td>
                                <strong><%= stationName %></strong><br>
                                <span class="text-muted small"><%= slotLabel %></span>
                            </td>
                            <td><%= vehicle %></td>
                            <td>
                                <div class="fw-bold text-success">₹<%= amt %></div>
                                <span class="badge bg-light text-dark border px-2 py-0"><%= mode %></span>
                            </td>
                            <td><span class="badge bg-success px-3 py-2 rounded-pill">Reserved</span></td>
                            <td class="text-end">
                              <button class="btn btn-sm btn-outline-secondary" 
                                      onclick="viewSlip('<%= txId %>', '<%= owner %>', '<%= vehicle %>', '<%= mode %>', '<%= amt %>', '<%= fullStationStr %>', '<%= slotLabel %>', '<%= formattedStart %>', '<%= formattedEnd %>')">
                                <i class="bi bi-receipt"></i> View Slip
                              </button>
                            </td>
                          </tr>
                          <%
                                 }
                                 if (!hasRecords) {
                                     out.println("<tr><td colspan='6' class='text-center text-muted py-4'>No current reservations found for " + userEmail + "</td></tr>");
                                 }
                             } catch (Exception e) { 
                                 e.printStackTrace();
                                 out.println("<tr><td colspan='6' class='text-center text-danger'>Failed to load bookings.</td></tr>");
                             } finally {
                                 if(rs != null) try{ rs.close(); } catch(Exception e){}
                                 if(ps != null) try{ ps.close(); } catch(Exception e){}
                                 if(con != null) try{ con.close(); } catch(Exception e){}
                             }
                          %>
                        </tbody>
                      </table>
                    </div>
                  </div>

                  <!-- PREVIOUS BOOKINGS TAB -->
                  <div class="tab-pane fade" id="history" role="tabpanel">
                    <div class="table-responsive">
                      <table class="table table-hover align-middle text-muted">
                        <thead class="table-light">
                          <tr>
                            <th>Booking ID</th>
                            <th>Station & Bay</th>
                            <th>Vehicle No.</th>
                            <th>Payment</th>
                            <th>Status</th>
                            <th class="text-end">Action</th>
                          </tr>
                        </thead>
                        <tbody>
                          <tr>
                            <td colspan="6" class="text-center text-muted py-4">No previous booking history available.</td>
                          </tr>
                        </tbody>
                      </table>
                    </div>
                  </div>

                </div>
              <% } %>

            </div>
          </div>
        </div>
      </div>
    </section>
  </main>

  <!-- Official Receipt Modal -->
  <div class="modal fade" id="slipModal" tabindex="-1" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
      <div class="modal-content rounded-4 border-0 shadow-lg">
        <div class="modal-header border-bottom-0 pb-0 no-print">
          <h5 class="modal-title fw-bold text-dark">Official Tax Invoice</h5>
          <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
        </div>
        <div class="modal-body p-4">
          
          <div class="receipt-slip bg-white p-4 border rounded-4 shadow-sm position-relative">
            <div class="receipt-header text-center pb-3 mb-3 border-bottom">
              <h3 class="fw-bold text-primary mb-0"><i class="bi bi-lightning-charge-fill text-warning"></i> Plugy</h3>
              <p class="text-muted small mb-1">Smart Management of EV Charging</p>
              <span class="badge bg-light text-secondary border px-2 py-1" style="font-size: 11px;">GSTIN: 27AAAAA0000A1Z5</span>
            </div>
            
            <div class="d-flex justify-content-between text-muted small mb-3 px-1">
              <span>Invoice: <strong class="text-dark" id="slipTxId"></strong></span>
              <span id="slipDate"></span>
            </div>

            <table class="table table-sm table-borderless text-start mb-0 align-middle">
              <tbody>
                <tr>
                  <td class="text-muted py-2">Customer Name</td>
                  <td class="fw-semibold py-2 text-end text-dark" id="slipOwner"></td>
                </tr>
                <tr>
                  <td class="text-muted py-2">Vehicle Number</td>
                  <td class="fw-semibold py-2 text-end text-dark" id="slipVehicle"></td>
                </tr>
                <tr>
                  <td class="text-muted py-2">Location</td>
                  <td class="fw-semibold py-2 text-end text-primary" id="slipStation"></td>
                </tr>
                <tr>
                  <td class="text-muted py-2">Reserved Bay</td>
                  <td class="fw-semibold py-2 text-end text-dark" id="slipBay"></td>
                </tr>
                <tr>
                  <td class="text-muted py-2">Valid From</td>
                  <td class="fw-semibold py-2 text-end text-dark" id="slipStart"></td>
                </tr>
                <tr>
                  <td class="text-muted py-2">Valid Until</td>
                  <td class="fw-semibold py-2 text-end text-danger" id="slipEnd"></td>
                </tr>
                <tr>
                  <td class="text-muted py-2">Payment Method</td>
                  <td class="fw-semibold py-2 text-end text-dark" id="slipMode"></td>
                </tr>
                <tr>
                  <td colspan="2"><hr class="my-2 border-dashed"></td>
                </tr>
                <tr>
                  <td class="fw-bold text-dark fs-6 py-1">Total Amount Paid</td>
                  <td class="fw-bold text-success fs-4 py-1 text-end">₹<span id="slipAmt"></span></td>
                </tr>
              </tbody>
            </table>
            
            <div class="text-center mt-4 pt-3 border-top">
              <p class="small text-muted mb-0 fw-medium">⚡ Please vacate the bay by the 'Valid Until' time.</p>
              <p class="text-muted" style="font-size: 10px;">This is a computer-generated digital receipt.</p>
            </div>
          </div>
          
          <button type="button" class="btn btn-search-custom w-100 py-3 mt-4 no-print shadow-sm" onclick="window.print()">
            <i class="bi bi-printer me-2"></i> Print / Save Receipt
          </button>
        </div>
      </div>
    </div>
  </div>

  <jsp:include page="footer_user.jsp" />
  <script src="assets/vendor/bootstrap/js/bootstrap.bundle.min.js"></script>
  <script src="assets/js/main.js"></script>
  
  <script>
    // Injects dynamic, accurate database values into the digital receipt modal
    function viewSlip(txId, owner, vehicle, mode, amt, stationName, bay, start, end) {
      document.getElementById('slipTxId').innerText = txId;
      document.getElementById('slipOwner').innerText = owner;
      document.getElementById('slipVehicle').innerText = vehicle;
      document.getElementById('slipMode').innerText = mode;
      document.getElementById('slipAmt').innerText = amt;
      document.getElementById('slipStation').innerText = stationName;
      document.getElementById('slipBay').innerText = bay;
      document.getElementById('slipStart').innerText = start;
      document.getElementById('slipEnd').innerText = end;
      
      const options = { year: 'numeric', month: 'short', day: 'numeric' };
      document.getElementById('slipDate').innerText = new Date().toLocaleDateString('en-US', options);
      
      var slipModal = new bootstrap.Modal(document.getElementById('slipModal'));
      slipModal.show();
    }
  </script>
</body>
</html>
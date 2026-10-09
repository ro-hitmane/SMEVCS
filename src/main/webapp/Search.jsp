<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.sql.*, com.connection.DBConnection" %>
<!DOCTYPE html>
<html lang="en">

<head>
  <meta charset="utf-8">
  <meta content="width=device-width, initial-scale=1.0" name="viewport">
  <title>Plugy - Search Charging Points</title>
  
  <!-- Favicons & Fonts -->
  <link href="assets/img/apple-touch-icon.png" rel="icon">
  <link href="https://fonts.googleapis.com/css?family=Open+Sans:300,400,600,700|Poppins:300,400,500,600,700" rel="stylesheet">

  <!-- Vendor CSS Files -->
  <link href="assets/vendor/bootstrap/css/bootstrap.min.css" rel="stylesheet">
  <link href="assets/vendor/bootstrap-icons/bootstrap-icons.css" rel="stylesheet">
  
  <!-- Main CSS File -->
  <link href="assets/css/style.css" rel="stylesheet">
  <link href="css/main.css" rel="stylesheet">
</head>

<body>

  <!-- navbar -->
  <jsp:include page="nav_user.jsp" />

  <!-- ======= Hero Section ======= -->
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
          <h2>Dashboard</h2>
          <p>Search Charging Points</p>
        </div>

        <div class="row justify-content-center">
          <div class="col-lg-10">
            <div class="dashboard-card shadow">
              
              <!-- Booking Form -->
              <form action="SearchSlot.jsp" method="post" id="evBookingForm">
                
                <!-- Hidden inputs to safely pass dynamically calculated values -->
                <input type="hidden" name="amount" id="hiddenAmount" value="0.00">
                
                <div class="row g-4 text-start">
                  
                  <!-- Left Column: Charging Requirements -->
                  <div class="col-md-6">
                    <div class="form-group mb-3">
                      <label class="custom-label">Select City / Area <span class="text-danger">*</span></label>
                      <select class="form-select custom-input" name="area" required>
                        <option value="" disabled selected>Select location address</option>
                        <%
                           Connection conStation = null;
                           PreparedStatement psStation = null;
                           ResultSet rsStation = null;
                           try {
                               conStation = DBConnection.getConnection();
                               String query = "SELECT DISTINCT area, city FROM tbl_station WHERE area IS NOT NULL AND area != '' ORDER BY area ASC";
                               psStation = conStation.prepareStatement(query);
                               rsStation = psStation.executeQuery();
                               
                               while(rsStation.next()) {
                                   String areaName = rsStation.getString("area");
                                   String cityName = rsStation.getString("city");
                        %>
                                   <option value="<%= areaName %>"><%= areaName %>, <%= cityName %></option>
                        <%
                               }
                           } catch (Exception e) {
                               e.printStackTrace();
                           } finally {
                               if(rsStation != null) try{ rsStation.close(); } catch(Exception e){}
                               if(psStation != null) try{ psStation.close(); } catch(Exception e){}
                               if(conStation != null) try{ conStation.close(); } catch(Exception e){}
                           }
                        %>
                      </select>
                    </div>

                    <div class="form-group mb-3">
                      <label class="custom-label">Vehicle Type <span class="text-danger">*</span></label>
                      <select class="form-select custom-input" name="vehicleType" id="vehicleType" onchange="calculatePrice()" required>
                        <option value="" disabled>Select EV Model / Type</option>
                        <option value="2-Wheeler">2-Wheeler (Standard)</option>
                        <option value="4-Wheeler-Normal">4-Wheeler (Normal AC)</option>
                        <option value="4-Wheeler-Fast" selected>4-Wheeler (Fast DC)</option>
                      </select>
                    </div>

                    <div class="form-group mb-3">
                      <label class="custom-label">Charging Port Type <span class="text-danger">*</span></label>
                      <select class="form-select custom-input" name="portType" id="portType" onchange="calculatePrice()">
                        <option value="CCS2">Type 2 / CCS2</option>
                        <option value="CHAdeMO">CHAdeMO</option>
                        <option value="GB/T">GB/T</option>
                      </select>
                    </div>

                    <div class="form-group mb-3">
                      <label class="custom-label">Booking Duration <span class="text-danger">*</span></label>
                      <select class="form-select custom-input" name="duration" id="durationSelect" onchange="calculatePrice()">
                        <option value="30">30 Minutes</option>
                        <option value="60" selected>1 Hour</option>
                        <option value="120">2 Hours</option>
                      </select>
                    </div>
                  </div>

                  <!-- Right Column: User & Billing Info -->
                  <div class="col-md-6">
                    <div class="form-group mb-3">
                      <label class="custom-label">Email Address <span class="text-danger">*</span></label>
                      <input type="email" class="form-control custom-input" name="email" placeholder="name@example.com" required>
                    </div>

                    <div class="form-group mb-3">
                      <label class="custom-label">Contact Number <span class="text-danger">*</span></label>
                      <input type="tel" class="form-control custom-input" name="contact" placeholder="Enter 10-digit mobile number" required>
                    </div>

                    <!-- Visual Total Amount (Read-only) -->
                    <div class="form-group mb-3">
                      <label class="custom-label">Estimated Amount (incl. GST) <span class="text-danger">*</span></label>
                      <div class="input-group">
                        <span class="input-group-text bg-white border-end-0">₹</span>
                        <input type="text" class="form-control custom-input border-start-0 fw-bold text-success" id="visualAmount" value="0.00" disabled>
                      </div>
                    </div>

                    <!-- Slot Counter & Pricing Breakdown -->
                    <div class="pricing-card p-3 rounded mb-3 bg-light border">
                      <div class="d-flex justify-content-between align-items-center mb-2">
                        <span class="text-muted small">Total Bays Needed</span>
                        <div class="d-flex align-items-center">
                          <button type="button" class="btn btn-sm btn-outline-secondary py-0 px-2" onclick="changeSlots(-1)">-</button>
                          <span id="slotCount" class="mx-2 fw-bold">1</span>
                          <button type="button" class="btn btn-sm btn-outline-secondary py-0 px-2" onclick="changeSlots(1)">+</button>
                        </div>
                      </div>

                      <div class="d-flex justify-content-between align-items-center mb-2">
                        <span class="text-muted small">Base Rate</span>
                        <span class="fw-semibold" id="baseRateDisplay">₹0.00 / hr</span>
                      </div>
                      <hr class="my-2">
                      <div class="d-flex justify-content-between align-items-center mb-1">
                        <span class="fw-semibold">Subtotal</span>
                        <span class="fw-bold text-dark" id="subtotalDisplay">₹0.00</span>
                      </div>
                      <div class="d-flex justify-content-between align-items-center mb-1">
                        <span class="text-muted small">GST (18%)</span>
                        <span class="fw-semibold text-muted" id="gstDisplay">₹0.00</span>
                      </div>
                      <div class="d-flex justify-content-between align-items-center mt-2">
                        <span class="fw-bold text-dark">Final Payable</span>
                        <span class="fw-bold text-success fs-5" id="finalPayableDisplay">₹0.00</span>
                      </div>
                    </div>
                  </div>
                </div>

                <!-- Submit Button -->
                <div class="text-center mt-4">
                  <button type="submit" class="btn btn-search-custom px-5 py-2">Find Available Slots</button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </div>
    </section>
  </main>

  <jsp:include page="footer_user.jsp" />

  <!-- Vendor JS Files -->
  <script src="assets/vendor/bootstrap/js/bootstrap.bundle.min.js"></script>
  
  <script src="assets/js/main.js"></script>

  <!-- Dynamic Pricing Engine -->
  <script>
    let slots = 1;

    function changeSlots(delta) {
      slots = Math.max(1, slots + delta);
      document.getElementById('slotCount').innerText = slots;
      calculatePrice();
    }

    function calculatePrice() {
      const vehicle = document.getElementById('vehicleType').value;
      const durationMins = parseInt(document.getElementById('durationSelect').value);
      const port = document.getElementById('portType').value;

      let baseHourlyRate = 0;
      if (vehicle === '2-Wheeler') {
        baseHourlyRate = 25.00;
      } else if (vehicle === '4-Wheeler-Normal') {
        baseHourlyRate = 75.00;
      } else if (vehicle === '4-Wheeler-Fast') {
        baseHourlyRate = 200.00;
      }

      // GB/T discount rule
      if (port === 'GB/T') {
        baseHourlyRate = baseHourlyRate * 0.90;
      }

      const durationHours = durationMins / 60;
      const subtotal = baseHourlyRate * durationHours * slots;
      const gst = subtotal * 0.18;
      const finalTotal = subtotal + gst;

      // Update UI Text
      document.getElementById('baseRateDisplay').innerText = "₹" + baseHourlyRate.toFixed(2) + " / hr";
      document.getElementById('subtotalDisplay').innerText = "₹" + subtotal.toFixed(2);
      document.getElementById('gstDisplay').innerText = "₹" + gst.toFixed(2);
      document.getElementById('finalPayableDisplay').innerText = "₹" + finalTotal.toFixed(2);
      
      // Update visual disabled input
      document.getElementById('visualAmount').value = finalTotal.toFixed(2);
      // Update hidden input that gets submitted securely
      document.getElementById('hiddenAmount').value = finalTotal.toFixed(2);
    }

    // Initialize pricing on page load
    document.addEventListener("DOMContentLoaded", function() {
      calculatePrice();
    });
  </script>

</body>
</html>
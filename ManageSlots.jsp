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
        <h1><a href="index.jsp"><span>TATA Power</span></a></h1>
      </div>

      <nav id="navbar" class="navbar">
        <ul>
         <li><a class="nav-link scrollto active" href="AdminHome.jsp">Home</a></li>        
          <li><a class="nav-link scrollto" href="ManageSlots.jsp">Manage Charging Slots</a></li>
           <li><a class="nav-link scrollto" href="ViewSlots.jsp">View Slots</a></li>
           <li><a class="nav-link scrollto" href="BookingHistory.jsp">View Booking History</a></li>
           
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
    <section id="contact" class="contact">
      <div class="container">

        <div class="section-title" data-aos="fade-up">
          <h2>Slots</h2>
          <p>Add Charging Slots</p>
        </div>

        <div class="row">
          <div class="col-lg-8 mt-5 mt-lg-0" data-aos="fade-left" data-aos-delay="200">

            <form action="ManageSlotsController" method="post" role="form" class="email-form">
             
              <div class="form-group mt-3">
                <input type="text" class="form-control" name="slotname" id="subject" placeholder="Slot Name" required>
              </div>
              
              <div class="form-group mt-3">
                <input type="text" class="form-control" name="slotaddress" id="subject" placeholder="Slot Address" required>
              </div>
              
               <div class="form-group mt-3">
                <input type="text" class="form-control" name="slotarea" id="subject" placeholder="Slot Area" required>
              </div>
              
               <div class="form-group mt-3">
                <input type="text" class="form-control" name="city" id="subject" placeholder="City" required>
              </div>
              
               <div class="form-group mt-3">
                <input type="text" class="form-control" name="state" id="subject" placeholder="State" required>
              </div>
               
               <div class="form-group mt-3">
                <input type="text" class="form-control" name="country" id="subject" placeholder="Country" required>
              </div>
             <br><br>
             <div class="text-center"><button type="submit">Add Slots</button></div>
            </form>
          </div>
        </div>

      </div>
    </section>
    
    <!-- End Section -->

  </main>
  <!-- End #main -->

  <!-- ======= Footer ======= -->
  <footer id="footer">
    

    <div class="container">
      <div class="copyright">
        &copy; Copyright <strong><span>TATA Power 2021</span></strong>. All Rights Reserved
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
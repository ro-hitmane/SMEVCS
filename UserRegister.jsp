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

	<!-- self CSS File -->
 	<link href="assets/css/style.css" rel="stylesheet">
 	
</head>
 <script>
function myFunction() {
  var x = document.getElementById("myInput");
  if (x.type === "password") {
    x.type = "text";
  } else {
    x.type = "password";
  }
}
</script>
<body>

  <!-- ======= Header ======= -->
  <header id="header" class="fixed-top d-flex align-items-center header-transparent">
    <div class="container d-flex align-items-center justify-content-between">

      <div class="logo">
        <h1><a href="index.jsp"><span>Charge it</span></a></h1>
      </div>

      <nav id="navbar" class="navbar">
      <ul>
          <li><a class="nav-link scrollto active" href="#hero">Home</a></li>
         <li><a class="nav-link scrollto " href="#about">About</a></li>
         <li><a class="nav-link scrollto " href="#contact">Contact Us</a></li>         
          <li><a class="nav-link scrollto" href="AdminLogin.jsp">Admin Login</a></li>
           <li><a class="nav-link scrollto" href="UserLogin.jsp">User Login</a></li>
        </ul>
        <i class="bi bi-list mobile-nav-toggle"></i>
      </nav>
      <!-- End navbar -->
    </div>
  </header>
  <!-- End Header -->

  <!-- ======= Hero Section ======= -->
  <section id="hero">

    <div class="container">
      <div class="row justify-content-between">
       
       
      </div>
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

  </section>
  <!-- End Hero -->

  <main id="main">
    <section id="contact" class="contact">
      <div class="container">

        <div class="section-title" data-aos="fade-up">
          <h2>User</h2>
          <p>User Registration</p>
        </div>

        <div class="row">

         

          <div class="col-lg-8 mt-5 mt-lg-0" data-aos="fade-left" data-aos-delay="200">

            <form action="UserRegistrationController" method="post" id="form1" role="form" class="email-form">
             
              <div class="form-group mt-3">
                <input type="text" class="form-control" name="name" id="subject" placeholder="Name" pattern="[A-Za-z]+" title="Name must be alphabets only" required="required">
              </div>
               <div class="form-group mt-3">
                <input type="text" class="form-control" name="address" id="subject" placeholder="Address" required>
              </div>
               <div class="form-group mt-3">
                <input type="text" class="form-control" name="mobile_no" id="subject" placeholder="Mobile No" pattern="[7-9]{1}[0-9]{9}" title="Mobile number must be starts with 7, 8 or 9 digit and total number of digits are 10" required="required">
              </div>
              <div class="form-group mt-3">
                <input type="email" class="form-control" name="email" id="subject" placeholder="Email" pattern="[a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,3}$" title="Email-Id must be valid format." required="required">
              </div>
               <div class="form-group mt-3">
                <input type="password" class="form-control" name="password" id="myInput" placeholder="Password" pattern="(?=^.{8,}$)((?=.*\d)|(?=.*\W+))(?![.\n])(?=.*[A-Z])(?=.*[a-z]).*$" title="Password must contain at least one number and one special character and one uppercase and lowercase letter, and at least 8 or more characters" required="required">
              <input type="checkbox" onclick="myFunction()">Show Password
              </div>
              <div class="form-group mt-3">
                <input type="password" class="form-control" name="conf_password" id="subject" placeholder="Confirm Password" pattern="(?=^.{8,}$)((?=.*\d)|(?=.*\W+))(?![.\n])(?=.*[A-Z])(?=.*[a-z]).*$" title="Password must contain at least one number and one special character and one uppercase and lowercase letter, and at least 8 or more characters" required="required">
              </div>
               <div class="form-group mt-3">
                <input type="text" class="form-control" name="vehicle_no" id="subject" placeholder="Vehicle No" required>
              </div>
               <div class="form-group mt-3">
                <input type="text" class="form-control" name="vehicle_name" id="subject" placeholder="Vehicle Name" required>
              </div>
              <div class="form-group mt-3">
                <input type="text" class="form-control" name="vehicle_owner_name" id="subject" placeholder="Vehicle Owner Name" pattern="[A-Za-z]+" title="Name must be alphabets only" required="required">
              </div>
             <br><br>
             
              <div class="text-center"> <button type="submit" form="form1" value="Submit">Register</button>&nbsp;&nbsp;&nbsp;<br><br>if you have already registered<br> <a href="UserLogin.jsp">click here?</a></div>
              
            </form>

          </div>

        </div>

      </div>
    </section>
    <!-- End Contact Section -->

  </main>
  <!-- End #main -->

  <!-- ======= Footer ======= -->
  <footer id="footer">
    

    <div class="container">
      <div class="copyright">
        &copy; Copyright <strong><span>Charge it 2021</span></strong>. All Rights Reserved
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

  <!-- Self Main JS File -->
  <script src="assets/js/main.js"></script>

</body>

</html>
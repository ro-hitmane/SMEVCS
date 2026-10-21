<!DOCTYPE html>
<html lang="en">

<head>
  <meta charset="utf-8">
  <meta content="width=device-width, initial-scale=1.0" name="viewport">

  <title>User Registration | SMEVCS</title>
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

  <!-- self Main CSS File -->
  <link href="assets/css/style.css?v=2" rel="stylesheet">
  <!-- Same extras as the login pages; card, inputs and button styles come from style.css -->
  <style>
    .contact .login-form .login-badge {
      width: 64px;
      height: 64px;
      margin: 0 auto 14px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 50%;
      font-size: 28px;
      color: #3f43fd;
      background: #f0f0ff;
      box-shadow: 0 8px 18px rgba(63, 67, 253, 0.18);
    }
    .contact .login-form .input-wrap input[type=password],
    .contact .login-form .input-wrap input[type=text] {
      padding-right: 44px;
    }
    .contact .login-form .toggle-password {
      position: absolute;
      top: 50%;
      right: 6px;
      transform: translateY(-50%);
      width: 36px;
      height: 36px;
      padding: 0;
      border: 0;
      border-radius: 50%;
      background: transparent;
      color: #8f93c9;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      transition: color 0.25s ease, background 0.25s ease;
    }
    .contact .login-form .toggle-password i {
      position: static;
      transform: none;
      color: inherit;
      pointer-events: none;
    }
    .contact .login-form .toggle-password:hover {
      color: #3f43fd;
      background: #f0f0ff;
    }
    .contact .login-form .login-footer {
      margin-top: 22px;
      padding-top: 16px;
      border-top: 1px solid #eceeff;
      font-size: 14px;
    }
    .contact .login-form .login-footer a {
      color: #6b6f9a;
      text-decoration: none;
      transition: color 0.25s ease;
    }
    .contact .login-form .login-footer a:hover {
      color: #3f43fd;
    }
  
    .contact .login-form .login-footer .register-link {
      color: #3f43fd;
      font-weight: 600;
    }
    .contact .login-form .login-footer .register-link:hover {
      color: #1acc8d;
    }
    .contact .login-form .login-footer p {
      margin: 0 0 10px;
      color: #6b6f9a;
    }
  </style>
</head>

<body>

<jsp:include page="nav_index.jsp" />


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

        <div class="row justify-content-center">
          <div class="col-lg-6 col-md-9 mt-5 mt-lg-0" data-aos="fade-up" data-aos-delay="200">

            <form action="UserRegistrationController" method="post" id="form1" role="form" class="email-form login-form">
              <div class="form-header text-center">
                <div class="login-badge"><i class="bi bi-person-plus"></i></div>
                <h3>Create your account</h3>
                <p>Register to search charging slots, book them and pay online.</p>
              </div>
              <% String error = (String) request.getAttribute("error");
   if (error != null) { %>
  <div class="alert alert-danger" role="alert"><%= error %></div>
<% } %>

              <div class="form-group ">
                <label for="name">Name</label>
                <div class="input-wrap">
                  <i class="bi bi-person"></i>
                  <input type="text" class="form-control" name="name" id="name" placeholder="Your name" pattern="[A-Za-z ]+" title="Name must be alphabets only" autocomplete="name" required="required">
                </div>
              </div>
              <div class="form-group mt-3">
                <label for="address">Address</label>
                <div class="input-wrap">
                  <i class="bi bi-geo-alt"></i>
                  <input type="text" class="form-control" name="address" id="address" placeholder="Your address" autocomplete="street-address" required="required">
                </div>
              </div>
              <div class="form-group mt-3">
                <label for="mobile_no">Mobile number</label>
                <div class="input-wrap">
                  <i class="bi bi-telephone"></i>
                  <input type="text" class="form-control" name="mobile_no" id="mobile_no" placeholder="10-digit mobile number" pattern="[7-9]{1}[0-9]{9}" title="Mobile number must be starts with 7, 8 or 9 digit and total number of digits are 10" inputmode="numeric" maxlength="10" autocomplete="tel" required="required">
                </div>
              </div>
              <div class="form-group mt-3">
                <label for="email">Email</label>
                <div class="input-wrap">
                  <i class="bi bi-envelope"></i>
                  <input type="email" class="form-control" name="email" id="email" placeholder="you@example.com" autocomplete="username" pattern="[a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,3}$" title="Email-Id must be valid format." required="required">
                </div>
              </div>
              <div class="form-group mt-3">
                <label for="password">Password</label>
                <div class="input-wrap">
                  <i class="bi bi-lock"></i>
                  <input type="password" class="form-control" name="password" id="password" placeholder="Create a password" autocomplete="new-password" pattern="(?=^.{8,}$)((?=.*\d)|(?=.*\W+))(?![.\n])(?=.*[A-Z])(?=.*[a-z]).*$" title="Password must contain at least one number and one special character and one uppercase and lowercase letter, and at least 8 or more characters" required="required">
                  <button type="button" class="toggle-password" id="togglePassword" aria-label="Show password">
                    <i class="bi bi-eye"></i>
                  </button>
                </div>
              </div>
              <div class="form-group mt-3">
                <label for="conf_password">Confirm password</label>
                <div class="input-wrap">
                  <i class="bi bi-lock"></i>
                  <input type="password" class="form-control" name="conf_password" id="conf_password" placeholder="Re-enter the password" autocomplete="new-password" pattern="(?=^.{8,}$)((?=.*\d)|(?=.*\W+))(?![.\n])(?=.*[A-Z])(?=.*[a-z]).*$" title="Password must contain at least one number and one special character and one uppercase and lowercase letter, and at least 8 or more characters" required="required">
                  <button type="button" class="toggle-password" id="toggleConfirm" aria-label="Show password">
                    <i class="bi bi-eye"></i>
                  </button>
                </div>
              </div>
              <div class="form-group mt-3">
                <label for="vehicle_no">Vehicle number</label>
                <div class="input-wrap">
                  <i class="bi bi-hash"></i>
                  <input type="text" class="form-control" name="vehicle_no" id="vehicle_no" placeholder="e.g. MH12AB1234" required="required">
                </div>
              </div>
              <div class="form-group mt-3">
                <label for="vehicle_name">Vehicle name</label>
                <div class="input-wrap">
                  <i class="bi bi-tag"></i>
                  <input type="text" class="form-control" name="vehicle_name" id="vehicle_name" placeholder="Make or model" required="required">
                </div>
              </div>
              <div class="form-group mt-3">
                <label for="vehicle_owner_name">Vehicle owner name</label>
                <div class="input-wrap">
                  <i class="bi bi-person-badge"></i>
                  <input type="text" class="form-control" name="vehicle_owner_name" id="vehicle_owner_name" placeholder="Name on the registration" pattern="[A-Za-z ]+" title="Name must be alphabets only" required="required">
                </div>
              </div>

              <div class="text-center mt-4">
                <button type="submit"><i class="bi bi-person-plus"></i> Register</button>
              </div>

              <div class="login-footer text-center">
                <p>Already registered? <a class="register-link" href="UserLogin.jsp">Log in</a></p>
                <a href="index.jsp"><i class="bi bi-arrow-left"></i> Back to home</a>
              </div>
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

  <script>
    // Show / hide password for both password fields
    [['togglePassword', 'password'], ['toggleConfirm', 'conf_password']].forEach(function (pair) {
      var btn = document.getElementById(pair[0]);
      var input = document.getElementById(pair[1]);
      btn.addEventListener('click', function () {
        var hidden = input.type === 'password';
        input.type = hidden ? 'text' : 'password';
        btn.setAttribute('aria-label', hidden ? 'Hide password' : 'Show password');
        btn.querySelector('i').className = hidden ? 'bi bi-eye-slash' : 'bi bi-eye';
      });
    });

    // Confirm password must match
    var pass = document.getElementById('password');
    var conf = document.getElementById('conf_password');
    function checkMatch() {
      conf.setCustomValidity(conf.value && pass.value !== conf.value ? 'Passwords do not match.' : '');
    }
    pass.addEventListener('input', checkMatch);
    conf.addEventListener('input', checkMatch);
  </script>

</body>

</html>
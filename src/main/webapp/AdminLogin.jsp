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
  <link href="assets/css/style.css?v=2" rel="stylesheet">
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
  
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
  </style>
</head>

<body>

  <jsp:include page="nav_index.jsp" />

  <!-- ======= Hero Section ======= -->
  <section id="hero">
    <div class="container">
      <div class="row justify-content-between"></div>
    </div>
    <svg class="hero-waves" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" viewBox="0 24 150 28 " preserveAspectRatio="none">
      <defs>
        <path id="wave-path" d="M-160 44c30 0 58-18 88-18s 58 18 88 18 58-18 88-18 58 18 88 18 v44h-352z">
      </defs>
      <g class="wave1"><use xlink:href="#wave-path" x="50" y="3" fill="rgba(255,255,255, .1)"></g>
      <g class="wave2"><use xlink:href="#wave-path" x="50" y="0" fill="rgba(255,255,255, .2)"></g>
      <g class="wave3"><use xlink:href="#wave-path" x="50" y="9" fill="#fff"></g>
    </svg>
  </section>

  <main id="main">
    <section id="contact" class="contact">
      <div class="container">

        <div class="section-title" data-aos="fade-up">
          <h2>Admin</h2>
          <p>Admin Login</p>
        </div>

        <div class="row justify-content-center">
          <div class="col-lg-5 col-md-8 mt-5 mt-lg-0" data-aos="fade-up" data-aos-delay="200">

            <form action="AdminLoginController" method="post" role="form" class="email-form login-form">
              <div class="form-header text-center">
                <div class="login-badge"><i class="bi bi-shield-lock"></i></div>
                <h3>Welcome back</h3>
                <p>Sign in to manage slots, bookings and queries.</p>
              </div>

              <% if (request.getAttribute("error") != null) { %>
                <div class="alert alert-danger text-center" role="alert" style="font-size: 14px; border-radius: 8px;">
                  <%= request.getAttribute("error") %>
                </div>
              <% } %>

              <div class="form-group">
                <label for="email">Email</label>
                <div class="input-wrap">
                  <i class="bi bi-envelope"></i>
                  <input type="email" class="form-control" name="email" id="email" placeholder="admin@example.com" autocomplete="username" required>
                </div>
              </div>

              <div class="form-group mt-3">
                <label for="password">Password</label>
                <div class="input-wrap">
                  <i class="bi bi-lock"></i>
                  <input type="password" class="form-control" name="password" id="password" placeholder="Enter your password" autocomplete="current-password" required>
                  <button type="button" class="toggle-password" id="togglePassword" aria-label="Show password">
                    <i class="bi bi-eye"></i>
                  </button>
                </div>
              </div>

              <div class="text-center mt-4">
                <button type="submit"><i class="bi bi-box-arrow-in-right"></i> Login</button>
              </div>

              <div class="login-footer text-center">
                <a href="index.jsp"><i class="bi bi-arrow-left"></i> Back to home</a>
              </div>
            </form>

          </div>
        </div>

      </div>
    </section>
  </main>

  <!-- ======= Footer ======= -->
  <footer id="footer">
    <div class="container">
      <div class="copyright">
        &copy; Copyright <strong><span>Plugy 2021</span></strong>. All Rights Reserved
      </div>
    </div>
  </footer>

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
    (function () {
      var btn = document.getElementById('togglePassword');
      var input = document.getElementById('password');
      if (!btn || !input) return;
      btn.addEventListener('click', function () {
        var show = input.type === 'password';
        input.type = show ? 'text' : 'password';
        btn.setAttribute('aria-label', show ? 'Hide password' : 'Show password');
        btn.querySelector('i').className = show ? 'bi bi-eye-slash' : 'bi bi-eye';
      });
    })();
  </script>

</body>
</html>
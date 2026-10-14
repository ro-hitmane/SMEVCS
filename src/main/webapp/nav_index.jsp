<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<%

// Get the exact name of the current page to dynamically highlight the active tab

String uri = request.getRequestURI();

String pageName = uri.substring(uri.lastIndexOf("/") + 1);

%>

<!-- ======= Header ======= -->

<header id="header" class="fixed-top d-flex align-items-center header-transparent">

<div class="container d-flex align-items-center justify-content-between">

<div class="logo">

<h1><a href="index.jsp"><span>&#9889; Plugy</span></a></h1>

</div>

<nav id="navbar" class="navbar">

<ul>

<li><a class="nav-link scrollto active" href="index.jsp">Home</a></li>

<li><a class="nav-link scrollto" href="#about">About</a></li>

<li><a class="nav-link scrollto" href="#contact">Contact Us</a></li>

<li class="dropdown login-dropdown">

<a class="nav-link login-btn" href="#" aria-haspopup="true"><span>Login</span> <i class="bi bi-chevron-down"></i></a>

<ul>

<li><a href="AdminLogin.jsp"><i class="bi bi-shield-lock"></i>&nbsp; Admin</a></li>

<li><a href="UserLogin.jsp"><i class="bi bi-person"></i>&nbsp; User</a></li>

</ul>

</li>

</ul>

<i class="bi bi-list mobile-nav-toggle"></i>

</nav>
</div>
  </header>
  
  <jsp:include page="chatbot_widget.jsp" />
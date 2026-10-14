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
      <h1><a href="UserHome.jsp"><span>⚡ Plugy</span></a></h1>
    </div>
    <nav id="navbar" class="navbar">
      <ul>
        <li><a class="nav-link scrollto <%= "AdminHome.jsp".equals(pageName) ? "active" : "" %>" href="AdminHome.jsp">Dashboard</a></li>
        <li><a class="nav-link scrollto <%= ("Stations.jsp".equals(pageName) || "ManageStation.jsp".equals(pageName) || "ViewSlots.jsp".equals(pageName) || "StationBookings.jsp".equals(pageName)) ? "active" : "" %>" href="Stations.jsp">Stations</a></li>
        <li><a class="nav-link scrollto" href="LogoutController">Logout</a></li>
      </ul>
      <i class="bi bi-list mobile-nav-toggle"></i>
    </nav>
  </div>
</header>
<!-- End Header -->
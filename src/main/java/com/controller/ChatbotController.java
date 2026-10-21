package com.controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.dao.ChatbotDao;
import com.dao.ChatbotDao.ActionLink;
import com.dao.ChatbotDao.BotResult;
import com.dao.ChatbotDao.MenuOption;

@WebServlet("/ChatbotController")
public class ChatbotController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final ChatbotDao dao = new ChatbotDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");

        String currentPage = request.getParameter("currentPage");
        if (currentPage == null || currentPage.trim().isEmpty()) {
            currentPage = "index.jsp";
        }

        String pageStationId = request.getParameter("pageStationId");
        HttpSession session = request.getSession(false);
        String role = detectRole(session, currentPage);
        String userEmail = getUserEmail(session);

        String rawNodeId = request.getParameter("nodeId");
        String rawQuery = request.getParameter("message");

        BotResult result;
        if (rawQuery != null && !rawQuery.trim().isEmpty()) {
            result = handleQuickSearchInput(rawQuery.trim(), role, userEmail, currentPage);
        } else {
            if (rawNodeId == null || rawNodeId.trim().isEmpty() || "init".equalsIgnoreCase(rawNodeId.trim())) {
                rawNodeId = "ROOT";
            }
            result = routeNode(rawNodeId.trim(), role, userEmail, currentPage, pageStationId);
        }

        response.getWriter().write(toJson(result, role));
    }

    // =========================================================================
    // MASTER ROUTER: HANDLES DYNAMIC DB NODES + STATIC DECISION-TREE NODES
    // =========================================================================

    private BotResult routeNode(String rawNodeId, String role, String userEmail, String currentPage, String pageStationId) {
        boolean isAdmin = "ADMIN".equals(role);
        boolean isUser = "USER".equals(role);

        // 1. Dynamic Area Drill-Down
        if (rawNodeId.regionMatches(true, 0, "AREA:", 0, 5)) {
            String areaName = rawNodeId.substring(5).trim();
            BotResult res = new BotResult();
            res.nodeId = "AREA:" + areaName;
            dao.populateStationsInArea(res, areaName, isAdmin, isUser);
            return attachGlobalMenu(res);
        }

        // 2. Dynamic Station Bay Breakdown
        if (rawNodeId.regionMatches(true, 0, "STATION:", 0, 8)) {
            int stId = parseStationId(rawNodeId.substring(8).trim());
            BotResult res = new BotResult();
            res.nodeId = "STATION:" + stId;
            if (stId > 0) {
                dao.populateSingleStationBayBreakdown(res, stId, isAdmin, isUser);
            } else {
                res.title = "⚠️ Error";
                res.reply = "Invalid station selected.";
            }
            return attachGlobalMenu(res);
        }

        // 3. Static Decision-Tree Nodes
        String nodeId = rawNodeId.toUpperCase();
        BotResult res = new BotResult();
        res.nodeId = nodeId;

        switch (nodeId) {

            case "ROOT":
            default:
                res.nodeId = "ROOT";
                
                int currentStId = parseStationId(pageStationId);
                if (currentStId > 0) {
                    res.options.add(new MenuOption("STATION:" + currentStId, "🎯", "Inspect Station #" + currentStId + " Bays"));
                }

                if (isAdmin) {
                    res.title = "🛡️ Admin Assistant";
                    res.reply = "Select an operation to audit live data or manage stations:";
                    res.options.add(new MenuOption("CAT_ADMIN", "📊", "Live Telemetry & Audits"));
                    res.options.add(new MenuOption("CAT_LIVE_SLOTS", "⚡", "Browse Stations & Slots"));
                    res.options.add(new MenuOption("CAT_ADMIN_GUIDE", "🏢", "How to Manage Stations"));
                } else {
                    res.title = "👋 Welcome to Plugy";
                    res.reply = "How can I help you today? Choose an option below:";
                    res.options.add(new MenuOption("CAT_BEGINNER", "🌱", "New to EV Charging?"));
                    res.options.add(new MenuOption("CAT_LIVE_SLOTS", "⚡", "Find Live Charging Stations"));
                    res.options.add(new MenuOption("CAT_BOOKING_HELP", "📅", "How to Book & Pricing Info"));
                    res.options.add(new MenuOption("CAT_ACCOUNT", "🧾", "My Bookings & Account Help"));
                    res.options.add(new MenuOption("CAT_TROUBLE", "🛠️", "Troubleshooting & Support"));
                }
                break;

            // --- BRANCH 1: BEGINNER'S GUIDE ---
            case "CAT_BEGINNER":
                res.title = "🌱 EV Charging Basics";
                res.reply = "Select a quick guide below:";
                res.options.add(new MenuOption("BEG_HOW_IT_WORKS", "🔋", "How does public charging work?"));
                res.options.add(new MenuOption("BEG_PLUG_TYPES", "🔌", "Which plug fits my vehicle?"));
                res.options.add(new MenuOption("BEG_SPEED_COMPARE", "⏱️", "Normal vs. Fast Charging time?"));
                res.options.add(new MenuOption("BEG_RAIN_SAFETY", "🌧️", "Is it safe to charge in rain?"));
                break;

            case "BEG_HOW_IT_WORKS":
                res.title = "🔋 How It Works";
                res.reply = "Charging takes 3 simple steps:";
                res.bullets.add("1. Book your time slot online.");
                res.bullets.add("2. Park in your assigned bay at the station.");
                res.bullets.add("3. Plug the cable securely into your vehicle.");
                break;

            case "BEG_PLUG_TYPES":
                res.title = "🔌 Plug Types Explained";
                res.reply = "We support 3 standard plugs:";
                res.bullets.add("🔵 CCS2: For modern electric cars (Tata, MG, Mahindra).");
                res.bullets.add("🟢 GB/T: For electric scooters, bikes, and fleet cars.");
                res.bullets.add("🟣 CHAdeMO: For select global/imported EVs.");
                res.options.add(new MenuOption("CAT_PLUG_FILTER", "🔍", "Search stations by plug type"));
                break;

            case "BEG_SPEED_COMPARE":
                res.title = "⏱️ Charging Speeds";
                res.reply = "Pick the speed that fits your schedule:";
                res.bullets.add("⚡ Fast DC: 20% to 80% in 30-45 minutes. Great for quick top-ups.");
                res.bullets.add("🚗 Normal AC: Takes 3-5 hours. Ideal while working or shopping.");
                res.bullets.add("🛵 2-Wheeler: 45-75 minutes for a solid charge.");
                break;

            case "BEG_RAIN_SAFETY":
                res.title = "🌧️ Safe in the Rain?";
                res.reply = "Yes! Public chargers are 100% waterproof.";
                res.bullets.add("Power only flows AFTER the plug is locked safely inside your car.");
                break;

            // --- BRANCH 2: LIVE STATIONS ---
            case "CAT_LIVE_SLOTS":
                res.title = "⚡ Live Stations";
                res.reply = "Find available charging bays right now:";
                res.options.add(new MenuOption("LIVE_ALL_AREAS", "📍", "Browse by Area (Pune/PCMC)"));
                res.options.add(new MenuOption("LIVE_2W", "🛵", "Find 2-Wheeler Slots"));
                res.options.add(new MenuOption("LIVE_4W_FAST", "⚡", "Find 4-Wheeler Fast Chargers"));
                res.options.add(new MenuOption("LIVE_4W_NORMAL", "🚗", "Find 4-Wheeler Normal Chargers"));
                res.options.add(new MenuOption("CAT_PLUG_FILTER", "🔌", "Filter by Plug Type"));
                break;

            case "LIVE_ALL_AREAS":
                dao.populateInteractiveAreaDirectory(res, isAdmin, isUser);
                break;

            case "CAT_PLUG_FILTER":
                res.title = "🔌 Filter by Plug";
                res.reply = "Select your connector type:";
                res.options.add(new MenuOption("LIVE_PLUG_CCS2", "🔵", "CCS2 (Modern Cars)"));
                res.options.add(new MenuOption("LIVE_PLUG_GBT", "🟢", "GB/T (2-Wheelers & Fleet)"));
                res.options.add(new MenuOption("LIVE_PLUG_CHADEMO", "🟣", "CHAdeMO (Global EVs)"));
                break;

            case "LIVE_2W":
                dao.populateFilteredStations(res, null, "2-Wheeler", null, isAdmin, isUser);
                break;

            case "LIVE_4W_FAST":
                dao.populateFilteredStations(res, null, "4-Wheeler-Fast", null, isAdmin, isUser);
                break;

            case "LIVE_4W_NORMAL":
                dao.populateFilteredStations(res, null, "4-Wheeler-Normal", null, isAdmin, isUser);
                break;

            case "LIVE_PLUG_CCS2":
                dao.populateFilteredStations(res, "CCS2", null, null, isAdmin, isUser);
                break;

            case "LIVE_PLUG_GBT":
                dao.populateFilteredStations(res, "GB/T", null, null, isAdmin, isUser);
                break;

            case "LIVE_PLUG_CHADEMO":
                dao.populateFilteredStations(res, "CHAdeMO", null, null, isAdmin, isUser);
                break;

            // --- BRANCH 3: BOOKING HELP ---
            case "CAT_BOOKING_HELP":
                res.title = "📅 Booking & Pricing";
                res.reply = "Get help with reservations and costs:";
                res.options.add(new MenuOption("BOOK_PREVIEW_WHY", "🔒", "Why can't I click 'Book Slot'?"));
                res.options.add(new MenuOption("BOOK_STEPS", "✅", "How do I book a slot?"));
                res.options.add(new MenuOption("BOOK_PRICING", "💰", "How is the fee calculated?"));
                res.options.add(new MenuOption("BOOK_BADGES", "🚦", "What do the status colors mean?"));
                break;

            case "BOOK_PREVIEW_WHY":
                res.title = "🔒 Preview Mode";
                res.reply = "If you don't see a 'Book' button, you are in Preview Mode.";
                res.bullets.add("You must pick a Date and Time window on the Search page first.");
                res.bullets.add("This prevents two drivers from booking the same slot at the same time.");
                res.actions.add(new ActionLink("📅 Pick Date & Time", "Search.jsp", "emerald"));
                break;

            case "BOOK_STEPS":
                res.title = "✅ Booking Steps";
                res.reply = "Follow these quick steps:";
                res.bullets.add("1. Open Search, pick your station, and enter your Time Window.");
                res.bullets.add("2. Click 'View Slots' and find a green 'Available' bay.");
                res.bullets.add("3. Click 'Book', enter your vehicle details, and pay securely.");
                break;

            case "BOOK_PRICING":
                res.title = "💰 Pricing Explained";
                res.reply = "Your charging fee is calculated upfront based on:";
                res.bullets.add("• Vehicle Type (2W vs 4W).");
                res.bullets.add("• Charger Speed (Normal vs Fast DC).");
                res.bullets.add("• Booking Duration (Start to End Time).");
                break;

            case "BOOK_BADGES":
                res.title = "🚦 Status Colors";
                res.reply = "Here is what the badges mean:";
                res.bullets.add("🟢 Available: Ready to book.");
                res.bullets.add("🟠 Reserved: Someone else booked this time. Pick another slot.");
                res.bullets.add("🔴 Offline: Charger is currently disabled.");
                break;

            // --- BRANCH 4: ACCOUNT & BOOKINGS ---
            case "CAT_ACCOUNT":
                res.title = "🧾 Account Help";
                res.reply = "Manage your bookings or get login help:";
                res.options.add(new MenuOption("ACC_MY_BOOKINGS", "🧾", "Check My Live Bookings"));
                res.options.add(new MenuOption("ACC_HOW_LOGIN", "🔑", "Where do I Log In or Register?"));
                res.options.add(new MenuOption("ACC_PASS_RULES", "🛡️", "Password format rules"));
                break;

            case "ACC_MY_BOOKINGS":
                dao.populateUserBookings(res, userEmail);
                break;

            case "ACC_HOW_LOGIN":
                res.title = "🔑 Login Portals";
                res.reply = "Select your portal below to sign in or register:";
                res.actions.add(new ActionLink("🔑 User Login", "UserLogin.jsp", "emerald"));
                res.actions.add(new ActionLink("📝 User Registration", "UserRegister.jsp", "primary"));
                res.actions.add(new ActionLink("🛡️ Admin Login", "AdminLogin.jsp", "outline"));
                break;

            case "ACC_PASS_RULES":
                res.title = "🛡️ Password Rules";
                res.reply = "Ensure your password has:";
                res.bullets.add("• Minimum 8 characters.");
                res.bullets.add("• At least 1 Uppercase & 1 Lowercase letter.");
                res.bullets.add("• At least 1 Number or Symbol (@, #, $).");
                break;

            // --- BRANCH 5: TROUBLESHOOTING ---
            case "CAT_TROUBLE":
                res.title = "🛠️ Troubleshooting";
                res.reply = "Facing an issue? Select an option:";
                res.options.add(new MenuOption("TRB_NOT_STARTING", "🔌", "Charging won't start"));
                res.options.add(new MenuOption("TRB_CABLE_LOCKED", "🔓", "Cannot unplug the cable"));
                res.options.add(new MenuOption("TRB_CONTACT", "📞", "Contact Support Desk"));
                break;

            case "TRB_NOT_STARTING":
                res.title = "🔌 Charging Won't Start";
                res.reply = "Check these quickly:";
                res.bullets.add("1. Turn OFF your vehicle completely. EVs won't charge in 'Drive' mode.");
                res.bullets.add("2. Push the heavy cable firmly until it locks with a click.");
                res.bullets.add("3. Ensure the Red Emergency Stop button on the machine isn't pushed in.");
                break;

            case "TRB_CABLE_LOCKED":
                res.title = "🔓 Cable Stuck";
                res.reply = "Never pull by force! Do this instead:";
                res.bullets.add("Press 'Unlock' on your car key fob twice to release the safety lock.");
                break;

            case "TRB_CONTACT":
                res.title = "📞 Support Desk";
                res.reply = "Reach out for instant help:";
                res.bullets.add("☎️ Helpline: +91 20 2550 0000");
                res.bullets.add("📧 Email: support@plugy.in");
                res.actions.add(new ActionLink("✉️ Online Contact Form", "index.jsp#contact", "emerald"));
                break;

            // --- BRANCH 6: ADMIN COMMANDS ---
            case "CAT_ADMIN":
                res.title = "📊 Admin Operations";
                res.reply = "Select a live report or workflow guide:";
                res.options.add(new MenuOption("ADMIN_TELEMETRY", "💰", "Live Revenue & Active Sessions"));
                res.options.add(new MenuOption("ADMIN_HARDWARE", "🛠️", "Audit Offline & Broken Bays"));
                res.options.add(new MenuOption("CAT_ADMIN_GUIDE", "⚙️", "Guide: Manage Stations & Slots"));
                break;

            case "ADMIN_TELEMETRY":
                dao.populateAdminTelemetry(res);
                break;

            case "ADMIN_HARDWARE":
                dao.populateAdminHardwareAudit(res);
                break;

            case "CAT_ADMIN_GUIDE":
                res.title = "⚙️ Admin Guide";
                res.reply = "How-to instructions for platform management:";
                res.options.add(new MenuOption("ADMIN_HOW_MANAGE", "➕", "How to Add a Station or Bay"));
                res.options.add(new MenuOption("ADMIN_HOW_STATUS", "🔄", "How to toggle Offline Status"));
                break;

            case "ADMIN_HOW_MANAGE":
                res.title = "➕ Add Stations & Bays";
                res.reply = "Follow these steps:";
                res.bullets.add("1. Create the station via the 'Stations' tab.");
                res.bullets.add("2. Click 'Manage' on that station row.");
                res.bullets.add("3. Add individual bays (e.g., A1, B1) and set them to 'Available'.");
                res.actions.add(new ActionLink("🏢 Open Stations Manager", "Stations.jsp", "emerald"));
                break;

            case "ADMIN_HOW_STATUS":
                res.title = "🔄 Toggle Status";
                res.reply = "To block a broken charger from being booked:";
                res.bullets.add("Go to Manage Station -> edit the bay status to 'Offline' or 'In maintenance'.");
                res.actions.add(new ActionLink("🏢 Open Stations Manager", "Stations.jsp", "primary"));
                break;
        }

        return attachGlobalMenu(res);
    }

    // =========================================================================
    // QUICK-SEARCH ROUTER
    // =========================================================================

    private BotResult handleQuickSearchInput(String query, String role, String userEmail, String currentPage) {
        String lower = query.toLowerCase();
        boolean isAdmin = "ADMIN".equals(role);
        boolean isUser = "USER".equals(role);

        if (lower.contains("preview") || lower.contains("no book") || lower.contains("cant book")) {
            return attachGlobalMenu(routeNode("BOOK_PREVIEW_WHY", role, userEmail, currentPage, null));
        }
        if (lower.contains("how to book") || lower.equals("book")) {
            return attachGlobalMenu(routeNode("BOOK_STEPS", role, userEmail, currentPage, null));
        }
        if (lower.contains("price") || lower.contains("cost") || lower.contains("tariff")) {
            return attachGlobalMenu(routeNode("BOOK_PRICING", role, userEmail, currentPage, null));
        }
        if (lower.contains("tata") || lower.contains("mg") || lower.contains("ola") || lower.contains("plug") || lower.contains("ccs")) {
            return attachGlobalMenu(routeNode("BEG_PLUG_TYPES", role, userEmail, currentPage, null));
        }
        if (lower.contains("rain") || lower.contains("shock") || lower.contains("safe")) {
            return attachGlobalMenu(routeNode("BEG_RAIN_SAFETY", role, userEmail, currentPage, null));
        }
        if (lower.contains("stuck") || lower.contains("unplug") || lower.contains("locked")) {
            return attachGlobalMenu(routeNode("TRB_CABLE_LOCKED", role, userEmail, currentPage, null));
        }
        if (lower.contains("not starting") || lower.contains("wont start")) {
            return attachGlobalMenu(routeNode("TRB_NOT_STARTING", role, userEmail, currentPage, null));
        }
        if (lower.contains("my booking") || lower.contains("receipt")) {
            return attachGlobalMenu(routeNode("ACC_MY_BOOKINGS", role, userEmail, currentPage, null));
        }
        if (lower.contains("password") || lower.contains("invalid")) {
            return attachGlobalMenu(routeNode("ACC_PASS_RULES", role, userEmail, currentPage, null));
        }
        if (lower.contains("login") || lower.contains("register")) {
            return attachGlobalMenu(routeNode("ACC_HOW_LOGIN", role, userEmail, currentPage, null));
        }
        if (lower.contains("contact") || lower.contains("phone") || lower.contains("support")) {
            return attachGlobalMenu(routeNode("TRB_CONTACT", role, userEmail, currentPage, null));
        }

        if (isAdmin && (lower.contains("revenue") || lower.contains("telemetry"))) {
            return attachGlobalMenu(routeNode("ADMIN_TELEMETRY", role, userEmail, currentPage, null));
        }
        if (isAdmin && (lower.contains("offline") || lower.contains("fault"))) {
            return attachGlobalMenu(routeNode("ADMIN_HARDWARE", role, userEmail, currentPage, null));
        }

        BotResult searchRes = new BotResult();
        searchRes.nodeId = "SEARCH_RESULT";
        searchRes.title = "🔍 Search Results";
        
        String chargerFilter = null;
        if (lower.contains("ccs")) chargerFilter = "CCS2";
        else if (lower.contains("chademo")) chargerFilter = "CHAdeMO";
        else if (lower.contains("gb")) chargerFilter = "GB/T";

        String vehicleFilter = null;
        if (lower.contains("2-wheeler") || lower.contains("bike") || lower.contains("scooter")) vehicleFilter = "2-Wheeler";
        else if (lower.contains("fast")) vehicleFilter = "4-Wheeler-Fast";
        else if (lower.contains("normal")) vehicleFilter = "4-Wheeler-Normal";

        dao.populateFilteredStations(searchRes, chargerFilter, vehicleFilter, query, isAdmin, isUser);
        
        return attachGlobalMenu(searchRes);
    }

    // =========================================================================
    // HELPERS & JSON
    // =========================================================================

    private BotResult attachGlobalMenu(BotResult res) {
        if (!"ROOT".equals(res.nodeId)) {
            res.options.add(new MenuOption("ROOT", "🏠", "Return to Main Menu"));
        }
        return res;
    }

    private int parseStationId(String val) {
        if (val == null) return -1;
        String trimmed = val.trim();
        if (trimmed.matches("\\d+")) {
            try { return Integer.parseInt(trimmed); } catch (Exception ignored) {}
        }
        return -1;
    }

    private String detectRole(HttpSession session, String currentPage) {
        // Role comes only from the server-side session, never from the client-supplied page name.
        if (session != null && session.getAttribute("admin") != null) return "ADMIN";
        if (session != null && (session.getAttribute("email") != null || session.getAttribute("user") != null)) return "USER";
        return "GUEST";
    }

    private String getUserEmail(HttpSession session) {
        if (session == null) return null;
        Object em = session.getAttribute("email");
        if (em == null) em = session.getAttribute("user");
        return em != null ? em.toString() : null;
    }

    private static String escJson(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\': sb.append("\\\\"); break;
                case '"':  sb.append("\\\""); break;
                case '\n': sb.append("\\n"); break;
                case '\r': break;
                case '\t': sb.append(" "); break;
                default:   sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String toJson(BotResult res, String role) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"role\":\"").append(escJson(role)).append("\",");
        sb.append("\"nodeId\":\"").append(escJson(res.nodeId)).append("\",");
        sb.append("\"title\":\"").append(escJson(res.title)).append("\",");
        sb.append("\"reply\":\"").append(escJson(res.reply)).append("\",");

        sb.append("\"bullets\":[");
        for (int i = 0; i < res.bullets.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(escJson(res.bullets.get(i))).append("\"");
        }
        sb.append("],");

        sb.append("\"options\":[");
        for (int i = 0; i < res.options.size(); i++) {
            MenuOption o = res.options.get(i);
            if (i > 0) sb.append(",");
            sb.append("{\"nodeId\":\"").append(escJson(o.nodeId))
              .append("\",\"icon\":\"").append(escJson(o.icon))
              .append("\",\"label\":\"").append(escJson(o.label))
              .append("\",\"badge\":\"").append(escJson(o.badge)).append("\"}");
        }
        sb.append("],");

        sb.append("\"actions\":[");
        for (int i = 0; i < res.actions.size(); i++) {
            ActionLink a = res.actions.get(i);
            if (i > 0) sb.append(",");
            sb.append("{\"label\":\"").append(escJson(a.label))
              .append("\",\"url\":\"").append(escJson(a.url))
              .append("\",\"style\":\"").append(escJson(a.style)).append("\"}");
        }
        sb.append("]}");
        return sb.toString();
    }
}
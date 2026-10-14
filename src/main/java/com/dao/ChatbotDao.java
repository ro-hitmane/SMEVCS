package com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.connection.DBConnection;

public class ChatbotDao {

    // =========================================================================
    // DATA STRUCTURES FOR GUIDED DECISION-TREE & ACTIONABLE UI
    // =========================================================================

    public static class ActionLink {
        public String label;
        public String url;
        public String style; // "primary", "emerald", "outline"

        public ActionLink(String label, String url) {
            this(label, url, "primary");
        }

        public ActionLink(String label, String url, String style) {
            this.label = label;
            this.url = url;
            this.style = style;
        }
    }

    public static class MenuOption {
        public String nodeId;
        public String icon;
        public String label;
        public String badge; // Optional right-side pill e.g. "4 Free" or "3 Hubs"

        public MenuOption(String nodeId, String icon, String label) {
            this(nodeId, icon, label, "");
        }

        public MenuOption(String nodeId, String icon, String label, String badge) {
            this.nodeId = nodeId;
            this.icon = icon;
            this.label = label;
            this.badge = badge != null ? badge : "";
        }
    }

    public static class BotResult {
        public String nodeId = "ROOT";
        public String parentId = "";
        public String breadcrumb = "Main Menu";
        public String title = "";
        public String reply = "";
        public List<String> bullets = new ArrayList<>();
        public String simpleNorm = "";
        public String normHi = "";
        public String normMr = "";
        public List<MenuOption> options = new ArrayList<>();
        public List<ActionLink> actions = new ArrayList<>();
    }

    // Reusable SQL snippet matching ViewSlots.jsp status logic 100%:
    // A slot is truly available right now if it is NOT Offline, NOT in Maintenance,
    // and has 0 overlapping active records in tbl_payment right now.
    private static final String SQL_IS_SLOT_AVAILABLE_NOW =
        "(sl.id IS NOT NULL "
        + "AND LOWER(COALESCE(TRIM(sl.status), 'available')) NOT LIKE '%offline%' "
        + "AND LOWER(COALESCE(TRIM(sl.status), 'available')) NOT LIKE '%maint%' "
        + "AND NOT EXISTS ("
        + "  SELECT 1 FROM tbl_payment p "
        + "  WHERE p.slot_id = sl.id AND p.start_time <= NOW() AND p.end_time > NOW()"
        + "))";

    private static final Set<String> STOPWORDS = new HashSet<>(Arrays.asList(
        "do", "does", "did", "you", "your", "we", "our", "have", "has", "had", "can", "could",
        "would", "will", "shall", "should", "may", "might", "please", "pls", "tell", "give",
        "get", "got", "want", "need", "looking", "look", "find", "search", "show", "list",
        "me", "us", "my", "i", "a", "an", "the", "some", "any", "all", "slot", "slots",
        "station", "stations", "hub", "hubs", "center", "centre", "in", "at", "on", "to",
        "from", "near", "nearby", "around", "by", "with", "of", "for", "available",
        "availability", "free", "open", "vacant", "empty", "charger", "chargers", "charging",
        "point", "points", "port", "ports", "bay", "bays", "book", "booking", "bookings",
        "reserve", "check", "status", "ccs2", "ccs", "chademo", "gbt", "gb", "t", "wheeler",
        "fast", "normal", "slow", "car", "cars", "bike", "bikes", "scooter", "two", "four",
        "2w", "4w", "ev", "electric", "vehicle", "vehicles", "is", "are", "was", "were",
        "there", "where", "what", "which", "who", "how", "many", "much", "manage", "view",
        "plugy", "smevcs", "network", "right", "now", "today", "current", "currently", "live"
    ));

    // =========================================================================
    // 1. DYNAMIC AREA DIRECTORY (Turns DB Areas into Clickable Menu Buttons)
    // =========================================================================

    public void populateInteractiveAreaDirectory(BotResult res, boolean isAdmin, boolean isUser) {
        int totalStations = 0;
        int totalSlots = 0;
        int freeNow = 0;

        String summarySql = "SELECT "
                + "(SELECT COUNT(*) FROM tbl_station) AS total_stations, "
                + "(SELECT COUNT(*) FROM tbl_slot) AS total_slots, "
                + "(SELECT COUNT(*) FROM tbl_slot sl WHERE " + SQL_IS_SLOT_AVAILABLE_NOW + ") AS free_now";

        String areaSql = "SELECT st.area, st.city, "
                + "COUNT(DISTINCT st.id) AS station_cnt, "
                + "COUNT(sl.id) AS slot_cnt, "
                + "COALESCE(SUM(CASE WHEN " + SQL_IS_SLOT_AVAILABLE_NOW + " THEN 1 ELSE 0 END), 0) AS free_cnt "
                + "FROM tbl_station st "
                + "LEFT JOIN tbl_slot sl ON st.id = sl.station_id "
                + "WHERE st.area IS NOT NULL AND TRIM(st.area) <> '' "
                + "GROUP BY st.area, st.city "
                + "ORDER BY free_cnt DESC, st.area ASC LIMIT 14";

        Connection con = DBConnection.getConnection();
        if (con == null) {
            res.title = "⚠️ Database Connection Unavailable";
            res.reply = "Could not connect to MySQL right now. Please verify that MySQL is running.";
            return;
        }

        try {
            try (PreparedStatement ps = con.prepareStatement(summarySql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    totalStations = rs.getInt("total_stations");
                    totalSlots = rs.getInt("total_slots");
                    freeNow = rs.getInt("free_now");
                }
            }

            try (PreparedStatement ps = con.prepareStatement(areaSql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String area = rs.getString("area").trim();
                    String city = rs.getString("city");
                    int stCnt = rs.getInt("station_cnt");
                    int freeCnt = rs.getInt("free_cnt");
                    int slCnt = rs.getInt("slot_cnt");

                    String label = area + (city != null && !city.trim().isEmpty() ? " (" + city.trim() + ")" : "");
                    String badge = freeCnt + "/" + slCnt + " Free (" + stCnt + (stCnt == 1 ? " Hub)" : " Hubs)");

                    // Clicking this option drills down into AREA:<areaName>
                    res.options.add(new MenuOption("AREA:" + area, "📍", label, badge));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { con.close(); } catch (Exception ignored) {}
        }

        res.title = "📍 Select an Area in Pune / PCMC";
        res.reply = "Plugy currently operates " + totalStations + " charging stations with "
                + totalSlots + " charging bays (" + freeNow + " bays are free right now). Tap any neighborhood below to see its stations:";

        if (res.options.isEmpty()) {
            res.bullets.add("No charging stations have been added to the database yet.");
        }

        res.simpleNorm = "💡 Everyday Tip: Tap your area below to see the exact stations there, or click 'Search & Book by Time' to reserve a bay directly.";
        res.normHi = "💡 सरल जानकारी: हमारे " + totalStations + " स्टेशनों में से अभी " + freeNow + " चार्जिंग बे खाली हैं। अपने इलाके पर क्लिक करें।";
        res.normMr = "💡 सोपी माहिती: आमच्या " + totalStations + " स्टेशन्सपैकी सध्या " + freeNow + " चार्जिंग स्लॉट्स रिकामे आहेत. तुमच्या परिसरावर क्लिक करा.";

        appendRoleSearchActions(res, isAdmin, isUser);
    }

    // =========================================================================
    // 2. DRILL-DOWN: ALL STATIONS IN A SPECIFIC AREA (e.g. AREA:Wakad)
    // =========================================================================

    public void populateStationsInArea(BotResult res, String areaName, boolean isAdmin, boolean isUser) {
        String cleanArea = areaName != null ? areaName.trim() : "";

        String sql = "SELECT st.id, st.station_name, st.area, st.city, st.address, st.contact, "
                + "COUNT(sl.id) AS total_slots, "
                + "COALESCE(SUM(CASE WHEN " + SQL_IS_SLOT_AVAILABLE_NOW + " THEN 1 ELSE 0 END), 0) AS free_slots "
                + "FROM tbl_station st "
                + "LEFT JOIN tbl_slot sl ON st.id = sl.station_id "
                + "WHERE LOWER(TRIM(st.area)) LIKE ? OR LOWER(TRIM(st.city)) LIKE ? "
                + "GROUP BY st.id, st.station_name, st.area, st.city, st.address, st.contact "
                + "ORDER BY free_slots DESC, st.station_name ASC";

        Connection con = DBConnection.getConnection();
        if (con == null) {
            res.reply = "Database connection is temporarily unavailable.";
            return;
        }

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            String like = "%" + cleanArea.toLowerCase() + "%";
            ps.setString(1, like);
            ps.setString(2, like);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int stId = rs.getInt("id");
                    String stName = rs.getString("station_name");
                    String address = rs.getString("address");
                    int total = rs.getInt("total_slots");
                    int free = rs.getInt("free_slots");

                    String statusBadge = free > 0 ? ("🟢 " + free + "/" + total + " Free") : ("🟠 0/" + total + " Free");
                    String addrLine = (address != null && !address.trim().isEmpty()) ? " — " + address.trim() : "";

                    res.bullets.add(stName + addrLine + " [" + statusBadge + "]");

                    // Turn each station into a clickable drill-down node to inspect individual bays (A1, A2, etc.)
                    res.options.add(new MenuOption(
                        "STATION:" + stId,
                        "⚡",
                        "Inspect Bays at " + stName,
                        free + "/" + total + " Free"
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { con.close(); } catch (Exception ignored) {}
        }

        res.title = "🏢 Charging Stations in " + cleanArea;
        if (res.bullets.isEmpty()) {
            res.reply = "We do not have an active station in '" + cleanArea + "' yet.";
            res.bullets.add("Tap 'Browse All Covered Areas' below to see the nearest active hub.");
            res.options.add(new MenuOption("LIVE_ALL_AREAS", "📍", "Browse All Covered Areas in Pune & PCMC"));
        } else {
            res.reply = "Found " + res.bullets.size() + " station(s) in " + cleanArea
                    + ". Tap a station below to inspect every individual charging bay and plug type:";
        }

        res.simpleNorm = "💡 Everyday Tip: Tap 'Inspect Bays' on any station below to see which exact plugs (CCS2, CHAdeMO, GB/T) and vehicle types are free right now.";
        res.normHi = "💡 सरल सुझाव: किसी भी स्टेशन के सभी प्लग और खाली स्लॉट देखने के लिए नीचे स्टेशन के नाम पर क्लिक करें।";
        res.normMr = "💡 सोपी टीप: कोणत्याही स्टेशनवरील सर्व प्लग आणि रिकामे स्लॉट पाहण्यासाठी खालील स्टेशनच्या नावावर क्लिक करा.";

        appendRoleSearchActions(res, isAdmin, isUser);
    }

    // =========================================================================
    // 3. DEEP DRILL-DOWN: SINGLE STATION BAY-BY-BAY BREAKDOWN (STATION:<id>)
    // =========================================================================

    public void populateSingleStationBayBreakdown(BotResult res, int stationId, boolean isAdmin, boolean isUser) {
        String stSql = "SELECT id, station_name, area, city, address, contact FROM tbl_station WHERE id = ?";

        // Exact status logic matching ViewSlots.jsp + fetches active booking end_time so user knows when it frees up
        String slotSql = "SELECT sl.id, sl.slot_label, sl.vehicle_type, sl.charger_type, sl.status, "
                + "(SELECT COUNT(*) FROM tbl_payment p WHERE p.slot_id = sl.id AND p.start_time <= NOW() AND p.end_time > NOW()) AS active_now, "
                + "(SELECT MAX(p.end_time) FROM tbl_payment p WHERE p.slot_id = sl.id AND p.start_time <= NOW() AND p.end_time > NOW()) AS busy_until "
                + "FROM tbl_slot sl WHERE sl.station_id = ? ORDER BY sl.slot_label ASC";

        Connection con = DBConnection.getConnection();
        if (con == null) {
            res.reply = "Database connection is temporarily unavailable.";
            return;
        }

        String stName = "Station #" + stationId;
        String area = "";
        String address = "";
        String contact = "";
        int freeCount = 0;
        int totalCount = 0;
        SimpleDateFormat timeFmt = new SimpleDateFormat("hh:mm a");

        try {
            try (PreparedStatement ps = con.prepareStatement(stSql)) {
                ps.setInt(1, stationId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        stName = rs.getString("station_name");
                        area = rs.getString("area") != null ? rs.getString("area") : "";
                        address = rs.getString("address") != null ? rs.getString("address") : "";
                        contact = rs.getString("contact") != null ? rs.getString("contact") : "";
                    } else {
                        res.title = "⚠️ Station Not Found";
                        res.reply = "This station ID no longer exists in the database.";
                        res.options.add(new MenuOption("LIVE_ALL_AREAS", "📍", "Back to All Stations"));
                        return;
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement(slotSql)) {
                ps.setInt(1, stationId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        totalCount++;
                        String label = rs.getString("slot_label");
                        String vType = rs.getString("vehicle_type");
                        String cType = rs.getString("charger_type");
                        String baseStatus = rs.getString("status");
                        if (baseStatus == null || baseStatus.trim().isEmpty()) {
                            baseStatus = "Available";
                        }
                        int activeNow = rs.getInt("active_now");
                        Timestamp busyUntil = rs.getTimestamp("busy_until");

                        String statusDisplay;
                        if ("Offline".equalsIgnoreCase(baseStatus) || baseStatus.toLowerCase().contains("maint")) {
                            statusDisplay = "🔴 " + baseStatus + " (Not Bookable)";
                        } else if (activeNow > 0) {
                            String untilStr = busyUntil != null ? (" until " + timeFmt.format(busyUntil)) : "";
                            statusDisplay = "🟠 Reserved Now" + untilStr;
                        } else {
                            statusDisplay = "🟢 Available Now";
                            freeCount++;
                        }

                        res.bullets.add("Bay " + label + " | " + vType + " | Plug: " + cType + " ➔ " + statusDisplay);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { con.close(); } catch (Exception ignored) {}
        }

        res.title = "⚡ " + stName + (area.isEmpty() ? "" : " (" + area + ")");
        StringBuilder desc = new StringBuilder();
        if (!address.isEmpty()) desc.append("📍 Address: ").append(address).append("\n");
        if (!contact.isEmpty()) desc.append("📞 Station Contact: ").append(contact).append("\n");
        desc.append("Live Status: ").append(freeCount).append(" of ").append(totalCount).append(" charging bays are free right now:");
        res.reply = desc.toString();

        if (res.bullets.isEmpty()) {
            res.bullets.add("No charging bays have been configured for this station yet.");
        }

        res.simpleNorm = "💡 Everyday Tip: Even if a bay says 'Reserved Now', you can still book it for a later time today! Click 'Select Date & Time to Book' below.";
        res.normHi = "💡 सरल जानकारी: यदि कोई स्लॉट अभी व्यस्त (Reserved) है, तब भी आप उसे आज के किसी बाद के समय के लिए बुक कर सकते हैं।";
        res.normMr = "💡 सोपी माहिती: जरी एखादा स्लॉट आत्ता व्यस्त (Reserved) असेल, तरीही तुम्ही पुढील वेळेसाठी तो बुक करू शकता.";

        if (isAdmin) {
            res.actions.add(new ActionLink("⚙️ Manage " + stName + " Bays", "ManageStation.jsp?stationId=" + stationId, "primary"));
            res.actions.add(new ActionLink("📋 View " + stName + " Bookings", "StationBookings.jsp?stationId=" + stationId, "emerald"));
            res.actions.add(new ActionLink("🔌 View Slots Table", "ViewSlots.jsp?stationId=" + stationId, "outline"));
        } else if (isUser) {
            res.actions.add(new ActionLink("📅 Select Date & Time to Book Here", "Search.jsp", "emerald"));
            res.actions.add(new ActionLink("👀 Preview " + stName + " Slots Page", "SearchSlot.jsp?stationId=" + stationId, "primary"));
        } else {
            res.actions.add(new ActionLink("🔑 Login as User to Book This Station", "UserLogin.jsp", "emerald"));
            res.actions.add(new ActionLink("📝 Create Free User Account", "UserRegister.jsp", "primary"));
        }

        if (!area.isEmpty()) {
            res.options.add(new MenuOption("AREA:" + area, "⬅️", "See Other Stations in " + area));
        }
        res.options.add(new MenuOption("LIVE_ALL_AREAS", "📍", "Browse All Pune & PCMC Areas"));
    }

    // =========================================================================
    // 4. FILTER STATIONS BY VEHICLE TYPE, PLUG TYPE, OR FREE-TEXT SEARCH
    // =========================================================================

    public void populateFilteredStations(BotResult res, String chargerFilter, String vehicleFilter, String rawAreaQuery, boolean isAdmin, boolean isUser) {
        List<String> locationTokens = new ArrayList<>();
        if (rawAreaQuery != null && !rawAreaQuery.trim().isEmpty()) {
            String cleaned = rawAreaQuery.toLowerCase().replaceAll("[^a-z0-9\\s]", " ").trim();
            for (String token : cleaned.split("\\s+")) {
                if (token.length() >= 3 && !STOPWORDS.contains(token)) {
                    locationTokens.add(token);
                }
            }
        }

        StringBuilder sql = new StringBuilder(
            "SELECT st.id, st.station_name, st.area, st.city, "
            + "COUNT(sl.id) AS matching_slots, "
            + "COALESCE(SUM(CASE WHEN " + SQL_IS_SLOT_AVAILABLE_NOW + " THEN 1 ELSE 0 END), 0) AS free_slots "
            + "FROM tbl_station st "
            + "LEFT JOIN tbl_slot sl ON st.id = sl.station_id "
        );

        List<String> params = new ArrayList<>();
        if (chargerFilter != null && !chargerFilter.isEmpty()) {
            sql.append("AND UPPER(TRIM(sl.charger_type)) = ? ");
            params.add(chargerFilter.toUpperCase());
        }
        if (vehicleFilter != null && !vehicleFilter.isEmpty()) {
            sql.append("AND LOWER(TRIM(sl.vehicle_type)) LIKE ? ");
            params.add("%" + vehicleFilter.toLowerCase() + "%");
        }

        sql.append("WHERE 1=1 ");
        if (!locationTokens.isEmpty()) {
            sql.append("AND (");
            for (int i = 0; i < locationTokens.size(); i++) {
                if (i > 0) sql.append(" OR ");
                sql.append("LOWER(st.area) LIKE ? OR LOWER(st.station_name) LIKE ? OR LOWER(st.city) LIKE ?");
                String like = "%" + locationTokens.get(i) + "%";
                params.add(like);
                params.add(like);
                params.add(like);
            }
            sql.append(") ");
        }

        if ((chargerFilter != null && !chargerFilter.isEmpty()) || (vehicleFilter != null && !vehicleFilter.isEmpty())) {
            sql.append("GROUP BY st.id, st.station_name, st.area, st.city HAVING COUNT(sl.id) > 0 ");
        } else {
            sql.append("GROUP BY st.id, st.station_name, st.area, st.city ");
        }
        sql.append("ORDER BY free_slots DESC, st.station_name ASC LIMIT 8");

        Connection con = DBConnection.getConnection();
        if (con == null) {
            res.reply = "Database connection is temporarily unavailable.";
            return;
        }

        try (PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setString(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int stId = rs.getInt("id");
                    String stName = rs.getString("station_name");
                    String area = rs.getString("area");
                    int total = rs.getInt("matching_slots");
                    int free = rs.getInt("free_slots");

                    String statusBadge = free > 0
                            ? ("🟢 " + free + " of " + total + " matching bays free now")
                            : ("🟠 0 of " + total + " free right now");
                    res.bullets.add(stName + " (" + area + ") — " + statusBadge);

                    // Make every matched station clickable so user can inspect its individual bays!
                    res.options.add(new MenuOption(
                        "STATION:" + stId,
                        "⚡",
                        "Inspect " + stName + " (" + area + ")",
                        free + "/" + total + " Free"
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { con.close(); } catch (Exception ignored) {}
        }

        if (!res.bullets.isEmpty()) {
            res.reply = "Here are the live matching stations from our database. Tap any station below to see its individual bays and plug details:";
        } else {
            res.reply = "No stations matched that exact filter right now.";
            res.bullets.add("Tap 'Browse All Covered Areas' below to see every active station across Pune & PCMC.");
            res.options.add(new MenuOption("LIVE_ALL_AREAS", "📍", "Browse All Covered Areas in Pune & PCMC"));
        }

        appendRoleSearchActions(res, isAdmin, isUser);
    }

    // =========================================================================
    // 5. USER PERSONAL BOOKINGS AUDIT (Active, Upcoming, Completed + Time Left)
    // =========================================================================

    public void populateUserBookings(BotResult res, String userEmail) {
        if (userEmail == null || userEmail.trim().isEmpty()) {
            res.title = "🔑 Sign In to View Your Personal Bookings";
            res.reply = "Because booking records contain your vehicle number and payment history, please sign in first:";
            res.bullets.add("If you already logged in, click 'Open My Bookings Page' below to view your reservation table.");
            res.bullets.add("If you haven't signed in yet, click 'User Login' below.");
            res.simpleNorm = "💡 Everyday Tip: All your past, current, and future charging sessions are saved automatically under your email address.";
            res.normHi = "💡 सरल जानकारी: अपनी बुकिंग और पेमेंट रसीद देखने के लिए कृपया यूज़र लॉग-इन करें।";
            res.normMr = "💡 सोपी माहिती: तुमचे बुकिंग आणि पेमेंट तपशील पाहण्यासाठी कृपया आधी युजर लॉग-इन करा.";
            res.actions.add(new ActionLink("🔑 User Login", "UserLogin.jsp", "emerald"));
            res.actions.add(new ActionLink("🧾 Open My Bookings Page", "MyBookings.jsp", "primary"));
            return;
        }

        String sql = "SELECT p.id, p.number, p.amt, p.start_time, p.end_time, "
                + "sl.slot_label, sl.charger_type, sl.vehicle_type, st.id AS station_id, st.station_name, st.area "
                + "FROM tbl_payment p "
                + "JOIN tbl_slot sl ON p.slot_id = sl.id "
                + "JOIN tbl_station st ON sl.station_id = st.id "
                + "WHERE LOWER(TRIM(p.email)) = LOWER(TRIM(?)) "
                + "ORDER BY p.start_time DESC LIMIT 5";

        Connection con = DBConnection.getConnection();
        if (con == null) {
            res.reply = "Database connection is currently unavailable.";
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a");
        SimpleDateFormat shortTime = new SimpleDateFormat("hh:mm a");
        long now = System.currentTimeMillis();

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, userEmail);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp start = rs.getTimestamp("start_time");
                    Timestamp end = rs.getTimestamp("end_time");
                    int stId = rs.getInt("station_id");
                    String stName = rs.getString("station_name");
                    String area = rs.getString("area");

                    String stateBadge = "✅ COMPLETED";
                    if (start != null && end != null) {
                        if (start.getTime() <= now && end.getTime() > now) {
                            long minsLeft = Math.max(1, (end.getTime() - now) / 60000L);
                            stateBadge = "🟢 CHARGING NOW (" + minsLeft + " min remaining)";
                        } else if (start.getTime() > now) {
                            stateBadge = "⏳ UPCOMING";
                        }
                    }

                    String timeWindow = (start != null ? sdf.format(start) : "N/A")
                            + (end != null ? " to " + shortTime.format(end) : "");

                    res.bullets.add(stateBadge + " — " + stName + " (" + area + ") | Bay "
                            + rs.getString("slot_label") + " (" + rs.getString("charger_type") + ") | Vehicle: "
                            + rs.getString("number") + " | " + timeWindow + " | Paid: ₹" + rs.getString("amt"));

                    if (res.options.size() < 2) {
                        res.options.add(new MenuOption("STATION:" + stId, "⚡", "Inspect " + stName + " Bays"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { con.close(); } catch (Exception ignored) {}
        }

        if (res.bullets.isEmpty()) {
            res.title = "📭 No Bookings Found for " + userEmail;
            res.reply = "You haven't made any charging reservations under this email yet.";
            res.bullets.add("Click 'Search & Book Your First Slot' below to pick a station and time window.");
            res.actions.add(new ActionLink("🔍 Search & Book Your First Slot", "Search.jsp", "emerald"));
        } else {
            res.title = "🧾 Your Charging Reservations (" + userEmail + ")";
            res.reply = "Here are your latest bookings directly from our live payment records:";
            res.actions.add(new ActionLink("🧾 Open Full My Bookings Table", "MyBookings.jsp", "primary"));
            res.actions.add(new ActionLink("➕ Book Another Charging Slot", "Search.jsp", "emerald"));
        }

        res.simpleNorm = "💡 Everyday Tip: Reach the station 5 minutes before your Start Time and park directly in the Bay Label (e.g., A1, B2) shown on your booking.";
        res.normHi = "💡 सरल सुझाव: अपने शुरू होने के समय से 5 मिनट पहले स्टेशन पहुँचें और अपने बुक किए गए बे नंबर पर गाड़ी पार्क करें।";
        res.normMr = "💡 सोपी टीप: तुमच्या बुकिंग वेळेच्या ५ मिनिटे आधी स्टेशनवर पोहोचा आणि तुमच्या बे नंबरवर गाडी पार्क करा.";
    }

    // =========================================================================
    // 6. ADMIN EXECUTIVE TELEMETRY (Revenue, Load, Top Hub, Unconfigured Hubs)
    // =========================================================================

    public void populateAdminTelemetry(BotResult res) {
        String kpiSql = "SELECT "
                + "(SELECT COALESCE(SUM(CAST(amt AS DECIMAL(10,2))), 0) FROM tbl_payment) AS total_rev, "
                + "(SELECT COALESCE(SUM(CAST(amt AS DECIMAL(10,2))), 0) FROM tbl_payment WHERE DATE(start_time) = CURDATE()) AS today_rev, "
                + "(SELECT COUNT(*) FROM tbl_payment WHERE start_time <= NOW() AND end_time > NOW()) AS active_now, "
                + "(SELECT COUNT(*) FROM tbl_payment WHERE start_time > NOW()) AS upcoming_cnt, "
                + "(SELECT COUNT(*) FROM tbl_payment) AS total_bookings, "
                + "(SELECT COUNT(*) FROM tbl_station st WHERE NOT EXISTS (SELECT 1 FROM tbl_slot sl WHERE sl.station_id = st.id)) AS empty_stations";

        String topHubSql = "SELECT st.id, st.station_name, st.area, "
                + "COUNT(p.id) AS booking_cnt, "
                + "COALESCE(SUM(CAST(p.amt AS DECIMAL(10,2))), 0) AS hub_rev "
                + "FROM tbl_station st "
                + "JOIN tbl_slot sl ON sl.station_id = st.id "
                + "JOIN tbl_payment p ON p.slot_id = sl.id "
                + "GROUP BY st.id, st.station_name, st.area "
                + "ORDER BY hub_rev DESC, booking_cnt DESC LIMIT 1";

        Connection con = DBConnection.getConnection();
        if (con == null) {
            res.reply = "Unable to query database telemetry.";
            return;
        }

        try {
            try (PreparedStatement ps = con.prepareStatement(kpiSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    res.title = "📊 Live Admin Executive Telemetry";
                    res.reply = "Real-time revenue, session load, and network health from MySQL:";
                    res.bullets.add("💰 Today's Revenue Collection: ₹" + String.format("%.2f", rs.getDouble("today_rev")));
                    res.bullets.add("🏦 All-Time Cumulative Revenue: ₹" + String.format("%.2f", rs.getDouble("total_rev"))
                            + " across " + rs.getInt("total_bookings") + " bookings");
                    res.bullets.add("⚡ Active Vehicles Charging Right Now: " + rs.getInt("active_now") + " live session(s)");
                    res.bullets.add("📅 Future Scheduled Reservations: " + rs.getInt("upcoming_cnt") + " upcoming booking(s)");

                    int emptySt = rs.getInt("empty_stations");
                    if (emptySt > 0) {
                        res.bullets.add("⚠️ Unconfigured Stations: " + emptySt + " station(s) currently have 0 charging bays added!");
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement(topHubSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int topId = rs.getInt("id");
                    String topName = rs.getString("station_name");
                    String topArea = rs.getString("area");
                    double topRev = rs.getDouble("hub_rev");
                    int topCnt = rs.getInt("booking_cnt");

                    res.bullets.add("🏆 Top Performing Hub: " + topName + " (" + topArea + ") — ₹"
                            + String.format("%.2f", topRev) + " (" + topCnt + " bookings)");
                    res.options.add(new MenuOption("STATION:" + topId, "🏆", "Inspect Top Hub: " + topName));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { con.close(); } catch (Exception ignored) {}
        }

        res.simpleNorm = "Admin Note: Figures update instantaneously as users complete bookings in tbl_payment.";
        res.normHi = "एडमिन सूचना: यह डेटा सीधे आपके लाइव डेटाबेस (tbl_payment) से रियल-टाइम में गणना किया गया है।";
        res.normMr = "ॲडमिन टीप: ही आकडेवारी थेट तुमच्या लाईव्ह डेटाबेसवरून (tbl_payment) रिअल-टाइममध्ये घेतली आहे.";

        res.options.add(new MenuOption("ADMIN_HARDWARE", "🛠️", "Run Hardware Fault Audit (Offline Bays)"));
        res.actions.add(new ActionLink("📊 Open Full Admin Dashboard", "AdminHome.jsp", "primary"));
        res.actions.add(new ActionLink("🏢 Open Stations Manager", "Stations.jsp", "emerald"));
    }

    // =========================================================================
    // 7. ADMIN HARDWARE FAULT & UNCONFIGURED STATION AUDIT
    // =========================================================================

    public void populateAdminHardwareAudit(BotResult res) {
        String faultSql = "SELECT st.id AS station_id, st.station_name, st.area, sl.slot_label, sl.charger_type, sl.status "
                + "FROM tbl_slot sl JOIN tbl_station st ON sl.station_id = st.id "
                + "WHERE LOWER(COALESCE(TRIM(sl.status), '')) LIKE '%offline%' "
                + "   OR LOWER(COALESCE(TRIM(sl.status), '')) LIKE '%maint%' "
                + "ORDER BY st.station_name, sl.slot_label LIMIT 10";

        String emptyStationSql = "SELECT st.id, st.station_name, st.area FROM tbl_station st "
                + "WHERE NOT EXISTS (SELECT 1 FROM tbl_slot sl WHERE sl.station_id = st.id) "
                + "ORDER BY st.station_name LIMIT 5";

        Connection con = DBConnection.getConnection();
        if (con == null) {
            res.reply = "Database unreachable.";
            return;
        }

        Set<Integer> addedStationIds = new HashSet<>();

        try {
            try (PreparedStatement ps = con.prepareStatement(faultSql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int stId = rs.getInt("station_id");
                    String stName = rs.getString("station_name");
                    String area = rs.getString("area");
                    res.bullets.add("🔴 " + stName + " (" + area + ") — Bay " + rs.getString("slot_label")
                            + " (" + rs.getString("charger_type") + ") is [" + rs.getString("status") + "]");

                    if (!addedStationIds.contains(stId) && res.actions.size() < 3) {
                        addedStationIds.add(stId);
                        res.actions.add(new ActionLink("🛠️ Fix Bays at " + stName, "ManageStation.jsp?stationId=" + stId, "emerald"));
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement(emptyStationSql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int stId = rs.getInt("id");
                    String stName = rs.getString("station_name");
                    String area = rs.getString("area");
                    res.bullets.add("⚠️ " + stName + " (" + area + ") — Has 0 slots configured! Users cannot book here until bays are added.");

                    if (!addedStationIds.contains(stId) && res.actions.size() < 4) {
                        addedStationIds.add(stId);
                        res.actions.add(new ActionLink("➕ Add Bays to " + stName, "ManageStation.jsp?stationId=" + stId, "primary"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { con.close(); } catch (Exception ignored) {}
        }

        if (res.bullets.isEmpty()) {
            res.title = "✅ 100% Hardware & Station Health";
            res.reply = "Every station has configured bays, and 0 bays are marked Offline or In Maintenance.";
            res.bullets.add("All charging connectors across the Plugy network are online and bookable.");
        } else {
            res.title = "⚠️ Hardware & Configuration Audit";
            res.reply = "Found the following bays or stations requiring administrator attention:";
        }

        res.simpleNorm = "Admin Tip: Click any 'Fix Bays' or 'Add Bays' button below to jump straight to that station's ManageStation.jsp control panel.";
        res.normHi = "एडमिन सूचना: नीचे दिए गए बटन पर क्लिक करके सीधे उस स्टेशन के 'Manage Station' पेज पर जाएँ और स्टेटस ठीक करें।";
        res.normMr = "ॲडमिन टीप: खालील बटणावर क्लिक करून थेट त्या स्टेशनच्या 'Manage Station' पेजवर जा आणि स्टेटस दुरुस्त करा.";

        res.actions.add(new ActionLink("🔌 View All Network Slots", "ViewSlots.jsp", "outline"));
        res.actions.add(new ActionLink("🏢 Open All Stations", "Stations.jsp", "primary"));
    }

    // =========================================================================
    // HELPER: STANDARD ROLE-BASED ACTION LINKS
    // =========================================================================

    private void appendRoleSearchActions(BotResult res, boolean isAdmin, boolean isUser) {
        if (isAdmin) {
            res.actions.add(new ActionLink("🏢 Manage All Stations", "Stations.jsp", "primary"));
            res.actions.add(new ActionLink("🔌 View All Slots Table", "ViewSlots.jsp", "outline"));
        } else if (isUser) {
            res.actions.add(new ActionLink("📅 Search & Book by Date/Time", "Search.jsp", "emerald"));
            res.actions.add(new ActionLink("👀 Preview All Slots", "SearchSlot.jsp", "primary"));
        } else {
            res.actions.add(new ActionLink("🔑 User Login to Book a Slot", "UserLogin.jsp", "emerald"));
            res.actions.add(new ActionLink("📝 Create Free User Account", "UserRegister.jsp", "primary"));
        }
    }
}
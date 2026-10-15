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

    public static class ActionLink {
        public String label;
        public String url;
        public String style;

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
        public String badge;

        public MenuOption(String nodeId, String icon, String label, String badge) {
            this.nodeId = nodeId;
            this.icon = icon;
            this.label = label;
            this.badge = badge != null ? badge : "";
        }
        public MenuOption(String nodeId, String icon, String label) {
            this(nodeId, icon, label, "");
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

    private static final String SQL_IS_SLOT_AVAILABLE_NOW =
        "(sl.id IS NOT NULL "
        + "AND LOWER(COALESCE(TRIM(sl.status), 'available')) NOT LIKE '%offline%' "
        + "AND LOWER(COALESCE(TRIM(sl.status), 'available')) NOT LIKE '%maint%' "
        + "AND NOT EXISTS ("
        + "  SELECT 1 FROM tbl_payment p "
        + "  WHERE p.slot_id = sl.id AND p.start_time <= NOW() AND p.end_time > NOW()"
        + "))";

    private static final Set<String> STOPWORDS = new HashSet<>(Arrays.asList(
        "do", "you", "we", "our", "have", "has", "had", "can", "could", "would", "will",
        "please", "tell", "give", "get", "want", "need", "find", "search", "show", "list",
        "me", "us", "my", "the", "some", "any", "all", "in", "at", "on", "to", "from",
        "near", "nearby", "around", "by", "with", "of", "for", "is", "are", "was", "were",
        "there", "where", "what", "which", "how", "many", "much", "right", "now", "live"
    ));

    // =========================================================================
    // 1. DYNAMIC AREA DIRECTORY (Strict Limit: Top 5 Areas)
    // =========================================================================
    public void populateInteractiveAreaDirectory(BotResult res, boolean isAdmin, boolean isUser) {
        String sql = "SELECT st.area, "
                + "COUNT(DISTINCT st.id) AS station_cnt, "
                + "COUNT(sl.id) AS slot_cnt, "
                + "COALESCE(SUM(CASE WHEN " + SQL_IS_SLOT_AVAILABLE_NOW + " THEN 1 ELSE 0 END), 0) AS free_cnt "
                + "FROM tbl_station st "
                + "LEFT JOIN tbl_slot sl ON st.id = sl.station_id "
                + "WHERE st.area IS NOT NULL AND TRIM(st.area) <> '' "
                + "GROUP BY st.area "
                + "ORDER BY free_cnt DESC LIMIT 5";

        Connection con = DBConnection.getConnection();
        if (con == null) {
            res.title = "⚠️ Connection Error";
            res.reply = "Could not connect to the network.";
            return;
        }

        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String area = rs.getString("area").trim();
                int stCnt = rs.getInt("station_cnt");
                int freeCnt = rs.getInt("free_cnt");
                
                String badge = freeCnt + " Free";
                res.options.add(new MenuOption("AREA:" + area, "📍", area + " (" + stCnt + " Hubs)", badge));
            }
        } catch (Exception e) { e.printStackTrace(); } 
        finally { try { con.close(); } catch (Exception ignored) {} }

        res.title = "📍 Select an Area";
        res.reply = "Here are the top 5 areas with available charging bays right now:";
        res.actions.add(new ActionLink("🗺️ View All Stations on Map", "Search.jsp", "outline"));
    }

    // =========================================================================
    // 2. STATIONS IN AREA (Strict Limit: Top 3 Stations)
    // =========================================================================
    public void populateStationsInArea(BotResult res, String areaName, boolean isAdmin, boolean isUser) {
        String cleanArea = areaName != null ? areaName.trim() : "";
        String sql = "SELECT st.id, st.station_name, "
                + "COUNT(sl.id) AS total_slots, "
                + "COALESCE(SUM(CASE WHEN " + SQL_IS_SLOT_AVAILABLE_NOW + " THEN 1 ELSE 0 END), 0) AS free_slots "
                + "FROM tbl_station st "
                + "LEFT JOIN tbl_slot sl ON st.id = sl.station_id "
                + "WHERE LOWER(TRIM(st.area)) LIKE ? "
                + "GROUP BY st.id, st.station_name "
                + "ORDER BY free_slots DESC LIMIT 3";

        Connection con = DBConnection.getConnection();
        if (con == null) return;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, "%" + cleanArea.toLowerCase() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int stId = rs.getInt("id");
                    String stName = rs.getString("station_name");
                    int free = rs.getInt("free_slots");
                    
                    res.options.add(new MenuOption("STATION:" + stId, "⚡", stName, free + " Free"));
                }
            }
        } catch (Exception e) { e.printStackTrace(); } 
        finally { try { con.close(); } catch (Exception ignored) {} }

        res.title = "🏢 Stations in " + cleanArea;
        if (res.options.isEmpty()) {
            res.reply = "No active stations found in this area.";
        } else {
            res.reply = "Tap a station below to inspect its individual bays:";
        }
    }

    // =========================================================================
    // 3. SINGLE STATION BREAKDOWN (Compact View)
    // =========================================================================
    public void populateSingleStationBayBreakdown(BotResult res, int stationId, boolean isAdmin, boolean isUser) {
        String stSql = "SELECT station_name FROM tbl_station WHERE id = ?";
        String slotSql = "SELECT sl.slot_label, sl.charger_type, "
                + "(SELECT COUNT(*) FROM tbl_payment p WHERE p.slot_id = sl.id AND p.start_time <= NOW() AND p.end_time > NOW()) AS active_now, "
                + "sl.status FROM tbl_slot sl WHERE sl.station_id = ? ORDER BY sl.slot_label ASC";

        Connection con = DBConnection.getConnection();
        if (con == null) return;

        String stName = "Station";
        int freeCount = 0;

        try {
            try (PreparedStatement ps = con.prepareStatement(stSql)) {
                ps.setInt(1, stationId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) stName = rs.getString("station_name");
                }
            }

            try (PreparedStatement ps = con.prepareStatement(slotSql)) {
                ps.setInt(1, stationId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String label = rs.getString("slot_label");
                        String plug = rs.getString("charger_type");
                        String status = rs.getString("status") != null ? rs.getString("status") : "Available";
                        int active = rs.getInt("active_now");

                        if (status.toLowerCase().contains("offline") || status.toLowerCase().contains("maint")) {
                            res.bullets.add("🔴 Bay " + label + " (" + plug + ") — " + status);
                        } else if (active > 0) {
                            res.bullets.add("🟠 Bay " + label + " (" + plug + ") — Reserved");
                        } else {
                            res.bullets.add("🟢 Bay " + label + " (" + plug + ") — Available");
                            freeCount++;
                        }
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); } 
        finally { try { con.close(); } catch (Exception ignored) {} }

        res.title = "⚡ " + stName;
        res.reply = freeCount + " bays are free right now:";
        
        // Contextual CTA: Only offer Book button if they are a user. No login spam!
        if (isUser) {
            res.actions.add(new ActionLink("📅 Book a Bay Here", "SearchSlot.jsp?stationId=" + stationId, "emerald"));
        } else if (isAdmin) {
            res.actions.add(new ActionLink("⚙️ Manage Station", "ManageStation.jsp?stationId=" + stationId, "outline"));
        }
    }

    // =========================================================================
    // 4. FILTERED STATIONS (Strict Limit: Top 3)
    // =========================================================================
    public void populateFilteredStations(BotResult res, String chargerFilter, String vehicleFilter, String rawAreaQuery, boolean isAdmin, boolean isUser) {
        String sql = "SELECT st.id, st.station_name, st.area, "
                + "COUNT(sl.id) AS total_slots, "
                + "COALESCE(SUM(CASE WHEN " + SQL_IS_SLOT_AVAILABLE_NOW + " THEN 1 ELSE 0 END), 0) AS free_slots "
                + "FROM tbl_station st "
                + "LEFT JOIN tbl_slot sl ON st.id = sl.station_id "
                + "WHERE 1=1 ";

        if (chargerFilter != null) sql += "AND UPPER(TRIM(sl.charger_type)) = '" + chargerFilter.toUpperCase() + "' ";
        if (vehicleFilter != null) sql += "AND LOWER(TRIM(sl.vehicle_type)) LIKE '%" + vehicleFilter.toLowerCase() + "%' ";

        sql += "GROUP BY st.id, st.station_name, st.area HAVING COUNT(sl.id) > 0 "
             + "ORDER BY free_slots DESC LIMIT 3";

        Connection con = DBConnection.getConnection();
        if (con == null) return;

        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int stId = rs.getInt("id");
                String stName = rs.getString("station_name");
                String area = rs.getString("area");
                int free = rs.getInt("free_slots");
                int total = rs.getInt("total_slots");

                String badge = free > 0 ? "🟢 " + free + "/" + total + " Free" : "🟠 Full";
                res.options.add(new MenuOption("STATION:" + stId, "⚡", stName + " (" + area + ")", badge));
            }
        } catch (Exception e) { e.printStackTrace(); } 
        finally { try { con.close(); } catch (Exception ignored) {} }

        if (res.options.isEmpty()) {
            res.reply = "No stations currently match that filter.";
        } else {
            res.reply = "Top 3 matching stations available right now:";
            res.actions.add(new ActionLink("🗺️ Search All on Map", "Search.jsp", "outline"));
        }
    }

    // =========================================================================
    // 5. USER BOOKINGS
    // =========================================================================
    public void populateUserBookings(BotResult res, String userEmail) {
        if (userEmail == null || userEmail.trim().isEmpty()) {
            res.title = "🔑 Authentication Required";
            res.reply = "Please sign in to view your secure booking history.";
            res.actions.add(new ActionLink("🔑 User Login", "UserLogin.jsp", "emerald"));
            return;
        }

        String sql = "SELECT p.amt, p.start_time, st.station_name "
                + "FROM tbl_payment p "
                + "JOIN tbl_slot sl ON p.slot_id = sl.id "
                + "JOIN tbl_station st ON sl.station_id = st.id "
                + "WHERE LOWER(TRIM(p.email)) = LOWER(TRIM(?)) "
                + "ORDER BY p.start_time DESC LIMIT 3";

        Connection con = DBConnection.getConnection();
        if (con == null) return;

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, hh:mm a");

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, userEmail);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    res.bullets.add(rs.getString("station_name") + " | " + sdf.format(rs.getTimestamp("start_time")));
                }
            }
        } catch (Exception e) { e.printStackTrace(); } 
        finally { try { con.close(); } catch (Exception ignored) {} }

        if (res.bullets.isEmpty()) {
            res.reply = "No recent bookings found.";
        } else {
            res.title = "🧾 Recent Bookings";
            res.reply = "Your latest 3 reservations:";
            res.actions.add(new ActionLink("View Full History", "MyBookings.jsp", "outline"));
        }
    
    }
 // =========================================================================
    // 6. ADMIN TELEMETRY & HARDWARE AUDIT
    // =========================================================================
    public void populateAdminTelemetry(BotResult res) {
        String sql = "SELECT "
                + "(SELECT COALESCE(SUM(CAST(amt AS DECIMAL(10,2))), 0) FROM tbl_payment) AS total_rev, "
                + "(SELECT COALESCE(SUM(CAST(amt AS DECIMAL(10,2))), 0) FROM tbl_payment WHERE DATE(start_time) = CURDATE()) AS today_rev, "
                + "(SELECT COUNT(*) FROM tbl_payment WHERE start_time <= NOW() AND end_time > NOW()) AS active_now, "
                + "(SELECT COUNT(*) FROM tbl_station st WHERE NOT EXISTS (SELECT 1 FROM tbl_slot sl WHERE sl.station_id = st.id)) AS empty_stations";

        Connection con = DBConnection.getConnection();
        if (con == null) {
            res.reply = "Unable to query database telemetry.";
            return;
        }

        try (PreparedStatement ps = con.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                res.bullets.add("💰 Today's Revenue: ₹" + String.format("%.2f", rs.getDouble("today_rev")));
                res.bullets.add("🏦 Total Revenue: ₹" + String.format("%.2f", rs.getDouble("total_rev")));
                res.bullets.add("⚡ Active Sessions Now: " + rs.getInt("active_now"));
                
                int emptySt = rs.getInt("empty_stations");
                if (emptySt > 0) res.bullets.add("⚠️ " + emptySt + " station(s) have 0 bays configured!");
            }
        } catch (Exception e) { e.printStackTrace(); } 
        finally { try { con.close(); } catch (Exception ignored) {} }

        res.title = "📊 Live Admin Telemetry";
        res.reply = "Real-time figures from the network:";
        res.actions.add(new ActionLink("📊 Open Full Dashboard", "AdminHome.jsp", "emerald"));
    }

    public void populateAdminHardwareAudit(BotResult res) {
        String faultSql = "SELECT st.station_name, sl.slot_label, sl.status "
                + "FROM tbl_slot sl JOIN tbl_station st ON sl.station_id = st.id "
                + "WHERE LOWER(COALESCE(TRIM(sl.status), '')) LIKE '%offline%' "
                + "   OR LOWER(COALESCE(TRIM(sl.status), '')) LIKE '%maint%' "
                + "ORDER BY st.station_name LIMIT 6";

        Connection con = DBConnection.getConnection();
        if (con == null) return;

        try (PreparedStatement ps = con.prepareStatement(faultSql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                res.bullets.add("🔴 " + rs.getString("station_name") + " (Bay " + rs.getString("slot_label") + ") — " + rs.getString("status"));
            }
        } catch (Exception e) { e.printStackTrace(); } 
        finally { try { con.close(); } catch (Exception ignored) {} }

        if (res.bullets.isEmpty()) {
            res.title = "✅ 100% Hardware Health";
            res.reply = "0 bays are marked Offline or In Maintenance.";
        } else {
            res.title = "⚠️ Hardware Faults";
            res.reply = "The following bays require attention:";
            res.actions.add(new ActionLink("⚙️ Fix Bays in Stations", "Stations.jsp", "outline"));
        }
    }
}
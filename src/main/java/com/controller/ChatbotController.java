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

        // Optional stationId if user/admin is currently viewing a specific station page
        String pageStationId = request.getParameter("pageStationId");

        HttpSession session = request.getSession(false);
        String role = detectRole(session, currentPage);
        String userEmail = getUserEmail(session);

        String rawNodeId = request.getParameter("nodeId");
        String rawQuery = request.getParameter("message");

        BotResult result;
        if (rawQuery != null && !rawQuery.trim().isEmpty()) {
            // User typed a keyword, car name, or area name in the bottom search bar
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
    // MASTER ROUTER: HANDLES DYNAMIC DB NODES + 35 STATIC DECISION-TREE NODES
    // =========================================================================

    private BotResult routeNode(String rawNodeId, String role, String userEmail, String currentPage, String pageStationId) {
        boolean isAdmin = "ADMIN".equals(role);
        boolean isUser = "USER".equals(role);

        // 1. Dynamic Area Drill-Down: "AREA:<AreaName>"
        if (rawNodeId.regionMatches(true, 0, "AREA:", 0, 5)) {
            String areaName = rawNodeId.substring(5).trim();
            BotResult res = new BotResult();
            res.nodeId = "AREA:" + areaName;
            res.parentId = "LIVE_ALL_AREAS";
            res.breadcrumb = "Main Menu > Live Stations > " + areaName;
            dao.populateStationsInArea(res, areaName, isAdmin, isUser);
            return res;
        }

        // 2. Dynamic Single Station Bay Breakdown: "STATION:<StationId>"
        if (rawNodeId.regionMatches(true, 0, "STATION:", 0, 8)) {
            String idPart = rawNodeId.substring(8).trim();
            int stId = parseStationId(idPart);
            BotResult res = new BotResult();
            res.nodeId = "STATION:" + stId;
            res.parentId = "LIVE_ALL_AREAS";
            res.breadcrumb = "Main Menu > Live Stations > Station #" + stId;
            if (stId > 0) {
                dao.populateSingleStationBayBreakdown(res, stId, isAdmin, isUser);
            } else {
                res.title = "⚠️ Invalid Station Selected";
                res.reply = "Please select a valid charging station from the directory below:";
                res.options.add(new MenuOption("LIVE_ALL_AREAS", "📍", "Browse All Covered Areas", "Live"));
            }
            return res;
        }

        // 3. Static Decision-Tree Nodes (Normalized to Uppercase)
        String nodeId = rawNodeId.toUpperCase();
        BotResult res = new BotResult();
        res.nodeId = nodeId;

        switch (nodeId) {

            // -----------------------------------------------------------------
            // ROOT MENU (Context-Aware Entry Point for Admin, User, and Guest)
            // -----------------------------------------------------------------
            case "ROOT":
            default:
                res.nodeId = "ROOT";
                res.parentId = "";
                res.breadcrumb = "🏠 Main Menu";

                // If the user/admin is currently on a page with ?stationId=X, offer a top shortcut!
                int currentStId = parseStationId(pageStationId);
                if (currentStId > 0) {
                    res.options.add(new MenuOption(
                        "STATION:" + currentStId,
                        "🎯",
                        "Inspect Current Page Station (#" + currentStId + ") Bays",
                        "On This Page"
                    ));
                }

                if (isAdmin) {
                    res.title = "🛡️ Plugy Admin Command & Guide";
                    res.reply = "Welcome, Administrator! Select any operational category below to audit live MySQL telemetry, inspect station bays, or review platform workflows:";
                    res.simpleNorm = "💡 Admin Tip: Click any option below to drill down. You can also type any area name (like 'Wakad' or 'Hadapsar') in the bottom search bar.";
                    res.normHi = "💡 एडमिन सुझाव: लाइव डेटाबेस रिपोर्ट या हार्डवेयर ऑडिट देखने के लिए नीचे दिए गए किसी भी विकल्प पर क्लिक करें।";
                    res.normMr = "💡 ॲडमिन टीप: लाईव्ह डेटाबेस रिपोर्ट किंवा स्टेशनची माहिती पाहण्यासाठी खालील पर्यायावर क्लिक करा.";

                    res.options.add(new MenuOption("CAT_ADMIN", "📊", "Live Revenue, Active Load & Hardware Audit", "Live DB"));
                    res.options.add(new MenuOption("CAT_LIVE_SLOTS", "⚡", "Browse Live Stations & Bay Status by Area", "Live DB"));
                    res.options.add(new MenuOption("CAT_ADMIN_GUIDE", "🏢", "Admin Guide: Stations, Bays & Bookings", "How-To"));
                    res.options.add(new MenuOption("CAT_BEGINNER", "🌱", "EV Charging Guide (Customer View)", "FAQ"));
                    res.options.add(new MenuOption("CAT_BOOKING_HELP", "📅", "Customer Booking & Preview Mode Rules", "Rules"));
                } else {
                    res.title = "👋 Welcome to Plugy EV Assistant";
                    res.reply = isUser
                            ? "Hi! Whether you are booking your first EV slot or checking live chargers in Pune & PCMC, tap a topic below and I will guide you step-by-step:"
                            : "New to Electric Vehicles or visiting Plugy for the first time? Tap any topic below — everything is explained in plain everyday language:";
                    res.simpleNorm = "💡 First time charging an EV? Tap 'New to EV Charging? (Start Here)' below for a simple 1-minute guide with zero technical jargon.";
                    res.normHi = "💡 इलेक्ट्रिक गाड़ी के लिए नए हैं? आसान भाषा में समझने के लिए पहले विकल्प 'New to EV Charging?' पर क्लिक करें।";
                    res.normMr = "💡 इलेक्ट्रिक गाडीसाठी नवीन आहात? सोप्या भाषेत समजून घेण्यासाठी पहिल्या पर्यायावर क्लिक करा.";

                    res.options.add(new MenuOption("CAT_BEGINNER", "🌱", "New to EV Charging? (Beginner's Guide)", "Start Here"));
                    res.options.add(new MenuOption("CAT_LIVE_SLOTS", "⚡", "Find Live Charging Stations & Free Slots", "Live DB"));
                    res.options.add(new MenuOption("CAT_BOOKING_HELP", "📅", "How to Book, Pricing & 'Preview Mode'", "Step-by-Step"));
                    res.options.add(new MenuOption("CAT_ACCOUNT", "🧾", "My Bookings, Login & Password Help", isUser ? "My Account" : "Sign In"));
                    res.options.add(new MenuOption("CAT_TROUBLE", "🛠️", "On-Site Troubleshooting & Support", "24x7 Help"));
                }
                break;

            // =========================================================================
            // BRANCH 1: NEW TO EV CHARGING? (BEGINNER'S GUIDE — ZERO JARGON)
            // =========================================================================
            case "CAT_BEGINNER":
                res.parentId = "ROOT";
                res.breadcrumb = "Main Menu > Beginner's Guide";
                res.title = "🌱 Beginner's Guide to EV Charging";
                res.reply = "Never used a public EV charging station before? Don't worry! Pick any question below and we will explain it using simple everyday comparisons:";
                res.simpleNorm = "💡 Everyday Norm: If you know how to charge a mobile phone and book a movie ticket online, you already know 95% of how Plugy works!";
                res.normHi = "💡 सरल नियम: यदि आप मोबाइल फोन चार्ज करना और ऑनलाइन मूवी टिकट बुक करना जानते हैं, तो आप Plugy का उपयोग आसानी से कर सकते हैं!";
                res.normMr = "💡 सोपा नियम: जर तुम्हाला मोबाईल चार्ज करता येतो आणि ऑनलाइन तिकीट बुक करता येते, तर तुम्ही Plugy सहज वापरू शकता!";

                res.options.add(new MenuOption("BEG_HOW_IT_WORKS", "🔋", "How does public EV charging actually work?", "1 Min Read"));
                res.options.add(new MenuOption("BEG_PLUG_TYPES", "🔌", "Which plug (CCS2, CHAdeMO, GB/T) fits my vehicle?", "Plug Guide"));
                res.options.add(new MenuOption("BEG_WHICH_VEHICLE", "🚗", "Which Vehicle Category should I select?", "2W vs 4W"));
                res.options.add(new MenuOption("BEG_SPEED_COMPARE", "⏱️", "Normal vs. Fast Charging — how long does it take?", "Time & Speed"));
                res.options.add(new MenuOption("BEG_BATTERY_KWH", "📐", "What does 'Battery Capacity (kWh)' mean?", "Simple Math"));
                res.options.add(new MenuOption("BEG_RAIN_SAFETY", "🌧️", "Is it safe to charge in rain or thunderstorms?", "Safety"));
                res.options.add(new MenuOption("BEG_DICTIONARY", "📖", "Simple Dictionary of Website Words", "A-Z Terms"));
                break;

            case "BEG_HOW_IT_WORKS":
                res.parentId = "CAT_BEGINNER";
                res.breadcrumb = "Main Menu > Beginner's Guide > How It Works";
                res.title = "🔋 How Does Public EV Charging Work?";
                res.reply = "An Electric Vehicle (EV) runs on a rechargeable battery instead of petrol. Using a Plugy station works in 3 simple steps:";
                res.bullets.add("1️⃣ Book Your Time Slot Online: Just like reserving a seat at a cinema, you pick your nearby station and time window (for example, 5:00 PM to 6:00 PM) on Plugy so nobody else takes your spot.");
                res.bullets.add("2️⃣ Park in Your Numbered Bay: Drive to the station and park in the exact bay number you booked (such as Bay A1 or Bay B2).");
                res.bullets.add("3️⃣ Plug In & Relax: Take the charging cable attached to the machine, push it into your vehicle's charging socket until it clicks, and relax while your battery tops up!");
                res.simpleNorm = "💡 Everyday Comparison: Think of a Plugy station like a public mobile-charging kiosk where you pre-book your socket in advance so you never stand in a queue.";
                res.normHi = "💡 सरल उदाहरण: जैसे आप सिनेमा हॉल की सीट पहले बुक करते हैं ताकि लाइन में न लगना पड़े, वैसे ही यहाँ अपना समय चुनें, स्टेशन पहुँचें और गाड़ी चार्ज करें।";
                res.normMr = "💡 सोपे उदाहरण: जसे आपण सिनेमाचे तिकीट आधी बुक करतो जेणेकरून रांगेत थांबावे लागत नाही, तसेच येथे आधी वेळ बुक करा आणि स्टेशनवर जाऊन गाडी चार्ज करा.";
                addStandardSearchAction(res, isAdmin, isUser);
                res.options.add(new MenuOption("BEG_PLUG_TYPES", "🔌", "Next: Which plug fits my vehicle?"));
                res.options.add(new MenuOption("BOOK_STEPS", "✅", "Show me how to book step-by-step"));
                break;

            case "BEG_PLUG_TYPES":
                res.parentId = "CAT_BEGINNER";
                res.breadcrumb = "Main Menu > Beginner's Guide > Plug Types";
                res.title = "🔌 Which Plug (CCS2, CHAdeMO, GB/T) Fits My EV?";
                res.reply = "Just like phones have USB Type-C and iPhone Lightning cables, electric vehicles use 3 standard plug shapes on Plugy:";
                res.bullets.add("🔵 CCS2 (The 'USB Type-C' for Modern Cars): Fits 95% of passenger electric cars in India — including Tata Nexon EV, Punch EV, Tiago EV, Curvv EV, MG ZS EV, Windsor EV, Hyundai Ioniq 5, BYD Atto 3, and Mahindra XUV400.");
                res.bullets.add("🟢 GB/T (For Scooters, Bikes & Fleet Cars): Fits electric 2-wheelers (Ola, Ather, TVS iQube, Bajaj Chetak) and commercial fleet cars like Tata Tigor EV / Xpres-T and Mahindra eVerito.");
                res.bullets.add("🟣 CHAdeMO (For Select Imported Cars): Used by specific international models like the Nissan Leaf.");
                res.simpleNorm = "💡 Quick Rule of Thumb: Driving a modern personal electric car? Pick CCS2. Riding an electric scooter/bike or driving a fleet cab? Pick GB/T.";
                res.normHi = "💡 आसान पहचान: यदि आपके पास नई इलेक्ट्रिक कार (Tata, MG, Mahindra) है तो 'CCS2' चुनें। यदि इलेक्ट्रिक स्कूटर/बाइक या टैक्सी फ्लीट कार है तो 'GB/T' चुनें।";
                res.normMr = "💡 सोपी ओळख: तुमच्याकडे नवीन इलेक्ट्रिक कार (Tata, MG, Mahindra) असल्यास 'CCS2' निवडा. इलेक्ट्रिक स्कूटर/बाईक असल्यास 'GB/T' निवडा.";
                res.options.add(new MenuOption("LIVE_PLUG_CCS2", "🔵", "Show Live Stations with CCS2 Plugs", "Live DB"));
                res.options.add(new MenuOption("LIVE_PLUG_GBT", "🟢", "Show Live Stations with GB/T Plugs", "Live DB"));
                res.options.add(new MenuOption("LIVE_PLUG_CHADEMO", "🟣", "Show Live Stations with CHAdeMO Plugs", "Live DB"));
                break;

            case "BEG_WHICH_VEHICLE":
                res.parentId = "CAT_BEGINNER";
                res.breadcrumb = "Main Menu > Beginner's Guide > Vehicle Types";
                res.title = "🚗 Which Vehicle Category Should I Pick on Plugy?";
                res.reply = "When searching or viewing bays on Plugy, you will see 3 Vehicle Categories:";
                res.bullets.add("🛵 2-Wheeler: Choose this for all electric scooters and electric motorcycles (Ola S1, Ather 450X, TVS iQube, Bajaj Chetak, Vida, Revolt).");
                res.bullets.add("🚗 4-Wheeler-Normal: Choose this for electric cars when you want gentle, economical charging while parked for 2 to 4 hours.");
                res.bullets.add("⚡ 4-Wheeler-Fast: Choose this for electric cars when you want high-speed DC express charging (20% to 80% in 30 to 45 minutes).");
                res.simpleNorm = "💡 Everyday Tip: A 4-Wheeler electric car can use either '4-Wheeler-Normal' or '4-Wheeler-Fast' depending on how quickly you need to get back on the road.";
                res.normHi = "💡 सरल जानकारी: स्कूटर/बाइक के लिए '2-Wheeler' चुनें। कार को जल्दी चार्ज करना हो तो '4-Wheeler-Fast' और आराम से करना हो तो '4-Wheeler-Normal' चुनें।";
                res.normMr = "💡 सोपी माहिती: स्कूटर/बाईकसाठी '2-Wheeler' निवडा. कार वेगाने चार्ज करायची असल्यास '4-Wheeler-Fast' आणि निवांत वेळ असल्यास '4-Wheeler-Normal' निवडा.";
                res.options.add(new MenuOption("LIVE_2W", "🛵", "Find Live 2-Wheeler Bays Now", "Live DB"));
                res.options.add(new MenuOption("LIVE_4W_FAST", "⚡", "Find Live 4-Wheeler Fast Bays Now", "Live DB"));
                res.options.add(new MenuOption("LIVE_4W_NORMAL", "🚗", "Find Live 4-Wheeler Normal Bays Now", "Live DB"));
                break;

            case "BEG_SPEED_COMPARE":
                res.parentId = "CAT_BEGINNER";
                res.breadcrumb = "Main Menu > Beginner's Guide > Normal vs Fast";
                res.title = "⏱️ Normal vs. Fast Charging — How Long Will I Wait?";
                res.reply = "Here is an honest, real-world time comparison so you know how long to book your slot:";
                res.bullets.add("⚡ 4-Wheeler-Fast (DC Fast Charger): Charges a typical electric car from 20% to 80% in 30 to 45 minutes. Best for highway trips or quick city top-ups.");
                res.bullets.add("🚗 4-Wheeler-Normal (AC Charger): Adds roughly 15% to 25% battery per hour (takes 3 to 5 hours for a full charge). Best while you are at work, watching a movie, or shopping.");
                res.bullets.add("🛵 2-Wheeler Charger: Charges most electric scooters from 20% to 80% in 45 to 75 minutes.");
                res.bullets.add("🔋 Why 80%? Just like your smartphone, EV batteries charge super fast from 0% to 80%, and then slow down between 80% and 100% to protect battery health.");
                res.simpleNorm = "💡 Everyday Comparison: Normal Charging is like plugging your phone into a laptop USB port overnight. Fast Charging is like a 65W Dash/SuperVOOC charger that fills 80% while you drink tea!";
                res.normHi = "💡 सरल नियम: बैटरी की सुरक्षा के लिए हर इलेक्ट्रिक गाड़ी 80% तक बहुत तेज़ी से चार्ज होती है और आखिरी 20% (80-100%) में धीरे चार्ज होती है।";
                res.normMr = "💡 सोपा नियम: बॅटरी सुरक्षित ठेवण्यासाठी प्रत्येक इलेक्ट्रिक गाडी ८०% पर्यंत खूप वेगाने चार्ज होते आणि ८०% ते १००% दरम्यान हळू चार्ज होते.";
                addStandardSearchAction(res, isAdmin, isUser);
                break;

            case "BEG_BATTERY_KWH":
                res.parentId = "CAT_BEGINNER";
                res.breadcrumb = "Main Menu > Beginner's Guide > Battery kWh Explained";
                res.title = "📐 What Does 'Battery Capacity (kWh)' Mean?";
                res.reply = "When booking or checking EV specs, you will see 'kWh' (Kilowatt-hour). Don't let the unit scare you:";
                res.bullets.add("⛽ It Is Simply Your 'Tank Size': In a petrol car, tank size is measured in Litres (e.g., 40 Litres). In an EV, battery tank size is measured in kWh (Units of electricity).");
                res.bullets.add("🛵 Typical Electric Scooter: Has a 2.5 to 4 kWh battery (gives 80 to 120 km range).");
                res.bullets.add("🚗 Typical City Electric Car (Tiago / Punch / Comet): Has a 17 to 35 kWh battery (gives 200 to 315 km range).");
                res.bullets.add("🚙 Large Electric SUV (Nexon / ZS EV / XUV400): Has a 40 to 50 kWh battery (gives 350 to 450 km range).");
                res.simpleNorm = "💡 Everyday Comparison: 1 kWh = 1 Unit of electricity on your home MSEB/Mahavitaran electricity bill! So a 30 kWh car battery holds 30 Units of electricity.";
                res.normHi = "💡 सरल गणित: 1 kWh का मतलब आपके घर के बिजली बिल की '1 यूनिट' होता है! यानी 30 kWh की कार बैटरी में 30 यूनिट बिजली आती है।";
                res.normMr = "💡 सोपे गणित: 1 kWh म्हणजे तुमच्या घरच्या लाईट बिलातील '१ युनिट' वीज! म्हणजेच 30 kWh च्या कार बॅटरीमध्ये ३० युनिट वीज मावते.";
                break;

            case "BEG_RAIN_SAFETY":
                res.parentId = "CAT_BEGINNER";
                res.breadcrumb = "Main Menu > Beginner's Guide > Rain Safety";
                res.title = "🌧️ Is It Safe to Charge in Heavy Pune Rain?";
                res.reply = "Yes — 100% safe! Every Plugy charging connector and EV socket is engineered with multi-layer weatherproofing (IP65/IP67 rated):";
                res.bullets.add("🔒 Zero Live Current While Holding the Plug: When the cable is in your hand, the metal pins carry zero electricity.");
                res.bullets.add("🤝 Computer Safety Lock First: Electricity only flows AFTER the plug is pushed deep into your car, mechanically locked, and the car's computer confirms a sealed, dry circuit.");
                res.bullets.add("⚡ Instant Auto-Cut: If moisture or a power surge is detected, the station cuts power in less than 0.01 seconds.");
                res.simpleNorm = "💡 Everyday Tip: You cannot get an electric shock from plugging in an EV in the rain because power stays completely OFF until the plug is locked inside the car port.";
                res.normHi = "💡 सरल जानकारी: बारिश में भी गाड़ी चार्ज करना 100% सुरक्षित है क्योंकि जब तक प्लग कार के अंदर लॉक नहीं होता, तब तक उसमें करंट चालू ही नहीं होता।";
                res.normMr = "💡 सोपी माहिती: मुसळधार पावसातही गाडी चार्ज करणे १००% सुरक्षित आहे कारण प्लग गाडीत लॉक होईपर्यंत त्यात अजिबात वीज नसते.";
                break;

            case "BEG_DICTIONARY":
                res.parentId = "CAT_BEGINNER";
                res.breadcrumb = "Main Menu > Beginner's Guide > Simple Dictionary";
                res.title = "📖 Simple Dictionary of Words on This Website";
                res.reply = "Here is what every term on the Plugy website means in plain English:";
                res.bullets.add("🏢 Station / Hub: The physical location (like Wakad Hub or Kothrud Hub) where multiple charging machines are installed.");
                res.bullets.add("🅿️ Slot / Bay (e.g., A1, A2, B1): One specific parking space and its dedicated charging cable inside a station.");
                res.bullets.add("👀 Preview Mode: When you open the Slots page just to browse without choosing a Date & Time first. To see the 'Book' button, you must choose your Date & Time on the Search page first.");
                res.bullets.add("🟢 Available: The bay is working and free for your chosen time.");
                res.bullets.add("🟠 Reserved / Occupied: Another driver has booked this bay right now. You can still book it for a later hour!");
                res.bullets.add("🔴 Offline / In Maintenance: The machine is turned off for servicing and cannot be booked.");
                res.simpleNorm = "💡 Everyday Comparison: 'Station' = Cinema Hall, 'Slot/Bay A1' = Seat Number A1, and 'Start/End Time' = Showtime.";
                res.normHi = "💡 सरल उदाहरण: 'Station' = सिनेमा हॉल, 'Bay A1' = सीट नंबर A1, और 'Preview Mode' = बिना समय चुने सिर्फ खाली सीटें देखना।";
                res.normMr = "💡 सोपे उदाहरण: 'Station' = चित्रपटगृह, 'Bay A1' = सीट क्रमांक A1, आणि 'Preview Mode' = वेळ न निवडता फक्त जागा पाहणे.";
                addStandardSearchAction(res, isAdmin, isUser);
                break;

            // =========================================================================
            // BRANCH 2: FIND LIVE CHARGING STATIONS & FREE SLOTS (LIVE MYSQL DRILL-DOWN)
            // =========================================================================
            case "CAT_LIVE_SLOTS":
                res.parentId = "ROOT";
                res.breadcrumb = "Main Menu > Live Stations & Slots";
                res.title = "⚡ Find Live Charging Stations & Free Bays";
                res.reply = "I am connected live to the Plugy MySQL database. Choose how you want to explore available stations right now:";
                res.simpleNorm = "💡 Tip: Tap 'Browse by Area' to pick your neighborhood in Pune/PCMC and inspect individual bays (A1, A2, B1) at each station!";
                res.normHi = "💡 सुझाव: अपने इलाके के स्टेशन और खाली स्लॉट देखने के लिए नीचे एक विकल्प चुनें।";
                res.normMr = "💡 टीप: तुमच्या परिसरातील स्टेशन्स आणि रिकामे स्लॉट्स पाहण्यासाठी खालील पर्याय निवडा.";

                res.options.add(new MenuOption("LIVE_ALL_AREAS", "📍", "Browse by Area in Pune & PCMC (Interactive)", "Drill-Down"));
                res.options.add(new MenuOption("LIVE_2W", "🛵", "Find 2-Wheeler Slots (Scooters & Bikes)", "Live DB"));
                res.options.add(new MenuOption("LIVE_4W_FAST", "⚡", "Find 4-Wheeler Fast Chargers (Express Cars)", "Live DB"));
                res.options.add(new MenuOption("LIVE_4W_NORMAL", "🚗", "Find 4-Wheeler Normal Chargers (Standard Cars)", "Live DB"));
                res.options.add(new MenuOption("CAT_PLUG_FILTER", "🔌", "Filter by Plug Type (CCS2 / CHAdeMO / GB/T)", "3 Connectors"));
                break;

            case "LIVE_ALL_AREAS":
                res.parentId = "CAT_LIVE_SLOTS";
                res.breadcrumb = "Main Menu > Live Stations > Select Area";
                dao.populateInteractiveAreaDirectory(res, isAdmin, isUser);
                break;

            case "CAT_PLUG_FILTER":
                res.parentId = "CAT_LIVE_SLOTS";
                res.breadcrumb = "Main Menu > Live Stations > By Plug Type";
                res.title = "🔌 Filter Live Stations by Plug Shape";
                res.reply = "Tap your vehicle's connector type below to query which stations have matching bays open right now:";
                res.simpleNorm = "💡 Reminder: Tata, MG, Hyundai, and Mahindra electric cars use CCS2. Electric scooters and fleet cabs typically use GB/T.";
                res.normHi = "💡 याद रखें: नई इलेक्ट्रिक कारों के लिए CCS2 और इलेक्ट्रिक स्कूटर/फ्लीट गाड़ियों के लिए GB/T चुनें।";
                res.normMr = "💡 लक्षात ठेवा: नवीन इलेक्ट्रिक कारसाठी CCS2 आणि इलेक्ट्रिक स्कूटरसाठी GB/T निवडा.";
                res.options.add(new MenuOption("LIVE_PLUG_CCS2", "🔵", "CCS2 Plugs (Modern Indian Electric Cars)", "Live DB"));
                res.options.add(new MenuOption("LIVE_PLUG_GBT", "🟢", "GB/T Plugs (Scooters, Bikes & Fleet EVs)", "Live DB"));
                res.options.add(new MenuOption("LIVE_PLUG_CHADEMO", "🟣", "CHAdeMO Plugs (Global EV Models)", "Live DB"));
                break;

            case "LIVE_2W":
                res.parentId = "CAT_LIVE_SLOTS";
                res.breadcrumb = "Main Menu > Live Stations > 2-Wheeler Bays";
                res.title = "🛵 Live 2-Wheeler Charging Stations";
                dao.populateFilteredStations(res, null, "2-Wheeler", null, isAdmin, isUser);
                res.simpleNorm = "💡 Tap any station button below to inspect its exact Bay Numbers (A1, A2...) and see when busy slots become free!";
                res.normHi = "💡 किसी भी स्टेशन के अंदर के बे (A1, A2) देखने के लिए नीचे उस स्टेशन के नाम पर क्लिक करें।";
                res.normMr = "💡 कोणत्याही स्टेशनमधील बे (A1, A2) पाहण्यासाठी खालील स्टेशनच्या नावावर क्लिक करा.";
                break;

            case "LIVE_4W_FAST":
                res.parentId = "CAT_LIVE_SLOTS";
                res.breadcrumb = "Main Menu > Live Stations > 4-Wheeler Fast";
                res.title = "⚡ Live 4-Wheeler Fast Charging Stations";
                dao.populateFilteredStations(res, null, "4-Wheeler-Fast", null, isAdmin, isUser);
                res.simpleNorm = "💡 Fast DC bays charge most cars from 20% to 80% in 30–45 minutes. Tap any station below for bay-by-bay details.";
                res.normHi = "💡 फास्ट चार्जर से कार 30-45 मिनट में चार्ज हो जाती है। विस्तृत बे जानकारी के लिए नीचे स्टेशन पर क्लिक करें।";
                res.normMr = "💡 फास्ट चार्जरने कार ३०-४५ मिनिटांत चार्ज होते. अधिक माहितीसाठी खालील स्टेशनवर क्लिक करा.";
                break;

            case "LIVE_4W_NORMAL":
                res.parentId = "CAT_LIVE_SLOTS";
                res.breadcrumb = "Main Menu > Live Stations > 4-Wheeler Normal";
                res.title = "🚗 Live 4-Wheeler Normal Charging Stations";
                dao.populateFilteredStations(res, null, "4-Wheeler-Normal", null, isAdmin, isUser);
                res.simpleNorm = "💡 Normal AC bays are economical and ideal for 1 to 3 hour parking sessions. Tap a station below to inspect its bays.";
                res.normHi = "💡 नॉर्मल चार्जर 1 से 3 घंटे की पार्किंग के लिए सबसे किफायती विकल्प है।";
                res.normMr = "💡 १ ते ३ तास गाडी पार्क करायची असल्यास नॉर्मल चार्जर सर्वात उत्तम पर्याय आहे.";
                break;

            case "LIVE_PLUG_CCS2":
                res.parentId = "CAT_PLUG_FILTER";
                res.breadcrumb = "Main Menu > Live Stations > CCS2 Plugs";
                res.title = "🔵 Live Stations with CCS2 Connectors";
                dao.populateFilteredStations(res, "CCS2", null, null, isAdmin, isUser);
                res.simpleNorm = "💡 Tap any station below to see which exact bay numbers have CCS2 connectors open right now.";
                res.normHi = "💡 किस बे नंबर पर CCS2 प्लग खाली है, यह देखने के लिए नीचे स्टेशन पर क्लिक करें।";
                res.normMr = "💡 कोणत्या बे नंबरवर CCS2 प्लग रिकामा आहे हे पाहण्यासाठी खालील स्टेशनवर क्लिक करा.";
                break;

            case "LIVE_PLUG_GBT":
                res.parentId = "CAT_PLUG_FILTER";
                res.breadcrumb = "Main Menu > Live Stations > GB/T Plugs";
                res.title = "🟢 Live Stations with GB/T Connectors";
                dao.populateFilteredStations(res, "GB/T", null, null, isAdmin, isUser);
                res.simpleNorm = "💡 Tap any station below to inspect individual GB/T bays and live availability.";
                res.normHi = "💡 GB/T बे की लाइव स्थिति देखने के लिए नीचे किसी भी स्टेशन पर क्लिक करें।";
                res.normMr = "💡 GB/T स्लॉट्सची सद्यस्थिती पाहण्यासाठी खालील स्टेशनवर क्लिक करा.";
                break;

            case "LIVE_PLUG_CHADEMO":
                res.parentId = "CAT_PLUG_FILTER";
                res.breadcrumb = "Main Menu > Live Stations > CHAdeMO Plugs";
                res.title = "🟣 Live Stations with CHAdeMO Connectors";
                dao.populateFilteredStations(res, "CHAdeMO", null, null, isAdmin, isUser);
                res.simpleNorm = "💡 Tap any station below to see exact CHAdeMO bay numbers and status.";
                res.normHi = "💡 CHAdeMO बे की जानकारी के लिए नीचे स्टेशन पर क्लिक करें।";
                res.normMr = "💡 CHAdeMO स्लॉटची माहिती पाहण्यासाठी खालील स्टेशनवर क्लिक करा.";
                break;

            // =========================================================================
            // BRANCH 3: HOW TO BOOK, PRICING & "PREVIEW MODE" EXPLAINED
            // =========================================================================
            case "CAT_BOOKING_HELP":
                res.parentId = "ROOT";
                res.breadcrumb = "Main Menu > Booking & Pricing Guide";
                res.title = "📅 How to Book, Pricing & 'Preview Mode' Help";
                res.reply = "Everything you need to know about reserving a charging bay, unlocking the 'Book' button, and how fees work:";
                res.simpleNorm = "💡 Most popular question: 'Why is there no Book button on the Slots page?' — tap the second option below!";
                res.normHi = "💡 सबसे ज़रूरी सवाल: 'स्लॉट पेज पर Book बटन क्यों नहीं दिख रहा?' — जानने के लिए दूसरा विकल्प चुनें!";
                res.normMr = "💡 सर्वात महत्त्वाचा प्रश्न: 'स्लॉट पेजवर Book बटण का दिसत नाही?' — जाणून घेण्यासाठी दुसरा पर्याय निवडा!";

                res.options.add(new MenuOption("BOOK_STEPS", "✅", "Step-by-step: How do I book a charging slot?", "3 Easy Steps"));
                res.options.add(new MenuOption("BOOK_PREVIEW_WHY", "🔒", "Why does it say 'Preview Mode' with no Book button?", "Must Read"));
                res.options.add(new MenuOption("BOOK_PRICING", "💰", "How is the charging fee (₹) calculated?", "Tariff Guide"));
                res.options.add(new MenuOption("BOOK_BADGES", "🚦", "What do Available, Reserved & Offline badges mean?", "Status Colors"));
                res.options.add(new MenuOption("BOOK_OVERLAP_RULE", "🛡️", "Can someone else take my bay during my booked time?", "Conflict Rule"));
                res.options.add(new MenuOption("BOOK_LATE_RULE", "⏰", "What happens if I arrive late or want to extend?", "Time Buffer"));
                break;

            case "BOOK_STEPS":
                res.parentId = "CAT_BOOKING_HELP";
                res.breadcrumb = "Main Menu > Booking Help > 3-Step Guide";
                res.title = "✅ How to Book a Charging Slot in 3 Easy Steps";
                res.reply = "Here is the exact path to reserve a charging bay on Plugy:";
                res.bullets.add("Step 1 — Open 'Search Charging Points' (Search.jsp): Select your City/Area or Station, and enter your Date, Start Time, and End Time.");
                res.bullets.add("Step 2 — Pick a Green 'Available' Bay (SearchSlot.jsp): Click 'View Slots'. Because you entered a time window, every free bay now displays an active 'Book' button!");
                res.bullets.add("Step 3 — Enter Vehicle & Payment Details: Click 'Book', enter your Vehicle Registration Number (e.g., MH12AB1234), review the calculated amount (₹), and submit.");
                res.simpleNorm = "💡 Why Step 1 Is Mandatory: You must always start from 'Search Charging Points' so the system knows your Start Time and End Time before showing the Book button.";
                res.normHi = "💡 ध्यान दें: हमेशा 'Search Charging Points' पेज से शुरुआत करें और तारीख व समय चुनें, तभी स्लॉट के आगे 'Book' बटन खुलेगा।";
                res.normMr = "💡 महत्त्वाचे: नेहमी 'Search Charging Points' पेजवरून सुरुवात करा आणि तारीख व वेळ निवडा, तरच स्लॉटसमोर 'Book' बटण दिसेल.";
                res.actions.add(new ActionLink("🔍 Start Step 1: Open Search Page", "Search.jsp", "emerald"));
                if (!isUser && !isAdmin) {
                    res.actions.add(new ActionLink("🔑 User Login (Required to Book)", "UserLogin.jsp", "primary"));
                }
                break;

            case "BOOK_PREVIEW_WHY":
                res.parentId = "CAT_BOOKING_HELP";
                res.breadcrumb = "Main Menu > Booking Help > Preview Mode Explained";
                res.title = "🔒 Why Is There No 'Book' Button on the Slots Page?";
                res.reply = "If you click 'Book Slot' (SearchSlot.jsp) directly in the top menu bar without picking a Date and Time first, the page opens in 'Preview Mode':";
                res.bullets.add("👀 What Preview Mode Is: A read-only live board that shows what each bay is doing right this second.");
                res.bullets.add("❓ Why Can't I Click Book Here? Because you haven't told the system WHICH date and WHAT time (e.g., 4:00 PM to 5:00 PM) you want the charger! Without a time window, the database cannot check if another car is already booked at that hour.");
                res.bullets.add("✅ 10-Second Fix: Click 'Select Date & Time Now' below, choose your Date, Start Time, and End Time on Search.jsp, and click View Slots — the 'Book' buttons will immediately appear!");
                res.simpleNorm = "💡 Everyday Comparison: Just like BookMyShow won't let you pick a seat until you choose the Movie Date and Showtime, Plugy unlocks the 'Book' button as soon as you pick your Date & Time.";
                res.normHi = "💡 त्वरित समाधान: नीचे दिए गए हरे बटन 'Select Date & Time Now' पर क्लिक करें, अपना समय भरें, और 'Book' बटन तुरंत खुल जाएगा!";
                res.normMr = "💡 झटपट उपाय: खालील हिरव्या 'Select Date & Time Now' बटणावर क्लिक करा, तुमची वेळ निवडा आणि 'Book' बटण लगेच सुरू होईल!";
                res.actions.add(new ActionLink("📅 Select Date & Time Now (Unlock Book Button)", "Search.jsp", "emerald"));
                break;

            case "BOOK_PRICING":
                res.parentId = "CAT_BOOKING_HELP";
                res.breadcrumb = "Main Menu > Booking Help > Pricing & Tariff";
                res.title = "💰 How Is the Charging Fee (₹) Calculated?";
                res.reply = "On Plugy, your charging cost is calculated transparently before you pay, based on three factors:";
                res.bullets.add("1️⃣ Vehicle & Charger Speed: 2-Wheeler bays have the lowest base tariff, followed by 4-Wheeler-Normal, and 4-Wheeler-Fast DC.");
                res.bullets.add("2️⃣ Session Duration & Battery Size: The time length between your Start Time and End Time combined with your vehicle's battery capacity.");
                res.bullets.add("3️⃣ 100% Upfront Price: You see the exact Rupee (₹) total on the booking form before clicking Pay — zero hidden service fees.");
                res.simpleNorm = "💡 Everyday Comparison: Charging an electric scooter costs around ₹15–₹30 per session, and charging an electric car costs roughly ₹1.20 per km (75% cheaper than petrol!).";
                res.normHi = "💡 सरल जानकारी: पेट्रोल की तुलना में इलेक्ट्रिक चार्जिंग का खर्च लगभग एक-चौथाई (75% सस्ता) आता है।";
                res.normMr = "💡 सोपी माहिती: पेट्रोलच्या तुलनेत इलेक्ट्रिक चार्जिंगचा खर्च फक्त एक-चतुर्थांश (७५% स्वस्त) येतो.";
                addStandardSearchAction(res, isAdmin, isUser);
                break;

            case "BOOK_BADGES":
                res.parentId = "CAT_BOOKING_HELP";
                res.breadcrumb = "Main Menu > Booking Help > Status Badges";
                res.title = "🚦 What Do Available, Reserved & Offline Mean?";
                res.reply = "Every slot row in Plugy displays a color-coded status badge:";
                res.bullets.add("🟢 Available (Green): The charger is healthy and nobody has booked your selected time window. Ready to book!");
                res.bullets.add("🟠 Reserved / Occupied (Amber): Another driver has an active booking during that time window. Pick another green bay or shift your time by 1 hour.");
                res.bullets.add("🔴 Offline (Red): The charger is powered down or disconnected and is blocked from booking.");
                res.bullets.add("🛠️ In Maintenance (Grey/Amber): Technicians are servicing this bay. It will reopen once the Admin marks it Available.");
                res.simpleNorm = "💡 Tip: Want to see when a 'Reserved Now' bay becomes free? Use 'Browse by Area' in this chatbot and tap the station — I will show you the exact time the current session ends!";
                res.normHi = "💡 सुझाव: यदि कोई स्लॉट 'Reserved' है, तो आप इस चैटबॉट में स्टेशन पर क्लिक करके देख सकते हैं कि वह कितने बजे खाली होगा!";
                res.normMr = "💡 टीप: एखादा स्लॉट 'Reserved' असल्यास तो किती वाजता रिकामा होईल हे पाहण्यासाठी या चॅटबॉटमध्ये स्टेशनवर क्लिक करा!";
                res.options.add(new MenuOption("LIVE_ALL_AREAS", "📍", "Check Live Bay Timers by Area", "Live DB"));
                break;

            case "BOOK_OVERLAP_RULE":
                res.parentId = "CAT_BOOKING_HELP";
                res.breadcrumb = "Main Menu > Booking Help > Double-Booking Protection";
                res.title = "🛡️ Can Two Drivers Accidentally Book the Same Slot?";
                res.reply = "No — Plugy enforces automatic SQL time-overlap protection on every reservation:";
                res.bullets.add("🔒 Exclusive Time Lock: Once you book Bay A1 from 4:00 PM to 5:00 PM, the database locks Bay A1 for that entire hour.");
                res.bullets.add("🚫 Zero Double-Booking: Any other user searching between 4:00 PM and 5:00 PM will see Bay A1 as 'Reserved' and cannot book over your slot.");
                res.bullets.add("✅ Back-to-Back Allowed: Another driver CAN book 3:00 PM to 4:00 PM or 5:00 PM to 6:00 PM on the same bay without conflict.");
                res.simpleNorm = "💡 Everyday Peace of Mind: Your reserved bay is 100% yours for your booked time window — no queue jumping is possible.";
                res.normHi = "💡 सुरक्षा गारंटी: एक बार जब आप कोई समय बुक कर लेते हैं, तो उस समय के दौरान कोई दूसरा व्यक्ति वह स्लॉट बुक नहीं कर सकता।";
                res.normMr = "💡 सुरक्षितता हमी: तुम्ही एकदा वेळ बुक केल्यावर, त्या वेळेत इतर कोणीही तो स्लॉट बुक करू शकत नाही.";
                break;

            case "BOOK_LATE_RULE":
                res.parentId = "CAT_BOOKING_HELP";
                res.breadcrumb = "Main Menu > Booking Help > Late Arrival & Buffer";
                res.title = "⏰ What If I Arrive Late due to Traffic?";
                res.reply = "Here is how your reservation window works if you get delayed on the road:";
                res.bullets.add("⏳ Your Bay Waits for You: Your booked bay remains reserved in your name from your Start Time right until your End Time.");
                res.bullets.add("⚠️ Remaining Time Applies: If you booked 5:00 PM to 6:00 PM and arrive at 5:20 PM, you can still charge for the remaining 40 minutes until 6:00 PM.");
                res.bullets.add("🚗 Smart Traffic Tip: Driving through Hinjewadi, Wakad, Mundhwa, or Chandni Chowk during peak hours? Add an extra 15–30 minutes to your booking window so you aren't rushed!");
                res.simpleNorm = "💡 Everyday Tip: Always add a 15-minute buffer to your End Time when travelling during evening peak traffic.";
                res.normHi = "💡 सरल सुझाव: शाम के ट्रैफिक में निकलते समय अपनी बुकिंग में 15-20 मिनट का अतिरिक्त समय ज़रूर जोड़ें।";
                res.normMr = "💡 सोपी टीप: संध्याकाळच्या ट्रॅफिकमध्ये प्रवास करताना बुकिंगच्या वेळेत १५-२० मिनिटे जादा वेळ नक्की ठेवा.";
                res.actions.add(new ActionLink("🧾 View My Bookings", "MyBookings.jsp", "primary"));
                break;

            // =========================================================================
            // BRANCH 4: MY BOOKINGS, LOGIN & PASSWORD HELP
            // =========================================================================
            case "CAT_ACCOUNT":
                res.parentId = "ROOT";
                res.breadcrumb = "Main Menu > My Bookings & Account";
                res.title = "🧾 My Bookings, Login & Account Help";
                res.reply = "Check your live charging reservations or get instant help with registration and password rules:";
                res.simpleNorm = "💡 Tap the first option below to pull your latest bookings and remaining charging time directly from the database!";
                res.normHi = "💡 अपनी लाइव बुकिंग और बचा हुआ चार्जिंग समय देखने के लिए पहले विकल्प पर क्लिक करें।";
                res.normMr = "💡 तुमचे लाईव्ह बुकिंग आणि उरलेला चार्जिंग वेळ पाहण्यासाठी पहिल्या पर्यायावर क्लिक करा.";

                res.options.add(new MenuOption("ACC_MY_BOOKINGS", "🧾", "Show My Active, Upcoming & Past Bookings", "Live DB"));
                res.options.add(new MenuOption("ACC_HOW_LOGIN", "🔑", "Where do I Log In or Create an Account?", "Portals"));
                res.options.add(new MenuOption("ACC_PASS_RULES", "🛡️", "Why is my Password or Email rejected on Login?", "Format Fix"));
                res.options.add(new MenuOption("ACC_RECEIPTS", "💳", "How do I check my Payment History & Vehicle Number?", "Receipts"));
                break;

            case "ACC_MY_BOOKINGS":
                res.parentId = "CAT_ACCOUNT";
                res.breadcrumb = "Main Menu > Account > My Live Bookings";
                dao.populateUserBookings(res, userEmail);
                break;

            case "ACC_HOW_LOGIN":
                res.parentId = "CAT_ACCOUNT";
                res.breadcrumb = "Main Menu > Account > Login & Registration";
                res.title = "🔑 How to Create an Account or Log In";
                res.reply = "Plugy provides dedicated portals for EV Drivers and Station Administrators:";
                res.bullets.add("📝 New EV Driver (UserRegister.jsp): Click 'Create Free User Account' below. Fill in your Name, Email, Mobile Number, and Password to register in 30 seconds.");
                res.bullets.add("👤 Existing EV Driver (UserLogin.jsp): Click 'User Login' below and sign in with your registered Email and Password.");
                res.bullets.add("🛡️ Station Operator (AdminLogin.jsp): Click 'Admin Login' to access the executive dashboard, station manager, and slot controls.");
                res.simpleNorm = "💡 Tap any shortcut button below to go straight to that login or signup screen.";
                res.normHi = "💡 लॉग-इन या नया अकाउंट बनाने के लिए नीचे दिए गए बटन पर क्लिक करें।";
                res.normMr = "💡 लॉग-इन किंवा नवीन खाते उघडण्यासाठी खालील बटणावर क्लिक करा.";
                res.actions.add(new ActionLink("🔑 Open User Login", "UserLogin.jsp", "emerald"));
                res.actions.add(new ActionLink("📝 Create Free User Account", "UserRegister.jsp", "primary"));
                res.actions.add(new ActionLink("🛡️ Open Admin Login", "AdminLogin.jsp", "outline"));
                break;

            case "ACC_PASS_RULES":
                res.parentId = "CAT_ACCOUNT";
                res.breadcrumb = "Main Menu > Account > Password & Email Rules";
                res.title = "🛡️ Why Is My Email or Password Not Accepted?";
                res.reply = "Our User Login and Registration forms use strict browser security validation. Make sure your input follows these exact rules:";
                res.bullets.add("📧 Email Must Be Lowercase: Enter your email in all lowercase letters (e.g., rohit@gmail.com, not Rohit@Gmail.com).");
                res.bullets.add("🔢 Minimum 8 Characters: Your password must be at least 8 characters long.");
                res.bullets.add("🔤 Required Mix: Your password MUST contain at least 1 Uppercase letter (A–Z), 1 Lowercase letter (a–z), and 1 Number (0–9) or Special Symbol (@, #, $, !).");
                res.bullets.add("👁️ Use the Eye Button: Click the 'Eye' icon on the right side of the password box to check for accidental spaces or typos.");
                res.simpleNorm = "💡 Valid Example: Plugy@123 or EvCharge#9 (Both have a capital letter, small letters, a symbol, a number, and 8+ characters).";
                res.normHi = "💡 सही पासवर्ड उदाहरण: Plugy@123 (इसमें बड़ा अक्षर P, छोटे अक्षर, @ चिह्न और नंबर 123 शामिल हैं, कुल 9 अक्षर)। ईमेल हमेशा छोटे अक्षरों (lowercase) में लिखें।";
                res.normMr = "💡 योग्य पासवर्ड उदाहरण: Plugy@123 (यात कॅपिटल अक्षर, स्मॉल अक्षरे, @ चिन्ह आणि अंक आहेत). ईमेल नेहमी स्मॉल लेटर्समध्ये (lowercase) टाका.";
                res.actions.add(new ActionLink("🔑 Try User Login Again", "UserLogin.jsp", "emerald"));
                res.actions.add(new ActionLink("📝 Open User Registration", "UserRegister.jsp", "primary"));
                break;

            case "ACC_RECEIPTS":
                res.parentId = "CAT_ACCOUNT";
                res.breadcrumb = "Main Menu > Account > Payment History";
                res.title = "💳 Where Can I View My Payment & Booking Receipts?";
                res.reply = "Every booking you confirm is recorded permanently in our payment table (tbl_payment):";
                res.bullets.add("1️⃣ Sign in to your User account and click 'My Booking' in the top navigation bar.");
                res.bullets.add("2️⃣ Your reservation table shows the Station Name, Area, Bay Label, Charger Type, Vehicle Number, Start/End Time, Live Status, and Amount Paid (₹).");
                res.bullets.add("3️⃣ Or simply tap 'Show My Active & Past Bookings' below to view them right inside this chat!");
                res.simpleNorm = "💡 Tap the button below to check your bookings right now.";
                res.normHi = "💡 अपनी पेमेंट और बुकिंग देखने के लिए नीचे दिए गए बटन पर क्लिक करें।";
                res.normMr = "💡 तुमचे पेमेंट आणि बुकिंग पाहण्यासाठी खालील बटणावर क्लिक करा.";
                res.options.add(new MenuOption("ACC_MY_BOOKINGS", "🧾", "Check My Bookings Inside Chat Now", "Live DB"));
                res.actions.add(new ActionLink("🧾 Open My Bookings Page", "MyBookings.jsp", "primary"));
                break;

            // =========================================================================
            // BRANCH 5: ON-SITE TROUBLESHOOTING & CUSTOMER SUPPORT
            // =========================================================================
            case "CAT_TROUBLE":
                res.parentId = "ROOT";
                res.breadcrumb = "Main Menu > Troubleshooting & Support";
                res.title = "🛠️ On-Site Troubleshooting & Support";
                res.reply = "At a station right now and need quick help? Select the issue you are facing:";
                res.simpleNorm = "💡 90% of charging issues at the bay are solved in under 30 seconds using the first guide below.";
                res.normHi = "💡 स्टेशन पर चार्जिंग शुरू न होने की समस्या अक्सर 30 सेकंड में नीचे दिए गए पहले उपाय से ठीक हो जाती है।";
                res.normMr = "💡 स्टेशनवर चार्जिंग सुरू न होण्याची अडचण खालील पहिल्या उपायाने ३० सेकंदात सुटते.";

                res.options.add(new MenuOption("TRB_NOT_STARTING", "🔌", "I plugged in my vehicle, but charging won't start", "4 Quick Fixes"));
                res.options.add(new MenuOption("TRB_CABLE_LOCKED", "🔓", "How do I unplug the cable when charging finishes?", "Unlock Plug"));
                res.options.add(new MenuOption("TRB_OFFLINE_BAY", "🔴", "The bay at the station says 'Offline' or 'Maintenance'", "Switch Bay"));
                res.options.add(new MenuOption("TRB_CONTACT", "📞", "Contact Plugy Support Desk (Phone / Email / Form)", "24x7 Desk"));
                break;

            case "TRB_NOT_STARTING":
                res.parentId = "CAT_TROUBLE";
                res.breadcrumb = "Main Menu > Troubleshooting > Not Starting";
                res.title = "🔌 Charging Won't Start? Do These 4 Quick Checks";
                res.reply = "If you connected the plug to your EV and charging hasn't started within 20 seconds:";
                res.bullets.add("1️⃣ Turn OFF Your Vehicle Ignition Completely: Almost all EVs block charging if the dashboard is still in 'READY / DRIVE' mode. Switch the car/scooter off and lock the doors.");
                res.bullets.add("2️⃣ Push the Plug Firmly Until It Clicks: Fast-charger cables (especially CCS2) are heavy. Lift the handle slightly and push firmly until you hear the lock click.");
                res.bullets.add("3️⃣ Check the Red Emergency Stop Knob: Look at the charging machine — if the round red Emergency Stop button is pushed in, twist it clockwise so it pops back out.");
                res.bullets.add("4️⃣ Unplug, Wait 15 Seconds & Re-Plug: Reset the digital handshake by unplugging for 15 seconds and reconnecting.");
                res.simpleNorm = "💡 Everyday Tip: Leaving the car ignition ON or not pushing the heavy CCS2 gun all the way in causes 9 out of 10 connection failures!";
                res.normHi = "💡 सबसे आम कारण: गाड़ी की चाबी/इग्निशन पूरी तरह बंद करें और दरवाज़े लॉक करें, फिर प्लग को कसकर लगाएँ।";
                res.normMr = "💡 सर्वात महत्त्वाचे: गाडीचे इग्निशन पूर्णपणे बंद करा, दरवाजे लॉक करा आणि प्लग घट्ट दाबा.";
                res.actions.add(new ActionLink("📞 Still Stuck? Contact Support", "index.jsp#contact", "primary"));
                break;

            case "TRB_CABLE_LOCKED":
                res.parentId = "CAT_TROUBLE";
                res.breadcrumb = "Main Menu > Troubleshooting > Unplug Cable";
                res.title = "🔓 Cable Won't Come Out? How to Unlock the EV Plug";
                res.reply = "Never pull a locked EV cable by force! For safety against theft and sparks, your vehicle locks the plug from the inside:";
                res.bullets.add("🔑 Step 1 — Unlock Your Vehicle Doors: Press the 'Unlock' button on your car key fob (or dashboard). In Tata, MG, Hyundai, and Mahindra EVs, unlocking the doors releases the charging port lock for 15 seconds.");
                res.bullets.add("🛑 Step 2 — Stop Session on Dashboard/Screen: If your battery isn't at 100% yet, press 'Stop Charging' on your car's infotainment screen first.");
                res.bullets.add("🔌 Step 3 — Press the Thumb Latch & Pull Gently: Once you hear the small motor click inside the charging port, press the button on the plug handle and pull it out smoothly.");
                res.simpleNorm = "💡 Everyday Tip: Pressing 'Unlock' twice on your car key remote immediately releases a stuck CCS2 charging gun!";
                res.normHi = "💡 सरल उपाय: अपनी कार की चाबी (Remote) पर 'Unlock' बटन दो बार दबाएँ — चार्जिंग प्लग का लॉक तुरंत खुल जाएगा!";
                res.normMr = "💡 सोपा उपाय: तुमच्या कारच्या चावीवरील (Remote) 'Unlock' बटण दोनदा दाबा — चार्जिंग प्लगचे लॉक लगेच उघडेल!";
                break;

            case "TRB_OFFLINE_BAY":
                res.parentId = "CAT_TROUBLE";
                res.breadcrumb = "Main Menu > Troubleshooting > Offline Bay";
                res.title = "🔴 What If a Station Bay Is Offline or In Maintenance?";
                res.reply = "If a bay is marked Offline or In Maintenance:";
                res.bullets.add("🔄 Use Any Neighboring Green Bay: Most Plugy hubs have multiple bays (A1, A2, B1, B2). Check the live bay breakdown here in the chatbot and pick an Available bay at the same station.");
                res.bullets.add("📍 Check Nearby Hub in Same Area: Tap 'Browse All Covered Areas' below to see other stations within 2–3 km.");
                res.simpleNorm = "💡 Note: Our system automatically disables booking on Offline bays so you never pay for a machine that is down.";
                res.normHi = "💡 जानकारी: ऑफलाइन मशीनों पर बुकिंग अपने आप लॉक हो जाती है ताकि आपका भुगतान सुरक्षित रहे।";
                res.normMr = "💡 माहिती: बंद असलेल्या मशीनवर बुकिंग आपोआप थांबवले जाते जेणेकरून तुमचे पैसे सुरक्षित राहतात.";
                res.options.add(new MenuOption("LIVE_ALL_AREAS", "📍", "Find Another Free Bay Nearby", "Live DB"));
                break;

            case "TRB_CONTACT":
                res.parentId = "CAT_TROUBLE";
                res.breadcrumb = "Main Menu > Troubleshooting > Contact Support";
                res.title = "📞 Plugy 24x7 Customer Support Desk";
                res.reply = "Need assistance with a booking, payment, or charging station in Pune & PCMC? Reach out to us directly:";
                res.bullets.add("📍 Operations Center: Shivajinagar, Pune, Maharashtra 411005");
                res.bullets.add("☎️ 24x7 EV Helpline: +91 20 2550 0000");
                res.bullets.add("📧 Support Email: support@plugy.in / info@plugy.com");
                res.bullets.add("✉️ Online Inquiry Form: Click 'Open Contact Form' below to send a message directly from the home page.");
                res.simpleNorm = "💡 Tip: Mention your Vehicle Number and Station Name so our support engineer can assist you immediately.";
                res.normHi = "💡 सुझाव: सहायता केंद्र पर संपर्क करते समय अपना वाहन नंबर और स्टेशन का नाम बताएं।";
                res.normMr = "💡 टीप: सपोर्ट टीमशी संपर्क साधताना तुमचा गाडी क्रमांक आणि स्टेशनचे नाव सांगा.";
                res.actions.add(new ActionLink("✉️ Open Contact Us Form", "index.jsp#contact", "emerald"));
                res.actions.add(new ActionLink("🏠 Go to Home Page", "index.jsp", "primary"));
                break;

            // =========================================================================
            // BRANCH 6: ADMIN COMMAND CENTER & WORKFLOW GUIDES
            // =========================================================================
            case "CAT_ADMIN":
                res.parentId = "ROOT";
                res.breadcrumb = "Main Menu > Admin Live Telemetry & Audits";
                res.title = "📊 Admin Live Telemetry & Hardware Audits";
                res.reply = "Select an administrative audit below to query live MySQL records from tbl_station, tbl_slot, and tbl_payment:";
                res.simpleNorm = "💡 All reports below run live SQL queries against your production tables.";
                res.normHi = "💡 नीचे दी गई सभी रिपोर्ट सीधे आपके लाइव MySQL डेटाबेस से आती हैं।";
                res.normMr = "💡 खालील सर्व रिपोर्ट्स थेट तुमच्या लाईव्ह MySQL डेटाबेसवरून येतात.";

                res.options.add(new MenuOption("ADMIN_TELEMETRY", "💰", "Executive Telemetry: Revenue, Active Load & Top Hub", "Live SQL"));
                res.options.add(new MenuOption("ADMIN_HARDWARE", "🛠️", "Hardware Audit: Offline Bays & Empty Stations", "Live SQL"));
                res.options.add(new MenuOption("LIVE_ALL_AREAS", "📍", "Inspect Stations & Individual Bays by Area", "Drill-Down"));
                res.options.add(new MenuOption("CAT_ADMIN_GUIDE", "🏢", "Admin Workflow Guide: Managing Stations & Slots", "How-To"));
                break;

            case "ADMIN_TELEMETRY":
                res.parentId = "CAT_ADMIN";
                res.breadcrumb = "Main Menu > Admin > Live Telemetry";
                dao.populateAdminTelemetry(res);
                break;

            case "ADMIN_HARDWARE":
                res.parentId = "CAT_ADMIN";
                res.breadcrumb = "Main Menu > Admin > Hardware Audit";
                dao.populateAdminHardwareAudit(res);
                break;

            case "CAT_ADMIN_GUIDE":
                res.parentId = isAdmin ? "ROOT" : "CAT_ADMIN";
                res.breadcrumb = "Main Menu > Admin Workflow Guide";
                res.title = "🏢 Admin Guide: Managing Stations, Bays & Bookings";
                res.reply = "Select a management task below for step-by-step instructions and direct admin page shortcuts:";
                res.simpleNorm = "💡 Every guide includes a 1-click button to jump straight to that admin screen.";
                res.normHi = "💡 हर गाइड के साथ सीधे उस एडमिन पेज पर जाने का बटन दिया गया है।";
                res.normMr = "💡 प्रत्येक गाईडसोबत थेट त्या ॲडमिन पेजवर जाण्यासाठी बटण दिले आहे.";

                res.options.add(new MenuOption("ADMIN_HOW_MANAGE", "➕", "How do I Add a New Station or Configure Bays?", "Stations.jsp"));
                res.options.add(new MenuOption("ADMIN_HOW_STATUS", "🔄", "How do I Toggle a Bay Between Available & Offline?", "ManageStation"));
                res.options.add(new MenuOption("ADMIN_HOW_BOOKINGS", "📋", "How do I View Customer Bookings for a Station?", "StationBookings"));
                break;

            case "ADMIN_HOW_MANAGE":
                res.parentId = "CAT_ADMIN_GUIDE";
                res.breadcrumb = "Main Menu > Admin Guide > Add Stations & Bays";
                res.title = "➕ How to Add Stations & Configure Charging Bays";
                res.reply = "Follow this workflow to expand your charging network:";
                res.bullets.add("1️⃣ Create the Station (Stations.jsp): Open 'Stations', enter the Station Name, City, Area, Address, and Contact Number, and submit.");
                res.bullets.add("2️⃣ Open Bay Manager (ManageStation.jsp): In the Stations table, click the 'Manage' button next to your station.");
                res.bullets.add("3️⃣ Add Individual Bays: Add slots with clear labels (A1, A2, B1), choose Vehicle Type (2-Wheeler, 4-Wheeler-Normal, 4-Wheeler-Fast), select Connector (CCS2, CHAdeMO, GB/T), and set Status to 'Available'.");
                res.simpleNorm = "💡 Admin Rule: A newly created station needs at least 1 Available slot in ManageStation.jsp before customers can book it.";
                res.normHi = "💡 एडमिन नियम: नया स्टेशन बनाने के बाद 'Manage' पर क्लिक करके कम से कम 1 स्लॉट ज़रूर जोड़ें ताकि ग्राहक बुकिंग कर सकें।";
                res.normMr = "💡 ॲडमिन नियम: नवीन स्टेशन तयार केल्यानंतर 'Manage' वर क्लिक करून किमान १ स्लॉट जोडा जेणेकरून ग्राहक बुकिंग करू शकतील.";
                res.actions.add(new ActionLink("🏢 Open Stations Manager", "Stations.jsp", "emerald"));
                res.actions.add(new ActionLink("🔌 View All Slots Table", "ViewSlots.jsp", "primary"));
                break;

            case "ADMIN_HOW_STATUS":
                res.parentId = "CAT_ADMIN_GUIDE";
                res.breadcrumb = "Main Menu > Admin Guide > Toggle Bay Status";
                res.title = "🔄 How to Put a Charger In Maintenance or Back Online";
                res.reply = "When a physical charger needs servicing or comes back online:";
                res.bullets.add("1️⃣ Go to 'Stations' (Stations.jsp) and click 'Manage' on that station.");
                res.bullets.add("2️⃣ Locate the bay (e.g., Bay A2) and change its status dropdown between 'Available', 'Offline', or 'In maintenance'.");
                res.bullets.add("3️⃣ Instant Sync: Marking a bay 'Offline' or 'In maintenance' immediately blocks new user bookings on SearchSlot.jsp and flags it in ViewSlots.jsp.");
                res.simpleNorm = "💡 Run the 'Hardware Audit' below anytime to see all currently Offline or Maintenance bays across your network.";
                res.normHi = "💡 किसी भी स्लॉट को 'Offline' करते ही यूज़र पेज पर उसकी बुकिंग तुरंत बंद हो जाती है।";
                res.normMr = "💡 कोणताही स्लॉट 'Offline' करताच युजर पेजवर त्याचे बुकिंग तात्काळ बंद होते.";
                res.options.add(new MenuOption("ADMIN_HARDWARE", "🛠️", "Run Live Hardware Fault Audit Now", "Live SQL"));
                res.actions.add(new ActionLink("🏢 Go to Stations List", "Stations.jsp", "primary"));
                break;

            case "ADMIN_HOW_BOOKINGS":
                res.parentId = "CAT_ADMIN_GUIDE";
                res.breadcrumb = "Main Menu > Admin Guide > Inspect Bookings";
                res.title = "📋 How to Monitor Customer Reservations & Revenue";
                res.reply = "Plugy gives administrators two dedicated views for monitoring customer bookings:";
                res.bullets.add("📊 Network-Wide Dashboard (AdminHome.jsp): Displays total revenue, today's collection, active charging sessions right now, top stations, and recent bookings.");
                res.bullets.add("🏢 Per-Station Booking Ledger (StationBookings.jsp): Open 'Stations' and click 'Bookings' on any row to see all reservations, vehicle numbers, time windows, and payments for that specific hub.");
                res.simpleNorm = "💡 Active sessions are calculated automatically whenever the current clock time is between start_time and end_time.";
                res.normHi = "💡 किसी विशेष स्टेशन की सारी बुकिंग देखने के लिए 'Stations' पेज पर उस स्टेशन के आगे 'Bookings' बटन दबाएँ।";
                res.normMr = "💡 विशिष्ट स्टेशनचे सर्व बुकिंग पाहण्यासाठी 'Stations' पेजवर त्या स्टेशनसमोर 'Bookings' बटण दाबा.";
                res.actions.add(new ActionLink("📊 Open Executive Dashboard", "AdminHome.jsp", "emerald"));
                res.actions.add(new ActionLink("🏢 Open Stations List", "Stations.jsp", "primary"));
                break;
        }

        return res;
    }

    // =========================================================================
    // SMART QUICK-SEARCH BAR ROUTER (Maps Typed Areas, Car Models & Keywords)
    // =========================================================================

    private BotResult handleQuickSearchInput(String query, String role, String userEmail, String currentPage) {
        String lower = query.toLowerCase();
        boolean isAdmin = "ADMIN".equals(role);
        boolean isUser = "USER".equals(role);

        // 1. Preview Mode / Missing Book Button
        if (lower.contains("preview") || lower.contains("no book") || lower.contains("cant book")
                || lower.contains("can't book") || lower.contains("button missing") || lower.contains("unlock")) {
            return routeNode("BOOK_PREVIEW_WHY", role, userEmail, currentPage, null);
        }

        // 2. How to Book
        if (lower.contains("how to book") || lower.contains("book step") || lower.contains("how do i book")
                || lower.equals("book") || lower.equals("booking")) {
            return routeNode("BOOK_STEPS", role, userEmail, currentPage, null);
        }

        // 3. Pricing / Tariff / Cost
        if (lower.contains("price") || lower.contains("cost") || lower.contains("rate")
                || lower.contains("tariff") || lower.contains("fee") || lower.contains("rupee") || lower.contains("how much")) {
            return routeNode("BOOK_PRICING", role, userEmail, currentPage, null);
        }

        // 4. Car / Scooter Brand Compatibility Lookup (Beginner-friendly!)
        if (lower.contains("tata") || lower.contains("nexon") || lower.contains("punch") || lower.contains("tiago")
                || lower.contains("curvv") || lower.contains("mg") || lower.contains("zs") || lower.contains("windsor")
                || lower.contains("comet") || lower.contains("mahindra") || lower.contains("xuv") || lower.contains("hyundai")
                || lower.contains("ioniq") || lower.contains("byd") || lower.contains("ola") || lower.contains("ather")
                || lower.contains("iqube") || lower.contains("chetak") || lower.contains("which plug") || lower.contains("connector")) {
            return routeNode("BEG_PLUG_TYPES", role, userEmail, currentPage, null);
        }

        // 5. Battery kWh / Units
        if (lower.contains("kwh") || lower.contains("unit") || lower.contains("battery size") || lower.contains("capacity")) {
            return routeNode("BEG_BATTERY_KWH", role, userEmail, currentPage, null);
        }

        // 6. Rain / Safety
        if (lower.contains("rain") || lower.contains("water") || lower.contains("shock") || lower.contains("safe")) {
            return routeNode("BEG_RAIN_SAFETY", role, userEmail, currentPage, null);
        }

        // 7. Cable Stuck / Unplug
        if (lower.contains("stuck") || lower.contains("unplug") || lower.contains("remove cable") || lower.contains("locked")) {
            return routeNode("TRB_CABLE_LOCKED", role, userEmail, currentPage, null);
        }

        // 8. Not Starting
        if (lower.contains("not starting") || lower.contains("wont start") || lower.contains("won't start") || lower.contains("not charging")) {
            return routeNode("TRB_NOT_STARTING", role, userEmail, currentPage, null);
        }

        // 9. Personal Bookings
        if (lower.contains("my booking") || lower.contains("my slot") || lower.contains("my reservation") || lower.contains("receipt")) {
            return routeNode("ACC_MY_BOOKINGS", role, userEmail, currentPage, null);
        }

        // 10. Password / Login Rules
        if (lower.contains("password") || lower.contains("invalid") || lower.contains("pattern")) {
            return routeNode("ACC_PASS_RULES", role, userEmail, currentPage, null);
        }
        if (lower.contains("login") || lower.contains("sign in") || lower.contains("register") || lower.contains("signup")) {
            return routeNode("ACC_HOW_LOGIN", role, userEmail, currentPage, null);
        }

        // 11. Contact / Helpline
        if (lower.contains("contact") || lower.contains("phone") || lower.contains("helpline") || lower.contains("support") || lower.contains("email")) {
            return routeNode("TRB_CONTACT", role, userEmail, currentPage, null);
        }

        // 12. Admin Shortcuts
        if (isAdmin && (lower.contains("revenue") || lower.contains("collection") || lower.contains("earning") || lower.contains("telemetry"))) {
            return routeNode("ADMIN_TELEMETRY", role, userEmail, currentPage, null);
        }
        if (isAdmin && (lower.contains("offline") || lower.contains("maintenance") || lower.contains("broken") || lower.contains("fault"))) {
            return routeNode("ADMIN_HARDWARE", role, userEmail, currentPage, null);
        }

        // 13. Default: Run a Live Database Station/Area/Plug Search (e.g., "Hadapsar", "Wakad", "CCS2", "2-Wheeler")
        BotResult searchRes = new BotResult();
        searchRes.nodeId = "SEARCH_RESULT";
        searchRes.parentId = "CAT_LIVE_SLOTS";
        searchRes.breadcrumb = "Main Menu > Search: " + query;
        searchRes.title = "🔍 Live Database Results for '" + query + "'";

        String chargerFilter = null;
        if (lower.contains("ccs")) chargerFilter = "CCS2";
        else if (lower.contains("chademo")) chargerFilter = "CHAdeMO";
        else if (lower.contains("gb")) chargerFilter = "GB/T";

        String vehicleFilter = null;
        if (lower.contains("2-wheeler") || lower.contains("2 wheeler") || lower.contains("2w")
                || lower.contains("bike") || lower.contains("scooter")) {
            vehicleFilter = "2-Wheeler";
        } else if (lower.contains("fast")) {
            vehicleFilter = "4-Wheeler-Fast";
        } else if (lower.contains("normal")) {
            vehicleFilter = "4-Wheeler-Normal";
        }

        dao.populateFilteredStations(searchRes, chargerFilter, vehicleFilter, query, isAdmin, isUser);
        searchRes.simpleNorm = "💡 Tap any station button above to inspect its individual bays (A1, A2...), or browse all areas below.";
        searchRes.normHi = "💡 ऊपर दिए गए किसी भी स्टेशन पर क्लिक करके उसके सभी स्लॉट (A1, A2...) देखें।";
        searchRes.normMr = "💡 वरील कोणत्याही स्टेशनवर क्लिक करून त्यातील सर्व स्लॉट्स (A1, A2...) पहा.";
        searchRes.options.add(new MenuOption("LIVE_ALL_AREAS", "📍", "Browse All Covered Areas in Pune & PCMC", "Directory"));
        searchRes.options.add(new MenuOption("ROOT", "🏠", "Return to Main Menu", "Home"));
        return searchRes;
    }

    // =========================================================================
    // HELPERS & ZERO-DEPENDENCY JSON SERIALIZER
    // =========================================================================

    private void addStandardSearchAction(BotResult res, boolean isAdmin, boolean isUser) {
        if (isAdmin) {
            res.actions.add(new ActionLink("🏢 Open Stations Manager", "Stations.jsp", "primary"));
        } else if (isUser) {
            res.actions.add(new ActionLink("📅 Search & Book a Slot Now", "Search.jsp", "emerald"));
        } else {
            res.actions.add(new ActionLink("🔑 User Login to Book", "UserLogin.jsp", "emerald"));
            res.actions.add(new ActionLink("📝 Create Free Account", "UserRegister.jsp", "primary"));
        }
    }

    private int parseStationId(String val) {
        if (val == null) return -1;
        String trimmed = val.trim();
        if (trimmed.matches("\\d+")) {
            try {
                return Integer.parseInt(trimmed);
            } catch (NumberFormatException ignored) {}
        }
        return -1;
    }

    private String detectRole(HttpSession session, String currentPage) {
        String p = currentPage != null ? currentPage.toLowerCase() : "";
        if (p.contains("adminhome") || p.contains("stations.jsp") || p.contains("managestation")
                || p.contains("viewslots") || p.contains("stationbookings") || p.contains("bookinghistory")) {
            return "ADMIN";
        }
        if (session != null && session.getAttribute("admin") != null) {
            return "ADMIN";
        }
        if (p.contains("userhome") || p.contains("search.jsp") || p.contains("searchslot")
                || p.contains("mybookings") || p.contains("payment.jsp")) {
            return "USER";
        }
        if (session != null && (session.getAttribute("email") != null
                || session.getAttribute("user") != null
                || session.getAttribute("userEmail") != null)) {
            return "USER";
        }
        return "GUEST";
    }

    private String getUserEmail(HttpSession session) {
        if (session == null) return null;
        Object em = session.getAttribute("email");
        if (em == null) em = session.getAttribute("userEmail");
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
        sb.append("\"parentId\":\"").append(escJson(res.parentId)).append("\",");
        sb.append("\"breadcrumb\":\"").append(escJson(res.breadcrumb)).append("\",");
        sb.append("\"title\":\"").append(escJson(res.title)).append("\",");
        sb.append("\"reply\":\"").append(escJson(res.reply)).append("\",");
        sb.append("\"simpleNorm\":\"").append(escJson(res.simpleNorm)).append("\",");
        sb.append("\"normHi\":\"").append(escJson(res.normHi)).append("\",");
        sb.append("\"normMr\":\"").append(escJson(res.normMr)).append("\",");

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
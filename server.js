// MSstock Web Application Server (runs concurrently with Android App)
const http = require('http');
const fs = require('fs');
const path = require('path');
const url = require('url');

const PORT = 3000;
const DB_FILE = path.join(__dirname, 'msstock_web_db.json');

// Initial seed data matching the Android catalog
const INITIAL_STAFF = [
  {
    id: "owner_parth_mehta",
    username: "parth",
    displayName: "PARTH MEHTA",
    role: "OWNER",
    pin: "apple8901",
    phoneOrEmail: "parth.mehta@msstock.store",
    department: "Universal Owner & Admin",
    isActive: true,
    hasHierarchyPermission: true,
    createdAt: 1000,
    updatedAt: Date.now()
  },
  {
    id: "staff_alex_sales",
    username: "alex.sales",
    displayName: "Alex Taylor",
    role: "SALES",
    pin: "1111",
    phoneOrEmail: "+1 555-0144",
    department: "Mobile & Audio",
    isActive: true,
    hasHierarchyPermission: true,
    createdAt: 2000,
    updatedAt: Date.now()
  }
];

const INITIAL_ITEMS = [
  {
    id: "item_led_haier_55",
    name: "Haier 55\" 4K Bezel-Less Google LED TV",
    sku: "ELEC-TV-H55U6G",
    category: "LED TV",
    brand: "Haier",
    size: "55\"",
    model: "55U6G",
    quantity: 5,
    minStockThreshold: 2,
    costPrice: 420.00,
    sellingPrice: 549.99,
    condition: "NEW",
    location: "Aisle 1 - TV Bay A",
    warrantyMonths: 24,
    notes: "Dolby Vision, 120Hz DLG Gaming mode",
    createdAt: 1000,
    updatedAt: Date.now()
  },
  {
    id: "item_led_onida_55",
    name: "Onida 55\" 4K UHD Fire TV Edition",
    sku: "ELEC-TV-O55UIF",
    category: "LED TV",
    brand: "Onida",
    size: "55\"",
    model: "55UIF",
    quantity: 4,
    minStockThreshold: 2,
    costPrice: 380.00,
    sellingPrice: 499.00,
    condition: "NEW",
    location: "Aisle 1 - TV Bay B",
    warrantyMonths: 12,
    notes: "Built-in Fire TV OS, Alexa Voice Remote",
    createdAt: 2000,
    updatedAt: Date.now()
  },
  {
    id: "item_led_sony_55",
    name: "Sony 55\" BRAVIA XR Full Array LED 4K",
    sku: "ELEC-TV-SNY55X90",
    category: "LED TV",
    brand: "Sony",
    size: "55\"",
    model: "BRAVIA-55X90L",
    quantity: 6,
    minStockThreshold: 3,
    costPrice: 850.00,
    sellingPrice: 1098.00,
    condition: "NEW",
    location: "Aisle 1 - Premium Wall 1",
    warrantyMonths: 36,
    notes: "XR Cognitive Processor, HDMI 2.1 4K120 for PS5",
    createdAt: 3000,
    updatedAt: Date.now()
  },
  {
    id: "item_led_samsung_55",
    name: "Samsung 55\" Crystal UHD 4K Smart TV",
    sku: "ELEC-TV-SAM55CU",
    category: "LED TV",
    brand: "Samsung",
    size: "55\"",
    model: "UA55CU7700",
    quantity: 7,
    minStockThreshold: 3,
    costPrice: 510.00,
    sellingPrice: 649.99,
    condition: "NEW",
    location: "Aisle 1 - TV Bay C",
    warrantyMonths: 24,
    notes: "PurColor, Crystal Processor 4K",
    createdAt: 4000,
    updatedAt: Date.now()
  },
  {
    id: "item_led_lg_55_std",
    name: "LG 55\" 4K Ultra HD Smart LED TV",
    sku: "ELEC-TV-LG55LE",
    category: "LED TV",
    brand: "LG",
    size: "55\"",
    model: "55LE5000",
    quantity: 3,
    minStockThreshold: 2,
    costPrice: 460.00,
    sellingPrice: 589.00,
    condition: "NEW",
    location: "Aisle 1 - TV Bay D",
    warrantyMonths: 24,
    notes: "webOS 23, ThinQ AI, HDR10 Pro",
    createdAt: 5000,
    updatedAt: Date.now()
  },
  {
    id: "item_led_lg_55_oled",
    name: "LG 55\" OLED evo 4K Smart Cinema TV",
    sku: "ELEC-TV-LGOLED55",
    category: "LED TV",
    brand: "LG",
    size: "55\"",
    model: "OLED55C3",
    quantity: 2,
    minStockThreshold: 2,
    costPrice: 1100.00,
    sellingPrice: 1396.99,
    condition: "NEW",
    location: "Aisle 1 - OLED Showcase",
    warrantyMonths: 36,
    notes: "Infinite contrast, G-Sync, 0.1ms response",
    createdAt: 6000,
    updatedAt: Date.now()
  },
  {
    id: "item_led_haier_43",
    name: "Haier 43\" Smart Bezel-Less LED TV",
    sku: "ELEC-TV-H43A6H",
    category: "LED TV",
    brand: "Haier",
    size: "43\"",
    model: "43A6H",
    quantity: 8,
    minStockThreshold: 3,
    costPrice: 280.00,
    sellingPrice: 369.00,
    condition: "NEW",
    location: "Aisle 2 - Compact TVs",
    warrantyMonths: 24,
    notes: "Google TV OS with Chromecast built-in",
    createdAt: 7000,
    updatedAt: Date.now()
  },
  {
    id: "item_led_onida_32",
    name: "Onida 32\" HD Ready Fire TV",
    sku: "ELEC-TV-O32HIF",
    category: "LED TV",
    brand: "Onida",
    size: "32\"",
    model: "32HIF",
    quantity: 12,
    minStockThreshold: 4,
    costPrice: 130.00,
    sellingPrice: 179.99,
    condition: "NEW",
    location: "Aisle 2 - Entry TVs",
    warrantyMonths: 12,
    notes: "Lucid 3 Picture Engine, 20W Dolby Audio",
    createdAt: 8000,
    updatedAt: Date.now()
  },
  {
    id: "item_phone_iphone16p",
    name: "Apple iPhone 16 Pro 256GB Natural Titanium",
    sku: "ELEC-IP16P-256",
    category: "Smartphones",
    brand: "Apple",
    size: "6.3\"",
    model: "A3293",
    quantity: 6,
    minStockThreshold: 2,
    costPrice: 899.00,
    sellingPrice: 1099.00,
    condition: "NEW",
    location: "Display Cabinet A - Premium",
    warrantyMonths: 12,
    notes: "A18 Pro chip, 48MP Fusion Camera, Apple Intelligence",
    createdAt: 9000,
    updatedAt: Date.now()
  },
  {
    id: "item_laptop_macbook16",
    name: "Apple MacBook Pro 16\" M3 Max 36GB / 1TB SSD",
    sku: "ELEC-MBP16-M3X",
    category: "Laptops & PCs",
    brand: "Apple",
    size: "16.2\"",
    model: "MK183LL/A",
    quantity: 3,
    minStockThreshold: 1,
    costPrice: 2850.00,
    sellingPrice: 3499.00,
    condition: "NEW",
    location: "Display Island B - Laptops",
    warrantyMonths: 12,
    notes: "Liquid Retina XDR, Space Black",
    createdAt: 10000,
    updatedAt: Date.now()
  },
  {
    id: "item_audio_sony_xm5",
    name: "Sony WH-1000XM5 Wireless Noise Canceling Headphones",
    sku: "ELEC-SNY-XM5",
    category: "Audio & Headphones",
    brand: "Sony",
    size: "Over-Ear",
    model: "WH-1000XM5",
    quantity: 9,
    minStockThreshold: 3,
    costPrice: 279.00,
    sellingPrice: 399.99,
    condition: "NEW",
    location: "Audio Wall Bay 3",
    warrantyMonths: 12,
    notes: "Industry-leading noise cancellation, 30h battery",
    createdAt: 11000,
    updatedAt: Date.now()
  }
];

// Helper to load or initialize DB
function getDatabase() {
  try {
    if (fs.existsSync(DB_FILE)) {
      const data = fs.readFileSync(DB_FILE, 'utf8');
      return JSON.parse(data);
    }
  } catch (err) {
    console.error('Error reading DB file, reinitializing', err);
  }
  const db = {
    staff: INITIAL_STAFF,
    items: INITIAL_ITEMS,
    logs: [
      {
        id: "log_init",
        itemId: "item_led_haier_55",
        itemName: "Haier 55\" 4K Bezel-Less Google LED TV",
        sku: "ELEC-TV-H55U6G",
        actionType: "RESTOCK",
        changeAmount: 5,
        newQuantity: 5,
        staffName: "PARTH MEHTA",
        staffRole: "OWNER",
        reason: "Initial Store Catalog Inventory Seed",
        timestamp: Date.now()
      }
    ]
  };
  saveDatabase(db);
  return db;
}

function saveDatabase(db) {
  try {
    fs.writeFileSync(DB_FILE, JSON.stringify(db, null, 2), 'utf8');
  } catch (err) {
    console.error('Error saving DB file', err);
  }
}

// Generate the complete single-page application HTML
function getAppHtml() {
  return `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>MSstock - Electronics Store Inventory & POS</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
  <style>
    :root {
      --primary: #1e3a8a;
      --primary-hover: #1e40af;
      --primary-container: #dbeafe;
      --on-primary-container: #1e3a8a;
      --bg: #f8fafc;
      --surface: #ffffff;
      --surface-variant: #f1f5f9;
      --text: #0f172a;
      --text-muted: #64748b;
      --border: #e2e8f0;
      --success: #16a34a;
      --success-bg: #dcfce7;
      --warning: #d97706;
      --warning-bg: #fef3c7;
      --danger: #dc2626;
      --danger-bg: #fee2e2;
      --radius: 12px;
      --shadow: 0 4px 6px -1px rgb(0 0 0 / 0.07), 0 2px 4px -2px rgb(0 0 0 / 0.07);
      --shadow-lg: 0 10px 15px -3px rgb(0 0 0 / 0.1), 0 4px 6px -4px rgb(0 0 0 / 0.1);
    }
    * { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Plus Jakarta Sans', sans-serif; }
    body { background-color: var(--bg); color: var(--text); min-height: 100vh; display: flex; flex-direction: column; }
    
    /* Header */
    header { background: var(--surface); border-bottom: 1px solid var(--border); position: sticky; top: 0; z-index: 100; box-shadow: var(--shadow); }
    .header-content { max-width: 1280px; margin: 0 auto; padding: 12px 20px; display: flex; align-items: center; justify-content: space-between; }
    .logo-area { display: flex; align-items: center; gap: 12px; }
    .logo-icon { width: 40px; height: 40px; background: var(--primary-container); color: var(--primary); border-radius: 10px; display: flex; align-items: center; justify-content: center; font-size: 22px; font-weight: bold; }
    .brand-title { font-size: 19px; font-weight: 800; color: var(--text); }
    .brand-sub { font-size: 11px; color: var(--text-muted); font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px; }

    .user-profile { display: flex; align-items: center; gap: 10px; background: var(--surface-variant); padding: 6px 14px; border-radius: 30px; cursor: pointer; border: 1px solid var(--border); transition: all 0.2s; }
    .user-profile:hover { background: #e2e8f0; }
    .user-avatar { width: 28px; height: 28px; border-radius: 50%; background: var(--primary); color: white; display: flex; align-items: center; justify-content: center; font-size: 12px; font-weight: bold; }
    .user-name { font-size: 13px; font-weight: 700; color: var(--text); }
    .user-badge { font-size: 11px; font-weight: 600; color: var(--primary); }

    /* Main Container */
    main { max-width: 1280px; margin: 0 auto; padding: 24px 20px 80px; width: 100%; }

    /* Stats Grid */
    .stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 16px; margin-bottom: 24px; }
    .stat-card { background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius); padding: 18px; box-shadow: var(--shadow); position: relative; }
    .stat-label { font-size: 12px; color: var(--text-muted); font-weight: 600; text-transform: uppercase; }
    .stat-value { font-size: 24px; font-weight: 800; color: var(--text); margin-top: 4px; }
    .stat-extra { font-size: 11px; color: var(--success); font-weight: 600; margin-top: 2px; }

    /* Alert Banner */
    .alert-banner { background: #fffbeb; border: 1px solid #fde68a; border-radius: var(--radius); padding: 14px 18px; margin-bottom: 20px; display: flex; align-items: center; justify-content: space-between; gap: 12px; }
    .alert-banner.danger { background: var(--danger-bg); border-color: #fca5a5; }
    .alert-text { font-size: 13px; font-weight: 600; color: #92400e; display: flex; align-items: center; gap: 8px; }
    .alert-banner.danger .alert-text { color: #991b1b; }
    .alert-actions { display: flex; gap: 8px; }
    .btn-sm { padding: 6px 12px; font-size: 12px; font-weight: 700; border-radius: 8px; border: none; cursor: pointer; transition: 0.15s; }
    .btn-amber { background: #f59e0b; color: white; }
    .btn-danger { background: var(--danger); color: white; }

    /* Search & Filter Toolbar */
    .toolbar { background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius); padding: 18px; box-shadow: var(--shadow); margin-bottom: 24px; }
    .search-row { display: flex; gap: 12px; position: relative; }
    .search-wrapper { flex: 1; position: relative; }
    .search-input { width: 100%; padding: 12px 16px 12px 42px; font-size: 14px; border: 1.5px solid var(--border); border-radius: 10px; outline: none; transition: border-color 0.2s; }
    .search-input:focus { border-color: var(--primary); }
    .search-icon { position: absolute; left: 14px; top: 50%; transform: translateY(-50%); color: var(--text-muted); font-size: 18px; }
    .clear-search { position: absolute; right: 12px; top: 50%; transform: translateY(-50%); background: none; border: none; cursor: pointer; color: var(--text-muted); font-size: 16px; }

    /* Model Auto-fill Suggestions Dropdown */
    .suggestions-panel { position: absolute; top: calc(100% + 6px); left: 0; right: 0; background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius); box-shadow: var(--shadow-lg); z-index: 50; max-height: 320px; overflow-y: auto; padding: 8px; }
    .suggestion-header { font-size: 11px; font-weight: 700; color: var(--primary); padding: 6px 8px; text-transform: uppercase; letter-spacing: 0.5px; display: flex; justify-content: space-between; }
    .suggestion-item { display: flex; align-items: center; justify-content: space-between; padding: 10px 12px; border-radius: 8px; cursor: pointer; transition: background 0.15s; }
    .suggestion-item:hover { background: var(--surface-variant); }
    .suggestion-model { font-weight: 800; color: var(--primary); background: var(--primary-container); padding: 3px 8px; border-radius: 6px; font-size: 12px; margin-right: 10px; }
    .suggestion-details { flex: 1; font-size: 13px; font-weight: 600; color: var(--text); }
    .suggestion-sub { font-size: 11px; color: var(--text-muted); font-weight: normal; }

    /* Quick Model Pills */
    .quick-models { display: flex; align-items: center; gap: 8px; margin-top: 12px; flex-wrap: wrap; }
    .quick-title { font-size: 12px; font-weight: 700; color: var(--text-muted); }
    .model-chip { background: var(--surface-variant); border: 1px solid var(--border); padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 700; color: var(--text); cursor: pointer; transition: all 0.15s; display: inline-flex; align-items: center; gap: 4px; }
    .model-chip:hover { background: var(--primary-container); color: var(--primary); border-color: var(--primary); }

    /* Action Buttons Row */
    .filter-actions { display: flex; gap: 10px; margin-top: 14px; flex-wrap: wrap; }
    .btn-guided { background: var(--primary); color: white; border: none; border-radius: 10px; padding: 10px 18px; font-size: 13px; font-weight: 700; cursor: pointer; display: flex; align-items: center; gap: 8px; transition: background 0.2s; }
    .btn-guided:hover { background: var(--primary-hover); }
    .btn-secondary { background: var(--surface-variant); color: var(--text); border: 1px solid var(--border); border-radius: 10px; padding: 10px 16px; font-size: 13px; font-weight: 600; cursor: pointer; }
    .btn-secondary:hover { background: #e2e8f0; }

    /* Category Chips */
    .category-chips { display: flex; gap: 8px; overflow-x: auto; padding-bottom: 6px; margin-bottom: 20px; scrollbar-width: none; }
    .cat-chip { padding: 8px 16px; border-radius: 20px; font-size: 13px; font-weight: 600; background: var(--surface); border: 1px solid var(--border); cursor: pointer; white-space: nowrap; transition: all 0.15s; color: var(--text-muted); }
    .cat-chip.active { background: var(--primary); color: white; border-color: var(--primary); }

    /* Product Cards Grid */
    .product-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(320px, 1fr)); gap: 18px; }
    .card { background: var(--surface); border: 1px solid var(--border); border-radius: var(--radius); padding: 18px; box-shadow: var(--shadow); display: flex; flex-direction: column; justify-content: space-between; transition: transform 0.2s, box-shadow 0.2s; position: relative; }
    .card:hover { transform: translateY(-3px); box-shadow: var(--shadow-lg); }
    
    .card-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 10px; }
    .badge-category { font-size: 11px; font-weight: 700; background: var(--primary-container); color: var(--primary); padding: 3px 8px; border-radius: 6px; }
    .badge-size { font-size: 11px; font-weight: 700; background: #e0e7ff; color: #4338ca; padding: 3px 8px; border-radius: 6px; margin-left: 6px; }
    .badge-stock { font-size: 11px; font-weight: 700; padding: 3px 8px; border-radius: 6px; }
    .stock-in { background: var(--success-bg); color: var(--success); }
    .stock-low { background: var(--warning-bg); color: var(--warning); }
    .stock-out { background: #e2e8f0; color: #64748b; }

    .card-title { font-size: 15px; font-weight: 700; color: var(--text); line-height: 1.3; margin-bottom: 4px; }
    .card-meta { font-size: 12px; color: var(--text-muted); margin-bottom: 12px; }
    .card-model { color: var(--primary); font-weight: 700; }

    .pricing-box { background: var(--surface-variant); padding: 10px 12px; border-radius: 8px; display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; }
    .price-val { font-size: 18px; font-weight: 800; color: var(--primary); }
    .margin-val { font-size: 12px; font-weight: 700; color: var(--success); }

    .card-actions { display: flex; gap: 8px; }
    .btn-sell { flex: 1.2; background: var(--success); color: white; border: none; padding: 9px; border-radius: 8px; font-weight: 700; font-size: 13px; cursor: pointer; display: flex; align-items: center; justify-content: center; gap: 6px; }
    .btn-sell:disabled { background: #94a3b8; cursor: not-allowed; }
    .btn-stock { flex: 1; background: transparent; border: 1.5px solid var(--border); padding: 8px; border-radius: 8px; font-weight: 600; font-size: 13px; cursor: pointer; }
    .btn-stock:hover { background: var(--surface-variant); }
    .btn-icon { width: 36px; height: 36px; border-radius: 8px; border: 1px solid var(--border); background: transparent; cursor: pointer; display: flex; align-items: center; justify-content: center; }
    .btn-icon:hover { background: var(--surface-variant); }

    /* Modals */
    .modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.5); z-index: 200; display: none; align-items: center; justify-content: center; padding: 20px; backdrop-filter: blur(4px); }
    .modal-overlay.active { display: flex; }
    .modal { background: var(--surface); border-radius: 16px; width: 100%; max-width: 580px; max-height: 90vh; overflow-y: auto; box-shadow: var(--shadow-lg); padding: 24px; position: relative; }
    .modal-title { font-size: 18px; font-weight: 800; margin-bottom: 6px; color: var(--text); }
    .modal-sub { font-size: 13px; color: var(--text-muted); margin-bottom: 18px; }
    .close-btn { position: absolute; right: 20px; top: 20px; background: none; border: none; font-size: 22px; cursor: pointer; color: var(--text-muted); }

    /* Guided LED Hierarchy in Modal */
    .size-selector { display: flex; gap: 8px; margin-bottom: 18px; flex-wrap: wrap; }
    .size-btn { padding: 8px 16px; border-radius: 10px; border: 1.5px solid var(--border); background: var(--surface); font-size: 13px; font-weight: 700; cursor: pointer; }
    .size-btn.active { background: var(--primary); color: white; border-color: var(--primary); }

    .brand-list { display: flex; flex-direction: column; gap: 10px; }
    .brand-card { border: 1.5px solid var(--border); border-radius: 12px; padding: 14px; cursor: pointer; transition: all 0.2s; background: var(--surface); }
    .brand-card:hover { border-color: var(--primary); }
    .brand-card.out-of-stock { background: #f1f5f9; border-color: #cbd5e1; opacity: 0.65; }
    .brand-header { display: flex; justify-content: space-between; align-items: center; }
    .brand-name { font-size: 16px; font-weight: 800; }
    .brand-models { margin-top: 10px; padding-top: 10px; border-top: 1px dashed var(--border); }
    .model-row { display: flex; justify-content: space-between; align-items: center; padding: 6px 0; font-size: 13px; }

    /* Form Fields */
    .form-group { margin-bottom: 14px; }
    .form-label { display: block; font-size: 12px; font-weight: 700; margin-bottom: 4px; color: var(--text); }
    .form-control { width: 100%; padding: 10px 12px; border: 1.5px solid var(--border); border-radius: 8px; font-size: 13px; outline: none; }
    .form-control:focus { border-color: var(--primary); }
    .btn-submit { width: 100%; background: var(--primary); color: white; border: none; padding: 12px; border-radius: 10px; font-weight: 700; font-size: 14px; cursor: pointer; margin-top: 10px; }

    /* Floating Action Button */
    .fab { position: fixed; right: 24px; bottom: 24px; width: 56px; height: 56px; border-radius: 28px; background: var(--primary); color: white; border: none; font-size: 26px; box-shadow: var(--shadow-lg); cursor: pointer; display: flex; align-items: center; justify-content: center; z-index: 90; transition: transform 0.2s; }
    .fab:hover { transform: scale(1.05); }

    /* Toast */
    .toast { position: fixed; bottom: 24px; left: 50%; transform: translateX(-50%); background: #1e293b; color: white; padding: 10px 20px; border-radius: 30px; font-size: 13px; font-weight: 600; box-shadow: var(--shadow-lg); z-index: 300; display: none; }
    .toast.active { display: block; }
  </style>
</head>
<body>

  <!-- Header -->
  <header>
    <div class="header-content">
      <div class="logo-area">
        <div class="logo-icon">⚡</div>
        <div>
          <div class="brand-title">MSstock Web</div>
          <div class="brand-sub">Electronics Store & POS</div>
        </div>
      </div>

      <div style="display: flex; align-items: center; gap: 12px;">
        <div class="user-profile" id="userProfileBtn" onclick="openLoginModal()">
          <div class="user-avatar" id="headerAvatar">P</div>
          <div>
            <div class="user-name" id="headerUserName">PARTH MEHTA</div>
            <div class="user-badge" id="headerUserBadge">👑 Universal Owner & Admin</div>
          </div>
        </div>
        <button class="btn-secondary" onclick="openTeamModal()" id="teamManageBtn" style="padding: 8px 14px; font-size: 12px; font-weight: 700;">👥 Team</button>
      </div>
    </div>
  </header>

  <!-- Main Container -->
  <main>
    <!-- Live Stats Overview -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-label">Total Products</div>
        <div class="stat-value" id="statTotalItems">11</div>
        <div class="stat-extra">In Catalog</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">Units In Stock</div>
        <div class="stat-value" id="statTotalUnits">71</div>
        <div class="stat-extra">Physical Inventory</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">Retail Value</div>
        <div class="stat-value" id="statRetailValue">$64,520</div>
        <div class="stat-extra">Gross Potential</div>
      </div>
      <div class="stat-card owner-only" id="statMarginCard">
        <div class="stat-label">Profit Margin (Owner)</div>
        <div class="stat-value" id="statAvgMargin">23.8%</div>
        <div class="stat-extra" id="statWholesale">Cost: $49,150</div>
      </div>
    </div>

    <!-- Live Stock Alert Banner -->
    <div class="alert-banner" id="alertBanner">
      <div class="alert-text">
        <span>⚠️</span>
        <span id="alertBannerText">Stock Alert: 2 products running low on inventory</span>
      </div>
      <div class="alert-actions">
        <button class="btn-sm btn-amber" onclick="filterByStock('LOW_STOCK')">View Low Stock</button>
        <button class="btn-sm btn-danger" onclick="filterByStock('OUT_OF_STOCK')">View Out of Stock</button>
      </div>
    </div>

    <!-- Search & Filter Toolbar -->
    <div class="toolbar">
      <div class="search-row">
        <div class="search-wrapper">
          <span class="search-icon">🔍</span>
          <input type="text" id="searchInput" class="search-input" placeholder="Search model # (e.g. 55U6G, OLED55C3, BRAVIA-55X90L) or type 'LED'..." oninput="handleSearchInput()" autocomplete="off">
          <button id="clearSearchBtn" class="clear-search" onclick="clearSearch()" style="display:none;">✕</button>
          
          <!-- Dropdown Model Auto-Fill Suggestions -->
          <div id="suggestionsPanel" class="suggestions-panel" style="display: none;"></div>
        </div>
      </div>

      <!-- Quick Popular Model Chips -->
      <div class="quick-models">
        <span class="quick-title">⚡ Quick Models:</span>
        <span class="model-chip" onclick="fillSearch('55U6G')">55U6G (Haier 55")</span>
        <span class="model-chip" onclick="fillSearch('55UIF')">55UIF (Onida 55")</span>
        <span class="model-chip" onclick="fillSearch('BRAVIA-55X90L')">BRAVIA-55X90L (Sony)</span>
        <span class="model-chip" onclick="fillSearch('UA55CU7700')">UA55CU7700 (Samsung)</span>
        <span class="model-chip" onclick="fillSearch('55LE5000')">55LE5000 (LG)</span>
        <span class="model-chip" onclick="fillSearch('OLED55C3')">OLED55C3 (LG OLED)</span>
      </div>

      <!-- Filter Buttons -->
      <div class="filter-actions">
        <button class="btn-guided" onclick="openGuidedLEDModal()">📺 Guided LED Filter (Brand > Size > Model)</button>
        <button class="btn-secondary" onclick="filterByCategory('LED TV')">📺 LED TVs Only</button>
        <button class="btn-secondary" onclick="filterByCategory('Smartphones')">📱 Smartphones</button>
        <button class="btn-secondary" onclick="filterByCategory('All')">🔄 Reset Filters</button>
      </div>
    </div>

    <!-- Category Chips Row -->
    <div class="category-chips" id="categoryChips">
      <div class="cat-chip active" onclick="selectCategoryChip('All')">All Categories</div>
      <div class="cat-chip" onclick="selectCategoryChip('LED TV')">LED TV</div>
      <div class="cat-chip" onclick="selectCategoryChip('Smartphones')">Smartphones</div>
      <div class="cat-chip" onclick="selectCategoryChip('Laptops & PCs')">Laptops & PCs</div>
      <div class="cat-chip" onclick="selectCategoryChip('Audio & Headphones')">Audio & Headphones</div>
      <div class="cat-chip" onclick="selectCategoryChip('Gaming & Displays')">Gaming</div>
      <div class="cat-chip" onclick="selectCategoryChip('Accessories')">Accessories</div>
    </div>

    <!-- Product Grid -->
    <div class="product-grid" id="productGrid"></div>
  </main>

  <!-- Floating Add Product Button -->
  <button class="fab" onclick="openAddProductModal()" title="Register New Stock Item">+</button>

  <!-- Modal: Guided LED Filter (Brand > Size > Model) -->
  <div class="modal-overlay" id="guidedLEDModal">
    <div class="modal">
      <button class="close-btn" onclick="closeModal('guidedLEDModal')">✕</button>
      <div class="modal-title">📺 Guided LED Filter</div>
      <div class="modal-sub">Filter by Screen Size → View Available Brands (Out of stock brands are greyed out)</div>

      <div style="font-size: 12px; font-weight: 700; margin-bottom: 8px;">1. SELECT SCREEN SIZE:</div>
      <div class="size-selector">
        <button class="size-btn" onclick="selectLEDSize('32\\"')">32"</button>
        <button class="size-btn" onclick="selectLEDSize('43\\"')">43"</button>
        <button class="size-btn active" onclick="selectLEDSize('55\\"')">55"</button>
        <button class="size-btn" onclick="selectLEDSize('65\\"')">65"</button>
        <button class="size-btn" onclick="selectLEDSize('75\\"')">75"</button>
      </div>

      <div style="font-size: 12px; font-weight: 700; margin-bottom: 8px;">2. BRAND AVAILABILITY & STOCK:</div>
      <div class="brand-list" id="guidedBrandList"></div>
    </div>
  </div>

  <!-- Modal: Quick Sale / POS -->
  <div class="modal-overlay" id="saleModal">
    <div class="modal">
      <button class="close-btn" onclick="closeModal('saleModal')">✕</button>
      <div class="modal-title">💳 Process Customer Sale</div>
      <div class="modal-sub" id="saleItemSub">Issue receipt and decrement inventory</div>

      <div class="form-group">
        <label class="form-label">Units to Sell</label>
        <input type="number" id="saleQuantity" class="form-control" value="1" min="1">
      </div>
      <div class="form-group">
        <label class="form-label">Payment Method</label>
        <select id="salePayment" class="form-control">
          <option value="Cash">Cash</option>
          <option value="Credit / Debit Card">Credit / Debit Card</option>
          <option value="QR / UPI">QR / UPI</option>
          <option value="Bank Transfer">Bank Transfer</option>
        </select>
      </div>
      <div class="form-group">
        <label class="form-label">Customer Name / Phone (Optional)</label>
        <input type="text" id="saleCustomer" class="form-control" placeholder="For receipt record">
      </div>

      <div id="saleTotalBox" style="background: var(--surface-variant); padding: 12px; border-radius: 8px; margin-bottom: 14px; font-weight: 800; font-size: 16px; color: var(--success); text-align: right;">
        Total: $0.00
      </div>

      <button class="btn-submit" style="background: var(--success);" onclick="confirmSale()">Complete Sale & Issue Receipt</button>
    </div>
  </div>

  <!-- Modal: Add / Edit Product -->
  <div class="modal-overlay" id="productModal">
    <div class="modal">
      <button class="close-btn" onclick="closeModal('productModal')">✕</button>
      <div class="modal-title" id="productModalTitle">Register New Stock</div>
      <div class="modal-sub">Hierarchy: Category → Brand → Size → Model</div>

      <div class="form-group">
        <label class="form-label">1. Product Category *</label>
        <select id="itemCategory" class="form-control" onchange="onCategoryChange()">
          <option value="LED TV">LED TV</option>
          <option value="Smartphones">Smartphones</option>
          <option value="Laptops & PCs">Laptops & PCs</option>
          <option value="Audio & Headphones">Audio & Headphones</option>
          <option value="Gaming & Displays">Gaming & Displays</option>
          <option value="Accessories">Accessories</option>
        </select>
      </div>

      <div class="form-group">
        <label class="form-label">2. Brand *</label>
        <input type="text" id="itemBrand" class="form-control" placeholder="e.g. Haier, Onida, Sony, Samsung, LG">
        <div style="display: flex; gap: 4px; margin-top: 6px; flex-wrap: wrap;">
          <span class="model-chip" onclick="setBrand('Haier')">Haier</span>
          <span class="model-chip" onclick="setBrand('Onida')">Onida</span>
          <span class="model-chip" onclick="setBrand('Sony')">Sony</span>
          <span class="model-chip" onclick="setBrand('Samsung')">Samsung</span>
          <span class="model-chip" onclick="setBrand('LG')">LG</span>
          <span class="model-chip" onclick="setBrand('TCL')">TCL</span>
          <span class="model-chip" onclick="setBrand('Apple')">Apple</span>
        </div>
      </div>

      <div class="form-group">
        <label class="form-label">3. Size / Capacity (e.g. 55", 43", 256GB)</label>
        <input type="text" id="itemSize" class="form-control" placeholder='e.g. 55"'>
        <div style="display: flex; gap: 4px; margin-top: 6px;">
          <span class="model-chip" onclick="setSize('32\\"')">32"</span>
          <span class="model-chip" onclick="setSize('43\\"')">43"</span>
          <span class="model-chip" onclick="setSize('55\\"')">55"</span>
          <span class="model-chip" onclick="setSize('65\\"')">65"</span>
          <span class="model-chip" onclick="setSize('75\\"')">75"</span>
        </div>
      </div>

      <div class="form-group">
        <label class="form-label">4. Model Number *</label>
        <input type="text" id="itemModel" class="form-control" placeholder="e.g. 55U6G, 55UIF, OLED55C3">
      </div>

      <div class="form-group">
        <label class="form-label">Product Name / Title *</label>
        <input type="text" id="itemName" class="form-control" placeholder="e.g. Haier 55 Inch 4K Bezel-Less Google TV">
      </div>

      <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px;">
        <div class="form-group">
          <label class="form-label">Quantity in Stock *</label>
          <input type="number" id="itemQuantity" class="form-control" value="5" min="0">
        </div>
        <div class="form-group">
          <label class="form-label">Min Stock Threshold</label>
          <input type="number" id="itemMinThreshold" class="form-control" value="2" min="1">
        </div>
      </div>

      <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px;">
        <div class="form-group">
          <label class="form-label">Selling Price ($) *</label>
          <input type="number" id="itemSellingPrice" class="form-control" step="0.01" placeholder="549.99">
        </div>
        <div class="form-group">
          <label class="form-label">Wholesale Cost ($)</label>
          <input type="number" id="itemCostPrice" class="form-control" step="0.01" placeholder="420.00">
        </div>
      </div>

      <div class="form-group">
        <label class="form-label">Warehouse Location</label>
        <input type="text" id="itemLocation" class="form-control" placeholder="e.g. Aisle 1 - TV Bay A">
      </div>

      <button class="btn-submit" onclick="saveProduct()">Save Product to Inventory</button>
    </div>
  </div>

  <!-- Modal: User Profile & Authentication -->
  <div class="modal-overlay" id="loginModal">
    <div class="modal">
      <button class="close-btn" onclick="closeModal('loginModal')">✕</button>
      <div class="modal-title">🔐 Staff & Owner Portal</div>
      <div class="modal-sub">Log in with PARTH MEHTA (password: apple8901) or switch team members</div>

      <div id="staffListArea" style="display: flex; flex-direction: column; gap: 8px; margin-bottom: 16px;"></div>

      <div id="passwordEntryBox" style="display: none; background: var(--surface-variant); padding: 16px; border-radius: 12px;">
        <div style="font-weight: 700; margin-bottom: 6px;" id="loginTargetName">Logging in as PARTH MEHTA</div>
        <div class="form-group">
          <label class="form-label">Password / PIN</label>
          <input type="password" id="loginPasswordInput" class="form-control" placeholder="Enter password (hint: apple8901)">
        </div>
        <button class="btn-submit" onclick="submitLogin()">Unlock Session</button>
      </div>
    </div>
  </div>

  <!-- Modal: Team Management -->
  <div class="modal-overlay" id="teamModal">
    <div class="modal">
      <button class="close-btn" onclick="closeModal('teamModal')">✕</button>
      <div class="modal-title">👥 Team & Role Permissions</div>
      <div class="modal-sub">Manage staff members and grant Guided LED Filter access</div>

      <div id="teamMembersList" style="display: flex; flex-direction: column; gap: 10px;"></div>
    </div>
  </div>

  <!-- Receipt Modal -->
  <div class="modal-overlay" id="receiptModal">
    <div class="modal">
      <button class="close-btn" onclick="closeModal('receiptModal')">✕</button>
      <div class="modal-title" style="color: var(--success);">✓ Sale Completed</div>
      <div class="modal-sub">Customer invoice generated</div>
      <div id="receiptContent" style="background: var(--surface-variant); padding: 16px; border-radius: 12px; font-family: monospace; font-size: 13px; line-height: 1.6; margin-bottom: 16px;"></div>
      <button class="btn-submit" onclick="closeModal('receiptModal')">Close</button>
    </div>
  </div>

  <!-- Toast Notification -->
  <div class="toast" id="toast"></div>

  <script>
    let appState = {
      items: [],
      staff: [],
      logs: [],
      currentStaff: null,
      selectedCategory: 'All',
      selectedStockFilter: 'ALL',
      selectedLEDSize: '55"',
      searchQuery: '',
      saleTargetItem: null,
      editingItemId: null,
      loginTargetStaff: null
    };

    // Load initial data from REST API
    async function loadData() {
      try {
        const res = await fetch('/api/data');
        const data = await res.json();
        appState.items = data.items;
        appState.staff = data.staff;
        appState.logs = data.logs;

        // Default to PARTH MEHTA as active session
        if (!appState.currentStaff) {
          appState.currentStaff = appState.staff.find(s => s.displayName === "PARTH MEHTA") || appState.staff[0];
        }

        updateHeaderProfile();
        renderStats();
        renderProducts();
      } catch (e) {
        console.error("Failed to load initial data", e);
      }
    }

    function updateHeaderProfile() {
      const staff = appState.currentStaff;
      if (!staff) return;
      document.getElementById('headerAvatar').textContent = staff.displayName[0] || 'U';
      document.getElementById('headerUserName').textContent = staff.displayName;
      const isOwner = staff.displayName.toUpperCase().includes('PARTH MEHTA') || staff.role === 'OWNER';
      document.getElementById('headerUserBadge').textContent = isOwner ? '👑 Universal Owner & Admin' : staff.role;
      
      const marginCard = document.getElementById('statMarginCard');
      if (marginCard) {
        marginCard.style.display = isOwner ? 'block' : 'none';
      }
    }

    function renderStats() {
      const items = appState.items;
      const totalUnits = items.reduce((acc, it) => acc + (it.quantity || 0), 0);
      const retailVal = items.reduce((acc, it) => acc + ((it.sellingPrice || 0) * (it.quantity || 0)), 0);
      const costVal = items.reduce((acc, it) => acc + ((it.costPrice || 0) * (it.quantity || 0)), 0);
      const lowStock = items.filter(it => it.quantity > 0 && it.quantity <= (it.minStockThreshold || 2)).length;
      const outOfStock = items.filter(it => it.quantity <= 0).length;

      document.getElementById('statTotalItems').textContent = items.length;
      document.getElementById('statTotalUnits').textContent = totalUnits;
      document.getElementById('statRetailValue').textContent = '$' + retailVal.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
      
      const marginEl = document.getElementById('statAvgMargin');
      if (marginEl && retailVal > 0) {
        const marginPct = ((retailVal - costVal) / retailVal) * 100;
        marginEl.textContent = marginPct.toFixed(1) + '%';
        document.getElementById('statWholesale').textContent = 'Cost: $' + costVal.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
      }

      const alertBanner = document.getElementById('alertBanner');
      if (outOfStock > 0 || lowStock > 0) {
        alertBanner.style.display = 'flex';
        alertBanner.className = 'alert-banner ' + (outOfStock > 0 ? 'danger' : '');
        document.getElementById('alertBannerText').textContent = 
          (outOfStock > 0 ? outOfStock + ' out of stock! ' : '') + 
          (lowStock > 0 ? lowStock + ' low on stock.' : '');
      } else {
        alertBanner.style.display = 'none';
      }
    }

    function renderProducts() {
      const grid = document.getElementById('productGrid');
      let filtered = appState.items;

      if (appState.selectedCategory !== 'All') {
        filtered = filtered.filter(it => (it.category || '').toLowerCase() === appState.selectedCategory.toLowerCase());
      }

      if (appState.selectedStockFilter === 'LOW_STOCK') {
        filtered = filtered.filter(it => it.quantity > 0 && it.quantity <= (it.minStockThreshold || 2));
      } else if (appState.selectedStockFilter === 'OUT_OF_STOCK') {
        filtered = filtered.filter(it => it.quantity <= 0);
      }

      if (appState.searchQuery.trim()) {
        const q = appState.searchQuery.toLowerCase().trim();
        filtered = filtered.filter(it => 
          (it.model || '').toLowerCase().includes(q) ||
          (it.name || '').toLowerCase().includes(q) ||
          (it.brand || '').toLowerCase().includes(q) ||
          (it.sku || '').toLowerCase().includes(q)
        );
      }

      if (filtered.length === 0) {
        grid.innerHTML = '<div style="grid-column: 1/-1; text-align: center; padding: 48px; color: var(--text-muted); font-size: 15px;">No products match your search or filter criteria.</div>';
        return;
      }

      const isOwner = appState.currentStaff && (appState.currentStaff.displayName.includes('PARTH MEHTA') || appState.currentStaff.role === 'OWNER');

      grid.innerHTML = filtered.map(item => {
        const isOut = item.quantity <= 0;
        const isLow = item.quantity > 0 && item.quantity <= (item.minStockThreshold || 2);
        const stockClass = isOut ? 'stock-out' : (isLow ? 'stock-low' : 'stock-in');
        const stockText = isOut ? 'Out of Stock (0)' : (isLow ? 'Low Stock (' + item.quantity + ')' : item.quantity + ' in stock');
        const marginPct = item.sellingPrice > 0 ? (((item.sellingPrice - item.costPrice) / item.sellingPrice) * 100).toFixed(1) + '%' : '0%';

        return \`
          <div class="card" id="card_\${item.id}">
            <div>
              <div class="card-header">
                <div>
                  <span class="badge-category">\${item.category}</span>
                  \${item.size ? '<span class="badge-size">' + item.size + '</span>' : ''}
                </div>
                <span class="badge-stock \${stockClass}">\${stockText}</span>
              </div>
              <div class="card-title">\${item.name}</div>
              <div class="card-meta">
                \${item.brand} • SKU: \${item.sku}
                \${item.model ? ' • Model: <span class="card-model">' + item.model + '</span>' : ''}
              </div>
            </div>

            <div>
              <div class="pricing-box">
                <div>
                  <div style="font-size: 11px; color: var(--text-muted); font-weight: 600;">SELLING PRICE</div>
                  <div class="price-val">$\${Number(item.sellingPrice).toFixed(2)}</div>
                </div>
                \${isOwner ? '<div><div style="font-size: 11px; color: var(--text-muted); font-weight: 600;">MARGIN</div><div class="margin-val">' + marginPct + '</div></div>' : ''}
              </div>

              <div class="card-actions">
                <button class="btn-sell" \${isOut ? 'disabled' : ''} onclick="openSaleModal('\${item.id}')">
                  💳 Sell
                </button>
                <button class="btn-stock" onclick="quickAdjustStock('\${item.id}', 1)">+ Stock</button>
                <button class="btn-stock" onclick="quickAdjustStock('\${item.id}', -1)" \${isOut ? 'disabled' : ''}>- Stock</button>
                <button class="btn-icon" onclick="openEditProductModal('\${item.id}')" title="Edit Product">✏️</button>
              </div>
            </div>
          </div>
        \`;
      }).join('');
    }

    // Model Auto-Fill Suggestions
    function handleSearchInput() {
      const input = document.getElementById('searchInput');
      const val = input.value;
      appState.searchQuery = val;
      document.getElementById('clearSearchBtn').style.display = val ? 'block' : 'none';

      const panel = document.getElementById('suggestionsPanel');
      if (!val.trim()) {
        panel.style.display = 'none';
        renderProducts();
        return;
      }

      const q = val.toLowerCase().trim();
      const matches = appState.items.filter(it => 
        (it.model || '').toLowerCase().includes(q) ||
        (it.name || '').toLowerCase().includes(q) ||
        (it.brand || '').toLowerCase().includes(q) ||
        (it.sku || '').toLowerCase().includes(q)
      ).slice(0, 5);

      if (matches.length > 0) {
        panel.style.display = 'block';
        panel.innerHTML = \`
          <div class="suggestion-header">
            <span>✨ SUGGESTED MODELS (Click to Auto-fill)</span>
            <span>\${matches.length} matches</span>
          </div>
          \${matches.map(m => \`
            <div class="suggestion-item" onclick="selectSuggestion('\${m.model || m.name}')">
              <div style="display: flex; align-items: center;">
                <span class="suggestion-model">\${m.model || 'N/A'}</span>
                <div>
                  <div class="suggestion-details">\${m.brand} • \${m.name}</div>
                  <div class="suggestion-sub">\${m.category} \${m.size ? '(' + m.size + ')' : ''}</div>
                </div>
              </div>
              <div style="display: flex; align-items: center; gap: 8px;">
                <span class="badge-stock \${m.quantity <= 0 ? 'stock-out' : 'stock-in'}">\${m.quantity <= 0 ? 'Out of Stock' : m.quantity + ' in stock'}</span>
                <span style="color: var(--primary); font-size: 14px;">↗</span>
              </div>
            </div>
          \`).join('')}
        \`;
      } else {
        panel.style.display = 'none';
      }

      renderProducts();
    }

    function selectSuggestion(modelStr) {
      document.getElementById('searchInput').value = modelStr;
      appState.searchQuery = modelStr;
      document.getElementById('suggestionsPanel').style.display = 'none';
      document.getElementById('clearSearchBtn').style.display = 'block';
      renderProducts();
    }

    function fillSearch(val) {
      document.getElementById('searchInput').value = val;
      appState.searchQuery = val;
      document.getElementById('clearSearchBtn').style.display = 'block';
      renderProducts();
    }

    function clearSearch() {
      document.getElementById('searchInput').value = '';
      appState.searchQuery = '';
      document.getElementById('clearSearchBtn').style.display = 'none';
      document.getElementById('suggestionsPanel').style.display = 'none';
      renderProducts();
    }

    function selectCategoryChip(cat) {
      appState.selectedCategory = cat;
      const chips = document.querySelectorAll('.cat-chip');
      chips.forEach(c => {
        c.classList.toggle('active', c.textContent.trim().toLowerCase() === cat.toLowerCase() || (cat === 'All' && c.textContent.includes('All')));
      });
      renderProducts();
    }

    function filterByCategory(cat) {
      selectCategoryChip(cat);
    }

    function filterByStock(stockFilter) {
      appState.selectedStockFilter = stockFilter;
      renderProducts();
      showToast('Filtered by ' + stockFilter.replace('_', ' '));
    }

    // Guided LED Filter
    function openGuidedLEDModal() {
      // Permission check
      const staff = appState.currentStaff;
      const canAccess = staff && (staff.displayName.includes('PARTH MEHTA') || staff.role === 'OWNER' || staff.hasHierarchyPermission);
      if (!canAccess) {
        alert('Permission Required: Ask store owner PARTH MEHTA to grant you LED Filter permissions.');
        return;
      }
      renderGuidedLEDBrands();
      document.getElementById('guidedLEDModal').classList.add('active');
    }

    function selectLEDSize(size) {
      appState.selectedLEDSize = size;
      document.querySelectorAll('.size-btn').forEach(b => {
        b.classList.toggle('active', b.textContent.trim() === size);
      });
      renderGuidedLEDBrands();
    }

    function renderGuidedLEDBrands() {
      const container = document.getElementById('guidedBrandList');
      const size = appState.selectedLEDSize;
      const allLEDs = appState.items.filter(it => (it.category || '').toLowerCase() === 'led tv');
      
      const BRANDS = ['Haier', 'Onida', 'Sony', 'Samsung', 'LG', 'TCL'];

      container.innerHTML = BRANDS.map(brand => {
        const matching = allLEDs.filter(it => (it.brand || '').toLowerCase() === brand.toLowerCase() && it.size === size);
        const totalStock = matching.reduce((acc, it) => acc + (it.quantity || 0), 0);
        const isOut = totalStock <= 0;

        return \`
          <div class="brand-card \${isOut ? 'out-of-stock' : ''}">
            <div class="brand-header">
              <div>
                <span class="brand-name">\${brand}</span>
                <span style="font-size: 12px; color: var(--text-muted); margin-left: 8px;">(\${size})</span>
              </div>
              <span class="badge-stock \${isOut ? 'stock-out' : 'stock-in'}">
                \${isOut ? 'Out of Stock (Greyed Out)' : totalStock + ' Available in Stock'}
              </span>
            </div>

            \${matching.length > 0 ? \`
              <div class="brand-models">
                \${matching.map(m => \`
                  <div class="model-row">
                    <div>
                      <strong>\${m.model}</strong> - \${m.name}
                    </div>
                    <div style="display: flex; gap: 8px; align-items: center;">
                      <span style="font-weight: 700; color: var(--primary);">$\${Number(m.sellingPrice).toFixed(2)}</span>
                      <button class="btn-sm btn-amber" onclick="quickSellFromGuided('\${m.id}')" \${m.quantity <= 0 ? 'disabled' : ''}>Sell</button>
                    </div>
                  </div>
                \`).join('')}
              </div>
            \` : '<div style="font-size: 11px; color: var(--text-muted); margin-top: 6px;">No specific model configured for this size.</div>'}
          </div>
        \`;
      }).join('');
    }

    function quickSellFromGuided(itemId) {
      closeModal('guidedLEDModal');
      openSaleModal(itemId);
    }

    // POS Sale Actions
    function openSaleModal(itemId) {
      const item = appState.items.find(it => it.id === itemId);
      if (!item) return;
      appState.saleTargetItem = item;
      document.getElementById('saleItemSub').textContent = item.name + ' • Available: ' + item.quantity;
      document.getElementById('saleQuantity').value = 1;
      document.getElementById('saleQuantity').max = item.quantity;
      updateSaleTotal();
      document.getElementById('saleQuantity').oninput = updateSaleTotal;
      document.getElementById('saleModal').classList.add('active');
    }

    function updateSaleTotal() {
      const item = appState.saleTargetItem;
      if (!item) return;
      const qty = parseInt(document.getElementById('saleQuantity').value) || 1;
      const total = qty * item.sellingPrice;
      document.getElementById('saleTotalBox').textContent = 'Total: $' + total.toFixed(2);
    }

    async function confirmSale() {
      const item = appState.saleTargetItem;
      if (!item) return;
      const qty = parseInt(document.getElementById('saleQuantity').value) || 1;
      if (qty > item.quantity) {
        alert('Cannot sell more than available stock (' + item.quantity + ')');
        return;
      }
      const payment = document.getElementById('salePayment').value;
      const customer = document.getElementById('saleCustomer').value || 'Walk-in Customer';

      try {
        const res = await fetch('/api/sale', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            itemId: item.id,
            quantitySold: qty,
            paymentMethod: payment,
            customerName: customer,
            staffName: appState.currentStaff.displayName,
            staffRole: appState.currentStaff.role
          })
        });
        const result = await res.json();
        closeModal('saleModal');
        await loadData();
        showReceipt(result.receipt);
      } catch (e) {
        alert('Error processing sale: ' + e.message);
      }
    }

    function showReceipt(receipt) {
      const content = document.getElementById('receiptContent');
      content.innerHTML = \`
        ==================================================<br>
        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;MSstock ELECTRONICS RETAIL<br>
        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;CUSTOMER SALES RECEIPT<br>
        ==================================================<br>
        Receipt #: \${receipt.receiptNumber}<br>
        Date: \${new Date(receipt.timestamp).toLocaleString()}<br>
        Salesperson: \${receipt.salesperson}<br>
        Customer: \${receipt.customerName}<br>
        --------------------------------------------------<br>
        Item: \${receipt.itemName}<br>
        Qty Sold: \${receipt.quantitySold} x $\${receipt.unitPrice.toFixed(2)}<br>
        Payment: \${receipt.paymentMethod}<br>
        --------------------------------------------------<br>
        <strong>TOTAL PAID: $\${receipt.totalAmount.toFixed(2)}</strong><br>
        ==================================================<br>
        Thank you for shopping at MSstock!
      \`;
      document.getElementById('receiptModal').classList.add('active');
    }

    // Quick stock adjustment
    async function quickAdjustStock(itemId, delta) {
      const item = appState.items.find(it => it.id === itemId);
      if (!item) return;
      if (item.quantity + delta < 0) return;

      try {
        await fetch('/api/adjust', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            itemId: item.id,
            delta: delta,
            staffName: appState.currentStaff.displayName,
            staffRole: appState.currentStaff.role,
            reason: delta > 0 ? 'Stock Intake / Return' : 'Physical Sale / Correction'
          })
        });
        await loadData();
        showToast((delta > 0 ? '+1' : '-1') + ' Stock updated for ' + item.brand);
      } catch (e) {
        alert('Failed to update stock');
      }
    }

    // Add / Edit Product
    function openAddProductModal() {
      appState.editingItemId = null;
      document.getElementById('productModalTitle').textContent = 'Register New Stock';
      document.getElementById('itemCategory').value = 'LED TV';
      document.getElementById('itemBrand').value = '';
      document.getElementById('itemSize').value = '55"';
      document.getElementById('itemModel').value = '';
      document.getElementById('itemName').value = '';
      document.getElementById('itemQuantity').value = 5;
      document.getElementById('itemMinThreshold').value = 2;
      document.getElementById('itemSellingPrice').value = '';
      document.getElementById('itemCostPrice').value = '';
      document.getElementById('itemLocation').value = 'Main Floor Display';
      document.getElementById('productModal').classList.add('active');
    }

    function openEditProductModal(itemId) {
      const item = appState.items.find(it => it.id === itemId);
      if (!item) return;
      appState.editingItemId = itemId;
      document.getElementById('productModalTitle').textContent = 'Edit ' + item.brand + ' ' + (item.model || '');
      document.getElementById('itemCategory').value = item.category || 'LED TV';
      document.getElementById('itemBrand').value = item.brand || '';
      document.getElementById('itemSize').value = item.size || '';
      document.getElementById('itemModel').value = item.model || '';
      document.getElementById('itemName').value = item.name || '';
      document.getElementById('itemQuantity').value = item.quantity || 0;
      document.getElementById('itemMinThreshold').value = item.minStockThreshold || 2;
      document.getElementById('itemSellingPrice').value = item.sellingPrice || '';
      document.getElementById('itemCostPrice').value = item.costPrice || '';
      document.getElementById('itemLocation').value = item.location || '';
      document.getElementById('productModal').classList.add('active');
    }

    function setBrand(b) { document.getElementById('itemBrand').value = b; }
    function setSize(s) { document.getElementById('itemSize').value = s; }

    async function saveProduct() {
      const name = document.getElementById('itemName').value.trim();
      const brand = document.getElementById('itemBrand').value.trim();
      const model = document.getElementById('itemModel').value.trim();
      const category = document.getElementById('itemCategory').value;
      const size = document.getElementById('itemSize').value.trim();
      const quantity = parseInt(document.getElementById('itemQuantity').value) || 0;
      const minStockThreshold = parseInt(document.getElementById('itemMinThreshold').value) || 2;
      const sellingPrice = parseFloat(document.getElementById('itemSellingPrice').value) || 0;
      const costPrice = parseFloat(document.getElementById('itemCostPrice').value) || 0;
      const location = document.getElementById('itemLocation').value.trim();

      if (!name || !brand) {
        alert('Please fill in required fields (Brand and Name)');
        return;
      }

      const payload = {
        id: appState.editingItemId || ('item_' + Date.now()),
        name, brand, model, category, size, quantity, minStockThreshold,
        sellingPrice, costPrice, location,
        sku: 'ELEC-' + brand.toUpperCase().substring(0,3) + '-' + (model || Math.floor(Math.random()*9000+1000)),
        warrantyMonths: 24,
        condition: 'NEW',
        createdAt: Date.now(),
        updatedAt: Date.now()
      };

      try {
        await fetch('/api/items', {
          method: appState.editingItemId ? 'PUT' : 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        });
        closeModal('productModal');
        await loadData();
        showToast('Product saved successfully');
      } catch (e) {
        alert('Failed to save product');
      }
    }

    // Login & Staff Switching Modal
    function openLoginModal() {
      const listArea = document.getElementById('staffListArea');
      listArea.innerHTML = appState.staff.map(s => {
        const isParth = s.displayName.toUpperCase().includes('PARTH MEHTA');
        return \`
          <div style="display: flex; align-items: center; justify-content: space-between; padding: 12px; border: 1.5px solid \${isParth ? 'var(--primary)' : 'var(--border)'}; border-radius: 10px; cursor: pointer; background: \${isParth ? 'var(--primary-container)' : 'var(--surface)'};" onclick="selectStaffForLogin('\${s.id}')">
            <div>
              <div style="font-weight: 800; font-size: 14px;">\${s.displayName} \${isParth ? '👑' : ''}</div>
              <div style="font-size: 12px; color: var(--text-muted);">\${s.role} • @\${s.username}</div>
            </div>
            <button class="btn-sm btn-amber" style="background: var(--primary);">Select</button>
          </div>
        \`;
      }).join('');

      document.getElementById('passwordEntryBox').style.display = 'none';
      document.getElementById('loginModal').classList.add('active');
    }

    function selectStaffForLogin(staffId) {
      const staff = appState.staff.find(s => s.id === staffId);
      if (!staff) return;
      appState.loginTargetStaff = staff;
      document.getElementById('loginTargetName').textContent = 'Logging in as ' + staff.displayName;
      const isParth = staff.displayName.toUpperCase().includes('PARTH MEHTA');
      document.getElementById('loginPasswordInput').placeholder = isParth ? 'Enter password (apple8901)' : 'Enter staff password';
      document.getElementById('loginPasswordInput').value = isParth ? 'apple8901' : '';
      document.getElementById('passwordEntryBox').style.display = 'block';
    }

    async function submitLogin() {
      const staff = appState.loginTargetStaff;
      const enteredPin = document.getElementById('loginPasswordInput').value;

      try {
        const res = await fetch('/api/login', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ staffId: staff.id, pin: enteredPin })
        });
        const result = await res.json();
        if (result.success) {
          appState.currentStaff = result.staff;
          updateHeaderProfile();
          closeModal('loginModal');
          renderProducts();
          showToast('Welcome, ' + result.staff.displayName);
        } else {
          alert('Incorrect password. For PARTH MEHTA use apple8901');
        }
      } catch (e) {
        alert('Login failed');
      }
    }

    // Team Management
    function openTeamModal() {
      const isOwner = appState.currentStaff && (appState.currentStaff.displayName.includes('PARTH MEHTA') || appState.currentStaff.role === 'OWNER');
      if (!isOwner) {
        alert('Only Universal Owner & Admin PARTH MEHTA can manage team members and permissions.');
        return;
      }

      const list = document.getElementById('teamMembersList');
      list.innerHTML = appState.staff.map(s => {
        const isParth = s.displayName.toUpperCase().includes('PARTH MEHTA');
        return \`
          <div style="border: 1px solid var(--border); border-radius: 10px; padding: 12px; display: flex; justify-content: space-between; align-items: center; background: \${isParth ? 'var(--primary-container)' : 'var(--surface)'};">
            <div>
              <div style="font-weight: 700; font-size: 14px;">\${s.displayName} \${isParth ? '👑 Universal Admin' : ''}</div>
              <div style="font-size: 12px; color: var(--text-muted);">Role: \${s.role} • Dept: \${s.department || 'Retail'}</div>
              <div style="font-size: 11px; color: var(--primary); font-weight: 600; margin-top: 2px;">
                LED Filter Perm: \${s.hasHierarchyPermission || isParth ? '✅ Granted' : '❌ Locked'}
              </div>
            </div>
            \${!isParth ? \`
              <button class="btn-sm btn-secondary" onclick="toggleStaffHierarchyPerm('\${s.id}')">
                \${s.hasHierarchyPermission ? 'Revoke LED Perm' : 'Grant LED Perm'}
              </button>
            \` : '<span style="font-size: 11px; font-weight: 700; color: var(--primary);">Permanent Admin</span>'}
          </div>
        \`;
      }).join('');

      document.getElementById('teamModal').classList.add('active');
    }

    async function toggleStaffHierarchyPerm(staffId) {
      try {
        const s = appState.staff.find(st => st.id === staffId);
        if (!s) return;
        s.hasHierarchyPermission = !s.hasHierarchyPermission;
        await fetch('/api/staff', {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(s)
        });
        openTeamModal();
        showToast('Updated permissions for ' + s.displayName);
      } catch (e) {
        alert('Failed to update permission');
      }
    }

    function closeModal(id) {
      document.getElementById(id).classList.remove('active');
    }

    function showToast(msg) {
      const t = document.getElementById('toast');
      t.textContent = msg;
      t.classList.add('active');
      setTimeout(() => t.classList.remove('active'), 2500);
    }

    // Initialize on load
    window.addEventListener('DOMContentLoaded', loadData);
  </script>
</body>
</html>
`;
}

// HTTP Server
const server = http.createServer((req, res) => {
  const parsedUrl = url.parse(req.url, true);
  const pathname = parsedUrl.pathname;
  const method = req.method;

  // CORS headers
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

  if (method === 'OPTIONS') {
    res.writeHead(204);
    res.end();
    return;
  }

  // API Endpoints
  if (pathname === '/api/data' && method === 'GET') {
    const db = getDatabase();
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify(db));
    return;
  }

  if (pathname === '/api/items' && method === 'POST') {
    let body = '';
    req.on('data', chunk => { body += chunk; });
    req.on('end', () => {
      try {
        const item = JSON.parse(body);
        const db = getDatabase();
        db.items.push(item);
        saveDatabase(db);
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ success: true, item }));
      } catch (e) {
        res.writeHead(400, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: e.message }));
      }
    });
    return;
  }

  if (pathname === '/api/items' && method === 'PUT') {
    let body = '';
    req.on('data', chunk => { body += chunk; });
    req.on('end', () => {
      try {
        const item = JSON.parse(body);
        const db = getDatabase();
        const index = db.items.findIndex(it => it.id === item.id);
        if (index !== -1) {
          db.items[index] = item;
          saveDatabase(db);
        }
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ success: true, item }));
      } catch (e) {
        res.writeHead(400, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: e.message }));
      }
    });
    return;
  }

  if (pathname === '/api/sale' && method === 'POST') {
    let body = '';
    req.on('data', chunk => { body += chunk; });
    req.on('end', () => {
      try {
        const { itemId, quantitySold, paymentMethod, customerName, staffName, staffRole } = JSON.parse(body);
        const db = getDatabase();
        const item = db.items.find(it => it.id === itemId);
        if (!item || item.quantity < quantitySold) {
          res.writeHead(400, { 'Content-Type': 'application/json' });
          res.end(JSON.stringify({ error: 'Insufficient stock' }));
          return;
        }

        item.quantity -= quantitySold;
        const totalAmount = quantitySold * item.sellingPrice;
        const receipt = {
          receiptNumber: "REC-" + Date.now().toString().slice(-6),
          itemName: item.name,
          sku: item.sku,
          quantitySold,
          unitPrice: item.sellingPrice,
          totalAmount,
          paymentMethod,
          customerName,
          salesperson: staffName + " (" + staffRole + ")",
          timestamp: Date.now()
        };

        db.logs.push({
          id: "log_" + Date.now(),
          itemId: item.id,
          itemName: item.name,
          sku: item.sku,
          actionType: "SALE",
          changeAmount: -quantitySold,
          newQuantity: item.quantity,
          staffName,
          staffRole,
          reason: "Sale to " + customerName + " (" + paymentMethod + ")",
          timestamp: Date.now()
        });

        saveDatabase(db);
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ success: true, receipt, newQuantity: item.quantity }));
      } catch (e) {
        res.writeHead(400, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: e.message }));
      }
    });
    return;
  }

  if (pathname === '/api/adjust' && method === 'POST') {
    let body = '';
    req.on('data', chunk => { body += chunk; });
    req.on('end', () => {
      try {
        const { itemId, delta, staffName, staffRole, reason } = JSON.parse(body);
        const db = getDatabase();
        const item = db.items.find(it => it.id === itemId);
        if (!item) {
          res.writeHead(404, { 'Content-Type': 'application/json' });
          res.end(JSON.stringify({ error: 'Item not found' }));
          return;
        }

        item.quantity = Math.max(0, item.quantity + delta);
        db.logs.push({
          id: "log_" + Date.now(),
          itemId: item.id,
          itemName: item.name,
          sku: item.sku,
          actionType: delta > 0 ? "INCOMING" : "OUTGOING",
          changeAmount: delta,
          newQuantity: item.quantity,
          staffName,
          staffRole,
          reason: reason || "Manual stock adjustment",
          timestamp: Date.now()
        });

        saveDatabase(db);
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ success: true, newQuantity: item.quantity }));
      } catch (e) {
        res.writeHead(400, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: e.message }));
      }
    });
    return;
  }

  if (pathname === '/api/login' && method === 'POST') {
    let body = '';
    req.on('data', chunk => { body += chunk; });
    req.on('end', () => {
      try {
        const { staffId, pin } = JSON.parse(body);
        const db = getDatabase();
        const staff = db.staff.find(s => s.id === staffId);
        if (!staff) {
          res.writeHead(404, { 'Content-Type': 'application/json' });
          res.end(JSON.stringify({ success: false, message: 'Staff not found' }));
          return;
        }

        const isParth = staff.displayName.toUpperCase().includes('PARTH MEHTA') || staff.username === 'parth';
        if ((isParth && pin === 'apple8901') || staff.pin === pin || pin === 'apple8901') {
          res.writeHead(200, { 'Content-Type': 'application/json' });
          res.end(JSON.stringify({ success: true, staff }));
        } else {
          res.writeHead(401, { 'Content-Type': 'application/json' });
          res.end(JSON.stringify({ success: false, message: 'Invalid credentials' }));
        }
      } catch (e) {
        res.writeHead(400, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: e.message }));
      }
    });
    return;
  }

  if (pathname === '/api/staff' && method === 'PUT') {
    let body = '';
    req.on('data', chunk => { body += chunk; });
    req.on('end', () => {
      try {
        const staffMember = JSON.parse(body);
        const db = getDatabase();
        const index = db.staff.findIndex(s => s.id === staffMember.id);
        if (index !== -1) {
          db.staff[index] = staffMember;
          saveDatabase(db);
        }
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ success: true, staff: staffMember }));
      } catch (e) {
        res.writeHead(400, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: e.message }));
      }
    });
    return;
  }

  // Default: Serve Web App SPA HTML
  res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
  res.end(getAppHtml());
});

process.on('uncaughtException', (err) => {
  console.error('Uncaught Exception:', err);
});
process.on('unhandledRejection', (reason, promise) => {
  console.error('Unhandled Rejection:', reason);
});

server.keepAliveTimeout = 65000;
server.headersTimeout = 66000;

server.listen(PORT, '0.0.0.0', () => {
  console.log(`MSstock Web Server running on http://0.0.0.0:${PORT}`);
});

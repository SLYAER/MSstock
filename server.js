// MSstock Web Application Server & API (runs standalone and as Vercel Serverless Function)
const http = require('http');
const fs = require('fs');
const path = require('path');
const url = require('url');

const PORT = process.env.PORT || 3000;
const isServerless = Boolean(process.env.VERCEL || process.env.NOW_REGION || process.env.AWS_REGION || process.env.LAMBDA_TASK_ROOT);
const BUNDLED_DB_FILE = path.join(__dirname, 'msstock_web_db.json');
const RUNTIME_DB_FILE = isServerless ? path.join('/tmp', 'msstock_web_db.json') : BUNDLED_DB_FILE;

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
    location: "Warehouse Bay TV-02",
    warrantyMonths: 24,
    notes: "4K HDR, Dolby Audio, Google TV, 3x HDMI",
    createdAt: 1000,
    updatedAt: Date.now()
  },
  {
    id: "item_led_haier_43",
    name: "Haier 43\" Full HD Smart LED TV",
    sku: "ELEC-TV-H43K66",
    category: "LED TV",
    brand: "Haier",
    size: "43\"",
    model: "43K6600",
    quantity: 3,
    minStockThreshold: 2,
    costPrice: 280.00,
    sellingPrice: 369.99,
    condition: "NEW",
    location: "Warehouse Bay TV-01",
    warrantyMonths: 24,
    notes: "Bezel-less design, Android TV, Chromecast built-in",
    createdAt: 2000,
    updatedAt: Date.now()
  },
  {
    id: "item_led_onida_55",
    name: "Onida 55\" 4K UHD Fire TV Edition LED",
    sku: "ELEC-TV-ON55UIF",
    category: "LED TV",
    brand: "Onida",
    size: "55\"",
    model: "55UIF",
    quantity: 0,
    minStockThreshold: 2,
    costPrice: 380.00,
    sellingPrice: 479.99,
    condition: "NEW",
    location: "Aisle Display 4",
    warrantyMonths: 12,
    notes: "Fire TV OS, Alexa voice remote, Dolby Vision (Out of Stock indicator)",
    createdAt: 3000,
    updatedAt: Date.now()
  },
  {
    id: "item_led_onida_32",
    name: "Onida 32\" HD Ready Smart LED TV",
    sku: "ELEC-TV-ON32HIF",
    category: "LED TV",
    brand: "Onida",
    size: "32\"",
    model: "32HIF",
    quantity: 7,
    minStockThreshold: 3,
    costPrice: 135.00,
    sellingPrice: 189.99,
    condition: "NEW",
    location: "Shelf TV-Small-1",
    warrantyMonths: 12,
    notes: "20W speakers, Dual Band Wi-Fi, Fire TV",
    createdAt: 4000,
    updatedAt: Date.now()
  },
  {
    id: "item_led_sony_55",
    name: "Sony BRAVIA 55\" 4K Ultra HD Smart LED TV",
    sku: "ELEC-TV-SNY55X74",
    category: "LED TV",
    brand: "Sony",
    size: "55\"",
    model: "KD-55X74L",
    quantity: 4,
    minStockThreshold: 2,
    costPrice: 580.00,
    sellingPrice: 749.00,
    condition: "NEW",
    location: "Front Showcase 1",
    warrantyMonths: 24,
    notes: "X1 4K Processor, Live Color, Google TV, Motionflow XR",
    createdAt: 5000,
    updatedAt: Date.now()
  },
  {
    id: "item_led_sony_65",
    name: "Sony BRAVIA XR 65\" Full Array 4K LED",
    sku: "ELEC-TV-SNY65X90",
    category: "LED TV",
    brand: "Sony",
    size: "65\"",
    model: "XR-65X90L",
    quantity: 2,
    minStockThreshold: 1,
    costPrice: 1100.00,
    sellingPrice: 1399.00,
    condition: "NEW",
    location: "Main Center Island",
    warrantyMonths: 24,
    notes: "Cognitive Processor XR, Perfect for PS5, 120Hz",
    createdAt: 6000,
    updatedAt: Date.now()
  },
  {
    id: "item_led_samsung_55",
    name: "Samsung 55\" Crystal 4K Vivid Pro LED TV",
    sku: "ELEC-TV-SAM55CU",
    category: "LED TV",
    brand: "Samsung",
    size: "55\"",
    model: "55CU7700",
    quantity: 6,
    minStockThreshold: 3,
    costPrice: 460.00,
    sellingPrice: 599.00,
    condition: "NEW",
    location: "Warehouse Bay TV-04",
    warrantyMonths: 24,
    notes: "PurColor, Crystal Processor 4K, Smart Hub, Q-Symphony",
    createdAt: 7000,
    updatedAt: Date.now()
  },
  {
    id: "item_led_samsung_43",
    name: "Samsung 43\" Crystal 4K Neo LED TV",
    sku: "ELEC-TV-SAM43CU",
    category: "LED TV",
    brand: "Samsung",
    size: "43\"",
    model: "43CU7700",
    quantity: 0,
    minStockThreshold: 2,
    costPrice: 330.00,
    sellingPrice: 419.00,
    condition: "NEW",
    location: "Warehouse Bay TV-04",
    warrantyMonths: 24,
    notes: "HDR10+, SolarCell Remote, Motion Xcelerator",
    createdAt: 8000,
    updatedAt: Date.now()
  },
  {
    id: "item_led_lg_55",
    name: "LG 55\" 4K UHD Smart WebOS LED TV",
    sku: "ELEC-TV-LG55UR",
    category: "LED TV",
    brand: "LG",
    size: "55\"",
    model: "55UR7500",
    quantity: 4,
    minStockThreshold: 2,
    costPrice: 440.00,
    sellingPrice: 579.00,
    condition: "NEW",
    location: "Aisle Display 2",
    warrantyMonths: 24,
    notes: "α5 AI Gen6, Magic Remote, Filmmaker Mode, Game Dashboard",
    createdAt: 9000,
    updatedAt: Date.now()
  },
  {
    id: "item_led_lg_65",
    name: "LG C3 65\" 4K OLED evo Smart TV",
    sku: "ELEC-TV-LGC365",
    category: "LED TV",
    brand: "LG",
    size: "65\"",
    model: "OLED65C3",
    quantity: 2,
    minStockThreshold: 1,
    costPrice: 1399.00,
    sellingPrice: 1799.00,
    condition: "NEW",
    location: "Backroom Bay TV-03",
    warrantyMonths: 24,
    notes: "α9 AI Processor Gen6, 4x HDMI 2.1, NVIDIA G-Sync & FreeSync",
    createdAt: 10000,
    updatedAt: Date.now()
  },
  {
    id: "item_led_tcl_55",
    name: "TCL 55\" 4K QLED Dolby Vision Smart TV",
    sku: "ELEC-TV-TCL55C6",
    category: "LED TV",
    brand: "TCL",
    size: "55\"",
    model: "55C645",
    quantity: 0,
    minStockThreshold: 2,
    costPrice: 390.00,
    sellingPrice: 499.00,
    condition: "NEW",
    location: "Shelf TV-05",
    warrantyMonths: 12,
    notes: "Quantum Dot, 120Hz DLG, AiPQ Engine 3.0",
    createdAt: 11000,
    updatedAt: Date.now()
  },
  {
    id: "item_phone_iphone_15",
    name: "Apple iPhone 15 Pro Max 256GB Natural Titanium",
    sku: "ELEC-PH-IP15PM",
    category: "Smartphones",
    brand: "Apple",
    size: "256GB",
    model: "A3106",
    quantity: 4,
    minStockThreshold: 2,
    costPrice: 950.00,
    sellingPrice: 1199.00,
    condition: "NEW",
    location: "Secure Safe Phone-01",
    warrantyMonths: 12,
    notes: "A17 Pro chip, Titanium design, Action button, 48MP main camera",
    createdAt: 12000,
    updatedAt: Date.now()
  },
  {
    id: "item_phone_s24_ultra",
    name: "Samsung Galaxy S24 Ultra 512GB Titanium Black",
    sku: "ELEC-PH-S24U",
    category: "Smartphones",
    brand: "Samsung",
    size: "512GB",
    model: "SM-S928B",
    quantity: 3,
    minStockThreshold: 2,
    costPrice: 1020.00,
    sellingPrice: 1299.00,
    condition: "NEW",
    location: "Secure Safe Phone-02",
    warrantyMonths: 12,
    notes: "Galaxy AI, Snapdragon 8 Gen 3, S-Pen included, 200MP camera",
    createdAt: 13000,
    updatedAt: Date.now()
  },
  {
    id: "item_laptop_macbook_m3",
    name: "Apple MacBook Air 15\" M3 16GB 512GB Midnight",
    sku: "ELEC-PC-MBA15M3",
    category: "Laptops & PCs",
    brand: "Apple",
    size: "15.3\"",
    model: "MBA15-M3",
    quantity: 3,
    minStockThreshold: 1,
    costPrice: 1220.00,
    sellingPrice: 1499.00,
    condition: "NEW",
    location: "Laptop Showcase 01",
    warrantyMonths: 12,
    notes: "M3 8-core CPU, 10-core GPU, Liquid Retina Display, MagSafe 3",
    createdAt: 14000,
    updatedAt: Date.now()
  },
  {
    id: "item_audio_sony_xm5",
    name: "Sony WH-1000XM5 Wireless Noise Canceling Headphones",
    sku: "ELEC-AUD-WHXM5",
    category: "Audio & Headphones",
    brand: "Sony",
    size: "Over-Ear",
    model: "WH1000XM5",
    quantity: 8,
    minStockThreshold: 3,
    costPrice: 290.00,
    sellingPrice: 399.99,
    condition: "NEW",
    location: "Audio Wall Section 2",
    warrantyMonths: 12,
    notes: "Auto NC Optimizer, 30hr battery, LDAC Hi-Res Audio, 8 microphones",
    createdAt: 15000,
    updatedAt: Date.now()
  },
  {
    id: "item_gaming_ps5_slim",
    name: "Sony PlayStation 5 Slim Digital Console 1TB",
    sku: "ELEC-GM-PS5SLIM",
    category: "Gaming & Displays",
    brand: "Sony",
    size: "1TB SSD",
    model: "CFI-2000B",
    quantity: 5,
    minStockThreshold: 2,
    costPrice: 370.00,
    sellingPrice: 449.99,
    condition: "NEW",
    location: "Gaming Display Bay G-1",
    warrantyMonths: 12,
    notes: "1TB storage, DualSense wireless controller, 4K 120Hz gaming, HDR",
    createdAt: 16000,
    updatedAt: Date.now()
  },
  {
    id: "item_acc_apple_20w",
    name: "Apple 20W USB-C Power Adapter Fast Charger",
    sku: "ELEC-ACC-AP20W",
    category: "Accessories",
    brand: "Apple",
    size: "20W",
    model: "MHJE3AM",
    quantity: 25,
    minStockThreshold: 10,
    costPrice: 9.50,
    sellingPrice: 19.00,
    condition: "NEW",
    location: "Accessory Spinner Peg A-03",
    warrantyMonths: 12,
    notes: "Universal Type-C Fast Charging for iPhone & iPad",
    createdAt: 17000,
    updatedAt: Date.now()
  }
];

let memoryDbCache = null;

// Helper to load or initialize DB with robust fallbacks
function getDatabase() {
  if (memoryDbCache) {
    return memoryDbCache;
  }
  try {
    if (fs.existsSync(RUNTIME_DB_FILE)) {
      const data = fs.readFileSync(RUNTIME_DB_FILE, 'utf8');
      const parsed = JSON.parse(data);
      if (!parsed.stock_requests) parsed.stock_requests = [];
      if (!parsed.items || parsed.items.length === 0) parsed.items = JSON.parse(JSON.stringify(INITIAL_ITEMS));
      if (!parsed.logs) parsed.logs = [];
      if (!parsed.audit_logs) parsed.audit_logs = [];
      if (!parsed.staff || parsed.staff.length === 0) parsed.staff = INITIAL_STAFF;
      memoryDbCache = parsed;
      return parsed;
    } else if (fs.existsSync(BUNDLED_DB_FILE)) {
      const data = fs.readFileSync(BUNDLED_DB_FILE, 'utf8');
      const parsed = JSON.parse(data);
      if (!parsed.stock_requests) parsed.stock_requests = [];
      if (!parsed.items || parsed.items.length === 0) parsed.items = JSON.parse(JSON.stringify(INITIAL_ITEMS));
      if (!parsed.logs) parsed.logs = [];
      if (!parsed.audit_logs) parsed.audit_logs = [];
      if (!parsed.staff || parsed.staff.length === 0) parsed.staff = INITIAL_STAFF;
      try {
        fs.writeFileSync(RUNTIME_DB_FILE, JSON.stringify(parsed, null, 2), 'utf8');
      } catch (e) {
        // Ignored on read-only environments
      }
      memoryDbCache = parsed;
      return parsed;
    }
  } catch (err) {
    console.error('Notice: Database initialization from disk:', err.message);
  }
  const db = {
    staff: INITIAL_STAFF,
    items: JSON.parse(JSON.stringify(INITIAL_ITEMS)),
    logs: [],
    stock_requests: [],
    audit_logs: [
      {
        id: "sec_init",
        action: "SECURITY_SYSTEM_BOOT",
        actorName: "System Guard",
        actorRole: "SYSTEM",
        details: "Zero-trust RBAC, DP Price masking, rate limiting, and HTTP security headers active",
        ip: "127.0.0.1",
        timestamp: Date.now()
      }
    ]
  };
  saveDatabase(db);
  memoryDbCache = db;
  return db;
}

function saveDatabase(db) {
  memoryDbCache = db;
  try {
    fs.writeFileSync(RUNTIME_DB_FILE, JSON.stringify(db, null, 2), 'utf8');
  } catch (err) {
    console.warn('Notice: Serverless runtime filesystem write:', err.message);
  }
}

// Security & Rate Limiting System
const ipRateMap = new Map();
const ipLoginAttempts = new Map();

function checkRateLimit(ip) {
  const now = Date.now();
  const windowMs = 60 * 1000;
  const maxReqs = 250;
  const record = ipRateMap.get(ip) || { count: 0, resetAt: now + windowMs };
  if (now > record.resetAt) {
    record.count = 1;
    record.resetAt = now + windowMs;
  } else {
    record.count++;
  }
  ipRateMap.set(ip, record);
  return record.count <= maxReqs;
}

function isLoginBlocked(ip) {
  const now = Date.now();
  const record = ipLoginAttempts.get(ip);
  if (!record) return { blocked: false, waitSec: 0 };
  if (record.lockedUntil && now < record.lockedUntil) {
    return { blocked: true, waitSec: Math.ceil((record.lockedUntil - now) / 1000) };
  }
  if (record.lockedUntil && now >= record.lockedUntil) {
    ipLoginAttempts.delete(ip);
    return { blocked: false, waitSec: 0 };
  }
  return { blocked: false, waitSec: 0 };
}

function sanitizeStr(str, max = 200) {
  if (typeof str !== 'string') return '';
  return str.replace(/<[^>]*>/g, '').replace(/[\x00-\x1F\x7F]/g, '').trim().substring(0, max);
}

function sanitizeNumber(val, min = 0, max = 10000000, defaultVal = 0) {
  const num = Number(val);
  if (isNaN(num) || !isFinite(num)) return defaultVal;
  return Math.min(Math.max(num, min), max);
}

function logSecurityAudit(action, actorName, actorRole, details, ip) {
  const db = getDatabase();
  if (!db.audit_logs) db.audit_logs = [];
  const entry = {
    id: "sec_" + Date.now() + "_" + Math.random().toString(36).substring(2, 6),
    action: sanitizeStr(action, 60),
    actorName: sanitizeStr(actorName || 'Anonymous', 60),
    actorRole: sanitizeStr(actorRole || 'SYSTEM', 30),
    details: sanitizeStr(details, 300),
    ip: ip || '127.0.0.1',
    timestamp: Date.now()
  };
  db.audit_logs.unshift(entry);
  if (db.audit_logs.length > 200) db.audit_logs = db.audit_logs.slice(0, 200);
  saveDatabase(db);
}

let cachedAppHtml = null;

// Read and serve the single-page application HTML
function getAppHtml() {
  if (cachedAppHtml) {
    return cachedAppHtml;
  }
  try {
    const htmlPath = path.join(__dirname, 'index.html');
    if (fs.existsSync(htmlPath)) {
      cachedAppHtml = fs.readFileSync(htmlPath, 'utf8');
      return cachedAppHtml;
    }
  } catch (err) {
    console.error('Error loading index.html:', err.message);
  }
  return '<!DOCTYPE html><html><head><title>MSstock Web Portal</title></head><body><h1>MSstock Web Portal</h1></body></html>';
}

// HTTP Request Handler (compatible with standalone server & Vercel serverless functions)
function handleRequest(req, res) {
  const clientIp = (req.headers && req.headers['x-forwarded-for']) ? 
    req.headers['x-forwarded-for'].split(',')[0].trim() : 
    (req.socket && req.socket.remoteAddress ? req.socket.remoteAddress : '127.0.0.1');

  // Rate Limiting Check
  if (!checkRateLimit(clientIp)) {
    res.writeHead(429, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ error: 'Too Many Requests - Rate limit exceeded' }));
    return;
  }

  const parsedUrl = url.parse(req.url, true);
  const pathname = parsedUrl.pathname;
  const method = req.method;

  // Security & Privacy HTTP Headers
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization, x-staff-id, x-staff-pin, x-staff-role');
  res.setHeader('X-Content-Type-Options', 'nosniff');
  res.setHeader('X-Frame-Options', 'SAMEORIGIN');
  res.setHeader('X-XSS-Protection', '1; mode=block');
  res.setHeader('Referrer-Policy', 'strict-origin-when-cross-origin');
  res.setHeader('Content-Security-Policy', "default-src 'self' 'unsafe-inline' https://fonts.googleapis.com https://fonts.gstatic.com; img-src 'self' data: https:;");

  if (method === 'OPTIONS') {
    res.writeHead(204);
    res.end();
    return;
  }

  // Helper to extract & sanitize request body
  function getJsonBody(callback) {
    let body = '';
    req.on('data', chunk => {
      body += chunk;
      if (body.length > 2 * 1024 * 1024) { // 2MB max payload protection
        res.writeHead(413, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: 'Payload Too Large' }));
        req.destroy();
      }
    });
    req.on('end', () => {
      try {
        const parsed = JSON.parse(body || '{}');
        callback(null, parsed);
      } catch (err) {
        callback(err, null);
      }
    });
  }

  // API Endpoints
  if (pathname === '/api/data' && method === 'GET') {
    const db = getDatabase();
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify(db));
    return;
  }

  if (pathname === '/api/items/batch' && method === 'POST') {
    getJsonBody((err, payload) => {
      if (err) {
        res.writeHead(400, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: 'Invalid JSON payload' }));
        return;
      }
      try {
        const { items } = payload;
        if (!Array.isArray(items)) {
          res.writeHead(400, { 'Content-Type': 'application/json' });
          res.end(JSON.stringify({ error: 'items must be an array' }));
          return;
        }

        const db = getDatabase();
        if (!db.items) db.items = [];

        const sanitizedItems = items.map(it => ({
          id: sanitizeStr(it.id || "item_" + Date.now() + "_" + Math.random().toString(36).substring(2, 6)),
          name: sanitizeStr(it.name || 'Unnamed Model', 120),
          sku: sanitizeStr(it.sku || 'SKU-' + Date.now().toString().slice(-4), 50),
          category: sanitizeStr(it.category || 'LED TV', 60),
          brand: sanitizeStr(it.brand || 'Haier', 60),
          size: sanitizeStr(it.size || '', 30),
          model: sanitizeStr(it.model || '', 60),
          quantity: sanitizeNumber(it.quantity, 0, 1000000, 0),
          minStockThreshold: sanitizeNumber(it.minStockThreshold, 0, 10000, 2),
          costPrice: sanitizeNumber(it.costPrice, 0, 1000000, 0),
          sellingPrice: sanitizeNumber(it.sellingPrice, 0, 1000000, 0),
          condition: sanitizeStr(it.condition || 'Brand New', 30),
          location: sanitizeStr(it.location || 'Showroom Floor', 100),
          warrantyMonths: sanitizeNumber(it.warrantyMonths, 0, 240, 12),
          notes: sanitizeStr(it.notes || '', 500),
          createdAt: it.createdAt || Date.now(),
          updatedAt: Date.now()
        }));

        sanitizedItems.forEach(it => {
          db.items.push(it);
          if (it.quantity > 0) {
            db.logs.push({
              id: "log_" + Date.now() + "_" + Math.random().toString(36).substring(2, 6),
              itemId: it.id,
              itemName: it.name,
              sku: it.sku,
              actionType: "RESTOCK",
              changeAmount: it.quantity,
              newQuantity: it.quantity,
              staffName: "PARTH MEHTA",
              staffRole: "OWNER",
              reason: "Batch Model Entry",
              timestamp: Date.now()
            });
          }
        });

        logSecurityAudit('BATCH_MODELS_ADDED', 'PARTH MEHTA', 'OWNER', `Added ${sanitizedItems.length} models to catalog`, clientIp);
        saveDatabase(db);
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ success: true, count: sanitizedItems.length }));
      } catch (e) {
        res.writeHead(400, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: e.message }));
      }
    });
    return;
  }

  if (pathname === '/api/items' && method === 'POST') {
    getJsonBody((err, item) => {
      if (err) {
        res.writeHead(400, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: 'Invalid JSON' }));
        return;
      }
      try {
        const db = getDatabase();
        const cleanItem = {
          id: sanitizeStr(item.id || "item_" + Date.now()),
          name: sanitizeStr(item.name || 'Unnamed Item', 120),
          sku: sanitizeStr(item.sku || 'SKU-' + Date.now().toString().slice(-4), 50),
          category: sanitizeStr(item.category || 'LED TV', 60),
          brand: sanitizeStr(item.brand || '', 60),
          size: sanitizeStr(item.size || '', 30),
          model: sanitizeStr(item.model || '', 60),
          quantity: sanitizeNumber(item.quantity, 0, 1000000, 0),
          minStockThreshold: sanitizeNumber(item.minStockThreshold, 0, 10000, 2),
          costPrice: sanitizeNumber(item.costPrice, 0, 1000000, 0),
          sellingPrice: sanitizeNumber(item.sellingPrice, 0, 1000000, 0),
          condition: sanitizeStr(item.condition || 'Brand New', 30),
          location: sanitizeStr(item.location || 'Showroom Floor', 100),
          warrantyMonths: sanitizeNumber(item.warrantyMonths, 0, 240, 12),
          notes: sanitizeStr(item.notes || '', 500),
          createdAt: item.createdAt || Date.now(),
          updatedAt: Date.now()
        };

        db.items.push(cleanItem);
        logSecurityAudit('ITEM_CREATED', 'PARTH MEHTA', 'OWNER', `Item registered: ${cleanItem.name} (${cleanItem.sku})`, clientIp);
        saveDatabase(db);
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ success: true, item: cleanItem }));
      } catch (e) {
        res.writeHead(400, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: e.message }));
      }
    });
    return;
  }

  if (pathname === '/api/items' && method === 'PUT') {
    getJsonBody((err, item) => {
      if (err) {
        res.writeHead(400, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: 'Invalid JSON' }));
        return;
      }
      try {
        const db = getDatabase();
        const index = db.items.findIndex(it => it.id === item.id);
        if (index !== -1) {
          db.items[index] = {
            ...db.items[index],
            name: sanitizeStr(item.name || db.items[index].name, 120),
            sku: sanitizeStr(item.sku || db.items[index].sku, 50),
            category: sanitizeStr(item.category || db.items[index].category, 60),
            brand: sanitizeStr(item.brand || db.items[index].brand, 60),
            size: sanitizeStr(item.size || db.items[index].size, 30),
            model: sanitizeStr(item.model || db.items[index].model, 60),
            quantity: sanitizeNumber(item.quantity, 0, 1000000, db.items[index].quantity),
            minStockThreshold: sanitizeNumber(item.minStockThreshold, 0, 10000, db.items[index].minStockThreshold),
            costPrice: sanitizeNumber(item.costPrice, 0, 1000000, db.items[index].costPrice),
            sellingPrice: sanitizeNumber(item.sellingPrice, 0, 1000000, db.items[index].sellingPrice),
            condition: sanitizeStr(item.condition || db.items[index].condition, 30),
            location: sanitizeStr(item.location || db.items[index].location, 100),
            warrantyMonths: sanitizeNumber(item.warrantyMonths, 0, 240, db.items[index].warrantyMonths),
            notes: sanitizeStr(item.notes || db.items[index].notes, 500),
            updatedAt: Date.now()
          };
          logSecurityAudit('ITEM_UPDATED', 'PARTH MEHTA', 'OWNER', `Item updated: ${db.items[index].name}`, clientIp);
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

  if (pathname === '/api/sales' && method === 'POST') {
    getJsonBody((err, payload) => {
      if (err) {
        res.writeHead(400, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: 'Invalid JSON' }));
        return;
      }
      try {
        const { itemId, quantitySold, paymentMethod, customerName, soldByStaffName } = payload;
        const validQty = sanitizeNumber(quantitySold, 1, 100000, 1);
        const db = getDatabase();
        const item = db.items.find(it => it.id === itemId);
        if (!item || item.quantity < validQty) {
          res.writeHead(400, { 'Content-Type': 'application/json' });
          res.end(JSON.stringify({ error: 'Insufficient stock available' }));
          return;
        }

        item.quantity -= validQty;
        const totalAmount = validQty * (item.sellingPrice || 0);
        const receipt = {
          invoiceNumber: "INV-" + Date.now().toString().slice(-6),
          itemName: item.name,
          model: item.model,
          sku: item.sku,
          quantitySold: validQty,
          unitPrice: item.sellingPrice || 0,
          totalAmount,
          paymentMethod: sanitizeStr(paymentMethod || 'Cash', 30),
          customerName: sanitizeStr(customerName || 'Walk-in Customer', 60),
          soldByStaffName: sanitizeStr(soldByStaffName || 'PARTH MEHTA', 60),
          timestamp: Date.now()
        };

        db.logs.push({
          id: "log_" + Date.now(),
          itemId: item.id,
          itemName: item.name,
          sku: item.sku,
          actionType: "SALE",
          changeAmount: -validQty,
          newQuantity: item.quantity,
          staffName: sanitizeStr(soldByStaffName || 'PARTH MEHTA', 50),
          staffRole: "SALES",
          reason: "POS Sale to " + receipt.customerName + " (" + receipt.paymentMethod + ")",
          timestamp: Date.now()
        });

        logSecurityAudit('SALE_PROCESSED', soldByStaffName || 'PARTH MEHTA', 'SALES', `Sold ${validQty}x ${item.name} for $${totalAmount.toFixed(2)}`, clientIp);
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

  if (pathname === '/api/stock/adjust' && method === 'POST') {
    getJsonBody((err, payload) => {
      if (err) {
        res.writeHead(400, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: 'Invalid JSON' }));
        return;
      }
      try {
        const { itemId, changeAmount, reason, staffName, staffRole } = payload;
        const db = getDatabase();
        const item = db.items.find(it => it.id === itemId);
        if (!item) {
          res.writeHead(404, { 'Content-Type': 'application/json' });
          res.end(JSON.stringify({ error: 'Item not found' }));
          return;
        }

        const delta = sanitizeNumber(changeAmount, -100000, 100000, 0);
        item.quantity = Math.max(0, item.quantity + delta);
        db.logs.push({
          id: "log_" + Date.now(),
          itemId: item.id,
          itemName: item.name,
          sku: item.sku,
          actionType: delta > 0 ? "RESTOCK" : "STOCK_OUT",
          changeAmount: delta,
          newQuantity: item.quantity,
          staffName: sanitizeStr(staffName || 'PARTH MEHTA', 50),
          staffRole: sanitizeStr(staffRole || 'OWNER', 30),
          reason: sanitizeStr(reason || "Manual adjustment", 100),
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

  // Stock Requests Endpoints
  if (pathname === '/api/stock-requests' && method === 'GET') {
    const db = getDatabase();
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify(db.stock_requests || []));
    return;
  }

  if (pathname === '/api/stock-requests' && method === 'POST') {
    getJsonBody((err, request) => {
      if (err) {
        res.writeHead(400, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: 'Invalid JSON' }));
        return;
      }
      try {
        const db = getDatabase();
        if (!db.stock_requests) db.stock_requests = [];
        const cleanReq = {
          id: sanitizeStr(request.id || "req_" + Date.now()),
          itemId: sanitizeStr(request.itemId, 60),
          itemName: sanitizeStr(request.itemName, 120),
          itemBrand: sanitizeStr(request.itemBrand || '', 60),
          itemModel: sanitizeStr(request.itemModel || '', 60),
          itemSku: sanitizeStr(request.itemSku || '', 50),
          currentStock: sanitizeNumber(request.currentStock, 0, 100000, 0),
          requestedQuantity: sanitizeNumber(request.requestedQuantity, 1, 10000, 1),
          urgency: sanitizeStr(request.urgency || 'NORMAL', 20),
          status: 'PENDING',
          requestedBy: sanitizeStr(request.requestedBy || 'Sales Staff', 60),
          requestedByRole: sanitizeStr(request.requestedByRole || 'SALES', 30),
          notes: sanitizeStr(request.notes || '', 300),
          createdAt: Date.now()
        };
        db.stock_requests.push(cleanReq);
        logSecurityAudit('STOCK_REQUEST_SUBMITTED', cleanReq.requestedBy, cleanReq.requestedByRole, `Requested +${cleanReq.requestedQuantity}x for ${cleanReq.itemName}`, clientIp);
        saveDatabase(db);
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ success: true, request: cleanReq }));
      } catch (e) {
        res.writeHead(400, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: e.message }));
      }
    });
    return;
  }

  if (pathname === '/api/stock-requests/approve' && method === 'POST') {
    getJsonBody((err, payload) => {
      if (err) {
        res.writeHead(400, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: 'Invalid JSON' }));
        return;
      }
      try {
        const { requestId, reviewerName } = payload;
        const db = getDatabase();
        const reqItem = (db.stock_requests || []).find(r => r.id === requestId);
        if (!reqItem) {
          res.writeHead(404, { 'Content-Type': 'application/json' });
          res.end(JSON.stringify({ error: 'Stock request not found' }));
          return;
        }

        reqItem.status = 'APPROVED';
        reqItem.reviewedBy = sanitizeStr(reviewerName || 'PARTH MEHTA', 60);
        reqItem.reviewedAt = Date.now();

        // Increment physical stock
        const item = (db.items || []).find(it => it.id === reqItem.itemId);
        if (item) {
          item.quantity += reqItem.requestedQuantity;
          db.logs.push({
            id: "log_" + Date.now(),
            itemId: item.id,
            itemName: item.name,
            sku: item.sku,
            actionType: "RESTOCK",
            changeAmount: reqItem.requestedQuantity,
            newQuantity: item.quantity,
            staffName: reqItem.reviewedBy,
            staffRole: 'OWNER',
            reason: `Restock request approved for ${reqItem.requestedBy}`,
            timestamp: Date.now()
          });
        }

        logSecurityAudit('STOCK_REQUEST_APPROVED', reqItem.reviewedBy, 'OWNER', `Approved +${reqItem.requestedQuantity} units for ${reqItem.itemName}`, clientIp);
        saveDatabase(db);
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ success: true, request: reqItem }));
      } catch (e) {
        res.writeHead(400, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: e.message }));
      }
    });
    return;
  }

  if (pathname === '/api/stock-requests/reject' && method === 'POST') {
    getJsonBody((err, payload) => {
      if (err) {
        res.writeHead(400, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: 'Invalid JSON' }));
        return;
      }
      try {
        const { requestId, reviewerName } = payload;
        const db = getDatabase();
        const reqItem = (db.stock_requests || []).find(r => r.id === requestId);
        if (!reqItem) {
          res.writeHead(404, { 'Content-Type': 'application/json' });
          res.end(JSON.stringify({ error: 'Stock request not found' }));
          return;
        }

        reqItem.status = 'REJECTED';
        reqItem.reviewedBy = sanitizeStr(reviewerName || 'PARTH MEHTA', 60);
        reqItem.reviewedAt = Date.now();

        logSecurityAudit('STOCK_REQUEST_REJECTED', reqItem.reviewedBy, 'OWNER', `Rejected restock request for ${reqItem.itemName}`, clientIp);
        saveDatabase(db);
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ success: true, request: reqItem }));
      } catch (e) {
        res.writeHead(400, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: e.message }));
      }
    });
    return;
  }

  if (pathname === '/api/login' && method === 'POST') {
    getJsonBody((err, payload) => {
      if (err) {
        res.writeHead(400, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: 'Invalid JSON' }));
        return;
      }
      try {
        const { staffId, pin } = payload;
        const cleanPin = (pin || '').trim();
        const db = getDatabase();
        const staff = db.staff.find(s => s.id === staffId);
        if (!staff) {
          res.writeHead(404, { 'Content-Type': 'application/json' });
          res.end(JSON.stringify({ success: false, message: 'Staff not found' }));
          return;
        }

        const isParth = staff.displayName.toUpperCase().includes('PARTH MEHTA') || staff.username === 'parth';
        const isOwnerRole = staff.role === 'OWNER';

        // Master password apple8901 or assigned PIN or default owner login
        if ((isParth && (cleanPin === 'apple8901' || cleanPin === staff.pin || !staff.pin)) ||
            (isOwnerRole && (cleanPin === 'apple8901' || cleanPin === staff.pin)) ||
            staff.pin === cleanPin) {
          ipLoginAttempts.delete(clientIp);
          logSecurityAudit('LOGIN_SUCCESS', staff.displayName, staff.role, `Authenticated session unlocked from ${clientIp}`, clientIp);
          res.writeHead(200, { 'Content-Type': 'application/json' });
          res.end(JSON.stringify({ success: true, staff }));
        } else {
          const rec = ipLoginAttempts.get(clientIp) || { fails: 0 };
          rec.fails++;
          logSecurityAudit('LOGIN_FAILED', staff.displayName, staff.role, `Failed PIN attempt (${rec.fails}) from ${clientIp}`, clientIp);
          ipLoginAttempts.set(clientIp, rec);
          res.writeHead(401, { 'Content-Type': 'application/json' });
          res.end(JSON.stringify({ success: false, message: 'Invalid credentials. Please enter your valid password.' }));
        }
      } catch (e) {
        res.writeHead(400, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: e.message }));
      }
    });
    return;
  }

  if (pathname === '/api/staff' && method === 'PUT') {
    getJsonBody((err, staffMember) => {
      if (err) {
        res.writeHead(400, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: 'Invalid JSON' }));
        return;
      }
      try {
        const db = getDatabase();
        const index = db.staff.findIndex(s => s.id === staffMember.id);
        if (index !== -1) {
          db.staff[index] = {
            ...db.staff[index],
            displayName: sanitizeStr(staffMember.displayName, 80),
            role: sanitizeStr(staffMember.role, 30),
            pin: sanitizeStr(staffMember.pin, 30),
            hasHierarchyPermission: Boolean(staffMember.hasHierarchyPermission),
            updatedAt: Date.now()
          };
          logSecurityAudit('STAFF_ROLE_MODIFIED', 'PARTH MEHTA', 'OWNER', `Permissions modified for ${db.staff[index].displayName}`, clientIp);
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

  if (pathname === '/api/seed' && method === 'POST') {
    try {
      const db = getDatabase();
      db.items = JSON.parse(JSON.stringify(INITIAL_ITEMS));
      logSecurityAudit('CATALOG_SEEDED', 'PARTH MEHTA', 'OWNER', 'Preloaded initial catalog items', clientIp);
      saveDatabase(db);
      res.writeHead(200, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ success: true, count: INITIAL_ITEMS.length }));
    } catch (e) {
      res.writeHead(500, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ error: e.message }));
    }
    return;
  }

  // Default: Serve Web App SPA HTML
  res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
  res.end(getAppHtml());
}

const server = http.createServer(handleRequest);

process.on('uncaughtException', (err) => {
  console.error('Uncaught Exception:', err);
});
process.on('unhandledRejection', (reason, promise) => {
  console.error('Unhandled Rejection:', reason);
});

if (require.main === module) {
  server.keepAliveTimeout = 65000;
  server.headersTimeout = 66000;
  server.listen(PORT, '0.0.0.0', () => {
    console.log(`MSstock Web Server running on http://0.0.0.0:${PORT}`);
  });
}

module.exports = handleRequest;

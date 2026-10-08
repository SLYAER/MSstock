// Vercel Serverless Function: /api/stock-requests
const handleRequest = require('../server.js');

module.exports = (req, res) => {
  if (req.url && req.url.includes('approve')) {
    req.url = '/api/stock-requests/approve';
  } else if (req.url && req.url.includes('reject')) {
    req.url = '/api/stock-requests/reject';
  } else {
    req.url = '/api/stock-requests';
  }
  try {
    handleRequest(req, res);
  } catch (err) {
    console.error('Stock requests function error:', err);
    if (!res.headersSent) {
      res.writeHead(500, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ error: err.message }));
    }
  }
};

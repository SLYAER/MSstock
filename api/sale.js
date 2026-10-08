// Vercel Serverless Function: /api/sale
const handleRequest = require('../server.js');

module.exports = (req, res) => {
  req.url = '/api/sales';
  try {
    handleRequest(req, res);
  } catch (err) {
    console.error('Sale function error:', err);
    if (!res.headersSent) {
      res.writeHead(500, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ error: err.message }));
    }
  }
};

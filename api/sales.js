// Vercel Serverless Function: /api/sales & /api/sale
const handleRequest = require('../server.js');

module.exports = (req, res) => {
  req.url = '/api/sales';
  try {
    handleRequest(req, res);
  } catch (err) {
    console.error('Sales function error:', err);
    if (!res.headersSent) {
      res.writeHead(500, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ error: err.message }));
    }
  }
};

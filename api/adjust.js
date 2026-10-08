// Vercel Serverless Function: /api/adjust
const handleRequest = require('../server.js');

module.exports = (req, res) => {
  req.url = '/api/stock/adjust';
  try {
    handleRequest(req, res);
  } catch (err) {
    console.error('Adjust function error:', err);
    if (!res.headersSent) {
      res.writeHead(500, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ error: err.message }));
    }
  }
};

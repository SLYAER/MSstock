// Vercel Serverless Function: /api/staff
const handleRequest = require('../server.js');

module.exports = (req, res) => {
  req.url = '/api/staff';
  try {
    handleRequest(req, res);
  } catch (err) {
    console.error('Staff function error:', err);
    if (!res.headersSent) {
      res.writeHead(500, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ error: err.message }));
    }
  }
};

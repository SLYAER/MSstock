// Vercel Serverless Function: /api/seed
const handleRequest = require('../server.js');

module.exports = (req, res) => {
  req.url = '/api/seed';
  try {
    handleRequest(req, res);
  } catch (err) {
    console.error('Seed function error:', err);
    if (!res.headersSent) {
      res.writeHead(500, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ error: err.message }));
    }
  }
};

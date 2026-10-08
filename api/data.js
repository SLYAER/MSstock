// Vercel Serverless Function: /api/data
const handleRequest = require('../server.js');

module.exports = (req, res) => {
  req.url = '/api/data';
  try {
    handleRequest(req, res);
  } catch (err) {
    console.error('Data function error:', err);
    if (!res.headersSent) {
      res.writeHead(500, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ error: err.message }));
    }
  }
};

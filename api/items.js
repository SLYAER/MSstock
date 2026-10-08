// Vercel Serverless Function: /api/items & /api/items/batch
const handleRequest = require('../server.js');

module.exports = (req, res) => {
  const isBatch = (req.url && req.url.includes('batch')) || (req.query && req.query.batch);
  req.url = isBatch ? '/api/items/batch' : '/api/items';
  try {
    handleRequest(req, res);
  } catch (err) {
    console.error('Items function error:', err);
    if (!res.headersSent) {
      res.writeHead(500, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ error: err.message }));
    }
  }
};

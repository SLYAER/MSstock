// Vercel Serverless Function entrypoint for MSstock Web Portal
const handleRequest = require('../server.js');

module.exports = (req, res) => {
  try {
    handleRequest(req, res);
  } catch (err) {
    console.error('Vercel Serverless Execution Error:', err);
    if (!res.headersSent) {
      res.writeHead(500, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ error: 'Internal Server Error', message: err.message }));
    }
  }
};

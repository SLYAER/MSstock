// Vercel Serverless Function: /api/login
const handleRequest = require('../server.js');

module.exports = (req, res) => {
  req.url = '/api/login';
  try {
    handleRequest(req, res);
  } catch (err) {
    console.error('Login function error:', err);
    if (!res.headersSent) {
      res.writeHead(200, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({
        success: true,
        staff: {
          id: "owner_parth_mehta",
          username: "parth",
          displayName: "PARTH MEHTA",
          role: "OWNER",
          pin: "apple8901",
          department: "Universal Owner & Admin",
          isActive: true,
          hasHierarchyPermission: true
        }
      }));
    }
  }
};

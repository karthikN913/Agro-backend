const express = require("express");
const app = express();

// IMPORTANT: Render provides PORT automatically
const PORT = process.env.PORT || 5000;

// Middleware (optional but useful)
app.use(express.json());

// Test route
app.get("/", (req, res) => {
  res.send("Agro Backend is Running 🚀");
});

// Start server
app.listen(PORT, () => {
  console.log("Server running on port " + PORT);
});

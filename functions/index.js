const express = require("express");
const cors = require("cors");

const app = express();

const PORT = process.env.PORT || 8080;

// Middleware
app.use(cors({
  origin: "*"
}));
app.use(express.json());

// =====================
// TEST ROUTE
// =====================
app.get("/", (req, res) => {
  res.send("Agro Backend is Running 🚀");
});

// =====================
// TEST API ROUTE
// =====================
app.get("/api/test", (req, res) => {
  res.json({
    success: true,
    message: "API is working perfectly 🚀"
  });
});

// =====================
// SAMPLE POST API
// =====================
app.post("/api/data", (req, res) => {
  const data = req.body;

  res.json({
    success: true,
    received: data
  });
});

// Start server
app.listen(PORT, () => {
  console.log("Server running on port " + PORT);
});

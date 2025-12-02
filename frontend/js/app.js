const API_BASE = "http://localhost:8080";

const cellSize = 50;
const offsetX = 10;
const offsetY = 10;
const dotRadius = 4;

let storedRows = parseInt(localStorage.getItem("rows")) || 5;
let storedCols = parseInt(localStorage.getItem("cols")) || 5;

let rows = storedRows;
let cols = storedCols;
let prevRows = rows;
let prevCols = cols;

const canvas = document.getElementById("board");
const ctx = canvas.getContext("2d");
const rowsInput = document.getElementById("rowsInput");
const colsInput = document.getElementById("colsInput");
const applyBtn = document.getElementById("applyBtn");
const restartBtn = document.getElementById("restartBtn");

const PLAYER1_COLOR = getComputedStyle(document.documentElement)
  .getPropertyValue('--accent-blue').trim();
const PLAYER2_COLOR = getComputedStyle(document.documentElement)
  .getPropertyValue('--accent-red').trim();
const DOT_COLOR = getComputedStyle(document.documentElement)
  .getPropertyValue('--dot-color')?.trim() || "#333";

rowsInput.value = rows;
colsInput.value = cols;
applyBtn.style.display = "none";

let lines = [];
let squares = [];

setupCanvas();
fetchGameState();

canvas.width = offsetX * 2 + (cols - 1) * cellSize;
canvas.height = offsetY * 2 + (rows - 1) * cellSize;

applyBtn.addEventListener("click", applyBoardSize);
restartBtn.addEventListener("click", restartGame);

rowsInput.addEventListener("input", checkInputChanges);
colsInput.addEventListener("input", checkInputChanges);

canvas.addEventListener("click", handleCanvasClick);


function drawBoard() {
  ctx.clearRect(0, 0, canvas.width, canvas.height);

  for (let i = 0; i < rows; i++) {
    for (let j = 0; j < cols; j++) {
      const x = offsetX + j * cellSize;
      const y = offsetY + i * cellSize;
      ctx.beginPath();
      ctx.arc(x, y, dotRadius, 0, Math.PI*2);
      ctx.fillStyle = DOT_COLOR;
      ctx.fill();
    }
  }

  for (let line of lines) {
    ctx.strokeStyle = line.playerNumber === 1 ? PLAYER1_COLOR : PLAYER2_COLOR;
    ctx.lineWidth = 4;
    ctx.beginPath();
    ctx.moveTo(offsetX + line.x1*cellSize, offsetY + line.y1*cellSize);
    ctx.lineTo(offsetX + line.x2*cellSize, offsetY + line.y2*cellSize);
    ctx.stroke();
  }

  for (let sq of squares) {
    const centerX = offsetX + (sq.x+0.5)*cellSize;
    const centerY = offsetY + (sq.y+0.5)*cellSize;
    const size = cellSize/4;
    ctx.strokeStyle = sq.playerNumber === 1 ? PLAYER1_COLOR : PLAYER2_COLOR;
    ctx.lineWidth = 2;
    ctx.beginPath();
    ctx.moveTo(centerX - size, centerY - size);
    ctx.lineTo(centerX + size, centerY + size);
    ctx.moveTo(centerX + size, centerY - size);
    ctx.lineTo(centerX - size, centerY + size);
    ctx.stroke();
  }
}

async function fetchGameState() {
  try {
    const response = await fetch(`${API_BASE}/get-state`);
    const data = await response.json();
    lines = data.lines || [];
    squares = data.squares || [];
    drawBoard();
    updateScores();
  } catch(err) {
    console.error("Error getting game state:", err);
  }
}

async function handleCanvasClick(e) {
  const rect = canvas.getBoundingClientRect();
  const mouseX = e.clientX - rect.left;
  const mouseY = e.clientY - rect.top;

  const clickedLine = getClickedLine(mouseX, mouseY);
  if (!clickedLine) return;

  const exists = lines.some(line =>
    (line.x1 === clickedLine.x1 && line.y1 === clickedLine.y1 &&
      line.x2 === clickedLine.x2 && line.y2 === clickedLine.y2) ||
    (line.x1 === clickedLine.x2 && line.y1 === clickedLine.y2 &&
      line.x2 === clickedLine.x1 && line.y2 === clickedLine.y1)
  );
  if (exists) {
    alert("The line is already busy!");
    return;
  }

  try {
    const response = await fetch(`${API_BASE}/make-move`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        playerId: "player1",
        lines: lines,
        squares: squares,
        move: clickedLine,
        rows: rows,
        cols: cols
      })
    });

    const data = await response.json();
    if (data.success) {
      lines = data.lines;
      squares = [...squares, ...data.squares.filter(
        sq => !squares.some(existing => existing.x === sq.x && existing.y === sq.y)
      )];
      drawBoard();
      updateScores();
    } else {
      alert("It's not your turn now or the line is busy!");
    }
    if(data.winner) {
      setTimeout(() => {
        alert("Winner: " + data.winner);
      }, 10);
    }
  } catch (err) {
    console.error("Error during move:", err);
  }
}

function getClickedLine(mouseX, mouseY){
  const threshold = 10;
  for(let i=0;i<rows;i++){
    for(let j=0;j<cols-1;j++){
      const x1=offsetX+j*cellSize;
      const y1=offsetY+i*cellSize;
      const x2=offsetX+(j+1)*cellSize;
      const y2=y1;
      if(mouseY>=y1-threshold && mouseY<=y1+threshold &&
        mouseX>=x1 && mouseX<=x2) return {x1:j, y1:i, x2:j+1, y2:i};
    }
  }
  for(let i=0;i<rows-1;i++){
    for(let j=0;j<cols;j++){
      const x1=offsetX+j*cellSize;
      const y1=offsetY+i*cellSize;
      const x2=x1;
      const y2=offsetY+(i+1)*cellSize;
      if(mouseX>=x1-threshold && mouseX<=x1+threshold &&
        mouseY>=y1 && mouseY<=y2) return {x1:j, y1:i, x2:j, y2:i+1};
    }
  }
  return null;
}

function applyBoardSize() {
  rows = parseInt(rowsInput.value);
  cols = parseInt(colsInput.value);

  prevRows = rows;
  prevCols = cols;

  localStorage.setItem("rows", rows);
  localStorage.setItem("cols", cols);

  lines = [];
  squares = [];

  canvas.width = offsetX * 2 + (cols - 1) * cellSize;
  canvas.height = offsetY * 2 + (rows - 1) * cellSize;

  drawBoard();
  updateScores();

  applyBtn.style.display = "none";

  restartGame();
}

async function restartGame() {
  try {
    await fetch(`${API_BASE}/restart`, { method: "POST" });

    lines = [];
    squares = [];

    drawBoard();
    updateScores();
  } catch (err) {
    console.error("Error while restarting:", err);
  }
}

function updateScores() {
  const score1 = squares.filter(sq => sq.playerNumber === 2).length;
  const score2 = squares.filter(sq => sq.playerNumber === 1).length;

  document.getElementById("score1").innerText = score1;
  document.getElementById("score2").innerText = score2;
}

function checkInputChanges() {
  const rowChanged = parseInt(rowsInput.value) !== prevRows;
  const colChanged = parseInt(colsInput.value) !== prevCols;
  applyBtn.style.display = (rowChanged || colChanged) ? "inline-block" : "none";
}

function setupCanvas() {
  canvas.width = offsetX * 2 + (cols - 1) * cellSize;
  canvas.height = offsetY * 2 + (rows - 1) * cellSize;
  drawBoard();
}

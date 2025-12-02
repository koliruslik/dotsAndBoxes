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
const logContainer = document.getElementById("logContainer");
const difficultySelect = document.getElementById("difficultySelect");
const difficultyHint = document.getElementById("difficultyHint");

const PLAYER1_COLOR = getComputedStyle(document.documentElement)
  .getPropertyValue('--accent-blue').trim();
const PLAYER2_COLOR = getComputedStyle(document.documentElement)
  .getPropertyValue('--accent-red').trim();
const DOT_COLOR = getComputedStyle(document.documentElement)
  .getPropertyValue('--dot-color')?.trim() || "#333";

const aiStatus = document.getElementById("aiStatus");
let isGameLocked = false;

rowsInput.value = rows;
colsInput.value = cols;
applyBtn.style.visibility = "hidden";

let lines = [];
let squares = [];

const difficultyInfo = {
  "2": "Super Fast (< 100ms).",
  "3": "Normal (~1-3 sec).",
  "4": "Hard (~5-20 sec)."
};

setupCanvas();
updateInputConstraints();

canvas.width = offsetX * 2 + (cols - 1) * cellSize;
canvas.height = offsetY * 2 + (rows - 1) * cellSize;

applyBtn.addEventListener("click", applyBoardSize);
restartBtn.addEventListener("click", restartGame);

rowsInput.addEventListener("input", handleInputChange);
colsInput.addEventListener("input", handleInputChange);
difficultySelect.addEventListener("change", onDifficultyChange);

canvas.addEventListener("click", handleCanvasClick);

let currentPlayer = 1;

function onDifficultyChange() {
  const depth = difficultySelect.value;
  difficultyHint.textContent = difficultyInfo[depth];
  updateInputConstraints();
}

function updateInputConstraints() {
  const maxVal = 6;
  rowsInput.max = maxVal;
  colsInput.max = maxVal;

  let changed = false;
  if (parseInt(rowsInput.value) > maxVal) {
    rowsInput.value = maxVal;
    changed = true;
  }
  if (parseInt(colsInput.value) > maxVal) {
    colsInput.value = maxVal;
    changed = true;
  }
}

function handleInputChange(e) {
  const max = parseInt(e.target.max);
  if (parseInt(e.target.value) > max) {
    e.target.value = max;
  }
  checkInputChanges();
}

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

async function handleCanvasClick(e) {
  if (isGameLocked) return;

  const clickedLine = getClickedLineFromMouse(e);
  if (!clickedLine || isLineTaken(clickedLine)) return;

  try {
    const playerData = await playerMove(clickedLine);
    currentPlayer = playerData.currentPlayer;

    if (currentPlayer === 2) {
      setLoadingState(true);

      while (currentPlayer === 2) {
        await new Promise(r => setTimeout(r, 50));

        const aiData = await makeAIMove();
        currentPlayer = aiData.currentPlayer;

        if (aiData.winner) break;
        if (aiData.error) {
          console.error(aiData.error);
          break;
        }
      }
    }
  } catch (error) {
    console.error("move error:", error);
    alert("Error connecting to server");
  } finally {
    setLoadingState(false);
  }
}

async function playerMove(move) {
  const response = await fetch(`${API_BASE}/make-move`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      playerId: "player1",
      lines,
      squares,
      move,
      rows,
      cols
    })
  });
  const data = await response.json();
  updateGameState(data);
  return data;
}

async function makeAIMove() {
  const depth = parseInt(difficultySelect.value);

  const response = await fetch(`${API_BASE}/ai-move`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      playerId: "player2",
      lines,
      squares,
      rows,
      cols,
      difficulty: depth
    })
  });
  const data = await response.json();
  updateGameState(data);
  return data;
}

function updateGameState(data) {
  if (!data) return;
  lines = data.lines || lines;
  squares = data.squares || squares;
  currentPlayer = data.currentPlayer;
  drawBoard();
  updateScores();

  if (data.winner) setTimeout(() => alert("Winner: " + data.winner), 10);

  if (data.aiLogs) {
    renderLogs(data.aiLogs);
  }
}

function renderLogs(logs) {
  logContainer.innerHTML = "";
  logs.forEach(log => {
    const div = document.createElement("div");
    div.className = "log-entry";
    if (log.includes("New Best Move") || log.includes("SELECTED")) {
      div.classList.add("log-highlight");
    }
    if (log.includes("Stats:")) {
      div.style.color = "#00bcd4";
      div.style.fontWeight = "bold";
    }
    div.textContent = log;
    logContainer.appendChild(div);
  });
  logContainer.scrollTop = logContainer.scrollHeight;
}

function getClickedLineFromMouse(e) {
  const rect = canvas.getBoundingClientRect();
  const mouseX = e.clientX - rect.left;
  const mouseY = e.clientY - rect.top;
  return getClickedLine(mouseX, mouseY);
}

function isLineTaken(line) {
  const exists = lines.some(l =>
    (l.x1 === line.x1 && l.y1 === line.y1 && l.x2 === line.x2 && l.y2 === line.y2) ||
    (l.x1 === line.x2 && l.y1 === line.y2 && l.x2 === line.x1 && l.y2 === line.y1)
  );
  if (exists) alert("The line is already busy!");
  return exists;
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

async function applyBoardSize() {
  const newRows = parseInt(rowsInput.value);
  const newCols = parseInt(colsInput.value);

  const depth = parseInt(difficultySelect.value);

  rows = newRows;
  cols = newCols;

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

  logContainer.innerHTML = '<div class="log-entry">New game started...</div>';

  applyBtn.style.visibility = "hidden";

  try {
    const response = await fetch(`${API_BASE}/set-size`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ rows: rows, cols: cols })
    });

    const data = await response.json();
    if(data.success){
      lines = data.lines;
      squares = data.squares;
      drawBoard();
      updateScores();
    }
  } catch (err) {
    console.error("Error setting board size:", err);
  }
  await restartGame();
}

async function restartGame() {
  try {
    await fetch(`${API_BASE}/restart`, { method: "POST" });

    lines = [];
    squares = [];

    logContainer.innerHTML = '<div class="log-entry">Game restarted...</div>';

    drawBoard();
    updateScores();
  } catch (err) {
    console.error("Error while restarting:", err);
  } finally {
    setLoadingState(false);
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
  applyBtn.style.visibility = (rowChanged || colChanged) ? "visible" : "hidden";
}

function setupCanvas() {
  canvas.width = offsetX * 2 + (cols - 1) * cellSize;
  canvas.height = offsetY * 2 + (rows - 1) * cellSize;
  drawBoard();
}

function setLoadingState(isLoading) {
  isGameLocked = isLoading;
  aiStatus.style.display = isLoading ? "block" : "none";
  canvas.style.cursor = isLoading ? "wait" : "pointer";
}

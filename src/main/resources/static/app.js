const size = 3;
let board = Array(size * size).fill(' ');
let nextPlayer = 'b';

function renderBoard() {
  const table = document.getElementById('board');
  table.innerHTML = '';
  for (let i = 0; i < size; i++) {
    const row = document.createElement('tr');
    for (let j = 0; j < size; j++) {
      const cell = document.createElement('td');
      const val = board[i * size + j];
      if (val !== ' ') cell.classList.add(val.toLowerCase());
      cell.textContent = val === ' ' ? '' : val;
      cell.onclick = () => playerMove(i, j);
      row.appendChild(cell);
    }
    table.appendChild(row);
  }
}

function playerMove(x, y) {
  const index = x * size + y;
  if (board[index] !== ' ' || document.getElementById('message').textContent) return;

  board[index] = nextPlayer.toUpperCase();
  renderBoard();
  nextPlayer = nextPlayer === 'b' ? 'w' : 'b';
  sendMove();
}

function sendMove() {
  fetch(`/api/squares/nextMove`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ size: size, data: board.join(''), nextPlayerColor: nextPlayer })
  })
  .then(res => res.json())
  .then(data => {
    if (data.x >= 0 && data.y >= 0) {
      board[data.x * size + data.y] = data.color.toUpperCase();
      renderBoard();
    }
    if (data.color.includes('wins') || data.color.includes('Draw')) {
      document.getElementById('message').textContent = data.color;
    }
  })
  .catch(err => console.error(err));
}

document.getElementById('restart').onclick = () => {
  board.fill(' ');
  nextPlayer = 'b';
  renderBoard();
  document.getElementById('message').textContent = '';
};

renderBoard();

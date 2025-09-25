let size, board, nextPlayer, lastMove, winningCells, gameMode, gameActive;

const table = document.getElementById('board');
const messageElem = document.getElementById('message');
const currentPlayerElem = document.getElementById('current-player');
const startButton = document.getElementById('start-game');
const restartButton = document.getElementById('restart');
const boardSizeInput = document.getElementById('board-size');

startButton.onclick = startGame;
restartButton.onclick = startGame;

function startGame() {
    size = parseInt(boardSizeInput.value);
    if (size < 3 || size > 10 || isNaN(size)) {
        messageElem.textContent = 'Пожалуйста, выберите размер доски от 3 до 10';
        return;
    }
    gameMode = document.getElementById('game-mode').value;
    board = Array(size * size).fill('.');
    nextPlayer = 'b';
    lastMove = null;
    winningCells = [];
    gameActive = true;
    messageElem.textContent = '';
    updateCurrentPlayerIndicator();
    renderBoard();
}

function updateCurrentPlayerIndicator() {
    currentPlayerElem.innerHTML = `<div class="piece ${nextPlayer}"></div>`;
}

function renderBoard() {
    table.innerHTML = '';
    for (let i = 0; i < size; i++) {
        const row = document.createElement('tr');
        for (let j = 0; j < size; j++) {
            const cell = document.createElement('td');
            const val = board[i * size + j];

            cell.innerHTML = '';

            if (val !== '.') {
                const circle = document.createElement('div');
                circle.classList.add('piece');
                circle.classList.add(val.toLowerCase());

                // подсветка выигрышных клеток
                if (winningCells.some(c => c[0] === i && c[1] === j)) {
                    circle.classList.add('winning');
                }

                // подсветка последнего хода
                if (lastMove && lastMove[0] === i && lastMove[1] === j) {
                    circle.classList.add('last-move');
                }

                cell.appendChild(circle);
            }

            // кликабельность
            cell.classList.add(gameActive && val === '.' ? 'clickable' : 'disabled');
            cell.onclick = () => playerMove(i, j);

            row.appendChild(cell);
        }
        table.appendChild(row);
    }
}

function playerMove(x, y) {
    if (!gameActive) {
        messageElem.textContent = 'Игра окончена. Нажмите "Перезапустить игру"';
        return;
    }
    if (board[x * size + y] !== '.') {
        messageElem.textContent = 'Эта клетка уже занята!';
        return;
    }

    board[x * size + y] = nextPlayer.toUpperCase();
    lastMove = [x, y];
    renderBoard();

    if (checkWinner(nextPlayer.toUpperCase())) {
        messageElem.textContent = `${nextPlayer.toUpperCase()} побеждает!`;
        gameActive = false;
        return;
    }

    if (isBoardFull()) {
        messageElem.textContent = 'Ничья!';
        gameActive = false;
        return;
    }

    if (gameMode === '2p') {
        togglePlayer();
    } else if (gameMode === 'vs-computer') {
        const humanColor = nextPlayer;
        setTimeout(() => sendMove(humanColor), 300);
    }
}

function togglePlayer() {
    nextPlayer = nextPlayer === 'b' ? 'w' : 'b';
    updateCurrentPlayerIndicator();
}

function checkWinner(color) {
    const cells = [];
    for (let i = 0; i < size; i++)
        for (let j = 0; j < size; j++)
            if (board[i * size + j] === color) cells.push([i, j]);

    for (let i = 0; i < cells.length; i++) {
        for (let j = i + 1; j < cells.length; j++) {
            const [x1, y1] = cells[i];
            const [x2, y2] = cells[j];
            const dx = x2 - x1, dy = y2 - y1;
            const variants = [[-dy, dx], [dy, -dx]];
            for (const [vx, vy] of variants) {
                const x3 = x1 + vx, y3 = y1 + vy;
                const x4 = x2 + vx, y4 = y2 + vy;
                if (x3 >= 0 && x3 < size && y3 >= 0 && y3 < size &&
                    x4 >= 0 && x4 < size && y4 >= 0 && y4 < size) {
                    if (board[x3 * size + y3] === color && board[x4 * size + y4] === color) {
                        winningCells = [[x1, y1], [x2, y2], [x3, y3], [x4, y4]];
                        return true;
                    }
                }
            }
        }
    }
    return false;
}

function isBoardFull() {
    return !board.includes('.');
}

function checkStatus() {
    if (!gameActive) return;

    if (checkWinner(nextPlayer.toUpperCase())) {
        messageElem.textContent = `${nextPlayer.toUpperCase()} побеждает!`;
        gameActive = false;
        renderBoard();
        return;
    }

    if (isBoardFull()) {
        messageElem.textContent = 'Ничья!';
        gameActive = false;
        renderBoard();
        return;
    }

    if (gameMode === '2p') {
        togglePlayer();
    } else if (gameMode === 'vs-computer') {
        const playerBeforeComputer = nextPlayer;
        setTimeout(() => sendMove(playerBeforeComputer), 300);
    }
}

function sendMove(humanColor) {
    if (!gameActive) return;

    document.getElementById('loading').style.display = 'block';
    const computerColor = humanColor === 'b' ? 'w' : 'b';

    fetch('/api/nextMove', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ size, data: board.join(''), nextPlayerColor: computerColor })
    })
    .then(res => res.json())
    .then(data => {
        if (data.x >= 0 && data.y >= 0) {
            board[data.x * size + data.y] = data.color.toUpperCase();
            lastMove = [data.x, data.y];

            renderBoard();

            if (checkWinner(data.color.toUpperCase())) {
                messageElem.textContent = `${data.color.toUpperCase()} побеждает!`;
                gameActive = false;
            } else if (isBoardFull()) {
                messageElem.textContent = 'Ничья!';
                gameActive = false;
            } else {
                nextPlayer = 'b';
                updateCurrentPlayerIndicator();
            }
        } else {
            messageElem.textContent = data.message || 'Ошибка хода компьютера';
            gameActive = false;
        }
    })
    .catch(err => {
        console.error('Ошибка при ходе компьютера:', err);
        messageElem.textContent = 'Ошибка сервера. Попробуйте снова.';
        gameActive = false;
    })
    .finally(() => {
        document.getElementById('loading').style.display = 'none';
    });
}

function updateCurrentPlayerIndicator() {
    currentPlayerElem.innerHTML = `<div class="piece ${nextPlayer}"></div>`;
}

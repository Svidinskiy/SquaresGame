class SquaresGameUI {
    constructor() {
        this.boardSize = 3;
        this.gameMode = 'pvp';
        this.currentPlayer = 'white';
        this.gameActive = false;
        this.gameStarted = false;
        this.boardState = [];
        this.movesHistory = [];
        this.computerPlaying = false;
        this.computerTimeout = null;
        this.winningSquares = null;
        this.lastMovePlayer = null;

        this.initializeElements();
        this.bindEvents();
        this.createBoard();
        this.updateUI();
    }

    initializeElements() {
        this.boardElement = document.getElementById('gameBoard');
        this.gameStatusElement = document.getElementById('gameStatus');
        this.sizeSlider = document.getElementById('boardSize');
        this.sizeValue = document.getElementById('sizeValue');
        this.startGameBtn = document.getElementById('startGameBtn');
        this.resetBtn = document.getElementById('resetBtn');
        this.gameModeRadios = document.querySelectorAll('input[name="gameMode"]');
        this.whitePlayerType = document.getElementById('whitePlayerType');
        this.blackPlayerType = document.getElementById('blackPlayerType');
        this.movesList = document.getElementById('movesList');
        this.gameResult = document.getElementById('gameResult');
        this.resultTitle = document.getElementById('resultTitle');
        this.winningInfo = document.getElementById('winningInfo');
        this.newRoundBtn = document.getElementById('newRoundBtn');
        this.modal = document.getElementById('resultModal');
        this.closeModal = document.getElementById('closeModal');
    }

    bindEvents() {
        this.sizeSlider.addEventListener('input', (e) => {
            this.boardSize = parseInt(e.target.value);
            this.sizeValue.textContent = `${this.boardSize}x${this.boardSize}`;
            this.createBoard();
            this.gameStarted = false;
            this.updateUI();
        });

        this.gameModeRadios.forEach(radio => {
            radio.addEventListener('change', (e) => {
                this.gameMode = e.target.value;
                this.updatePlayerTypes();
                this.gameStarted = false;
                this.updateUI();
            });
        });

        this.startGameBtn.addEventListener('click', () => this.startGame());
        this.resetBtn.addEventListener('click', () => this.resetGame());
        this.newRoundBtn.addEventListener('click', () => this.startNewRound());
        this.closeModal.addEventListener('click', () => this.closeResultModal());
    }

    updateUI() {
        this.startGameBtn.disabled = this.gameStarted;
        this.startGameBtn.textContent = this.gameStarted ? 'Игра начата' : 'Начать игру';

        if (!this.gameStarted) {
            this.gameStatusElement.textContent = 'Настройте игру и нажмите "Начать игру"';
        } else if (!this.gameActive) {
            this.gameStatusElement.textContent = 'Игра завершена';
        }
    }

    startGame() {
        this.gameStarted = true;
        this.startNewRound();
        this.updateUI();
    }

    createBoard() {
        this.boardElement.innerHTML = '';
        this.boardElement.style.gridTemplateColumns = `repeat(${this.boardSize}, 1fr)`;
        this.boardElement.style.gridTemplateRows = `repeat(${this.boardSize}, 1fr)`;

        const maxBoardSize = 400;
        const cellSize = Math.min(maxBoardSize / this.boardSize, 80);
        const boardSizePx = cellSize * this.boardSize;

        this.boardElement.style.width = `${boardSizePx}px`;
        this.boardElement.style.height = `${boardSizePx}px`;

        this.boardState = Array(this.boardSize).fill().map(() =>
            Array(this.boardSize).fill(null)
        );

        for (let y = 0; y < this.boardSize; y++) {
            for (let x = 0; x < this.boardSize; x++) {
                const cell = document.createElement('div');
                cell.className = 'cell';
                cell.dataset.x = x;
                cell.dataset.y = y;
                cell.addEventListener('click', () => this.handleCellClick(x, y));
                this.boardElement.appendChild(cell);
            }
        }
    }

    startNewRound() {
        if (!this.gameStarted) {
            this.startGame();
            return;
        }

        this.stopComputerPlay();
        this.gameActive = true;
        this.movesHistory = [];
        this.winningSquares = null;
        this.lastMovePlayer = null;
        this.hideGameResult();
        this.updateMovesList();

        this.currentPlayer = 'white';
        this.createBoard();
        this.updateGameStatus();
        this.updatePlayerTypes();
        this.clearWinningHighlight();

        if (this.gameMode === 'cvc') {
            this.startComputerGame();
        }
    }

    resetGame() {
        this.stopComputerPlay();
        this.gameActive = false;
        this.gameStarted = false;
        this.movesHistory = [];
        this.winningSquares = null;
        this.lastMovePlayer = null;
        this.createBoard();
        this.updateGameStatus();
        this.updateMovesList();
        this.hideGameResult();
        this.clearWinningHighlight();
        this.updateUI();
    }

    handleCellClick(x, y) {
        if (!this.gameActive || !this.gameStarted || this.boardState[y][x] !== null) return;
        const currentPlayerType = this.getCurrentPlayerType();
        if (currentPlayerType === 'Компьютер') return;
        this.makeMove(x, y);
    }

    async makeMove(x, y) {
        if (!this.gameActive) return;
        if (this.boardState[y][x] !== null) return;

        this.lastMovePlayer = this.currentPlayer;
        this.boardState[y][x] = this.currentPlayer;

        this.movesHistory.push({
            player: this.currentPlayer,
            position: { x, y },
            moveNumber: this.movesHistory.length + 1
        });

        this.updateBoard();
        this.updateMovesList();

        const gameStatus = await this.checkGameStatus(this.lastMovePlayer);

        if (gameStatus === 'win') {
            this.handleWin();
            return;
        } else if (gameStatus === 'draw') {
            this.handleDraw();
            return;
        }

        this.switchPlayer();
        this.updateGameStatus();

        if (this.shouldComputerMove()) {
            await this.makeComputerMove();
        }
    }

    switchPlayer() {
        this.currentPlayer = this.currentPlayer === 'white' ? 'black' : 'white';
    }

    shouldComputerMove() {
        if (!this.gameActive) return false;
        const currentPlayerType = this.getCurrentPlayerType();
        return (this.gameMode === 'pvc' || this.gameMode === 'cvc') && currentPlayerType === 'Компьютер';
    }

    async checkGameStatus(playerToCheck) {
        const boardData = this.getBoardData();
        const playerColor = playerToCheck === 'white' ? 'w' : 'b';

        try {
            const response = await fetch('/api/nextMove', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    size: this.boardSize,
                    data: boardData,
                    nextPlayerColor: playerColor
                })
            });

            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
            const result = await response.json();

            if (result.message) {
                if (result.message.includes('wins')) {
                    this.winningSquares = result.winningSquare || null;
                    return 'win';
                } else if (result.message.includes('Draw')) {
                    return 'draw';
                } else if (result.message.includes('finished')) {
                    return 'win';
                }
            }
            return 'active';
        } catch (error) {
            return this.isBoardFull() ? 'draw' : 'active';
        }
    }

    async makeComputerMove() {
        if (!this.gameActive) return;

        const boardData = this.getBoardData();
        const nextPlayerColor = this.currentPlayer === 'white' ? 'w' : 'b';

        try {
            const response = await fetch('/api/nextMove', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    size: this.boardSize,
                    data: boardData,
                    nextPlayerColor: nextPlayerColor
                })
            });

            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
            const result = await response.json();

            if (result.x >= 0 && result.y >= 0) {
                if (this.boardState[result.y][result.x] !== null) {
                    this.makeRandomMove();
                    return;
                }

                await new Promise(resolve => setTimeout(resolve, 500));
                await this.makeMove(result.x, result.y);
            } else {
                if (result.message) {
                    if (result.message.includes('wins')) {
                        this.winningSquares = result.winningSquare || null;
                        this.handleWin();
                    } else if (result.message.includes('Draw')) {
                        this.handleDraw();
                    }
                }
            }
        } catch (error) {
            console.error('Ошибка при запросе к бэкенду:', error);
            this.makeRandomMove();
        }
    }

    makeRandomMove() {
        const emptyCells = [];
        for (let y = 0; y < this.boardSize; y++) {
            for (let x = 0; x < this.boardSize; x++) {
                if (this.boardState[y][x] === null) emptyCells.push({ x, y });
            }
        }

        if (emptyCells.length > 0) {
            const randomCell = emptyCells[Math.floor(Math.random() * emptyCells.length)];
            this.makeMove(randomCell.x, randomCell.y);
        } else {
            this.handleDraw();
        }
    }

    getBoardData() {
        let data = '';
        for (let y = 0; y < this.boardSize; y++) {
            for (let x = 0; x < this.boardSize; x++) {
                const cell = this.boardState[y][x];
                data += cell === 'white' ? 'W' : cell === 'black' ? 'B' : '.';
            }
        }
        return data;
    }

    isBoardFull() {
        return this.boardState.every(row => row.every(cell => cell !== null));
    }

    handleWin() {
        if (!this.gameActive) return;
        this.gameActive = false;
        this.stopComputerPlay();
        this.disableBoard();

        const winner = this.lastMovePlayer === 'white' ? 'Белые' : 'Чёрные';
        this.showGameResult(`Победа ${winner}!`, this.winningSquares);
        this.highlightWinningSquares();
    }

    handleDraw() {
        if (!this.gameActive) return;
        this.gameActive = false;
        this.stopComputerPlay();
        this.disableBoard();
        this.showResultModal();
    }

    showGameResult(title, winningSquares = null) {
        this.resultTitle.textContent = title;
        if (winningSquares) {
            this.winningInfo.innerHTML = `
                <div class="winning-info">
                    <strong>Выигрышный квадрат:</strong><br>
                    ${winningSquares.map(coord => `(${coord[0]}, ${coord[1]})`).join(' → ')}
                </div>
            `;
        } else {
            this.winningInfo.innerHTML = '';
        }
        this.gameResult.style.display = 'block';
    }

    hideGameResult() {
        this.gameResult.style.display = 'none';
    }

    showResultModal() {
        this.modal.style.display = 'flex';
    }

    closeResultModal() {
        this.modal.style.display = 'none';
        this.startNewRound();
    }

    disableBoard() {
        document.querySelectorAll('.cell').forEach(cell => cell.classList.add('disabled'));
    }

    highlightWinningSquares() {
        if (!this.winningSquares) return;
        this.winningSquares.forEach(coord => {
            const [x, y] = coord;
            const cell = this.boardElement.querySelector(`[data-x="${x}"][data-y="${y}"]`);
            if (cell) cell.classList.add('winning');
        });
    }

    clearWinningHighlight() {
        document.querySelectorAll('.cell.winning').forEach(cell => cell.classList.remove('winning'));
    }

    updateBoard() {
        document.querySelectorAll('.cell').forEach(cell => {
            const x = parseInt(cell.dataset.x);
            const y = parseInt(cell.dataset.y);
            const value = this.boardState[y][x];
            const wasWinning = cell.classList.contains('winning');

            cell.className = 'cell';
            if (value) cell.classList.add(value);

            if (wasWinning && this.winningSquares) {
                const isStillWinning = this.winningSquares.some(coord => coord[0] === x && coord[1] === y);
                if (isStillWinning) cell.classList.add('winning');
            }
        });

        if (this.winningSquares) this.highlightWinningSquares();
    }

    updateGameStatus() {
        if (!this.gameStarted) {
            this.gameStatusElement.textContent = 'Настройте игру и нажмите "Начать игру"';
            return;
        }

        if (!this.gameActive) {
            this.gameStatusElement.textContent = 'Игра завершена';
            return;
        }

        const playerName = this.currentPlayer === 'white' ? 'Белые' : 'Чёрные';
        const playerType = this.getCurrentPlayerType();
        this.gameStatusElement.textContent = `Ходят ${playerName} (${playerType})`;

        document.querySelectorAll('.player-info').forEach(info => info.classList.remove('active'));
        const activePlayerInfo = this.currentPlayer === 'white' ?
            document.querySelector('.player-info.white') : document.querySelector('.player-info.black');
        if (activePlayerInfo) activePlayerInfo.classList.add('active');
    }

    updatePlayerTypes() {
        switch (this.gameMode) {
            case 'pvp':
                this.whitePlayerType.textContent = 'Игрок';
                this.blackPlayerType.textContent = 'Игрок';
                break;
            case 'pvc':
                this.whitePlayerType.textContent = this.currentPlayer === 'white' ? 'Игрок' : 'Компьютер';
                this.blackPlayerType.textContent = this.currentPlayer === 'black' ? 'Игрок' : 'Компьютер';
                break;
            case 'cvc':
                this.whitePlayerType.textContent = 'Компьютер';
                this.blackPlayerType.textContent = 'Компьютер';
                break;
        }
    }

    getCurrentPlayerType() {
        if (this.gameMode === 'pvp') return 'Игрок';
        if (this.gameMode === 'pvc') {
            return this.currentPlayer === 'white' ? 'Игрок' : 'Компьютер';
        }
        return 'Компьютер';
    }

    updateMovesList() {
        this.movesList.innerHTML = '';
        this.movesHistory.forEach(move => {
            const moveElement = document.createElement('div');
            moveElement.className = 'move-item';
            moveElement.textContent = `Ход ${move.moveNumber}: ${move.player === 'white' ? 'Белые' : 'Чёрные'} (${move.position.x}, ${move.position.y})`;
            this.movesList.appendChild(moveElement);
        });
        this.movesList.scrollTop = this.movesList.scrollHeight;
    }

    stopComputerPlay() {
        this.computerPlaying = false;
        if (this.computerTimeout) {
            clearTimeout(this.computerTimeout);
            this.computerTimeout = null;
        }
    }

    startComputerGame() {
        if (this.gameMode === 'cvc' && this.gameStarted) {
            this.computerPlaying = true;
            this.makeComputerMove();
        }
    }
}

document.addEventListener('DOMContentLoaded', () => {
    new SquaresGameUI();
});
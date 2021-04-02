package com.jpipeline.javafxclient;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Slider;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

public class MainMenuController {

    private Main main;

    private final int LINE_WIDTH = 1;
    private final int FPS = 60;
    private final int INTERVAL = 1;

    private int horizontalCount;
    private int verticalCount;
    private int nodeWidth;
    private int animationDelay;
    private Node[][] nodes;
    private NodeType draggingType = null;
    private Node source;
    private Node destination;
    private GraphicsContext gc;
    private boolean activeSearch = false;
    private boolean isRefreshed = true;
    private Thread searchThread;

    @FXML
    public Canvas mainCanvas;
    @FXML
    public Button startButton;
    @FXML
    public Slider delaySlider;
    @FXML
    public Slider resolutionSlider;
    @FXML
    public ComboBox algoCombo;
    @FXML
    public ComboBox heuristicCombo;

    public void init() throws IOException {
        animationDelay = (int)delaySlider.getValue();
        horizontalCount = (int) resolutionSlider.getValue();

        // вычисляем ширину клеток
        nodeWidth = (int) (mainCanvas.getWidth() - INTERVAL * horizontalCount * 2) / horizontalCount;
        // вычиялем количество клеток по вертикали
        verticalCount = (int) mainCanvas.getHeight() / (nodeWidth + INTERVAL * 2);

        // получаем контекст холста
        gc = mainCanvas.getGraphicsContext2D();
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(LINE_WIDTH);

        // передаем в Node необходимые параметры
        Node.setGc(gc);
        Node.setInterval(INTERVAL);
        Node.setNodeWidth(nodeWidth);
        Node.setLineWidth(LINE_WIDTH);


        // устанавливаем начальные значения ComboBox'ов
        algoCombo.getSelectionModel().selectFirst();
        heuristicCombo.getSelectionModel().selectFirst();

        // инициализируем события
        initEvents();

        // инициализируем массив узлов
        initNodesArray();

        // запускаем поток перерисовки холста
        startUpdateCanvasThread();
    }

    public void startSearch() {
        // если выполняется поиск - прерываем его
        if (activeSearch) {
            searchThread.interrupt();
            activeSearch = false;
            refreshCanvas();
            return;
        }
        // если холст не был очищен после предыдущего поиска - очищаем
        if (!isRefreshed) refreshCanvas();
        isRefreshed = false;

        Callable<Void> callable = null;

        // в зависимости от выбранного в ComboBox'е алгоритма инициализируем объект Callable
        switch (algoCombo.getSelectionModel().getSelectedIndex()) {
            case 0:
                callable = () -> { breadthFirstSearch(); return null; };
                break;
            case 1:
                callable = () -> { a(); return null; };
                break;
            case 2:
                callable = () -> { dijkstra(); return null; };
                break;
            case 3:
                callable = () -> { bellmanFord(); return null; };
                break;
        }
        if (callable != null) {
            Callable<Void> finalCallable = callable;
            // инициализируем поток
            searchThread = new Thread(() -> {
                try {
                    finalCallable.call();
                } catch (Exception e) {
                    enableGUI();
                    activeSearch = false;
                }
            });
            activeSearch = true;
            // отключаем элементы управления
            disableGUI();
            // запускаем поток
            searchThread.start();
        }
    }

    private void bellmanFord() throws InterruptedException {
        var m = 0;
        class Edge {
            public Node a, b;
            public double cost;
            Edge(Node a, Node b, double cost)
            {
                this.a = a;
                this.b = b;
                this.cost = cost;
            }
        }
        
        List<Edge> edges = new ArrayList<>();
        Node[][] p = new Node[verticalCount][horizontalCount];
        double[][] d = new double[verticalCount][horizontalCount];


        for (Node[] row : p) Arrays.fill(row, null);
        for (double[] row : d) Arrays.fill(row, Double.MAX_VALUE);
        
        for (int i = 0; i<verticalCount; i++)
        {
            for (int j = 0; j<horizontalCount; j++)
            {
                var currentNode = nodes[i][j];

                if (currentNode.isBarrier())
                {
                    continue;
                }
                for (var ii=-1; ii<2; ii++)
                {
                    for (var jj=-1; jj<2; jj++)
                    {
                        if (currentNode.y+ii >= 0 && currentNode.x+jj >= 0
                                && currentNode.y+ii < nodes.length && currentNode.x+jj < nodes[0].length
                                && !(ii==0 && jj==0)
                                && !(nodes[currentNode.y+ii][currentNode.x+jj].isBarrier()))
                        {
                            if ((ii==-1 && jj==-1) || (ii==-1 && jj==1) || (ii==1 && jj==-1) || (ii==1 && jj==1))
                            {
                                edges.add(new Edge(currentNode, nodes[currentNode.y+ii][currentNode.x+jj], 1.1));
                            }
                            else
                            {
                                edges.add(new Edge(currentNode, nodes[currentNode.y+ii][currentNode.x+jj], 1));
                            }
                        }
                    }
                }
            }
        }
        Node currentNode = source;
        d[currentNode.y][currentNode.x] = 0;
        for (var nadeus = 0; nadeus<nodes.length*nodes[0].length; nadeus++)
        {
            var changes = false;
            for (var i = 0; i<edges.size(); i++)
            {
                var a = edges.get(i).a;
                if (d[a.y][a.x]<Double.MAX_VALUE)
                {
                    Node b = edges.get(i).b;

                    if (!b.isMarked() && !b.isSource() && !b.isDestination())
                    {
                        b.setType(NodeType.MARKED);
                        Thread.sleep(animationDelay);
                        m++;
                    }

                    if (d[b.y][b.x]>d[a.y][a.x]+edges.get(i).cost)
                    {
                        d[b.y][b.x] = d[a.y][a.x]+edges.get(i).cost;
                        p[b.y][b.x] = nodes[a.y][a.x];
                        changes = true;
                    }
                }
            }
            if (!changes)
            {
                break;
            }
        }
        traceBack(p, m);
    }
    
    

    private void dijkstra() throws InterruptedException {
        int m = 0;

        boolean[][] used = new boolean[verticalCount][horizontalCount];
        Node[][] p = new Node[verticalCount][horizontalCount];
        double[][] d = new double[verticalCount][horizontalCount];
        
        for (boolean[] row : used) Arrays.fill(row, false);
        for (Node[] row : p) Arrays.fill(row, null);
        for (double[] row : d) Arrays.fill(row, Double.MAX_VALUE);

        Node currentNode = source;
        d[currentNode.y][currentNode.x] = 0;

        for (int i = 0; i<verticalCount; i++)
        {
            for (int j = 0; j<horizontalCount; j++)
            {
                currentNode = null;
                for (int k = 0; k<verticalCount; k++)
                {
                    for (int t = 0; t<horizontalCount; t++)
                    {
                        if (!used[k][t] && (currentNode == null || d[k][t] < d[currentNode.y][currentNode.x]))
                        {
                            currentNode = nodes[k][t];
                            d[currentNode.y][currentNode.x] = d[k][t];
                        }
                    }
                }
                if (d[currentNode.y][currentNode.x] == Double.MAX_VALUE)
                {
                    break;
                }
                m++;
                currentNode.setType(NodeType.INSPECTED);
                Thread.sleep(animationDelay);
                used[currentNode.y][currentNode.x] = true;
                for (int ii=-1; ii<2; ii++)
                {
                    for (int jj=-1; jj<2; jj++)
                    {
                        if (currentNode.y+ii >= 0 && currentNode.x+jj >= 0
                                && currentNode.y+ii < nodes.length && currentNode.x+jj < nodes[0].length
                                && !(ii==0 && jj==0)
                                && !(nodes[currentNode.y+ii][currentNode.x+jj].isBarrier()))
                        {
                            Node next = nodes[currentNode.y+ii][currentNode.x+jj];
                            double distance;
                            if ((ii==-1 && jj==-1) || (ii==-1 && jj==1) || (ii==1 && jj==-1) || (ii==1 && jj==1))
                            {
                                distance = 1.1;
                            }
                            else
                            {
                                distance = 1;
                            }
                            if (d[currentNode.y][currentNode.x] + distance < d[next.y][next.x])
                            {
                                d[next.y][next.x] = d[currentNode.y][currentNode.x] + distance;
                                p[next.y][next.x] = currentNode;
                                m++;
                                next.setType(NodeType.MARKED);
                                Thread.sleep(animationDelay);
                            }
                        }
                    }
                }
            }
        }
        traceBack(p, m);
    }

    private double heuristic(Node v) {
        int h =heuristicCombo.getSelectionModel().getSelectedIndex();
        if (h == 0) {
            return (double) (Math.abs(v.x - destination.x) + Math.abs(v.y - destination.y));
        }
        else if (h == 1) {
            return Math.sqrt(Math.pow(Math.abs(v.x - destination.x), 2) + Math.pow(Math.abs(v.y - destination.y), 2));
        }
        else if (h == 2) {
            return (double) Math.max(Math.abs(v.x - destination.x), Math.abs(v.y - destination.y));
        }
        return 0;
    }
    private Node getMin(ArrayList<Node> pool, double[][] f, Node[][] nodes) {
        double min = Integer.MAX_VALUE;
        Node minNode = null;
        int index = -1;
        Node currentNode = null;

        for (int i = 0; i < pool.size(); i++) {
            currentNode = pool.get(i);
            if (f[currentNode.y][currentNode.x] < min) {
                min = f[currentNode.y][currentNode.x];
                minNode = currentNode;
                index = i;
            }
        }
        pool.remove(index);
        return minNode;
    }
    
    void a() throws InterruptedException {
        int m = 0;

        boolean[][] used = new boolean[verticalCount][horizontalCount];
        Node[][] p = new Node[verticalCount][horizontalCount];
        double[][] d = new double[verticalCount][horizontalCount];
        double[][] f = new double[verticalCount][horizontalCount];
        
        for (boolean[] row : used) Arrays.fill(row, false);
        for (Node[] row : p) Arrays.fill(row, null);
        for (double[] row : d) Arrays.fill(row, Double.MAX_VALUE);
        for (double[] row : f) Arrays.fill(row, Double.MAX_VALUE);
        

        Node currentNode = source;

        ArrayList<Node> pool = new ArrayList<>();
        pool.add(currentNode);

        used[currentNode.y][currentNode.x] = true;
        d[currentNode.y][currentNode.x] = 0;
        f[currentNode.y][currentNode.x] = d[currentNode.y][currentNode.x] + heuristic(currentNode);


        while (!pool.isEmpty()) {
            currentNode = getMin(pool, f, nodes);

            if (currentNode == destination) {
                traceBack(p, m);
                return;
            }
            m++;
            currentNode.setType(NodeType.INSPECTED);
            Thread.sleep(animationDelay);
            for (int i = -1; i < 2; i++) {
                for (int j = -1; j < 2; j++) {
                    if (currentNode.y + i >= 0 && currentNode.x + j >= 0 && currentNode.y + i < nodes.length && currentNode.x + j < nodes[0].length) {
                        Node next = nodes[currentNode.y + i][currentNode.x + j];

                        if (!(next.isBarrier()) && !(i == 0 && j == 0) && !(next.isSource())) {
                            double tentativeScore;
                            if ((i == -1 && j == -1) || (i == -1 && j == 1) || (i == 1 && j == -1) || (i == 1 && j == 1)) {
                                tentativeScore = d[currentNode.y][currentNode.x] + 1.1;
                            }
                            else {
                                tentativeScore = d[currentNode.y][currentNode.x] + 1;
                            }

                            if (used[next.y][next.x] && tentativeScore >= d[next.y][next.x]) {
                                continue;
                            }
                            else {
                                p[next.y][next.x] = currentNode;
                                d[next.y][next.x] = tentativeScore;
                                f[next.y][next.x] = d[next.y][next.x] + heuristic(next);



                                if (!pool.contains(next))
                                {
                                    pool.add(next);
                                    used[next.y][next.x] = true;
                                    m++;
                                    next.setType(NodeType.MARKED);            
                                    Thread.sleep(animationDelay);

                                }
                            }
                        }
                    }
                }
            }
        }

        traceBack(p, m);
    }
    
    void breadthFirstSearch() throws InterruptedException {
        int m = 0;

        boolean[][] used = new boolean[verticalCount][horizontalCount];
        Node[][] p = new Node[verticalCount][horizontalCount];
        double[][] d = new double[verticalCount][horizontalCount];
        
        for (boolean[] row : used) Arrays.fill(row, false);
        for (Node[] row : p) Arrays.fill(row, null);
        for (double[] row : d) Arrays.fill(row, Double.MAX_VALUE);

        Node currentNode = source;

        ArrayDeque<Node> queue = new ArrayDeque<>();
        queue.offer(currentNode);

        used[currentNode.y][currentNode.x] = true;
        d[currentNode.y][currentNode.x] = 0;

        while (!queue.isEmpty()) {
            currentNode = queue.poll();
            m++;
            currentNode.setType(NodeType.INSPECTED);
            Thread.sleep(animationDelay);
            for (int i = -1; i < 2; i++) {
                for (int j = -1; j < 2; j++) {
                    if (currentNode.y + i >= 0 && currentNode.x + j >= 0 && currentNode.y + i < nodes.length && currentNode.x + j < nodes[0].length) {
                        Node next = nodes[currentNode.y + i][currentNode.x + j];
                        if (!(next.isBarrier()) && !(i == 0 && j == 0) && !(next.isSource())) {
                            Node parent = p[next.y][next.x];
                            if (p[next.y][next.x] == null || d[currentNode.y][currentNode.x] < d[parent.y][parent.x]) {
                                if ((i == -1 && j == -1) || (i == -1 && j == 1) || (i == 1 && j == -1) || (i == 1 && j == 1))
                                {
                                    d[next.y][next.x] = d[currentNode.y][currentNode.x] + 1.1;
                                }
                                else {
                                    d[next.y][next.x] = d[currentNode.y][currentNode.x] + 1;
                                }
                                p[next.y][next.x] = currentNode;
                                queue.offer(next);
                                m++;
                                next.setType(NodeType.MARKED);
                                Thread.sleep(animationDelay);
                            }
                        }
                    }
                }
            }
        }
        traceBack(p, m);
    }

    void traceBack(Node[][] parents, int operationsCount) throws InterruptedException {
        int d = 0;
        Node destination = this.destination;

        if (!(parents[destination.y][destination.x] == null)) {
            Node currentNode = destination;
            while (!(parents[currentNode.y][currentNode.x] == null)) {
                d++;
                currentNode = parents[currentNode.y][currentNode.x];
                currentNode.setType(NodeType.TRACED);
                Thread.sleep(animationDelay);
            }
            Platform.runLater(() -> new Alert(Alert.AlertType.INFORMATION, "The path have been found! Number of operations: " + operationsCount).showAndWait() );

        }
        else {
            Platform.runLater(() -> new Alert(Alert.AlertType.ERROR, "The end point is unreachable from the initial!").showAndWait() );
        }
        activeSearch = false;
        // включаем элементы управления
        enableGUI();
    }

    private void refreshCanvas() {
        for (int y = 0; y < verticalCount; y++) {
            for (int x = 0; x < horizontalCount; x++) {
                Node node = nodes[y][x];
                // если узел - не старт, не финиш и не препятствие - делаем его свободным
                if (!node.isSource() && !node.isDestination() && !node.isBarrier()) {
                    node.setType(NodeType.DEFAULT);
                }
            }
        }
    }

    private void resetCanvas(int hCount) {
        horizontalCount = hCount;
        // вычиялем ширину клеток
        nodeWidth = (int) (mainCanvas.getWidth() - INTERVAL * horizontalCount * 2) / horizontalCount;
        // вычиялем количество клеток по вертикали
        verticalCount = (int) mainCanvas.getHeight() / (nodeWidth + INTERVAL * 2);
        // предаем в Node ширину клеток
        Node.setNodeWidth(nodeWidth);
        // переинициализируем массив узлов
        initNodesArray();
        // чистим холст
        gc.clearRect(0, 0, mainCanvas.getWidth(), mainCanvas.getHeight());
    }
    private void updateCanvas() {
        // обновляем холст
        for (int j = 0; j < verticalCount; j++) {
            for (int i = 0; i < horizontalCount; i++) {
                nodes[j][i].draw();
            }
        }
    }

    private void initNodesArray() {
        // инициализируем массив узлов и заполняем
        nodes = new Node[verticalCount][horizontalCount];
        for (int j = 0; j < verticalCount; j++) {
            for (int i = 0; i < horizontalCount; i++) {
                nodes[j][i] = new Node(i, j);
            }
        }
        // устанавливаем начальную и конечную точки
        source = nodes[0][0];
        source.setType(NodeType.SOURCE);
        destination = nodes[verticalCount - 1][horizontalCount - 1];
        destination.setType(NodeType.DESTINATION);
    }

    private void startUpdateCanvasThread() {
        Task task = new Task<Void>() {
            @Override
            public Void call() throws InterruptedException {
                while (true) {
                    Platform.runLater(() -> {
                        updateCanvas();
                    });
                    Thread.sleep(1000 / FPS);
                }
            }
        };
        Thread thread = new Thread(task);
        thread.start();
    }

    private void initEvents() {
        // устанавливаем события на изменение значения слайдеров
        delaySlider.valueProperty().addListener((observableValue, oldValue, newValue) -> animationDelay = newValue.intValue());
        resolutionSlider.valueProperty().addListener((observableValue, oldValue, newValue) -> resetCanvas(newValue.intValue()));

        // комбобокс с эвристиками включен только тогда, когда выбрал алгоритм А*
        algoCombo.valueProperty().addListener((observableValue, oldValue, newValue) -> {
            if (algoCombo.getSelectionModel().getSelectedIndex() == 1) heuristicCombo.setDisable(false);
            else heuristicCombo.setDisable(true);
        });

        // событие на нажатие по холсту
        mainCanvas.setOnMousePressed(event -> {
            // если поиск активен - ничего не делаем
            if (activeSearch) return;
            // если холст не очищен после предыдущего поиска - очищаем
            if (!isRefreshed) refreshCanvas();
            // получаем координаты узла в массиве
            int x = (int) event.getX() / (nodeWidth + 2 * INTERVAL);
            int y = (int) event.getY() / (nodeWidth + 2 * INTERVAL);
            if (x >= horizontalCount || y >= verticalCount || x < 0 || y < 0) return;
            Node node = nodes[y][x];
            switch (node.getType()) {
                case BARRIER: // если узел - препятствие, делаем его свободным и запускаем процесс удаления препятствий
                    node.setType(NodeType.DEFAULT);
                    draggingType = NodeType.DEFAULT;
                    break;
                case SOURCE: // если узел - начальный, запускаем процесс перетаскивания
                    draggingType = NodeType.SOURCE;
                    break;
                case DESTINATION: // если узел - конечный, запускаем процесс перетаскивания
                    draggingType = NodeType.DESTINATION;
                    break;
                default: // если узел - свободный, делаем его препятствием и запускаем процесс установки препятствий
                    node.setType(NodeType.BARRIER);
                    draggingType = NodeType.BARRIER;
                    break;
            }
        });
        // событие на ведение мышью по холсту
        mainCanvas.setOnMouseDragged(event -> {
            // по факту - проверка, нажата ли мышь
            if (draggingType == null) return;
            int x = (int) event.getX() / (nodeWidth + 2 * INTERVAL);
            int y = (int) event.getY() / (nodeWidth + 2 * INTERVAL);
            if (x >= horizontalCount || y >= verticalCount || x < 0 || y < 0) return;
            Node node = nodes[y][x];
            switch (draggingType) {
                case BARRIER: // рисование препятствиями
                    node.setType(NodeType.BARRIER); break;
                case DEFAULT: // удаление препятствий
                    node.setType(NodeType.DEFAULT); break;
                case SOURCE: // перенос начальной точки
                    source.forceType(NodeType.DEFAULT);
                    node.setType(NodeType.SOURCE);
                    source = node;
                    break;
                case DESTINATION: // перенос конечной точки
                    destination.forceType(NodeType.DEFAULT);
                    node.setType(NodeType.DESTINATION);
                    destination = node;
                    break;
            }
        });
        // на отпускание кнопки мыши - завершаем процесс работы с холстом
        mainCanvas.setOnMouseReleased(event -> {
            draggingType = null;
        });
    }

    void disableGUI() {
        Platform.runLater(() -> {
            startButton.setText("Stop");
            algoCombo.setDisable(true);
            heuristicCombo.setDisable(true);
            resolutionSlider.setDisable(true);
        });
    }

    void enableGUI() {
        Platform.runLater(() -> {
            startButton.setText("Start");
            algoCombo.setDisable(false);
            if (algoCombo.getSelectionModel().getSelectedIndex() == 1)
                heuristicCombo.setDisable(false);
            resolutionSlider.setDisable(false);
        });
    }

    public void setMain(Main main) {
        this.main = main;
    }
}

package lu.uni.binfo.main;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.effect.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;

public class CrazyBall extends Application {

    private Effect ballEffect = null;
    private Effect lineEffect = null;

    private Point2D p_0;
    private Point2D p_1;
    private Point2D p_2;
    private Point2D p_3;

    private double bX;
    private double bY;

    private Line horizontal;
    private Line vertical;

    private ArrayList<Line> alLines = new ArrayList<>();

    private final int radius = 18;
    private Circle[] balls = new Circle[radius];
    private Circle C_temp = new Circle(6, Color.RED);
    private Color[] colorArray = {Color.RED, Color.AQUA, Color.DARKORANGE, Color.GOLD,
            Color.DARKKHAKI, Color.CADETBLUE, Color.MAGENTA, Color.MEDIUMSEAGREEN, Color.SLATEGRAY, Color.PURPLE};

    private double tempo = 0.009;

    private boolean isTrailOn = false;
    private boolean drawBezier = false;
    private boolean keepOldBezier = false;
    private boolean hasBeenCleared = false;

    private void increaseTempo(double increase) {
        if(tempo + increase <= 1 && tempo + increase > 0)
            tempo += increase;
//        System.out.println(tempo);
    }

    private Point2D movePointFromBorder(Point2D p, double width, double height, double radius) {
        double x = p.getX();
        double y = p.getY();

        if(x == 0)
            x += radius;
        else if (x == width)
            x -= radius;

        if(y == 0)
            y += radius;
        else if (y == height)
            y -= radius;

        return new Point2D(x,y);
    }

    private Point2D getNextPoint(Point2D p) {
        if(p == p_0)
            return p_1;
        else if(p == p_1)
            return p_2;
        else if(p == p_2)
            return p_3;
        else if(p == p_3)
            return p_0;
        else return null; // -> should not happen hopefully
    }

    private int getRandomNumber(int min, int max) {
        return (int) (Math.random()*(max-min) + min);
    }

    private void addMainComponents(Pane canvas) {
        canvas.getChildren().add(vertical);
        canvas.getChildren().add(horizontal);
        canvas.getChildren().add(C_temp);
        if(!isTrailOn)
            canvas.getChildren().add(balls[0]);
    }

    private void drawFeatures(Pane canvas, Line line, Paint newColor) {
        if(isTrailOn) {
            if(!drawBezier && canvas.getChildren().contains(balls[balls.length-1]))
                canvas.getChildren().remove(balls[balls.length-1]);
            else if(drawBezier) {
                if(!hasBeenCleared && balls[1] != null && balls[2] != null) {
                    double x1 = balls[1].getLayoutX();
                    double y1 = balls[1].getLayoutY();
                    double x2 = balls[2].getLayoutX();
                    double y2 = balls[2].getLayoutY();
                    line = new Line(x1, y1, x2, y2);
                    line.setEffect(lineEffect);
                    line.setStrokeWidth(2);
                    line.setStroke(newColor);
                    canvas.getChildren().add(line);
                    line.toBack();
                    canvas.getChildren().remove(balls[balls.length-1]);
                    alLines.add(line);
                }
            }
            hasBeenCleared = false;
            balls[0] = new Circle(radius, newColor);
            balls[0].setEffect(ballEffect);
            balls[0].setLayoutX(bX);
            balls[0].setLayoutY(bY);
            for (int i = balls.length-1; i > 0; i--) {
                balls[i] = balls[i-1];
                if(balls[i] != null)
                    balls[i].setRadius(balls[i-1].getRadius()-1);
            }
            canvas.getChildren().add(balls[0]);
        }
    }

    private void deleteOldLines(Pane canvas) {
        if(isTrailOn && !keepOldBezier) {
            for (int i = 0; i < alLines.size(); i++) {
                if(canvas.getChildren().contains(alLines.get(i))) {
                    canvas.getChildren().remove(alLines.get(i));
                }
            }
            alLines = new ArrayList<>();
        }
    }

    private void resetPanel(Pane canvas) {
        hasBeenCleared = true;
        alLines.clear();
        canvas.getChildren().clear();
        addMainComponents(canvas);
    }

    @Override
    public void start(Stage stage) {

        Pane canvas = new Pane();
        Scene scene = new Scene(canvas, 1300, 800);
        scene.setFill(Color.GREEN);

        vertical = new Line(canvas.getWidth()/2, 0, canvas.getWidth()/2, canvas.getHeight());
        horizontal = new Line(0, canvas.getHeight()/2, canvas.getWidth(), canvas.getHeight()/2);

        p_0 = new Point2D(canvas.getWidth()/2, 0);
        p_1 = new Point2D(canvas.getWidth(), canvas.getHeight()/2);
        p_2 = new Point2D(canvas.getWidth()/2, canvas.getHeight());
        p_3 = new Point2D(0, canvas.getHeight()/2);

        final Point2D pS = p_0;

        balls[0] = new Circle(radius, colorArray[getRandomNumber(0, colorArray.length)]);
        balls[0].setEffect(ballEffect);

        addMainComponents(canvas);

        stage.setTitle("Crazy Ball");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(30), new EventHandler<>() {

            double t = 0;

            Point2D pStart = movePointFromBorder(pS, canvas.getWidth(), canvas.getHeight(), radius);
            Point2D pN = getNextPoint(pS);
            Point2D pNext = movePointFromBorder(pN, canvas.getWidth(), canvas.getHeight(), radius);

            Paint newPaint = balls[0].getFill();

            @Override
            public void handle(ActionEvent e) {

                if(t == 0) {
                    C_temp.setLayoutX(canvas.getWidth() / 2 - C_temp.getRadius() + getRandomNumber(-200, 200));
                    C_temp.setLayoutY(canvas.getHeight() / 2 - C_temp.getRadius() + getRandomNumber(-200, 200));
                }

                bX = Math.pow(1-t, 2)*pStart.getX() + 2*(1-t)*t* C_temp.getLayoutX() + Math.pow(t, 2)*pNext.getX();
                bY = Math.pow(1-t, 2)*pStart.getY() + 2*(1-t)*t* C_temp.getLayoutY() + Math.pow(t, 2)*pNext.getY();
                balls[0].setLayoutX(bX);
                balls[0].setLayoutY(bY);

                t += tempo;

                Line line = null;
                drawFeatures(canvas, line, newPaint);

                if(t >= 1) {
                    deleteOldLines(canvas);
                    t = 0;
                    pStart = pNext;
                    pN = getNextPoint(pN);
                    pNext = movePointFromBorder(pN, canvas.getWidth(), canvas.getHeight(), radius);
                    do {
                        newPaint = colorArray[getRandomNumber(0, colorArray.length)];
                    } while(balls[0].getFill().equals(newPaint));
                    if(!isTrailOn)
                        balls[0].setFill(newPaint);
                }
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        scene.addEventHandler(KeyEvent.KEY_PRESSED, (key) -> {
            if(key.getCode() == KeyCode.T) {
                isTrailOn = !isTrailOn;
                resetPanel(canvas);
            }
            else if(key.getCode() == KeyCode.D)
                drawBezier = !drawBezier;
            else if(key.getCode() == KeyCode.K)
                keepOldBezier = !keepOldBezier;
            else if(key.getCode() == KeyCode.C)
                resetPanel(canvas);
            else if(key.getCode() == KeyCode.ADD)
                increaseTempo(0.005);
            else if(key.getCode() == KeyCode.SUBTRACT)
                increaseTempo(-0.005);
            else if(key.getCode() == KeyCode.P)
                if(timeline.getStatus() == Animation.Status.PAUSED)
                    timeline.play();
                else
                    timeline.pause();
            else {
                if(key.getCode() == KeyCode.DIGIT0)
                    ballEffect = null;
                if(key.getCode() == KeyCode.DIGIT1)
                    ballEffect = new Lighting();
                if(key.getCode() == KeyCode.DIGIT2)
                    ballEffect = new Bloom();
                if(key.getCode() == KeyCode.DIGIT3)
                    ballEffect = new GaussianBlur();
                if(key.getCode() == KeyCode.DIGIT4)
                    lineEffect = null;
                if(key.getCode() == KeyCode.DIGIT5)
                    lineEffect = new Lighting();
                if(key.getCode() == KeyCode.DIGIT6)
                    lineEffect = new Bloom();
                if(key.getCode() == KeyCode.DIGIT7)
                    lineEffect = new GaussianBlur();
                balls[0].setEffect(ballEffect);
            }
        });
    }

    public static void main(String[] args) {
        launch();
    }
}
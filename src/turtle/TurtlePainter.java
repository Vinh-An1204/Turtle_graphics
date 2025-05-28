package turtle;


public interface TurtlePainter {


    void forward(final int points);


    void back(final int points);


    void right(final int degrees);


    void left(final int degrees);


    void set(final int x, final int y);


    void penUp();


    void penDown();
    void cls();


    /**
     * Resets the turtle's angel to 0 degrees, which effectively is the turtle facing EAST
     */
    void resetAngle();


    void finish();
}

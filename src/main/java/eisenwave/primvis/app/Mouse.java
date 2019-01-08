package eisenwave.primvis.app;

public class Mouse {
    
    private double x = 0, y = 0;
    private boolean leftDown = false, rightDown = false;
    
    public double getX() {
        return x;
    }
    
    public double getY() {
        return y;
    }
    
    public boolean isLeftDown() {
        return leftDown;
    }
    
    public boolean isRightDown() {
        return rightDown;
    }
    
    public void setX(double x) {
        this.x = x;
    }
    
    public void setY(double y) {
        this.y = y;
    }
    
    public void setPos(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
    public void setLeftDown(boolean leftDown) {
        this.leftDown = leftDown;
    }
    
    public void setRightDown(boolean rightDown) {
        this.rightDown = rightDown;
    }
    
}

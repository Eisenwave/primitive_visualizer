package eisenwave.primvis.app;

public class Window {
    
    private final long id;
    private int width, height;
    
    public Window(long id, int width, int height) {
        this.id = id;
        this.width = width;
        this.height = height;
    }
    
    public long getId() {
        return id;
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public float getAspectRatio() {
        return (float) width / height;
    }
    
    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }
    
}

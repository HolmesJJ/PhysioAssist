package sg.com.nyp.a164936j.physioAssist.models;

public class DynamicFloatArray {
    private float[] stackedBar;

    //constructor
    public DynamicFloatArray() {
        this.stackedBar = new float[1];
    }

    public float getStackedBar(int exercises) {
        if(exercises >= stackedBar.length){
            return 0;
        }else{
            return stackedBar[exercises];
        }
    }

    public void put(int newSize, float value){
        if(newSize >= stackedBar.length){
            //increase size
            int updateSize = 2 * newSize;
            float[] newStackedBar = new float[updateSize];
            System.arraycopy(stackedBar, 0,newStackedBar,0,stackedBar.length);
            stackedBar = newStackedBar;
        }

        stackedBar[newSize] = value;

    }
}

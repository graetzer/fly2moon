package de.trivago.missionmoon.compass;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class CompassSensor {

SensorManager sm;
int lastDirection = -1;
int lastPitch;
int lastRoll;
boolean firstReading = true;


static CompassSensor mInstance;

public static CompassSensor getInstance(Context ctx){
    if(mInstance == null){
        mInstance = new CompassSensor(ctx);
    }
    return mInstance;
}

private CompassSensor(Context ctx){
    sm = (SensorManager) ctx.getSystemService(Context.SENSOR_SERVICE);
    onResume();
}

public void onResume(){
    sm.registerListener(sensorListener, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    sm.registerListener(sensorListener, sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_UI);
    firstReading = true;
}

public void onPause(){
    sm.unregisterListener(sensorListener);
}

private final SensorEventListener sensorListener = new SensorEventListener(){
    float accelerometerValues[] = null;
    float geomagneticMatrix[] = null;
    @Override
	public void onSensorChanged(SensorEvent event) {
        if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE)
            return;

        switch (event.sensor.getType()) {
        case Sensor.TYPE_ACCELEROMETER:
            accelerometerValues = event.values.clone();

            break;
        case Sensor.TYPE_MAGNETIC_FIELD:
            geomagneticMatrix = event.values.clone();
            break;
        }   

        if (geomagneticMatrix != null && accelerometerValues != null && event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {

            float[] R = new float[16];
            float[] I = new float[16];
            //float[] outR = new float[16];

            //Get the rotation matrix, then remap it from camera surface to world coordinates
            SensorManager.getRotationMatrix(R, I, accelerometerValues, geomagneticMatrix);
            //SensorManager.remapCoordinateSystem(R, SensorManager.AXIS_X, SensorManager.AXIS_Z, outR);
            float values[] = new float[4];
            //SensorManager.getOrientation(outR,values);
            SensorManager.getOrientation(R,values);

            int direction = normalizeDegrees(filterChange((int)Math.toDegrees(values[0])));
            int pitch = normalizeDegrees(Math.toDegrees(values[1]));
            int roll = normalizeDegrees(Math.toDegrees(values[2]));
            if(direction != lastDirection){
                lastDirection = direction;
                lastPitch = pitch;
                lastRoll = roll;
            }
        }
    }

    @Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
};


//Normalize a degree from 0 to 360 instead of -180 to 180
private int normalizeDegrees(double rads){
    return (int)((rads+360)%360);
}

//We want to ignore large bumps in individual readings.  So we're going to cap the number of degrees we can change per report
private static final int MAX_CHANGE = 3;
private int filterChange(int newDir){
    newDir = normalizeDegrees(newDir);
    //On the first reading, assume it's right.  Otherwise NW readings take forever to ramp up
    if(firstReading){
        firstReading = false;
        return newDir;
    }       

    //Figure out how many degrees to move
    int delta = newDir - lastDirection;
    int normalizedDelta = normalizeDegrees(delta);
    int change = Math.min(Math.abs(delta),MAX_CHANGE);

    //We always want to move in the direction of lower distance.  So if newDir is lower and delta is less than half a circle, lower lastDir
    // Same if newDir is higher but the delta is more than half a circle (you'd be faster in the other direction going lower).
    if( normalizedDelta > 180 ){
        change = -change;
    }

    return lastDirection+change;
}

public int getLastDirection(){
    return lastDirection;
}
public int getLastPitch(){
    return lastPitch;
}
public int getLastRoll(){
    return lastRoll;
}



}
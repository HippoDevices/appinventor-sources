// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt


package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ErrorMessages;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Physical world component that can detect shaking and measure
 * acceleration in three dimensions.  It is implemented using
 * android.hardware.SensorListener
 * (http://developer.android.com/reference/android/hardware/SensorListener.html).
 *
 * <p>From the Android documentation:
 * "Sensor values are acceleration in the X, Y and Z axis, where the X axis
 * has positive direction toward the right side of the device, the Y axis has
 * positive direction toward the top of the device and the Z axis has
 * positive direction toward the front of the device. The direction of the
 * force of gravity is indicated by acceleration values in the X, Y and Z
 * axes. The typical case where the device is flat relative to the surface of
 * the Earth appears as -STANDARD_GRAVITY in the Z axis and X and Y values
 * close to zero. Acceleration values are given in SI units (m/s^2)."
 *
 */
// TODO(user): ideas - event for knocking
@DesignerComponent(version = YaVersion.ACCELEROMETERSENSOR_COMPONENT_VERSION,
    description = "Non-visible component that can detect shaking and " +
    "measure acceleration approximately in three dimensions using SI units " +
    "(m/s<sup>2</sup>).  The components are: <ul>\n" +
    "<li> <strong>xAccel</strong>: 0 when the phone is at rest on a flat " +
    "     surface, positive when the phone is tilted to the right (i.e., " +
    "     its left side is raised), and negative when the phone is tilted " +
    "     to the left (i.e., its right size is raised).</li>\n " +
    "<li> <strong>yAccel</strong>: 0 when the phone is at rest on a flat " +
    "     surface, positive when its bottom is raised, and negative when " +
    "     its top is raised. </li>\n " +
    "<li> <strong>zAccel</strong>: Equal to -9.8 (earth's gravity in meters per " +
    "     second per second when the device is at rest parallel to the ground " +
    "     with the display facing up, " +
    "     0 when perpindicular to the ground, and +9.8 when facing down.  " +
    "     The value can also be affected by accelerating it with or against " +
    "     gravity. </li></ul>",
    category = ComponentCategory.HIPPOADK,
    nonVisible = true,
    iconName = "images/hippoadk.png")
@SimpleObject
public class Motor extends AndroidNonvisibleComponent implements BluetoothConnectionListener  {
  private static final int TOY_ROBOT = 0x0804; // from android.bluetooth.BluetoothClass.Device.
  
  protected UsbAccessory usbaccessory;
  protected BluetoothClient bluetooth;
  private String TAG = "AnalogWrite";
  private String MotorPort = "";
  private int Flag = 0;
  
  /**
   * Creates a new AccelerometerSensor component.
   *
   * @param container  ignored (because this is a non-visible component)
   */
  public Motor(ComponentContainer container){
    super(container.$form());
    //form.registerForOnResume(this);
    //form.registerForOnStop(this);
  }
  
  /**

   */
  @SimpleProperty(category = PropertyCategory.BEHAVIOR,description = "The Motor Port used")
  public String MotorPort() {
    return MotorPort;
  }
  
  /**
   * Specifies the motor ports that are used for driving.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "")
  @SimpleProperty
  public void MotorPort(String motorport) {
	  MotorPort = motorport;
  }
  
	@SimpleFunction(description = "MotorDrive")
	public void MotorDrive(int value) {
		byte[] USBCommandPacket = new byte[5];
		USBCommandPacket[0] = (byte) 0xF4;
		if(MotorPort.equals("A")){
			USBCommandPacket[1] = (byte) 0x10;
		}
		if(MotorPort.equals("B")){
			USBCommandPacket[1] = (byte) 0x20;
		}
		if(MotorPort.equals("C")){
			USBCommandPacket[1] = (byte) 0x30;
		}
		if(value < 0){
			USBCommandPacket[1] |= 0x01;
			USBCommandPacket[2]	= (byte) ((-1 * value) & 0X7F);
			USBCommandPacket[3]	= (byte) ((-1 *value) >> 7);
		}else{
			USBCommandPacket[1] &= 0xFE;
			USBCommandPacket[2]	= (byte) (value & 0X7F);
			USBCommandPacket[3]	= (byte) (value >> 7);
		}
	    if (usbaccessory != null) {
		usbaccessory.SendCommand(USBCommandPacket);
	    }
	    if (bluetooth != null) {
	    	bluetooth.write("ss",USBCommandPacket);
	    }
		Log.d(TAG,"USBCommandPacket[0] = " + (int)USBCommandPacket[0]);
		Log.d(TAG,"USBCommandPacket[1] = " + (int)USBCommandPacket[1]);
		Log.d(TAG,"USBCommandPacket[2] = " + (int)USBCommandPacket[2]);
	}

  /**
   * Specifies the BluetoothClient component that should be used for communication.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_UsbAccessory,
      defaultValue = "")
  @SimpleProperty(userVisible = false)
  public void UsbAccessory(UsbAccessory usbaccessory1) {
    if (usbaccessory1 != null) {
    	usbaccessory = usbaccessory1;
    	//usbaccessory.attachComponent(this, Collections.singleton(TOY_ROBOT));
    }
  }
  
  /**
   * Specifies the BluetoothClient component that should be used for communication.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_BLUETOOTHCLIENT,
      defaultValue = "")
  @SimpleProperty(userVisible = false)
  public void BluetoothClient(BluetoothClient bluetoothClient) {
  //  if (bluetooth != null) {
  //    bluetooth.removeBluetoothConnectionListener(this);
  //    bluetooth.detachComponent(this);
  //    bluetooth = null;
   // }

    if (bluetoothClient != null) {
      bluetooth = bluetoothClient;
      //bluetooth.attachComponent(this, Collections.singleton(TOY_ROBOT));
      bluetooth.addBluetoothConnectionListener(this);
      if (bluetooth.IsConnected()) {
         //We missed the real afterConnect event.
        afterConnect(bluetooth);
      }
    }
  }

@Override
public void afterConnect(BluetoothConnectionBase bluetoothConnection) {
	// TODO Auto-generated method stub
	
}

@Override
public void beforeDisconnect(BluetoothConnectionBase bluetoothConnection) {
	// TODO Auto-generated method stub
	
}
  
}

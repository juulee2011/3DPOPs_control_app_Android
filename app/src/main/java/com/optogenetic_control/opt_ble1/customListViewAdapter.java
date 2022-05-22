package com.optogenetic_control.wpt_ble1;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

public class customListViewAdapter extends ArrayAdapter {
    private final Activity context;
    private final ArrayList<String> deviceNames;
    private final ArrayList<String> deviceAddresses;
    final ArrayList<String> deviceStatus;
    final ArrayList<String> deviceBattery;
    private ArrayList<Character> states1;
    private ArrayList<Character> states2;
    private ArrayList<Boolean> buttonA;
    private ArrayList<Boolean> buttonB;
    private ArrayList<Boolean> buttonC;
    private ArrayList<Boolean> buttonD;
    private ArrayList<Boolean> buttonE;
    private ArrayList<Boolean> buttonF;
    private ArrayList<Boolean> buttonG;
    private ArrayList<Boolean> buttonH;
    private ArrayList<Boolean> buttonQ;
    private ArrayList<Boolean> buttonR;
    private ArrayList<Boolean> buttonS;

    public customListViewAdapter(Activity context,
                                 ArrayList<String> deviceNames,
                                 ArrayList<String> deviceAddresses,
                                 ArrayList<String> deviceStatus,
                                 ArrayList<String> deviceBattery) {
        super(context,R.layout.listview_devicecontrol , deviceNames);

        this.context=context;
        this.deviceNames = deviceNames;
        this.deviceAddresses = deviceAddresses;
        this.deviceStatus = deviceStatus;
        this.deviceBattery = deviceBattery;
        this.states1 = new ArrayList<Character>(Collections.nCopies(7, 'x'));
        this.states2 = new ArrayList<Character>(Collections.nCopies(7, 'x'));
        this.buttonA = new ArrayList<Boolean>(Collections.nCopies(7, false));
        this.buttonB = new ArrayList<Boolean>(Collections.nCopies(7, false));
        this.buttonC = new ArrayList<Boolean>(Collections.nCopies(7, false));
        this.buttonD = new ArrayList<Boolean>(Collections.nCopies(7, false));
        this.buttonE = new ArrayList<Boolean>(Collections.nCopies(7, false));
        this.buttonF = new ArrayList<Boolean>(Collections.nCopies(7, false));
        this.buttonG = new ArrayList<Boolean>(Collections.nCopies(7, false));
        this.buttonH = new ArrayList<Boolean>(Collections.nCopies(7, false));
        this.buttonQ = new ArrayList<Boolean>(Collections.nCopies(7, false));
        this.buttonR = new ArrayList<Boolean>(Collections.nCopies(7, false));
        this.buttonS = new ArrayList<Boolean>(Collections.nCopies(7, false));
    };
    public void changeState(String address, int state) {
        Log.d("Debug", "device status changed");
        Log.d("Change State", address);
        Log.d("Change State Check", deviceAddresses.get(0));
        if (state == 1)
            deviceStatus.set(deviceAddresses.indexOf(address),"Connected");
        else if (state == 2)
            deviceStatus.set(deviceAddresses.indexOf(address),"DisconnectedR");
        else
            deviceStatus.set(deviceAddresses.indexOf(address),"Disconnected");
        notifyDataSetChanged();
    }

    public void disconnectBattery(String address) {
        deviceBattery.set(deviceAddresses.indexOf(address), "XX");
        notifyDataSetChanged();
    }

    public void changeBattery(String address, String level) {
        deviceBattery.set(deviceAddresses.indexOf(address),level.replaceFirst("^0+","") + "%");
        notifyDataSetChanged();
    }

    public ArrayList<Character> getStates1(){return states1;}
    public ArrayList<Character> getStates2(){return states2;}

    public View getView(final int position, View view, ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.listview_devicecontrol, null,true);

        //this code gets references to objects in the listview_row.xml file
        TextView nameTextField = (TextView) rowView.findViewById(R.id.list_deviceName);
        TextView addressTextField = (TextView) rowView.findViewById(R.id.list_deviceAddress);
        TextView statusTextField = (TextView) rowView.findViewById(R.id.list_deviceStatus);

        //this code sets the values of the objects to values from the arrays
        nameTextField.setText(deviceNames.get(position));
        addressTextField.setText(deviceAddresses.get(position));
        statusTextField.setText(deviceStatus.get(position));

        final Button clickButtona = (Button) rowView.findViewById(R.id.button_a);
        clickButtona.setActivated(buttonA.get(position));
        clickButtona.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonA.set(position,!buttonA.get(position));
                if(buttonA.get(position)){states1.set(position,'a');}
                else{states1.set(position,'x');}
                notifyDataSetChanged();
            }
        });

        final Button clickButtonb = (Button) rowView.findViewById(R.id.button_b);
        clickButtonb.setActivated(buttonB.get(position));
        clickButtonb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonB.set(position,!buttonB.get(position));
                if(buttonB.get(position)){states1.set(position,'b');}
                else{states1.set(position,'x');}
                notifyDataSetChanged();
            }
        });

        final Button clickButtonc = (Button) rowView.findViewById(R.id.button_c);
        clickButtonc.setActivated(buttonC.get(position));
        clickButtonc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonC.set(position,!buttonC.get(position));
                if(buttonC.get(position)){states1.set(position,'c');}
                else{states1.set(position,'x');}
                notifyDataSetChanged();
            }
        });

        final Button clickButtond = (Button) rowView.findViewById(R.id.button_d);
        clickButtond.setActivated(buttonD.get(position));
        clickButtond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonD.set(position,!buttonD.get(position));
                if(buttonD.get(position)){states1.set(position,'d');}
                else{states1.set(position,'x');}
                notifyDataSetChanged();
            }
        });

        final Button clickButtone = (Button) rowView.findViewById(R.id.button_e);
        clickButtone.setActivated(buttonE.get(position));
        clickButtone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonE.set(position,!buttonE.get(position));
                if(buttonE.get(position)){states2.set(position,'e');}
                else{states2.set(position,'x');}
                notifyDataSetChanged();
            }
        });

        final Button clickButtonf = (Button) rowView.findViewById(R.id.button_f);
        clickButtonf.setActivated(buttonF.get(position));
        clickButtonf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonF.set(position,!buttonF.get(position));
                if(buttonF.get(position)){states2.set(position,'f');}
                else{states2.set(position,'x');}
                notifyDataSetChanged();
            }
        });

        final Button clickButtong = (Button) rowView.findViewById(R.id.button_g);
        clickButtong.setActivated(buttonG.get(position));
        clickButtong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonG.set(position,!buttonG.get(position));
                if(buttonG.get(position)){states2.set(position,'g');}
                else{states2.set(position,'x');}
                notifyDataSetChanged();
            }
        });

        final Button clickButtonh = (Button) rowView.findViewById(R.id.button_h);
        clickButtonh.setActivated(buttonH.get(position));
        clickButtonh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonH.set(position,!buttonH.get(position));
                if(buttonH.get(position)){states2.set(position,'h');}
                else{states2.set(position,'x');}
                notifyDataSetChanged();
            }
        });

        final Button clickButtonq = (Button) rowView.findViewById(R.id.button_q);
        clickButtonq.setActivated(buttonQ.get(position));
        clickButtonq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonQ.set(position,!buttonQ.get(position));
                if(buttonQ.get(position)){states1.set(position,'q');}
                else{states1.set(position,'x');}
                notifyDataSetChanged();
            }
        });

        final Button clickButtonr = (Button) rowView.findViewById(R.id.button_r);
        clickButtonr.setActivated(buttonR.get(position));
        clickButtonr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonR.set(position,!buttonR.get(position));
                if(buttonR.get(position)){states1.set(position,'r');}
                else{states1.set(position,'x');}
                notifyDataSetChanged();
            }
        });

        final Button clickButtons = (Button) rowView.findViewById(R.id.button_s);
        clickButtons.setActivated(buttonS.get(position));
        clickButtons.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonS.set(position,!buttonS.get(position));
                if(buttonS.get(position)){states1.set(position,'s');}
                else{states1.set(position,'x');}
                notifyDataSetChanged();
            }
        });

        return rowView;
    };
    void clearSelection() {
        this.buttonA = new ArrayList<Boolean>(Collections.nCopies(7, false));
        this.buttonB = new ArrayList<Boolean>(Collections.nCopies(7, false));
        this.buttonC = new ArrayList<Boolean>(Collections.nCopies(7, false));
        this.buttonD = new ArrayList<Boolean>(Collections.nCopies(7, false));
        this.buttonE = new ArrayList<Boolean>(Collections.nCopies(7, false));
        this.buttonF = new ArrayList<Boolean>(Collections.nCopies(7, false));
        this.buttonG = new ArrayList<Boolean>(Collections.nCopies(7, false));
        this.buttonH = new ArrayList<Boolean>(Collections.nCopies(7, false));
        this.buttonQ = new ArrayList<Boolean>(Collections.nCopies(7, false));
        this.buttonR = new ArrayList<Boolean>(Collections.nCopies(7, false));
        this.buttonS = new ArrayList<Boolean>(Collections.nCopies(7, false));
        this.states1 = new ArrayList<Character>(Collections.nCopies(7, 'x'));
        this.states2 = new ArrayList<Character>(Collections.nCopies(7, 'x'));
        notifyDataSetChanged();
    }
}

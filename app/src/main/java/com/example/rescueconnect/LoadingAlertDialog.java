package com.example.rescueconnect;

import android.app.Activity;
import android.view.LayoutInflater;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class LoadingAlertDialog {
    private Activity activity;
    private AlertDialog alertDialog;
    private String message;

    LoadingAlertDialog(Activity act,String msg){
        activity=act;
        message = msg;
    }

    void startLoading(){
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.loadingdialog,null));
//        ((TextView)activity.findViewById(R.id.loadingtextView)).setText(message);
        builder.setCancelable(false);
        alertDialog = builder.create();
        alertDialog.show();
    }

    void stopLoading(){
        alertDialog.dismiss();
    }
}

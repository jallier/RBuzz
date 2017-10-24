package com.jallier.rbuzz;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.util.Log;

/**
 * Created by Justin on 24/10/2017.
 */

public class AcceptContactRequestDialogFragment extends DialogFragment {
    private final static String TAG = "AcceptContactFragment";
    public PositiveContactRequestListener pListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            pListener = (PositiveContactRequestListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(e + context.toString() + " must implement ContactRequestListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        String sender = args.getString("sender");
        sender = sender + " " + getString(R.string.dialogAcceptContactRequestMessage);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.dialogAcceptContactRequestTitle).setMessage(sender).setPositiveButton(R.string.dialogYes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                pListener.onDialogPositiveClick(AcceptContactRequestDialogFragment.this);
            }
        }).setNegativeButton(R.string.dialogNo, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Nothing needs to be done here
            }
        });
        return builder.create();
    }

    interface PositiveContactRequestListener {
        void onDialogPositiveClick(DialogFragment dialog);
    }
}

package com.example.cyliner_tracker;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.speedata.libuhf.IUHFService;
import com.speedata.libuhf.UHFManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class Register extends AppCompatActivity {
    IUHFService iuhfService;
    String epc, result;
    Button Reading, Submit;
    TextView resultValue;
    EditText cylinderID;
    Dialog dialog;
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Reading = findViewById(R.id.ReadingBtn);
        resultValue = findViewById(R.id.ResultValue);
        Submit = findViewById(R.id.RegisterBtn);
        cylinderID = findViewById(R.id.ManuFacture_Name);
        dialog = new Dialog(Register.this);
        progressDialog = new ProgressDialog(Register.this);


        iuhfService = UHFManager.getUHFService(this);
        iuhfService.setOnInventoryListener(var1 -> {
            epc = var1.getEpc();
        });

        Reading.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iuhfService.openDev();
                result = iuhfService.read_area(1, "2", "6", "00000000");
//                Toast.makeText(Register.this, result, Toast.LENGTH_SHORT).show();
//                result="43434343";


                resultValue.setVisibility(View.VISIBLE);
                resultValue.setText(result);
            }
        });
        Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    String id = cylinderID.getText().toString();
                    if (id.length() != 0) {
                        progressDialog.setMessage("Tag Registering...");
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                        SubmitData(result, id);
                    } else {
                        cylinderID.setError("Enter Cylinder");
                        Toast.makeText(Register.this, "Some Details Require...", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }

    private void SubmitData(String result, String id) throws JSONException {

        String url = "http://164.52.223.163:4500/api/submittrackinginfo/AddTagID";
        JSONObject obj = new JSONObject();

        obj.put(result, id);
//        obj.put("Cylinder",id);

        RequestQueue queue = Volley.newRequestQueue(this);
        final String requestBody = obj.toString();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, response -> {
            progressDialog.dismiss();
            Toast.makeText(Register.this, "Register" + response, Toast.LENGTH_SHORT).show();
            Log.i("VOLLEY", response);
            showDialog();
            Clear();
        }, error -> {
            progressDialog.dismiss();
            alertDialog();
            System.out.println("Error Message " + error.getMessage());
            Clear();
//            dialog.dismiss();
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return requestBody == null ? null : requestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", requestBody, "utf-8");
                    return null;
                }
            }
        };

        queue.add(stringRequest);
    }

    public void Clear() {
        resultValue.setText("");
        resultValue.setVisibility(View.GONE);
        cylinderID.setText("");

    }

    public void showDialog() {
//        TextView text = (TextView) dialog.findViewById(R.id.text_dialog);
//        text.setText("Register Cylinder");
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.customdailog);
        dialog.show();

    }

    private void alertDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("Already Tag Register ");
        dialog.setTitle("Register Alert");
        dialog.setPositiveButton("YES",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                                        int which) {
                        Toast.makeText(getApplicationContext(), "Yes", Toast.LENGTH_LONG).show();
                    }
                });
        dialog.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplicationContext(), "cancel", Toast.LENGTH_LONG).show();
            }
        });
        AlertDialog alertDialog = dialog.create();
        alertDialog.show();
    }
}
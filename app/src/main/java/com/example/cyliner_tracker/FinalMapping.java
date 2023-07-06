package com.example.cyliner_tracker;


import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.speedata.libuhf.IUHFService;
import com.speedata.libuhf.UHFManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class FinalMapping extends AppCompatActivity {
    Button takePicture, Submit;
    Button Reading;
    private static final int Require_Image = 101;
    ImageView imageView;
    IUHFService iuhfService;
    TextView resultValue;
    String epc, resultV;
    Bitmap bitmap;
    byte[] decodedString;
    EditText manufactureNAME, CylinderID;
    Dialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_final_mapping);
        takePicture = findViewById(R.id.TakePicture);
        imageView = findViewById(R.id.imageView2);
        Submit = findViewById(R.id.Submit_Button);
        manufactureNAME = findViewById(R.id.ManuFacture_Name);
        CylinderID = findViewById(R.id.IdCylinder);
        Reading = findViewById(R.id.Reading_btn);
        resultValue = findViewById(R.id.ScanResult);
        dialog = new Dialog(FinalMapping.this);


        iuhfService = UHFManager.getUHFService(this);
        iuhfService.setOnInventoryListener(var1 -> epc = var1.getEpc());
        takePicture.setOnClickListener(v -> TakePictue());
        Reading.setOnClickListener(v -> {
            iuhfService.openDev();
            resultV = iuhfService.read_area(1, "2", "6", "00000000");
//            Toast.makeText(FinalMapping.this, resultV, Toast.LENGTH_SHORT).show();
            resultValue.setVisibility(View.VISIBLE);
            resultValue.setText(resultV);

        });

        Submit.setOnClickListener(v -> {
            String name = manufactureNAME.getText().toString();
            try {
                if (name.length() != 0) {
                    SubmitData(name, resultV);
                } else {
                    manufactureNAME.setError("Please Enter Manufacture Name");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
//
        });
    }


    public void TakePictue() {
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (i.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(i, Require_Image);
        }


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Require_Image && resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            bitmap = (Bitmap) bundle.get("data");
            String path = data.getDataString();
            imageView.setImageBitmap(bitmap);
            decodedString = Base64.decode(String.valueOf(bitmap), Base64.DEFAULT);
            System.out.println("Image String " + Arrays.toString(decodedString));
        }
    }

    public String getEncoded64ImageStringFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        byte[] byteFormat = stream.toByteArray();

        // Get the Base64 string
        String imgString = Base64.encodeToString(byteFormat, Base64.NO_WRAP);
        return imgString;
    }

    private void SubmitData(String name, String resultV) throws JSONException {

        String myImage = getEncoded64ImageStringFromBitmap(bitmap);
        String url = "http://164.52.223.163:4500/api/submittrackinginfo/UpdateCylinderInfoRaw";
        JSONObject obj = new JSONObject();

        obj.put("TagID", resultV);
        obj.put("ManufacturerName", name);
        obj.put("Image", myImage);

        RequestQueue queue = Volley.newRequestQueue(this);
        final String requestBody = obj.toString();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, response -> {
            Toast.makeText(FinalMapping.this, "Success" + response, Toast.LENGTH_SHORT).show();
            Log.i("VOLLEY", response);
            Clear();
            showDialog();
//            dialog.dismiss();
        }, error -> {
            Log.e("VOLLEY Negative", error.toString());
            Toast.makeText(FinalMapping.this, "Failed...", Toast.LENGTH_SHORT).show();

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

    public void showDialog() {
//        TextView text = (TextView) dialog.findViewById(R.id.text_dialog);
//        text.setText("Mapped Cylinder");
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.customdailog);
        dialog.show();

    }

    public void Clear() {
        manufactureNAME.setText("");
        imageView.setImageResource(R.drawable.ic_baseline_search_24);
        CylinderID.setText("");
        resultValue.setText("");
        resultValue.setVisibility(View.GONE);
    }
}
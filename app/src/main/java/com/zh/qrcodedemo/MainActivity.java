package com.zh.qrcodedemo;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.androidnetworking.interfaces.StringRequestListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.EnumMap;
import java.util.Map;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private EditText name, price, quantity;
    TextView manufacturingDate, expireDate;
    ProgressDialog dialog;
    CheckBox qrCheckBox, barcodeCheckBox;
    String imagePath = "";
    boolean isExpireDate = false;

    private IntentIntegrator qrScan;
    IntentResult result;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AndroidNetworking.initialize(this);

        name = findViewById(R.id.productName);
        price = findViewById(R.id.productPrice);
        quantity = findViewById(R.id.productQuantity);
        manufacturingDate = findViewById(R.id.productManufacturingDate);
        expireDate = findViewById(R.id.productExpireDate);
        qrCheckBox = findViewById(R.id.qrCheckBox);
        barcodeCheckBox = findViewById(R.id.barCodeCheckBox);
        qrScan = new IntentIntegrator(this);




        qrCheckBox.setChecked(true);

        qrCheckBox.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked){
                            barcodeCheckBox.setChecked(false);
                        }
                        else {
                            barcodeCheckBox.setChecked(true);
                        }
                    }
                }
        );

        barcodeCheckBox.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked){
                            qrCheckBox.setChecked(false);
                        }
                        else {
                            qrCheckBox.setChecked(true);
                        }
                    }
                }
        );

        manufacturingDate.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isExpireDate = false;
                        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());

                        DatePickerDialog dialog = new DatePickerDialog(MainActivity.this,MainActivity.this,
                                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH));
                        dialog.show();
                    }
                }
        );
        expireDate.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isExpireDate = true;
                        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());

                        DatePickerDialog dialog = new DatePickerDialog(MainActivity.this,MainActivity.this,
                                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                                calendar.get(Calendar.DAY_OF_MONTH));
                        dialog.show();
                    }
                }
        );

        dialog = new ProgressDialog(this);
        dialog.setMessage("Adding...");

    }


    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;

    Bitmap encodeAsBitmap(String contents, BarcodeFormat format, int img_width, int img_height) throws WriterException {
        String contentsToEncode = contents;
        if (contentsToEncode == null) {
            return null;
        }
        Map<EncodeHintType, Object> hints = null;
        String encoding = guessAppropriateEncoding(contentsToEncode);
        if (encoding != null) {
            hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, encoding);
        }
        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix result;
        try {
            result = writer.encode(contentsToEncode, format, img_width, img_height, hints);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    private static String guessAppropriateEncoding(CharSequence contents) {
        // Very crude at the moment
        for (int i = 0; i < contents.length(); i++) {
            if (contents.charAt(i) > 0xFF) {
                return "UTF-8";
            }
        }
        return null;
    }


    Bitmap TextToImageEncode(String Value) throws WriterException {
        BitMatrix bitMatrix;
        try {
            int QRcodeWidth = 500;
            bitMatrix = new MultiFormatWriter().encode(
                    Value,
                    BarcodeFormat.DATA_MATRIX.QR_CODE,
                    QRcodeWidth, QRcodeWidth, null
            );

        } catch (IllegalArgumentException Illegalargumentexception) {

            return null;
        }
        int bitMatrixWidth = bitMatrix.getWidth();

        int bitMatrixHeight = bitMatrix.getHeight();

        int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];

        for (int y = 0; y < bitMatrixHeight; y++) {
            int offset = y * bitMatrixWidth;

            for (int x = 0; x < bitMatrixWidth; x++) {

                pixels[offset + x] = bitMatrix.get(x, y) ?
                        getResources().getColor(R.color.QRBLACK):getResources().getColor(R.color.QRWHITE);
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444);

        bitmap.setPixels(pixels, 0, 500, 0, 0, bitMatrixWidth, bitMatrixHeight);
        return bitmap;


    }

    public void AddClick(View view) {
        boolean isEmpty = false;

        if(TextUtils.isEmpty(name.getText().toString())){
            name.setError("Enter Name");
            isEmpty = true;
        }if(TextUtils.isEmpty(price.getText().toString())){
            price.setError("Enter price");
            isEmpty = true;
        }if(TextUtils.isEmpty(quantity.getText().toString())){
            quantity.setError("Enter Quantity");
            isEmpty = true;
        }if(TextUtils.isEmpty(manufacturingDate.getText().toString())){
            manufacturingDate.setError("Seletc Manufacturing Date");
            isEmpty = true;
        }if(TextUtils.isEmpty(expireDate.getText().toString())){
            expireDate.setError("Seletc Expire Date");
            isEmpty = true;
        }

        if(!isEmpty){
            dialog.show();
            AndroidNetworking.post("http://swmapplication.com/API/add_product")
                    .addBodyParameter("name", name.getText().toString())
                    .addBodyParameter("price", price.getText().toString())
                    .addBodyParameter("quantity", quantity.getText().toString())
                    .addBodyParameter("manufactureDate", manufacturingDate.getText().toString())
                    .addBodyParameter("expire", expireDate.getText().toString())
                    .addBodyParameter("qrcode", "empty")
                    .setTag("test")
                    .setPriority(Priority.MEDIUM)
                    .build()
                    .getAsString(new StringRequestListener() {
                        @Override
                        public void onResponse(final String response) {
                            Bitmap bitmap = null;
                            try {
                                if(qrCheckBox.isChecked()){
                                    bitmap = TextToImageEncode(response);
                                }
                                else {
                                    bitmap = encodeAsBitmap(response, BarcodeFormat.CODE_128, 600, 300);
                                }


                            } catch (WriterException e) {
                                Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                            }

                            ByteArrayOutputStream byteArrayInputStream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayInputStream);
                            final byte[] imageBytes = byteArrayInputStream.toByteArray();
                            final String stringImage = Base64.encodeToString(imageBytes,Base64.DEFAULT);

                            uploadQrCode(stringImage,response);
                            // do anything with response
                        }
                        @Override
                        public void onError(ANError error) {
                            dialog.dismiss();
                            Toast.makeText(MainActivity.this, "No Network", Toast.LENGTH_LONG).show();
                            // handle error
                        }
                    });
        }

    }


    public void uploadQrCode(String qr, String id){
        AndroidNetworking.post("http://swmapplication.com/API/updateQrCode")
                .addBodyParameter("qr", qr)
                .addBodyParameter("id", id)
                .setTag("test")
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsString(new StringRequestListener() {
                    @Override
                    public void onResponse(String response) {
                        dialog.dismiss();
                        imagePath = response;
                        Toast.makeText(MainActivity.this, "Added Successfully Click Image to SHARE", Toast.LENGTH_LONG).show();
                        name.setText("");
                        price.setText("");
                        // do anything with response
                    }
                    @Override
                    public void onError(ANError error) {
                        dialog.dismiss();
                        Toast.makeText(MainActivity.this, "No Network", Toast.LENGTH_LONG).show();
                        // handle error
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {

            //if qrcode has nothing in it
            if (result.getContents() == null) {
                Toast.makeText(this, "Result Not Found", Toast.LENGTH_LONG).show();
            } else {

                final ProgressDialog saleDialog = new ProgressDialog(MainActivity.this);
                saleDialog.setMessage("Checking...");
                saleDialog.show();
                //if qr contains data
                AndroidNetworking.post("http://swmapplication.com/API/checkForSale")
                        .addBodyParameter("id", result.getContents().toString())
                        .setTag("test")
                        .setPriority(Priority.MEDIUM)
                        .build()
                        .getAsString(new StringRequestListener() {
                            @Override
                            public void onResponse(String response) {
                                saleDialog.dismiss();

                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    String is_sale = jsonObject.getString("is_sale");

                                    if(response.equalsIgnoreCase("sale")){
                                        String product = "Product Detail \n\n" + "Name: " + jsonObject.getString("name") +
                                                "\n" + "Price: " + jsonObject.getString("price") + "\n" +
                                                "Manufacture Date: " + jsonObject.getString("manufacturing_date") +
                                                "\n" + "Expire Date: " + jsonObject.getString("expire_date");
                                        AlertDialog.Builder builder= new AlertDialog.Builder(MainActivity.this);
                                        builder.setTitle("Successfully Sold");
                                        builder.setMessage(product);

                                        String[] productDetail = new String[4];

                                        productDetail[0] = "Name: " + jsonObject.getString("name");
                                        productDetail[1] = "Price: " + jsonObject.getString("price");
                                        productDetail[2] = "Manufacture Date: " + jsonObject.getString("manufacturing_date");
                                        productDetail[3] = "Expire Date: " + jsonObject.getString("expire_date");

                                        builder.setItems(productDetail, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        });

                                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        });

                                        builder.show();
                                        Toast.makeText(MainActivity.this, "Successfully Sold", Toast.LENGTH_SHORT).show();
                                    }
                                    else if(response.equalsIgnoreCase("invalid")){
                                        Toast.makeText(MainActivity.this, "Invalid Code", Toast.LENGTH_SHORT).show();
                                    }
                                    else {

                                        String product = "Product Detail \n\n" + "Name: " + jsonObject.getString("name") +
                                                "\n" + "Price: " + jsonObject.getString("price") + "\n" +
                                                "Manufacture Date: " + jsonObject.getString("manufacturing_date") +
                                                "\n" + "Expire Date: " + jsonObject.getString("expire_date");

                                        AlertDialog.Builder builder= new AlertDialog.Builder(MainActivity.this);

                                        builder.setTitle("Already sold");
                                        builder.setMessage(product);

                                        String[] productDetail = new String[4];

                                        /*productDetail[0] = "Name: " + jsonObject.getString("name");
                                        productDetail[1] = "Price: " + jsonObject.getString("price");
                                        productDetail[2] = "Manufacture Date: " + jsonObject.getString("manufacturing_date");
                                        productDetail[3] = "Expire Date: " + jsonObject.getString("expire_date");

                                        builder.setItems(productDetail, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        });*/

                                        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        });

                                        builder.show();
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                // do anything with response
                            }
                            @Override
                            public void onError(ANError error) {
                                saleDialog.dismiss();
                                Toast.makeText(MainActivity.this, "No Network", Toast.LENGTH_SHORT).show();
                                // handle error
                            }
                        });


            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.scan){
            qrScan.initiateScan();
        }
        return  true;
    }

    public void shareClick(View view) {
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("text/plain");
        share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

        // Add data to the intent, the receiving app will decide
        // what to do with it.
        share.putExtra(Intent.EXTRA_SUBJECT, "Code");
        share.putExtra(Intent.EXTRA_TEXT, imagePath);

        startActivity(Intent.createChooser(share, "Share link!"));
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
        String date = datePicker.getYear() + "-" + String.valueOf(datePicker.getMonth() + 1) + "-" + datePicker.getDayOfMonth();

        if(isExpireDate){
            expireDate.setText(date);
        }
        else {
            manufacturingDate.setText(date);
        }

    }
}

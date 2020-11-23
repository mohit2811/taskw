package com.arora.developer.task1;

import android.app.DatePickerDialog;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    EditText nameEdit, descEdit, expiryEdit;
    Spinner spinner;
    Calendar myCalendar;
    String catName = "select", name, desc, expiry, idd = "0";
    int day, month, yearS;
    ProgressBar progressBar;
    ArrayList<String> category, idlist;
    ArrayList<Uri> images;
    int PICK_IMAGE_MULTIPLE = 1;
    LinearLayout imageContainer;
    ArrayList<Bitmap> imagesEncodedList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        initListeners();
        getCategory();
    }

    private void initListeners() {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                catName = parent.getItemAtPosition(position).toString();
                idd = String.valueOf(id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                yearS = year;
                month = monthOfYear;
                day = dayOfMonth;
                updateLabel();
            }

        };

        expiryEdit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(MainActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    private void updateLabel() {
        String myFormat = "YYYY:MM:DD HH:MM:SS"; //format to be sent
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        expiryEdit.setText(new StringBuilder()
                // Month is 0 based so add 1
                .append(day).append("/").append(month + 1).append("/").append(yearS).append(" "));
        expiry = sdf.format(myCalendar.getTime());
    }


    private void initData() {
        category = new ArrayList<>();
        category.add(catName);
        idlist = new ArrayList<>();
        idlist.add(idd);
        progressBar = findViewById(R.id.progressBar);
        imagesEncodedList = new ArrayList<>();
        images = new ArrayList<Uri>();
        imageContainer = findViewById(R.id.imageContainer);
        //recyclerView=findViewById(R.id.recyclerView);
        myCalendar = Calendar.getInstance();
        spinner = findViewById(R.id.spinner);
        nameEdit = findViewById(R.id.nameEditTxt);
        descEdit = findViewById(R.id.descEditTxt);
        expiryEdit = findViewById(R.id.expiryEditTxt);
    }

    private void getCategory() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, "http://dev1.xicom.us/xttest/get_categories.php", new com.android.volley.Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.d("Volley response ", response);

                model jsonObject = new Gson().fromJson(response.toString(), model.class);
                for (int i = 0; i < jsonObject.getCategories().size(); i++) {
                    category.add(jsonObject.getCategories().get(i).getName());
                    idlist.add(jsonObject.getCategories().get(i).getId());
                }
                setSpinnerAdaper();
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.d("Volley error", error.getMessage());
            }
        });

        Volley.newRequestQueue(MainActivity.this).add(stringRequest);
    }

    private void setSpinnerAdaper() {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, category);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter);

    }

    public void addImages(View view) {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_MULTIPLE);
    }


    public void save(View view) {
        if (validateData()) {
            try {
                progressBar.setVisibility(View.VISIBLE);
                new uploadSelctedIMG().execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(MainActivity.this, "Enter Valid Details", Toast.LENGTH_LONG).show();
        }

    }

    private boolean validateData() {
//Validating Data

        name = nameEdit.getText().toString();
        desc = descEdit.getText().toString();


        if (catName.equals("select"))
            return false;
        if (name.equals(""))
            return false;
        if (desc.equals(""))
            return false;
        if (expiry.equals(""))
            return false;

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            // When an Image is picked
            if (requestCode == PICK_IMAGE_MULTIPLE && resultCode == RESULT_OK
                    && null != data) {
                // Get the Image from data

                ClipData mClipData = data.getClipData();

                // String imagepath = getPath(data.getData().getPath());
                imageContainer.removeAllViews();
                int pickedImageCount;

                for (pickedImageCount = 0; pickedImageCount < mClipData.getItemCount();
                     pickedImageCount++) {
                    final ImageView productImageView =
                            new ImageView(MainActivity.this);


                    productImageView.setAdjustViewBounds(true);
                    productImageView.setScaleType(ImageView.ScaleType.FIT_XY);

                    productImageView.setLayoutParams(new LinearLayout
                            .LayoutParams(200,
                            200));

                    imageContainer.addView(productImageView);
                    Picasso.get().load(mClipData.getItemAt(pickedImageCount).getUri()).error(R.drawable.bg).into(new com.squareup.picasso.Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

                            imagesEncodedList.add(bitmap);
                            productImageView.setImageBitmap(bitmap);
                        }

                        @Override
                        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                            System.out.println(e);
                            productImageView.setImageDrawable(getResources().getDrawable(R.drawable.bg));
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {

                        }
                    });
                }

            } else {
                Toast.makeText(MainActivity.this, "You haven't picked any Image",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "Error: Something went wrong " + e.getMessage(), Toast.LENGTH_LONG)
                    .show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public byte[] bitmapToByte(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);

        byte[] imageBytes = byteArrayOutputStream.toByteArray();


        return imageBytes;
    }

    class uploadSelctedIMG extends AsyncTask<String, String, Boolean> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        protected Boolean doInBackground(String... arg0) {
            try {

                //save bitmap to file

                File tempFile = new File(getApplicationContext().getCacheDir(), "image.jpg");

                tempFile.createNewFile();

                FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
                for (int i = 0; i < imagesEncodedList.size(); i++)
                    fileOutputStream.write(bitmapToByte(imagesEncodedList.get(i)));

                fileOutputStream.flush();

                fileOutputStream.close();

                //Upload data
                OkHttpClient client = new OkHttpClient().newBuilder()
                        .build();
                MediaType mediaType = MediaType.parse("text/plain");
                RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("category_id", idd)
                        .addFormDataPart("name", name)
                        .addFormDataPart("desc", desc)
                        .addFormDataPart("expiry", expiry)
                        .addFormDataPart("product_image[0]", tempFile.getAbsolutePath(),
                                RequestBody.create(MediaType.parse("application/octet-stream"),
                                        new File(tempFile.getAbsolutePath())))
                        .build();

                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url("http://dev1.xicom.us/xttest/save_user.php")
                        .method("POST", body)
                        .build();
                Response response = null;

                response = client.newCall(request).execute();
                if (response.code() == 200)
                    return true;

                Log.d("upload image response ", response.body().string());

            } catch (IOException ex) {
                ex.printStackTrace();

                //  Toast.makeText(DetailedActivity.this, "mm"+ex , Toast.LENGTH_SHORT).show();

                Log.d("upload error ", ex.getMessage());


            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);

            //if successful upload
            progressBar.setVisibility(View.GONE);

            if (result)
                Toast.makeText(MainActivity.this, "Success", Toast.LENGTH_SHORT).show();
        }

    }

}
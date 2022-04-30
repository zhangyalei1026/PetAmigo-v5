package com.example.petamigo.Post;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.petamigo.MainActivity;
import com.example.petamigo.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class PostActivity extends AppCompatActivity implements View.OnClickListener{
    private final DatabaseReference root = FirebaseDatabase.getInstance().getReference().child("myPosts");
    ImageView closeBut, postPic, displayLocation_iv;
    EditText caption, location;
    AppCompatButton postButton;
    ActivityResultLauncher<String> mGetContent;
    Uri resultUri = null;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    public static final int LOCATION_CODE = 301;
    private LocationManager locationManager;
    private String locationProvider = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        closeBut = findViewById(R.id.postCloseBut);
        postPic = findViewById(R.id.postPic);
        caption = findViewById(R.id.postCont);
        location = findViewById(R.id.postLoc);
        postButton = findViewById(R.id.postBut);
        displayLocation_iv = findViewById(R.id.displayLocation_iv);
        closeBut.setOnClickListener(this);
        postPic.setOnClickListener(this);
        caption.setOnClickListener(this);
        location.setOnClickListener(this);
        postButton.setOnClickListener(this);
        displayLocation_iv.setOnClickListener(this);
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();


        mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                Intent intent = new Intent(PostActivity.this, CropperActivity.class);
                intent.putExtra("Data", result.toString());
                startActivityForResult(intent, 101);
            }
        });
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.postCloseBut:
                startActivity(new Intent(PostActivity.this , MainActivity.class));
                finish();
                break;
            case R.id.postBut:
                final String randomKey = UUID.randomUUID().toString();
                final ProgressDialog pd = new ProgressDialog(this);
                PostModel postModel = new PostModel();
                postModel.setPostWord(caption.getText().toString());
                postModel.setPostLoc(location.getText().toString());
                postModel.setPostUser(FirebaseAuth.getInstance().getCurrentUser().getUid());
                pd.setTitle("Uploading Image...");
                pd.show();
                // Create a reference to "mountains.jpg"
                StorageReference mountainsRef = storageReference.child("images/" + randomKey);
                mountainsRef.putFile(resultUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                mountainsRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        postModel.setPostImg(uri.toString());
                                        String postId = root.push().getKey();
                                        postModel.setPostId(postId);
                                        root.child(postId).setValue(postModel);
                                        pd.dismiss();
                                        Snackbar.make(view, "Image uploaded", Snackbar.LENGTH_LONG);
                                        startActivity(new Intent(PostActivity.this , MainActivity.class));
                                        finish();
                                    }
                                });

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                pd.dismiss();
                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                double progressPercent = (100.00 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                                pd.setMessage("Progress: " + progressPercent + "%");
                            }
                        });
                break;
            case R.id.postPic:
                mGetContent.launch("image/*");
            //added by Zhang
            case R.id.displayLocation_iv:
                String locationResult = getLocation();
                //address_tv.setText(location);
                location.setText(locationResult);
                //Toast.makeText(this, "Location provider not available!", Toast.LENGTH_SHORT).show();

        }
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == -1 && requestCode == 101) {
            String result = data.getStringExtra("RESULT");

            if (result != null) {
                resultUri = Uri.parse(result);
            }
            postPic.setImageURI(resultUri);

        }
    }

    private String getLocation() {
        String locationResult = "";
        //1.get a location Manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //2.获取位置提供器，GPS或是NetWork
        List<String> providers = locationManager.getProviders(true);

        if (providers.contains(LocationManager.GPS_PROVIDER)) {
            //如果是GPS
            locationProvider = LocationManager.GPS_PROVIDER;
            //Log.v("TAG", "Location provider: GPS");
        } else if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
            //如果是Network
            locationProvider = LocationManager.NETWORK_PROVIDER;
            //Log.v("TAG", "Location provider: Network");
        }else {
            //Toast.makeText(this, "Location provider not available!", Toast.LENGTH_SHORT).show();
            //return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //ask for permission
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                //if no permission, then ask for permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_CODE);
            } else {
                //3.获取上次的位置，一般第一次运行，此值为null
                Location location = locationManager.getLastKnownLocation(locationProvider);
                if (location!=null){
                    Toast.makeText(this, location.getLongitude() + " " +
                            location.getLatitude() + "",Toast.LENGTH_SHORT).show();
                    DecimalFormat df = new DecimalFormat("#.00000");
                    //df.format(0.912385);
                    //String coordinate = String.valueOf(location.getLatitude()) + "\n"+ String.valueOf(location.getLongitude());
                    String coordinate = "Latitude: " + df.format(location.getLatitude()) + "\n" + "Longitude: " + df.format(location.getLongitude());
                    //latitude_tv.setText(coordinate);

                    //Log.v("TAG", "获取上次的位置-经纬度："+location.getLongitude()+"   "+location.getLatitude());

                    List<Address> addressList = getAddress(location);
                    //address_tv.setText(addressList.get(0).getLocality() + ", " + addressList.get(0).getAdminArea());
                    locationResult = addressList.get(0).getLocality() + ", " + addressList.get(0).getAdminArea();


                }else{
                    //监视地理位置变化，第二个和第三个参数分别为更新的最短时间minTime和最短距离minDistace
                    locationManager.requestLocationUpdates(locationProvider, 3000, 1,locationListener);
                }
            }
        } else {
            Location location = locationManager.getLastKnownLocation(locationProvider);
            if (location!=null){
                //Toast.makeText(this, location.getLongitude() + " " +
                //location.getLatitude() + "", Toast.LENGTH_SHORT).show();
                //Log.v("TAG", "获取上次的位置-经纬度："+location.getLongitude()+"   "+location.getLatitude());
                getAddress(location);

            }else{
                //监视地理位置变化，第二个和第三个参数分别为更新的最短时间minTime和最短距离minDistace
                locationManager.requestLocationUpdates(locationProvider, 3000, 1,locationListener);
            }
        }
        return locationResult;
    }

    public LocationListener locationListener = new LocationListener() {
        // Provider的状态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
        // Provider被enable时触发此函数，比如GPS被打开
        @Override
        public void onProviderEnabled(String provider) {
        }
        // Provider被disable时触发此函数，比如GPS被关闭
        @Override
        public void onProviderDisabled(String provider) {
        }
        //当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                //如果位置发生变化，重新显示地理位置经纬度
                //Toast.makeText(MainActivity.this, location.getLongitude() + " " +
                //location.getLatitude() + "", Toast.LENGTH_SHORT).show();
                //Log.v("TAG", "监视地理位置变化-经纬度："+location.getLongitude()+"   "+location.getLatitude());
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_CODE:
                if (grantResults.length > 0 && grantResults[0] == getPackageManager().PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    //Toast.makeText(this, "申请权限", Toast.LENGTH_LONG).show();
                    try {
                        List<String> providers = locationManager.getProviders(true);
                        if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
                            //如果是Network
                            locationProvider = LocationManager.NETWORK_PROVIDER;

                        } else if (providers.contains(LocationManager.GPS_PROVIDER)) {
                            //如果是GPS
                            locationProvider = LocationManager.GPS_PROVIDER;
                        }
                        Location location = locationManager.getLastKnownLocation(locationProvider);
                        if (location != null) {
                            //Toast.makeText(this, location.getLongitude() + " " +
                            //location.getLatitude() + "", Toast.LENGTH_SHORT).show();
                            //Log.v("TAG", "获取上次的位置-经纬度：" + location.getLongitude() + "   " + location.getLatitude());
                        } else {
                            // 监视地理位置变化，第二个和第三个参数分别为更新的最短时间minTime和最短距离minDistace
                            locationManager.requestLocationUpdates(locationProvider, 0, 0, locationListener);
                        }

                    } catch (SecurityException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(this, "No Permission Granted!", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }


    //获取地址信息:城市、街道等信息
    private List<Address> getAddress(Location location) {
        List<Address> result = null;
        try {
            if (location != null) {
                Geocoder gc = new Geocoder(this, Locale.getDefault());
                result = gc.getFromLocation(location.getLatitude(),
                        location.getLongitude(), 1);
                //Toast.makeText(this, "获取地址信息："+result.toString(), Toast.LENGTH_LONG).show();
                //Log.v("TAG", "获取地址信息："+result.toString());
                //address_tv.setText(result.get(0).getLocality() + ", " + result.get(0).getAdminArea());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
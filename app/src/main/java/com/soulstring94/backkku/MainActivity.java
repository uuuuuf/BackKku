package com.soulstring94.backkku;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    Button btnImage;
    ImageView imageView;
    Switch toggle;

    boolean toggleFlag = false;
    boolean permission1 = false;
    boolean permission2 = false;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    private static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 1;
    private static final int SEND_IMAGE_REQUEST_CODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initMain();

        editor = sharedPreferences.edit();
        if(sharedPreferences.getString("toggleFlag", "") != null
                || !sharedPreferences.getString("toggleFlag", "").equals("")) {
            if(sharedPreferences.getString("toggleFlag", "") == "true"
                    || sharedPreferences.getString("toggleFlag", "").equals("true")) {
                toggle.setChecked(true);
                toggleFlag = true;
            } else {
                toggle.setChecked(false);
                toggleFlag = false;
            }
        } else {
            editor.putString("toggleFlag", "false");
            toggle.setChecked(false);
            toggleFlag = false;
        }

        View.OnClickListener clickListener = view -> {
            switch (view.getId()) {
                case R.id. btnImage:
                    Intent intent1 = new Intent(Intent.ACTION_PICK);
                    intent1.setType("image/*");
                    startActivityForResult(intent1, SEND_IMAGE_REQUEST_CODE);
                    break;
            }
        };

        btnImage.setOnClickListener(clickListener);

        toggle.setOnClickListener(v -> {
            toggleFlag = !toggleFlag;
            Intent trueIntent = new Intent(MainActivity.this, MyService.class);
            trueIntent.putExtra("toggleFlag", toggleFlag);
            startService(trueIntent);

            if(toggleFlag) {
                toggle.setChecked(true);
                editor.putString("toggleFlag", "true");
                editor.apply();
            } else {
                toggle.setChecked(false);
                editor.putString("toggleFlag", "false");
                editor.apply();
            }
        });
    }

    private void initMain() {
        if(!permission1 || !permission2) {
            firstPermission();
        }

        btnImage = findViewById(R.id.btnImage);
        imageView = findViewById(R.id.imageView);
        toggle = findViewById(R.id.toggle);

        sharedPreferences = getSharedPreferences("toggleFlag", MODE_PRIVATE);
    }

    public void checkPermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
                permission1 = true;
            }
        }
    }

    private void sendImage(Bitmap bitmap) throws IOException {
        File outputDir = getCacheDir(); // Get the cache directory
        File outputFile = File.createTempFile("image", ".png", outputDir);

        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(this, MyService.class);
        intent.putExtra("image_path", outputFile.getAbsolutePath());
        startService(intent);

        toggleFlag = true;
        toggle.setChecked(true);
        editor.putString("toggleFlag", "true");
        editor.apply();

        Intent intent2 = new Intent(this, MyService.class);
        intent2.putExtra("toggleFlag", toggleFlag);
        startService(intent2);
    }

    private void firstPermission() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("권한 요청");
        builder.setMessage("Back Key를 사용하려면 권한을 허용해주세요.");
        builder.setPositiveButton("허용", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                checkPermission();

                try {
                    Thread.sleep(1000);
                    secondPermission();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        builder.setNegativeButton("거절", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                System.exit(0);
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void secondPermission() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("접근성 요청");
        builder.setMessage("뒤로가기 기능을 사용하기 위하여\n설치된 앱 > BackKku > 사용 안 함을 사용 중으로 바꿔주세요.");
        builder.setPositiveButton("허용", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
                permission2 = true;
            }
        });
        builder.setNegativeButton("거절", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                System.exit(0);
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SEND_IMAGE_REQUEST_CODE) {
            if(resultCode == RESULT_OK) {
                Uri uri = data.getData();
                imageView.setImageURI(uri);

                BitmapDrawable drawable = (BitmapDrawable)imageView.getDrawable();
                Bitmap bitmap = drawable.getBitmap();

                try {
                    sendImage(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if(requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(!Settings.canDrawOverlays(this)) {
                    Toast.makeText(MainActivity.this, R.string.permission_disagree, Toast.LENGTH_SHORT).show();
                    checkPermission();
                }
            }
        }
    }
}
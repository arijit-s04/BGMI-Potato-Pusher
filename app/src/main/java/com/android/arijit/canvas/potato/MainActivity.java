package com.android.arijit.canvas.potato;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.documentfile.provider.DocumentFile;

import com.android.arijit.canvas.potato.databinding.ActivityMainBinding;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    private String FILE_NAME = "game_patch_1.4.1.15924.pak";
    private ActivityMainBinding binding;
    private final String[] perm = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private Uri dest;
    @SuppressLint("SdCardPath")
    private String srcPath = "/sdcard/potato/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadFiles();
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.tvPakName.setText(FILE_NAME);
        checkPermissions();

        dest = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid/document/primary%3AAndroid%2Fdata%2Fcom.pubg.imobile%2Ffiles%2FUE4Game%2FShadowTrackerExtra%2FShadowTrackerExtra%2FSaved%2FPaks%2F" + FILE_NAME);
        try {
            grantUriPermission("com.android.arijit.canvas.potato", dest, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (Exception e) {
            Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            i.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid/document/primary%3AAndroid"));
            i.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            startActivityForResult(i, 1001);
        }

        srcPath += FILE_NAME;


        binding.btnPush.setOnClickListener(v -> {
            binding.tvReport.setVisibility(View.GONE);
            File patch = new File("/storage/emulated/0/Android/data/com.pubg.imobile/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Paks/" + FILE_NAME);
            if (patch.exists()) {
                toggle(binding.tvReport);
                binding.tvReport.setText(getText(R.string.exists));
                return;
            }
            patch = new File(srcPath);
            if(!patch.exists()){
                toggle(binding.tvReport);
                binding.tvReport.setText(getText(R.string.no_input));
                return;
            }
            toggle(binding.progressBar);
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                i.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid/document/primary%3AAndroid%2Fdata%2Fcom.pubg.imobile%2Ffiles%2FUE4Game%2FShadowTrackerExtra%2FShadowTrackerExtra%2FSaved%2FPaks"));
                i.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                startActivityForResult(i, 9999);
            }
            else {
                transferFileNonScoped();
            }

        });

        binding.editTool.setOnClickListener(v -> {
            toggle(binding.tvPakName);
            toggle(binding.editTool);
            toggle(binding.etPakName);
            toggle(binding.doneTool);
            binding.etPakName.setText(FILE_NAME);
        });

        binding.doneTool.setOnClickListener(v -> {
            String newFile = binding.etPakName.getText().toString();
            setFiles(newFile);
            toggle(binding.tvPakName);
            toggle(binding.editTool);
            toggle(binding.etPakName);
            toggle(binding.doneTool);
            binding.tvPakName.setText(FILE_NAME);
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 9999:
                if (data == null) {
                    toggle(binding.progressBar);
                    toggle(binding.tvReport);
                    binding.tvReport.setText(R.string.permission_denied);
                    break;
                }
                Log.i("TAG", "onActivityResult: " + data.getData());

                getContentResolver().takePersistableUriPermission(data.getData(), Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

                DocumentFile foo = DocumentFile.fromTreeUri(this, data.getData());
                if (foo != null)
                    foo.createFile( "bin", FILE_NAME);
                transferFile();
                break;
            case 1001:
                if(data != null)
                    getContentResolver().takePersistableUriPermission(data.getData(), Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                break;
        }
    }

    private void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, perm, 101);
        }
    }

    private void toggle(@NonNull View v) {
        if (v.getVisibility() == View.VISIBLE) {
            v.setVisibility(View.GONE);
        } else v.setVisibility(View.VISIBLE);
    }
    private void transferFile() {
        String res = null;
        try {
            InputStream in = new FileInputStream(srcPath);
            ParcelFileDescriptor pfd = getContentResolver().
                    openFileDescriptor(dest, "w");
            FileOutputStream fileOutputStream =
                    new FileOutputStream(pfd.getFileDescriptor());
            byte[] buffer = new byte[1024];
            int read = in.read(buffer);
            while (read != -1) {
                fileOutputStream.write(buffer, 0, read);
                read = in.read(buffer);
            }
            // Let the document provider know you're done by closing the stream.
            fileOutputStream.close();
            pfd.close();
            res = "Done!!!";
        } catch (Exception e) {
            res = e.getMessage();
            e.printStackTrace();
        } finally {
            toggle(binding.progressBar);
            toggle(binding.tvReport);
            binding.tvReport.setText(res);
        }
    }
    private void loadFiles(){
        SharedPreferences sh = getSharedPreferences("filename", MODE_PRIVATE);
        FILE_NAME = sh.getString("name", FILE_NAME);
    }
    @SuppressLint("ApplySharedPref")
    private void setFiles(String fileName){
        FILE_NAME = fileName;
        SharedPreferences.Editor et = getSharedPreferences("filename", MODE_PRIVATE).edit();
        et.putString("name", FILE_NAME);
        et.commit();
    }
    private void transferFileNonScoped(){
        String res = null;
        try{
            InputStream in = new FileInputStream(srcPath);
            FileOutputStream fileOutputStream =
                    new FileOutputStream("/storage/emulated/0/Android/data/com.pubg.imobile/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/Paks/" + FILE_NAME);
            byte[] buffer = new byte[1024];
            int read = in.read(buffer);
            while (read != -1) {
                fileOutputStream.write(buffer, 0, read);
                read = in.read(buffer);
            }
            // Let the document provider know you're done by closing the stream.
            fileOutputStream.close();
            res = "Done!!!";
        }
        catch (Exception e) {
            res = e.getMessage();
            e.printStackTrace();
        } finally {
            toggle(binding.progressBar);
            toggle(binding.tvReport);
            binding.tvReport.setText(res);
        }
    }
}
/*
ParcelFileDescriptor pfd = getActivity().getContentResolver().
                openFileDescriptor(uri, "w");
        FileOutputStream fileOutputStream =
                new FileOutputStream(pfd.getFileDescriptor());

StringBuilder stringBuilder = new StringBuilder();
    try (InputStream inputStream =
            getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(
            new InputStreamReader(Objects.requireNonNull(inputStream)))) {
        String line;
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
        }
    }
 */
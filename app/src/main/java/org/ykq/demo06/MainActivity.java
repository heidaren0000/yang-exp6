package org.ykq.demo06;
import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {
    private EditText mEditText;
    private TextInputEditText mTextInputEditText;
    private Button  mButton2File;
    private Button  mButton2Preference;
    private Button  mButton2Read;

    // set filename to global for later read, simple but seems not graceful :/
    private String filename;

    @Override
    protected void onCreate(Bundle savedInstanceStates) {
        super.onCreate(savedInstanceStates);
        setContentView(R.layout.activity_main);

        // get all stuff
        mEditText = findViewById(R.id.filename_or_keyname);
        mTextInputEditText = findViewById(R.id.text2store);
        mButton2File = findViewById(R.id.btn2file);
        mButton2Preference = findViewById(R.id.btn2preference);
        mButton2Read = findViewById(R.id.btn2read);

        // preparing for share_preference
        // get SharedPreference and Editor, we just use default SharedPreference from PreferenceManger
        SharedPreferences spDefault = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor speDefault = spDefault.edit();

        mEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                filename = ((EditText)view).getText().toString();
            }
        });

        mButton2Preference.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String keyname = mEditText.getText().toString();
                String texts2store = mTextInputEditText.getText().toString();
                speDefault.putString(keyname, texts2store);
                speDefault.apply();
            }
        });

        // preparing for file system storage.
        // note: it's really annoying that some apps just rudely ask you for storage permission
        //       and store shits inside /sdcard, which makes device storage kinda looks like garbage field
        //       so even though i'm gonna let the app ask storage permission,
        //       however, files would be stored inside /sdcard/Android/[package_name].
        //       personally i think this is the best practice to both demonstrate permission granting and file storing.

        mButton2File.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // we ask for permission first, it required in newer android.
                // my test environment is not above Marshmallow so i cannot test if it works :(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 12);
                    }
                }

                filename = mEditText.getText().toString();
                String text2store = mTextInputEditText.getText().toString();
                try{
                    File file = new File(getExternalFilesDir(null), filename);
                    // mode private might be the only option now in 2023(for higher SDK version), not checked yet
                    FileOutputStream fout = new FileOutputStream(file);
                    OutputStreamWriter foutw = new OutputStreamWriter(fout, StandardCharsets.UTF_8);
                    BufferedWriter bufferedWriter = new BufferedWriter(foutw);
                    bufferedWriter.write(text2store);
                    bufferedWriter.close();
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        // read all preferences and content of target file name and prints into toast
        mButton2Read.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filename = mEditText.getText().toString();
                String dataFromSP = getString(R.string.template_read_pref).toString() + spDefault.getAll().toString() + "\n";
                String dataFromFile = getString(R.string.template_read_file).toString() + readFromTargetFile(filename) + "\n";
                String toaster = dataFromSP + dataFromFile;
                Toast.makeText(getApplicationContext(), toaster, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String readFromTargetFile(String filename) {
        FileInputStream fin = null;
        try {
            File file = new File(getExternalFilesDir(null), filename);
            if(!file.exists()) {
                return "File not exists!";
            }
            fin = new FileInputStream(file);
            InputStreamReader inr = new InputStreamReader(fin, StandardCharsets.UTF_8);
            BufferedReader inrb = new BufferedReader(inr);
            StringBuilder result = new StringBuilder();
            String line = null;
            while((line = inrb.readLine())!= null) {
                result.append(line);
            }
            inrb.close();
            return result.toString();
        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
            return "File not exist.";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

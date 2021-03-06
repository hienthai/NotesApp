package com.hienthai.notesapp.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.hienthai.notesapp.R;
import com.hienthai.notesapp.database.NotesDatabase;
import com.hienthai.notesapp.entities.Note;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import gun0912.tedimagepicker.builder.TedImagePicker;
import gun0912.tedimagepicker.builder.listener.OnSelectedListener;

public class SaveNoteActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = SaveNoteActivity.class.getName();
//    private static final int REQUEST_CODE_STORAGE_CAMERA = 1;
//    private static final int REQUEST_CODE_SELECT_IMAGE = 2;

    private ImageView imgBack, imgDone, imgNote;

    private EditText edtNoteTitle, edtNoteSubTitle, edtContentNote;
    private TextView txtDateTime, txtUrl;
    private View viewSubTitleIndicator;

    private LinearLayout layoutUrl;

    private AlertDialog addUrlDialog, deleteNoteDialog;


    private String selectedColor, selectedImagePath;

    private Note dataNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_note);

        anhXa();

        // format date Friday, 30 june 1998 20:21 PM
        txtDateTime.setText(new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault()).format(new Date()));


        imgBack.setOnClickListener(this);
        imgDone.setOnClickListener(this);


        selectedColor = "#333333";
        selectedImagePath = "";

        if (getIntent().getBooleanExtra("isViewOrUpdate", false)) {
            dataNote = (Note) getIntent().getSerializableExtra("note");

            setViewOrUpdateNote();
        }

        findViewById(R.id.imgDeleteUrl).setOnClickListener(v -> {
            txtUrl.setText(null);
            layoutUrl.setVisibility(View.GONE);
        });

        findViewById(R.id.imgDeleteIamge).setOnClickListener(v -> {
            imgNote.setImageBitmap(null);
            imgNote.setVisibility(View.GONE);
            findViewById(R.id.imgDeleteIamge).setVisibility(View.GONE);

            selectedImagePath = "";
        });


        initOptionsLayout();
        setColorSubtitleIndicator();


        Log.e(TAG, "onCreate");
    }

    private void setViewOrUpdateNote() {
        edtNoteTitle.setText(dataNote.getTitle());
        edtNoteSubTitle.setText(dataNote.getSubtitle());
        edtContentNote.setText(dataNote.getNoteText());
        txtDateTime.setText(dataNote.getDateTime());

        if (dataNote.getImagePath() != null && !dataNote.getImagePath().trim().isEmpty()) {
            imgNote.setImageURI(Uri.parse(dataNote.getImagePath()));
            imgNote.setVisibility(View.VISIBLE);
            findViewById(R.id.imgDeleteIamge).setVisibility(View.VISIBLE);

            selectedImagePath = dataNote.getImagePath();
        }

        if (dataNote.getWebLink() != null && !dataNote.getWebLink().trim().isEmpty()) {
            txtUrl.setText(dataNote.getWebLink());
            layoutUrl.setVisibility(View.VISIBLE);
        }
    }

    private void anhXa() {
        imgBack = findViewById(R.id.imgBack);
        imgDone = findViewById(R.id.imgSave);
        edtNoteTitle = findViewById(R.id.edtNoteTitle);
        edtNoteSubTitle = findViewById(R.id.edtNoteSubTitle);
        edtContentNote = findViewById(R.id.edtContentNote);
        txtDateTime = findViewById(R.id.txtDateTime);
        viewSubTitleIndicator = findViewById(R.id.viewSubTitleIndicator);

        imgNote = findViewById(R.id.imgNote);

        txtUrl = findViewById(R.id.txtUrl);
        layoutUrl = findViewById(R.id.layoutUrl);
    }


    private void saveNote() {
        if (edtNoteTitle.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Note title can't be empty!", Toast.LENGTH_SHORT).show();
            return;
        } else if (edtNoteSubTitle.getText().toString().trim().isEmpty() && edtContentNote.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Note can't be empty!", Toast.LENGTH_SHORT).show();
            return;
        }


        final Note note = new Note();
        note.setTitle(edtNoteTitle.getText().toString());
        note.setSubtitle(edtNoteSubTitle.getText().toString());
        note.setNoteText(edtContentNote.getText().toString());
        note.setDateTime(txtDateTime.getText().toString());
        note.setColor(selectedColor);
        note.setImagePath(selectedImagePath);

        if (layoutUrl.getVisibility() == View.VISIBLE) {
            note.setWebLink(txtUrl.getText().toString());
        }

        if (dataNote != null) {
            note.setId(dataNote.getId());
        }

        NotesDatabase.getInstance(this).noteDAO().insertNote(note);

        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();

    }

    private void initOptionsLayout() {
        LinearLayout layoutOptions = findViewById(R.id.layoutOptions);
        BottomSheetBehavior<LinearLayout> bottomSheetBehavior = BottomSheetBehavior.from(layoutOptions);
        layoutOptions.findViewById(R.id.txtTitleChooseColor).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });


        ImageView imgColor1 = layoutOptions.findViewById(R.id.imgColor1);
        ImageView imgColor2 = layoutOptions.findViewById(R.id.imgColor2);
        ImageView imgColor3 = layoutOptions.findViewById(R.id.imgColor3);
        ImageView imgColor4 = layoutOptions.findViewById(R.id.imgColor4);
        ImageView imgColor5 = layoutOptions.findViewById(R.id.imgColor5);

        layoutOptions.findViewById(R.id.viewColor1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedColor = "#333333";
                imgColor1.setImageResource(R.drawable.ic_done);
                imgColor2.setImageResource(0);
                imgColor3.setImageResource(0);
                imgColor4.setImageResource(0);
                imgColor5.setImageResource(0);

                setColorSubtitleIndicator();
            }
        });

        layoutOptions.findViewById(R.id.viewColor2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedColor = "#FDBE3B";
                imgColor1.setImageResource(0);
                imgColor2.setImageResource(R.drawable.ic_done);
                imgColor3.setImageResource(0);
                imgColor4.setImageResource(0);
                imgColor5.setImageResource(0);

                setColorSubtitleIndicator();
            }
        });

        layoutOptions.findViewById(R.id.viewColor3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedColor = "#FF4842";
                imgColor1.setImageResource(0);
                imgColor2.setImageResource(0);
                imgColor3.setImageResource(R.drawable.ic_done);
                imgColor4.setImageResource(0);
                imgColor5.setImageResource(0);

                setColorSubtitleIndicator();
            }
        });

        layoutOptions.findViewById(R.id.viewColor4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedColor = "#3A52FC";
                imgColor1.setImageResource(0);
                imgColor2.setImageResource(0);
                imgColor3.setImageResource(0);
                imgColor4.setImageResource(R.drawable.ic_done);
                imgColor5.setImageResource(0);

                setColorSubtitleIndicator();
            }
        });


        layoutOptions.findViewById(R.id.viewColor5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedColor = "#81DD17";
                imgColor1.setImageResource(0);
                imgColor2.setImageResource(0);
                imgColor3.setImageResource(0);
                imgColor4.setImageResource(0);
                imgColor5.setImageResource(R.drawable.ic_done);

                setColorSubtitleIndicator();
            }
        });

        if (dataNote != null && dataNote.getColor() != null && !dataNote.getColor().trim().isEmpty()) {
            switch (dataNote.getColor()) {

                case "#FDBE3B":
                    layoutOptions.findViewById(R.id.viewColor2).performClick();

                    break;
                case "#FF4842":
                    layoutOptions.findViewById(R.id.viewColor3).performClick();
                    break;
                case "#3A52FC":
                    layoutOptions.findViewById(R.id.viewColor4).performClick();
                    break;
                case "#81DD17":
                    layoutOptions.findViewById(R.id.viewColor5).performClick();
                    break;

            }
        }


        layoutOptions.findViewById(R.id.imgChooseImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
//                if (ContextCompat.checkSelfPermission(
//                        getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
//                        != PackageManager.PERMISSION_GRANTED) {
//                    ActivityCompat.requestPermissions(SaveNoteActivity.this, new String[]
//                            {Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_CAMERA);
//                } else {
//                    selectImage();
//                }

                requestPermissions();
            }
        });

        layoutOptions.findViewById(R.id.imgAddUrl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                showDialog();
            }
        });

        if (dataNote != null) {
            layoutOptions.findViewById(R.id.layoutOptionDeleteNote).setVisibility(View.VISIBLE);
            layoutOptions.findViewById(R.id.layoutOptionDeleteNote).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    showDeleteNoteDialog();
                }
            });
        }

    }

    private void requestPermissions() {

        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {

                Toast.makeText(SaveNoteActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();

                openImagePicker();

            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                Toast.makeText(SaveNoteActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }

        };

        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
                .check();
    }

    private void openImagePicker() {
        TedImagePicker.with(this)
                .start(new OnSelectedListener() {
                    @Override
                    public void onSelected(Uri uri) {
                        showSingleImage(uri);
                    }
                });
    }

    private void showSingleImage(Uri uri) {
        imgNote.setImageURI(uri);
        imgNote.setVisibility(View.VISIBLE);
        findViewById(R.id.imgDeleteIamge).setVisibility(View.VISIBLE);
        selectedImagePath = uri.toString();
    }

    private void showDeleteNoteDialog() {
        if (deleteNoteDialog == null) {

            AlertDialog.Builder builder = new AlertDialog.Builder(SaveNoteActivity.this);

            View view = LayoutInflater.from(this).inflate(R.layout.layout_delete_note, (ViewGroup) findViewById(R.id.deleteNoteDialog));

            builder.setView(view);

            deleteNoteDialog = builder.create();

            if (deleteNoteDialog.getWindow() != null) {
                deleteNoteDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }


            view.findViewById(R.id.txtDeleteNoteDialog).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    NotesDatabase.getInstance(getApplicationContext()).noteDAO().deleteNote(dataNote);

                    Intent intent = new Intent();

                    setResult(RESULT_OK, intent);

                    deleteNoteDialog.dismiss();

                    finish();
                }
            });

            view.findViewById(R.id.txtCancelDialog).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteNoteDialog.dismiss();
                }
            });


        }

        deleteNoteDialog.show();
    }

//    private void selectImage() {
//
//        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//        if (intent.resolveActivity(getPackageManager()) != null) {
//            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
//        }
//
//    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        if (requestCode == REQUEST_CODE_STORAGE_CAMERA && grantResults.length > 0) {
//            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                selectImage();
//            } else {
//                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

    private void setColorSubtitleIndicator() {

        viewSubTitleIndicator.setBackgroundColor(Color.parseColor(selectedColor));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
//            if (data != null) {
//
//                Uri imageSelected = data.getData();
//                if (imageSelected != null) {
//                    try {
//                        InputStream inputStream = getContentResolver().openInputStream(imageSelected);
//
//                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
//                        imgNote.setImageBitmap(bitmap);
//                        imgNote.setVisibility(View.VISIBLE);
//                        findViewById(R.id.imgDeleteIamge).setVisibility(View.VISIBLE);
//
//
//                        selectedImagePath = getImagePathFromInputStreamUri(imageSelected);
//
//                        System.out.println(selectedImagePath);
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
    }


//    public String getImagePathFromInputStreamUri(Uri uri) {
//        InputStream inputStream = null;
//        String filePath = null;
//
//        if (uri.getAuthority() != null) {
//            try {
//                inputStream = getContentResolver().openInputStream(uri); // context needed
//                File photoFile = createTemporalFileFrom(inputStream);
//
//                filePath = photoFile.getPath();
//
//            } catch (FileNotFoundException e) {
//                // log
//            } catch (IOException e) {
//                // log
//            } finally {
//                try {
//                    inputStream.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//
//        return filePath;
//    }
//
//    private File createTemporalFileFrom(InputStream inputStream) throws IOException {
//        File targetFile = null;
//
//        if (inputStream != null) {
//            int read;
//            byte[] buffer = new byte[8 * 1024];
//
//            targetFile = createTemporalFile();
//            OutputStream outputStream = new FileOutputStream(targetFile);
//
//            while ((read = inputStream.read(buffer)) != -1) {
//                outputStream.write(buffer, 0, read);
//            }
//            outputStream.flush();
//
//            try {
//                outputStream.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        return targetFile;
//    }
//
//    private File createTemporalFile() {
//        return new File(getExternalCacheDir(), "tempFile.jpg"); // context needed
//    }


    private void showDialog() {
        if (addUrlDialog == null) {

            AlertDialog.Builder builder = new AlertDialog.Builder(SaveNoteActivity.this);

            View view = LayoutInflater.from(this).inflate(R.layout.layout_add_url, (ViewGroup) findViewById(R.id.layoutAddUrl));

            builder.setView(view);

            addUrlDialog = builder.create();

            if (addUrlDialog.getWindow() != null) {
                addUrlDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            EditText edtUrl = view.findViewById(R.id.edtAddUrl);
            edtUrl.requestFocus();

            view.findViewById(R.id.txtAdd).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (edtUrl.getText().toString().trim().isEmpty()) {
                        Toast.makeText(SaveNoteActivity.this, "Url Empty!", Toast.LENGTH_SHORT).show();

                    } else if (!Patterns.WEB_URL.matcher(edtUrl.getText().toString()).matches()) {
                        Toast.makeText(SaveNoteActivity.this, "Url invalid!", Toast.LENGTH_SHORT).show();

                    } else {
                        txtUrl.setText(edtUrl.getText().toString());
                        layoutUrl.setVisibility(View.VISIBLE);

                        addUrlDialog.dismiss();
                    }
                }
            });

            view.findViewById(R.id.txtCancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addUrlDialog.dismiss();
                }
            });


        }

        addUrlDialog.show();
    }


    @Override
    protected void onStart() {
        super.onStart();

        Log.e(TAG, "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.e(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.e(TAG, "onDestroy");
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.e(TAG, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.e(TAG, "onResume");
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        Log.e(TAG, "onRestart");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imgSave:
                saveNote();
                break;
            case R.id.imgBack:
                onBackPressed();
                break;
        }
    }
}
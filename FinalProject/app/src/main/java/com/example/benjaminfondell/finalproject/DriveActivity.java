package com.example.benjaminfondell.finalproject;

import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.drive.CreateFileActivityOptions;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.OpenFileActivityOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.io.IOException;
import java.util.Collections;
import java.util.Objects;

/**
 * Created by benjaminfondell on 11/24/17.
 */

public class DriveActivity extends MainActivity {

    //class variables
    private static final String TAG = "MainActivity";
    private static final int REQUEST_NEW_NOTE_CODE = NEXT_REQUEST_CODE;
    private static final int REQUEST_CODE_OPENER = NEXT_REQUEST_CODE + 1;
    private static final String FILE_TYPE_TEXT = "text/plain";

    //constructor on create
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {

        Log.d(TAG, "onCreate: Create Drive Activity.");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set xml fields and buttons
        mEditTextTitle = (EditText) findViewById(R.id.editTextTitle);
        mEditTextNoteBody = (EditText) findViewById(R.id.editTextContents);
        mSaveButton = (Button) findViewById(R.id.buttonSave);
        final ImageButton cameraButton = findViewById(R.id.camera_button);

        //do not display image string in edit text field
        if(mMetadata!=null) 
        {
            if (!Objects.equals("image/jpeg",mMetadata.getMimeType())) refreshUiFromCurrentFile();
        }

        //handle camera button click
        //take picture with device
        cameraButton.setOnClickListener(new View.OnClickListener() 
        {
            public void onClick(View v)
            {
                startActivityForResult(
                        new Intent(MediaStore.ACTION_IMAGE_CAPTURE), REQUEST_TAKE_PICTURE_CODE);
            }
        });

        //handle save button click
        mSaveButton.setOnClickListener(new View.OnClickListener() 
        {
            @Override
            public void onClick(View v)
            {
                save();
            }
        });
    }

    //build menu and buttons
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        Log.d(TAG, "onCreateOptionsMenu: Menu Created.");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //handle menu item buttons
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        Log.d(TAG, "onOptionsItemSelected: Item selected from menu.");
        if (item.getItemId() == R.id.menu_new)
        {
            Log.i(TAG, "New note selected.");
            createFile();

        } else if (item.getItemId() == R.id.menu_open)
        {
            Log.i(TAG, "Open file selected.");
            openFile();
        }
        else if(item.getItemId() == R.id.logout_button)
        {
            Log.i(TAG, "Logout selected.");
            signOut();
        }
        return super.onOptionsItemSelected(item);
    }

    //on result of activity handle open and create note
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {

        Log.d(TAG, "onActivityResult: ");
        switch (requestCode)
        {
            //new note create
            case REQUEST_NEW_NOTE_CODE:
                Log.d(TAG, "onActivityResult: Create new note;");
            //open note
            case REQUEST_CODE_OPENER:

                if (resultCode == RESULT_OK)
                {
                    //get current image in app
                    mDriveIdCurrent = (DriveId) data.getParcelableExtra(
                            OpenFileActivityOptions.EXTRA_RESPONSE_DRIVE_ID);
                    //if no current image then create new image in drive
                    if (mDriveIdCurrent == null)
                    {
                        mPictureToSave = (Bitmap) data.getExtras().get("data");
                        Log.i(TAG, "Creating new contents.");
                        final Bitmap image = mPictureToSave;

                        //place picture in drive
                        mDriveClientResource
                                .createContents()
                                .continueWithTask(
                                        new Continuation<DriveContents, Task<Void>>()
                                        {
                                            @Override
                                            public Task<Void> then(@NonNull Task<DriveContents> task) throws Exception
                                            {
                                                return createFileIntentSender(task.getResult(), image);
                                            }
                                        });

                        //clear picture variable to avoid duplicates
                        mPictureToSave = null;
                        return;

                    }
                    else
                    {
                        //disolay the current note
                        getCurrentFile();
                    }
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void openFile()
    {
        Log.d(TAG, "openFile: Open was pressed. ");
        Log.i(TAG, "Open Drive file.");

        //test that user is signed in
        if (!isSignedIn())
        {
            Log.w(TAG, "Failed to open file, user is not signed in.");
            return;
        }

        //set app fields
        EditText et = findViewById(R.id.editTextContents);
        EditText etTitle = findViewById(R.id.editTextTitle);

        //set hint
        et.setHint("Enter text here...");
        etTitle.setHint("Title");

        //established open file options
        final OpenFileActivityOptions openFileActivityOptions =
                new OpenFileActivityOptions.Builder()
                        .setMimeType(Collections.singletonList(FILE_TYPE_TEXT))
                        .build();

        //open new file with intent sender to api
        mDriveClient.newOpenFileActivityIntentSender(openFileActivityOptions)
                .addOnSuccessListener(new OnSuccessListener<IntentSender>()
                {
                    @Override
                    public void onSuccess(IntentSender intentSender)
                    {
                        //send request to api
                        try
                        {
                            //startIntentSender(<intent>,<request code>,<fill intent>,<flags mask>,<flag values>,<extra flags>);
                            startIntentSenderForResult(intentSender, REQUEST_NEW_NOTE_CODE, null, 0, 0, 0);
                        } catch (SendIntentException e) {
                            Log.w(TAG, "Unable to send intent.", e);
                        }
                    }
                });
    }

    //create a new image file
    private void createFile() {
        Log.d(TAG, "createFile: ");
        Log.i(TAG, "Create drive file.");

        //if not signed in break
        if (!isSignedIn())
        {
            Log.w(TAG, "Failed to create file, user is not signed in.");
            return;
        }

        //clear variables
        mDriveContents = null;
        mMetadata = null;

        //create contents of file from edit text fields
        mDriveClientResource.createContents()
                .continueWithTask(new Continuation<DriveContents, Task<IntentSender>>()
                {
                    @Override
                    public Task<IntentSender> then(@NonNull Task<DriveContents> task)
                    {
                        if (!task.isSuccessful())
                        {
                            return Tasks.forException(task.getException());
                        }
                        Log.i(TAG, "New contents created.");

                        //build note object for api
                        MetadataChangeSet metadataChangeSet =
                                new MetadataChangeSet.Builder()
                                        .setMimeType(FILE_TYPE_TEXT)
                                        .build();

                        CreateFileActivityOptions createFileActivityOptions =
                                new CreateFileActivityOptions.Builder()
                                        .setInitialMetadata(metadataChangeSet)
                                        .setInitialDriveContents(task.getResult())
                                        .build();

                        return mDriveClient.newCreateFileActivityIntentSender(createFileActivityOptions);
                    }
                }).addOnSuccessListener(new OnSuccessListener<IntentSender>()
        {
            @Override
            public void onSuccess(IntentSender intentSender)
            {
                //send request to create file with note contents
                Log.i(TAG, "New CreateActivityIntent created.");
                try
                {

                    //startIntentSender(<intent>,<request code>,<fill intent>,<flags mask>,<flag values>,<extra flags>);
                    startIntentSenderForResult(intentSender, REQUEST_NEW_NOTE_CODE, null, 0, 0, 0);

                } catch (SendIntentException e) {
                    Log.e(TAG, "Failed to launch file chooser.", e);
                }
            }
        });
    }

    //save file to drive
    private void save()
    {
        Log.d(TAG, "save: Save was pressed. ");
        Log.d(TAG, "Saving...");

        //display message if new note must be selected
        if (mDriveIdCurrent == null) {

            displayToast(R.string.msg_newnote);
            return;
        }

        //modify if the note this is default because it already exists in drive
        new DriveFileModify(mDriveClientResource) {

            //set title and note body to contents of edit fields
            @Override
            public Edits edit(DriveContents driveContents)
            {
                MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                        .setTitle(mEditTextTitle.getText().toString())
                        .build();
                try
                {
                    byte[] body = mEditTextNoteBody.getText().toString().getBytes();
                    driveContents.getOutputStream().write(body);

                } catch (IOException e)
                {
                    Log.e(TAG, "IOException while reading from driveContents output stream", e);
                }
                return new Edits(metadataChangeSet, driveContents);
            }

            //on save display resulsts of save
            @Override
            protected void onPostExecute(Boolean saveSuccess)
            {
                if (saveSuccess)
                {
                    Log.d(TAG, "onPostExecute: Save successful");
                    displayToast(R.string.msg_saved);
                } else
                {
                    Log.d(TAG, "onPostExecute: Error saving file.");
                    displayToast(R.string.msg_errsaving);
                }
            }
        }.execute(mDriveIdCurrent);
    }


}


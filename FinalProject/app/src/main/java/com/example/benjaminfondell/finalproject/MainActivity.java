package com.example.benjaminfondell.finalproject;

/**
 * Created by benjaminfondell on 11/24/17.
 */

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.drive.CreateFileActivityOptions;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.drive.query.SortOrder;
import com.google.android.gms.drive.query.SortableField;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;


public class MainActivity extends Activity {

    //main variables
    private static final String TAG = "Main-";
    private static final int REQUEST_SIGN_IN_CODE = 0;
    protected static final int REQUEST_TAKE_PICTURE_CODE = 1;
    private static final int REQUEST_NEW_NOTE_CODE = 2;
    protected static final int NEXT_REQUEST_CODE = 1;
    protected GoogleSignInClient mSignInClient;
    protected DriveClient mDriveClient;
    protected DriveResourceClient mDriveClientResource;
    protected Bitmap mPictureToSave;
    public DriveId mNewestFile;
    public  DriveId mDriveIdCurrent;
    public  Metadata mMetadata;
    public  DriveContents mDriveContents;
    public EditText mEditTextTitle;
    public  EditText mEditTextNoteBody;
    public Button mSaveButton;
    public DriveId mInitialFolder;

    //on create constructor
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreate: Creating main. Sign in user. ");
        super.onCreate(savedInstanceState);
        signIn();
        setContentView(R.layout.activity_main);
    }

    //sign in client
    protected void signIn()
    {
        Log.d(TAG, "signIn: Signing in to API");
        Log.i(TAG, "Start sign in");
        mSignInClient = buildSignInClient();
        startActivityForResult(mSignInClient.getSignInIntent(), REQUEST_SIGN_IN_CODE);
    }

    //sign out client on logout press
    protected void signOut()
    {
        Log.d(TAG, "signOut: Signing out of API");
        mSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        signIn();
                    }
                });
    }

    //check that user is signed in to drive
    public boolean isSignedIn()
    {
        Log.d(TAG, "isSignedIn: User is signed in already.");
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        return mSignInClient != null
                && (signInAccount != null
                && signInAccount.getGrantedScopes().contains(Drive.SCOPE_FILE));
    }

    //build client information and scope
    // NOTE: android drive is limited to file scope
    // for further scope of folders and other drive rescources,
    // must use rest api
    private GoogleSignInClient buildSignInClient()
    {
        Log.d(TAG, "buildSignInClient: Building sign in options");
        GoogleSignInOptions signInOptions =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestScopes(Drive.SCOPE_FILE)
                        .build();
        return GoogleSignIn.getClient(this, signInOptions);
    }

    //save image to drive
    private void savePicture()
    {
        Log.d(TAG, "savePicture: Save file to drive.");
        Log.i(TAG, "Creating new image contents.");
        final Bitmap picture = mPictureToSave;

        mDriveClientResource
                .createContents()
                .continueWithTask(
                        new Continuation<DriveContents, Task<Void>>()
                        {
                            @Override
                            public Task<Void> then(@NonNull Task<DriveContents> task) throws Exception
                            {
                                return createFileIntentSender(task.getResult(), picture);
                            }
                        });
    }

    //file intent creator for saving picture
    protected Task<Void> createFileIntentSender(DriveContents driveContents, Bitmap image) {

        Log.d(TAG, "createFileIntentSender: ");
        Log.i(TAG, "New contents created.");
        OutputStream outputStream = driveContents.getOutputStream();
        ByteArrayOutputStream imageStream = new ByteArrayOutputStream();

        //compress image for sending
        image.compress(Bitmap.CompressFormat.PNG, 100, imageStream);

        //write image to drive out stream
        try
        {
            outputStream.write(imageStream.toByteArray());
        }
        catch (IOException e)
        {
            Log.w(TAG, "Unable to write file contents.", e);
        }

        //set default date title for image
        Date currentTime = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDate =  new SimpleDateFormat("dd/MM/yyyy");
        String dateTitle = simpleDate.format(currentTime);

        //initialize file metadata
        MetadataChangeSet metadataChangeSet =
                new MetadataChangeSet.Builder()
                        .setMimeType("image/jpeg")
                        .setTitle(dateTitle +".png")
                        .build();

        //initialize file options
        CreateFileActivityOptions createFileActivityOptions =
                new CreateFileActivityOptions.Builder()
                        .setInitialMetadata(metadataChangeSet)
                        .setInitialDriveContents(driveContents)
                        .build();

        //send file to drive
        return mDriveClient
                .newCreateFileActivityIntentSender(createFileActivityOptions)
                .continueWith(
                        new Continuation<IntentSender, Void>()
                        {
                            @Override
                            public Void then(@NonNull Task<IntentSender> task) throws Exception
                            {
                                startIntentSenderForResult(task.getResult(), REQUEST_NEW_NOTE_CODE, null, 0, 0, 0);
                                return null;
                            }
                        });
    }


    // handle result
    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        Log.d(TAG, "onActivityResult: ");
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            // handle sign in
            case REQUEST_SIGN_IN_CODE:
                Log.i(TAG, "Sign in request code");

                if (resultCode == RESULT_OK) {
                    Log.i(TAG, "Signed in successfully.");

                    //use current account
                    mDriveClient = Drive.getDriveClient(this, GoogleSignIn.getLastSignedInAccount(this));

                    //get drive from client account
                    mDriveClientResource =
                            Drive.getDriveResourceClient(this, GoogleSignIn.getLastSignedInAccount(this));

                    //set up sorted file list from drive
                    SortOrder sortOrder = new SortOrder.Builder().addSortAscending(SortableField.MODIFIED_DATE).build();

                    //query drive for most recent file
                    Query query =
                            new Query.Builder()
                                    .addFilter(Filters.eq(SearchableField.MIME_TYPE, "text/plain"))
                                    .setSortOrder(sortOrder)
                                    .build();

                    Task<MetadataBuffer> queryTask = mDriveClientResource.query(query);

                    queryTask
                            .addOnSuccessListener(this,
                                    new OnSuccessListener<MetadataBuffer>() {

                                        @Override
                                        public void onSuccess(MetadataBuffer metadataBuffer) {

                                            //break if no file was found in query, create new note toast
                                            if(metadataBuffer.getCount() == 0){

                                                Log.d(TAG, "onFailure: No text file found in google drive.");
                                                displayToast(R.string.neednote_msg);
                                                return;
                                            }
                                            //if foyund set the displayed note to the most recent created note
                                            Log.d(TAG, "onSuccess: "+metadataBuffer.getCount());
                                            int count = metadataBuffer.getCount();
                                            count = count - 1;
                                            mNewestFile = metadataBuffer.get(count).getDriveId();
                                            Log.d(TAG, "onSuccess: "+ mNewestFile.encodeToString());
                                            mDriveIdCurrent = mNewestFile;
                                            getCurrentFile();
                                        }
                                    });
                }
                break;
            //handle picture
            case REQUEST_TAKE_PICTURE_CODE:
                Log.i(TAG, "capture image request code");

                if (resultCode == Activity.RESULT_OK)
                {
                    Log.i(TAG, "Image captured successfully.");

                    mPictureToSave = (Bitmap) data.getExtras().get("data");
                    savePicture();
                }
                break;
            //handle new note creation
            case REQUEST_NEW_NOTE_CODE:
                Log.i(TAG, "creator request code");

                if (resultCode == RESULT_OK)
                {
                    Log.i(TAG, "Image successfully saved.");
                    mPictureToSave = null;
                }
                break;
        }
    }

    //display the current file referenced by driveId
    public void getCurrentFile() {
        Log.d(TAG, "getCurrentFile: ");
        Log.d(TAG, "Retrieving...");
        //get file
        final DriveFile file = mDriveIdCurrent.asDriveFile();

        mDriveClientResource.getMetadata(file)
                .continueWithTask(new Continuation<Metadata, Task<DriveContents>>() {
                    @Override
                    public Task<DriveContents> then(@NonNull Task<Metadata> task)
                    {
                        if (task.isSuccessful())
                        {
                            mMetadata = task.getResult();
                            return mDriveClientResource.openFile(file, DriveFile.MODE_READ_ONLY);
                        } else
                        {
                            return Tasks.forException(task.getException());
                        }
                    }
                }).addOnSuccessListener(new OnSuccessListener<DriveContents>()
        {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onSuccess(DriveContents driveContents) {
                mDriveContents = driveContents;

                //if image file do not display
                if(!Objects.equals("image/jpeg",mMetadata.getMimeType())){
                    refreshUiFromCurrentFile();
                }

            }
        });
    }

    //refresh the app screen to the current file
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void refreshUiFromCurrentFile() {
        Log.d(TAG, "refreshUiFromCurrentFile: ");
        Log.d(TAG, "Refreshing...");

        //if no file do not display
        if (mDriveIdCurrent == null) {
            mSaveButton.setEnabled(false);
            return;
        }
        //allow user to save file
        mSaveButton.setEnabled(true);

        if (mMetadata == null){
            return;
        }

        //if nothing in file do not display
       if( mDriveContents == null) {
           return;
       }

        Log.d(TAG, "refreshUiFromCurrentFile: " + mMetadata.getMimeType());

        //if file is not an image set the file title and contents to app fields
        if(!Objects.equals("image/jpeg",mMetadata.getMimeType())) {

            Log.d(TAG, "refreshUiFromCurrentFile: The conditional did not work");

            //display title and body of note
            mEditTextTitle.setText(mMetadata.getTitle());
            try {
                String contents = readFileString(mDriveContents.getInputStream());
                mEditTextNoteBody.setText(contents);
            } catch (IOException e) {
                Log.e(TAG, "Error reading contents.", e);
                displayToast(R.string.msg_errreading);
                //do not allow user to save
                mSaveButton.setEnabled(false);
            }
        }
    }

    //display toasts to app
    public void displayToast(int id)
    {
        Log.d(TAG, "displayToast: ");
        Toast.makeText(this, id, Toast.LENGTH_LONG).show();
    }

    //helper for reading file strings
    //https://developer.android.com/reference/java/io/FileInputStream.html
    public String readFileString(InputStream is) throws IOException
    {
        BufferedReader fileReader = new BufferedReader(new InputStreamReader(is));
        StringBuilder stringBuilder = new StringBuilder();
        String fileLine;
        while ((fileLine = fileReader.readLine()) != null)
        {
            stringBuilder.append(fileLine);
        }
        return stringBuilder.toString();
    }

}
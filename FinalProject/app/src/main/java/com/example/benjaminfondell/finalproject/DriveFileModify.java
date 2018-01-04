package com.example.benjaminfondell.finalproject;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.tasks.Tasks;

import java.util.concurrent.ExecutionException;

//Helper activity for making asynchronous calls to the api to edit the files
// the following was built based on the below post
// https://stackoverflow.com/questions/37407368/android-drive-api-download-file/37567310
public abstract class DriveFileModify extends AsyncTask<DriveId, Boolean, Boolean>
{

    private static final String TAG = "EditFileBackground";
    private DriveResourceClient driveResourceClient;
    public abstract Edits edit(DriveContents driveContents);

    //Constructor
    public DriveFileModify(DriveResourceClient driveResourceClient)
    {
        this.driveResourceClient = driveResourceClient;
    }

    //class variable constructor
    public class Edits
    {
        private MetadataChangeSet mMetadataChangeSet;
        private DriveContents mDriveContents;
        public MetadataChangeSet getMetadataChangeSet()
        {
            return mMetadataChangeSet;
        }
        public DriveContents getDriveContents()
        {
            return mDriveContents;
        }
        public Edits(MetadataChangeSet metadataChangeSet, DriveContents contents)
        {
            mMetadataChangeSet = metadataChangeSet;
            mDriveContents = contents;
        }
    }

    @Override
    protected Boolean doInBackground(DriveId... params)
    {
        //get passed driveid
        DriveFile file = params[0].asDriveFile();

        //edit contents of fle in drive
        try {
            DriveContents contents =
                    Tasks.await(driveResourceClient.openFile(file, DriveFile.MODE_WRITE_ONLY));

            Edits changes = edit(contents);
            MetadataChangeSet setChanges = changes.getMetadataChangeSet();
            DriveContents updateNoteBody = changes.getDriveContents();

            if (setChanges != null)
            {
                Tasks.await(driveResourceClient.updateMetadata(file, setChanges));
            }

            if (updateNoteBody != null)
            {
                Tasks.await(driveResourceClient.commitContents(updateNoteBody, setChanges));
            }
        } catch (ExecutionException | InterruptedException e)
        {
            Log.e(TAG, "Error editing file.", e);
            return false;
        }

        return true;
    }
}

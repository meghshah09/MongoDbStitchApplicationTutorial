package com.example.mongodbstitchapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.text.format.DateUtils;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.core.auth.StitchUser;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteFindIterable;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;
import com.mongodb.stitch.core.auth.providers.anonymous.AnonymousCredential;
import com.mongodb.stitch.core.services.mongodb.remote.RemoteInsertOneResult;

import org.bson.Document;
import java.util.*;

public class MainActivity extends AppCompatActivity {
    //StitchAppClient stitchAppClient = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Stitch.initializeDefaultAppClient(
                getResources().getString(R.string.my_app_id)
        );

        final StitchAppClient stitchAppClient = Stitch.getDefaultAppClient();
        stitchAppClient.getAuth().loginWithCredential(new AnonymousCredential()).addOnSuccessListener(new OnSuccessListener<StitchUser>() {
            @Override
            public void onSuccess(final StitchUser stitchUser) {
                // 2. Instantiate a RemoteMongoClient
                final RemoteMongoClient mongoClient = stitchAppClient.getServiceClient(RemoteMongoClient.factory, "mongodb-atlas");
                RemoteMongoCollection<Document> myCollection = mongoClient.getDatabase("test").getCollection("my_collection");
                Document doc = new Document();
                doc.append("time",new Date().getTime());
                doc.append("user_id", stitchUser.getId());
                myCollection.insertOne(doc).addOnSuccessListener(new OnSuccessListener<RemoteInsertOneResult>() {
                    @Override
                    public void onSuccess(RemoteInsertOneResult remoteInsertOneResult) {
                        Log.d("STITCH", "One document inserted");
                    }
                });

                final RemoteFindIterable<Document> query = myCollection.find().sort(new Document("time", -1)).limit(5);
                final ArrayList<Document> result = new ArrayList<Document>();
                query.into(result).addOnSuccessListener(new OnSuccessListener<ArrayList>() {
                    @Override
                    public void onSuccess(ArrayList arrayList) {
                        Log.d("Query",result.toString());
                        String output="";
                        for(Document d : result){
                             output = output + "You opened this app: ";
                            // Loop through the results
                            output = (String) (output + DateUtils.getRelativeDateTimeString(MainActivity.this,
                                    (long)d.get("time"), DateUtils.SECOND_IN_MILLIS, DateUtils.WEEK_IN_MILLIS, 0));
                            output = output+"\n\n";

                        }
                        TextView myAwesomeTextView = (TextView)findViewById(R.id.viewer);

                        //in your OnCreate() method
                        myAwesomeTextView.setText(output);
                    }
                });


            }

        });
    }
}

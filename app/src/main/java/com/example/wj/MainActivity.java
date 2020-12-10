package com.example.wj;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashSet;

public class MainActivity extends AppCompatActivity {

    static ArrayList<String> notes;
    static ArrayList<String> files = new ArrayList<String>();
    static ArrayAdapter<String> adapter; //helps put the arraylist into listview
    static ListView listView;
    private Class NoteEditorActivity;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        notes = new ArrayList<>(); //define here to ensure refresh upon reentry to MainActivity


        listView = (ListView)findViewById(R.id.listView); //where all the notes are displayed
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("com.example.wj.MainActivity", Context.MODE_PRIVATE); //stores file names as preferences
        HashSet<String> set = (HashSet<String>)sharedPreferences.getStringSet("files", null); //load info in sharedprefs into a hashset, then add it to arraylist later


        Button button = findViewById(R.id.add_entry); //this is the plus button
        button.setOnClickListener(v ->{
            Intent intent = new Intent(getApplicationContext(), NoteEditorActivity.class);
            startActivity(intent); //opens text entry area
        });

        Button button2 = findViewById(R.id.button_first); //Opens weather app
        button2.setOnClickListener(v ->{
            Intent intent = new Intent(getApplicationContext(), Weather.class);
            startActivity(intent);
        });



        if(set == null)
        {
            //notes.add("Test");

        }

        else
        {
            files = new ArrayList<>(set);

        }
        for(int i = 0; i<files.size(); i++)
        {
            try {
                notes.add(readFromFile(files.get(i))); //for each file in files, read contents
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ArrayList<String> notes2 = new ArrayList<>(); //this is used to store where the notes originally were before sorting. used to delete corresponding filenames.
        for(int d = 0; d<notes.size(); d++)
        {
            notes2.add(notes.get(d));
        }

        for(int i = 0; i<notes.size()-1; i++)
        {
            for(int j = 0; j<notes.size()-i-1; j++)
            {
                if(notes.get(j).compareTo(notes.get(j+1))<0)
                {
                    String temp = notes.get(j);
                    notes.set(j, notes.get(j+1));
                    notes.set(j+1, temp);
                }
            }
        }

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, notes);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() { //when an entry is clicked
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Intent intent = new Intent(getApplicationContext(), NoteEditorActivity.class);
                intent.putExtra("noteID", position); //this gives context in terms of what entry was clicked which likewise tells us what text should be loaded into the dialog box to edit
                startActivity(intent);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() { //longclick
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id)
            {
                new AlertDialog.Builder(MainActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Delete?")
                        .setMessage("Are you sure you want to delete this note?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                int indexrem = 0;
                                for(int i = 0; i<notes2.size(); i++)
                                {
                                    if(notes2.get(i).equals(notes.get(position)))
                                    {
                                        indexrem = i;
                                    }
                                }
                                notes.remove(position);
                                deleteFile(files.get(indexrem)); //remove file
                                files.remove(indexrem); //remove filename from arraylist


                                adapter.notifyDataSetChanged();


                                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("com.example.wj.MainActivity", Context.MODE_PRIVATE);
                                HashSet<String> set = new HashSet<>(MainActivity.files);
                                sharedPreferences.edit().putStringSet("files", set).apply(); //update filenames to sharedprefs
                            }
                        })

                        .setNegativeButton("No", null)
                        .show();

                return true;               // this was initially false but we change it to true as if false, the method assumes that we want to do a short click after the long click as well
            }
        });
    }



    public String readFromFile(String filename) throws IOException { //read the file
        String result = "";
        InputStream inputStream = openFileInput(filename);
        if(inputStream != null)
        {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String temp = "";
            StringBuilder stringBuilder = new StringBuilder();

            while((temp = bufferedReader.readLine()) != null)
            {
                stringBuilder.append(temp);
                stringBuilder.append("\n");
            }

            inputStream.close();
            result = stringBuilder.toString();
        }
        return result;
    }

    public void writeToFile(String filename, String message) //write to the file
    {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput(filename,
                    Context.MODE_PRIVATE));
            outputStreamWriter.write(message);
            outputStreamWriter.close();

        } catch(FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
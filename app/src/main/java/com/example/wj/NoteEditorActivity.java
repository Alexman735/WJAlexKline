package com.example.wj;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashSet;
import java.util.Random;

@RequiresApi(api = Build.VERSION_CODES.O)
public class NoteEditorActivity extends AppCompatActivity {
    int noteID; //noteID is the position of the note to be modified in the arraylist as specified by the intent, defaults to -1(new note)
    public static String r; //string of fully edited text
    public static String getTime() //this timestamps when the file was made, presents that info to user, and is used for sorting
    {
        LocalDateTime now = LocalDateTime.now();
        String ampm = "a.m.";
        int gethour = now.getHour();
        int getminute = now.getMinute();
        String gms = getminute+"";
        if(gethour > 12)
        {
            gethour = gethour-12;
            ampm = "p.m.";
        }
        if(getminute<10)
        {
            gms = "0"+gms;
        }

        String format = now.getMonthValue()+"/"+now.getDayOfMonth()+"/"+now.getYear()+"   "+gethour+":"+gms+" "+ampm+" -   ";
        return format;
    }

    public static void addNewNote(String add) //upon creation of a note, the newest note will be the first displayed
    {
        MainActivity.notes.add("");
        for(int i = MainActivity.notes.size() - 1; i>=1; i--)
        {
            MainActivity.notes.set(i, MainActivity.notes.get(i-1));
        }
        MainActivity.notes.set(0, add);
    }

    public static void changeNote(int pos, String newtext) //upon edit of a note, it is re-timestamped and readded
    {
        MainActivity.notes.remove(pos);
        addNewNote(newtext);
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_editor); //brings up xml w/ textbox

        Button button = findViewById(R.id.done);
        button.setOnClickListener(v ->{
            Random ra = new Random();
            double rand = ra.nextDouble(); //generates a random double for file-creation

            String filename = "file"+rand+".txt";
            writeToFile(filename, r);
            MainActivity.files.add(filename);



            SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("com.example.wj.MainActivity", Context.MODE_PRIVATE);
            HashSet<String> set = new HashSet<>(MainActivity.files); //stores new filename in sharedprefs
            sharedPreferences.edit().putStringSet("files", set).apply();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent); //opens text entry area
        });

        EditText editText = (EditText)findViewById(R.id.editText); //this is the text edit box popup
        Intent intent = getIntent(); //collects intent from MainActivity
        /*
        value indicates whether intent was to create a new note or edit an old one.
        Defaults to -1, which is creating a new note. otherwise, this value is the position of
        the note that was clicked as specified by the short click intent*/
        noteID = intent.getIntExtra("noteID", -1);


        if(noteID != -1) //if the intent had val !=-1, then not a new note. text from notes arraylist loaded into box for editing purposes
        {
            String s = MainActivity.notes.get(noteID);
            String[] strarr = s.split("-", 2);
            String newstr = strarr[1].substring(3);
            editText.setText(newstr);
        }

        else //otherwise, new note. the editor starts off with an empty note to edit, and a new empty entry is added to notes arraylist to be modified later.
        {
            addNewNote(getTime());
            noteID = 0;
            MainActivity.adapter.notifyDataSetChanged();
        }


        editText.addTextChangedListener(new TextWatcher() //this handy function listens for a change in the text at which point it saves it
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
                    //leave this blank, no action required
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                r = getTime()+String.valueOf(s);
                changeNote(noteID, r);
                noteID = 0;
                MainActivity.adapter.notifyDataSetChanged(); //make sure edits to the entry are displayed to user


            }

            @Override
            public void afterTextChanged(Editable s)
            {
                    //Leave this blank, no action required.
            }
        });
    }
    public void writeToFile(String filename, String message)
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
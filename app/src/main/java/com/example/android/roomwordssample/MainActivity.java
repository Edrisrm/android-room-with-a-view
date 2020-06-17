package com.example.android.roomwordssample;

/*
 * Copyright (C) 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;


public class MainActivity extends AppCompatActivity {

    public static final int NEW_WORD_ACTIVITY_REQUEST_CODE = 1;
    public static  final int EDIT_WORD_ACTIVITY_REQUEST_CODE = 2;

    private WordViewModel mWordViewModel;
    private Button editButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);//appbarr
        setSupportActionBar(toolbar);

        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        final WordListAdapter adapter = new WordListAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Get a new or existing ViewModel from the ViewModelProvider.
        //tomo un nuevo o existente viewmodel del proveddor, osear wordviewmodel
        mWordViewModel = ViewModelProviders.of(this).get(WordViewModel.class);

        // Add an observer on the LiveData returned by getAlphabetizedWords.
        // The onChanged() method fires when the observed data changes and the activity is
        // in the foreground.
        mWordViewModel.getAllWords().observe(this, new Observer<List<Word>>() {
            @Override
            public void onChanged(@Nullable final List<Word> words) {
                // Update the cached copy of the words in the adapter.
                adapter.setWords(words);
            }
        });
        recyclerView.addOnItemTouchListener(
                new RecyclerViewClickItemListener(this, new RecyclerViewClickItemListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, final int position) {
                        editButton = findViewById(R.id.button_edit);

                        editButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Toast.makeText(getApplicationContext(), "Edit button is called"+adapter.getWordAt(position).getWord() , Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(MainActivity.this, NewWordActivity.class);
                                intent.putExtra(ApplicationString.EXTRA_REPLY_WORD_ID, adapter.getWordAt(position).getId());
                                intent.putExtra(ApplicationString.EXTRA_REPLY, adapter.getWordAt(position).getWord());

                                startActivityForResult(intent, EDIT_WORD_ACTIVITY_REQUEST_CODE);
                            }
                        });
                    }
                })
        );


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, NewWordActivity.class);
                startActivityForResult(intent, NEW_WORD_ACTIVITY_REQUEST_CODE);
            }
        });
        ItemTouchHelper helper = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(0,
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(RecyclerView recyclerView,
                                          RecyclerView.ViewHolder viewHolder,
                                          RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder,
                                         int direction) {
                        int position = viewHolder.getAdapterPosition();
                        Word myWord = adapter.getWordAtPosition(position);
                        Toast.makeText(MainActivity.this, "Deleting " +
                                myWord.getWord(), Toast.LENGTH_LONG).show();

                        // Delete the word
                        mWordViewModel.deleteWord(myWord);
                    }
                });

        helper.attachToRecyclerView(recyclerView);
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
                if (requestCode == NEW_WORD_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
                    Word word = new Word(data.getStringExtra(NewWordActivity.EXTRA_REPLY));
                    mWordViewModel.insert(word);
                } else if (requestCode == EDIT_WORD_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
                    int id = data.getIntExtra(ApplicationString.EXTRA_REPLY_WORD_ID, -1);
                    if (id == -1){
                        Toast.makeText(this, "Note can't be updated", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String palabra = data.getStringExtra(ApplicationString.EXTRA_REPLY);

                    Word word = new Word(palabra);
                    word.setId(id);
                    mWordViewModel.update(word);
                    Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this, "Note not saved", Toast.LENGTH_SHORT).show();
                }

        }

}

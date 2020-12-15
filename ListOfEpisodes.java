package com.hfad.mypodcastplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ListOfEpisodes extends AppCompatActivity {

    ListView lvRss;
    ArrayList<String> titles;

    ArrayList<String> links;
    public static final String LOG_TAG = "URI Intent";
    private String Urldata;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listofepisodes);


        titles = new ArrayList<>();
        links = new ArrayList<>();
        lvRss = findViewById(R.id.lvRss);
        lvRss.setOnItemClickListener(new AdapterView.OnItemClickListener() {


            @Override
            public void onItemClick(AdapterView<?> parent, View view, int posistion, long id) {
                //Når der klikkes på den viste episode sendes man videre til MediaPLayer som bruger linket til at afspille

                Log.d("Uri56", "posistion " + posistion);

                Log.d("Uri56", "links" + links.toString());

                Uri uri = Uri.parse(links.get(posistion));
                Log.d("Uri56", links.get(posistion));
                Log.d("Uri56", "Uri56" + uri.toString());
                Uri episodetitle = Uri.parse(titles.get(posistion));
                Intent intent = new Intent(ListOfEpisodes.this, MediaPlayer.class);

                intent.putExtra(MediaPlayer.URLTEST, links.get(posistion));
                Log.d("testUri", "testuri" + uri.toString());

                intent.putExtra(MediaPlayer.TITLE, titles.get(posistion));
                startActivity(intent);
                Log.d(LOG_TAG, "putExtra " + uri.toString());

            }
        });

        new ProcessInBackground().execute();
        Log.d(LOG_TAG, titles.toString());
    }

    public InputStream getInputStream(URL url) throws IOException {
        return url.openConnection().getInputStream();
    }

    public class ProcessInBackground extends AsyncTask<Integer, Void, Exception> {

        //Viser en besked   mens data hentes
        ProgressDialog progressDialog = new ProgressDialog(ListOfEpisodes.this);
        Exception exception = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog.setMessage("Igang med at hente RSS feed. Vent venligst....");
            progressDialog.show();
        }

        @Override
        protected Exception doInBackground(Integer... params) {

            try {
                //Her oprettes et objekt af URL
                URL url = new URL("https://www.dr.dk/mu/feed/genstart.xml?format=podcast");
                // bruges i forbindelse med at hive data fra Xml dokumentet
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                //Under støtter ikke XML namespaces
                factory.setNamespaceAware(false);
                // bruger factory som nu er i stand til at bruge informationen fra XML filen
                XmlPullParser xpp = factory.newPullParser();
                //Tager URL object som er et XML dokument og er UTF encoded
                xpp.setInput(getInputStream(url), "UTF_8");
                //sætter om programmet læser inde i <item> tag
                boolean InsideItem = false;
                // tjekker om det er et åbent eller lukket tag
                int eventType = xpp.getEventType();
                // While lykke som løber igennem XML dokumentet ind til det slutter
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    // Leder efter start tag <>
                    if (eventType == XmlPullParser.START_TAG) {
                        //getName giver navnet på et bestemt tag<>
                        if (xpp.getName().equalsIgnoreCase("item")) {
                            InsideItem = true;
                        }
                        //Tjekker at vi er inde i <item> tag og bruger <title> tag
                        else if (xpp.getName().equalsIgnoreCase("title")) {
                            if (InsideItem) {
                                titles.add(xpp.nextText());
                            }
                        }
                        // Tjekker at tag er <url> og at det er inde i <item>
                        else if (xpp.getName().equalsIgnoreCase("enclosure")) {
                            if (InsideItem) {
                                links.add(xpp.getAttributeValue(0));
                            }
                        }

                    }
                    // Løber i gennem item tag og tjekke om det er lukket og sætter InsideItem False
                    else if (eventType == XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("item")) {
                        InsideItem = false;
                    }
                    // Sørger før at hele <item> tag bliver løbet igennem selv om det støder på andre tag navne end <title> og <link>
                    eventType = xpp.next();

                }

            }
            // Tjekker om URL valid
            catch (MalformedURLException e) {
                exception = e;

            }
            // Når der udtrækkes data fra XML filen og de ikke er valid
            catch (XmlPullParserException e) {
                exception = e;
            }
            // Input Output Exeption
            catch (IOException e) {
                exception = e;
            }
            return exception;

        }

        @Override
        protected void onPostExecute(Exception e) {
            super.onPostExecute(e);
            //Opretter en Arrayadapt som indsætter  titles i listview
            ArrayAdapter<String> adapter = new ArrayAdapter<>(ListOfEpisodes.this, android.R.layout.simple_list_item_1, titles);
            //Sætter dataene fra adaptere ind i ListView
            lvRss.setAdapter(adapter);
            progressDialog.dismiss();

        }

        @Override
        protected void onCancelled(Exception e) {
            super.onCancelled(e);
        }
    }

}
package com.elead1.RandomBeer;

/**
 * @author Eric Leadbetter
 */
import android.app.*;
import android.graphics.drawable.Drawable;
import android.os.*;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.*;
import android.widget.*;
import android.util.Log;
import java.util.Random;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Scanner;

public class MainActivity extends Activity
{
	Button picker;
	TextView choice;
	ImageView image_field;
	HashMap<String, String> beers;
	Random rng;
	Scanner file_reader;
	String image_url_base;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        
        /*Try to open the list of beers. If it fails, we're done here.*/
		try {
			file_reader = new Scanner(getAssets().open("beer_list.txt"));
		} catch (IOException e) {
			Log.e(getResources().getString(R.string.app_name), e.getMessage());
			finish();
		}
		
		//Make a new random number generator, seeded with current time.
        rng = new Random(new java.util.Date().getTime());
        //Make a hashmap to store the beer names and their urls
		beers = new HashMap<String, String>();
		
		//Disable the button until after we import the beer list
		picker = (Button)findViewById(R.id.beer_button);
		picker.setEnabled(false);
		
		//Format the TextView and make its links clickable
		choice =  (TextView)findViewById(R.id.beer_choice_text);
		choice.setMovementMethod(LinkMovementMethod.getInstance());
		choice.setGravity(Gravity.CENTER);
		choice.setTextSize((float) 40.0);
		
		image_field = (ImageView)findViewById(R.id.image_field);
		image_url_base = "http://beeradvocate.com/im/beers/";

		//Load the beers!
		load_beer(file_reader);
		
		//Provide the button with it's click action
		picker.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v)
			{
				//Decide on a beer from the HashMap using the RNG
				String beer = (String)beers.keySet().toArray()[rng.nextInt(beers.size())];
				//Split the beer's url up so we can build the image URL
				String [] url_components = beers.get(beer).split("/");
				
				//Set the text in the TextView to a link to the BeerAdvocate page
				choice.setText(Html.fromHtml("<a href=\"" + beers.get(beer) + "\">" + beer + "</a>"));
				
				//If this beer came with a URL, get the image and render it in the ImageView
				if(url_components.length == 7)
				{
					//This is done using a private class that enables asynchronous processing, to avoid locking
					//the main thread (required by Android, I didn't come up with this).
					new DownloadImageTask().execute(url_components[6]);
				}
			}
			
			class DownloadImageTask extends AsyncTask<String, Void, Drawable>{

				@Override
				//Does the actual 'getting' of the image
				protected Drawable doInBackground(String... arg0) {
					// TODO Auto-generated method stub
					Object content = null;
					try{
						//Pull the image down
						java.net.URL image_url = new java.net.URL(image_url_base + arg0[0] + ".jpg");
						content = image_url.getContent();
						
					}
					catch(MalformedURLException mue)
					{
						Log.e("RandomBeer", mue.getMessage());
					}
					catch(IOException ioe)
					{
						Log.e("RandomBeer", ioe.getMessage());
					}
					//Create an actual drawable image
					InputStream is = (InputStream)content;
					Drawable image = Drawable.createFromStream(is, "BeerAdvocate");
					return image;
				}
				
				@Override
				//Runs in the UI thread after execution. Sets the ImageView's content.
				protected void onPostExecute(Drawable result)
				{
					image_field.setImageDrawable(result);
				}
			}
		});
    }
    
    //Small method to load the beer list from the file into the HashMap
    public void load_beer(Scanner sc)
    {
    	while(sc.hasNext())
    	{
    		String line = sc.nextLine();
    		String[] beer = line.split(",");
    		beers.put(beer[0], (beer.length == 2 ? beer[1] : ""));
    	}
    	//Enable the button now that we're done loading.
    	picker.setEnabled(true);
    }
}

